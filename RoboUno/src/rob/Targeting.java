package rob;

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;

import com.fuzzylite.Engine;
import com.fuzzylite.activation.General;
import com.fuzzylite.defuzzifier.Centroid;
import com.fuzzylite.norm.s.Maximum;
import com.fuzzylite.norm.t.AlgebraicProduct;
import com.fuzzylite.norm.t.Minimum;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Spike;
import com.fuzzylite.term.Gaussian;
import com.fuzzylite.term.Ramp;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;

public class Targeting extends AdvancedRobot {
	
	int lastHit;
	
	public void initialize(){
		setBodyColor(Color.red);
		setGunColor(Color.blue);
		setRadarColor(Color.green);
		setScanColor(Color.red);
		lastHit = 0;
	}
	
	public void run() {

		while (true) {
	
		 Engine engine = new Engine();
	     engine.setName("TargetingMoving");
	     engine.setDescription("");

	     InputVariable heat = new InputVariable();
	     heat.setName("heat");
	     heat.setDescription("");
	     heat.setEnabled(true);
	     heat.setRange(0.000, 3.000); 
	     heat.setLockValueInRange(false);
	     heat.addTerm(new Ramp("hot", 1.00, 3.10));
	     heat.addTerm(new Gaussian("middle", 1.00, 1.20));
	     heat.addTerm(new Ramp("cold", 0.20, 0.00));
	     engine.addInputVariable(heat);
	        
	     InputVariable hit = new InputVariable();
	     hit.setName("hit");
	     hit.setDescription("");
	     hit.setEnabled(true);
	     hit.setRange(0.000, 40.000); 
	     hit.setLockValueInRange(false);
	     hit.addTerm(new Gaussian("long", 40.0, 14.0));
	     hit.addTerm(new Gaussian("middle", 25, 20));
	     hit.addTerm(new Gaussian("short", 4, 6));;
	     engine.addInputVariable(hit);

	     OutputVariable target = new OutputVariable();
	     target.setName("target");
	     target.setDescription("");
	     target.setEnabled(true);
	     target.setRange(0, 900); 
	     target.setLockValueInRange(false);
	     target.setAggregation(new Maximum());
	     target.setDefuzzifier(new Centroid(100));
	     target.setDefaultValue(Double.NaN);
	     target.setLockPreviousValue(false);
	     target.addTerm(new Gaussian("high", 750, 450));
	     target.addTerm(new Gaussian("low", 200, 350));
	     engine.addOutputVariable(target);
	     
	     OutputVariable move = new OutputVariable();
	     move.setName("move");
	     move.setDescription("");
	     move.setEnabled(true);
	     move.setRange(0, 500);
	     move.setLockValueInRange(false);
	     move.setAggregation(new Maximum());
	     move.setDefuzzifier(new Centroid(100));
	     move.setDefaultValue(Double.NaN);
	     move.setLockPreviousValue(false);
	     move.addTerm(new Spike("high", 350, 160));
	     move.addTerm(new Spike("middle", 250, 150));
	     move.addTerm(new Spike("small", 50, 100));
	     engine.addOutputVariable(move);

	     RuleBlock mamdani = new RuleBlock();
	     mamdani.setName("mamdani");
	     mamdani.setDescription("");
	     mamdani.setEnabled(true);
	     mamdani.setConjunction(null);
	     mamdani.setDisjunction(null);
	     mamdani.setImplication(new AlgebraicProduct());
	     mamdani.setConjunction(new Minimum());
	     mamdani.setDisjunction(new Maximum());
	     mamdani.setImplication(new Minimum());
	     mamdani.setActivation(new General());
	     
	     mamdani.addRule(Rule.parse("if heat is hot then target is very low and move is high", engine));
	     mamdani.addRule(Rule.parse("if heat is middle and hit is short then target is low and move is middle", engine));
	     mamdani.addRule(Rule.parse("if heat is middle and hit is long then target is high and move is middle", engine));
	     mamdani.addRule(Rule.parse("if heat is cold and hit is long then target is very high and move is very small", engine));
	     mamdani.addRule(Rule.parse("if heat is cold and hit is middle then target is high and move is small", engine));
	     
	     engine.addRuleBlock(mamdani);
	     
	     heat = engine.getInputVariable("heat");
         hit = engine.getInputVariable("hit");
         OutputVariable targeting = engine.getOutputVariable("target");
         OutputVariable moving = engine.getOutputVariable("move");
         
         heat.setValue(getGunHeat());
         hit.setValue(lastHit);
         engine.process();
        
         ahead(moving.getValue());
		 ahead(-moving.getValue());
		 turnGunRight((int)targeting.getValue());
		 
		 lastHit++;
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		if (e.getDistance() < 50 && getEnergy() > 50) {
			fire(3);
		}
		else {
			fire(2);
		}
	}
	
	public void onBulletHit(){
		lastHit=0;
	}


	public void onHitRobot(HitRobotEvent e) {
		if (e.getBearing() > -10 && e.getBearing() < 10) {
			fire(3);
		}
		if (e.isMyFault()) {
			turnRight(10);
		}
	}
}
package rob;

import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;

import java.awt.*;

import com.fuzzylite.Engine;
import com.fuzzylite.activation.General;
import com.fuzzylite.defuzzifier.Centroid;
import com.fuzzylite.norm.s.Maximum;
import com.fuzzylite.norm.t.AlgebraicProduct;
import com.fuzzylite.norm.t.Minimum;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Gaussian;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;


public class FirePower extends Robot {

	boolean peek; 
	double moveAmount; 

	public void run() {
		
		setColors(Color.red,Color.blue,Color.green);

		moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		peek = false;

		turnLeft(getHeading() % 90);
		ahead(moveAmount);
		peek = true;
		turnGunRight(90);
		turnRight(90);

		while (true) {
			peek = true;
			ahead(moveAmount);
			peek = false;
			turnRight(90);
		}
	}

	public void onHitRobot(HitRobotEvent e) {
		if (e.getBearing() > -90 && e.getBearing() < 90) {
			back(100);
		}
		else {
			ahead(100);
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		 Engine engine = new Engine();
	     engine.setName("Fire");
	     engine.setDescription("");

	     InputVariable distance = new InputVariable();
	     distance.setName("distance");
	     distance.setDescription("");
	     distance.setEnabled(true);
	     distance.setRange(0.00, 300.00);
	     distance.setLockValueInRange(false);
	     distance.addTerm(new Gaussian("short", 30.00, 50.00));
	     distance.addTerm(new Gaussian("middle", 150.00, 90.00));
	     distance.addTerm(new Gaussian("long", 250.00, 60.00));
	     engine.addInputVariable(distance);
	        
	     InputVariable energy = new InputVariable();
	     energy.setName("energy");
	     energy.setDescription("");
	     energy.setEnabled(true);
	     energy.setRange(0.000, 100.000); 
	     energy.setLockValueInRange(false);
	     energy.addTerm(new Gaussian("low", 25.00, 30.00));
	     energy.addTerm(new Gaussian("middle", 50.00, 30.00));
	     energy.addTerm(new Gaussian("high", 75.00, 30.00));
	     engine.addInputVariable(energy);

	     OutputVariable power = new OutputVariable();
	     power.setName("power");
	     power.setDescription("");
	     power.setEnabled(true);
	     power.setRange(Rules.MIN_BULLET_POWER, Rules.MAX_BULLET_POWER); //0.1-3.0
	     power.setLockValueInRange(false);
	     power.setAggregation(new Maximum());
	     power.setDefuzzifier(new Centroid(100));
	     power.setDefaultValue(Double.NaN);
	     power.setLockPreviousValue(false);
	     power.addTerm(new Gaussian("high", 2.5, 0.6));
	     power.addTerm(new Gaussian("low", 0.6, 0.6));
	     engine.addOutputVariable(power);

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
	     
	     mamdani.addRule(Rule.parse("if distance is short and energy is high then power is very high", engine));  
	     mamdani.addRule(Rule.parse("if distance is long and energy is low then power is very low", engine));
	     mamdani.addRule(Rule.parse("if distance is long or energy is low then power is low", engine));
	     mamdani.addRule(Rule.parse("if distance is middle and energy is high then power is high", engine));
	     mamdani.addRule(Rule.parse("if distance is short and energy is middle then power is high", engine));
	     
	     engine.addRuleBlock(mamdani);
     
        distance = engine.getInputVariable("distance");
	    energy = engine.getInputVariable("energy");
	    OutputVariable steer = engine.getOutputVariable("power");
	        
	    distance.setValue(e.getDistance());
	    energy.setValue(getEnergy());
	    engine.process();
	        
	    fire(steer.getValue());
		
		if (peek) {
			scan();
		}
	}
}


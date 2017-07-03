package rob;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

import java.awt.*;

import com.fuzzylite.*;
import com.fuzzylite.activation.General;
import com.fuzzylite.defuzzifier.Centroid;
import com.fuzzylite.norm.s.Maximum;
import com.fuzzylite.norm.t.AlgebraicProduct;
import com.fuzzylite.norm.t.Minimum;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Spike;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;

public class Movement extends AdvancedRobot {
	
	int rightLeft;
	
	public void run() {
		
		initialize();
		
		while (true) {
			Engine engine = new Engine();
		     engine.setName("Moving");
		     engine.setDescription("");

		     InputVariable robots = new InputVariable();
		     robots.setName("robots");
		     robots.setDescription("");
		     robots.setEnabled(true);
		     robots.setRange(0.00, 25.00); 
		     robots.setLockValueInRange(false);
		     robots.addTerm(new Spike("little", 4.00, 5.00));
		     robots.addTerm(new Spike("middle", 10.00, 6.00));
		     robots.addTerm(new Spike("many", 20.00, 6.00));
		     engine.addInputVariable(robots);
		        
		     InputVariable energy = new InputVariable();
		     energy.setName("energy");
		     energy.setDescription("");
		     energy.setEnabled(true);
		     energy.setRange(0.000, 100.000); 
		     energy.setLockValueInRange(false);
		     energy.addTerm(new Spike("low", 25.00, 30.00));
		     energy.addTerm(new Spike("middle", 50.00, 30.00));
		     energy.addTerm(new Spike("high", 75.00, 30.00));
		     engine.addInputVariable(energy);

		     OutputVariable move = new OutputVariable();
		     move.setName("move");
		     move.setDescription("");
		     move.setEnabled(true);
		     move.setRange(0, 5000);
		     move.setLockValueInRange(false);
		     move.setAggregation(new Maximum());
		     move.setDefuzzifier(new Centroid(100));
		     move.setDefaultValue(Double.NaN);
		     move.setLockPreviousValue(false);
		     move.addTerm(new Spike("high", 4000, 1000));
		     move.addTerm(new Spike("middle", 2500, 1500));
		     move.addTerm(new Spike("small", 500, 1000));
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
		     
		     mamdani.addRule(Rule.parse("if robots is little and energy is low then move is very high", engine));
		     mamdani.addRule(Rule.parse("if robots is little and energy is middle then move is high", engine));
		     mamdani.addRule(Rule.parse("if robots is middle and energy is low then move is high", engine));
		     mamdani.addRule(Rule.parse("if robots is little and energy is high then move is middle", engine));
		     mamdani.addRule(Rule.parse("if robots is many and energy is low then move is middle", engine));
		     mamdani.addRule(Rule.parse("if robots is many and energy is high then move is very small", engine));  
		     mamdani.addRule(Rule.parse("if robots is many and energy is middle then move is small", engine));
		     mamdani.addRule(Rule.parse("if robots is middle and energy is high then move is small", engine));
		     
		     engine.addRuleBlock(mamdani);

	        robots = engine.getInputVariable("robots");
	        energy = engine.getInputVariable("energy");
	        OutputVariable steer = engine.getOutputVariable("move");
	        
	        robots.setValue(getOthers());
	        energy.setValue(getEnergy());
	        engine.process();
	        
			setTurnRight(steer.getValue()*rightLeft);
			setMaxVelocity(7);
			ahead(steer.getValue());
			turnGunRight(360);
			
			rightLeft *= -1;
			}
	}
	
	public void initialize(){
		setBodyColor(Color.black);
		setGunColor(Color.blue);
		setRadarColor(Color.red);
		setScanColor(Color.red);
		rightLeft = 1;
	}
	
	public void onHitWall(HitWallEvent e) {
		// Bounce off!
		int turn = (int)getHeading();
		turn = turn%90;
		turnLeft((90+turn)*rightLeft);
		ahead(170);
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if (e.getDistance() < 50 && getEnergy() > 50) {
			fire(3);
		}
		else {
			fire(2);
		}
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

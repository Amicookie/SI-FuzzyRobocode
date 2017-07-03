package rob;

import com.fuzzylite.Engine;
import com.fuzzylite.FuzzyLite;
import com.fuzzylite.Op;
import com.fuzzylite.activation.General;
import com.fuzzylite.defuzzifier.Centroid;
import com.fuzzylite.norm.s.Maximum;
import com.fuzzylite.norm.t.AlgebraicProduct;
import com.fuzzylite.norm.t.Minimum;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Triangle;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;

import robocode.Rules;

/*
 * Author: Karolina Zaborowska
 * 1.0 @ 2017-07-02
 * 
 */


public class FuzzyRobotControl {
	
	//----------------------------------------initial VARIABLES----------------------------------------------
	
	Engine engine = new Engine();
	
	
	//-----Run Speed-----
	private InputVariable dmgTaken = new InputVariable();
	private InputVariable enemyDistance = new InputVariable();
	
	private OutputVariable runSpeed = new OutputVariable();
	
	//-----Fire Power-----
	private InputVariable dontBePersonalHP = new InputVariable(); //hehejoke
	private InputVariable enemyHP = new InputVariable(); //used laterz as well
	//+enemyDistance
	
	private OutputVariable firePower = new OutputVariable();
	
	//-----Bearing-----
	private InputVariable enemyBearing = new InputVariable();
	private InputVariable enemyVelocity = new InputVariable();
	//+enemyHP
	//+dontBePersonalHP
	//+enemyDistance
	
	private OutputVariable personalBearing = new OutputVariable();
	
	//----------------------------------------CODE----------------------------------------------
	
	public FuzzyRobotControl(double battlefieldWidth, double battlefieldHeight) {
		
		
		double battlefieldDiagonal = Math.sqrt(Math.pow(battlefieldWidth, 2) + Math.pow(battlefieldHeight, 2));
		
		engine.setName("Robot");
		engine.setDescription("First test fuzzy robot");

		
		//-----------------------------------Run Speed-------------------------------------------------
		
        final double minPower = Rules.MIN_BULLET_POWER; //0.1
    	final double maxPower = Rules.MAX_BULLET_POWER; //3.0
		
		dmgTaken.setName("dmgTaken");
		dmgTaken.setEnabled(true);
		dmgTaken.setRange(minPower, maxPower);
		dmgTaken.setLockValueInRange(false);
		dmgTaken.addTerm(new Triangle("Low", minPower, minPower, maxPower, 30.0));
		dmgTaken.addTerm(new Triangle("Medium", minPower, maxPower/3, maxPower, 1.0));
		dmgTaken.addTerm(new Triangle("High", minPower, maxPower, maxPower, 1.0));
		dmgTaken.addTerm(new Triangle("VeryHigh", maxPower/2, maxPower, maxPower, 5.0));
		dmgTaken.addTerm(new Triangle("Over9000", maxPower*0.6, maxPower, maxPower, 50.0));
		engine.addInputVariable(dmgTaken);
		
		enemyDistance.setName("enemyDistance");
		enemyDistance.setEnabled(true);
		enemyDistance.setRange(0.0, battlefieldDiagonal); 
		enemyDistance.setLockValueInRange(false);
        enemyDistance.addTerm(new Triangle("Close",  0.0, 0.0, battlefieldDiagonal, 10.0));
        enemyDistance.addTerm(new Triangle("Medium", 0.0, battlefieldDiagonal*0.6, battlefieldDiagonal, 5.0));
        enemyDistance.addTerm(new Triangle("Far", 0.0, battlefieldDiagonal, battlefieldDiagonal, 10.0));
		engine.addInputVariable(enemyDistance);
		
		final double minVelocity = 0;
    	final double maxVelocity = Rules.MAX_VELOCITY; //8.0
		
		runSpeed.setName("runSpeed");
		runSpeed.setRange(minVelocity, maxVelocity);
		runSpeed.setEnabled(true);
		runSpeed.setLockValueInRange(false);
		runSpeed.setAggregation(new Maximum());
		runSpeed.setDefuzzifier(new Centroid(100));
		runSpeed.setDefaultValue(8.0);
		runSpeed.setLockPreviousValue(false);
		runSpeed.addTerm(new Triangle("Low", minVelocity, minVelocity, maxVelocity, 4.0));
		runSpeed.addTerm(new Triangle("Medium", minVelocity, maxVelocity / 2, maxVelocity, 1.0));
		runSpeed.addTerm(new Triangle("High", minVelocity, maxVelocity, maxVelocity, 2.0));
        engine.addOutputVariable(runSpeed);

        
        
        //----------------------------------Fire Power----------------------------------------------------
        
        final double minHP = 0.0;
    	final double maxHP = 100.0; //can be higher because of drain rate (if robot hits enemy, HP increases), though take 100 as max
        
        dontBePersonalHP.setName("myHP");
        dontBePersonalHP.setEnabled(true);
        dontBePersonalHP.setRange(minHP, maxHP);
        dontBePersonalHP.setLockValueInRange(false);
        dontBePersonalHP.addTerm(new Triangle("Low", minHP, minHP, maxHP, 20.0));
        dontBePersonalHP.addTerm(new Triangle("Medium", minHP, minHP/2, maxHP, 10.0));
        dontBePersonalHP.addTerm(new Triangle("High", minHP, maxHP, maxHP, 1.0));
		engine.addInputVariable(dontBePersonalHP);
        
		enemyHP.setName("enemyHP");
		enemyHP.setEnabled(true);
		enemyHP.setRange(minHP, maxHP); 
		enemyHP.setLockValueInRange(false);
		enemyHP.addTerm(new Triangle("Low", minHP, minHP, maxHP, 20.0));
		enemyHP.addTerm(new Triangle("Medium", minHP, minHP/2, maxHP, 10.0));
		enemyHP.addTerm(new Triangle("High", minHP, maxHP, maxHP, 1.0));
		engine.addInputVariable(enemyHP);
		
        //+++++enemyDistance (used previously)
		
		firePower.setName("firePower");
		firePower.setRange(minPower, maxPower);
		firePower.setEnabled(true);
		firePower.setLockValueInRange(false);
		firePower.setAggregation(new Maximum());
		firePower.setDefuzzifier(new Centroid(100));
		firePower.setDefaultValue(1.0);
		firePower.setLockPreviousValue(false);
		firePower.addTerm(new Triangle("Low", minPower, minPower, maxPower, 30.0));
		firePower.addTerm(new Triangle("Medium", minPower, maxPower/3, maxPower, 1.0));
		firePower.addTerm(new Triangle("High", minPower, maxPower, maxPower, 1.0));
		firePower.addTerm(new Triangle("VeryHigh", maxPower/2, maxPower, maxPower, 5.0));
		firePower.addTerm(new Triangle("Over9000", maxPower*0.6, maxPower, maxPower, 50.0));
        engine.addOutputVariable(firePower);
		
        
        
        //------------------------------------Bearing------------------------------------------------------
        
        enemyBearing.setName("enemyBearing");
        enemyBearing.setEnabled(true);
        enemyBearing.setRange(0, 90);
        enemyBearing.setLockValueInRange(false);
        enemyBearing.addTerm(new Triangle("Close", 0, 0, 90, 5.0));
        enemyBearing.addTerm(new Triangle("Medium", 0, 45, 90, 1.0));
        enemyBearing.addTerm(new Triangle("Far", 0, 90, 90, 5.0));
		engine.addInputVariable(enemyBearing);
        
		enemyVelocity.setName("enemyVelocity");
		enemyVelocity.setEnabled(true);
		enemyVelocity.setRange(minVelocity, maxVelocity);
		enemyVelocity.setLockValueInRange(false);
		enemyVelocity.addTerm(new Triangle("Slow", minVelocity, minVelocity, maxVelocity, 1.0));
		enemyVelocity.addTerm(new Triangle("Medium", minVelocity, maxVelocity/2, maxVelocity, 1.0));
		enemyVelocity.addTerm(new Triangle("Fast", minVelocity, maxVelocity, maxVelocity, 2.0));
		engine.addInputVariable(enemyVelocity);
        
		//+++++enemyHP (used previously)
		//+++++dontBePersonalHP (used previously)
		//+++++enemyDistance (used previously)
		
		personalBearing.setName("personalBearing");
		personalBearing.setRange(0, 90);
		personalBearing.setEnabled(true);
		personalBearing.setLockValueInRange(false);
		personalBearing.setAggregation(new Maximum());
		personalBearing.setDefuzzifier(new Centroid(100));
		personalBearing.setDefaultValue(0);
		personalBearing.setLockPreviousValue(false);
		personalBearing.addTerm(new Triangle("Closer", 0, 0, 90, 5.0));
		personalBearing.addTerm(new Triangle("MediumDistance", 0, 45, 90, 1.0));
		personalBearing.addTerm(new Triangle("Further", 0, 90, 90, 5.0));
        engine.addOutputVariable(personalBearing);
        
        
        
        //------------------------------------Rule Block---------------------------------------------------
        
        RuleBlock mamdani = new RuleBlock();
        mamdani.setName("mamdani");
        mamdani.setDescription("");
        mamdani.setEnabled(true);
        mamdani.setConjunction(new Minimum());
        mamdani.setDisjunction(new Maximum());
        mamdani.setImplication(new AlgebraicProduct());
        mamdani.setActivation(new General());
        
        
        
        //-Run Speed-
        mamdani.addRule(Rule.parse("if dmgTaken is Low and enemyDistance is Far then runSpeed is Low", engine));
        mamdani.addRule(Rule.parse("if dmgTaken is Low or enemyDistance is Close then runSpeed is Medium", engine));
        mamdani.addRule(Rule.parse("if dmgTaken is Medium or enemyDistance is Medium then runSpeed is Medium", engine));
        mamdani.addRule(Rule.parse("if dmgTaken is High or enemyDistance is Close then runSpeed is Medium", engine));
        mamdani.addRule(Rule.parse("if dmgTaken is VeryHigh then runSpeed is High", engine));
        mamdani.addRule(Rule.parse("if dmgTaken is Over9000 then runSpeed is High", engine));
        mamdani.addRule(Rule.parse("if dmgTaken is Over9000 or dmgTaken is VeryHigh and enemyDistance is Close then runSpeed is High", engine));
        
        
        //-Fire Power-
//        mamdani.addRule(Rule.parse("if myHP is High and enemyHP is Low or enemyDistance is Far then firePower is High", engine));
//        mamdani.addRule(Rule.parse("if myHP is High and enemyHP is High or enemyDistance is Far then firePower is Medium", engine));
        mamdani.addRule(Rule.parse("if enemyDistance is Far or myHP is Low then firePower is Low", engine));
        mamdani.addRule(Rule.parse("if enemyHP is Medium and enemyDistance is Far then firePower is Low", engine));
        mamdani.addRule(Rule.parse("if enemyDistance is Close then firePower is Over9000", engine));
        mamdani.addRule(Rule.parse("if enemyDistance is Close and myHP is High then firePower is Over9000", engine));
        mamdani.addRule(Rule.parse("if enemyDistance is Medium and enemyHP is Low then firePower is High", engine));
        mamdani.addRule(Rule.parse("if enemyDistance is Close and myHP is Low then firePower is Medium", engine));
        mamdani.addRule(Rule.parse("if myHP is Medium and enemyHP is Medium then firePower is Medium", engine));
        mamdani.addRule(Rule.parse("if myHP is High or enemyHP is Low then firePower is VeryHigh", engine));
        
        
        //-Bearing-
        mamdani.addRule(Rule.parse("if enemyDistance is Close then personalBearing is Further", engine));
        mamdani.addRule(Rule.parse("if enemyDistance is Far then personalBearing is Closer", engine));
        mamdani.addRule(Rule.parse("if enemyDistance is Medium then personalBearing is MediumDistance", engine));
        
        
        engine.addRuleBlock(mamdani);
        
        

        StringBuilder status = new StringBuilder();
        if (!engine.isReady(status))
            throw new RuntimeException("[engine error] engine is not ready:n" + status);  

	}
	
	
	public double getSpeed(double damage, double distance){
		enemyDistance = engine.getInputVariable("enemyDistance");
		dmgTaken = engine.getInputVariable("dmgTaken");
		runSpeed = engine.getOutputVariable("runSpeed");
     	
	    enemyDistance.setValue(distance);
	    dmgTaken.setValue(damage);
	    
	    engine.process();
	        	 
	    double runSpeedo = runSpeed.getValue();    
	        
	    FuzzyLite.logger().info(String.format(
	               "enemyDistance.input = %s, dmgTaken.input = %s -> action.output = %s",
	               Op.str(enemyDistance.getValue()), Op.str(dmgTaken.getValue()), Op.str(runSpeedo)));
	        	
		
		return runSpeedo;
	}
	
	public double getBearing(double distance, double enemyBear, double enemyVel, double enemyHealth, double personalHealth){
		
		enemyDistance = engine.getInputVariable("enemyDistance");
		enemyBearing = engine.getInputVariable("enemyBearing");
		enemyVelocity = engine.getInputVariable("enemyVelocity");
		enemyHP = engine.getInputVariable("enemyHP");
		dontBePersonalHP = engine.getInputVariable("myHP");
		personalBearing = engine.getOutputVariable("personalBearing");
		
		
		enemyDistance.setValue(distance);
		enemyBearing.setValue(enemyBear);
		enemyVelocity.setValue(enemyVel);
		enemyHP.setValue(enemyHealth);
		dontBePersonalHP.setValue(personalHealth);
		
		engine.process();
		
		double ourBearing = personalBearing.getValue();
		
		FuzzyLite.logger().info(String.format(
	               "enemyDistance.input = %s, enemyBearing.input = %s, enemyVelocity.input = %s, enemyHP.input = %s, dontBePersonalHP.input = %s -> action.output = %s",
	               Op.str(enemyDistance.getValue()), Op.str(enemyBearing.getValue()), 
	               Op.str(enemyVelocity.getValue()), Op.str(enemyHP.getValue()), 
	               Op.str(dontBePersonalHP.getValue()), Op.str(ourBearing)));

		
		return ourBearing;
		
	}

	public double getFirepower(double distance, double enemyHealth, double personalHealth){
		
		enemyDistance = engine.getInputVariable("enemyDistance");
		enemyHP = engine.getInputVariable("enemyHP");
		dontBePersonalHP = engine.getInputVariable("myHP");
		firePower = engine.getOutputVariable("firePower");
		
		
		enemyDistance.setValue(distance);
		enemyHP.setValue(enemyHealth);
		dontBePersonalHP.setValue(personalHealth);
		
		engine.process();
		
		double fire = firePower.getValue();
		
		FuzzyLite.logger().info(String.format(
	               "enemyDistance.input = %s, enemyHP.input = %s, dontBePersonalHP.input = %s -> action.output = %s",
	               Op.str(enemyDistance.getValue()), Op.str(enemyHP.getValue()), Op.str(dontBePersonalHP.getValue()), Op.str(fire)));

		
		return fire;

	}
	
	public static void main(String[] args) {
    	FuzzyRobotControl fuzzy = new FuzzyRobotControl(800, 600);
    	System.out.println(Rules.MIN_BULLET_POWER);
    	System.out.println(Rules.MAX_BULLET_POWER);
    	System.out.println(Rules.MAX_VELOCITY);
    	System.out.println(Rules.ROBOT_HIT_DAMAGE);
    	
    	fuzzy.getSpeed(0.2, 100);
    	fuzzy.getSpeed(3, 100);
    	fuzzy.getSpeed(1, 100);
    	fuzzy.getSpeed(0.5, 100);
    	fuzzy.getSpeed(2, 100);
    	
    	
    	fuzzy.getBearing(600, 100, 8.0, 100, 90);
    	fuzzy.getBearing(100, 10, 2, 100, 20);
    	fuzzy.getBearing(50, 30, 5, 20, 100);
    	
    	fuzzy.getFirepower(0, 20, 100);
    	fuzzy.getFirepower(100, 20, 100);
    	fuzzy.getFirepower(100, 100, 20);
    	fuzzy.getFirepower(50, 50, 50);
    	
    }
}

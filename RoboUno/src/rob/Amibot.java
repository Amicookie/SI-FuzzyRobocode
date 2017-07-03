package rob;

import java.awt.Color;

import robocode.*;

/*
 * Author: Karolina Zaborowska
 * 1.0 @ 2017-07-02
 * 
 */

public class Amibot extends AdvancedRobot {

	private AdvancedEnemyBot enemy = new AdvancedEnemyBot();
	private FuzzyRobotControl fuzzyControl;
		
	public double enemyDistance = 0.0; 
	public double runFastly = 0.0; //btw. it's an inside joke :3 ikr fastly is incorrect ;)
	private int dir = 1;
	private byte moveDirection = 1;
	private int tooCloseToWall = 0;
	private int wallMargin = 60;

	
	
	//------------------------a method used for "on hit by bullet" purpose, though it doesnt work as intended ;(-------
	void doMovement(double pixels) {

		// get the coordinate points of the center of the battlefield
		double xmid = getBattleFieldWidth() / 2;
		double ymid = getBattleFieldHeight() / 2;
		// get the absolute bearing between my robot and the center
		double absBearingToCenter = Utils.absoluteBearing(getX(), getY(), xmid, ymid);
		// calculate how much I need to turn to get there
		double turn = absBearingToCenter - getHeading();
		// normalize the turn for more efficient movement
		setTurnRight(Utils.normalizeBearing(turn));

		// wait 'til we're turned in the right direction
		waitFor(new TurnCompleteCondition(this));

		// (Point2D is a useful class)
		//double distanceToCenter = Point2D.distance(getX(), getY(), xmid, ymid);
		setAhead(pixels);
	}
	
	
	public void run() {
		
		fuzzyControl = new FuzzyRobotControl(getBattleFieldWidth(), getBattleFieldHeight());
		
		setColors(Color.cyan, Color.magenta, Color.cyan);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);

		// Don't get too close to the walls
		addCustomEvent(new Condition("too_close_to_walls") {
				public boolean test() {
					return (
						// we're too close to the left wall
						(getX() <= wallMargin ||
						 // or we're too close to the right wall
						 getX() >= getBattleFieldWidth() - wallMargin ||
						 // or we're too close to the bottom wall
						 getY() <= wallMargin ||
						 // or we're too close to the top wall
						 getY() >= getBattleFieldHeight() - wallMargin)
						);
					}
				});

		while (true) {
			doRadar();
			doMove();
			doGun();
			execute();
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {

		enemyDistance = e.getDistance();
		
		// calculate firepower based on distance
		double firePower = fuzzyControl.getFirepower(enemyDistance, e.getEnergy(), getEnergy());
		
		//firePower = Double.parseDouble(new DecimalFormat("##.#").format(firePower));
		
		// calculate speed of bullet
		double bulletSpeed = 20 - firePower * 3;
		// distance = rate * time, solved for time
		long time = (long)(enemy.getDistance() / bulletSpeed);
		
		System.out.println("Fire power: "+firePower);
		
		// track if we have no enemy, the one we found is significantly
		// closer, or we scanned the one we've been tracking.
		if ( enemy.none() || e.getDistance() < enemy.getDistance() - 70 || e.getName().equals(enemy.getName())) {
			// track him using the NEW update method
			enemy.update(e, this);
		}
		
		// calculate gun turn to predicted x,y location
		double futureX = enemy.getFutureX(time);
		double futureY = enemy.getFutureY(time);
		double absDeg = Utils.absoluteBearing(getX(), getY(), futureX, futureY);
		// turn the gun to the predicted x,y location
		setTurnGunRight(Utils.normalizeBearing(absDeg - getGunHeading()));
		
		
		// new bearing
		double newPosition = fuzzyControl.getBearing(enemyDistance, e.getBearing(), e.getVelocity(), e.getEnergy(), getEnergy());
		
		//newPosition = Double.parseDouble(new DecimalFormat("##.#").format(newPosition));
		
		double enemyB = enemy.getBearing();
		//Double.parseDouble(new DecimalFormat("##.#").format(enemy.getBearing()));
		
		System.out.println("New position: "+newPosition);
		System.out.println("Enemy bearing: "+enemyB);
		
		if(e.getDistance()>350){
			setTurnLeft(newPosition-enemyB);
		}
		setAhead((newPosition+enemyB) * moveDirection);
		
		//setTurnRight(e.getBearing() + 90);
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
			setFire(firePower);
		}
		
		
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {

		System.out.println("OnHitByBullet?");
		
		runFastly = fuzzyControl.getSpeed(event.getPower(), enemyDistance);
		
		//runFastly = Double.parseDouble(new DecimalFormat("##.#").format(runFastly));
		
		moveDirection *= -1;

		System.out.println(runFastly*10+" run fastly!!!!!!"); 
		
		doMovement(runFastly*10);
		
		if (getVelocity() == 0) {
			setMaxVelocity(8);
			moveDirection *= -1;
			setAhead(10000 * moveDirection);
		}
		
	}
	
	@Override
	public void onRobotDeath(RobotDeathEvent e) {
		// see if the robot we were tracking died
		if (e.getName().equals(enemy.getName())) {
			enemy.reset();
		}
	}   

	@Override
	public void onCustomEvent(CustomEvent e) {
		if (e.getCondition().getName().equals("too_close_to_walls"))
		{
			if (tooCloseToWall <= 0) {
				// if we weren't already dealing with the walls, we are now
				tooCloseToWall += wallMargin;
				setMaxVelocity(0); // stop!!!
			}
		}
	}

	@Override
	public void onHitWall(HitWallEvent e) { 
		dir =- dir;
	}

	@Override
	public void onHitRobot(HitRobotEvent e) { 
		tooCloseToWall = 0; 
	}

	void doRadar() {
		// rotate the radar
		setTurnRadarRight(360);
	}

	public void doMove() {
		// always square off our enemy, turning slightly toward him
		setTurnRight(Utils.normalizeBearing(enemy.getBearing() + 90 - (15 * moveDirection)));

		// if we're close to the wall, eventually, we'll move away
		if (tooCloseToWall > 0) tooCloseToWall--;

		// switch directions if we've stopped
		// (also handles moving away from the wall if too close)
		if (getVelocity() == 0) {
			setMaxVelocity(8);
			moveDirection *= -1;
			setAhead(10000 * moveDirection);
		}
		
//		if(){
			double newPosition = fuzzyControl.getBearing(enemyDistance, enemy.getBearing(), enemy.getVelocity(), enemy.getEnergy(), getEnergy());
			
			//newPosition = Double.parseDouble(new DecimalFormat("##.#").format(newPosition));
			
			double enemyB = enemy.getBearing();
					//Double.parseDouble(new DecimalFormat("##.#").format(enemy.getBearing()));
			
			System.out.println("New position: "+newPosition+" And result is: "+(newPosition-enemyB)+" Z plusem: "+(newPosition+enemyB));
			
			if(enemy.getDistance()>350){
				setTurnLeft(newPosition-enemyB);
			}
			setAhead((newPosition+enemyB) * moveDirection);
			
			
//		}
		
	}

	void doGun() {

		if (enemy.none())
			return;

		double firePower = fuzzyControl.getFirepower(enemy.getDistance(), enemy.getEnergy(), getEnergy());
		
		//firePower = Double.parseDouble(new DecimalFormat("##.#").format(firePower));

		double bulletSpeed = 20 - firePower * 3;
		long time = (long)(enemy.getDistance() / bulletSpeed);
		double futureX = enemy.getFutureX(time);
		double futureY = enemy.getFutureY(time);
		double absDeg = Utils.absoluteBearing(getX(), getY(), futureX, futureY);
		setTurnGunRight(Utils.normalizeBearing(absDeg - getGunHeading()));
		
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
			setFire(firePower);
		}
	}
	
	
	
	
	
}

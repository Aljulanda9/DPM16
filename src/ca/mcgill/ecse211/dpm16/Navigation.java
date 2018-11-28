package ca.mcgill.ecse211.dpm16;


import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.utility.Delay;

public class Navigation extends Thread{

	public Odometer odo;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private boolean isNavigating;
	private double[][] waypoints;

	private static double TRACK = MainController.TRACK;
	private static final double WHEELRAD = MainController.WHEEL_RADIUS;

	public static int FORWARD_SPEED = 175;
	public static int ROTATE_SPEED = 70;

	private static final double GRID_SIZE = 30.48;
	public double prevtheta = 0;
	public Navigation(Odometer odo,EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double[][] waypoints) {
		this.odo = odo;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.isNavigating = true;
		this.waypoints = waypoints;
	}

	public void run() {
		try {
			Thread.sleep(2000);
		}
		catch(InterruptedException e) {
			// there is nothing to be done here because it is not expected that
			// the odometer will be interrupted by another thread
		}
		for (int i = 0; i<waypoints.length;i++) {
			travelTo(waypoints[i][0], waypoints[i][1]);
		}
	}
	
	
	
	private static double odoAngle = 0;

	/**
	 * Let the robot travel to the specified destination
	 * @param x The x value of the destination
	 * @param y The y value of the destination
	 */
	public void travelTo(double x, double y) {
		
		this.isNavigating = true;

		double calcTheta = 0, len = 0, deltaX = 0, deltaY = 0;

		
		odoAngle = odo.getXYT()[2];

		deltaX = x*30.48- odo.getXYT()[0];
		deltaY = y*30.48 - odo.getXYT()[1];
	
		double xxx = odo.getXYT()[0];
		double yyy = odo.getXYT()[1];
		len = Math.hypot(Math.abs(deltaX), Math.abs(deltaY));

		//get angle up to 180
		calcTheta = Math.toDegrees(Math.atan2(deltaX, deltaY));

		//if result is negative subtract it from 360 to get the positive
		if (calcTheta < 0)
			calcTheta = 360 - Math.abs(calcTheta);

		// turn to the found angle
		turnTo2(calcTheta);
	

		// go
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(WHEELRAD, len), true);
		rightMotor.rotate(convertDistance(WHEELRAD, len), false);
		odo.setX(x*30.48);
		odo.setY(y*30.48);
	}
	
	
	
	
	
public void travelTo2(double x, double y) {
		
		this.isNavigating = true;

		double calcTheta = 0, len = 0, deltaX = 0, deltaY = 0;

		
		odoAngle = odo.getXYT()[2];

		deltaX = x*30.48- odo.getXYT()[0];
		deltaY = y*30.48 - odo.getXYT()[1];
	
		double xxx = odo.getXYT()[0];
		double yyy = odo.getXYT()[1];
		len = Math.hypot(Math.abs(deltaX), Math.abs(deltaY));

		//get angle up to 180
		calcTheta = Math.toDegrees(Math.atan2(deltaX, deltaY));

		//if result is negative subtract it from 360 to get the positive
		if (calcTheta < 0)
			calcTheta = 360 - Math.abs(calcTheta);

		// turn to the found angle
		//turnTo2(calcTheta);
	

		// go
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(WHEELRAD, len), true);
		rightMotor.rotate(convertDistance(WHEELRAD, len), false);
		odo.setX(x*30.48);
		odo.setY(y*30.48);
	}
	
	
public void travelTo3(double x, double y) {
	
	this.isNavigating = true;

	double calcTheta = 0, len = 0, deltaX = 0, deltaY = 0;

	
	odoAngle = odo.getXYT()[2];

	deltaX = x*30.48- odo.getXYT()[0];
	deltaY = y*30.48 - odo.getXYT()[1];

	double xxx = odo.getXYT()[0];
	double yyy = odo.getXYT()[1];
	len = Math.hypot(Math.abs(deltaX), Math.abs(deltaY));

	//get angle up to 180
	calcTheta = Math.toDegrees(Math.atan2(deltaX, deltaY));

	//if result is negative subtract it from 360 to get the positive
	if (calcTheta < 0)
		calcTheta = 360 - Math.abs(calcTheta);

	// turn to the found angle
	turnTo2(calcTheta);
	
	Delay.msDelay(2000);
	LightLocaliser1.beforeTunnel();
	Delay.msDelay(2000);


	// go
	leftMotor.setSpeed(FORWARD_SPEED);
	rightMotor.setSpeed(FORWARD_SPEED);
	leftMotor.rotate(convertDistance(WHEELRAD, len), true);
	rightMotor.rotate(convertDistance(WHEELRAD, len), false);
	odo.setX(x*30.48);
	odo.setY(y*30.48);
}





	
	public void turnTo2(double newTheta) {boolean turnLeft = false; //to do the minimal turn
	double deltaAngle = 0;
	// get the delta nagle
	//deltaAngle = newTheta - odoAngle;
	 deltaAngle = newTheta - prevtheta;
	// if the delta angle is negative find the equivalent positive
	if (deltaAngle < 0) {
		deltaAngle = 360 - Math.abs(deltaAngle);
	}

	// Check if angle is the minimal or not
	if (deltaAngle > 180) {
		turnLeft = true;
		deltaAngle = 360 - Math.abs(deltaAngle);
	} else {
		turnLeft = false;
	}

	// set to rotation speed
	leftMotor.setSpeed(ROTATE_SPEED);
	rightMotor.setSpeed(ROTATE_SPEED);

	//turn robot to direction we chose
	if (turnLeft) {
		leftMotor.rotate(-convertAngle(WHEELRAD, TRACK, deltaAngle), true);
		rightMotor.rotate(convertAngle(WHEELRAD, TRACK, deltaAngle), false);
	} else {
		leftMotor.rotate(convertAngle(WHEELRAD, TRACK, deltaAngle), true);
		rightMotor.rotate(-convertAngle(WHEELRAD, TRACK, deltaAngle), false);
	}
	prevtheta=newTheta;

}
	
	
	
	
	
	
	
	

	/**
	 * Let the robot get to the specified heading by turning a minimal rotation angle
	 * @param newTheta The heading that the robot needs to rotate to
	 * 
	 * 
	 * 
	 * 
	 */
	public void turnTo(double newTheta) {

		// Calculate how much the robot needs to rotate from its current heading to the specified heading
		double theta = newTheta - odo.getXYT()[2];
		
		//double theta = newTheta - prevtheta;
		
		// Calculate the "minimal" angle to rotate
		// Positive angle: angle to rotate clockwise; Negative angle: angle to rotate counter-clockwise
		// If the absolute value of the angle to rotate is > 180, then the angle to rotate in the opposite direction must be smaller
		// We can +/- 360 to find that smaller angle
		
		if ( theta > 180) {
			theta = theta - 360;
		}
		else if ( theta < -180 ) {
			theta = theta + 360;
		}
		

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(convertAngle(WHEELRAD, TRACK, theta), true);
		rightMotor.rotate(-convertAngle(WHEELRAD, TRACK, theta), false);
		//prevtheta=theta;
	}

	/**
	 * Move the robot forward or backward for a specified distance
	 * @param distance Distance to travel 
	 * @param forward  Represent if the robot moves forward or backward
	 */
	public void move(double distance, boolean forward) {
		
		if (forward==true) {
			leftMotor.rotate(convertDistance(WHEELRAD, distance),true);
			rightMotor.rotate(convertDistance(WHEELRAD, distance), false);
			
		}
		
		else {
			leftMotor.rotate(-convertDistance(WHEELRAD, distance),true);
			rightMotor.rotate(-convertDistance(WHEELRAD, distance), false);
		}
		
		Delay.msDelay(500);
		
	}

	
	 public void advance(long distance) {
		    leftMotor.rotate(convertDistance(WHEELRAD, distance), true);
		    rightMotor.rotate(convertDistance(WHEELRAD, distance), false);
		  }
	
	 public void turnCW(long degree) {
		    leftMotor.rotate(
		        convertAngle(WHEELRAD, TRACK, degree), true);
		    rightMotor.rotate(
		        -convertAngle(WHEELRAD, TRACK, degree), true);
		   // prevtheta=prevtheta+degree;
		  }
	public boolean isNavigating() {
		return isNavigating;
	}
	
	private int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}

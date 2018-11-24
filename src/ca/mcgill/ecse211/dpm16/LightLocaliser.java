package ca.mcgill.ecse211.dpm16;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class LightLocaliser {
	
	private Navigation navigator;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private SampleProvider cs;
	private float[] colorData;
	private float color;
	
	
	private final int SENSOR_OFFSET = 13;
	private final int FORWARD_SPEED = 150;
	private final int ROTATE_SPEED = 70;
	private final int NUM_OF_LINES = 4;
	
	private final double DISTANCE_OFFSET = 13;
	private final double DISTANCE_OFFSET_BACK = DISTANCE_OFFSET+1.0;
	private final double BLACK_LOWER_BOUND = 0.01;
	private final double BLACK_UPPER_BOUND = 0.35;
	private final int ANGLE_OFFSET = 20;
	
	
	public LightLocaliser(Navigation navigator, Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, SampleProvider cs, float[] colorData) {
		this.navigator = navigator;
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftMotor.setSpeed(FORWARD_SPEED);
		this.rightMotor.setSpeed(FORWARD_SPEED);
		this.cs = cs;
		this.colorData = colorData;
	}
	
	
	 
	/**
	 * This method moves the robot to the origin and let it rotate to the positive y direction
	 */
	public void doLocalization() {
		
		// Keep moving forward until a black line is detected
		while ( !(getColor()>=BLACK_LOWER_BOUND && getColor()<=BLACK_UPPER_BOUND) ) {
			leftMotor.forward();
			rightMotor.forward();
		}
		
		// When detected a black line, move backwards for a set distance
		// This is to make sure that the robot arrives at a place 
		// where it can reach the negative x/y-axis and positive x/y-axis when rotating
		Sound.beep();
		navigator.move(DISTANCE_OFFSET_BACK, false);
		
		// Do the same thing for the horizontal direction
		navigator.turnTo(90);
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		
		// Keep moving forward until a black line is detected
		while ( !(getColor()>=BLACK_LOWER_BOUND && getColor()<=BLACK_UPPER_BOUND) ) {
			leftMotor.forward();
			rightMotor.forward();
		}
		Sound.beep();
		navigator.move(DISTANCE_OFFSET, false);
		
		// Initialize an array to record the robots heading each time it detects a black line
		double []line_heading = new double[NUM_OF_LINES];
		
		// A counter to record how many black lines are detected
		int count_line = 0;
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		// Keep the robot rotating until it detects all 4 black lines
		// i.e. negative x-axis, positive y-axis, positive x-axis, and negative y-axis
		// The black lines are detected in the order stated above
		while (count_line<NUM_OF_LINES) {
			
			if (getColor()>=BLACK_LOWER_BOUND && getColor()<=BLACK_UPPER_BOUND) {
				Sound.beep();
				// Record the robot's heading when it detects a black line
				line_heading[count_line] = odometer.getXYT()[2];
				// Increment the counter of detected black lines
				count_line++;	
				
				/*
				 * This is to prevent the robot from reporting black line detection multiple times
				 * when it actually reaches only one black line
				 * Since a black line has a width of ~0.5cm,
				 * the robot can detect "black" color multiple times when crossing it 
				 */
				try {
					Thread.sleep(200);
				}
				catch (InterruptedException e) {
					
				}
				
			}
			
			leftMotor.forward();
			rightMotor.backward();
			
		}
		
		/* 
		 * When the robot stops rotating, it must be at the intersection of its path and the negative y-axis
		 * Because this is the last point where it detects a black line
		 * Calculate its actual position at this point
		 * 
		 */
		double negX_heading = line_heading[0];
		double posY_heading = line_heading[1];
		double posX_heading = line_heading[2];
		double negY_heading = line_heading[3];
		
		
		// Here, the X and Y values in the standard (x,y,theta) convention are calculated using geomotry knowledge
		double thetaY = wrapAngle(negY_heading - posY_heading);
		double thetaX = wrapAngle(posX_heading - negX_heading);
		
		double correctedX = (-1) * SENSOR_OFFSET * Math.cos( Math.toRadians(thetaY/2) );
		double correctedY = (-1) * SENSOR_OFFSET * Math.cos( Math.toRadians(thetaX/2) );
		
		// Update the X and Y values in the odometer
		odometer.setX(correctedX);
		odometer.setY(correctedY);
		
		// Calculate by how much the robot's current theta convention deviates from the standard theta convention
		double delTheta = 270 - negY_heading + 0.5 * thetaY;
		
		// Calculate what the theta of the robot's current heading should be in the standard convention
		double correctedTheta = wrapAngle(odometer.getXYT()[2] + delTheta);
		
		// Update the correct theta in the odometer
		odometer.setTheta(correctedTheta);
		
		// Navigate to the origin
		navigator.travelTo(0, 0);
		
		// Rotate to the 0 degree heading
		navigator.turnTo(-ANGLE_OFFSET);
		odometer.setTheta(0);
		Delay.msDelay(3000);
		
	}
	
	
	/**
	 * Retrieve data from the color sensor 
	 * @return Color sample data from the color sensor
	 */
	private float getColor() {
		cs.fetchSample(colorData, 0);
		this.color = colorData[0];
		return this.color;
	}
	
	/**
	 * @param angle The angle to be wrapped 
	 * @return The angle wrapped in degrees from 0 to 360
	 */
	private double wrapAngle (double angle) {
		if (angle > 360) {
			angle = angle - 360;
		}
		else if ( angle<0 ) {
			angle = angle + 360;
		}
		return angle;
	}
	

}

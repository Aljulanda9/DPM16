package ca.mcgill.ecse211.dpm16;

import ca.mcgill.ecse211.dpm16.Odometer;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class USLocaliser implements Runnable {
	private int distance;
	private Odometer odo;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private SampleProvider us;
	private float[] data;
	private int option;
	private Navigation navigator;
	private int filterControl;

	private final int WALL_DIST = 40;
	private final int MARGIN = 3;
	private final int ROTATE_SPEED = 150;
	private final int FILTER_OUT = 5;
	private final int LARGE_SENSOR_DIST = 40;
	private final int MAX_SENSOR_DIST = 255;
	private final int SMALL_STANDARD_ANGLE = 45;
	private final int LARGE_STANDARD_ANGLE = 225;
	private final int ANGLE_OFFSET = 2;


	public USLocaliser(Odometer odo, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, SampleProvider us, float[] data, Navigation navigator) {
		this.odo = odo;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		this.us = us;
		this.data = data;
		this.navigator = navigator;
		this.filterControl = 0;
		this.distance = 255;
	}

	/**
	 * This method let the robot rotate to the positive y direction
	 */

	@Override
	public void run() {

		double thetaA = 0.0;
		double thetaB = 0.0;

		//Falling edge
		if (true) {
			
			// Delay the start time for motor rotation to wait for Ultrasonic sensor to turn on
			Delay.msDelay(2000);

			// Rotate until the robot sees no wall
			while ( getFilteredData() < ( WALL_DIST + MARGIN ) ) {
				leftMotor.forward();
				rightMotor.backward();
			}

			// Keep rotating until the robot sees the wall
			while ( getFilteredData() > ( WALL_DIST - MARGIN ) ) {
				leftMotor.forward();
				rightMotor.backward();
			}
			
			Sound.beep();
			// During testing, we found that if we did not set the robot's speed to 0
			// It would rotate ~10 degrees more than it should
			leftMotor.setSpeed(0);
			rightMotor.setSpeed(0);


			// Store the heading at this point
			thetaA = odo.getXYT()[2];


			// Set the robot's speed for it to rotate later
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			// Switch direction and rotate until the robot sees no wall
			while ( getFilteredData() < ( WALL_DIST + MARGIN ) ) {
				leftMotor.backward();
				rightMotor.forward();
			}

			// Keep rotating until the robot sees the wall
			while ( getFilteredData() > ( WALL_DIST - MARGIN ) ) {
				leftMotor.backward();
				rightMotor.forward();
			}

			
			Sound.beep();
			// During testing, we found that if we did not set the robot's speed to 0
			// It would rotate ~10 degrees more than it should
			leftMotor.setSpeed(0);
			rightMotor.setSpeed(0);

			// Store the heading at this point
			thetaB = odo.getXYT()[2];

		}

		//Rising edge
//		else if (option==Button.ID_RIGHT) {
//			
//			// Delay the start time for motor rotation to wait for Ultrasonic sensor to turn on
//			Delay.msDelay(2000);
//
//			// Rotate the robot until it sees the wall
//			while ( getFilteredData() > ( WALL_DIST - MARGIN ) ) {
//				leftMotor.backward();
//				rightMotor.forward();
//			}
//
//
//			// Keep rotating until it sees no wall
//			while ( getFilteredData() < ( WALL_DIST + MARGIN ) ) {
//				leftMotor.backward();
//				rightMotor.forward();
//			}
//
//			Sound.beep();
//			// During testing, we found that if we did not set the robot's speed to 0
//			// It would rotate ~10 degrees more than it should
//			leftMotor.setSpeed(0);
//			rightMotor.setSpeed(0);
//
//			// Store the heading at this point
//			thetaA = odo.getXYT()[2];		
//
//			leftMotor.setSpeed(ROTATE_SPEED);
//			rightMotor.setSpeed(ROTATE_SPEED);
//
//
//			// Switch direction and rotate until it sees the wall
//			while ( getFilteredData() > ( WALL_DIST - MARGIN ) ) {
//				leftMotor.forward();
//				rightMotor.backward();
//			}
//
//			// Keep rotating until it sees no wall
//			while ( getFilteredData() < ( WALL_DIST + MARGIN ) ) {
//				leftMotor.forward();
//				rightMotor.backward();
//			} 
//
//			Sound.beep();
//			// During testing, we found that if we did not set the robot's speed to 0
//			// It would rotate ~10 degrees more than it should
//			leftMotor.setSpeed(0);
//			rightMotor.setSpeed(0);
//
//
//			// Store the heading at this point
//			thetaB = odo.getXYT()[2];
//
//		}

		double delTheta = 0.0;

		// Calculate by how much the robot's current theta convention deviates from the standard theta convention
		// The formula was derived using geometry knowledge
		if ( thetaA > thetaB ) {
			delTheta = SMALL_STANDARD_ANGLE - ( thetaA + thetaB ) / 2;
		}

		else if ( thetaA < thetaB ) {
			delTheta = LARGE_STANDARD_ANGLE - ( thetaA + thetaB ) / 2;
		}

		// Calculate what the theta of the robot's current heading should be in the standard convention
		double updatedTheta = odo.getXYT()[2]+delTheta;

		// Wrap the corrected theta in degrees from 0 to 360
		if (updatedTheta < 0) {
			updatedTheta = 360 + updatedTheta;
		}
		else if (updatedTheta > 360) {
			updatedTheta = updatedTheta - 360;
		}

		// Correct the theta in the odometer
		odo.setTheta(updatedTheta);
		

		// Use navigator to rotate to the desired heading
		navigator.turnTo(-ANGLE_OFFSET); 
		odo.setTheta(0);
		
	}

	/**
	 * Retrieve and filter the data from the ultrasonic sensor
	 * @return Filtered reading from the ultrasonic sensor
	 */
	private int getFilteredData() {

		us.fetchSample(data, 0);
		int raw_distance = (int) (data[0]*100.0);

		// If the detected distance is very large
		// Count how many times such a large distance consecutively appear 
		if (raw_distance > LARGE_SENSOR_DIST && filterControl < FILTER_OUT) {
			filterControl++;
		}
		// If such a large distance continuously appear, it is a true value instead of noise
		else if (raw_distance > LARGE_SENSOR_DIST) {
			raw_distance = MAX_SENSOR_DIST;
			this.distance = raw_distance;
		}
		// If the detected distance is not very large, then it is reliable
		// Also set the counter to 0 for future use
		else {
			filterControl = 0;
			this.distance = raw_distance;
		}

		return this.distance;
	}

	
	/**
	 * To be used by other classes to access the sensor reading
	 * @return Filtered distance from the wall
	 */
	public int readUSDistance() {
		return this.distance;
	}


}

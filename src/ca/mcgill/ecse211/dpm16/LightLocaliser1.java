package ca.mcgill.ecse211.dpm16;

import java.util.ArrayList;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class LightLocaliser1 {
	static int dist= 15;
	static float oldSampleLeft;
	static float oldSampleRight;
	private static final int ROTATE_SPEED = 100;
	private static final long FREQUENCY = 10;
	private static final double D =10.35;
	private static SampleProvider sample1;
	private static SampleProvider sample2;
	ArrayList<Double> readingsArr = new ArrayList<>();
	ArrayList<Double> thetas = new ArrayList<>();
	private Odometer odometer;
	private static EV3ColorSensor colorSensor;
	private static float[] colorData;
	private EV3ColorSensor colorSensor2;
	private static float[] colorData2;
	private static Navigation navigation;
	private static EV3LargeRegulatedMotor leftMotor, rightMotor;
	private long correctionStart, correctionEnd, startTime;
	private double thisColor, prevColor, difference;
	private double y1, y2, x1, x2 = 0;
	private boolean firstLine = true;

	/**Constructor for the light sensor localizer
	 * @param odometer : current odometer running
	 * @param  colorSensor,colordata : current sensor variables instantiated
	 * @param leftMotor,rigthMotor :robot motors
	 */
	public LightLocaliser1(
			Odometer odometer,
			SampleProvider sample1,
			float[] colorData,
			SampleProvider sample2,
			float[] colorData2,
			Navigation navigation,
			EV3LargeRegulatedMotor leftMotor,
			EV3LargeRegulatedMotor rightMotor) {
		this.odometer = odometer;
		this.sample1 = sample1;
		this.colorData = colorData;
		this.sample2 = sample2;
		this.colorData2 = colorData2;
		this.navigation = navigation;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;

		//  resetMotor();
	}

	/** main method that has the flow logic. First we rotate the robot by 375 to make sure we cross alll lines.
	 * we keep getting ensor readings and comparing the internsity different to catch the black lines
	 * when we have caught 4 black lines we proceed to calculate the offset by the methods provided in the tutorial
	 * we then correct the odometer reading and head to 0,0 */
	public void localize()  throws OdometerExceptions {
		findLineAhead(true);
		Delay.msDelay(3000);
		findLineAhead(false);
	}
public static void beforeTunnel() {
	
	int foundLeft = 0;
	int foundRight = 0;


	leftMotor.forward();
	rightMotor.forward();

	while(true) {
		// Get color sensor readings
		sample1.fetchSample(colorData, 0); // acquire data
		sample2.fetchSample(colorData2, 0); 

		// If line detected for left sensor (intensity less than 0.3), only count once by keeping track of last value
		if((colorData[0]) < 0.27 && oldSampleLeft > 0.27 && foundLeft == 0) {
			leftMotor.stop(true);
			foundLeft++;
		}
		// If line detected for right sensor (intensity less than 0.3), only count once by keeping track of last value
		if((colorData2[0]) < 0.27 && oldSampleRight > 0.27 && foundRight == 0) {
			rightMotor.stop(true);
			foundRight++;
		}

		// Store last color readings
		oldSampleLeft = colorData[0];
		oldSampleRight = colorData2[0];

		// If line found for both sensors, exit
		if(foundLeft == 1 && foundRight == 1) {
			break;
		}
	}

	// Move forward by length of offset
	navigation.move(dist, false);
	leftMotor.setAcceleration(500);
	rightMotor.setAcceleration(500);
	
	
	
}
	
	
	
	public static void findLineAhead(Boolean first) throws OdometerExceptions {
		// Track how many lines found by left and right sensor
		int foundLeft = 0;
		int foundRight = 0;


		leftMotor.forward();
		rightMotor.forward();

		while(true) {
			// Get color sensor readings
			sample1.fetchSample(colorData, 0); // acquire data
			sample2.fetchSample(colorData2, 0); 

			// If line detected for left sensor (intensity less than 0.3), only count once by keeping track of last value
			if((colorData[0]) < 0.27 && oldSampleLeft > 0.27 && foundLeft == 0) {
				leftMotor.stop(true);
				foundLeft++;
			}
			// If line detected for right sensor (intensity less than 0.3), only count once by keeping track of last value
			if((colorData2[0]) < 0.27 && oldSampleRight > 0.27 && foundRight == 0) {
				rightMotor.stop(true);
				foundRight++;
			}

			// Store last color readings
			oldSampleLeft = colorData[0];
			oldSampleRight = colorData2[0];

			// If line found for both sensors, exit
			if(foundLeft == 1 && foundRight == 1) {
				break;
			}
		}

		// Move forward by length of offset
		navigation.move(dist, false);
		// Turn left 90 degrees to face 0 degrees
		
		if(first) {
			navigation.turnCW(90);
		}else {
			navigation.turnCW(-90);
		}
		
		//	Navigation.setSpeedAcceleration(LIGHTLOC_MOTOR_SPEED, LIGHTLOC_MOTOR_ACCELERATION);

	}


}

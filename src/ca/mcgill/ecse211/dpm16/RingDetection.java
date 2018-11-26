package ca.mcgill.ecse211.dpm16;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;

/**
 * The thread/class is used to travel through the tunnel and navigates to the tree and
 * Detects the color and travels back.
 * 
 * @author Reem and Aljulanda
 */
public class RingDetection{
	private Navigation navigator;
	private ColorDetector colorDetector; 
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private EV3LargeRegulatedMotor usMotor;
	private Grabber grabber;
	public Odometer odometer;
	private final double TRACK;
	private final double WHEEL_RAD;
	public static final double TILE_SIZE=30.48;;
	public static final int FORWARD_SPEED = 120;
	private static final int ROTATE_SPEED = 120;
	private final int US_ROTATION = 270; //constant for the US sensor rotation when bang bang starts/stops


	public RingDetection(ColorDetector colorDetector, Navigation navigation, Odometer odometer, Grabber grabber, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor sensorMotor,
			final double TRACK, final double WHEEL_RAD) { // constructor
		this.navigator = navigation;
		this.colorDetector = colorDetector;
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.grabber  = grabber;
		this.TRACK = TRACK;
		this.WHEEL_RAD = WHEEL_RAD;
		this.usMotor = sensorMotor;
		usMotor.resetTachoCount();
	}


	
	public void detect() {
		Navigation.FORWARD_SPEED = 10;
		//prepare for grabbing lower ring
		//now angle is -30
		grabber.move(30);
				
		
		//grab lower ring
		navigator.move(10, true);
		
		
		//go back to prepare for grabbing upper ring
		navigator.move(10, false);
		
		///////////////////////////////////////////
		///////////////////////////////////////////

		grabber.move(-30);
		
		
		//detect color
		colorDetector.detect();
		
		grabber.move(10);
		grabber.move(-20);
		
		//move to upper ring
		navigator.move(10, true);

		//lift upper ring
		grabber.move(-15);
		/////////////////////////////////
		//////////////////////////////////////
		
		
		//move back
		navigator.move(10, false);
		//detect color
		colorDetector.detect();

		Navigation.FORWARD_SPEED = 250;

	}

}

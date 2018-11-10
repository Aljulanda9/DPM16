package ca.mcgill.ecse211.dpm16;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * The thread/class is used to travel through the tunnel and navigates to the tree and
 * Detects the color and travels back.
 * 
 * @author Reem and Aljulanda
 */
public class RingDetection implements Runnable{
	private Navigation navigation;
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


	public RingDetection(Navigation navigation, Odometer odometer, Grabber grabber, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor sensorMotor,
			final double TRACK, final double WHEEL_RAD) { // constructor
		this.navigation = navigation;
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.grabber  = grabber;
		this.TRACK = TRACK;
		this.WHEEL_RAD = WHEEL_RAD;
		this.usMotor = sensorMotor;
		usMotor.resetTachoCount();
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//start at -20
		//slowly advance towards the tree 5mc
		
		navigation.move(10, true);
		
		
		//backward 5cm
		
		
		
		
		grabber.move(30);
		//now we are at 10degrees
		
		//advance 5cm
		
		//grab the ring
		grabber.move(25);
		
		//go back to base
		

		
	}

}

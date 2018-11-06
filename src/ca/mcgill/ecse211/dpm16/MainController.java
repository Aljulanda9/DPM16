// Lab4.java

package ca.mcgill.ecse211.dpm16;


import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**The main class that starts the motors, sensors and the localization process.
 * After localization it starts the navigation process which in turn, starts 
 * the ring and color detection then call the grabber class to the Grab the ring.
 * @author Reem Madkour
 */
public class MainController {


	private static final Port usPort = LocalEV3.get().getPort("S3");
	public static final EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S2);
	private static final EV3LargeRegulatedMotor leftMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor rampMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	private static final EV3LargeRegulatedMotor sensorMotor =  new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

	//color to be found
	public static final String color ="Green";

	public static  boolean inSquare =false;
	public static final int targetRing = 0;
	public static final int startingCorner = 1;
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 9.1;
	public static final double TAU = 360;
	//rectangle parameters
	public static final int[] lowerLeftCorner = {3, 3};
	public static final int[] upperRightCorner = {6,5 };
	
	//competition parameters
	public static int Red_UR_x;
	public static int Red_UR_y;
	public static int Red_LL_x;
	public static int Red_LL_y;
	
	public static int Green_UR_x;
	public static int Green_UR_y;
	public static int Green_LL_x;
	public static int Green_LL_y;
	
	public static int BRR_UR_x;
	public static int BRR_UR_y;
	public static int BRR_LL_x;
	public static int BRR_LL_y;
	
	public static int BRG_UR_x;
	public static int BRG_UR_y;
	public static int BRG_LL_x;
	public static int BRG_LL_y;
	
	public static int TR_UR_x;
	public static int TR_UR_y;
	public static int TR_LL_x;
	public static int TR_LL_y;
	
	
	private int TG_UR_x;
	private int TG_UR_y;
	private int TG_LL_x;
	private int TG_LL_y;
	//public static ObjectLocalizer oLocal = null;
	public static void main(String[] args) throws OdometerExceptions {
		int buttonChoice;

		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
		SampleProvider usDistance =
				usSensor.getMode("Distance"); // usDistance provides samples from this instance
		float[] usData =
				new float[usDistance.sampleSize()]; // usData is the buffer in which data are returned



		// buffer for sensor values
		float[] colorData = new float[colorSensor.getRedMode().sampleSize()];

		TextLCD t = LocalEV3.get().getTextLCD();


		do {
			// clear the display
			t.clear();

			// ask the user whether rising or falling edge
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString(" Color | Falling", 0, 2);
			t.drawString(" test  |   edge ", 0, 3);
			t.drawString("       |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);


		t.clear();
		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RADIUS);
		OdometryDisplay odometryDisplay = new OdometryDisplay(t);

		Thread odoThread = new Thread(odometer);
		odoThread.start();
		Navigation navigation =
				new Navigation(odometer, leftMotor, rightMotor, sensorMotor, MainController.TRACK, MainController.WHEEL_RADIUS);


		Grabber grabber = new Grabber(rampMotor);
		//oLocal = new ObjectLocalizer(usSensor, usData, leftMotor, rightMotor,odometer, navigation);

		RingDetection ringDetection =
				new RingDetection(odometer, leftMotor, rightMotor, sensorMotor, MainController.TRACK, MainController.WHEEL_RADIUS);
		UltrasonicLocalizer ultrasoniclocal;
		if (buttonChoice == Button.ID_LEFT) {
			/*
			 * First part of DEMO: classify colors.
			 */
			//ColorClassification coloring = new ColorClassification(colorSensor, usSensor, usData, t);
			t.clear();
			//coloring.run();

			ultrasoniclocal =
					new UltrasonicLocalizer(
							odometer,
							usSensor,
							usData,
							UltrasonicLocalizer.LocalizationType.RISING_EDGE,
							navigation,
							leftMotor,
							rightMotor, t);
		} else {
			/*
			 * Second part of DEMO: search and find.
			 */
			Thread odoDisplayThread = new Thread(odometryDisplay);
			odoDisplayThread.start();

			ultrasoniclocal =
					new UltrasonicLocalizer(
							odometer,
							usSensor,
							usData,
							UltrasonicLocalizer.LocalizationType.FALLING_EDGE,
							navigation,
							leftMotor,
							rightMotor, t);
		}
		ultrasoniclocal.localize();

		LightLocalizer ll =
				new LightLocalizer(odometer, colorSensor, colorData, navigation, leftMotor, rightMotor);
		ll.localize();

		grabber.rest();
		
		ringDetection.run();
		
		grabber.rest();
		
		
		while (Button.waitForAnyPress() != Button.ID_ENTER) ;
		System.exit(0);
	}
}

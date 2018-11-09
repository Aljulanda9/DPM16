package ca.mcgill.ecse211.dpm16;


import ca.mcgill.ecse211.dpm16.Odometer;
import ca.mcgill.ecse211.dpm16.Display;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class MainController {
	
	protected static final EV3LargeRegulatedMotor leftMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

	protected static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

	private static final Port usPort = LocalEV3.get().getPort("S3");
	
	private static final Port csPort = LocalEV3.get().getPort("S1");
	
	private static final EV3LargeRegulatedMotor rampMotor = 
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

	
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 10.32;
	
	//competition parameters
	public static int Red_UR_x;
	public static int Red_UR_y;
	public static int Red_LL_x;
	public static int Red_LL_y;
	
	public static int Green_UR_x;
	public static int Green_UR_y;
	public static int Green_LL_x ;
	public static int Green_LL_y;
	
	public static int TNR_UR_x = 4;
	public static int TNR_UR_y = 1;
	public static int TNR_LL_x = 2;
	public static int TNR_LL_y = 2;
	
	public static int TNG_UR_x;
	public static int TNG_UR_y;
	public static int TNG_LL_x;
	public static int TNG_LL_y;
		
	
	private static int TR_x;
	private static int TR_y;
	private static int TG_x;
	private static int TG_y;

	
	public static void main(String[] args) throws OdometerExceptions {
		
		int buttonChoice;

		final TextLCD t = LocalEV3.get().getTextLCD();

		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RADIUS);
	    Display odometryDisplay = new Display(lcd); // No need to change
	    

		// Set up ultrasonic sensor
		@SuppressWarnings("resource") // Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are returned

		Navigation navigator = new Navigation(odometer, leftMotor, rightMotor, null);


		do {
			// clear the display
			t.clear();

			// ask the user whether the motors should drive in a square or float
			t.drawString("<  Left|Right  >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString("   Fall|Rise  ", 0, 2);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		Grabber grabber = new Grabber(rampMotor);
		
		//before localization
		grabber.move(45);
		
		//after localization
		grabber.move(-45);
		
		//now we are at 0 degrees.. go to the tunnel
		
		//pushing angle for lower ring
		grabber.move(-20);
		
		//go to the tree
		
		//slowly advance towards the tree 5mc
		
		
		//backward 5cm
		
		//prepare for grabbing the upper ring
		grabber.move(30);
		//now we are at 10degrees
		
		//advance 5cm
		
		//grab the ring
		grabber.move(25);
		
		//go back to base
		
		/*
		// Left: falling edge; Right: rising edge
		USLocaliser usLocaliser = new USLocaliser(odometer, leftMotor, rightMotor, usDistance, usData, buttonChoice, navigator);
	
	    Thread odoThread = new Thread(odometer);
	    odoThread.start();
	    Thread odoDisplayThread = new Thread(odometryDisplay);
	    odoDisplayThread.start();
        Thread localiserThread = new Thread(usLocaliser);
        localiserThread.start();

		buttonChoice = Button.waitForAnyPress();

		if (buttonChoice==Button.ID_LEFT) {

			// Set up color sensor
			EV3ColorSensor csSensor = new EV3ColorSensor(csPort);
			csSensor.setFloodlight(lejos.robotics.Color.RED);
			SampleProvider csColor = csSensor.getRedMode();
			float[] colorData = new float[csColor.sampleSize()];
			LightLocaliser lightLocaliser = new LightLocaliser(navigator, odometer, leftMotor, rightMotor, csColor, colorData);	
			
			// Start light localization
			lightLocaliser.doLocalization();

		}
		
		double[][] waypoints = {{0,1.5}, {1.5,1.5}, {5.5, 1.5}, {5.5, 0.5}};
		
		int i = 0; 
		while(i<waypoints.length) {
			if(i == 1) {
				odometer.setTheta(0);
				navigator.turnTo(90);
			}

			if(i == 3) {
				odometer.setTheta(90);
				navigator.turnTo(90);
			}
			
			navigator.travelTo(waypoints[i][0], waypoints[i][1]);
	
			i++;
		}
		
		
		*/
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}


}

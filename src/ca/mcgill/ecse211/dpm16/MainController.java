package ca.mcgill.ecse211.dpm16;


import java.util.Map;
import ca.mcgill.ecse211.dpm16.LightLocaliser1;
import lejos.hardware.Button;
import lejos.hardware.Sound;
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

	private static final Port csPortRing = LocalEV3.get().getPort("S2");
	

	private static final Port csPort2 = LocalEV3.get().getPort("S4");


	private static final EV3LargeRegulatedMotor rampMotor = 
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));


	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RADIUS = 2.13;
	public static double TRACK = 9.8;

	// ** Set these as appropriate for your team and current situation **
	private static final String SERVER_IP = "192.168.2.42";
	private static final int TEAM_NUMBER = 16;

	// Enable/disable printing of debug info from the WiFi class
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;


	//competition parameters
	private static boolean horizontal = false;

	//Teams
	public static int RedTeam = 0;
	public  static int GreenTeam = 0;
	public  static int RedCorner = 0;
	public  static int GreenCorner = 0;

	//Red region
	public  static int Red_UR_x = 0;
	public  static int Red_UR_y = 0;
	public  static int Red_LL_x = 0;
	public  static int Red_LL_y = 0;

	//Green region
	public  static int Green_UR_x = 0;
	public  static int Green_UR_y = 0;
	public  static int Green_LL_x = 0;
	public  static int Green_LL_y = 0;

	//Red tunnel
	public  static int TNR_UR_x = 0;
	public  static int TNR_UR_y = 0;
	public  static int TNR_LL_x = 0;
	public  static int TNR_LL_y = 0;

	//Green tunnel
	public  static int TNG_UR_x = 0;
	public  static int TNG_UR_y = 0;
	public  static int TNG_LL_x = 0;
	public  static int TNG_LL_y = 0;

	//real tunnel
	public  static int T_UR_x = 0;
	public  static int T_UR_y = 0;
	public  static int T_LL_x = 0;
	public  static int T_LL_y = 0;

	//real tree
	public  static int T_x = 0;
	public  static int T_y = 0;



	//Island
	public  static int Island_UR_x = 0;
	public  static int Island_UR_y = 0;
	public  static int Island_LL_x = 0;
	public  static int Island_LL_y = 0;

	//Red Tree
	public  static int TR_x = 0;
	public  static int TR_y = 0;

	//Green Tree
	public  static int TG_x = 0;
	public  static int TG_y = 0;

	public static int corner = 0;


	public static void main(String[] args) throws OdometerExceptions {
		// Initialize WifiConnection class
		WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);

		final TextLCD t = LocalEV3.get().getTextLCD();



		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RADIUS);
		Display odometryDisplay = new Display(lcd); // No need to change
		
		// Set up ultrasonic sensor
		@SuppressWarnings("resource") // Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are returned

		Navigation navigator = new Navigation(odometer, leftMotor, rightMotor, null);

		Grabber grabber = new Grabber(rampMotor);

		EV3ColorSensor csSensorRing = new EV3ColorSensor(csPortRing);

		ColorDetector colorDetector = new ColorDetector(csSensorRing);


		RingDetection detector = new RingDetection(colorDetector, navigator, odometer, grabber, leftMotor, rightMotor, rampMotor, TRACK, WHEEL_RADIUS);


		USLocaliser usLocaliser = new USLocaliser(odometer, leftMotor, rightMotor, usDistance, usData, navigator);
		
		
		// Set up color sensor
		EV3ColorSensor csSensor = new EV3ColorSensor(csPort);
		csSensor.setFloodlight(lejos.robotics.Color.RED);
		SampleProvider csColor = csSensor.getRedMode();
		float[] colorData = new float[csColor.sampleSize()];
		
		EV3ColorSensor csSensor2 = new EV3ColorSensor(csPort2);
		csSensor2.setFloodlight(lejos.robotics.Color.RED);
		SampleProvider csColor2 = csSensor2.getRedMode();
		float[] colorData2 = new float[csColor2.sampleSize()];
		//LightLocaliser lightLocaliser = new LightLocaliser(navigator, odometer, leftMotor, rightMotor, csColor, colorData);	
		LightLocaliser1 lightLocaliser = new LightLocaliser1(odometer,csColor, colorData,csColor2, colorData2, navigator,leftMotor, rightMotor);	

		//odo,sensor,data,nav,motors
		// Connect to server and get the data, catching any errors that might occur


		try {
			/*
			 * getData() will connect to the server and wait until the user/TA presses the "Start" button
			 * in the GUI on their laptop with the data filled in. Once it's waiting, you can kill it by
			 * pressing the upper left hand corner button (back/escape) on the EV3. getData() will throw
			 * exceptions if it can't connect to the server (e.g. wrong IP address, server not running on
			 * laptop, not connected to WiFi router, etc.). It will also throw an exception if it connects
			 * but receives corrupted data or a message from the server saying something went wrong. For
			 * example, if TEAM_NUMBER is set to 1 above but the server expects teams 17 and 5, this robot
			 * will receive a message saying an invalid team number was specified and getData() will throw
			 * an exception letting you know.
			 */


			Map data = conn.getData();

			// Example 2 : Print out specific values
			//System.out.println("Map:\n" + data);

			RedTeam = ((Long) data.get("RedTeam")).intValue();
			GreenTeam = ((Long) data.get("GreenTeam")).intValue();
			RedCorner = ((Long) data.get("RedCorner")).intValue();
			GreenCorner = ((Long) data.get("GreenCorner")).intValue();

			//Red region
			Red_UR_x = ((Long) data.get("Red_UR_x")).intValue();
			Red_UR_y = ((Long) data.get("Red_UR_y")).intValue();
			Red_LL_x = ((Long) data.get("Red_LL_x")).intValue();
			Red_LL_y = ((Long) data.get("Red_LL_y")).intValue();

			//Green region
			Green_UR_x = ((Long) data.get("Green_UR_x")).intValue();
			Green_UR_y = ((Long) data.get("Green_UR_y")).intValue();
			Green_LL_x = ((Long) data.get("Green_LL_x")).intValue();
			Green_LL_y = ((Long) data.get("Green_LL_y")).intValue();

			//Red tunnel
			TNR_UR_x = ((Long) data.get("TNR_UR_x")).intValue();
			TNR_UR_y = ((Long) data.get("TNR_UR_y")).intValue();
			TNR_LL_x = ((Long) data.get("TNR_LL_x")).intValue();
			TNR_LL_y = ((Long) data.get("TNR_LL_y")).intValue();

			//Green tunnel
			TNG_UR_x = ((Long) data.get("TNG_UR_x")).intValue();
			TNG_UR_y = ((Long) data.get("TNG_UR_y")).intValue();
			TNG_LL_x = ((Long) data.get("TNG_LL_x")).intValue();
			TNG_LL_y = ((Long) data.get("TNG_LL_y")).intValue();

			//Island
			Island_UR_x = ((Long) data.get("Island_UR_x")).intValue();
			Island_UR_y = ((Long) data.get("Island_UR_y")).intValue();
			Island_LL_x = ((Long) data.get("Island_LL_x")).intValue();
			Island_LL_y = ((Long) data.get("Island_LL_y")).intValue();

			//Red Tree
			TR_x = ((Long) data.get("TR_x")).intValue();
			TR_y = ((Long) data.get("TR_y")).intValue();

			//Green Tree
			TG_x = ((Long) data.get("TG_x")).intValue();
			TG_y = ((Long) data.get("TG_y")).intValue();


		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		if(GreenTeam == 16) {
			//real tunnel
			T_UR_x = TNG_UR_x;
			T_UR_y = TNG_UR_y;
			T_LL_x = TNG_LL_x ;
			T_LL_y = TNG_LL_y ;

			//real tree
			T_x = TG_x;
			T_y = TG_x;

			//corner
			corner = GreenCorner;

		}else {
			T_UR_x = TNR_UR_x;
			T_UR_y = TNR_UR_y;
			T_LL_x = TNR_LL_x ;
			T_LL_y = TNR_LL_y ;

			//real tree
			T_x = TR_x;
			T_y = TR_y;

			//corner
			corner = RedCorner;

		}
		int buttonChoice;

		t.clear();




		Thread odoThread = new Thread(odometer);
		odoThread.start();
		Thread odoDisplayThread = new Thread(odometryDisplay);
		odoDisplayThread.start();


		//lift grabber before localization
		grabber.move(-45);

				
		
		usLocaliser.run();
		Delay.msDelay(3000);


		// Start light localization
		//lightLocaliser.doLocalization();
		lightLocaliser.localize();
		Delay.msDelay(500);

		Sound.beep();
		Sound.beep();
		Sound.beep();
		
		//navigator.turnTo(-15);
		
		Delay.msDelay(500);
		
		odometer.setTheta(0);
		
		
		Delay.msDelay(2000);
		if(corner == 0) {
			odometer.setXYT(1*30.48, 1*30.48, 0);
			navigator.prevtheta = 0;
		}

		if(corner == 1) {
			odometer.setXYT(7*30.48, 1*30.48, 270);
			navigator.prevtheta = 270;
		}

		if(corner == 2) {
			odometer.setXYT(7*30.48, 7*30.48, 180);
			navigator.prevtheta = 180;
		}

		if(corner == 3) {
			odometer.setXYT(1*30.48, 7*30.48, 90);
			navigator.prevtheta = 90;
		}

		Delay.msDelay(2000);

		//determine if horizontal or vertical
		if(Math.abs(T_UR_y -T_LL_y) == 1) {
			horizontal = true;
		}


		//lower grabber after localization
		grabber.move(45);
		double waypoints[][] = new double[5][2];
		if(horizontal) {
			waypoints[0][0] = T_LL_x - 1.5;
			waypoints[0][1] = T_LL_y + 0.3;
			waypoints[1][0] = T_LL_x - 1.0;
			waypoints[1][1] = T_LL_y + 0.3;

			waypoints[2][0] = T_UR_x + 1.0;
			waypoints[2][1] = T_UR_y - 0.7;

			waypoints[3][0] = T_x - 2;
			waypoints[3][1] = T_y;
			
			waypoints[4][0] = T_x - 1;
			waypoints[4][1] = T_y;

		}else {
			waypoints[0][0] = T_LL_x + 0.3;
			waypoints[0][1] = T_LL_y - 1.5;
			
			waypoints[1][0] = T_LL_x + 0.3;
			waypoints[1][1] = T_LL_y - 1.0;
			
			waypoints[2][0] = T_UR_x - 0.7;
			waypoints[2][1] = T_UR_y + 1.0;

			waypoints[3][0] = T_x - 2;
			waypoints[3][1] = T_y;
			
			waypoints[4][0] = T_x - 1;
			waypoints[4][1] = T_y;
		}

		//double[][] waypoints = {{1,2}, {3, 2}, {3,1}, {1,2}};

		int i = 0;
		while(i<waypoints.length) {
			
			
			
			navigator.travelTo(waypoints[i][0], waypoints[i][1]);
			
			if(i==1||i==3) {
				LightLocaliser1.beforeTunnel();
				Delay.msDelay(2000);
			}
			Delay.msDelay(2000);
			i++;
		}
		
		Sound.beep();
		Sound.beep();
		Sound.beep();
		detector.detect();


		//travel back to base using the same path that it came from
		i = waypoints.length-1;
		while(i>=0) {
			
			navigator.travelTo(waypoints[i][0], waypoints[i][1]);
			i--;
		}

		navigator.travelTo(1, 1);
		
		grabber.move(-75);
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}


}

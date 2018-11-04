package ca.mcgill.ecse211.dpm16;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/*
 * class to be used to detect the ring in the search area
 */
public class ObjectLocalizer {
	Navigation navigation;
	Odometer odometer;
	private double x;
	private double y;
	SampleProvider usSensor;
	boolean obs; 
	//values determined after testing
	private final int tolerance = 10;
	private final int ringRange = 5; 
	private final int ringAngle = 65;
	private final int ringCounter = 63;
	private float Distance;
	private float[] distance;
	private float initialDistance;
	private final double TRACK=dpm16.TRACK;
	private final double WHEEL_RAD=dpm16.WHEEL_RADIUS;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	static boolean bigFound=false;


	public ObjectLocalizer(SampleProvider usSensor, float[] distance, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer, Navigation navigation) {
		this.distance = distance;
		this.usSensor = usSensor;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.navigation = navigation;
	}

	/**gives the angle you need  the wheels to roate by to cover a certain arc length (distance)
	 * @param radius: radius of wheel
	 * @param distance: distance you want the robot to travel 
	 *@return: angle for rotate
	 */

	public static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	/*
	 * processing the data measured by the US sensor 
	 */
	public void processUSData() {
		ColorDetector detector = new ColorDetector(dpm16.colorSensor);

		//sample counter determines if the found object is a ring or noise
		int sampleCounter =0;
		boolean found=false;

		if( bigFound==(false)) {

			usSensor.fetchSample(distance, 0);
			Distance= distance[0]*100;

			//only detect rings within 50cm
			if(Distance < 50)  /** if the value measure is within the range compared to the intial value measured increase the counter value**/
			{
				initialDistance = Distance;
				//long while loop to detect each ring. 1000 determined after testing
				while(sampleCounter <= 1000&&bigFound==false) {
					//ensure samples are still for the same object
					//Distance > 5cm gives the sensor room to move away from the ring once detection is done
					if((Distance >= initialDistance-tolerance && Distance <= initialDistance + tolerance && Distance > ringRange) ) {
						sampleCounter ++; 
						usSensor.fetchSample(distance, 0);
						Distance= distance[0]*100;

						//63 was determined after testing
						if(sampleCounter >= ringCounter && found==false) {

							x=odometer.getXYT()[0];
							y=odometer.getXYT()[1];

							dpm16.inSquare=false;

							//turn towards the ring and move forward towards it
							navigation.turnCW2(ringAngle);
							navigation.travelTo((x+(Distance/1.3))/30.48, y/30.48, false);

							//once reached the ring stop to detect color
							leftMotor.stop();
							rightMotor.stop();
							if(detector.detect(dpm16.color)==true) {
								//if correct color is detected
								//beep and go to end point
								Sound.beep();
								found=true;
								bigFound=true;
								navigation.i=6;

							}
							else {
								//wrong color. Beep twice and continue to next way point
								Sound.beep();
								Sound.beep();

								//navigate around the ring (MANUAL NAVIGATION). Numbers determined after testing.
								leftMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, 90), true);  
								rightMotor.rotate(convertAngle(WHEEL_RAD, TRACK, 90), false);

								leftMotor.rotate(convertDistance(WHEEL_RAD, 28), true);
								rightMotor.rotate(convertDistance(WHEEL_RAD, 28), false);

								leftMotor.rotate(convertAngle(WHEEL_RAD, TRACK, 90), true);
								rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, 90), false);

								leftMotor.rotate(convertDistance(WHEEL_RAD, 28), true);
								rightMotor.rotate(convertDistance(WHEEL_RAD, 28), false);

								//delay to ensure that the robot has passed the current ring before
								//starting to detect for other rings
								Delay.msDelay(3000);

								found=true;
								navigation.i--;} 

							break;

						}

					}
					else {
						sampleCounter = 0;
						break;
					}

				}

			}
		}
	}




}
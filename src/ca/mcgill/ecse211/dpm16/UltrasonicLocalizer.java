package ca.mcgill.ecse211.dpm16;



import lejos.hardware.Sound;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
/**This class localizes the robot in the starting corner using the ultrasonic sensor
 * 
 */

public class UltrasonicLocalizer {
  private static final int ROTATE_SPEED = 100;
  private static final long LOOP_TIME = 5;
  ArrayList<Double[]> lastNValue = new ArrayList<>();
 
  private Odometer odometer;
  private SampleProvider usSensor;
  private float[] usData;
  private LocalizationType localizationType;
  private Navigation navigation;

  private EV3LargeRegulatedMotor leftMotor, rightMotor;


  private double alpha, beta;
  private long startTime, correctionStart, correctionEnd;
  private double biggest, smallest, maxTime, minTime;

  TextLCD t;

  /**Constructor for the ultrasonic localizer
   * @param odometer : current odometer running
   * @param  usSensor,usData : current sensor variables instantiated
   * @param localizationType : is falling/rising edge?
   * @param leftMotor,rigthMotor :robot motors
   */
  public UltrasonicLocalizer(
      Odometer odometer,
      SampleProvider usSensor,
      float[] usData,
      LocalizationType localizationType,
      Navigation navigation,
      EV3LargeRegulatedMotor leftMotor,
      EV3LargeRegulatedMotor rightMotor,
      TextLCD t) {
    this.odometer = odometer;
    this.usSensor = usSensor;
    this.usData = usData;
    this.localizationType = localizationType;
    this.navigation = navigation;

    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;

    resetMotor();
    this.t = t;
  }

  /** This is the meain method for localization. it sends the localization type to its corresponding method.
  
   *  */
  public void localize() {
    // checks the time at which the localization starts
    startTime = System.currentTimeMillis();

    // different methods depending on which edge was selected
    if (localizationType == LocalizationType.FALLING_EDGE) {
	  // Delay the start time for motor rotation to wait for Ultrasonic sensor to turn on
	  Delay.msDelay(2000);
	  
      findFallingEdge();
    } else {
  	  // Delay the start time for motor rotation to wait for Ultrasonic sensor to turn on
  	  Delay.msDelay(2000);
  	  
      findRisingEdge();
    }
  }

  /** method gets sensor readings and caps them at 100, wait time is manually done for more accuracy */
  private float getFilteredData() {
    correctionStart = System.currentTimeMillis();

    usSensor.fetchSample(usData, 0);
    float distance = usData[0] * 100;
    float returnedDistance;

    if (distance > 100) {
      returnedDistance = 100;
    } else {
      returnedDistance = distance;
    }

    //synchronization of smapling rate
    correctionEnd = System.currentTimeMillis();
    if (correctionEnd - correctionStart < LOOP_TIME) {
      try {
        Thread.sleep(LOOP_TIME - (correctionEnd - correctionStart));
      } catch (InterruptedException e) {
      }
    }

    return returnedDistance;
  }

  /** this method finds the alpha and beta for the falling edges, and updates the odometer. It rotates
   * the robot clockwise until it senses an edg, then counter clockwise till it seses another edge and
   * records alpha and beta and sends them to updating oodmeter method */
  public void findFallingEdge() {
    navigation.turnCW(360);

    // this takes care of checking if a falling edge has been crossed to assign it to alpha
    while (odometer.getXYT()[2] < MainController.TAU) {
      if (fallingEdgeCaught()) {
    	
        resetMotor();
        alpha = odometer.getXYT()[2];
        break;
      }
    }

    navigation.turnCCW(360);
    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (Exception e) {
    }

    // checks for the other falling edge to assign to beta
    while (360+odometer.getXYT()[2]>0) {
      if (fallingEdgeCaught()) {
        resetMotor();
        beta = odometer.getXYT()[2];
        break;
      }
    }

   
    updateOdometerTheta(alpha, beta);
  }

  /** Checks if there has been a falling edge by getting the sensor distance readings and comparing them with
   * previous ones, if the biggest distance came after the smallest one, then a falling edge has been detected*/
  public boolean fallingEdgeCaught() {
   //array that stores sensor readings to be compared
    lastNValueAdd(getFilteredData());
    lastNValueFind();

  
    if (biggest > 80 && smallest < 20 && maxTime < minTime) {
      this.lastNValue.clear();
      Sound.setVolume(30);
      Sound.beep();
      return true;
    } else {
      return false;
    }
  }

   /** this method finds the alpha and beta for the rising edges, and updates the odometer. It rotates
   * the robot clockwise until it senses an edg, then counter clockwise till it seses another edge and
   * records alpha and beta and sends them to updating oodmeter method */
  public void findRisingEdge() {
    navigation.turnCW(360);

    // this takes care of checking if a rising edge has been crossed to assign it to alpha
    while (odometer.getXYT()[2] < MainController.TAU) {
      if (risingEdgeCaught()) {
        resetMotor();
        alpha = odometer.getXYT()[2];
        break;
      }
    }

    navigation.turnCCW(360);

    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (Exception e) {
    }

    while (odometer.getXYT()[2] > 0) {
      if (risingEdgeCaught()) {
        resetMotor();
        beta = odometer.getXYT()[2];
        break;
      }
    }

    updateOdometerTheta(alpha, beta);
  }

  /** Checks if there has been a falling edge by getting the sensor distance readings and comparing them with
   * previous ones, if the biggest distance came before the smallest one, then a rising edge has been detected */
  public boolean risingEdgeCaught() {
   
    lastNValueAdd(getFilteredData());
    lastNValueFind();

  
    if (biggest > 80 && smallest < 20 && maxTime > minTime) {
      this.lastNValue.clear();
      Sound.setVolume(30);
      Sound.beep();
      return true;
    } else {
      return false;
    }
  }

  /** method sets the biggest distance and the smallest distance sensed, and their time in the array */
  public void lastNValueFind() {
    biggest = -200;
    smallest = 200;
    maxTime = -1000;
    minTime = 1000;

    for (int i = 0; i < this.lastNValue.size(); i++) {
      if (this.lastNValue.get(i)[0] > biggest) {
        biggest = this.lastNValue.get(i)[0]; //for all entries, find the max distance
        maxTime = lastNValue.get(i)[1];
      }
      if (this.lastNValue.get(i)[0] < smallest) {
        smallest = this.lastNValue.get(i)[0];  //for all entries find the min distance
        minTime = lastNValue.get(i)[1];
      }
    }
  }

  /** adding new distance to the array, removes old values to make space */
  public void lastNValueAdd(double value) {
    Double[] entry = {value, (double) System.currentTimeMillis() - startTime};
   
    if (this.lastNValue.size() > 40) {
     
      this.lastNValue.remove(0);
      this.lastNValue.add(entry);
    } else {
     
      this.lastNValue.add(entry);
    }
  }

  /** stop motors */
  private void resetMotor() {
    leftMotor.stop(true);
    rightMotor.stop(true);
   
    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
      motor.setAcceleration(3000);
      motor.setSpeed(ROTATE_SPEED);
    }
  }

  /** method calculates deltha Theta of the robot and uses navigation class methods to turn the robot to the right direction
   * by offsetting the odometer readings by that value
   * @param alpha: angle to sesne first wall
   * @param beta : angle to sense second wall
   * 
   */
  private void updateOdometerTheta(double alpha, double beta) {
    double dTheta;

  
    if (alpha < beta && localizationType == LocalizationType.FALLING_EDGE
        || alpha > beta && localizationType == LocalizationType.RISING_EDGE) {
      dTheta = (225) - ((alpha + beta) / 2); 
      Sound.setVolume(50);
      Sound.buzz();
    } else {
      dTheta = (45) - ((alpha + beta) / 2); 
    }


    odometer.setTheta(odometer.getXYT()[2] + dTheta);

    navigation.turnTo(0);
  }

  // enum
  public enum LocalizationType {
    FALLING_EDGE,
    RISING_EDGE
  }
}

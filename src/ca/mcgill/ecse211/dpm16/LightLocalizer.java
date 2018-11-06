package ca.mcgill.ecse211.dpm16;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;

import java.util.ArrayList;

/**
 * This class localizes the robot in the starting corner using the light sensor.
 * @author Reem
 *
 */
public class LightLocalizer {
  
  private static final int ROTATE_SPEED = 100;
  private static final long FREQUENCY = 10;
  private static final double D = 2;

  ArrayList<Double> readingsArr = new ArrayList<>();
  ArrayList<Double> thetas = new ArrayList<>();
  private Odometer odometer;
  private EV3ColorSensor colorSensor;
  private float[] colorData;
  private Navigation navigation;
  private EV3LargeRegulatedMotor leftMotor, rightMotor;
  private long correctionStart, correctionEnd, startTime;
  private double thisColor, prevColor, difference;
  private double y1, y2, x1, x2 = 0;
  private boolean firstLine = true;

  /**Constructor for the light sensor localizer
   * @param odometer : current odometer running
   * @param  colorSensor,colordata : current sensor variables instantiated
   * @param leftMotor,rigthMotor :robot motors
   */
  public LightLocalizer(
      Odometer odometer,
      EV3ColorSensor colorSensor,
      float[] colorData,
      Navigation navigation,
      EV3LargeRegulatedMotor leftMotor,
      EV3LargeRegulatedMotor rightMotor) {
    this.odometer = odometer;
    this.colorSensor = colorSensor;
    this.colorData = colorData;
    this.navigation = navigation;
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;

    resetMotor();
  }

  /** main method that has the flow logic. First we rotate the robot by 375 to make sure we cross alll lines.
   * we keep getting ensor readings and comparing the internsity different to catch the black lines
   * when we have caught 4 black lines we proceed to calculate the offset by the methods provided in the tutorial
   * we then correct the odometer reading and head to 0,0 */
  public void localize() {
    
    gotoEstimatedOrigin();
    startTime = System.currentTimeMillis();
    navigation.turnCW(360);
    while (odometer.getXYT()[2] < 360) {
      thisColor = getFilteredData();
      if (firstLine) {
        prevColor = thisColor;
        firstLine = false;
      }
    
      
      //difference between readings
      difference = thisColor - prevColor;
      prevColor = thisColor;

      // add difference to array to be compared later
      lastNValueAdd(difference);
      if (pastline()) { //if comparison mthod returns that there is a black line, add that and its angle
        thetas.add(odometer.getXYT()[2]);
        if (thetas.size() == 4) {
          break; //done when 4 lines are detected
        }
      }
    }

    //getting angles
    y1 = thetas.get(0);
    x1 = thetas.get(1);
    y2 = thetas.get(2);
    x2 = thetas.get(3);
    //The angle that subtends the arc connecting the intersections of
    //the light sensor's path with the y-axis/x-axis is theta calculated by subtracting both from each other
    double xOffset = -D * Math.cos((y2 - y1) / 2);
    double yOffset = -D * Math.abs(Math.cos((x2 - x1) / 2));

    // corrrect the odometer readings
    odometer.setX(xOffset);
    odometer.setY(yOffset);

    // travel to 0,0 using navigation class methods
    navigation.turnTo(0);
    navigation.travelTo(0, 0,false);
 
  }

  /** get reading from light sensor, manually synchronized for accuracy */
  private float getFilteredData() {
    correctionStart = System.currentTimeMillis();

    colorSensor.getRedMode().fetchSample(colorData, 0);
    float colorValue = colorData[0] * 100;

   
    correctionEnd = System.currentTimeMillis();
    if (correctionEnd - correctionStart < FREQUENCY) {
      try {
        Thread.sleep(FREQUENCY - (correctionEnd - correctionStart));
      } catch (InterruptedException e) {
      }
    }

    return colorValue;
  }

  /** checking if we pased a line, by comparing the differences between readings  if the difference ebtween the differences spiked,
   * we have a line*/
  public boolean pastline() {
    double biggest = -100;
    double smallest = 100;

    // since crossing a line causes a drop and a rise in the derivative, the filter
    // only considers a line crossed if the biggest value is higher than some threshold
    for (int i = 0; i < this.readingsArr.size(); i++) {
      // and the reverse for the lowest value, thus creating one beep per line
      if (this.readingsArr.get(i) > biggest) {
        biggest = this.readingsArr.get(i);
      }
      if (this.readingsArr.get(i) < smallest) {
        smallest = this.readingsArr.get(i);
      }
    }

    // if a sample is considered to be a line, the array is cleared as to not retrigger an other
    // time
    if (biggest > 5 && smallest < -3) {
      this.readingsArr.clear();
      Sound.setVolume(30);
      Sound.beep();
      return true;
    } else {
      return false;
    }
  }

  /** adding to the array of readings */
  public void lastNValueAdd(double value) {

    if (this.readingsArr.size() > 40) {
      // the oldest value is removed to make space

      this.readingsArr.remove(0);
      this.readingsArr.add(value);
    } else {
  

      this.readingsArr.add(value);
    }
  }

  /**
   * method orientes towards the 45, advances a bit, turn back to the 0 degree to have a better
   * chance of catching all the lines
   */
  public void gotoEstimatedOrigin() {
    navigation.turnTo(MainController.TAU / 8);
    navigation.advance(15);
    navigation.turnTo(0);
  }

  /** method resets the motors */
  private void resetMotor() {
    leftMotor.stop(true);
    rightMotor.stop(true);
 
    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
      motor.setAcceleration(3000);
      motor.setSpeed(ROTATE_SPEED);
    }
  }
}
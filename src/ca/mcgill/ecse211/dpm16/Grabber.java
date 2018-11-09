package ca.mcgill.ecse211.dpm16;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.utility.Delay;

/**This class is used to determine the color of the detected ring.
 * @author Aljulanda
 * @author Reem
 */
public class Grabber {
	private EV3LargeRegulatedMotor rampMotor;
	
	public Grabber(EV3LargeRegulatedMotor rampMotor) {
		this.rampMotor = rampMotor; 
		this.rampMotor.setSpeed(50);
	}
	
	/**
	 * The method lowers the ramp by angle degrees
	 */
	public void move(int angle) {
		rampMotor.rotate(angle);
		Delay.msDelay(1000);
	}
}

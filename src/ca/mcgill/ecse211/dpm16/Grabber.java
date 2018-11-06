package ca.mcgill.ecse211.dpm16;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**This class is used to determine the color of the detected ring.
 * @author Aljulanda
 * @author Reem
 */
public class Grabber {
	private EV3LargeRegulatedMotor rampMotor;
	
	public Grabber(EV3LargeRegulatedMotor rampMotor) {
		this.rampMotor = rampMotor; 
	}
	
	/**
	 * The method lowers the ramp by 90 degrees
	 */
	public void rest() {
		rampMotor.rotate(90);
	}
	
	/**
	 * The method lifts the ramp by 90 degrees
	 */
	public void lift() {
		rampMotor.rotate(-90);
	}
}

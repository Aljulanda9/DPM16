package ca.mcgill.ecse211.dpm16;

import java.util.Arrays;

import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;


/**This class is used to determine the color of the detected ring.
 * @author Aljulanda
 */

public class ColorDetector{

	private EV3ColorSensor cSensor;
	private float[] colorSample;

	//average NORMALIZED values of RGB for each ring. Data was collected by the test engineer.
	//all color data to be used in the class are NORMALIZED
	private double mBlueR = 0.1732410055;
	private double mBlueG = 0.6778531281;
	private double mBlueB = 0.7144947101;
	private double mGreenR = 0.4777487339;
	private double mGreenG = 0.8592604804;
	private double mGreenB = 0.1828320925;
	private double mYellowR = 0.8541708187;
	private double mYellowG = 0.5005476676;
	private double mYellowB = 0.140869603;
	private double mOrangeR = 0.9547663589;
	private double mOrangeG = 0.2766071505;
	private double mOrangeB = 0.1091314998;

	private float R;
	private float G; 
	private float B;

	private double nR; 
	private double nG;
	private double nB;

	private double Blue;
	private double Green;
	private double Yellow;
	private double Orange;
	
	/*
	 * Calculations explanation:
	 * 1- Calculate mean normalized RGB data for each ring
	 * 2- Collect RGB color sample using the sample.
	 * 3- Normalize the collected sample.
	 * 4- Calculate the deviation from the mean data for each ring.
	 * 5- The ring with the smallest deviation is the found color.
	 */
	
	public ColorDetector(EV3ColorSensor cSensor) {
		this.cSensor = cSensor;
	}
	
	/**
	 * The method returns the number corresponding to each ring as in the project specification document.
	 * 1 for blue, 2 for green, 3 for yellow and 4 for orange. The method also beeps according to the number 
	 * of the ring.
	 * @return the number of the found ring
	 */
	public int detect() {

		cSensor.setFloodlight(true);
		SampleProvider csColors = cSensor.getRGBMode();
		colorSample = new float[csColors.sampleSize()];

		while(true) {
			csColors.fetchSample(colorSample, 0);
			R = colorSample[0];
			G = colorSample[1];
			B = colorSample[2];
			
			//normalize
			nR = R/(Math.sqrt(R*R + G*G + B*B));
			nG = G/(Math.sqrt(R*R + G*G + B*B));
			nB = B/(Math.sqrt(R*R + G*G + B*B));

			//calculate deviation for each ring
			Blue = Math.sqrt(Math.pow(nR - mBlueR, 2) + Math.pow(nG - mBlueG, 2) + Math.pow(nB - mBlueB, 2));
			Green = Math.sqrt(Math.pow(nR - mGreenR, 2) + Math.pow(nG - mGreenG, 2) + Math.pow(nB - mGreenB, 2));
			Yellow = Math.sqrt(Math.pow(nR - mYellowR, 2) + Math.pow(nG - mYellowG, 2) + Math.pow(nB - mYellowB, 2));
			Orange = Math.sqrt(Math.pow(nR - mOrangeR, 2) + Math.pow(nG - mOrangeG, 2) + Math.pow(nB - mOrangeB, 2));

			double[] list = {Blue, Green, Yellow, Orange};

			//sorted array
			Arrays.sort(list);

			//return true if list[0] is the required color
			
			if(list[0] == Blue) {
				Sound.beep();
				return 1;
			}
			
			if(list[0] == Green) {
				Sound.beep();
				Sound.beep();
				return 2;
			}
			
			if(list[0] == Yellow) {
				Sound.beep();
				Sound.beep();
				Sound.beep();
				return 3;
			}
			
			if(list[0] == Orange) {
				Sound.beep();
				Sound.beep();
				Sound.beep();
				Sound.beep();
				return 4;
			}
			return 0;
		}

	}

}

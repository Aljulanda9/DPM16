package ca.mcgill.ecse211.dpm16;

import java.util.Arrays;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.hardware.lcd.TextLCD;

/*
 * A class to classify the color of the rings into Green, Blue,
 * Yellow and Orange. The class detects the ring the displays the color
 */
public class ColorClassification implements Runnable {

	private EV3ColorSensor cSensor;
	private float[] colorSample;
	private float[] usData;
	private SensorModes usSensor;
	private TextLCD t;
	private float distance;

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

	private double dBlue;
	private double dGreen;
	private double dYellow;
	private double dOrange;
	
	/*
	 * Calculations explanation:
	 * 1- Calculate mean normalized RGB data for each ring
	 * 2- Collect RGB color sample using the sample.
	 * 3- Normalize the collected sample.
	 * 4- Calculate the deviation from the mean data for each ring.
	 * 5- The ring with the smallest deviation is the found color.
	 */

	public ColorClassification(EV3ColorSensor cSensor, SensorModes usSensor, float[] usData, TextLCD t) {
		this.cSensor = cSensor;
		this.usSensor = usSensor;
		this.usData = usData;
		this.t = t;
	}


	@Override
	public void run() {

		cSensor.setFloodlight(true);
		SampleProvider csColors = cSensor.getRGBMode();
		colorSample = new float[csColors.sampleSize()];

		while(true) {

			usSensor.fetchSample(usData, 0);
			distance = usData[0] * 100;
			String display = "";
			
			//detect color only if object is within 15cm 
			if(distance < 15) {
				t.clear();
				
				t.drawString("Object Detected", 0, 0);
				t.drawString("Distance = " + distance, 0, 2);
				csColors.fetchSample(colorSample, 0);
				
				R = colorSample[0];
				G = colorSample[1];
				B = colorSample[2];

				//normalize
				nR = R/(Math.sqrt(R*R + G*G + B*B));
				nG = G/(Math.sqrt(R*R + G*G + B*B));
				nB = B/(Math.sqrt(R*R + G*G + B*B));

				//calculate deviation for each ring
				dBlue = Math.sqrt(Math.pow(nR - mBlueR, 2) + Math.pow(nG - mBlueG, 2) + Math.pow(nB - mBlueB, 2));
				dGreen = Math.sqrt(Math.pow(nR - mGreenR, 2) + Math.pow(nG - mGreenG, 2) + Math.pow(nB - mGreenB, 2));
				dYellow = Math.sqrt(Math.pow(nR - mYellowR, 2) + Math.pow(nG - mYellowG, 2) + Math.pow(nB - mYellowB, 2));
				dOrange = Math.sqrt(Math.pow(nR - mOrangeR, 2) + Math.pow(nG - mOrangeG, 2) + Math.pow(nB - mOrangeB, 2));

				double[] list = {dBlue, dGreen, dYellow, dOrange};

				//sorted array
				Arrays.sort(list);

				//print list[0] which is going to the detected color
				if(list[0] == dBlue) {
					display = "Blue";
				}

				if(list[0] == dGreen) {
					display = "Green";
				}

				if(list[0] == dYellow) {
					display = "Yellow";
				}

				if(list[0] == dOrange) {
					display = "Orange";
				}
				
				t.drawString(display, 0, 3);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				t.clear();
			}
			
		}
	}

}
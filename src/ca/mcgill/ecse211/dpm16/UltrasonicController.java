package ca.mcgill.ecse211.dpm16;

public interface UltrasonicController {

  public void processUSData(int distance);

  public int readUSDistance();
}

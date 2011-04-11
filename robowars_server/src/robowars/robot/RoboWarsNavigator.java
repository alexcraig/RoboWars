package robowars.robot;



import lejos.robotics.Pose;
import lejos.robotics.navigation.Pilot;
/*
 * Subclass of simple navigator overrides the steer function
 */
public class RoboWarsNavigator extends SimpleNavigator{

	public RoboWarsNavigator(Pilot pilot) {
		super(pilot);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Function to perform the steer command updates position and resets the pilot
	 * If there has been a parity switch
	 * @param turnBearing
	 * @param throttle
	 */
	public void steer(float turnBearing, float throttle) {
		updatePose();
		if(throttle<0&&super.getPilot().getParity()!=1){
			super.getPilot().reset();
			_distance0=0;
			_angle0=0;
		}
		if(throttle>0&&super.getPilot().getParity()!=-1){
			super.getPilot().reset();
			_distance0=0;
			_angle0=0;
		}
	    super.setCurrent(false);
	    super.getPilot().robowarsSteer(turnBearing, throttle);
	}
}

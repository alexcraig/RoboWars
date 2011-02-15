package robowars.robot;

import lejos.robotics.navigation.Pilot;

public class RoboWarsNavigator extends SimpleNavigator{

	public RoboWarsNavigator(Pilot pilot) {
		super(pilot);
		// TODO Auto-generated constructor stub
	}

	public void steer(float turnBearing, float throttle) {
	    updatePose();
	    super.setCurrent(false);
	    super.getPilot().robowarsSteer(turnBearing, throttle);
	}
}

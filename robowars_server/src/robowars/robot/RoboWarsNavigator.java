package robowars.robot;



import lejos.robotics.Pose;
import lejos.robotics.navigation.Pilot;

public class RoboWarsNavigator extends SimpleNavigator{

	public RoboWarsNavigator(Pilot pilot) {
		super(pilot);
		// TODO Auto-generated constructor stub
	}

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

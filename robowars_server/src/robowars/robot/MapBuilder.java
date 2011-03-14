package robowars.robot;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import robowars.shared.model.MapPoint;
import robowars.shared.model.RobotMap;

public class MapBuilder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			LejosOutputStream out=new LejosOutputStream(new FileOutputStream("colorMap.txt"));
			RobotMap map=new RobotMap();
			for(int r=20; r>-1; r--){
				for(int c=20; c>-1; c--){
					if(r%2==0&&c%2==0)map.addPoint(new MapPoint(c*2, r*2, lejos.robotics.Colors.BLUE));
					else if(r%2==1&&c%2==0)map.addPoint(new MapPoint(c*2, r*2, lejos.robotics.Colors.RED));
					else if(r%2==0&&c%2==1)map.addPoint(new MapPoint(c*2, r*2, lejos.robotics.Colors.YELLOW));
					else if(r%2==1&&c%2==1)map.addPoint(new MapPoint(c*2, r*2, lejos.robotics.Colors.GREEN));
				}
			}
			out.writeObject(map);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

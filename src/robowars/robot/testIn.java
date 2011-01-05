package robowars.robot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import robowars.shared.model.RobotCommand;

public class testIn {
	public static void main (String args[]){
		LejosInputStream o = null;
		try {
			o=new LejosInputStream(new FileInputStream("test.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println(o.readObject());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

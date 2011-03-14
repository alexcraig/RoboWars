package robowars.test;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Date;

import javax.swing.JFrame;

import lejos.robotics.Pose;

import robowars.robot.Listener;
import robowars.server.view.Admin2DGameView;
import robowars.shared.model.*;

public class ModelSimulator implements KeyListener, GameListener{
	
	private GameModel model;

	public ModelSimulator(GameType type){
		
		switch(type){
			case LIGHTCYCLES:
				model = new LightCycles();break;
			case TANK_SIMULATION:
				model = new TankSimulation();break;
			case FREETEST:
				model = new FreeTest();break;
		}
		
		JFrame frame=new JFrame();
		frame.addKeyListener(this);
		frame.setVisible(true);
		
		model.addListener(this);
		model.addRobot(new GameRobot("robot1"));
		model.addRobot(new GameRobot("robot2", new Pose(400,400,180), 1));
		model.addListener(new Admin2DGameView(600, model));
		
		
	}
	
	public static void main(String args[]){
		new ModelSimulator(GameType.parseString(args[0]));
		
	}
	
	@Override
	public void gameStateChanged(GameEvent e){
		
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode()==KeyEvent.VK_UP){
			System.out.println("Forward");
			Pose newPose = new Pose(model.getGameRobot("robot1").getPose().getX(),
					model.getGameRobot("robot1").getPose().getY(),
					model.getGameRobot("robot1").getPose().getHeading());
			newPose.moveUpdate(3);
			model.updateRobotPosition("robot1", newPose);
			model.updateGameState(1);
		}
		if(arg0.getKeyCode()==KeyEvent.VK_LEFT){
			System.out.println("Left");
			Pose newPose = new Pose(model.getGameRobot("robot1").getPose().getX(),
					model.getGameRobot("robot1").getPose().getY(),
					model.getGameRobot("robot1").getPose().getHeading());
			newPose.rotateUpdate(3);
			model.updateRobotPosition("robot1", newPose);
			model.updateGameState(1);
		}
		if(arg0.getKeyCode()==KeyEvent.VK_RIGHT){	
			System.out.println("Right");
			Pose newPose = new Pose(model.getGameRobot("robot1").getPose().getX(),
					model.getGameRobot("robot1").getPose().getY(),
					model.getGameRobot("robot1").getPose().getHeading());
			newPose.rotateUpdate(-3);
			model.updateRobotPosition("robot1", newPose);
			model.updateGameState(1);
		}
		if(arg0.getKeyCode()==KeyEvent.VK_W){
			System.out.println("Forward");
			Pose newPose = new Pose(model.getGameRobot("robot2").getPose().getX(),
					model.getGameRobot("robot2").getPose().getY(),
					model.getGameRobot("robot2").getPose().getHeading());
			newPose.moveUpdate(3);
			model.updateRobotPosition("robot2", newPose);
			model.updateGameState(1);
		}
		if(arg0.getKeyCode()==KeyEvent.VK_A){
			System.out.println("Right");
			Pose newPose = new Pose(model.getGameRobot("robot2").getPose().getX(),
					model.getGameRobot("robot2").getPose().getY(),
					model.getGameRobot("robot2").getPose().getHeading());
			newPose.rotateUpdate(3);
			model.updateRobotPosition("robot2", newPose);
			model.updateGameState(1);
		}
		if(arg0.getKeyCode()==KeyEvent.VK_D){
			System.out.println("Left");
			Pose newPose = new Pose(model.getGameRobot("robot2").getPose().getX(),
					model.getGameRobot("robot2").getPose().getY(),
					model.getGameRobot("robot2").getPose().getHeading());
			newPose.rotateUpdate(-3);
			model.updateRobotPosition("robot2", newPose);
			model.updateGameState(1);
		}
		if(arg0.getKeyCode()==KeyEvent.VK_S){
			System.out.println("Stop");
			//dataOut.writeObject(RobotCommand.stop());
		}
		if(arg0.getKeyCode()==KeyEvent.VK_X){
			System.out.println("Exit");
			//dataOut.writeObject(RobotCommand.exit());
			System.exit(0);
		}
		if(arg0.getKeyCode()==KeyEvent.VK_DOWN){
			
		}
	}
	
	public void keyTyped(KeyEvent e){
		
	}
	
	public void keyReleased(KeyEvent e){
		
	}
}

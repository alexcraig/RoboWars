package robowars.server.view;

import robowars.shared.model.GameListener;
import robowars.shared.model.GameEvent;
import robowars.shared.model.GameModel;
import robowars.shared.model.GameRobot;
import robowars.shared.model.GameEntity;
import robowars.shared.model.Obstacle;

import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.image.BufferStrategy;

public class Admin2DGameView extends JFrame implements GameListener{

	private Canvas canvas;
	private GameModel model;
	private Graphics2D buffedG2D;
	
	//NOTE: Currently the view and the model itself only supports square arenas.
	public Admin2DGameView(int size, GameModel model){
		super("Admin Game View");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
		   try {
		        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		    }
		    catch (Exception e2) {
		        System.err.println("Unable to load default look and feel.");
		        System.exit(1); // Might not want to exit
		    }
		}
		this.model = model;
		this.setMinimumSize(new Dimension(size,size));
		this.canvas = new Canvas();
		this.getContentPane().add(canvas);
		canvas.setBackground(Color.BLACK);
		buffedG2D = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();

		
		drawState();
		
		this.pack();
        this.setResizable(false);
		this.setVisible(true);
	}
	
	private void drawState(){
		drawPlayers(buffedG2D);
		drawWalls(buffedG2D);
		
	}
	
	private void drawPlayers(Graphics2D g2d){
		g2d.setColor(Color.BLUE);
		for(GameRobot r : model.getGameRobotList()){
			g2d.drawRect((int) r.getPose().getX(), (int) r.getPose().getY(), (int) r.getWidth(), (int) r.getLength());
		}
	}
	
	private void drawWalls(Graphics2D g2d){
		g2d.setColor(Color.YELLOW);
		for(GameEntity e : model.getEntities()){
			if(e instanceof Obstacle)
				g2d.drawRect((int) e.getPose().getX(), (int) e.getPose().getY(), (int) e.getWidth(), (int) e.getLength());
		}
	}
	
	public void gameStateChanged(GameEvent e) {
		drawState();
	}
}

package robowars.server.view;

import robowars.shared.model.GameListener;
import robowars.shared.model.GameEvent;
import robowars.shared.model.GameModel;
import robowars.shared.model.GameRobot;
import robowars.shared.model.GameEntity;
import robowars.shared.model.Obstacle;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;

public class Admin2DGameView extends JFrame implements GameListener{
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(Admin2DGameView.class);

	private Canvas canvas;
	private GameModel model;
	private Graphics2D buffedG2D;
	private int size;
	
	//NOTE: Currently the view and the model itself only supports square arenas.
	public Admin2DGameView(int size, GameModel model){
		super("Admin Game View");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.model = model;
		this.size = size;
		this.setMinimumSize(new Dimension(size,size));
		this.canvas = new Canvas();
		this.getContentPane().add(canvas);
		canvas.setBackground(Color.BLACK);
		
		this.pack();
        this.setResizable(false);
		this.setVisible(true);
		
		// Note: Buffers must be created AFTER the canvas is visible (otherwise an
		// IllegalStateException will be thrown)
		canvas.createBufferStrategy(2);
		buffedG2D = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
		drawState();
	}
	
	private void drawState(){
		// Note: The transforms here have to do with LeJOS and AWT using different
		// co-ordinate systems. AWT places the origin in the top left of the canvas,
		// with the y-axis increasing toward the bottom of the screen. However, with
		// the y-axis in this orientation the directions of turns produced through
		// LeJOS will appear to be reversed. To fix this, a transform is used
		// to invert the y-axis and translate the origin to the bottom left corner
		// of the canvas.
		
		// Get the current transform matrix
		AffineTransform saveAT = buffedG2D.getTransform();
		
		// Flip Y coordinates and translate origin
		AffineTransform reflection = new AffineTransform();
		reflection.setToScale(1, -1);
		AffineTransform translation = new AffineTransform();
		translation.setToTranslation(0, canvas.getHeight());
		buffedG2D.transform(translation);
		buffedG2D.transform(reflection);
		
		// Render
		buffedG2D.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		drawPlayers(buffedG2D);
		drawWalls(buffedG2D);
		
		// Restore original transform matrix
		buffedG2D.setTransform(saveAT);
		
		// Display and flip buffers
		canvas.getBufferStrategy().show();
		buffedG2D = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
	}
	
	private void drawPlayers(Graphics2D g2d) {
		for(GameRobot r : model.getGameRobotList()) {
			// Draw the "body" of the robot
			g2d.setColor(Color.BLUE);
			g2d.fillOval((int) r.getPose().getX(), (int) r.getPose().getY(), (int) r.getWidth(), (int) r.getLength());
			
			// Draw the "nose" of the robot
			g2d.setColor(Color.ORANGE);
			g2d.fillOval((int) ((r.getPose().getX() + 2*r.getWidth()/5) + Math.cos(Math.toRadians(r.getPose().getHeading())) * (r.getWidth()/2)),
					(int) ((r.getPose().getY() + 2*r.getLength()/5) + Math.sin(Math.toRadians(r.getPose().getHeading())) * (r.getLength()/2)),
					(int) (r.getWidth() / 5),
					(int) (r.getLength() / 5));
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

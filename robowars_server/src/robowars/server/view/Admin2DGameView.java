package robowars.server.view;

import robowars.shared.model.GameListener;
import robowars.shared.model.GameEvent;
import robowars.shared.model.GameModel;
import robowars.shared.model.GameRobot;
import robowars.shared.model.GameEntity;
import robowars.shared.model.Obstacle;
import robowars.shared.model.Posture;

import java.util.ArrayList;

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
	
	/** 
	 * A scaling factor used to reconcile differences in the size of the
	 * arena and the size of the 2D display window.
	 */
	private float scalingFactor;

	private Canvas canvas;
	private GameModel model;
	private Graphics2D buffedG2D;
	private int size;
	private boolean collisionFlag;
	private ArrayList<Posture> hitMarkers;
	
	//NOTE: Currently the view and the model itself only supports square arenas.
	public Admin2DGameView(int size, GameModel model){
		super("Admin Game View");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
        // Note: This will crash if a arena of size 0 is ever used
        this.scalingFactor = size / model.getArenaSize();
        
		this.model = model;
		this.size = size;
		this.hitMarkers = new ArrayList<Posture>();
		this.setMinimumSize(new Dimension(size,size + 30));
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
		drawEntities(buffedG2D);
		if(collisionFlag){
			drawCollision(buffedG2D);
		}
		drawMarkers(buffedG2D);
		
		// Restore original transform matrix
		buffedG2D.setTransform(saveAT);
		
		// Display and flip buffers
		canvas.getBufferStrategy().show();
		buffedG2D = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
	}
	
	private void drawEntities(Graphics2D g2d){
		for(GameEntity e : model.getEntities()){
			System.out.println("Centre: (" + e.getPosture().getX() + "," + e.getPosture().getY() + "," + e.getPosture().getHeading() + ")");
			
			int n = e.getVertices().length;
			int x[] = new int[n];
			int y[] = new int[n];
			e.getCoordArrays(x, y);
			//System.out.print("Vertices: ");
			//for (int i = 0; i < n; i++){
			//	System.out.print("(" + x[i] + "," + y[i] + ")");
			//}
			//System.out.print("/n");
			
			// Scaling factor implementation
			for(int i = 0; i < n; i++) {
				x[i] = (int)(x[i] * scalingFactor);
				y[i] = (int)(y[i] * scalingFactor);
			}
			
			g2d.setColor(Color.BLUE);
			g2d.fillPolygon(x, y, n);
			g2d.setColor(Color.ORANGE);
			g2d.fillOval((int)(e.getPosture().getX() * scalingFactor), 
					(int)(e.getPosture().getY() * scalingFactor), 5, 5);
		}
	}
	
	private void drawCollision(Graphics2D g2d){
		g2d.setColor(Color.RED);
		g2d.fillOval(20, 30, 20, 20);
	}
	
	private void drawMarkers(Graphics2D g2d){
		g2d.setColor(Color.YELLOW);
		for(Posture m : hitMarkers){
			g2d.fillOval((int)(m.getLocation().getX() * scalingFactor), 
					(int)(m.getLocation().getY() * scalingFactor), 10, 10);
		}
	}
	
	public void gameStateChanged(GameEvent e) {
		drawState();
		
		if(e.getEventType() == GameEvent.PROJECTILE_HIT){
			//hitMarkers.add(e.getEventCause().clonePosture());
		}
		
		if(e.getEventType() == GameEvent.COLLISION_DETECTED){
			collisionFlag = true;
		}else{
			collisionFlag= false;
		}
	}
}

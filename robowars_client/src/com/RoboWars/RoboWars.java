package com.RoboWars;

import java.util.Observable;
import java.util.Observer;

import com.RoboWars.opengl.MediaClient;
import com.RoboWars.opengl.OpenGLRenderer;
import com.RoboWars.opengl.Point3D;
import com.RoboWars.opengl.mesh.Cube;
import com.RoboWars.opengl.mesh.SimplePlane;

import robowars.server.controller.ClientCommand;
import robowars.shared.model.GameEntity;
import robowars.shared.model.GameModel;
import robowars.shared.model.Obstacle;
import robowars.shared.model.Posture;
import robowars.shared.model.Vector;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class RoboWars extends Activity implements SensorEventListener, Observer
{	
	/** The minimum interval at which orientation updates should be pushed to the server */
	public static final long ORIENTATION_MINIMUM_INTERVAL_MS = 300;
	
	/** The maximum interval at which orientation updates should be pushed to the server */
	public static final long ORIENTATION_MAXIMUM_INTERVAL_MS = 2000;
	
	/** 
	 * The threshold for input change that should prompt an immediate update
	 * to the server (all orientations are scaled to a range of -1 to 1 before
	 * this comparison is applied).
	 */
	public static final float ORIENTATION_DELTA_THRESHOLD = (float)0.10;
	
	/* The TabHost. */
	TabHost tabHost;
	
	/* Views invoked by the application. */
	private TextView chat, users;
	private EditText entry, server, port, user;
	private Button	 launch;
	private CheckBox spectatorButton, readyButton;
	
	/* Check if we're ready/spectator. */
	private boolean spectator, ready;
	
	/** The time at which the reading of the orientation sensor was last updated */
	private long lastOrientationUpdate;
	
	private LobbyModel lobbyModel;		// Model for the lobby.
	private com.RoboWars.ClientGameModel gameModel;		// Model for the game.
	private TcpClient tcp;				// TCP controller.
	
	private String userlist;		// Users currently in the lobby.
	
	private SensorManager mSensorManager;	// Manages the accelerometer and other sensors.
    
    TextView mTextViewAcc;	// Text view for accelerometer.
    TextView mTextViewMag;	// Text view for magnetic field.
    TextView mTextViewOri;	// Text view for orientation.
	
	private static final int MAX_LINES = 12;	// Max lines to show in the chat lobby.

	/** MediaPlayer used to display streaming video */
	private MediaClientTab mediaClient;
	
	/** The last values for each orientation vector that were sent to the server */
	private double lastAzimuth, lastPitch, lastRoll;
	
	// OpenGL Variables.
	private boolean inOpenGL;
	
	private OpenGLRenderer renderer;
	private MediaClient mClient;
	private SimplePlane plane;

	private Point3D currentDrawCoords;
	
    /**
     * Creates a tab view.
     */
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	tabHost=(TabHost)findViewById(R.id.tabHost);
    	tabHost.setCurrentTab(0);
    	tabHost.setup();
    	tabHost.setKeepScreenOn(true);
    	
    	/* First tab (Webcam interface; control the robot from here). */
    	TabSpec spec1=tabHost.newTabSpec("Main");
    	spec1.setIndicator("Main");
    	spec1.setContent(R.id.tab1);
    	
    	/* Second tab (Lobby interface, contains chat and userlist). */
    	TabSpec spec2=tabHost.newTabSpec("Lobby");
    	spec2.setIndicator("Lobby");
    	spec2.setContent(R.id.tab2);

    	/* Third tab (Configuration, contains username and server information). */
    	TabSpec spec3=tabHost.newTabSpec("Config");
    	spec3.setIndicator("Config");
    	spec3.setContent(R.id.tab3);
    	
    	/* Fourth tab (MediaPlayer). */
    	TabSpec spec4=tabHost.newTabSpec("Video");
    	spec4.setIndicator("Video");
    	spec4.setContent(R.id.tab4);

    	/* Add the tabs to the tabHost. */
    	tabHost.addTab(spec1);
    	tabHost.addTab(spec2);
    	tabHost.addTab(spec3);
    	tabHost.addTab(spec4);
    	
    	/* Instantiate the model and add the view as an observer. */
    	lobbyModel = new LobbyModel();
    	gameModel = new ClientGameModel();
    	lobbyModel.addObserver(this);
    	gameModel.addObserver(this);
    	
    	lobbyModel.setVersion(getString(R.string.version));
    	
    	/* Reference to the application's widgets. */
    	chat 			= (TextView) findViewById(R.id.chat);
    	users			= (TextView) findViewById(R.id.users);
    	entry 			= (EditText) findViewById(R.id.entry);
    	server 			= (EditText) findViewById(R.id.server);
    	port 			= (EditText) findViewById(R.id.port);
    	user			= (EditText) findViewById(R.id.username);
    	launch			= (Button)	 findViewById(R.id.launchButton);
    	spectatorButton = (CheckBox) findViewById(R.id.spectatorCheckBox);
    	readyButton 	= (CheckBox) findViewById(R.id.readyCheckBox);
    	
        
        /* Setup the sensor manager. */
        
        // Use these lines if using Android emulator.
        
        //mSensorManager = SensorManagerSimulator.getSystemService(this, SENSOR_SERVICE);
        //mSensorManager.connectSimulator();

        // Use this line if using phone application.
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

    	/* Allow scrolling of the chat and user list. */
    	chat.setMovementMethod(new ScrollingMovementMethod());
    	users.setMovementMethod(new ScrollingMovementMethod());
    	
    	lastOrientationUpdate = 0;
    	
    	/* Setup the media client */
    	ImageStreamView mediaView = (ImageStreamView) findViewById(R.id.mediaSurface);
    	TextView mediaStatus = (TextView) findViewById(R.id.mediaStatus);
    	mediaClient = new MediaClientTab(mediaView, mediaStatus);
    	
    	lastAzimuth = 0;
    	lastPitch = 0;
    	lastRoll = 0;
    	
    	/* Initially blank user list. */
    	userlist = "";
    	
    	/* Initially not ready and not spectator. */
    	ready = spectator = false;
    	inOpenGL = false;
    	launch.setEnabled(false);
    }
    
    /**
     * Print a message in the chat lobby.
     * @param msg
     */
    public void printMessage(final String msg, int type)
    {
    	this.runOnUiThread(new Runnable(){
    		public void run(){
    			chat.append(msg + "\n");
    			if (chat.getLineCount() > MAX_LINES) chat.scrollBy(0, chat.getLineHeight());
            }
        });
    }
    
    /**
     * Refreshes the lobby user list.
     */
    public void updateUsers()
    {   
    	this.runOnUiThread(new Runnable(){
    		public void run(){
    			users.setText("");
    			users.append(userlist);
            }
        });
    }
    
    /**
     * Method called whenever a button is pressed.
     * Determines which button was pressed by getting the
     * view's ID, then act accordingly.
     */
    public void buttonClicked(View view)
    {
    	ClientCommand cmd;
    	
    	switch (view.getId())
    	{
    		/* Connect (to TCP server) button. */
    		case (R.id.connect):
    			
    			// Obtain login information.
	    		String username = user.getText().toString();
	        	String address 	= server.getText().toString();
	        	int portNumber	= Integer.parseInt(port.getText().toString());
	        	
	        	// Disable the 'Connect' button.
	        	view.setEnabled(false);
	        	
	        	// Establish connection.
	        	lobbyModel.setMyUser(new User(username));
	        	tcp = new TcpClient(lobbyModel, gameModel);
	        	tcp.connect(address, portNumber);
	        	
				mediaClient.launchMediaStream(portNumber + 1);
	        	break;
	        
	        /* Send button. */
    		case (R.id.send):
    			
    			// Get the text and set the entry field blank.
    			String message = entry.getText().toString();
        		entry.setText("");
        		
        		// Try to generate a client command, send the string as a
        		// chat message if command generation fails
        		//ClientCommand cmd = ClientCommand.parse(message);
        		if (tcp != null)
        		{
	        		cmd = new ClientCommand(ClientCommand.CHAT_MESSAGE);
	        		cmd.setStringData(message);
	        		tcp.sendClientCommand(cmd);
        		}
        		break;
        	
        	/* Spectator checkbox. If there is a TCP connection, inform the server
        	 * of the status change. */
    		case (R.id.spectatorCheckBox):
    			
    			if (spectator == true) spectator = false;
    			else spectator = true;
    			
    			if (tcp != null)
    			{
    				cmd = new ClientCommand(ClientCommand.SPECTATOR_STATUS);
    				cmd.setBoolData(spectator);
    				tcp.sendClientCommand(cmd);
    			}
    			break;
    			
    		/* Ready checkbox. If there is a TCP connection, inform the server
        	 * of the status change.*/
    		case (R.id.readyCheckBox):
    			
    			if (ready) {
    				ready = false;
    				launch.setEnabled(false);
    			}
    			else {
    				ready = true;
    				launch.setEnabled(true);
    			}
    		
	    		if (tcp != null)
				{
					cmd = new ClientCommand(ClientCommand.READY_STATUS);
					cmd.setBoolData(ready);
					tcp.sendClientCommand(cmd);
				}
    			break;
        		
    		/* Launch button. */
    		case (R.id.launchButton):
    		
			if (tcp != null)
			{
				cmd = new ClientCommand(ClientCommand.LAUNCH_GAME);
				cmd.setBoolData(true);
				tcp.sendClientCommand(cmd);
				
				ready = false;
				spectator = false;
				
				spectatorButton.setChecked(false);
				readyButton.setChecked(false);
				
				tabHost.setCurrentTab(R.id.tab4);
			}
    		break;
    		
    		case (R.id.goOpenGL):
    			
    			if (!inOpenGL) goToOpenGLView();
    			break;
	        	
	        default:
	        	printMessage("Unknown button pressed.", LobbyModel.ERROR);
    	}
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(this);
		super.onStop();
	}

	/**
	 * Updates based on changes to the model.
	 */
	public void update(Observable observable, Object data)
	{
		// OpenGL change.
		if (data instanceof GameModel)
		{
			// Check if the game finished. If so, stop the game.
			if (!gameModel.gameInProgress()) {
				goToLobbyView();
				return;
			}
			
			// Check if we're in OpenGL... If not, go into OpenGL and draw the walls.
			if (!inOpenGL)
			{
				goToOpenGLView();
				for (Obstacle e : gameModel.getObstacles()) drawGameEntity(e, 20f);
			}
			
			// Draw the players and projectiles.
			else
			{
				
			}
			
						
		}
		
		// Check for new chat message.
		if (lobbyModel.newChatMessage())
		{
			printMessage(lobbyModel.getChatBuffer(), lobbyModel.getChatBufferType());
		}
		
		// Check for changes in the user list.
		if (lobbyModel.usersUpdated())
		{
			userlist = lobbyModel.getUsers();
			updateUsers();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO:	Currently assumes orientation events are the only events
		//			that will be received.
		
		// Scale all values to 1 to -1 range
		float azimuth = (event.values[0] - 180) / 180; // Azimuth sensor standard range 0 to 360
		float pitch = (clamp(-event.values[2], -45, 45) / 45);	// Pitch sensor standard range -90 to 90
		float roll = (clamp(event.values[1], -45, 45) / 45);	// Roll sensor standard range -180 to 180

		// Set the text display on client side
		/*
		mTextViewOri.setText("Orientation:\n" 
				+ "Azimuth:  " + azimuth + " (" + event.values[0] + ")\n" 
				+ "Pitch:  " + pitch + " (" + event.values[2] + ")\n"
				+ "Roll:  " + roll + " (" + event.values[1] + ")\n");
		*/
		
		if(System.currentTimeMillis() - lastOrientationUpdate > ORIENTATION_MAXIMUM_INTERVAL_MS ||
				(System.currentTimeMillis() - lastOrientationUpdate > ORIENTATION_MINIMUM_INTERVAL_MS
				&& (Math.abs(azimuth - lastAzimuth) > ORIENTATION_DELTA_THRESHOLD
				|| Math.abs(pitch - lastPitch) > ORIENTATION_DELTA_THRESHOLD
				|| Math.abs(roll - lastRoll) > ORIENTATION_DELTA_THRESHOLD))) {
			
			// Send the new orientation to the server
			if(tcp != null) {
				ClientCommand cmd = new ClientCommand(ClientCommand.GAMEPLAY_COMMAND);
				cmd.setOrientation(azimuth, pitch, roll);
				tcp.sendClientCommand(cmd);
				lastAzimuth = azimuth;
				lastRoll = roll;
				lastPitch = pitch;
				lastOrientationUpdate = System.currentTimeMillis();
			}
		}
	}
	
	/**
	 * Clamps an input value between a provided minimum and maximum, and returns
	 * the value
	 * @param input	The input value
	 * @param min	The minimum output value
	 * @param max	The maximum output value
	 * @return	The input value, clamped to the provided minimum and maximum
	 */
	private float clamp(float value, float min, float max) {
		if(value > max) return max;
		if(value < min) return min;
		return value;
	}
	
	/**
	 * Call this function to change the view into the OpenGL view.
	 */
	private void goToOpenGLView()
	{
		if (inOpenGL) return;
		else inOpenGL = true;
		
		// Remove the title bar from the window.
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Make the windows into full screen mode.
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create a OpenGL view.
		GLSurfaceView view = new GLSurfaceView(this);

		// Creating and attaching the renderer.
		renderer = new OpenGLRenderer();
		view.setRenderer(renderer);
		setContentView(view);

		// Create and init the media streamer.
		// mClient = new MediaClient(this);
		// mClient.launchMediaStream(33331);

		// Create a new plane.
		plane = new SimplePlane(GameModel.DEFAULT_ARENA_SIZE, GameModel.DEFAULT_ARENA_SIZE);
		
		plane.rx = -45;
		
		plane.loadBitmap(BitmapFactory.decodeResource(getResources(),
				R.drawable.logo));

		// Set the default drawing location (initially center of the plane).
		currentDrawCoords = new Point3D(GameModel.DEFAULT_ARENA_SIZE / 2,
				GameModel.DEFAULT_ARENA_SIZE / 2, 0.0f);

		renderer.addMesh(plane);
	}
	
	/**
	 * Call this method to change the view into the lobby/settings view.
	 */
	private void goToLobbyView()
	{
		if (!inOpenGL) return;
		else inOpenGL = false;
	}
	
	
	
	/* ********************************************** */
	/* Methods called when in the OpenGL perspective. */
	/* ********************************************** */
	
	//////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Sets the plane to texturize the incoming video image.
	 * 
	 * @param image
	 */
	public synchronized void setVideoImage(Bitmap image) {
		plane.loadBitmap(image);
	}

	/**
	 * Renders the given 2D game entity with a supplied height as a 3D
	 * rectangle.
	 * 
	 * @param entity
	 *            The game entity to render.
	 * @param height
	 *            The height of the entity in 3D.
	 */
	public void drawGameEntity(GameEntity entity, float height) {
		Vector vertices[] = entity.getVertices();

		float x1, x2, y1, y2; // The min and max values for x and y coordinates.
		x1 = x2 = y1 = y2 = 0; // Initially 0.

		// Get the endpoints of the obstacle.
		for (int i = 0; i < vertices.length; i++) {
			// Initially set the min and max as the first vertex found.
			if (i == 0) {
				x1 = x2 = vertices[i].getX();
				y1 = y2 = vertices[i].getY();
			}
			// Continue cycling through until the min and max values are
			// found.
			else {
				x1 = Math.min(x1, vertices[i].getX());
				x2 = Math.max(x2, vertices[i].getX());
				y1 = Math.min(y1, vertices[i].getY());
				y2 = Math.max(y2, vertices[i].getY());
			}
		}

		// Now get the size of the entity and construct it.
		float length = Math.abs(x2 - x1);
		float width = Math.abs(y2 - y1);

		// Log.e("RoboWarsOpenGL", "Got obstacle parameters: l:" + length +
		// ", w:" + width + ", h:" + height);

		Cube cube = new Cube(length, width, height);

		currentDrawCoords.setX(entity.getPosture().getX());
		currentDrawCoords.setY(entity.getPosture().getY());
		currentDrawCoords.setZ(height / 2);

		cube.x = currentDrawCoords.getX();
		cube.y = currentDrawCoords.getY();
		cube.z = currentDrawCoords.getZ();

		//cube.setColor(100f, 150f, 200f, 0f);
		// Log.e("RoboWarsOpenGL", "Successfully created the obstacle.");

		// Render the entity.
		renderer.addMesh(cube);

		// Log.e("RoboWarsOpenGL", "Obstacle rendered.");
	}

	/**
	 * Renders a game entity onto the plane as a series of cubes. The height of
	 * the entity will be half of the smallest dimension (length or width) for
	 * now.
	 * 
	 * @param entity
	 *            The entity we're drawing.
	 */
	public void drawGameEntityInParts(GameEntity entity) {
		Vector vertices[] = entity.getVertices();

		float x1, x2, y1, y2; // The min and max values for x and y coordinates.
		x1 = x2 = y1 = y2 = 0; // Initially 0.

		// Get the endpoints of the obstacle.
		for (int i = 0; i < vertices.length; i++) {
			// Initially set the min and max as the first vertex found.
			if (i == 0) {
				x1 = x2 = vertices[i].getX();
				y1 = y2 = vertices[i].getY();
			}
			// Continue cycling through until the min and max values are
			// found.
			else {
				x1 = Math.min(x1, vertices[i].getX());
				x2 = Math.max(x2, vertices[i].getX());
				y1 = Math.min(y1, vertices[i].getY());
				y2 = Math.max(y2, vertices[i].getY());
			}
		}

		// Now we get the size of cubes to draw, which is either the length or
		// the width found previously, depending on which one is smaller.
		float length = Math.abs(x2 - x1);
		float width = Math.abs(y2 - y1);

		float cubeSize = Math.min(length, width);

		// Now find out how many cubes to draw...
		int numberOfCubes = (int) (Math.max(length, width) / cubeSize);

		// Now render the cubes on to the plane.
		for (int i = 0; i < numberOfCubes; i++) {
			// Make a new cube.
			Cube cube = new Cube(cubeSize);
			cube.setColor(100, 200, 150, 0);

			// since we're drawing from the center, the bottom half needs to be
			// placed above the plane.
			currentDrawCoords.setZ(cubeSize / 2);

			if (length > width) // Draw from left to right
			{
				// The cube is located at the minimum, plus half the length of a
				// cube (drawn from center), plus we need to offset by the
				// number of cubes already drawn.
				currentDrawCoords.setX(x1 + (cubeSize / 2) + (i * cubeSize));
				currentDrawCoords.setY((y1 + y2) / 2);
			}

			else if (width > length) // Draw from bottom to top
			{
				// The cube is located at the minimum, plus half the width of a
				// cube (drawn from center), plus we need to offset by the
				// number of cubes already drawn.
				currentDrawCoords.setY(y1 + (cubeSize / 2) + (i * cubeSize));
				currentDrawCoords.setX((x1 + x2) / 2);
			}

			else // The cube has a square base.
			{
				// Create one big cube and render it instead of a bunch of small
				// cubes. The height (z) is half of the length.
				cube = new Cube(length, width, length / 2);
				cube.setColor(100, 200, 150, 0);
				currentDrawCoords.setX((x1 + x2) / 2); // Center of length
				currentDrawCoords.setY((y1 + y2) / 2); // Center of width

				// This one may be tricky to imagine; the z coordinate
				// is 1/2 of the height of the cube (because we're drawing
				// from center, and we don't want half of the cube below the
				// plane). The height of the cube is already 1/2 of the length,
				// so we actually need 1/4 of the length to properly render
				// right on top of the plane.
				currentDrawCoords.setZ((x2 - x1) / 4);
			}
			// Now we have our location, so place the cube there.
			cube.x = currentDrawCoords.getX();
			cube.y = currentDrawCoords.getY();
			cube.z = currentDrawCoords.getZ();

			// Now render it.
			renderer.addMesh(cube);
		}
	}
}
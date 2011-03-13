package com.RoboWars;

import java.util.Observable;
import java.util.Observer;

import robowars.server.controller.ClientCommand;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
	
	/* Views invoked by the application. */
	private TextView chat, users;
	private EditText entry, server, port, user;
	private Button	 launch;
	
	/* Check if we're ready/spectator. */
	private boolean spectator, ready;
	
	/** The time at which the reading of the orientation sensor was last updated */
	private long lastOrientationUpdate;
	
	private LobbyModel lobbyModel;		// Model for the lobby.
	private GameModel gameModel;		// Model for the game.
	private TcpClient tcp;				// TCP controller.
	
	private String userlist;		// Users currently in the lobby.
	
	private SensorManager mSensorManager;	// Manages the accelerometer and other sensors.
    
    TextView mTextViewAcc;	// Text view for accelerometer.
    TextView mTextViewMag;	// Text view for magnetic field.
    TextView mTextViewOri;	// Text view for orientation.
	
	private static final int MAX_LINES = 12;	// Max lines to show in the chat lobby.

	/** MediaPlayer used to display streaming video */
	private MediaClient mediaClient;
	
	/** The last values for each orientation vector that were sent to the server */
	private double lastAzimuth, lastPitch, lastRoll;
	
    /**
     * Creates a tab view.
     */
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
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
    	lobbyModel.addObserver(this);
    	lobbyModel.setVersion(getString(R.string.version));
    	
    	/* Reference to the application's widgets. */
    	chat 			= (TextView) findViewById(R.id.chat);
    	users			= (TextView) findViewById(R.id.users);
    	entry 			= (EditText) findViewById(R.id.entry);
    	server 			= (EditText) findViewById(R.id.server);
    	port 			= (EditText) findViewById(R.id.port);
    	user			= (EditText) findViewById(R.id.username);
    	launch			= (Button)	 findViewById(R.id.launchButton);
        
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
    	mediaClient = new MediaClient(mediaView, mediaStatus);
    	
    	lastAzimuth = 0;
    	lastPitch = 0;
    	lastRoll = 0;
    	
    	/* Initially blank user list. */
    	userlist = "";
    	
    	/* Initially not ready and not spectator. */
    	ready = spectator = false;
    	launch.setEnabled(false);
    }
    
    /**
     * Print a message in the chat lobby.
     * @param msg
     */
    public void printMessage(final String msg)
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
			}
    		break;
	        	
	        default:
	        	printMessage("Unknown button pressed.");
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
		// Check for new chat message.
		if (lobbyModel.newChatMessage())
		{
			printMessage(lobbyModel.getChatBuffer());
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
		mTextViewOri.setText("Orientation:\n" 
				+ "Azimuth:  " + azimuth + " (" + event.values[0] + ")\n" 
				+ "Pitch:  " + pitch + " (" + event.values[2] + ")\n"
				+ "Roll:  " + roll + " (" + event.values[1] + ")\n");
		
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
}
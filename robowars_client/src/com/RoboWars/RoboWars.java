package com.RoboWars;

import java.util.Observable;
import java.util.Observer;

import robowars.server.controller.ClientCommand;
import android.app.Activity;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class RoboWars extends Activity implements SensorListener, Observer
{	
	/** The interval at which orientation updates should be pushed to the server */
	public static final long ORIENTATION_INTERVAL_MS = 300;
	
	/* Views invoked by the application. */
	private TextView chat, users;
	private EditText entry, server, port, user;
	
	/** The time at which the reading of the orientation sensor was last updated */
	private long lastOrientationUpdate;
	
	private LobbyModel model;		// General application model.
	private TcpClient tcp;			// TCP controller.
	
	private String userlist;		// Users currently in the lobby.
	
	private SensorManager mSensorManager;	// Manages the accelerometer and other sensors.
    
    TextView mTextViewAcc;	// Text view for accelerometer.
    TextView mTextViewMag;	// Text view for magnetic field.
    TextView mTextViewOri;	// Text view for orientation.
	
	private static final int MAX_LINES = 12;	// Max lines to show in the chat lobby.

	/** MediaPlayer used to display streaming video */
	private MediaClient mediaClient;
	
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
    	model = new LobbyModel();
    	model.addObserver(this);
    	model.setVersion(getString(R.string.version));
    	
    	/* Reference to the application's widgets. */
    	chat 			= (TextView) findViewById(R.id.chat);
    	users			= (TextView) findViewById(R.id.users);
    	entry 			= (EditText) findViewById(R.id.entry);
    	server 			= (EditText) findViewById(R.id.server);
    	port 			= (EditText) findViewById(R.id.port);
    	user			= (EditText) findViewById(R.id.username);
    	
    	/* The x,y,z coordinates of the orientation of the phone. */
    	mTextViewAcc = (TextView) findViewById(R.id.textAcc);
        mTextViewMag = (TextView) findViewById(R.id.textMag);
        mTextViewOri = (TextView) findViewById(R.id.textOri);
        
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
    	EditText mediaAddress = (EditText) findViewById(R.id.mediaAddress);
    	EditText mediaPort = (EditText) findViewById(R.id.mediaPort);
    	ImageStreamView mediaView = (ImageStreamView) findViewById(R.id.mediaSurface);
    	TextView mediaStatus = (TextView) findViewById(R.id.mediaStatus);
    	mediaClient = new MediaClient(mediaView, mediaAddress, mediaPort, mediaStatus);
    	
    	/* Initially blank user list. */
    	userlist = "";
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
	        	model.setMyUser(new User(username));
	        	tcp = new TcpClient(model);
	        	tcp.connect(address, portNumber);
	        	
	        	break;
	        
	        /* Send button. */
    		case (R.id.send):
    			
    			// Get the text and set the entry field blank.
    			String message = entry.getText().toString();
        		entry.setText("");
        		
        		// Try to generate a client command, send the string as a
        		// chat message if command generation fails
        		ClientCommand cmd = ClientCommand.parse(message);
        		if(cmd == null) {
        			cmd = new ClientCommand(ClientCommand.CHAT_MESSAGE);
        			cmd.setStringData(message);
        		}
        		tcp.sendClientCommand(cmd);
        		
        		break;
        	
        	/* Start Video button */
    		case(R.id.streamMedia):
    			Log.i("RoboWars", "Stream Video Button Pressed");
    			mediaClient.launchStream();
    			break;
    		
    		/* Stop Video button */
    		case(R.id.stopMedia):
    			Log.i("RoboWars", "Stop Video Button Pressed");
    			mediaClient.terminateStream();
    			break;
	        	
	        default:
	        	printMessage("Unknown button pressed.");
    	}
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, SensorManager.SENSOR_ORIENTATION);
				
	}

	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(this);
		super.onStop();
	}

	public void onAccuracyChanged(int sensor, int accuracy) { }

	public void onSensorChanged(int sensor, float[] values) {
		// Scale all values to 1 to -1 range
		values[0] = (values[0] - 180) / 180; // Azimuth sensor standard range 0 to 360
		values[1] = (values[1] / 180);	// Pitch sensor standard range -180 to 180
		values[2] = (values[2] / 90);	// Roll sensor standard range -90 to 90
		
		if(System.currentTimeMillis() - lastOrientationUpdate > ORIENTATION_INTERVAL_MS) {
			switch(sensor) {
			case SensorManager.SENSOR_ORIENTATION:
				// Set the text display on client side
				mTextViewOri.setText("Orientation: " 
						+ values[0] + ", " 
						+ values[1] + ", "
						+ values[2]);
				
				// Send the new orientation to the server
				if(tcp != null) {
					ClientCommand cmd = new ClientCommand(ClientCommand.GAMEPLAY_COMMAND);
					cmd.setOrientation(values[0], values[1], values[2]);
					tcp.sendClientCommand(cmd);
				}
				break;
			default:
				break;
			}
			
			lastOrientationUpdate = System.currentTimeMillis();
		}
	}

	/**
	 * Updates based on changes to the model.
	 */
	public void update(Observable observable, Object data)
	{
		// Check for new chat message.
		if (model.newChatMessage())
		{
			printMessage(model.getChatBuffer());
		}
		
		// Check for changes in the user list.
		if (model.usersUpdated())
		{
			userlist = model.getUsers();
			updateUsers();
		}
	}
}
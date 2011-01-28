package com.RoboWars;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class RoboWars extends Activity implements SensorListener, Observer
{	
	/** 
	 * The address of the RTSP stream to use for video streaming (ideally, this
	 * should be sent by the server upon connection.
	 */
	public static final String DEFAULT_RTSP_STREAM_ADDRESS = "rtsp://192.168.1.104:5544/Test";
	
	/** The interval at which orientation updates should be pushed to the server */
	public static final long ORIENTATION_INTERVAL_MS = 300;
	
	/* Views invoked by the application. */
	private TextView chat, users, videoStatus;
	private EditText entry, server, port, user, rtspAddress;
	
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
	private MediaPlayer mp;
	
	/** Flag to determine if the SurfaceHolder for the MediaPlayer is currently initialized */
	private boolean mediaSurfaceCreated;
	
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
    	rtspAddress		= (EditText) findViewById(R.id.rtspAddress);
    	
    	/* The x,y,z coordinates of the orientation of the phone. */
    	mTextViewAcc = (TextView) findViewById(R.id.textAcc);
        mTextViewMag = (TextView) findViewById(R.id.textMag);
        mTextViewOri = (TextView) findViewById(R.id.textOri);
        videoStatus = (TextView) findViewById(R.id.videoStatus);
        
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
    	
    	/* Setup media surface for use with MediaPlayer */
    	rtspAddress.setText(DEFAULT_RTSP_STREAM_ADDRESS);
    	mediaSurfaceCreated = false;
    	SurfaceView mediaView = (SurfaceView)findViewById(R.id.mediaSurface);
        SurfaceHolder holder = mediaView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				Log.i("RoboWars", "Got surfaceDestroyed callback");
				mediaSurfaceCreated = false;
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				Log.i("RoboWars", "Got surfaceCreated callback");
		        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		        mediaSurfaceCreated = true;
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {}
		});
    	
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
        		
        		// Send the message over TCP to the server.
        		tcp.sendMessage(message);
        		
        		break;
        	
        	/* Start Video button */
    		case(R.id.streamMedia):
    			Log.i("RoboWars", "Stream Video Button Pressed");
    			launchMediaPlayer(((SurfaceView)findViewById(R.id.mediaSurface)).getHolder());
    			break;
    		
    		/* Stop Video button */
    		case(R.id.stopMedia):
    			Log.i("RoboWars", "Stop Video Button Pressed");
    			destroyMediaPlayer("Video Status: Disabled");
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
		
		if(System.currentTimeMillis() - lastOrientationUpdate > ORIENTATION_INTERVAL_MS) {
			switch(sensor) {
			case SensorManager.SENSOR_ORIENTATION:
				mTextViewOri.setText("Orientation: " 
						+ values[0] + ", " 
						+ values[1] + ", "
						+ values[2]);
				
				if(tcp != null) {
					tcp.sendMessage("c:<" 
							+ values[0] + "," 
							+ values[1] + ","
							+ values[2] + ">");
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
	
	/**
	 * Starts the MediaPlayer used for displaying streaming video, given that the
	 * SurfaceHolder has already been initialized. The MediaPlayer will attempt
	 * to stream from the RTSP stream address provided in the rtspAddress text field.
	 * @param holder	The SurfaceHolder to display the video in (must already
	 * 					be fully initialized)
	 */
	private void launchMediaPlayer(SurfaceHolder holder) {
		// Do nothing if MediaPlayer already exists or surface holder
		// has not been initialized
		if(mp != null || !mediaSurfaceCreated) return;
		
		// Generate new MediaPlayer, and load RTSP address from the provided
		// text field
		Log.i("RoboWars", "Generating Media Player");
		mp = new MediaPlayer();
		mp.setDisplay(holder);
		mp.setScreenOnWhilePlaying(true);
		try {
			mp.setDataSource(rtspAddress.getText().toString());
		} catch (Exception e) {
			return;
		}
		
		// Begin buffering of video stream
		Log.i("RoboWars", "Beginning MediaPlayer preparation.");
		videoStatus.setText("Video Status: Buffering (0%)");
		mp.prepareAsync();
    	
		// Listener to initiate video playback when buffering is complete
    	mp.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer arg0) {
				Log.i("RoboWars", "Preparation Complete, starting play");
				videoStatus.setText("Video Status: Enabled");
				arg0.start();
				Log.i("RoboWars", "MediaPlayer activated.");
			}
    	});
    	
    	// Listener to update status text with current buffering percentage
    	mp.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				Log.i("RoboWars", "MediaPlayer reports percent buffered: " + percent);
				videoStatus.setText("Video Status: Buffering (" + percent + "%)");
			}
    	});
    	
    	// Listener to release MediaPlayer resources if RTSP address cannot be
    	// accessed (or other error occurs)
    	mp.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e("RoboWars", "ERROR: " + what + " / " + extra);
				destroyMediaPlayer("Video Status: Could not connect to RTSP server at: " +
						rtspAddress.getText().toString());
				return false;
			}
    	});
	}
	
	/**
	 * Stops the current MediaPlayer (if one exists) and releases its resources.
	 * @param statusMessage	The message that should be displayed in the video
	 * 						status text field.
	 */
	private void destroyMediaPlayer(String statusMessage) {
		if(mp != null) {
			mp.stop();
			mp.release();
			mp = null;
			Log.i("RoboWars", "Destroyed existing MediaPlayer");
			videoStatus.setText(statusMessage);
			return;
		}
		Log.i("RoboWars", "No existing MediaPlayer to destroy");
	}
}
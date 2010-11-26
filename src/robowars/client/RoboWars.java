package robowars.client;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class RoboWars extends Activity implements Observer
{
	/* Views invoked by the application. */
	TextView chat, users;
	EditText entry, server, port, user;
	ScrollView chatScrollView, userScrollView;
	
	LobbyModel model;		// General application model.
	TcpClient tcp;			// TCP controller.
	
	String userlist;		// Users currently in the lobby.
	
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

    	/* Add the tabs to the tabHost. */
    	tabHost.addTab(spec1);
    	tabHost.addTab(spec2);
    	tabHost.addTab(spec3);
    	
    	/* Instantiate the model and add the view as an observer. */
    	model = new LobbyModel();
    	model.addObserver(this);
    	
    	/* Reference to the application's widgets. */
    	chat 			= (TextView) findViewById(R.id.chat);
    	users			= (TextView) findViewById(R.id.users);
    	entry 			= (EditText) findViewById(R.id.entry);
    	server 			= (EditText) findViewById(R.id.server);
    	port 			= (EditText) findViewById(R.id.port);
    	user			= (EditText) findViewById(R.id.username);
    	chatScrollView 	= (ScrollView) findViewById(R.id.chatScrollView);
    	
    	/* Allow scrolling of the chat and user list. */
    	chat.setMovementMethod(new ScrollingMovementMethod());
    	users.setMovementMethod(new ScrollingMovementMethod());
    	
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
    			chatScrollView.scrollTo(0, chat.getHeight());
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
	        	
	        default:
	        	printMessage("Unknown button pressed.");
    	}
    }
    
    public void goForward(View view)
    {
    	tcp.sendMessage("c:w");
    }
    
    public void goBackward(View view)
    {
    	tcp.sendMessage("c:s");
    }
    
    public void goLeft(View view)
    {
    	tcp.sendMessage("c:a");
    }
    
    public void goRight(View view)
    {
    	tcp.sendMessage("c:d");
    }
    
    public void stop(View view)
    {
    	tcp.sendMessage("c:");
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
package robowars.client;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class RoboWars extends Activity implements Observer
{
	TextView chat;
	EditText entry, server, port, user, pass;
	
	TcpClient tcp;
	
    /**
     * Create a tab view.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
    	tabHost.setup();

    	TabSpec spec1=tabHost.newTabSpec("Main");
    	spec1.setIndicator("Main");
    	spec1.setContent(R.id.tab1);
    	
    	TabSpec spec2=tabHost.newTabSpec("Lobby");
    	spec2.setIndicator("Lobby");
    	spec2.setContent(R.id.tab2);

    	TabSpec spec3=tabHost.newTabSpec("Config");
    	spec3.setIndicator("Config");
    	spec3.setContent(R.id.tab3);

    	tabHost.addTab(spec1);
    	tabHost.addTab(spec2);
    	tabHost.addTab(spec3);
    	
    	chat 	= (TextView) findViewById(R.id.chat);
    	entry 	= (EditText) findViewById(R.id.entry);
    	server 	= (EditText) findViewById(R.id.server);
    	port 	= (EditText) findViewById(R.id.port);
    	user	= (EditText) findViewById(R.id.username);
    	pass	= (EditText) findViewById(R.id.password);
    }
    
    public void printMessage(final String msg)
    {
    	this.runOnUiThread(new Runnable(){
    		public void run(){
    			chat.append(msg + "\n");
            }
        });
    }
    
    public void sendClicked(View view)
    {
    	String message = entry.getText().toString();
    	tcp.sendMessage("m:" + message);
    }
    
    public void connectClicked(View view)
    {
    	String address 	= server.getText().toString();
    	int portNumber	= Integer.parseInt(port.getText().toString());
    	
    	tcp = new TcpClient(this);
    	tcp.connect(address, portNumber);
    }

	/**
	 * Updates based on changes to the model.
	 */
	public void update(Observable observable, Object data) {
		//TODO: Implement
	}
}
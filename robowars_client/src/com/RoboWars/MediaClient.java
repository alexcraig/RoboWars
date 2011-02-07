package com.RoboWars;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Establishes a connection to the media server and streams video frames 
 * to the Android client for the game.
 */
public class MediaClient {
	/** 
	 * The default address and port to use for a streaming media connection
	 */
	public static final String DEFAULT_MEDIA_ADDRESS = "192.168.1.109";
	public static final int DEFAULT_MEDIA_PORT = 33331;
	
	/** Fields that the media server IP address and port should be read from */
	private EditText addressField;
	private EditText portField;
	
	/** The text display that status indications should be written to */
	private TextView statusView;
	
	/** ImageStreamView that images read by the MediaClient should be rendered to */
	private ImageStreamView mediaView;
	
	/** Socket used to connect to the server and read images */
	private Socket mediaSocket;
	
	/** The thread which decodes images from the socket */
	private DecoderThread decodeThread;
	
	/**
	 * Generates a new MediaClient and sets the input fields to their default values.
	 * @param mediaView	The view that images read from the network should be rendered to
	 * @param addressField	The field that the server IP should be read from
	 * @param portField		The field that the server port should be read from
	 * @param statusView	The text display for status indiciation to be written to
	 */
	public MediaClient(ImageStreamView mediaView, EditText addressField, 
			EditText portField, TextView statusView) {
		this.mediaView = mediaView;
		this.addressField = addressField;
		this.portField = portField;
		this.statusView = statusView;
		mediaSocket = null;
		
		addressField.setText(DEFAULT_MEDIA_ADDRESS);
		portField.setText(Integer.toString(DEFAULT_MEDIA_PORT));
		statusView.setText("Streaming player initialized.");
	}
	
	/**
	 * Opens a connection to the server specified in the address and port
	 * input fields, and launches a stream decoding thread. Does nothing
	 * if a connection is already active.
	 */
	public void launchStream() {
		if(mediaSocket != null) {
			// Video stream is already active, do nothing
			Log.i("RoboWars", "Attempted to launch active stream, ignoring.");
			return;
		}
		
		String ipAddress = addressField.getText().toString();
		int port;
		try {
			port = Integer.parseInt(portField.getText().toString());
		} catch (NumberFormatException e) {
			statusView.setText("Valid port could not be parsed from text: " 
					+ portField.getText().toString());
			return;
		}
		
		try {
			mediaSocket = new Socket(ipAddress, port);
			statusView.setText("Connection successfully opened to " + ipAddress
					+ ":" + port);
		} catch (UnknownHostException e) {
			statusView.setText("Unknown host: " + ipAddress
					+ ":" + port);
			mediaSocket = null;
			return;
		} catch (IOException e) {
			statusView.setText("IO Exception opening socket: " + ipAddress
					+ ":" + port);
			mediaSocket = null;
			return;
		}

		decodeThread = new DecoderThread(mediaView, mediaSocket);
		new Thread(decodeThread).start();
	}
	
	/**
	 * Terminates an existing decoding thread, and closes any active connection
	 * to the media server. Does nothing if no decoding thread is currently active.
	 */
	public void terminateStream() {
		if(decodeThread == null) {
			// If no decodeThread is initialized, then the stream is not activated
			// Do nothing
			Log.i("RoboWars", "Attempted to terminate dead stream, ignoring.");
			return;
		}
		
		statusView.setText("Connection terminated.");
		decodeThread.terminate();
		mediaSocket = null;
		
	}
	
	/**
	 * A thread implementation which decodes images in standard formats (gif and png
	 * definitely supported, jpg should be according to Android docs but fails in testing) 
	 * and passes them to an ImageStreamView to be rendered.
	 */
	private class DecoderThread implements Runnable {
		/** The view to render images to */
		private ImageStreamView mediaView;
		
		/** The socket to read images from and write acknowledgements to */
		private Socket mediaSocket;
		
		/** Input and output streams for the socket */
		private FlushedInputStream socketIn;
		private PrintWriter socketOut;
		
		/** Flag to signal that thread should terminate */
		private boolean terminateFlag;
		
		/**
		 * Generates a new DecoderThread
		 * @param mediaView	The view to pass images to
		 * @param mediaSocket	The socket to read images from (must already be
		 * 						successfully connected)
		 */
		public DecoderThread(ImageStreamView mediaView, Socket mediaSocket) {
			this.mediaView = mediaView;
			this.mediaSocket = mediaSocket;
			socketIn = null;
			socketOut = null;
			terminateFlag = false;
		}
		
		/**
		 * Sets the terminate flag of the decoder thread. Any current decoding operation
		 * will complete, after which a QUIT message will be written to the socket.
		 * The socket will be closed, and a null image will be passed to the
		 * streaming view.
		 */
		public void terminate() {
			terminateFlag = true;
		}
		
		@Override
		/**
		 * Continually read images from the socket until the termination
		 * flag is set. Writes an ACK to the socket after each read, and
		 * writes a QUIT message before terminating.
		 */
		public void run() {
			try {
				socketOut = new PrintWriter(mediaSocket.getOutputStream());
				socketIn = new FlushedInputStream(mediaSocket.getInputStream());
			} catch (IOException e) {
				try {
					mediaSocket.close();
				} catch (IOException err) {
					Log.i("RoboWars", "Error closing media socket.");
				}
				
				terminateStream();
				return;
			}
	        
			Bitmap image = null;
			while(!terminateFlag) {
				image = BitmapFactory.decodeStream(socketIn);
				if(image == null) {
					// Happens when only partial data is available, ignore for now
				} else {
					mediaView.setImage(image);
					socketOut.println("ACK");
					socketOut.flush();
				}
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			mediaView.setImage(null);
			try {
				if(socketOut != null) {
					socketOut.println("QUIT");
					socketOut.flush();
					socketOut.close();
					socketIn.close();
				}
					
				mediaSocket.close();
				mediaSocket = null;
			} catch (IOException e) {
				statusView.setText("Error closing media socket.");
			}
		}
	}

	/**
	 * InputStream which only implements the skip method. This provides a workaround
	 * for a bug in Android which prevents the BitmapFactory static methods from
	 * reading images from a network socket.
	 */
	private class FlushedInputStream extends FilterInputStream {
	    public FlushedInputStream(InputStream inputStream) {
	        super(inputStream);
	    }
	
	    @Override
	    public long skip(long n) throws IOException {
	        long totalBytesSkipped = 0L;
	        while (totalBytesSkipped < n) {
	            long bytesSkipped = in.skip(n - totalBytesSkipped);
	            if (bytesSkipped == 0L) {
	                  int readByte = read();
	                  if (readByte < 0) {
	                      break;  // we reached EOF
	                  } else {
	                      bytesSkipped = 1; // we read one byte
	                  }
	           }
	            totalBytesSkipped += bytesSkipped;
	        }
	        return totalBytesSkipped;
	    }
	}
}

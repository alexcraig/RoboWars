package com.RoboWars;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.TextView;

/**
 * Establishes a connection to the media server and streams video frames 
 * to the Android client for the game.
 */
public class MediaClient implements Runnable {
	/** Size of the buffer for incoming media packets (in bytes) */
	public static int INC_BUFFER_SIZE = 16384;
	
	/** 
	 * The default address and port to use for a streaming media connection
	 */
	public static final String DEFAULT_MEDIA_ADDRESS = "239.0.0.123";
	public static final int DEFAULT_MEDIA_PORT = 33331;
	
	/** The text display that status indications should be written to */
	private TextView statusView;
	
	/** ImageStreamView that images read by the MediaClient should be rendered to */
	private ImageStreamView mediaView;
	
	/** Socket used to connect to the server and read images */
	private DatagramSocket mediaSocket;
	
	/**
	 * Generates a new MediaClient and sets the input fields to their default values.
	 * @param mediaView	The view that images read from the network should be rendered to
	 * @param addressField	The field that the server IP should be read from
	 * @param portField		The field that the server port should be read from
	 * @param statusView	The text display for status indiciation to be written to
	 */
	public MediaClient(ImageStreamView mediaView, 
			TextView statusView) {
		this.mediaView = mediaView;
		this.statusView = statusView;
		mediaSocket = null;
		statusView.setText("Streaming player initialized.");
	}
	
	/**
	 * Opens a socket listening on the specified port, and launches a thread to
	 * decode incoming packets.
	 * 
	 * TODO: Calling this function more than once on the same mediaClient is likely
	 * to crash the app
	 * 
	 * @param port	The port to listen for packets on.
	 */
	public void launchMediaStream(int port) {
		if(mediaSocket != null) {
			mediaSocket.close();
			mediaSocket = null;
		}
		
		try {
			mediaSocket = new DatagramSocket(port);
		} catch (IOException e) {
			Log.e("RoboWars", "Could not bind media socket, video stream will not be supported.");
			statusView.setText("Could not bind media socket, video stream will not be supported.");
			mediaSocket = null;
		}
		statusView.setText("Streaming player awaiting packets on port: " + port + "");
		new Thread(this).start();
	}
	
	@Override
	/**
	 * Continually read images from the socket.
	 */
	public void run() {
		Bitmap image = null;
		byte[] incBuffer = new byte[INC_BUFFER_SIZE];
		
		while(true) {
			DatagramPacket recvPacket = new DatagramPacket(incBuffer, incBuffer.length);
			
			try {
				Log.i("RoboWars", "Reading media packet.");
				mediaSocket.receive(recvPacket);
			} catch (IOException e) {
				Log.e("RoboWars", "Error reading incoming media packet.");
				// TODO: Try to correct somehow?
				continue;
			}
			Log.i("RoboWars", "Read media packet.");
			
			recvPacket.getData();
			
			long preDecode = System.currentTimeMillis();
			image = BitmapFactory.decodeByteArray(recvPacket.getData(), 0, recvPacket.getLength());
			
			if(image == null) {
				// Happens when only partial data is available, ignore for now
			} else {
				Log.i("RoboWars", "Decoding took: " + (System.currentTimeMillis() - preDecode)
						+ " ms.");
				mediaView.setImage(image);
			}
		}
	}
}

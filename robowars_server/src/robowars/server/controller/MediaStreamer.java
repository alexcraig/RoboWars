package robowars.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.media.cdp.GlobalCaptureDevicePlugger;

import org.apache.log4j.Logger;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.Image;
import com.lti.civil.VideoFormat;
import com.lti.civil.awt.AWTImageConverter;
import com.lti.civil.impl.jni.NativeCaptureSystemFactory;

/**
 * Handles the streaming of live video and transmission of other camera
 * status messages to connected users. This class also stores a list of
 * all available cameras, and manages the selection of a currently active
 * camera.
 */
public class MediaStreamer implements Runnable, CaptureObserver {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(MediaStreamer.class);
	
	public static long IMAGE_WRITE_INTERVAL = 200;
	public static int VIDEO_HEIGHT = 240;
	public static int VIDEO_WIDTH = 320;
	
	/** Port to serve images from */
	private int serverPort;
	
	/** Device to capture images from */
	private CaptureDeviceInfo captureDevice;
	
	/** The stream to receive images from */
	private CaptureStream captureStream;
	
	private ArrayList<MediaClientRecord> clients;
	
	/** 
	 * A list of all connected CameraControllers (each controller represents
	 * a separately connected USB webcam).
	 */
	private List<CameraController> cameras;
	
	/**
	 * The camera currently selected to stream video from.
	 */
	private CameraController selectedCamera;
	
	/** 
	 * Stream which is currently being captured. This should always be null
	 * when no active capture is in progress. 
	 */
	private CaptureStream currentStream;

	/**
	 * Generates a new MediaStreamer
	 * @arg port	The port at which to accept incoming media connections
	 */
	public MediaStreamer(int port) 
	{
		RegistryDefaults.registerAll(RegistryDefaults.FMJ);
		serverPort = port;
		captureDevice = null;
		cameras = new ArrayList<CameraController>();
		clients = new ArrayList<MediaClientRecord>();
		currentStream = null;
		GlobalCaptureDevicePlugger.addCaptureDevices();
	}
	
	/**
	 * @return	The port the media streamer is serving frames on
	 */
	public int getPort() {
		return serverPort;
	}
	
	/**
	 * Opens a listening socket, and creates a new MediaClientRecord whenever
	 * a new connection is received.
	 */
	public void waitForConnections() {
		try {
			// Establish the listen socket.
			ServerSocket listenSocket = new ServerSocket(serverPort);
			
			log.info("MediaServer initialized and waiting on port: " + serverPort);
			
			// Process HTTP service requests in an infinite loop.
			while (true) {
			    // Listen for a TCP connection request.
				Socket clientSocket = listenSocket.accept();
				log.info("Got new connection from: " + clientSocket.getInetAddress().getCanonicalHostName());
		
			    // Start a new thread to handle the new client.
				MediaClientRecord newClient = new MediaClientRecord(clientSocket);
				new Thread(newClient).start();
			    synchronized(clients) {
			    	clients.add(newClient);
			    }
			}
		} catch (IOException e) {
			log.error("Socket error in media listen thread, terminating thread.");
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Removes a client from the list of clients to serve video frames to
	 * @param client	The client to stop serving media to
	 */
	public void removeClient(MediaClientRecord client) {
		synchronized(clients) {
			clients.remove(this);
		}
	}
	
	@Override
	/**
	 * Launches the media server
	 */
	public void run() {
		this.waitForConnections();
	}
	
	/**
	 * Re-detects all camera devices connected to the system. Returns without
	 * performing any action if a media stream is currently being served.
	 */
	public synchronized void updateDeviceList() {
		if(currentStream != null) {
			log.error("Attempted to update capture list while serving a stream.");
			return;
		}
		
		// Store the old list of cameras, and generate a new list to store
		// cameras detected during this update
		List<CameraController> oldCameras = cameras;
		cameras = new ArrayList<CameraController>();
		
		// Detect all connected cameras, and generate a new CameraController
		// for those which were not present during the last update of the device
		// list
		NativeCaptureSystemFactory captureFactory = new NativeCaptureSystemFactory();
		try {
			CaptureSystem captureSystem = captureFactory.createCaptureSystem();
			captureSystem.init();
			List<com.lti.civil.CaptureDeviceInfo> devices = captureSystem.getCaptureDeviceInfoList();
			
			for(com.lti.civil.CaptureDeviceInfo device : devices) {
				log.info("LTI-Civil found device: " + device.getDescription() + " - " + device.getDeviceID());
				
				boolean foundExisting = false;
				for(CameraController c : oldCameras) {
					if(c.getDeviceId().equals(device.getDeviceID())) {
						cameras.add(c);
						foundExisting = true;
						break;
					}
				}
				
				if(!foundExisting) {
					CameraController newCam = new CameraController(device);
					cameras.add(newCam);
				}
			}
			
			if(cameras.isEmpty()) {
				selectedCamera = null;
			} else {
				selectedCamera = cameras.get(0); // Note: Defaults to first detected camera
			}

		} catch (CaptureException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return	The currently selected CameraController (or null if no
	 * 			camera has been selected)
	 */
	public CameraController getActiveCamera() {
		return selectedCamera;
	}
	
	/**
	 * Starts streaming of the selected camera if the required resolution
	 * is supported.
	 */
	public synchronized void playStream() {
		if(getActiveCamera() != null) {
			CaptureStream stream = getActiveCamera().getCaptureStream();
			if(stream != null) {
				try {
					
					boolean gotValidFormat = false;
					for(VideoFormat v : stream.enumVideoFormats()) {
						if(v.getWidth() == VIDEO_WIDTH && v.getHeight() == VIDEO_HEIGHT) {
							stream.setVideoFormat(v);
							
							log.info("Selected capture format:\n" + v.getWidth() 
									+ "x" + v.getHeight() + " - " + v.getFPS() 
									+ " fps - " + v.getFormatType() + "(" + VideoFormat.RGB24 
									+ " RGB24, " + VideoFormat.RGB32 + " RGB32)");
							
							gotValidFormat = true;
							break;
						}
					}
					
					
					if(!gotValidFormat) {
						log.error("Selected capture device does not support the required resolution: "
								+ VIDEO_WIDTH + " x " + VIDEO_HEIGHT + " px");
					}
					
					
					stream.setObserver(this);
					stream.start();
					currentStream = stream;
				} catch (CaptureException e) {
					log.error("Attempt to play capture stream failed.");
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Stops the currently active media stream (does nothing if no stream
	 * is active)
	 */
	public synchronized void stopStream() {
		if(currentStream != null) {
			try {
				currentStream.stop();
			} catch (CaptureException e) {
				log.error("Attempt to stop capture stream failed.");
				e.printStackTrace();
			} finally {
				currentStream.setObserver(null);
				try {
					currentStream.dispose();
				} catch (CaptureException e) {
					log.error("Attempt to dispose capture stream failed.");
					e.printStackTrace();
				}
			}
			
		}
	}
	
	/**
	 * Sets the active camera. The specified camera must be either null (video
	 * stream disabled), or a camera controller previously detected by the
	 * MediaStreamer instance.
	 * @param newCam	The camera controller to use as the active camera, or null
	 * 					if no video feed is enabled.
	 */
	public void setActiveCamera(CameraController newCam) {
		if(newCam == null || cameras.contains(newCam)) {
			selectedCamera = newCam;
		}
	}
	
	/**
	 * @return	The list of all CameraControllers detected by the MediaStreamer
	 */
	public List<CameraController> getAvailableCameras() {
		return cameras;
	}
	
	@Override
	/** Logs errors in the capture stream */
	public void onError(CaptureStream stream, CaptureException err) {
		log.error("Error in capture stream:\n" + err.getMessage());
	}

	@Override
	/** Called whenever a new frame is read from the active capture stream */
	public void onNewImage(CaptureStream stream, Image image) {
		synchronized(clients) {
			for(MediaClientRecord client : clients) {
				client.writeImageToCaptureStream(image);
			}
		}
	}
	
	/**
	 * Thread implementation to store information on a connected client, and
	 * continually read incoming ACK messages in response to outgoing
	 * video frames.
	 */
	private class MediaClientRecord implements Runnable {
		/** Socket used to communicate with the client */
		private Socket clientSocket;
		
		/** Output stream to write to the socket */
		private OutputStream socketOut;
		
		/** Input reader to read from the client socket */
		private BufferedReader socketIn;
		
		/** Flag to signal that the socket reading thread should terminate */
		private boolean terminateFlag;
		
		/** Flag used to determine if an ACK has been received for the previous frame */
		private boolean gotAck;
		
		/** Mutex for the acknowledgement flag */
		private Object ackLock;
		
		/**
		 * Generates a new MediaClientRecord
		 * @param clientSocket	The connected socket for the client connection
		 */
		public MediaClientRecord(Socket clientSocket) {
			this.clientSocket = clientSocket;
			terminateFlag = false;
			gotAck = true;
			ackLock = new Object();
			
			try {
				socketOut = clientSocket.getOutputStream();
				socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				log.error("Error getting socket streams");
				return;
			}
		}
		
		/**
		 * Writes the provided image to the client socket, given that an ACK
		 * has been received for the previous image.
		 * @param image	The image to be written to the network.
		 */
		public void writeImageToCaptureStream(Image image) {
			if(socketOut != null) {
				try {
					synchronized(ackLock) {
						if(gotAck) {
							synchronized(socketOut) {
								// Note: Image must be sent as png or gif (Android does not appear to support
								// decoding of jpg?)
								ImageIO.write(AWTImageConverter.toBufferedImage(image), "png", socketOut);
								socketOut.flush();
								log.info("Wrote image to socket.");
							}
							gotAck = false;
						}
					}
				} catch (IOException e) {
					log.error("Error writing JPEG to socket");
					gotAck = false;
					terminateFlag = true;
				}
			}
		}

		@Override
		/**
		 * Continually reads from the client's socket to receive ACK or QUIT messages
		 */
		public void run() {
			String reply = "";
			try {
				while(!terminateFlag) {
					// log.info("Reading data from client: " + clientSocket.getInetAddress().getCanonicalHostName());
					reply = socketIn.readLine();
					if(reply == null) break;
					
					// log.info("Read reply: " + reply);
					if(reply.equalsIgnoreCase("ACK")) {
						synchronized(ackLock) {
							gotAck = true;
							// log.info("Got ACK");
						}
					} else if (reply.equalsIgnoreCase("QUIT")) {
						log.info("Got QUIT message.");
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				log.info("Terminating client.");
				removeClient(this);
				try {
					clientSocket.close();
					socketIn.close();
					socketOut.close();
				} catch (IOException e) {
					log.error("Error closing client socket");
					e.printStackTrace();
				}
			}
		}
	}
}

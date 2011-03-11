package robowars.server.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import robowars.shared.model.User;

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
public class MediaStreamer implements Runnable, ServerLobbyListener, CaptureObserver {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(MediaStreamer.class);
	
	/** 
	 * Toggle this flag to set whether a testing media client should be generated
	 * on initialization.
	 */
	public static boolean ENABLE_TEST_CLIENT = true;
	
	/**
	 * The IP address to use for the test client.
	 */
	public static String TEST_CLIENT_HOSTNAME = "192.168.1.106";
	
	/** The minimum interval (in ms) between frame transmissions to the network */
	public static long IMAGE_WRITE_INTERVAL = 100;
	
	/** The desired video resolution width and height */
	public static int VIDEO_WIDTH = 320;
	public static int VIDEO_HEIGHT = 240;
	
	/** Size of the buffer for reading incoming packets */
	public static int INC_BUFFER_SIZE = 16384;
	
	/** The number of bytes of data that should be sent in each packet */
	public static int PACKET_SIZE = 1450;
	
	/** Socket used to send image frames to the network */
	private DatagramSocket serverSocket;
	
	/** Port to use for transmission of image frames */
	private int mediaPort;
	
	/** The time at which an image was last written to the network stream */
	private long lastImageWrite;
	
	/** A list of all connected clients */
	private ArrayList<User> clients;
	
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
	
	/** The Capture Observer to attach to the next stream that is opened. */
	private CaptureObserver observer;
	
	/** 
	 * The next sequence number to use for image packets (should just alternate between
	 * 1 and 0)
	 */
	private int nextSeqNum;

	/**
	 * Generates a new MediaStreamer
	 * @param port	The port at which to accept incoming media connections
	 */
	public MediaStreamer(int port) 
	{
		mediaPort = port;
		cameras = new ArrayList<CameraController>();
		clients = new ArrayList<User>();
		serverSocket = null;
		currentStream = null;
		observer = this;
		lastImageWrite = System.currentTimeMillis();
		nextSeqNum = 0;
		
		if(ENABLE_TEST_CLIENT) {
			try {
				addUser(new User("MediaTestClient", InetAddress.getByName(TEST_CLIENT_HOSTNAME)));
				log.info("Media Test client established. Serving media stream to: "
						+ InetAddress.getByName(TEST_CLIENT_HOSTNAME).getHostAddress()
						+ ":" + getPort());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return	The port the media streamer is serving frames on
	 */
	public int getPort() {
		return mediaPort;
	}
	
	/**
	 * Opens a listening socket, and creates a new MediaClientRecord whenever
	 * a new connection is received.
	 */
	public void waitForConnections() {
		byte[] incomingBuffer = new byte[INC_BUFFER_SIZE];
		
		try {
			// Establish the datagram socket.
			serverSocket = new DatagramSocket(mediaPort);
			log.info("MediaServer initialized and waiting at port: " + mediaPort);
			
			// Process incoming UDP packets
			// TODO: Figure out what needs to be done here once multiple client
			// support is implemented
			while (true) {}
		} catch (IOException e) {
			log.error("Socket error in media listen thread, terminating thread.");
			e.printStackTrace();
			return;
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
				
				// Add the previously existing camera controller for the detected
				// device, or create a new one of no controller is available
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
							log.info("Selected capture format: " + v.getWidth() 
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
					
					stream.setObserver(observer);
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
	 * Sets the observer that will be used to capture frames from the next
	 * opened stream. Changes will not take place until the next call
	 * to playStream().
	 * 
	 * @param observer	The observer object to use with the next opened capture stream.
	 */
	public synchronized void setObserver(CaptureObserver observer) {
		if(observer == null) {
			this.observer = this;
		} else {
			this.observer = observer;
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
				currentStream = null;
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
	
	/**
	 * @return	True if a capture stream is currently active (i.e. currentStream
	 * 			is not null)
	 */
	public boolean isStreaming() {
		return currentStream != null;
	}
	
	/**
	 * Adds a new user to the list of users that should receive a media stream.
	 * 
	 * @param user	The user to stream media to
	 * @return	True if the user was successfully added, false if not (duplicate
	 * 			record existed)
	 */
	public boolean addUser(User user) {
		synchronized(clients) {
			for(User u : clients) {
				if(u == user) {
					return false;
				}
			}
			clients.add(user);
			log.info("Added user \"" + user.getUsername() + "\" to streaming media clients.");
			return true;
		}
	}
	
	/**
	 * Removes a user from the list of users that should receive a media stream.
	 * 
	 * @param user	The user to stop streaming media to
	 * @return	True if the user was successfully removed, false if not (user
	 * 			could not be found in existing client list)
	 */
	public boolean removeUser(User user) {
		synchronized(clients) {
			log.info("Removing user \"" + user.getUsername() + "\" from streaming media clients.");
			return clients.remove(user);
		}
	}
	
	@Override
	/**
	 * Starts and stops the network video stream when a game is launched
	 * or terminated.
	 */
	public void lobbyGameStateChanged(LobbyGameEvent event) {
		if(event.getEventType() == ServerLobbyEvent.EVENT_GAME_LAUNCH) {
			// Game is launching, stream video to the network
			log.info("Game launched, streaming media to network.");
			stopStream();
			setObserver(null);
			playStream();
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_GAME_OVER) {
			// Game is ending, stop video streaming
			log.info("Game terminated, closing media stream to network.");
			stopStream();
		}
		
	}
	
	@Override
	/**
	 * Adds and removes users from the streaming media client list when a user
	 * joins or leaves the server lobby.
	 */
	public void userStateChanged(LobbyUserEvent event) {
		User user = event.getUser();
		if(event.getEventType() == ServerLobbyEvent.EVENT_PLAYER_JOINED) {
			addUser(user);
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_PLAYER_LEFT) {
			removeUser(user);
		}
	}

	@Override
	public void robotStateChanged(LobbyRobotEvent event) {}

	@Override
	public void lobbyChatMessage(LobbyChatEvent event) {}
	
	@Override
	/** Logs errors in the capture stream */
	public void onError(CaptureStream stream, CaptureException err) {
		log.error("Error in capture stream:\n" + err.getMessage());
	}

	@Override
	/** Called whenever a new frame is read from the active capture stream */
	public void onNewImage(CaptureStream stream, Image image) {
		if(System.currentTimeMillis() > lastImageWrite + IMAGE_WRITE_INTERVAL) {
			try {
				lastImageWrite = System.currentTimeMillis();
				
				long preSocket = System.currentTimeMillis();
				ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
				DataOutputStream dataOut = new DataOutputStream(byteOutput);
				
				BufferedImage bufImg = AWTImageConverter.toBufferedImage(image);
				ImageIO.write(AWTImageConverter.toBufferedImage(image), "jpg", dataOut);
				dataOut.flush();
				
				byte[] frameBuffer = byteOutput.toByteArray();
				// log.info("Generated packet is " + frameBuffer.length + " bytes long.");
				
				int numPackets = (int)Math.ceil((double)frameBuffer.length / PACKET_SIZE);
				// log.info("Sending data as " + numPackets + " packets.");
				
				byteOutput.reset();
				
				int dataIndex = 0;
				
				for(int segIndex = 0; segIndex < numPackets; segIndex++) {
					// Write out the frame sequence number (either 1 or 0)
					dataOut.writeInt(nextSeqNum);
					
					// Write out the segment number
					dataOut.writeInt(segIndex);
					
					// Write out the "last segment" field
					if(segIndex == numPackets - 1) {
						dataOut.writeBoolean(true);
					} else {
						dataOut.writeBoolean(false);
					}
					
					// Determine how much image data should be written to this
					// packet
					int writeLength = 0;
					if((segIndex * PACKET_SIZE) + PACKET_SIZE < frameBuffer.length) {
						writeLength = PACKET_SIZE;
					} else if ((segIndex * PACKET_SIZE) + PACKET_SIZE >= frameBuffer.length) {
						writeLength = frameBuffer.length - (segIndex * PACKET_SIZE);
					}
					// log.info("Writing " + writeLength + " bytes to packet " + segIndex);
					
					dataOut.write(frameBuffer, (segIndex * PACKET_SIZE), writeLength);
					dataOut.flush();
					
					byte[] packet = byteOutput.toByteArray();
					byteOutput.reset();
					
					synchronized(clients) {
						for(User u : clients) {
							sendDataPacket(u, packet);
						}
					}
				}
				
				//log.info("Writing image to clients took: " 
				//		+ (System.currentTimeMillis() - preSocket) + " ms.");
				
				dataOut.close();
			} catch (IOException e1) {
				log.error("Error multicasting image frame.");
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * Sends the data contained in the passed buffer to the client
	 * @param dataBuffer	The data to send to the client
	 */
	private void sendDataPacket(User user, byte[] dataBuffer) {
		DatagramPacket outputPacket = new DatagramPacket(dataBuffer, dataBuffer.length, 
				user.getAddress(), mediaPort);
		
		try {
			serverSocket.send(outputPacket);
			// log.info("Sent data packet to: " + user.getAddress().getHostAddress() 
			//		+ ":" + mediaPort);
		} catch (IOException e) {
			log.error("Error sending image frame to client: " + user.getAddress().getHostAddress()
					+ ":" + mediaPort);
			e.printStackTrace();
		}
	}
}

package com.RoboWars.opengl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.RoboWars.RoboWars;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Establishes a connection to the media server and streams video frames to the
 * Android client for the OPENGL game.
 */
public class MediaClient {
	/** Size of the buffer for incoming image frames (in bytes) **/
	public static int IMG_BUFFER_SIZE = 32768;

	/** Size of the buffer used for individual incoming packets (in bytes) */
	public static int PACKET_BUFFER_SIZE = 8192;

	/**
	 * The default address and port to use for a streaming media connection
	 */
	public static final String DEFAULT_MEDIA_ADDRESS = "192.168.1.101";
	public static final int DEFAULT_MEDIA_PORT = 33331;

	/**
	 * Reference to the Android view to draw the video stream
	 */
	private final RoboWars view;

	/** Socket used to connect to the server and read images */
	private DatagramSocket mediaSocket;

	/** Thread used to reassemble incoming image data */
	private DecoderThread decodeThread;

	/**
	 * Generates a new MediaClient and sets the input fields to their default
	 * values.
	 * 
	 * @param mediaView
	 *            The view that images read from the network should be rendered
	 *            to
	 * @param addressField
	 *            The field that the server IP should be read from
	 * @param portField
	 *            The field that the server port should be read from
	 * @param statusView
	 *            The text display for status indiciation to be written to
	 */
	public MediaClient(RoboWars view) {
		this.view = view;
		mediaSocket = null;
		decodeThread = null;
	}

	/**
	 * Opens a socket listening on the specified port, and launches a thread to
	 * decode incoming packets.
	 * 
	 * TODO: Calling this function more than once on the same mediaClient is
	 * likely to crash the app
	 * 
	 * @param port
	 *            The port to listen for packets on.
	 */
	public void launchMediaStream(int port) {
		if (mediaSocket != null) {
			mediaSocket.close();
			mediaSocket = null;
		}

		try {
			mediaSocket = new DatagramSocket(port);
		} catch (IOException e) {
			Log.e("RoboWars",
					"Could not bind media socket, video stream will not be supported.");
			mediaSocket = null;
		}
		if (decodeThread != null) {
			decodeThread.terminate();
			decodeThread = null;
		}

		decodeThread = new DecoderThread(mediaSocket, view);
		new Thread(decodeThread).start();
	}

	private class DecoderThread implements Runnable {
		/** The socket to read incoming packets from */
		private final DatagramSocket readSocket;

		/** Buffer to hold incoming image data */
		private final byte[] imageBuffer;

		/** Buffer to hold data from individual packets */
		private final byte[] packetBuffer;

		/** Flag to signal that the thread should terminate */
		private boolean terminationFlag;

		/**
		 * ImageStreamView that images decoded by the thread should be rendered
		 * to
		 */
		private final RoboWars view;

		/**
		 * Generates a new DecoderThread
		 * 
		 * @param readSocket
		 *            The socket to read packets from
		 * @param mediaView
		 *            The view to render images to
		 */
		public DecoderThread(DatagramSocket readSocket, RoboWars view2) {
			this.readSocket = readSocket;
			this.view = view2;
			terminationFlag = false;
			imageBuffer = new byte[IMG_BUFFER_SIZE];
			packetBuffer = new byte[PACKET_BUFFER_SIZE];
		}

		/**
		 * Sets the termination flag (causes the thread to finish any read
		 * operation in progress and terminate).
		 */
		public synchronized void terminate() {
			terminationFlag = true;
		}

		/**
		 * Continually read images from the socket.
		 */
		public void run() {
			Bitmap image = null;
			int expectedFrame = 0;
			int expectedSegment = 0;
			int offset = 0;
			int readLength = 0;
			int totalRead = 0;

			// Flag that should be set when further data from the same frame
			// should be ignored (i.e. when a packet was dropped or received
			// out of order)
			boolean error = false;

			while (!terminationFlag) {

				DatagramPacket recvPacket = new DatagramPacket(packetBuffer,
						packetBuffer.length);

				try {
					mediaSocket.receive(recvPacket);
				} catch (IOException e) {
					Log.e("RoboWars", "Error reading incoming media packet.");
					// TODO: Try to correct somehow?
					continue;
				}

				try {
					ByteArrayInputStream byteInput = new ByteArrayInputStream(
							packetBuffer);
					DataInputStream readData = new DataInputStream(byteInput);

					// Read the frame number (should be either 1 or 0)
					int frameNum = readData.readInt();
					int segmentNum = readData.readInt();
					boolean lastSegment = readData.readBoolean();
					// Log.d("RoboWars", "Read packet, frame#: " + frameNum +
					// "  seg#: " + segmentNum
					// + "   lastSegment: " + lastSegment + "   length: " +
					// recvPacket.getLength());

					if (segmentNum == 0) {
						// This is the first packet of a new frame
						expectedFrame = frameNum;
						expectedSegment = segmentNum + 1;
						offset = 0;
						totalRead = 0;
						error = false;
					} else if (frameNum != expectedFrame) {
						// A packet has been received for an unexpected frame
						// number.
						// This could just be old packet arriving out of order,
						// ignore it
						continue;
					} else if (frameNum == expectedFrame
							&& segmentNum == expectedSegment) {
						// Valid packet arriving in order
						expectedSegment = segmentNum + 1;
					} else if (frameNum == expectedFrame
							&& segmentNum != expectedSegment) {
						// Packet arrived out of order for a frame that has
						// already been
						// partially transmitted. Mark an error, and ignore
						// further
						// packets until the next frame is sent
						error = true;
					}

					if (!error) {
						// Read the image data from the packet to the image
						// buffer

						// (-5) -> Already read 2 ints (4 bytes each) and a
						// boolean (1 byte)
						// Log.d("RoboWars", "Copying data, offset: " + offset +
						// "   length: " + (recvPacket.getLength() - 9));
						readLength = byteInput.read(imageBuffer, offset,
								recvPacket.getLength() - 9);
						offset += readLength;
						totalRead += readLength;

						// If the last segment flag was set, try to decode the
						// received data
						if (lastSegment) {
							long preDecode = System.currentTimeMillis();
							image = BitmapFactory.decodeByteArray(imageBuffer,
									0, totalRead);

							if (image == null) {
								// Happens when only partial data is available,
								// ignore for now
							} else {
								// Log.d("RoboWars", "Decoding took: " +
								// (System.currentTimeMillis() - preDecode)
								// + " ms.");
								view.setVideoImage(Bitmap.createScaledBitmap(
										image, 512, 512, false));
							}
						}

						Thread.yield();
					}
				} catch (IOException e) {
					Log.e("RoboWars",
							"Error reading incoming media packet, skipping.");
					error = true;
				}
			}

			mediaSocket.close();
		}
	}
}

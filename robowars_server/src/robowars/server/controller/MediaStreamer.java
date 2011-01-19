package robowars.server.controller;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.rtp.*;

import org.apache.log4j.Logger;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureSystem;
import com.lti.civil.impl.jni.NativeCaptureSystemFactory;

import net.sf.fmj.*;
import java.net.*;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushDataSource;

import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.media.cdp.GlobalCaptureDevicePlugger;
import net.sf.fmj.media.rtp.RTPSessionMgr;
import net.sf.fmj.ui.FmjStudio;
import net.sf.fmj.ui.application.CaptureDeviceBrowser;
import net.sf.fmj.utility.ClasspathChecker;

/**
 * Handles the streaming of live video and transmission of other camera
 * status messages to connected users. This class also stores a list of
 * all available cameras, and manages the selection of a currently active
 * camera.
 */
public class MediaStreamer {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(MediaStreamer.class);
	
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
	 * Generates a new MediaStreamer
	 */
	public MediaStreamer() 
	{
		RegistryDefaults.registerAll(RegistryDefaults.FMJ);
		cameras = new ArrayList<CameraController>();
		GlobalCaptureDevicePlugger.addCaptureDevices();
	}
	
	/**
	 * Testing function for using the LTI-Civil library (lower level than FMJ)
	 * Usage of the lower level library may be necessary to set video capture
	 * parameters (resolution, frame rate, etc.)
	 */
	public void ltiCivilTest() {
		NativeCaptureSystemFactory captureFactory = new NativeCaptureSystemFactory();
		try {
			CaptureSystem captureSystem = captureFactory.createCaptureSystem();
			List<com.lti.civil.CaptureDeviceInfo> devices = captureSystem.getCaptureDeviceInfoList();
			
			for(com.lti.civil.CaptureDeviceInfo device : devices) {
				log.info("LTI-Civil found device: " + device.getDescription() + " - " + device.getDeviceID());

			}
		} catch (CaptureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Clears the existing camera controller list and re-detects connected
	 * capture devices. Note that the coordinate information for existing
	 * cameras will be lost.
	 */
	public void updateDeviceList() {
		// Store the old list of cameras, and generate a new list to store
		// cameras detected during this update
		List<CameraController> oldCameras = cameras;
		cameras = new ArrayList<CameraController>();

		// Iterate through all detected cameras, and generate a new camera controller
		// for those which did not exist in the previous camera list
		Vector<CaptureDeviceInfo> webcamListing = CaptureDeviceManager.getDeviceList(null);
		for(CaptureDeviceInfo info : webcamListing) {
			if(info.getLocator().toString().startsWith("civil")) {
				log.info("Found video device: " + info.getName());
				for(Format f : info.getFormats()) {
					log.info("\"" + info.getName() + "\" supports format: " + f.toString());
				}
				
				boolean foundExisting = false;
				for(CameraController c : oldCameras) {
					if(c.getMediaLocator() == info.getLocator()) {
						cameras.add(c);
						foundExisting = true;
						break;
					}
				}
				
				if(!foundExisting) {
					CameraController newCam = new CameraController(info);
					cameras.add(newCam);
				}
			}
		}
		
		selectedCamera = null;
		
		oldCameras.clear();
		oldCameras = null;
	}
	
	private void initVideoStreamTest() {
		if(selectedCamera != null) {
			try {
				// create the RTP Manager
				RTPManager rtpManager = RTPManager.newInstance();
				 
				// create the local endpoint for the local interface on
				// any local port
				SessionAddress localAddress = new SessionAddress();
				 
				// initialize the RTPManager
				rtpManager.initialize( localAddress);
		
				// add the ReceiveStreamListener if you need to receive data
				// and do other application specific stuff
				// ...
				 
				// specify the remote endpoint of this unicast session 
				// the address string and port numbers in the following lines
				// need to be replaced with your values.
				
				// TODO: Read these addresses from connected UserProxies
				InetAddress ipAddress = InetAddress.getByName( "168.1.2.3");
				 
				SessionAddress remoteAddress = new SessionAddress(ipAddress, 3000);
		
				// open the connection
				rtpManager.addTarget( remoteAddress);
				 
				// create a send stream for the output data source of a processor
				// and start it
				DataSource dataOutput = Manager.createDataSource(
						 selectedCamera.getMediaLocator());
		
				SendStream sendStream = rtpManager.createSendStream( dataOutput, 1);
				sendStream.start();
				 
				// send data and do other application specific stuff,
				// ...
				 
				// close the connection if no longer needed.
				rtpManager.removeTarget( remoteAddress, "client disconnected.");
				 
				// call dispose at the end of the life-cycle of this RTPManager so
				// it is prepared to be garbage-collected.
				rtpManager.dispose();
			} catch (Exception e) {
				log.error("Error initializing RTP stream.");
				e.printStackTrace();
			}
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
	 * @return	The list of all CameraControllers dedected by the MediaStreamer
	 */
	public List<CameraController> getAvailableCameras() {
		return cameras;
	}
}

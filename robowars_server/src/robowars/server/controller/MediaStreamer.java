package robowars.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;

import org.apache.log4j.Logger;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureSystem;
import com.lti.civil.impl.jni.NativeCaptureSystemFactory;

import net.sf.fmj.*;
import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.media.cdp.GlobalCaptureDevicePlugger;
import net.sf.fmj.ui.FmjStudio;
import net.sf.fmj.ui.application.CaptureDeviceBrowser;
import net.sf.fmj.utility.ClasspathChecker;

/**
 * Handles the streaming of live video and transmission of other camera
 * status messages to connected users. This class also stores a list of
 * all available camera, and manages the selection of a currently active
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
	 * 
	 * TODO:	Keep coordinate information for devices which have been
	 * 			previously configured
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
				
				// ------------- TESTING -----------------
				selectedCamera = cameras.get(0); // TODO: Set through UI, this is just for testing
				// ------------- TESTING -----------------
			}
		}
		
		oldCameras.clear();
		oldCameras = null;
	}
	
	/**
	 * @return	The currently selected CameraController (or null if no
	 * 			camera has been selected)
	 */
	public CameraController getActiveCamera() {
		return selectedCamera;
	}
	
	/**
	 * @return	The list of all CameraControllers dedected by the MediaStreamer
	 */
	public List<CameraController> getAvailableCameras() {
		return cameras;
	}
}

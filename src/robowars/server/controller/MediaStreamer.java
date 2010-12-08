package robowars.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;

import org.apache.log4j.Logger;

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

	public MediaStreamer() 
	{
		cameras = new ArrayList<CameraController>();
		generateCameraList();
	}
	
	/**
	 * Clears the existing camera controller list and re-detects connected
	 * capture devices. Note that the coordinate information for existing
	 * cameras will be lost.
	 */
	public void generateCameraList() {
		// This may only need to be called once at initialization, might
		// want to test moving it to SystemControl
		GlobalCaptureDevicePlugger.addCaptureDevices();
		
		cameras.clear();

		Vector<CaptureDeviceInfo> webcamListing = CaptureDeviceManager.getDeviceList(null);
		for(CaptureDeviceInfo info : webcamListing) {
			if(info.getLocator().toString().startsWith("civil")) {
				log.info("Found video device: " + info.getName());
				for(Format f : info.getFormats()) {
					log.info("\"" + info.getName() + "\" supports format: " + f.toString());
				}
				
				cameras.add(new CameraController(info));
			}
		}

	}
}

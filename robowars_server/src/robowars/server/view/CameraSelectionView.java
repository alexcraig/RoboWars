package robowars.server.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.media.Controller;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import robowars.server.controller.CameraController;
import robowars.server.controller.MediaStreamer;

/**
 * A GUI for the selection and viewing of local video streams.
 */
public class CameraSelectionView extends JFrame implements WindowListener {
	/** Special string to represent no camera selected in camera selection options */
	private final String NO_CAMERA = "No Video Feed";
	
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(CameraSelectionView.class);
	
	/** The MediaStreamer control object managing camera selection and settings */
	private MediaStreamer mediaSrc;
	
	/** 
	 * JPanel which contains the selected video feed (effectively determines
	 * local display resolution) 
	 */
	private JPanel videoPanel;
	
	/** FMJ Player for the selected video feed */
	private Player player;
	
	/** The combo box used for camera selection */
	private JComboBox camSelectBox;
	
	/** A list of available cameras to be displayed in the combo box */
	private DefaultComboBoxModel availableCams;
	
	/** Text fields to display / input settings for the currently selected camera */
	private JTextField xPos, yPos, zPos, hOrientation, vOrientation, fov;

	/**
	 * Generates a new CameraSelectionView, which defaults to hidden.
	 * @param windowTitle	The title for the frame.
	 * @param mediaSource	The MediaStreamer that this view should manage settings for.
	 */
	public CameraSelectionView(String windowTitle, MediaStreamer mediaSource) {
		
		super(windowTitle);
		mediaSrc = mediaSource;
		this.addWindowListener(this);
		
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		this.getContentPane().add(sidePanel, BorderLayout.EAST);
		
		// Source Selection Panel
		JPanel selectionPanel = new JPanel();
		selectionPanel.setBorder(BorderFactory.createTitledBorder("Source Selection"));
		sidePanel.add(selectionPanel, BorderLayout.NORTH);
		
		availableCams = new DefaultComboBoxModel();
		availableCams.addElement(NO_CAMERA);
		availableCams.setSelectedItem(NO_CAMERA);
		camSelectBox = new JComboBox(availableCams);
		
		JButton detectDevices = new JButton("Detect Devices");
		detectDevices.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateDeviceList();
			}
		});
		selectionPanel.add(detectDevices);
		selectionPanel.add(camSelectBox);
		
		// Position Settings panel
		JPanel positionPanel = new JPanel();
		positionPanel.setBorder(BorderFactory.createTitledBorder("Camera Settings"));
		positionPanel.setLayout(new BoxLayout(positionPanel, BoxLayout.Y_AXIS));
		sidePanel.add(positionPanel, BorderLayout.CENTER);
		
		JPanel xPosPanel = new JPanel();
		xPosPanel.add(new JLabel("X Position: "));
		xPos = new JTextField(10);
		xPosPanel.add(xPos);
		positionPanel.add(xPosPanel);
		
		JPanel yPosPanel = new JPanel();
		yPosPanel.add(new JLabel("Y Position: "));
		yPos = new JTextField(10);
		yPosPanel.add(yPos);
		positionPanel.add(yPosPanel);
		
		JPanel zPosPanel = new JPanel();
		zPosPanel.add(new JLabel("Z Position: "));
		zPos = new JTextField(10);
		zPosPanel.add(zPos);
		positionPanel.add(zPosPanel);
		
		JPanel hOrientationPanel = new JPanel();
		hOrientationPanel.add(new JLabel("Horizontal Orientation: "));
		hOrientation = new JTextField(10);
		hOrientationPanel.add(hOrientation);
		positionPanel.add(hOrientationPanel);
		
		JPanel vOrientationPanel = new JPanel();
		vOrientationPanel.add(new JLabel("Vertical Orientation: "));
		vOrientation = new JTextField(10);
		vOrientationPanel.add(vOrientation);
		positionPanel.add(vOrientationPanel);
		
		JPanel fieldOfViewPanel = new JPanel();
		fieldOfViewPanel.add(new JLabel("Field of View: "));
		fov = new JTextField(10);
		fieldOfViewPanel.add(fov);
		positionPanel.add(fieldOfViewPanel);
		
		// Default input fields to disabled until an active camera is found
		setFieldsEnabled(false);
		
		// CAMERA TESTING
		updateDeviceList();
		
		if(mediaSrc.getActiveCamera() != null) {
			updateSettingsFields();
			setFieldsEnabled(true);
			
			try {
				log.debug("Attempting to start streaming of: " 
						+ mediaSrc.getActiveCamera().getCameraName() 
						+ " - " + mediaSrc.getActiveCamera().getMediaLocator());
				player = Manager.createPlayer(mediaSrc.getActiveCamera().getMediaLocator());
				
				player.realize(); // Note: Call does not block
				while(player.getState() != Controller.Realized) {
					// Do nothing / wait for realization
					try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
				}
	
				videoPanel = new JPanel();
				videoPanel.setBorder(BorderFactory.createTitledBorder("Current Video Stream"));
				videoPanel.setPreferredSize(new Dimension(640, 480));
				videoPanel.add(player.getVisualComponent());
				this.getContentPane().add(videoPanel, BorderLayout.CENTER);
				player.start(); // Note: Call does not block
				
			} catch (NoPlayerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.pack();
        this.setResizable(false);
	}
	
	/**
	 * Initiates a refresh of connected devices on the media streamer, and adds
	 * the results to the camera selection combo box.
	 */
	private void updateDeviceList() {
		availableCams.removeAllElements();
		availableCams.addElement(NO_CAMERA);
		
		mediaSrc.updateDeviceList();
		for(CameraController c : mediaSrc.getAvailableCameras()) {
			availableCams.addElement(c);
		}
		
		if(mediaSrc.getActiveCamera() != null) {
			availableCams.setSelectedItem(mediaSrc.getActiveCamera());
		} else {
			availableCams.setSelectedItem(NO_CAMERA);
		}
	}

	@Override
	/** Disable local display of the video stream on window close */
	public void windowClosing(WindowEvent e) {
		log.info("windowClosing");
		if(player != null) {
			// Note: Call to stop() will leave the player in the "Prefetched" state
			player.stop();
		}
	}
	
	@Override
	/** Activate local display of the video stream on window activation */
	public void windowActivated(WindowEvent e) {
		log.info("windowActivated");
		if(player != null && player.getState() == Controller.Prefetched) {
			player.start();
		}
	}
	
	/**
	 * Enables or disables all the input fields for camera settings.
	 * @param enabled	True if the fields should be enabled, false if not
	 */
	private void setFieldsEnabled(boolean enabled) {
		xPos.setEnabled(enabled);
		yPos.setEnabled(enabled);
		zPos.setEnabled(enabled);
		hOrientation.setEnabled(enabled);
		vOrientation.setEnabled(enabled);
		fov.setEnabled(enabled);
	}
	
	/**
	 * Sets the value of all setting input fields to the current settings
	 * of the active camera (if one is selected).
	 */
	private void updateSettingsFields() {
		if(mediaSrc.getActiveCamera() != null) {
			xPos.setText(Float.toString(mediaSrc.getActiveCamera().getxPos()));
			yPos.setText(Float.toString(mediaSrc.getActiveCamera().getyPos()));
			zPos.setText(Float.toString(mediaSrc.getActiveCamera().getzPos()));
			hOrientation.setText(Float.toString(mediaSrc.getActiveCamera().getHorOrientation()));
			vOrientation.setText(Float.toString(mediaSrc.getActiveCamera().getVerOrientation()));
			fov.setText(Float.toString(mediaSrc.getActiveCamera().getFov()));
		}
		
	}

	@Override
	public void windowClosed(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowOpened(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
}

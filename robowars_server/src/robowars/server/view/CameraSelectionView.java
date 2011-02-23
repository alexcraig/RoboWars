package robowars.server.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import robowars.server.controller.CameraController;
import robowars.server.controller.MediaStreamer;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.Image;
import com.lti.civil.awt.AWTImageConverter;
import com.lti.civil.swing.ImageFrame;

/**
 * A GUI for the selection and viewing of local video streams.
 */
public class CameraSelectionView extends JFrame implements WindowListener, CaptureObserver {
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
	
	/** ImageFrame used to display the camera selection preview */
	private ImageFrame player;
	
	/** The combo box used for camera selection */
	private JComboBox camSelectBox;
	
	/** A list of available cameras to be displayed in the combo box */
	private DefaultComboBoxModel availableCams;
	
	/** Text fields to display / input settings for the currently selected camera */
	private JTextField xPos, yPos, zPos, hOrientation, vOrientation, fov;
	
	/** Buttons to apply or reset camera settings */
	private JButton applySettingsBtn, resetSettingsBtn;

	/**
	 * Generates a new CameraSelectionView, which defaults to hidden.
	 * @param windowTitle	The title for the frame.
	 * @param mediaSource	The MediaStreamer that this view should manage settings for.
	 */
	public CameraSelectionView(String windowTitle, MediaStreamer mediaSource) {
		
		super("Camera Options - " + windowTitle);
		mediaSrc = mediaSource;
		this.addWindowListener(this);
		
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		this.getContentPane().add(sidePanel, BorderLayout.EAST);
		
		// --- Source Selection Panel ---
		JPanel selectionPanel = new JPanel();
		selectionPanel.setBorder(BorderFactory.createTitledBorder("Source Selection"));
		sidePanel.add(selectionPanel, BorderLayout.NORTH);
		
		availableCams = new DefaultComboBoxModel();
		availableCams.addElement(NO_CAMERA);
		availableCams.setSelectedItem(NO_CAMERA);
		
		camSelectBox = new JComboBox(availableCams);
		camSelectBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object selectedItem = camSelectBox.getSelectedItem();
				
				// Check if the "NO_CAMERA" string is selected. If so, disable
				// all fields and set the active camera to null
				if(selectedItem == NO_CAMERA) {
					setFieldsEnabled(false);
					mediaSrc.setActiveCamera(null);
				} else {
					// A new camera has been selected
					CameraController newCam = (CameraController)selectedItem;
					mediaSrc.setActiveCamera(newCam);
					refreshSettingsFields();
					setFieldsEnabled(true);
				}
			}
		});
		
		JButton detectDevices = new JButton("Detect Devices");
		detectDevices.addActionListener(new ActionListener() {
			@Override
			/** Trigger a re-detection of connected cameras */
			public void actionPerformed(ActionEvent e) {
				if(mediaSrc.isStreaming()) {
					stopVideoPlayer();
				}
				updateDeviceList();
			}
		});
		selectionPanel.add(detectDevices);
		
		JButton startPreview = new JButton("View Preview");
		startPreview.addActionListener(new ActionListener() {
			@Override
			/** Activate media streaming */
			public void actionPerformed(ActionEvent e) {
				initVideoPlayer(CameraSelectionView.this);
			}
		});
		selectionPanel.add(startPreview);
		
		JButton startNetwork = new JButton("Start Network Stream");
		startNetwork.addActionListener(new ActionListener() {
			@Override
			/** Activate media streaming */
			public void actionPerformed(ActionEvent e) {
				initVideoPlayer(null);
			}
		});
		selectionPanel.add(startNetwork);
		
		JButton stopStream = new JButton("Stop Preview/Stream");
		stopStream.addActionListener(new ActionListener() {
			@Override
			/** Deactivate media streaming */
			public void actionPerformed(ActionEvent e) {
				mediaSrc.stopStream();
				mediaSrc.setObserver(null);
				player.setVisible(false);
			}
		});
		selectionPanel.add(stopStream);
		selectionPanel.add(camSelectBox);
		
		// --- Position Settings panel ---
		JPanel positionPanel = new JPanel();
		positionPanel.setBorder(BorderFactory.createTitledBorder("Camera Settings"));
		positionPanel.setLayout(new BoxLayout(positionPanel, BoxLayout.Y_AXIS));
		sidePanel.add(positionPanel, BorderLayout.CENTER);
		
		xPos = generateTextEntryPanel("X Position: ", 10, positionPanel);
		yPos = generateTextEntryPanel("Y Position: ", 10, positionPanel);
		zPos = generateTextEntryPanel("Z Position: ", 10, positionPanel);
	
		hOrientation = generateTextEntryPanel("Horizontal Orientation: ", 10, 
				positionPanel);
		vOrientation = generateTextEntryPanel("Vertical Orientation: ", 10, 
				positionPanel);
		
		fov = generateTextEntryPanel("Field of View: ", 10, positionPanel);
		
		applySettingsBtn = new JButton("Apply Settings");
		applySettingsBtn.addActionListener(new ActionListener() {
			@Override
			/** 
			 * Attempt to apply changed settings, and refresh the fields if
			 * the update was successful.
			 */
			public void actionPerformed(ActionEvent arg0) {
				if(applySettingsFields()) {
					refreshSettingsFields();
				}
			}
		});
		positionPanel.add(applySettingsBtn);
		
		resetSettingsBtn = new JButton("Reset Settings");
		resetSettingsBtn.addActionListener(new ActionListener() {
			@Override
			/** Reset all settings fields to their currently applied values. */
			public void actionPerformed(ActionEvent arg0) {
				refreshSettingsFields();
			}
		});
		positionPanel.add(resetSettingsBtn);
		
		// Default input fields to disabled until an active camera is found
		setFieldsEnabled(false);
		
		// Camera Preview Frame
		player = new ImageFrame("Camera Preview - " + windowTitle);
		
		
		updateDeviceList();	
		this.pack();
	    this.setResizable(false);
	}
	
	/**
	 * Updates the video player to display the currently selected active camera,
	 * or removes it if no camera is selected.
	 * @param observer	The observer to use for the stream (typically "this" to
	 * 					display a local preview, and "null" for network stream)
	 */
	private void initVideoPlayer(CaptureObserver observer) {
		mediaSrc.stopStream();
		mediaSrc.setObserver(observer);
		mediaSrc.playStream();
		if(mediaSrc.isStreaming() && observer == this) {
			player.setVisible(true);
		} else {
			player.setVisible(false);
		}
	}
	
	/**
	 * Stops any currently active video player and removes it from the frame
	 */
	private void stopVideoPlayer() {
		mediaSrc.stopStream();
		mediaSrc.setObserver(null);
		player.setVisible(false);
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
	
	/**
	 * Generates a text entry panel (a panel containing a JLabel and a JTextField),
	 * and adds it to the provided containing panel.
	 * @param label	The text string that should be displayed in the JLabel
	 * @param textColumns	The number of columns for the JTextField
	 * @param containingPanel	The panel that the resulting panel should be added to
	 * @return	A reference to the contained JTextField
	 */
	private JTextField generateTextEntryPanel(String label, int textColumns, 
			JPanel containingPanel) {
		JPanel entryPanel = new JPanel();
		entryPanel.add(new JLabel(label));
		JTextField textEntry = new JTextField(10);
		textEntry.addKeyListener(new TextFieldEditListener());
		entryPanel.add(textEntry);
		containingPanel.add(entryPanel);
		return textEntry;
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
		applySettingsBtn.setEnabled(enabled);
		resetSettingsBtn.setEnabled(enabled);
		
		if(!enabled) {
			xPos.setText("");
			yPos.setText("");
			zPos.setText("");
			hOrientation.setText("");
			vOrientation.setText("");
			fov.setText("");
		}
	}
	
	/**
	 * Sets the value of all setting input fields to the current settings
	 * of the active camera (if one is selected).
	 */
	private void refreshSettingsFields() {
		if(mediaSrc.getActiveCamera() != null) {
			xPos.setText(Float.toString(mediaSrc.getActiveCamera().getxPos()));
			yPos.setText(Float.toString(mediaSrc.getActiveCamera().getyPos()));
			zPos.setText(Float.toString(mediaSrc.getActiveCamera().getzPos()));
			hOrientation.setText(Float.toString(mediaSrc.getActiveCamera().getHorOrientation()));
			vOrientation.setText(Float.toString(mediaSrc.getActiveCamera().getVerOrientation()));
			fov.setText(Float.toString(mediaSrc.getActiveCamera().getFov()));
		}
		
		xPos.setBackground(Color.WHITE);
		yPos.setBackground(Color.WHITE);
		zPos.setBackground(Color.WHITE);
		hOrientation.setBackground(Color.WHITE);
		vOrientation.setBackground(Color.WHITE);
		fov.setBackground(Color.WHITE);
	}
	
	/**
	 * Applies the values of the settings fields to the currently active camera
	 * controller (if they are all read as valid).
	 * 
	 * @return	True if settings were successfully applied, false if not
	 */
	private boolean applySettingsFields() {
		try {
			// Read settings from value text fields
			float xPosVal = Float.parseFloat(xPos.getText());
			float yPosVal = Float.parseFloat(yPos.getText());
			float zPosVal = Float.parseFloat(zPos.getText());
			
			float hOrienVal = Float.parseFloat(hOrientation.getText());
			float vOrienVal = Float.parseFloat(vOrientation.getText());
			
			float fovVal = Float.parseFloat(fov.getText());
			
			// TODO: Additional checks on value correctness (no negatives, etc.)
			//		 Need to figure out best way to represent values to OpenGL first
			
			// Apply settings to current camera controller
			if(mediaSrc.getActiveCamera() != null) {
				mediaSrc.getActiveCamera().setPosition(xPosVal, yPosVal, zPosVal);
				mediaSrc.getActiveCamera().setOrientation(hOrienVal, vOrienVal);
				mediaSrc.getActiveCamera().setFov(fovVal);
				return true;
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, 
					"One or more camera setting values could not be parsed as valid numbers.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		
		return false;
	}
	
	// CaptureObserver Methods
	@Override
	public void onError(CaptureStream arg0, CaptureException arg1) {
	}

	@Override
	public void onNewImage(CaptureStream arg0, Image image) {
		player.setImage(AWTImageConverter.toBufferedImage(image));
	}

	// WindowListener Methods
	@Override
	/** Disable local display of the video stream on window close */
	public void windowClosing(WindowEvent e) {
		stopVideoPlayer();
	}
	
	@Override
	/** Activate local display of the video stream on window activation */
	public void windowActivated(WindowEvent e) {
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
	
	/**
	 * A listener to capture events whenever a text field is edited.
	 */
	private class TextFieldEditListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent arg0) {
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
		}

		@Override
		/** 
		 * Changes the background color of a text field when text is entered
		 * (used to indicate that changes have not been saved)
		 */
		public void keyTyped(KeyEvent arg0) {
			((JTextField)arg0.getSource()).setBackground(
					new Color(255, 200, 200));
		}
		
	}
}

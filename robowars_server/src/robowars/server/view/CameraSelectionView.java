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
		
		JPanel xCoord = new JPanel();
		xCoord.add(new JLabel("X Position: "));
		xCoord.add(new JTextField(10));
		positionPanel.add(xCoord);
		
		JPanel yCoord = new JPanel();
		yCoord.add(new JLabel("Y Position: "));
		yCoord.add(new JTextField(10));
		positionPanel.add(yCoord);
		
		JPanel zCoord = new JPanel();
		zCoord.add(new JLabel("Z Position: "));
		zCoord.add(new JTextField(10));
		positionPanel.add(zCoord);
		
		JPanel xHeading = new JPanel();
		xHeading.add(new JLabel("X Heading: "));
		xHeading.add(new JTextField(10));
		positionPanel.add(xHeading);
		
		JPanel yHeading = new JPanel();
		yHeading.add(new JLabel("Y Heading: "));
		yHeading.add(new JTextField(10));
		positionPanel.add(yHeading);
		
		JPanel zHeading = new JPanel();
		zHeading.add(new JLabel("Z Heading: "));
		zHeading.add(new JTextField(10));
		positionPanel.add(zHeading);
		
		JPanel fieldOfView = new JPanel();
		fieldOfView.add(new JLabel("Field of View: "));
		fieldOfView.add(new JTextField(10));
		positionPanel.add(fieldOfView);
		
		
		// CAMERA TESTING
		updateDeviceList();
		
		if(mediaSource.getActiveCamera() != null) {
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

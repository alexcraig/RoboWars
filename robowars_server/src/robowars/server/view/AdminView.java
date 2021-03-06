package robowars.server.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import lejos.robotics.Pose;

import org.apache.log4j.Logger;

import robowars.server.controller.BluetoothServer;
import robowars.server.controller.LobbyChatEvent;
import robowars.server.controller.LobbyGameEvent;
import robowars.server.controller.LobbyRobotEvent;
import robowars.server.controller.LobbyUserEvent;
import robowars.server.controller.MediaStreamer;
import robowars.server.controller.RobotProxy;
import robowars.server.controller.ServerLobby;
import robowars.server.controller.ServerLobbyEvent;
import robowars.server.controller.ServerLobbyListener;
import robowars.shared.model.GameEvent;
import robowars.shared.model.GameListener;
import robowars.shared.model.GameModel;
import robowars.shared.model.GameType;
import robowars.shared.model.RobotCommand;

/**
 * Provides a GUI for the server administrator to modify configuration options
 * and monitor the current server/game state.
 */
public class AdminView extends JFrame implements GameListener, ServerLobbyListener {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(AdminView.class);
	
	/** List models for the list of connected robots and users */
	private DefaultListModel robotListModel, userListModel;
	
	/** Area for displaying chat text */
	private JTextArea mainChatArea;
	
	/** JLabel to show the currently selected game type */
	private JLabel curGameType;
	
	/** Reference to the ServerLobby this AdminView should manage */
	private ServerLobby lobby;
	
	/** The server responsible for communication the the NXT robots */
	private BluetoothServer bluetooth;
	
	/** Reference to the frame used for camera selection */
	private JFrame cameraSelect;
	
	/** 
	 * Generates a new AdminView frame
	 * @param frameTitle	The title of the frame
	 * @param lobby			The ServerLobby that this view should listen for events from
	 * @param mediaSource	The MediaStreamer managing camera selection and settings
	 * @param BluetoothServer	The BluetoothServer responsible for robot communications
	 **/
	public AdminView(String windowTitle, ServerLobby lobby, MediaStreamer mediaSource,
			BluetoothServer bluetooth) {
		super(windowTitle);
		
		// TODO:	Should have a window listener that cleanly terminates active
		// 			connections on window close.
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setLayout(new BorderLayout());
        this.lobby = lobby;
        this.bluetooth = bluetooth;

		// Setup menus
        initMenus();
        
        // Setup main content area
        initContentArea();
        
        // Initialize the camera selection frame
        cameraSelect = new CameraSelectionView(windowTitle, 
        		mediaSource);
        cameraSelect.setLocationRelativeTo(this);

        // Set view to listen on the provided ServerLobby
        lobby.addLobbyStateListener(this);
        
        this.pack();
        this.setResizable(false);
		this.setVisible(true);
	}
	

	/**
	 * Initializes the menu bar for the frame.
	 */
	private void initMenus() {
		JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        
		// File
		JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        this.getJMenuBar().add(fileMenu);
        
		// File -> Quit
        JMenuItem quit = new JMenuItem("Quit", KeyEvent.VK_Q);
        quit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                        AdminView.this.dispose();
                        System.exit(0);
                }
        });
        fileMenu.add(quit);
        
        // Settings 
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic(KeyEvent.VK_S);
        this.getJMenuBar().add(settingsMenu);
        
        // Settings -> Camera Options
        JMenuItem cameraOptions = new JMenuItem("Camera Options", KeyEvent.VK_C);
        cameraOptions.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                	cameraSelect.setVisible(true);
                }
        });
        settingsMenu.add(cameraOptions);
        
        // Robots
        JMenu robotsMenu = new JMenu("Robots");
        settingsMenu.setMnemonic(KeyEvent.VK_R);
        this.getJMenuBar().add(robotsMenu);
        
        // Robots -> Launch Robot Detection
        JMenuItem detectRobots = new JMenuItem("Launch Robot Detection", KeyEvent.VK_L);
        detectRobots.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
					bluetooth.initRobotDetection();
                }
        });
        robotsMenu.add(detectRobots);
	}
	
	/**
	 * Initializes the main content area of the frame (player and robot
	 * lists, as well as the chat area and status label)
	 */
	private void initContentArea() {
		// Setup user and player lists
        robotListModel = new DefaultListModel();
        userListModel = new DefaultListModel();
        JList robotList = new JList(robotListModel);
        JList userList = new JList(userListModel);
        robotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        new PositionResetListener(robotList);
        
        // Setup the connected robots and user lists
        JPanel connectedListsPanel = new JPanel();
        connectedListsPanel.setLayout(new BorderLayout());
        this.getContentPane().add(connectedListsPanel, BorderLayout.EAST);
        
        JPanel userListPanel = new JPanel();
        userListPanel.setBorder(BorderFactory.createTitledBorder("Connected Users"));
        userListPanel.add(new JScrollPane(userList));
        connectedListsPanel.add(userListPanel, BorderLayout.NORTH);
        
        JPanel robotListPanel = new JPanel();
        robotListPanel.setBorder(BorderFactory.createTitledBorder("Connected Robots"));
        robotListPanel.add(new JScrollPane(robotList));
        connectedListsPanel.add(robotListPanel, BorderLayout.SOUTH);
        
        // Setup the main chat area
        mainChatArea = new JTextArea();
        mainChatArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        mainChatArea.setEditable(false);
        mainChatArea.setLineWrap(true);
        mainChatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(mainChatArea);
        chatScrollPane.setPreferredSize(new Dimension(500,200));
        this.getContentPane().add(chatScrollPane, BorderLayout.WEST);
        
        // Add a label showing the currently selected game type
        curGameType = new JLabel();
        setGameTypeLabel(lobby.getCurrentGameType());
		this.getContentPane().add(curGameType, BorderLayout.SOUTH);
	}

	@Override
	/** @see ServerLobbyListener#userStateChanged(LobbyUserEvent) */
	public void userStateChanged(LobbyUserEvent event) {
		if(event.getEventType() == ServerLobbyEvent.EVENT_PLAYER_JOINED) {
			userListModel.addElement(event.getUser().getUsername());
			addLineToMainChat(event.toString());
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_PLAYER_LEFT) {
			userListModel.removeElement(event.getUser().getUsername());
			addLineToMainChat(event.toString());
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_PLAYER_STATE_CHANGE) {
			addLineToMainChat(event.toString());
		}
	}

	@Override
	/** @see ServerLobbyListener#robotStateChanged(LobbyRobotEvent) */
	public void robotStateChanged(LobbyRobotEvent event) {
		if(event.getEventType() == ServerLobbyEvent.EVENT_ROBOT_REGISTERED) {
			robotListModel.addElement(event.getRobot().getIdentifier());
			addLineToMainChat(event.toString());
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_ROBOT_UNREGISTERED) {
			robotListModel.removeElement(event.getRobot().getIdentifier());
			addLineToMainChat(event.toString());
		}
	}

	@Override
	/** @see ServerLobbyListener#lobbyGameStateChanged(LobbyGameEvent) */
	public void lobbyGameStateChanged(LobbyGameEvent event) {
		if(event.getEventType() == ServerLobbyEvent.EVENT_GAMETYPE_CHANGE) {
			addLineToMainChat(event.toString());
			setGameTypeLabel(event.getGameType());
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_GAME_LAUNCH) {
			// addLineToMainChat(event.toString());
			ServerLobby source = (ServerLobby) event.getSource();
			Admin2DGameView view = new Admin2DGameView(500, source.getCurrentGame().getGameModel());
			source.getCurrentGame().getGameModel().addListener(view);
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_GAME_OVER) {
			// addLineToMainChat(event.toString());
		}
		
	}
	
	/** 
	 * Adds a time stamped line of text to the main chat area (followed by
	 * a newline)
	 * @param text	The text to add to the chat pane
	 */
	private void addLineToMainChat(String text) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("[h:mm:ss a] ");
		mainChatArea.setText(mainChatArea.getText() + dateFormat.format(new Date())
				+ text + "\n");
	}
	
	/**
	 * Sets the game type label to show the specified game type
	 * @param gameType	The game type to display
	 */
	private void setGameTypeLabel(GameType gameType) {
		curGameType.setText("Selected Game Type: " + gameType.toString() + " (Minimum players = "
				+ gameType.getMinimumPlayers() + ")");
		this.pack();
	}


	@Override
	/** @see ServerLobbyListener#lobbyChatMessage(LobbyChatEvent) */
	public void lobbyChatMessage(LobbyChatEvent event) {
		 addLineToMainChat(event.toString());
	}
	@Override
	public void gameStateChanged(GameEvent event){}
	
	/**
	 * Control class which manages issuing position reset commands to registered
	 * robots based on admin input.
	 */
	private class PositionResetListener extends MouseAdapter {
		/** 
		 * The JList that this list should use to determine clicked indexes.
		 */
		private JList robotList;
		
		/**
		 * Generates a new PositionResetListener watching the passed JList
		 * @param robotList	The list of connected robot identifiers
		 */
		public PositionResetListener(JList robotList) {
			this.robotList = robotList;
			robotList.addMouseListener(this);
		}
		
		/**
		 * If a robot in the list of connected robots is double clicked and no
		 * game is currently in progress that admin is prompted to enter a new
		 * position and heading for the robot.
		 */
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if(lobby.gameInProgress()) {
					// Ignore attempts to reset position when game is in progress
					log.info("Attempted position reset during active game.");
					return;
				}
				
				int index = robotList.locationToIndex(e.getPoint());
				String robotId = (String)robotList.getModel().getElementAt(index);
				try {
					float xPos = Float.parseFloat(JOptionPane.showInputDialog(AdminView.this, 
							"Please enter new X position coordinate.",
							robotId + " - Position Reset", JOptionPane.DEFAULT_OPTION));
					float yPos = Float.parseFloat(JOptionPane.showInputDialog(AdminView.this, 
							"Please enter new Y position coordinate.",
							robotId + " - Position Reset", JOptionPane.DEFAULT_OPTION));
					float heading = Float.parseFloat(JOptionPane.showInputDialog(AdminView.this, 
							"Please enter new heading.",
							robotId + " - Position Reset", JOptionPane.DEFAULT_OPTION));
					
					Pose newPos = new Pose(xPos, yPos, heading);
					
					RobotProxy robot = lobby.getRobotProxy(robotId);
					if(robot != null) {
						robot.sendCommand(RobotCommand.setPosition(newPos));
					}
					  
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(AdminView.this, 
							"Invalid input, please try again.",
					robotId + " - Position Reset (FAILED)", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}

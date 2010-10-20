package robowars.server.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;

import org.apache.log4j.Logger;

import robowars.server.controller.*;
import robowars.shared.model.GameListener;
import robowars.shared.model.GameType;

/**
 * Provides a GUI for the server administrator to modify configuration options
 * and monitor the current server/game state.
 */
public class AdminView extends JFrame implements GameListener, ServerLobbyListener {
	/** The logger used by this class */
	private static Logger log = Logger.getLogger(UserProxy.class);
	
	/** List models for the list of connected robots and users */
	DefaultListModel robotListModel, userListModel;
	
	/** Area for displaying chat text */
	JTextArea mainChatArea;
	
	/** JLabel to show the currently selected game type */
	private JLabel curGameType;
	
	/** 
	 * Generates a new AdminView frame
	 * @param frameTitle	The title of the frame
	 * @param lobby			The ServerLobby that this view should listen for events from
	 **/
	public AdminView(String windowTitle, ServerLobby lobby) {
		super(windowTitle);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
		// Set look and feel
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
		   try {
		        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		    }
		    catch (Exception e2) {
		        System.err.println("Unable to load default look and feel.");
		        System.exit(1); // Might not want to exit
		    }
		}

		// Setup Menus
		JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        initFileMenu();
        
        // Setup user and player lists
        robotListModel = new DefaultListModel();
        userListModel = new DefaultListModel();
        JList robotList = new JList(robotListModel);
        JList userList = new JList(userListModel);
        
        // Setup the connected robots and user lists
        JPanel connectedListsPanel = new JPanel();
        connectedListsPanel.setLayout(new BorderLayout());
        this.getContentPane().add(connectedListsPanel, BorderLayout.EAST);
        
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BorderLayout());
        JLabel userLabel = new JLabel("Users");
        userLabel.setPreferredSize(new Dimension(100, 15));
        userListPanel.add(userLabel, BorderLayout.NORTH);
        userListPanel.add(new JScrollPane(userList), BorderLayout.SOUTH);
        connectedListsPanel.add(userListPanel, BorderLayout.NORTH);
        
        JPanel robotListPanel = new JPanel();
        robotListPanel.setLayout(new BorderLayout());
        JLabel robotLabel = new JLabel("Robots");
        robotLabel.setPreferredSize(new Dimension(100, 15));
        robotListPanel.add(robotLabel, BorderLayout.NORTH);
        robotListPanel.add(new JScrollPane(robotList), BorderLayout.SOUTH);
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

        // Set view to listen on the provided ServerLobby
        lobby.addLobbyStateListener(this);
        
        this.pack();
        this.setResizable(false);
		this.setVisible(true);
	}
	

	/**
	 * Initializes the "File" menu and adds it to the main menu bar.
	 */
	private void initFileMenu() {
		// Add the "File" menu
		JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        this.getJMenuBar().add(fileMenu);
        
		// Quit Menu Option
        JMenuItem quit = new JMenuItem("Quit", KeyEvent.VK_Q);
        quit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                        AdminView.this.dispose();
                        System.exit(0);
                }
        });
        fileMenu.add(quit);

	}

	@Override
	/** @see ServerLobbyListener#userStateChanged(UserStateEvent) */
	public void userStateChanged(UserStateEvent event) {
		if(event.getEventType() == ServerLobbyEvent.EVENT_PLAYER_JOINED) {
			userListModel.addElement(event.getUser().getUsername());
			addLineToMainChat(event.getUser().getUsername() + " has joined the server.");
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_PLAYER_LEFT) {
			userListModel.removeElement(event.getUser().getUsername());
			addLineToMainChat(event.getUser().getUsername() + " has left the server.");
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_PLAYER_CHAT_MESSAGE) {
			addLineToMainChat(event.getUser().getUsername() + ": " + event.getUser().getLastChatMessage());
		}
		
	}

	@Override
	/** @see ServerLobbyListener#robotStateChanged(RobotStateEvent) */
	public void robotStateChanged(RobotStateEvent event) {
		if(event.getEventType() == ServerLobbyEvent.EVENT_ROBOT_REGISTERED) {
			robotListModel.addElement(event.getRobot().getIdentifier());
			addLineToMainChat("Robot " + event.getRobot().getIdentifier() + " has registered with the server.");
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_ROBOT_UNREGISTERED) {
			robotListModel.removeElement(event.getRobot().getIdentifier());
			addLineToMainChat("Robot " + event.getRobot().getIdentifier() + " has unregistered with the server.");
		}
	}

	@Override
	/** @see ServerLobbyListener#lobbyGameStateChanged(LobbyGameEvent) */
	public void lobbyGameStateChanged(LobbyGameEvent event) {
		if(event.getEventType() == ServerLobbyEvent.EVENT_GAMETYPE_CHANGE) {
			addLineToMainChat("Game type changed to: " + event.getGameType().toString());
			setGameTypeLabel(event.getGameType());
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_GAME_LAUNCH) {
			addLineToMainChat("--- New game has been launched ---");
		} else if (event.getEventType() == ServerLobbyEvent.EVENT_GAME_OVER) {
			addLineToMainChat("--- Game in progress terminated ---");
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
		curGameType.setText("Selected Game Type: " + gameType.toString());
		this.pack();
	}
}

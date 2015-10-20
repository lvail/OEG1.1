package client;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.Timer;

import shared.action.Action;

/**
 * ClientApplet provides some helper functions and assistance to Requests.java.
 * In particular, it handles the close of the login window and starts the
 * ClientMessges and ClientOperator classes for this applet to connect to the
 * server. Also, it contains the round countdown logic to update the countdown
 * timer in the applet. The design of this class and the other applet GUI
 * classes including Map.java are not fully optimal and could be more
 * straightforward.
 */
public class ClientFrame implements ActionListener {

    /** ClientOperator for this Client */
    ClientOperator operator;

    /** How many milliseconds remain in the countdown. */
    long remaining;
    /** The total length of a round, in milliseconds */
    long roundLength;
    /** The current round number */
    int curRound;
    /** The total number of rounds */
    int totalRnd;
    /** When countdown was last updated */
    long lastUpdate;
    /** Timer to update the countdown every second */
    Timer timer;
    /** Format minutes:seconds with leading zeros */
    NumberFormat format;
    /** The main GUI to display information */
    public Requests GUI;

    public Login login;

    /**
     * Start the applet by creating a login dialog and setting the on close
     * handler to start the connection when that dialog is closed.
     * 
     * @param GUI
     *            The main applet GUI object.
     */
    public ClientFrame(Requests GUIIn) {

        GUI = GUIIn;

        format = NumberFormat.getNumberInstance();
        format.setMinimumIntegerDigits(2); // pad with 0 if necessary
        try {
            // create the Login window to get team name and server location
            final Login dialog = this.login = new Login(new JFrame(""), this);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    dialog.setVisible(true);
                }
            });

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Callback method called when the Login window closes Creates a
     * ClientOperator with the info from Login window. Sends the initial logon
     * request to the server.
     * 
     * @param login
     *            The Login window that contains server IP and team name
     *            information
     */
    public void dialogClose(Login login, boolean register) {
        @SuppressWarnings("deprecation")
        String password = login.passwordField.getText().trim();
        if (register) {
            JPanel panel = new JPanel();
            JLabel label = new JLabel("Verify password: ");
            final JPasswordField pass = new JPasswordField(15);
            panel.add(label);
            panel.add(pass);
            pass.requestFocus();
            ScheduledExecutorService scheduler =
                            Executors.newSingleThreadScheduledExecutor();

            scheduler.schedule(new Runnable() {
                public void run() {
                    pass.requestFocus();
                }
            }, 100L, TimeUnit.MILLISECONDS);

            int option = JOptionPane.showConfirmDialog(login, panel, "Register",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);
            if (option != JOptionPane.OK_OPTION)
                return;

            String result = new String(pass.getPassword()).trim();
            if (!password.equals(result)) {
                JOptionPane.showMessageDialog(login,
                                "You entered different passwords!", "Register",
                                JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        String team = login.getTxtTeamName().getText();

        team = team.trim().replace("#", "").replace(":", "");
        if (team.length() > 128) {
            team = team.substring(0, 128);
        }

        String ip = login.getTxtServerIpAddress().getText().trim();
        if (team.length() > 0) {
            login.setVisible(false);
            ClientMessages serverSocket;
            if ((serverSocket = initializeConnection(ip)) != null) {
                GUI.setTeamName(team);
                setIP(ip);
                operator = new ClientOperator(this, team, serverSocket);
                GUI.setOperator(operator);
                serverSocket.sendLogin(register, team, password);
            }
            else {
                JOptionPane.showMessageDialog(login,
                                "Failed to connect to server.", "Connect",
                                JOptionPane.ERROR_MESSAGE);
                login.setVisible(true);
            }
        }
    }

    /**
     * Create the connection with the server at the given IP Set serverSock to
     * hold the ClientMessages socket object
     * 
     * @param ip
     *            Location of the OEG server.
     * @return True if connection is successful.
     */
    public ClientMessages initializeConnection(String ip) {
        ClientMessages serverSocket = null;
        try {
            // create a socket connection to the server
            // Socket sock = new Socket(ip, 8121);

            // passes ip to create socket and puts it on port 8121
            Socket connectionSocket = new Socket();
            connectionSocket.connect(new InetSocketAddress(ip, 8121), 3000);
            connectionSocket.setSoTimeout(0);

            // create a new thread to listen for input from server
            serverSocket = new ClientMessages(this, connectionSocket);

            serverSocket.start();

        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("socket failed to start");
        }
        return serverSocket;
    }

    /** ActionPerform handler called when the second timer hits zero */
    public void actionPerformed(ActionEvent e) {
        updateDisplay();
    }

    /**
     * Update the displayed time in the UI. This method is called from
     * actionPerformed() which is itself invoked by the timer.
     */
    void updateDisplay() {
        long now = System.currentTimeMillis(); // current time in ms
        long elapsed = now - lastUpdate; // ms elapsed since last update
        remaining -= elapsed; // adjust remaining time
        lastUpdate = now; // remember this update time

        // Convert remaining milliseconds to mm:ss format and display
        if (remaining <= 0) {
            remaining = 0; // roundLength;
        }

        int hours = (int) remaining / 60000 / 60;
        int minutes = (int) (remaining / 60000) % 60;
        int seconds = (int) ((remaining % 60000) / 1000);
        String days = "";
        if (hours >= 24) {
            days = (hours / 24) + ((hours / 24 == 1) ? " Day, " : " Days - ");
            hours = hours % 24;
        }
        GUI.setTimer(curRound,
                        days + format.format(hours) + ":"
                                        + format.format(minutes) + ":"
                                        + format.format(seconds),
                        totalRnd);

        // This changes the color of the timer when the time is less than 10
        // seconds
        if (seconds <= 10 && minutes == 1 && hours == 1) {
            if ((seconds % 2) == 0)
                GUI.flashTimer(1);
            else
                GUI.flashTimer(0);
        }
        else
            GUI.flashTimer(2);
        // If we've completed the countdown beep and display new page

        GUI.setBank(operator.getBank().getAdjustedBalance());
    }

    /**
     * Reset the countdown second timer.
     */
    void resume() {
        // Restore the time we're counting down from and restart the timer.
        timer = new Timer(1000, this);
        timer.setInitialDelay(0); // First timer is immediate.
        timer.start(); // Start the timer
    }

    /**
     * Show a string in the history window of the GUI
     * 
     * @param result
     *            The string to be displayed
     */
    public void addActionQueue(Action result) {
        GUI.appendResult(result);
    }

    /**
     * Pass through to the Requests GUI to check if an action exists in the
     * action queue.
     * 
     * @param check
     *            The Action to check for.
     * @return True if the action exists in the action queue.
     */
    public boolean checkActionQueue(Action check) {
        return GUI.checkExists(check);
    }

    /**
     * Display the location that the applet is connected to in the GUI.
     * 
     * @param ip
     *            Network location of the server.
     */
    public void setIP(String ip) {
        GUI.setIP(ip);
    }

    /**
     * Pass through to the GUI to reset the Action queue.
     */
    public void resetActionQueue() {
        GUI.resetActionQueue();
    }

    /**
     * Set and create the Map by passing it the size of the grid.
     * 
     * @param limit
     *            A Point representing the size of the Map.
     */
    public void setMap(Point limit) {
        GUI.constructMap(limit);
    }

    /**
     * Display a message to the user on the GUI.
     * 
     * @param string
     *            The message to show.
     */
    public void displayMessage(String string) {
        GUI.addMessage(string);
    }

    /**
     * Disable the GUI from starting any more actions.
     */
    public void killAll() {
        // GUI.setEnabled(false);
        GUI.disableActions();
    }

    /**
     * Pass through to refresh the GUI paint display.
     */
    public void refreshGUI() {
        GUI.refreshGUI();
    }
}

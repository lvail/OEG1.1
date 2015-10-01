package client;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * A simple Login JDialog to get login information from the client user before a
 * connection is made with the server.
 */
public class Login extends JDialog implements ActionListener {
    /** UID needed for JDialog */
    private static final long serialVersionUID = -747551867879253130L;
    /** Panel that contains all content of the dialog */
    private final JPanel      contentPanel     = new JPanel();
    /** The text field to get the IP address of the server from the user */
    private JTextField        txtServerIpField;
    /** The text field to get the team name from the user */
    private JTextField        txtTeamName;
    public JPasswordField     passwordField;
    boolean                   id               = false;
    private JButton           ok, can;

    /**
     * Create the dialog.
     * 
     * @param callback
     *            is the client
     */
    public Login(Frame clientApplet, final ClientApplet callback) {
        super(clientApplet, "Login", true);
        setLayout(new GridBagLayout());
        setResizable(false);
        setLocation(400, 300);
        /********************** IP Label */
        JLabel ipLabel = new JLabel("Host:");
        GridBagConstraints gbc_ipLabel = new GridBagConstraints();
        gbc_ipLabel.gridx = 0;
        gbc_ipLabel.gridy = 0;
        gbc_ipLabel.insets = new Insets(9, 8, 6, 2);
        gbc_ipLabel.anchor = GridBagConstraints.WEST;
        gbc_ipLabel.fill = GridBagConstraints.NONE;
        add(ipLabel, gbc_ipLabel);
        /********************** Ip Field */
        txtServerIpField = new JTextField(15);
        GridBagConstraints gbc_txtServerIpField = new GridBagConstraints();
        gbc_txtServerIpField.gridx = 1;
        gbc_txtServerIpField.gridy = 0;
        gbc_txtServerIpField.weightx = 1.0;
        gbc_txtServerIpField.gridwidth = 2;
        gbc_txtServerIpField.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtServerIpField.insets = new Insets(9, 4, 6, 8);
        gbc_txtServerIpField.anchor = GridBagConstraints.EAST;
        add(txtServerIpField, gbc_txtServerIpField);
        /********************** Team Label */
        JLabel nameLabel = new JLabel("Team:");
        GridBagConstraints gbc_nameLabel = new GridBagConstraints();
        gbc_nameLabel.gridx = 0;
        gbc_nameLabel.gridy = 1;
        gbc_nameLabel.insets = new Insets(6, 8, 6, 2);
        gbc_nameLabel.anchor = GridBagConstraints.WEST;
        gbc_nameLabel.fill = GridBagConstraints.NONE;
        add(nameLabel, gbc_nameLabel);
        /********************** Team Field */
        txtTeamName = new JTextField(15);
        GridBagConstraints gbc_txtTeamName = new GridBagConstraints();
        gbc_txtTeamName.gridx = 1;
        gbc_txtTeamName.gridy = 1;
        gbc_txtTeamName.weightx = 1.0;
        gbc_txtTeamName.gridwidth = 2;
        gbc_txtTeamName.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtTeamName.insets = new Insets(6, 4, 6, 8);
        gbc_txtTeamName.anchor = GridBagConstraints.EAST;
        add(txtTeamName, gbc_txtTeamName);
        /********************** Password Label */
        JLabel passwordLabel = new JLabel("Password:");
        GridBagConstraints gbc_passwordLabel = new GridBagConstraints();
        gbc_passwordLabel.gridx = 0;
        gbc_passwordLabel.gridy = 2;
        gbc_passwordLabel.insets = new Insets(6, 8, 6, 2);
        gbc_passwordLabel.anchor = GridBagConstraints.WEST;
        gbc_passwordLabel.fill = GridBagConstraints.NONE;
        add(passwordLabel, gbc_passwordLabel);
        /********************** Password Field */
        passwordField = new JPasswordField(15);
        GridBagConstraints gbc_passwordField = new GridBagConstraints();
        gbc_passwordField.gridx = 1;
        gbc_passwordField.gridy = 2;
        gbc_passwordField.weightx = 1.0;
        gbc_passwordField.gridwidth = 2;
        gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
        gbc_passwordField.insets = new Insets(6, 4, 6, 8);
        gbc_passwordField.anchor = GridBagConstraints.EAST;
        add(passwordField, gbc_passwordField);
        /*********************** OK Button */
        JButton okButton = new JButton("OK");
        GridBagConstraints gbc_okButton = new GridBagConstraints();
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                callback.dialogClose(Login.this, false);
            }
        });
        gbc_okButton.gridx = 0;
        gbc_okButton.gridy = 3;
        gbc_okButton.insets = new Insets(6, 7, 8, 3);
        gbc_okButton.fill = GridBagConstraints.HORIZONTAL;
        add(okButton, gbc_okButton);
        getRootPane().setDefaultButton(okButton);
        /*********************** Register Button */
        JButton registerButton = new JButton("Register");
        GridBagConstraints gbc_registerButton = new GridBagConstraints();
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                callback.dialogClose(Login.this, true);
            }
        });
        gbc_registerButton.gridx = 1;
        gbc_registerButton.gridy = 3;
        gbc_registerButton.insets = new Insets(6, 3, 8, 3);
        add(registerButton, gbc_registerButton);
        /*********************** Cancel Button */
        JButton cancelButton = new JButton("Cancel");
        GridBagConstraints gbc_cancelButton = new GridBagConstraints();
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });
        gbc_cancelButton.gridx = 2;
        gbc_cancelButton.gridy = 3;
        gbc_cancelButton.insets = new Insets(6, 3, 8, 7);
        add(cancelButton, gbc_cancelButton);
        /********************************************/
        // createFrame();
        pack();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }

    /*
     * void createFrame() { Dimension d = getToolkit().getScreenSize();
     * setLocation(d.width/4,d.height/3); }
     */

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == ok) {
            id = true;
            setVisible(false);
        } else if (ae.getSource() == can) {
            id = false;
            setVisible(false);
            System.exit(1);
        }
    }

    /**
     * Get the content of the Team name text field.
     * 
     * @return the name of the team as a JTextField.
     */
    public JTextField getTxtTeamName() {
        return txtTeamName;
    }

    /**
     * Get the content of the IP address text field.
     * 
     * @return the IP address of the server.
     */
    public JTextField getTxtServerIpAddress() {
        return txtServerIpField;
    }
}

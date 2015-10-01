package client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;

/** A simple Login JDialog to get login information from the client
 * user before a connection is made with the server.
 */
public class Login extends JDialog implements ActionListener{
	/** UID needed for JDialog */
	private static final long serialVersionUID = -747551867879253130L;
	/** Panel that contains all content of the dialog */
	private final JPanel contentPanel = new JPanel();
	/** The text field to get the IP address of the server from the user */
	private JTextField txtServerIpAddress;
	/** The text field to get the team name from the user */
	private JTextField txtTeamName;
	public JPasswordField password;
	boolean id = false;
	private JButton ok,can;
	/**
	 * Create the dialog.
	 * @param callback is the client
	 */
			 public Login(Frame clientApplet, final ClientApplet callback){
			  super(clientApplet, "Login", true);
			  setLayout(new GridLayout(4,3));
			  setResizable(false);
			  txtTeamName = new JTextField();
			  txtServerIpAddress=new JTextField(3+1+3+1+3);
			  password = new JPasswordField(15);
			  //password.setEchoChar('¤');
			  add(new JLabel("Host:", SwingConstants.CENTER));
			  add(txtServerIpAddress);
			  add(new JLabel("Team:", SwingConstants.CENTER));
			  add(txtTeamName);
			  add(new JLabel("Password:", SwingConstants.CENTER));
			  add(password);
			  addOKPanel(callback);
			  addCancelPanel();
			  createFrame();
			  pack();
				EventQueue.invokeLater(new Runnable() {
					public void run() {
							setVisible(true);
					}
				});
		  }

		 void addOKPanel(final ClientApplet callback){
			 JPanel p=new JPanel();
			 p.setLayout(new FlowLayout());
		 JButton okButton = new JButton("OK");
		 okButton.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent arg0) {
				 callback.dialogClose(Login.this, false);
			 }
		 });
		 okButton.setActionCommand("OK");
		 p.add(okButton);
		 JButton registerButton = new JButton("Register");
		 registerButton.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent arg0) {
				 callback.dialogClose(Login.this, true);
			 }
		 });
		 registerButton.setActionCommand("Register");
		 p.add(registerButton);
		 add(p);
		 getRootPane().setDefaultButton(okButton);
		 }
		 
		 void addCancelPanel(){
			 JPanel p=new JPanel();
			 p.setLayout(new FlowLayout());
			 p.add(can = new JButton("Cancel"));
			 can.addActionListener(this);
			 add(p);
		 }

		 void createFrame() {
		  Dimension d = getToolkit().getScreenSize();
		  setLocation(d.width/4,d.height/3);
		  }

		 public void actionPerformed(ActionEvent ae){
		  if(ae.getSource() == ok) {
		    id = true;
		    setVisible(false);
		    }
		  else if(ae.getSource() == can) {
		    id = false;
		    setVisible(false);
		    System.exit(1);
		    }
		  }

	/** Get the content of the Team name text field.
	 * @return the name of the team as a JTextField.
	 */
	public JTextField getTxtTeamName() {
		return txtTeamName;
	}
	/** Get the content of the IP address text field.
	 * @return the IP address of the server.
	 */
	public JTextField getTxtServerIpAddress() {
		return txtServerIpAddress;
	}
}

package server;

import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

public class Operator {
	private String teamName;
	private Action actionQueue;
	private Bank bank;
	private ClientMessages clientSocket;
	private CellKnowledge information;
	private Properties save;

	public Operator(String teamName, ClientMessages clientSocket) {
		this.teamName = teamName;
		this.clientSocket = clientSocket;
		
		/*Get a Properties object to store information in.
		 * Will load an already saved file based on teamName if exists.
		 */
		this.save = SavedInformation.getProperties(teamName);
		
		//if a file is loaded, load from saved
		if(save.getProperty("loaded").equals("true")) {
			loadFromSaved();
		//else, load new objects
		} else {
			loadNew();
		}
		
		//print property list to console for debugging
		printProperties();

	}

	
	/** Creates new empty objects for this Operator
	 */
	private void loadNew() {
		//initialize Bank
		this.bank = new Bank();
		
		//initialize CellKnowledge
		//this.information = new CellKnowledge();
		
		//initialize Action
		this.actionQueue = new Action();
		
		//debug
		System.out.println("loadNew");
	}
	
	/** Creates new objects and fills them with
	 * data from the saved file.
	 */
	private void loadFromSaved(){
		
		//debug
		System.out.println("loadSaved");
		printProperties();
	}
	
	/** Restore a client session when a team reconnects.
	 * Sends all necessary data back to the new client socket.
	 * @param clientSocket The new socket
	 */
	public void restore(ClientMessages clientSocket) {
		this.clientSocket = clientSocket;
		
		//re-send data here
	}
	

	/** Gets the team name.
	 * @return teamName The team name
	 */
	public String getName() {
		return teamName;
	}
	/** Gets the client socket to be used by the Director
	 * @return the ClientMessages for this Operator
	 */
	public ClientMessages getSocket(){
		return clientSocket;
	}
	
	public void newAction(){
		
	}
	
	public void newOperation(){
		
	}
	
	/** Store all the necessary information to the
	 *  save Properties object. Send to SavedInformation
	 *  to write to the file.
	 */
	public void storeInformation() {
		save.setProperty("name", teamName);
		save.setProperty("ip", clientSocket.getIP());
		
		SavedInformation.saveProperties(save);
	}
	
	//debug method
	public void printProperties() {
		save.list(System.out);
	}
}

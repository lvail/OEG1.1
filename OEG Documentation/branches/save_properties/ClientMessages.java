package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientMessages extends Thread {
	private Socket sock;
	private PrintWriter clientOut;
	private BufferedReader clientIn;
	private String IP;
	private String name;
	
	Director director;
	
	/** Constructor that stores the director
	 * and socket for this unique client.
	 * @param inDirector Director that incoming messages
	 * from the client will be sent to.
	 * @param sock Socket where messages are passed.
	 */
	public ClientMessages(Director inDirector, Socket sock) {
		this.sock = sock;
		this.director = inDirector;
		IP = sock.getInetAddress().getHostAddress();
		this.name = "none";
	}
	
	/** Method that is called when the thread is created.
	 * Initializes the input and output streams. Waits for
	 * incoming messages from the client.
	 */
	public void run() {
		try {
			//create reader in from client
			clientIn = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			
			//create writer out to client
			clientOut = new PrintWriter(
					sock.getOutputStream(), true);
			
			//notify client of successful connection
			clientOut.println("Connection Made with " + IP + ".");
			
			boolean done = false;
			//loop to take in input from client
			while(!done) {
				//wait for next input from client
				String str = clientIn.readLine();
				if(str == null) {
					done = true;
				}
				else {
					System.out.println(str); //display locally
					parse(str); //send to be parsed
				}
			}
			sock.close(); //close socket
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: " + e);
		}
	}
	
	/** Takes an input string that came from the client
	 * and decides what it means and forwards it to a
	 * method in director. 
	 * @param in String that needs to be decoded
	 */
	private void parse(String in) {
		String loginS = "login:";
		String chatS = "chat:";
		String saveS = "save:";
		
		//if the message from client is a login message
		if(in.startsWith(loginS)) {
			String teamName = in.substring(in.indexOf(loginS)+ (loginS.length()));
			director.newClient(this, teamName);
			this.name = teamName;
		}
		//message is a chat message
		if(in.startsWith(chatS)) {
			String chatText = in.substring(in.indexOf(chatS) + chatS.length());
			director.sendChat(chatS + name + ": " + chatText);
		}
		if(in.startsWith(saveS)) {
			director.saveGame();
		}
	}
	
	/**Takes a string that came from Director and 
	 * sends it directly out to the client
	 * @param out String to be sent to the clients
	 */
	public void sendData(String out) {
		clientOut.println(out);
	}
	
	/** Close the client connection. To be called when
	 * the client is no longer needed.
	 * @return Returns true if close was successful.
	 */
	public boolean close() {
		sendData("close:");
		try {
			clientIn.close();
			clientOut.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}  
	
	public String getIP() {
		return IP;
	}
}
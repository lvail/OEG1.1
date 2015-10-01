package server;

import java.net.*;

public class Server {

	public static Director mainDirector;
	public static InetAddress localIP;
	
	public static void main(String[] args) {
		
		mainDirector = new Director();
		Socket sock;
		
		try {
			//get IP address of local server
			localIP = InetAddress.getLocalHost();
			//IP address of current client
			InetAddress remoteIP;
			
			//start the server (open a port)
			ServerSocket s = new ServerSocket(8121);
			
			//print the IP address to the console
			System.out.println("Server IP address: " + localIP.getHostAddress());
			
			while(true) {
				//wait for a client connection
				sock = s.accept();
				//hold his IP
				remoteIP = sock.getInetAddress();
				
				//display new connection in console
				System.out.println("Connection Made with " +
						remoteIP.getHostAddress()+ ".");
				
				//create new thread to handle this client socket
				ClientMessages handler = new ClientMessages(mainDirector, sock);
				
				//start the thread
				handler.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: " + e);
		}
	}
	
	
}


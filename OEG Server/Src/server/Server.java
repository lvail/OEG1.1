package server;

import java.awt.EventQueue;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server statically starts the Director GUI, and then once its object is
 * created, it creates Director and waits for new, incoming socket connections.
 * Each client socket connection is wrapped in a ServerMessages object
 */
public class Server extends Thread {

    /** The main director of the OEG gameplay that manages all clients. */
    public static Director    mainDirector;
    /** The local IP network address of this OEG server. */
    public static InetAddress localIP;

    /**
     * main method creates the Manager GUI, which waits for user action to
     * create a Server object and start the game/server.
     * 
     * @param args
     *            String of command line arguments. Unused.
     */
    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            //@Override//not sure why this is hear possible removal later
            public void run() {
                try {
                    String ip = InetAddress.getLocalHost().getHostAddress();
                    new Manage(ip, mainDirector);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Server constructor simply initializes Director
     * 
     * @param window
     *            The Manage UI window object.
     * @param file
     *            The XML file selected by Manage and being opened by Director.
     */
    public Server(Manage window, File file) {
        mainDirector = new Director(window, file);
        window.setDirector(mainDirector);
    }

    /**
     * Starts the new thread to wait for and handle new client socket
     * connections.
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        waitConnections();
    }

    /**
     * Creates a ServerSocket connections. Waits for and handles new client
     * socket connections by wrapping them in a new thread and ServerMessages
     * object. mainDirector is given to this object so that it can add itself to
     * Director once login information is passed.
     */
    private void waitConnections() {
        Socket sock;
        try {
            // get IP address of local server
            localIP = InetAddress.getLocalHost();
            // IP address of current client
            InetAddress remoteIP;

            // start the server (open a port)
            ServerSocket oegServerSocket = new ServerSocket(8121);

            // print the IP address to the console
            System.out.println("Server IP address: " + oegServerSocket.getInetAddress());

            while (true) {
                // wait for a client connection and returns a socket
                sock = oegServerSocket.accept();
                // hold his IP
                remoteIP = sock.getInetAddress();

                // display new connection in console
                System.out.println("Connection Made with "
                        + remoteIP.getHostAddress() + ".");

                // create new thread to handle this client socket
                ServerMessages handler = new ServerMessages(mainDirector, sock);

                // start the thread
                handler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e);
            mainDirector.serverStop();
        }
    }
}

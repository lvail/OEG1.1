package shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import server.ServerMessages;
import server.ServerOperator;
import shared.action.Event;

/**
 * Handles all messages that are passed between Server and Client. Creates
 * wrappers to the input and output streams. Sends data (Strings) out, and
 * continuously waits for incoming data. Each Messages object runs on its own
 * Thread
 */
public class Messages extends Thread {
    // set if testing the program
    // TODO Switch this boolean value to use in production.
    @SuppressWarnings("javadoc")
    public final static boolean TESTMODE = false;

    /** The socket associated with this Messages object. */
    protected Socket         sock;
    /**
     * The output container to the socket, to which Strings are sent/written.
     */
    protected PrintWriter    sockOut;
    /** The input container to the socket, from which Strings are received. */
    protected BufferedReader sockIn;
    /**
     * The IP network address of the computer on the other side of the socket.
     */
    protected String         IP;
    /**
     * The operator that uses this socket for communication, and for which this
     * socket exists.
     */
    protected Operator       operator;
    /**
     * The name of the OEG team that is using this socket. Used to identify the
     * socket to humans.
     */
    protected String         name;
    /**
     * Probably completely unnecessary. This is a reference to the child of this
     * messages object, either ServerMessages or ClientMessages. I believe this
     * could be structured more properly and is unneeded.
     */
    private Messages         child;
    /**
     * Map of message Prefixes (or, the type of a message) from the String
     * representation to its Enumerated Prefix type.
     */
    Map<String, Prefix>      prefixMap;

    /**
     * An enumerated list of every type of message that is sent in OEG. Every
     * message that is passed through the socket must have a Prefix that
     * identifies it on the other side. Most Prefixes are only sent one way, but
     * there may be a couple that are sent both ways.
     */
    protected enum Prefix {
        LOGIN, SERVER, CLOSE, BID, STARTTIME, WONBID, BANKBALANCE, NEWROUND,
        TEST, LOSTBID, GRIDLIMITS, BIDLIMIT, SEISMICREQUEST, SEISMICDATA,
        SEISMICCOSTS, DRILLREQUEST, CELL, GAS, OIL, DRILLRESULT, REMOVEBID,
        REMOVESEISMIC, REMOVEDRILL, BIDQUEUE, SEISMICQUEUE, DRILLQUEUE,
        LANDOWNER, ROCK, DRILLCOST, ENDGAME, WARNING, REGISTER, ENDROUND
    }

    /**
     * Constructor initializes the socket that is used to send messages over the
     * network.
     * 
     * @param sock
     *            The socket that this object is wrapping.
     */
    public Messages(Socket sock) {
        initializeSocket(sock);
        createPrefixMap();
    }

    /**
     * Initialize the socket by wrapping the input and output streams in
     * BufferedReader and PrintWriter objects respectively
     * 
     * @param sock
     *            The socket being wrapped.
     */
    private void initializeSocket(Socket sock) {
        try {
            // setup input from server
            sockIn = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));

            // setup output to server
            sockOut = new PrintWriter(sock.getOutputStream(), true);
            IP = sock.getInetAddress().getHostAddress();

            this.name = "none";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a map from String to Prefix enumeration by looping through all
     * Prefix elements and using their name() method to get the string
     * representation of the Prefix.
     */
    private void createPrefixMap() {
        prefixMap = new HashMap<String, Prefix>();
        for (Prefix p : Prefix.values()) {
            prefixMap.put(p.name(), p);
        }
    }

    /**
     * Add a reference to the child object of this Messages. Probably
     * technically unnecessary and should be removed in the future, but helps to
     * view the relationship between Messages and ServerMessages/ClientMessages.
     * 
     * @param child
     *            The child of Messages. Either ServerMessages or ClientMessages
     */
    public void addChild(Messages child) {
        this.child = child;
    }

    /**
     * Method that is called when thread is created. Waits for messages from
     * server and sends them to be decoded. Continues indefinitely.
     */
    @Override
    public void run() {
        String str;
        boolean more = true;
        try {
            while (more) {
                // wait for next line from server
                str = sockIn.readLine();
                if (str != null) {
                    // parse server message
                    parse(str);
                } else
                    more = false;
            }
            close();
        } catch (IOException ioe) {
            // if the connection has been closed, handle it properly.
            if (ioe.getMessage().equals("Connection reset")) {
                // print message to console
                System.out.println(
                        "Client " + name + " at " + IP + " disconnected.");

                if (operator instanceof ServerOperator) {
                    // create a logout event for logging
                    Event logout = new Event(operator);
                    logout.setEventLogout();
                    ((ServerOperator) operator).addHistory(logout);
                } else {
                    // close the socket cleanly, if possible.
                    // java.lang.ClassCastException: server.ServerMessages
                    // cannot be cast to client.ClientMessages
                    // at shared.Messages.run(Messages.java:137)
                    child.close();
                }
                // if some other exception, just dump the stack
            } else {
                ioe.printStackTrace();
                System.out.println("Exception: " + ioe);
            }
        }
        // close the socket cleanly, if possible.
        if (child instanceof ClientMessages) {
            ((ClientMessages) child).close();
        }
    }

    /**
     * Decode the incoming socket message. Gets the prefix from the string, and
     * makes sure it exists in prefixMap. Sends the prefix and its message to
     * the appropriate child (ServerMessages or ClientMessages).
     * 
     * @param in
     *            The incoming string with both Prefix and message. Should be in
     *            form -- "PREFIX:Message string"
     */
    private void parse(String in) {
        // print the message to the console for viewing
        if (operator != null)
            System.out.println("IN: " + operator.getName() + ": " + in);
        else
            System.out.println("IN: " + in);

        // find the location of the colon:
        int colon = in.indexOf(':');
        // if the colon exists
        if (colon != -1) {
            // extract the prefix from the socket string
            String prefix = in.substring(0, colon).toUpperCase();

            // check that the prefix is valid
            if (prefixMap.containsKey(prefix)) {
                // extract the message from the socket string
                String message = in.substring(in.indexOf(':') + 1, in.length());

                // Send to the parse(String, Prefix) method for processing by a
                // child.
                // Note -- This method of distinguishing server/client works,
                // but may
                // not be the most proper way to do things.
                if (child instanceof ServerMessages)
                    ((ServerMessages) child).parse(message,
                            prefixMap.get(prefix));
                else
                    ((ClientMessages) child).parse(message,
                            prefixMap.get(prefix));

                // if the prefix is not valid
            } else {
                System.out.println("Unknown message: " + in);
            }
            // if no colon exists
        } else {
            if (in.length() > 1)
                System.out.println("Messages Unknown message: " + in);
        }
    }

    /**
     * Parse abstract method to be extended by subclasses. Called by
     * parse(String), after it determines what type of message the string is.
     * 
     * @param in
     *            The string coming in from the socket to be parsed.
     * @param pr
     *            The type of message being handled.
     */
    public void parse(String in, Prefix pr) {
    };

    /**
     * Send a message across the socket using the Prefix of the message and the
     * message contents. Format -- "PREFIX:Message string".
     * 
     * @param p
     *            The Prefix enumerated type of the message being sent.
     * @param message
     *            The contents of the message being sent.
     */
    public void sendMessage(Prefix p, String message) {
        System.out.println("OUT: " + p.name() + ":" + message);
        if (sockOut != null) {
            sockOut.println(p.name() + ":" + message);
            sockOut.flush();
        } else
            System.out.println("sockOut is NULL for " + operator.getName());
    }

    /**
     * Close the client connection. To be called when the client is no longer
     * needed.
     * 
     * @return Returns true if close was successful.
     */
    public boolean close() {
        /*
         * notify the other side of the socket that the connection is being
         * closed
         */
        sendMessage(Prefix.CLOSE, "");
        try {
            /*
             * if socket still exists, close the read, write wrappers and the
             * socket itself.
             */
            if (sock != null) {
                sock.shutdownInput();
                sock.shutdownOutput();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Set the operator that uses and is associated with this connection.
     * 
     * @param operator
     *            The operator object for this socket.
     */
    public void setOperator(Operator operator) {
        this.operator = operator;
    }
}

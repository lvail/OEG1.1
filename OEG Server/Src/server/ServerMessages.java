package server;

import java.net.Socket;

import shared.Messages;

/**
 * Handles all socket messages sent and received to a single client from the
 * server. This includes receiving messages and passing them to the correct
 * method in Director, and providing an interface to send specific messages out.
 * There is one thread/object for each client that the server manages.
 */
public class ServerMessages extends Messages {

    /** Director that will handle messages received from clients */
    Director director;

    /**
     * Constructor that stores the director and socket for this unique client.
     * 
     * @param inDirector
     *            Director that incoming messages from the client will be sent
     *            to.
     * @param sock
     *            Socket where messages are passed.
     */
    public ServerMessages(Director inDirector, Socket sock) {
        super(sock);
        super.addChild(this);
        this.director = inDirector;

        // notify client of successful connection
        sockOut.println("server:Connection Made with " + IP + ".");
    }

    /**
     * Takes an input string that came from the client and decides what it means
     * and forwards it to a method in director.
     * 
     * @param in
     *            The message to be passed.
     * @param pr
     *            The prefix identifying the message.
     */
    @Override
    public void parse(String in, Prefix pr) {
        // switch of all possible incoming messages, using the Prefix
        // enumeration in Messages.
        switch (pr) {

            case ENDROUND:
                director.endRound();
                break;
            case LOGIN:
            case REGISTER:
                // login credentials from the client. The client can now be
                // added to Director.
                String[] parts = in.split(",");
                director.newClient(this, pr == Prefix.REGISTER, parts[0],
                        parts[1]);
                this.name = parts[0];
                break;
            case TEST:
                // test signals end of round during development
                if (Messages.TESTMODE) {
                    director.endRound();
                }
                break;
            case BID:
                ((ServerOperator) operator).newBid(in);
                director.manage.refreshInfo();
                break;
            case SEISMICREQUEST:
                ((ServerOperator) operator).newSeismicRequest(in);
                director.manage.refreshInfo();
                break;
            case DRILLREQUEST:
                ((ServerOperator) operator).newDrillRequest(in);
                director.manage.refreshInfo();
                break;
            case REMOVEBID:
                ((ServerOperator) operator).removeBid(in);
                director.manage.refreshInfo();
                break;
            case REMOVESEISMIC:
                ((ServerOperator) operator).removeSeismicRequest(in);
                director.manage.refreshInfo();
                break;
            case REMOVEDRILL:
                ((ServerOperator) operator).removeDrillRequest(in);
                director.manage.refreshInfo();
                break;
            // TODO Don't think needed. If any problems with Gas or Oil,
            // uncomment.
            /*
             * case OIL: ((ServerOperator) operator).newDrillRequest(in); break;
             * case GAS: ((ServerOperator) operator).newDrillRequest(in); break;
             */
            default:
                System.out.println("Unknown message: " + in);
        }
    }

    /**
     * Signal to the operator that they won bid
     * 
     * @param bid
     *            is the Bid of the winner
     */
    public void wonBid(String bid) {
        sendMessage(Prefix.WONBID, bid);
    }

    /**
     * Signal to the operator that they lost bid
     * 
     * @param bid
     *            is the Bid of the loser
     */
    public void lostBid(String bid) {
        sendMessage(Prefix.LOSTBID, bid);
    }

    /**
     * Send the low bid limit to the client.
     * 
     * @param amount
     *            Amount in dollars of the lowest bid possible.
     */
    public void sendBidLimit(int amount) {
        sendMessage(Prefix.BIDLIMIT, "" + amount);
    }

    /**
     * Send the current timer information to the client. Format --
     * "mRemaining#mRoundLength#roundNumber".
     * 
     * @param milliseconds
     *            Time remaining in the current countdown.
     * @param roundLength
     *            Length of the countdown/each round.
     * @param roundNum
     *            The current round number.
     */
    public void startTimer(long milliseconds, long roundLength, int roundNum,
            int numRounds) {
        sendMessage(Prefix.STARTTIME, milliseconds + "#" + roundLength + "#"
                + roundNum + "#" + numRounds);
    }

    /**
     * Signal a new round start, and send the current bank balance to the
     * client.
     * 
     * @param balance
     *            The amount in $ of the operator's current bank balance.
     */
    public void sendNewRound(int balance) {
        sendMessage(Prefix.NEWROUND, "" + balance);
    }

    /**
     * Signal the end of the current round(For Manage)
     * 
     * @param message
     *            Mesage to be sent
     */
    public void endRound(String message) {
        sendMessage(Prefix.ENDROUND, message);
    }

    /**
     * Send the current bank balance to the client.
     * 
     * @param amount
     *            The amount in $ of the operator's current bank balance.
     */
    public void sendBankBalance(int amount) {
        sendMessage(Prefix.BANKBALANCE, amount + "");
    }

    /**
     * Send a generic server message ("I.E. Welcome messages").
     * 
     * @param message
     *            Message to be sent.
     */
    public void sendInfo(String message) {
        sendMessage(Prefix.SERVER, message);
    }

    /**
     * Send a warning messages.
     * 
     * @param warning
     *            Message to be sent.
     */
    public void sendWarning(String message) {
        sendMessage(Prefix.WARNING, message);
    }

    /**
     * Send the x and y and depth limits of the grid.
     * 
     * @param xLimit
     *            Number of cells horizontally.
     * @param yLimit
     *            Number of cells vertically.
     * @param numLayers
     *            Depth of cells.
     */
    public void sendGridLimits(int xLimit, int yLimit, int numLayers,
            int[] layers) {
        String msg = xLimit + "#" + yLimit + "#" + numLayers + "#";
        for (int i = 0; i < layers.length; i++)
            msg += layers[i] + "#";
        sendMessage(Prefix.GRIDLIMITS, msg);

    }

    /**
     * Send cost of seismic line requests.
     * 
     * @param in
     *            String representing cost.
     */
    public void sendSeismicCosts(String in) {
        sendMessage(Prefix.SEISMICCOSTS, in);
    }

    public void sendDrillCost(int drillCost) {
        sendMessage(Prefix.DRILLCOST, drillCost + "");

    }

    /**
     * Send string of rocktype data.
     * 
     * @param layers
     *            String representing rocktype array.
     */
    public void sendLayer(String layers) {
        sendMessage(Prefix.SEISMICDATA, layers);
    }

    /**
     * Send string of gas data.
     * 
     * @param gas
     *            String representing gas array.
     */
    public void sendGas(String gas) {
        sendMessage(Prefix.GAS, gas);
    }

    /**
     * Send string of oil data.
     * 
     * @param oil
     *            String representing oil array.
     */
    public void sendOil(String oil) {
        sendMessage(Prefix.OIL, oil);
    }

    public void sendRock(String rockSocket) {
        sendMessage(Prefix.ROCK, rockSocket);
    }

    /**
     * Send the contents of an entire cell
     * 
     * @param c
     *            String representing the entire cell
     */
    public void sendCell(String c) {
        sendMessage(Prefix.CELL, c);
    }

    /**
     * Send the results of a drill request.
     * 
     * @param drillSocket
     *            String representing the drilling result.
     */
    public void sendDrill(String drillSocket) {
        sendMessage(Prefix.DRILLRESULT, drillSocket);

    }

    /**
     * Send the current bid queue for when clients reconnect.
     * 
     * @param socket
     *            The bid queue as a string.
     */
    public void sendBidQueue(String socket) {
        sendMessage(Prefix.BIDQUEUE, socket);
    }

    /**
     * Send the current seismic queue for when clients reconnect.
     * 
     * @param socket
     *            The seismic queue as a string.
     */
    public void sendSeismicQueue(String socket) {
        sendMessage(Prefix.SEISMICQUEUE, socket);
    }

    /**
     * Send the current drill queue for when clients reconnect.
     * 
     * @param socket
     *            The drill queue as a string.
     */
    public void sendDrillQueue(String socket) {
        sendMessage(Prefix.DRILLQUEUE, socket);

    }

    public void sendOwner(String socket) {
        sendMessage(Prefix.LANDOWNER, socket);

    }

    public void sendEndGame(String socket) {
        sendMessage(Prefix.ENDGAME, socket);
    }

}
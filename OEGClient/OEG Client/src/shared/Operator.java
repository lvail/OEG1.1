package shared;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;

import shared.action.Action;
import shared.action.Bid;
import shared.action.Drill;
import shared.action.SeismicRequest;

/**
 * Represents a player in the OEG game. Handles the team name, its socket
 * connection, a grid of information the team knows, and actions that each team
 * needs to take throughout the game.
 */
public class Operator {
    /**
     * The instance of Grid containing only information that this Operator is
     * aware of.
     */
    protected Grid opGrid;
    /** The name of the Operator user */
    protected String teamName;
    /**
     * The current queue of bids the operator is planning for this round
     */
    protected LinkedList<Bid> bidQueue;
    /**
     * The current queue of seismic requests the operator is planning for this
     * round
     */
    protected LinkedList<SeismicRequest> seismicQueue;
    /**
     * The current queue of drill requests the operator is planning for this
     * round
     */
    protected LinkedList<Drill> drillQueue;

    /**
     * The bank account holding balance information for this Operator
     */
    protected BankAccount bank;
    /**
     * The socket connection to or from the server for this Operator
     */
    public Messages socket;

    /**
     * Creates a new Operator given a team name and a socket.
     * 
     * @param teamNameIn
     *            is the name of the team.
     * @param socketIn
     *            is the socket that messages will be passed to.
     */
    public Operator(String teamNameIn, Messages socketIn) {
        teamName = teamNameIn;
        socket = socketIn;
        // Give the pre-existing socket a
        // reference to this Operator
        socket.setOperator(this);

        bank = new BankAccount();

        // initialize Action queues
        bidQueue = new LinkedList<Bid>();
        seismicQueue = new LinkedList<SeismicRequest>();
        drillQueue = new LinkedList<Drill>();
    }

    /**
     * Creates a new, more basic Operator given only a team name. Use of this
     * constructor does not allow action queues or socket information.
     * 
     * @param teamName
     *            Name of the operator.
     */
    public Operator(String teamName) {
        this.teamName = teamName;
        this.bank = new BankAccount();
    }

    /**
     * Gets the team name.
     * 
     * @return teamName The name of the Operator.
     */
    public String getName() {
        return teamName;
    }

    // /** Gets the operator's socket to be
    // used by the Director
    // * @return the Messages object for this
    // Operator */
    // public Messages getSocket() {
    // return socket;
    // }

    /**
     * Change the socket used by Operator to a new socket connection.
     * 
     * @param s
     *            is the Messages socket wrapper for this Operator to use.
     */
    public void setSocket(Messages s) {
        this.socket = s;
        s.setOperator(this);
    }

    /** Closes this Operator's current socket. */
    public void closeSocket() {
        if (socket != null) {
            socket.close();
            socket.setOperator(null);
            socket = null;
        }
    }

    /**
     * Creates a new bid for this Operator from the input passed along the
     * socket.
     * 
     * @param socketIn
     *            is the Bid passed over the socket.
     */
    public void newBid(String socketIn) {
        bidQueue.add(new Bid(this, socketIn));
    }

    /**
     * Creates a new Seismic Line Request for this Operator from the input
     * passed along the socket
     * 
     * @param socketIn
     *            is the Seismic passed over the socket.
     */
    public void newSeismicRequest(String socketIn) {
        seismicQueue.add(new SeismicRequest(this, socketIn));
    }

    /**
     * Creates a new Drill Request for this Operator from the input passed along
     * the Socket.
     * 
     * @param socketIn
     *            is the Drill passed over the socket.
     */
    public void newDrillRequest(String socketIn) {
        drillQueue.add(new Drill(this, socketIn));
    }

    /**
     * Removes a bid for this Operator which has data equivalent to the data
     * passed along the socket.
     * 
     * @param socketIn
     *            is the Bid passed over the socket.
     */
    public void removeBid(String socketIn) {
        // create the Bid from the socket
        // information
        Bid remove = new Bid(this, socketIn);
        removeFromQueue(remove, bidQueue);
    }

    /**
     * Removes a Seismic Line Request for this Operator which has data
     * equivalent to the data passed along the socket.
     * 
     * @param socketIn
     *            is the Seismic passed over the socket.
     */
    public void removeSeismicRequest(String socketIn) {
        SeismicRequest remove = new SeismicRequest(this, socketIn);
        removeFromQueue(remove, seismicQueue);
    }

    /**
     * Removes a Drill Request for this Operator which has data equivalent to
     * the data passed along the socket.
     * 
     * @param socketIn
     *            is the Drill passed over the socket.
     */
    public void removeDrillRequest(String socketIn) {
        Drill remove = new Drill(this, socketIn);
        removeFromQueue(remove, drillQueue);
    }

    /**
     * Remove an action from an Action queue by looping through the elements of
     * the queue and comparing using equals() method for Action. Let it be clear
     * that Action objects are never used, rather subclasses of Action that
     * override equals().
     * 
     * @param remove
     *            The Action to be removed from a queue.
     * @param queue
     *            The queue from which the action is to be removed.
     */
    public void removeFromQueue(Action remove,
                    LinkedList<? extends Action> queue) {
        // iterate through the queue
        Iterator<? extends Action> i = queue.iterator();
        // for every element in the queue
        while (i.hasNext()) {
            // if the element is equal to the
            // bid received from the socket
            if (i.next().equals(remove)) {
                // remove it
                i.remove();
                // print a log message
                System.out.println("REMOVE ACTION: " + remove.toSocket());
            }
        }
    }

    /**
     * Returns a linked list of this Operator's current bids.
     * 
     * @return Linked list of current bids.
     */
    public LinkedList<Bid> getBidQueue() {
        return bidQueue;
    }

    /**
     * Returns a linked list of this Operator's current seismic line requests.
     * 
     * @return Linked list of current seismic line requests.
     */
    public LinkedList<SeismicRequest> getSeismicQueue() {
        return seismicQueue;
    }

    /**
     * Returns a linked list of this Operator's current drill requests.
     * 
     * @return Linked list of current drill requests.
     */
    public LinkedList<Drill> getDrillQueue() {
        return drillQueue;
    }

    /**
     * Resets this Operator's queue of current bids.
     */
    public void resetBidQueue() {
        bidQueue.clear();
    }

    /**
     * Resets this Operator's queue of current seismic line requests.
     */
    public void resetSeismicQueue() {
        seismicQueue.clear();
    }

    /**
     * Resets this Operator's queue of current drill requests.
     */
    public void resetDrillQueue() {
        drillQueue.clear();
    }

    /**
     * Returns this Operator's bank.
     * 
     * @return the Operator's Bank.
     */
    public BankAccount getBank() {
        return bank;
    }

    /**
     * Returns this Operator's bank balance.
     * 
     * @return How much money is in the bank.
     */
    public int getBankBalance() {
        return this.bank.getBalance();
    }

    /**
     * Set the bank balance for this Operator. This also resets the unusable
     * amount
     * 
     * @param amount
     *            The new bank balance for this Operator.
     */
    public void setBankBalance(int amount) {
        this.bank.setBalance(amount);
        this.bank.resetUnusable();
    }

    /**
     * Update the bank balance for this Operator by x amount.
     * 
     * @param amount
     *            The amount, + or -, for the bank balance to change.
     */
    public void updateBankBalance(int amount) {
        bank.updateFunds(amount);
    }

    /**
     * Gather the result of an auction for a particular bid.
     * 
     * @param bid
     *            The winning bid of the auction.
     */
    public void bidResult(Bid bid) {
        opGrid.setOwner(bid.getOwner().getName(), bid.getPoint());
    }

    // used by client ClientMessages when
    // receiving seismic request
    /**
     * Set the result of a seismic request using the string representation of
     * the data for a particular cell.
     * 
     * @param in
     *            The string containing layer data for a particular point.
     */
    public void seismicRequestResult(String in) {
        opGrid.setLayersSocket(in);
    }

    /**
     * Set oil information received in a socket message.
     * 
     * @param in
     *            The oil info for a particular cell as a string.
     */
    public void oilInfo(String in) {
        opGrid.setOilSocket(in);
    }

    /**
     * Set gas information received in a socket message.
     * 
     * @param in
     *            The gas info for a particular cell as a string.
     */
    public void gasInfo(String in) {
        opGrid.setGasSocket(in);
    }

    public void rockInfo(String in) {
        opGrid.setRockSocket(in);
    }

    /**
     * Set the result of a drill request, including the gas and oil rates for
     * the cell
     * 
     * @param p
     *            The point where the drilling took place.
     * @param gas
     *            The int array of gas rate values at different layers at p.
     * @param oil
     *            The int array of oil rate values at different layers at p.
     */
    public void drillRequestResult(Point p, Integer[] gas, Integer[] oil,
                    LithologicType[] rock) {
        boolean in = true;
        opGrid.setDrilled(p, in);
        opGrid.setGas(p, gas);
        opGrid.setOil(p, oil);
        opGrid.setRock(p, rock);
    }

    /**
     * Set the result of a drill request, not including the gas and oil rates.
     * 
     * @param in
     *            A string representation of the successful drill.
     */
    public void drillRequestResult(String in) {
        Drill drill = new Drill(this, in);
        opGrid.setDrilled(drill.getPoint(), true);
    }

    /**
     * Get the grid of information known to this operator.
     * 
     * @return A Grid object that is a subset of the Director's grid, containing
     *         only information that this Operator knows.
     */
    public Grid getGrid() {
        return opGrid;
    }

    @Override
    public String toString() {
        return teamName;
    }
}

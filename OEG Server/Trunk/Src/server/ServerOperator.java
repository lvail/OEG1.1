package server;

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.LinkedList;

import shared.Grid;
import shared.LithologicType;
import shared.Operator;
import shared.action.Action;
import shared.action.Bid;
import shared.action.Drill;
import shared.action.Event;
import shared.action.SeismicRequest;

/**
 * Child of Operator that adds additional tasks which are only needed by
 * Director to manage the game, including sending messages to ClientOperator.
 */
public class ServerOperator extends Operator {

    /** A log of all past actions this Operator has taken */
    protected LinkedList<Action> history;
    public String                password;

    /**
     * Create a new ServerOperator, and initialize the history and opGrid.
     * 
     * @param d
     *            The director that is creating this object.
     * @param name
     *            The name of the Operator.
     * @param client
     *            The socket that is used by the Operator.
     */
    public ServerOperator(Director d, String name, String password,
            ServerMessages client) {
        super(name, client);
        this.password = password;
        history = new LinkedList<Action>();
        client.sendBankBalance(bank.getStart());
        opGrid = new Grid(d.getGrid().getDimensions(),
                d.getGrid().getNumLayers());
    }

    /**
     * Updates the Server Side bank with the winning bid cost. Notifys the
     * client that they won the bid. Bid is placed in the opGrid elsewhere
     * through setOwnership(Bid).
     * 
     * @param won
     *            is the winning bid
     */
    public void wonBid(Bid won) {
        ((ServerMessages) socket).wonBid(won.toSocket());
        bank.updateFunds(0 - won.getCost());
    }

    /**
     * Notify the client that they lost the bid. Bid is placed in the opGrid
     * elsewhere through setOwnership(Bid).
     * 
     * @param won
     *            The bid that won the auction.
     */
    public void lostBid(Bid won) {
        ((ServerMessages) socket).lostBid(won.toSocket());
    }

    /**
     * Send a generic message from the server to the client.
     * 
     * @param message
     *            The message to be sent.
     */
    public void sendInfo(String message) {
        ((ServerMessages) socket).sendInfo(message);
    }

    /**
     * Called when an Operator reconnects. Sends all data associated with this
     * operator to the client counterpart.
     */
    public void sendExistingData() {
        ((ServerMessages) socket).sendBankBalance(getBankBalance());
        sendGrid();
        sendQueues();
    }

    /** Send the entire contents of opGrid to the client */
    public void sendGrid() {
        String[] cells = opGrid.getGridSocket();
        for (String c : cells) {
            if (c != null) {
                ((ServerMessages) socket).sendCell(c);
            }
        }
    }

    /** Send the contents of the current action queues to the client */
    public void sendQueues() {
        LinkedList<Bid> bidQueue = getBidQueue();
        LinkedList<SeismicRequest> seismicQueue = getSeismicQueue();
        LinkedList<Drill> drillQueue = getDrillQueue();

        if (!bidQueue.isEmpty()) {
            for (Bid bid : bidQueue) {
                ((ServerMessages) socket).sendBidQueue(bid.toSocket());
            }
        }
        if (!seismicQueue.isEmpty()) {
            for (SeismicRequest sm : seismicQueue) {
                ((ServerMessages) socket).sendSeismicQueue(sm.toSocket());
            }
        }
        if (!drillQueue.isEmpty()) {
            for (Drill drill : drillQueue) {
                ((ServerMessages) socket).sendDrillQueue(drill.toSocket());
            }
        }

    }

    /**
     * Send seismic information at a particular cell to the client. Seismic data
     * is fudged.
     * 
     * @param p
     *            The point where seismic request was made.
     * @param layer
     *            The layer information as an Integer array.
     */
    public void sendSeismicLayer(Point p, Integer[] layer) {

        layer = Grid.fudge(layer);
        opGrid.setLayers(p, layer);
        opGrid.setSeismicExists(p, true);
        ((ServerMessages) socket).sendLayer(opGrid.getLayersSocket(p));
    }

    /**
     * Send drill information at a particular cell to the client. This includes
     * gas and oil rates that are being returned by the results of the drilling.
     * 
     * @param p
     *            The point where the drill occurred.
     * @param drillSocket
     *            The drill request formatted as a socket string.
     * @param gas
     *            The gas results as an array.
     * @param oil
     *            The oil results as an array.
     * @param gasSocket
     *            The gas results as a socket string.
     * @param oilSocket
     *            The oil results as a socket string.
     * @param rockSocket
     *            The rock type results as a socket string.
     * @param rock
     *            The rock type result as an array.
     */
    public void sendDrillStuffs(Point p, String drillSocket, Integer[] gas,
            Integer[] oil, String gasSocket, String oilSocket,
            String rockSocket, LithologicType[] rock) {
        ((ServerMessages) socket).sendDrill(drillSocket);
        ((ServerMessages) socket).sendGas(gasSocket);
        ((ServerMessages) socket).sendOil(oilSocket);
        ((ServerMessages) socket).sendRock(rockSocket);
        drillRequestResult(p, gas, oil, rock);
    }

    public void sendDrillStuffs(String drillSocket, String cellToSocket) {
        opGrid.cellFromSocket(cellToSocket);
        ((ServerMessages) socket).sendDrill(drillSocket);
        ((ServerMessages) socket).sendCell(cellToSocket);

    }

    public void sendEndGame(ServerOperator winner) {
        DecimalFormat myFormatter = new DecimalFormat("$###,###,###");
        String output = myFormatter.format(winner.getBankBalance());
        String outmessage = winner.teamName + " is the winner with " + output
                + " in their bank.";
        ((ServerMessages) socket).sendEndGame(outmessage);
    }

    /**
     * Add an item to the history log.
     * 
     * @param s
     *            The Action to be added to the log.
     */
    public void addHistory(Action s) {
        history.add(s);
    }

    /**
     * Get the history log of all actions this operator has performed
     * 
     * @return A LinkedList of Actions.
     */
    public LinkedList<? extends Action> getHistory() {
        return history;
    }

    /**
     * Send end of round information to the client, including the new round
     * number and current bankAccount information after action queues were
     * processed. Also resets the action queues.
     * 
     * @param timeRemaining
     *            Current time in the new countdown timer.
     * @param roundLength
     *            The length of a round.
     * @param curRound
     *            The current round number.
     */
    public void endRound(long timeRemaining, long roundLength, int curRound,
            int numRounds) {
        Event endRound;

        bank.applyIncome();

        // update the client's bank balance with current balance.
        ((ServerMessages) socket).sendNewRound(getBankBalance());
        // update the client's round and timer to the current time and round.
        ((ServerMessages) socket).startTimer(timeRemaining, roundLength,
                curRound, numRounds);

        // create end of round event and store in history log
        endRound = new Event(this);
        endRound.setEventNewRound(curRound);
        addHistory(endRound);

        // reset action queues
        resetBidQueue();
        resetSeismicQueue();
        resetDrillQueue();
    }

    /**
     * Set the ownership of a piece of land according to the winning bid that is
     * passed.
     * 
     * @param w
     *            The winning bid that contains the new location and owner of
     *            the piece of land.
     */
    public void setOwnership(Bid w) {
        super.bidResult(w);
        ((ServerMessages) socket).sendOwner(w.toSocket());
    }

    /**
     * Add to the income of this operator according to the gas and oil rates
     * that are passed in
     * 
     * @param gasRate
     *            The gas rate in num barrels per day.
     * @param oilRate
     *            The oil rate in
     */
    public void addIncome(int gasRate, int oilRate) {
        // rate of production for a piece of land * income in $ per barrel * #
        // of days per round
        bank.adjustIncome(gasRate * Action.gasRate * Director.timeStep);
        bank.adjustIncome(oilRate * Action.oilRate * Director.timeStep);
    }

    /**
     * Change the Team name
     * 
     * @param newname
     *            The String reprenting the team's name
     */
    public void setName(String newName) {
        super.teamName = newName;
    }

    /**
     * Change the Team's Balance
     * 
     * @param newname
     *            The String reprenting the team's name
     */
    public void setBalance(int newBalance) {
        bank.setBalance(newBalance);
    }

    public int getBalance() {
        return bank.getBalance();
    }

    public void sendWarning(String msg) {
        ((ServerMessages) socket).sendWarning(msg);
    }

    @Override
    public String toString() {
        return super.toString();
    }

}

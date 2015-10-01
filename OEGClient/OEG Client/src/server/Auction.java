package server;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Vector;

import shared.Grid;
import shared.action.Bid;

/**
 * Class to perform "Auctions", that will process all the bids each operator has
 * made during a round and decide the winning bid for each piece of land (or
 * cell) that a bid was placed on.
 */
public class Auction { 

    /**
     * Array of bid queues (LinkedList implements Queue) for the current round,
     * one for each operator
     */
    LinkedList<Bid>[] bids;

    /** The grid upon which bids have been placed. */
    private Grid grid;

    /** Collection of priority queues, one for each piece of land */
    private Vector<PriorityQueue<Bid>> priorityList;

    /**
     * Constructor to create and process a new "auction". The auction covers all
     * bids on all cells of land that have been bid on.
     * 
     * @param bids
     *            An array of queues of bids to be processed. Each array element
     *            is a LinkedList of bids for a particular cell on the Grid.
     * @param grid
     *            The grid from which cells are auctioned off.
     */
    public Auction(LinkedList<Bid>[] bids, Grid grid) {

        this.bids = bids;

        this.grid = grid;

        priorityList = new Vector<PriorityQueue<Bid>>();

        // simulate the auction
        auction();

        // gather and distribute the results
        // getResults();
    }

    /**
     * Perform the auction by pulling every bid from each Operator's queue and
     * placing it in the correct Priority Queue
     */
    public void auction() {
        ListIterator<Bid> bidQueue;
        PriorityQueue<Bid> priority = null;

        Bid currentBid;
        // for each Operator's queue of bids
        for (int i = 0; i < bids.length; i++) {
            if (bids[i] == null) continue;
            bidQueue = bids[i].listIterator();

            // for every bid in the list
            while (bidQueue.hasNext()) {
                // get the next bid
                currentBid = bidQueue.next();
                // check if it is already owned
                if (!grid.isOwned(currentBid.getPoint())) {
                    // get the priority queue for that
                    // piece of land
                    priority = getQueue(currentBid);
                    // add the bid to the priority queue
                    // for that piece of land
                    priority.add(currentBid);
                }
            }
        }
    }

    /**
     * Returns a priority queue for the piece of land that bid is located on. If
     * none exists yet, create and add a new one.
     * 
     * @param bid
     *            The bid, the comparator of which compares x,y location.
     * @return an existing or new PriorityQueue for the bid's cell
     */
    private PriorityQueue<Bid> getQueue(Bid bid) {
        // iterator to loop through the list of priority queues
        ListIterator<PriorityQueue<Bid>> iterator = priorityList.listIterator();
        // to hold the current possible priority queue
        PriorityQueue<Bid> existing;

        // for every priority queue that exists currently
        while (iterator.hasNext()) {
            // get the queue
            existing = iterator.next();
            // if the top of the queue is on the same
            // piece of land as bid, it is the desired queue
            if (existing.peek().equalsLand(bid)) {
                return existing;
            }
        }
        // if none are found, create and add a new PriorityQueue to priorityList
        PriorityQueue<Bid> newPriority;
        newPriority = new PriorityQueue<Bid>();
        priorityList.add(newPriority);
        return newPriority;
    }

    /**
     * Process the the priorityList that resulted from auction. Notify the
     * winning bidder of each piece of land that he won, and send the winning
     * bid back to all losers.
     * 
     * @return An array of winning Bids.
     */
    public LinkedList<Bid> getResults() {
        ListIterator<PriorityQueue<Bid>> iterator = priorityList.listIterator();
        LinkedList<Bid> winningBids = new LinkedList<Bid>();
        Bid winner;
        Bid lost;

        // for every land priority queue that exists
        while (iterator.hasNext()) {
            // get the priority queue
            PriorityQueue<Bid> curPrior = iterator.next();
            // get the winner
            winner = curPrior.remove();
            winningBids.push(winner);
            // process the losers
            while (!curPrior.isEmpty()) {
                // get a loser
                lost = curPrior.remove();
                // send the winner to the loser
                ((ServerOperator) lost.getOwner()).lostBid(winner);
                // add the losing bid to the loser's history
                ((ServerOperator) lost.getOwner()).addHistory(lost);
            }
            // tell winner that he won with his winning bid
            winner.setWon();
            ((ServerOperator) winner.getOwner()).wonBid(winner);
            // add the winning bid to the winner's history
            ((ServerOperator) winner.getOwner()).addHistory(winner);

            // in our grid, we set the owner of the won cell
            grid.setOwner(winner.getOwner().getName(), winner.getPoint());
        }
        return winningBids;
    }
}

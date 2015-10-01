package shared.action;

import java.awt.Point;
import java.text.DecimalFormat;

import shared.Operator;

public class Bid extends Action implements Comparable<Bid> {
    /**
     * The amount of the bid.
     */
    private int   bidAmount;
    /**
     * The Point coordinate of the bid.
     */
    private Point landP;

    public static String noOwner = "Not Owned.";

    public boolean won = false;

    /**
     * Constructor for the Bid class.
     * 
     * @param owner
     *            The Operator who submitted the bid.
     * @param bidAmount
     *            The amount of the bid.
     * @param land
     *            The location of the bid.
     */
    public Bid(Operator owner, int bidAmount, Point land) {
        super(owner);
        this.bidAmount = bidAmount;
        this.landP = land;
        this.cost = this.bidAmount;
    }

    /**
     * Constructor that forms a bid based on a socket message
     * 
     * @param operator
     *            Owner of the bid.
     * @param socketIn
     *            Socket message received that contains Bid values.
     */
    public Bid(Operator operator, String socketIn) {
        super(operator);
        String[] split = socketIn.split("#");
        this.owner = operator;
        parseSocket(split);

    }

    /**
     * Constructor that forms a bid fully from a socket message.
     * 
     * @param socketIn
     *            Socket message received that contains all Bid data.
     */
    public Bid(String socketIn) {
        super();
        String[] split = socketIn.split("#");
        setOwner(new Operator(split[1]));
        parseSocket(split);
    }

    /**
     * Sets Bid values from a String array.
     * 
     * @param socket
     *            The String array containing the data for the Bid.
     */
    private void parseSocket(String[] socket) {
        this.bidAmount = Integer.parseInt(socket[3]);
        this.landP = new Point();
        this.landP.y = Integer.parseInt(socket[5]);
        this.landP.x = Integer.parseInt(socket[7]);
        this.cost = this.bidAmount;
    }

    /**
     * Check that this Bid is valid
     * 
     * @param bankLimit
     *            Current bank account value of operator that must be higher
     *            than amount of bid.
     * @param maxP
     *            Point of the Bid on the Grid.
     * @return True is the Bid is valid.
     */
    public boolean checkValid(int bankLimit, Point maxP) {
        if (owner.getGrid().getOwner(landP) == null
                || owner.getGrid().getOwner(landP).equals(Bid.noOwner)) {
            // if bidAmount is not limited by bank amount and is greater than
            // min
            if (bankLimit >= bidAmount && bidAmount >= getMinBid()) {
                // if landP is inside of maxP and greater than 0
                if (landP.x < maxP.x && landP.y < maxP.y && landP.x >= 0
                        && landP.y >= 0) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Returns the amount of the bid.
     * 
     * @return the amount of the bid.
     */
    /*
     * public int getAmount() { return bidAmount; }
     */

    /**
     * Get the location of the Bid.
     * 
     * @return the x,y coordinate of the land.
     */
    public Point getPoint() {
        return landP;
    }

    /**
     * makes the bid class comparable, compares amounts of bids.
     * 
     * @param The
     *            bid to compare to this bid.
     * @return Positive if passed bid is higher than this bid.
     */
    @Override
    public int compareTo(Bid in) {
        return in.getCost() - this.bidAmount;
    }

    /**
     * Checks to see if two bids are for the same section on the grid.
     * 
     * @param other,
     *            the other bid to check with this bid.
     * @return True if the bids are the same, False if they are different
     */
    public boolean equalsLand(Bid other) {
        return (landP.y == other.getPoint().y && landP.x == other.getPoint().x);
    }

    /**
     * Checks if this Bid is equivalent to another action, namely a bid.
     * 
     * @param other
     *            The action to compare this Bid to.
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Bid) {
            if (super.equals(other)) {
                if (equalsLand((Bid) other)) {
                    // if(bidAmount == other.getCost()){
                    return true;
                    // }
                }
            }
        }
        return false;
    }

    /**
     * formats the bid for sending over a socket.
     * 
     * @return returns the bid as a string formatted for transfer over the
     *         socket
     */
    @Override
    public String toSocket() {
        return "op#" + owner.getName() + "#price#" + bidAmount + "#y#" + landP.y
                + "#x#" + landP.x;
    }

    /**
     * Formats the bid as a string.
     * 
     * @return returns the bid formated as a string.
     */
    @Override
    public String toString() {
        DecimalFormat myFormatter = new DecimalFormat("$###,###,###");
        String output = myFormatter.format(bidAmount);
        return "Bid -- " + ((won) ? "WON " : "") + "Amount: " + output
                + " - Land: " + "(" + landP.x + ", " + landP.y + ") - "
                + super.toString();
    }

    public void setWon() {
        won = true;
    }

    public boolean won() {
        return won;
    }
}

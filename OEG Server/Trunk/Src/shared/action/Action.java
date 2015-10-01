package shared.action;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import shared.Operator;

/**
 * Superclass that holds similar information for all types of requests that are
 * made in OEG.
 */
public class Action {

    /** The cost to setup a seismic request */
    protected static int seismicSetup;
    /** The additional cost per cell of a seismic request */
    protected static int seismicRate;
    /** The cost of a drill action */
    public static int    drillCost;
    /** Rate of income for successful found oil, per barrel */
    public static int    oilRate;
    /** rate of income for successful found gas, 1000 cubic feet */
    public static int    gasRate;
    /** The minimum cost to lease a piece of land */
    protected static int minBid = 0;

    /** The owner, or party responsible for this Action */
    protected Operator owner;
    /** The time this Action was created */
    protected Date     timeStamp;

    /** The bank balance that existed before this Action was made */
    protected int beginBalance;
    /** The total cost of this Action */
    protected int cost;

    /**
     * Constructor for when the owner is known immediately
     * 
     * @param owner
     *            The owner responsible for this action.
     */
    public Action(Operator owner) {
        this.timeStamp = new Date();
        setOwner(owner);
    }

    /** Constructor for when the owner is not yet known */
    public Action() {
        this.timeStamp = new Date();
    }

    /**
     * Set the owner after this object as been created.
     * 
     * @param owner
     *            The owner responsible for this action.
     */
    public void setOwner(Operator owner) {
        this.owner = owner;
        this.beginBalance = owner.getBankBalance();
    }

    /**
     * Set the cost to set up a seismic request
     * 
     * @param amount
     *            is the setup cost to do survey
     */
    public static void setSeismicSetup(int amount) {
        seismicSetup = amount;
    }

    /**
     * Set the cost per cell of a seismic request.
     * 
     * @param amount
     *            is the additional cost based on length of survey line
     */
    public static void setSeismicRate(int amount) {
        seismicRate = amount;
    }

    /**
     * Set the minimum bid amount for a piece of land.
     * 
     * @param amount
     *            is the minimum lease bid per grid cell
     */
    public static void setMinBid(int amount) {
        minBid = amount;
    }

    /**
     * Get the minimum bid amount for a piece of land.
     * 
     * @return The minimum amount an operator can bid for a lease on a piece of
     *         land.
     */
    public static int getMinBid() {
        return minBid;
    }

    /**
     * Get the overall cost of this Action
     * 
     * @return The cost of the Action, in $.
     */
    public int getCost() {
        return cost;
    }

    /**
     * Get the cost of drilling on a piece of land.
     * 
     * @param amount
     *            is the cost of drilling in each section.
     */
    public static void setDrillCost(int amount) {
        drillCost = amount;
    }

    /**
     * Set the rate of income for successful found oil, per barrel.
     * 
     * @param amount
     *            is the amount of money PER BARREL.
     */
    public static void setOilRate(int amount) {
        oilRate = amount;
    }

    /**
     * Set the rate of income for successful found gas, 1000 cubic feet.
     * 
     * @param amount
     *            is the amount per 1000 cubic feet.
     */
    public static void setGasRate(int amount) {
        gasRate = amount;
    }

    /**
     * Returns the operator who owns the bid.
     * 
     * @return the owner of the bid.
     */
    public Operator getOwner() {
        return owner;
    }

    /**
     * Set the costs of a seismic request, from a socket string
     * 
     * @param in
     *            The string holding seismicSetup and seismicRate info.
     */
    public static void setSeismicCosts(String in) {
        String[] parse = in.split("#");
        seismicSetup = Integer.parseInt(parse[1]);
        seismicRate = Integer.parseInt(parse[3]);
    }

    /**
     * Get the costs of a seismic request as a socket string
     * 
     * @return A socket representation of seismic costs.
     */
    public static String seismicCostsToSocket() {
        return "setup#" + seismicSetup + "#rate#" + seismicRate;
    }

    /**
     * Compare the names of the owners of this Action
     * 
     * @param other
     *            The other Action to compare against.
     * @return Returns TRUE if this owner is equal to other's owner.
     */
    @Override
    public boolean equals(Object other) {
        if (owner.getName().equals(((Action) other).getOwner().getName())) {
            return true;
        }
        return false;
    }

    /**
     * Returns a string with the balance after the Action and the time of the
     * action.
     * 
     * @return Formated string with the balance and time.
     */
    @Override
    public String toString() {
        DecimalFormat myFormatter = new DecimalFormat("$###,###,###");
        String output = myFormatter.format(beginBalance - cost);

        SimpleDateFormat formatter =
                new SimpleDateFormat("MMM dd, h:mm a", Locale.US);
        String timeOutput = formatter.format(this.timeStamp);

        return " Balance: " + output + " Time: " + timeOutput;
    }

    /**
     * Set the contents of this action as a socket string. Never needed.
     * 
     * @return A socket representation of this Action.
     */
    public String toSocket() {
        return owner.getName() + "#" + cost;
    }

}
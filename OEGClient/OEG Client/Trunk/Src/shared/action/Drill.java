package shared.action;

import java.awt.Point;
import java.text.DecimalFormat;

import shared.Grid;
import shared.Operator;

public class Drill extends Action {
    /**
     * The location of the bid.
     */
    private Point landP;

    /**
     * The depth of the bid. !currently unused!
     */
    private int depth;

    /**
     * Constructor for a Drill given an Owner and a Point.
     * 
     * @param owner
     *            is the owner of the drill
     * @param the
     *            point to drill.
     */
    public Drill(Operator owner, Point p) {
        super(owner);
        this.landP = p;
        this.owner = owner;
        this.cost = Action.drillCost;
    }

    /**
     * Constructor that forms a Drill from an Owner and a socket message.
     * 
     * @param owner,
     *            the owner of the drill.
     * @param socketIn,
     *            the socket message
     */
    public Drill(Operator owner, String socketIn) {
        super(owner);
        String[] split = socketIn.split("#");
        parseSocket(split);
        this.owner = owner;
        this.cost = Action.drillCost;
    }

    // UNUSED CONSTRUCTOR
    /*
     * public Drill(String socketIn) { super(); this.owner = null; String[]
     * split = socketIn.split("#"); parseSocket(split); }
     */

    /**
     * Sets the points of this drill from a String array.
     * 
     * @param The
     *            String array containing the coordinates of the drill.
     */
    private void parseSocket(String[] socket) {
        landP = new Point();
        this.landP.x = Integer.parseInt(socket[1]);
        this.landP.y = Integer.parseInt(socket[3]);
    }

    /**
     * Checks to see if a drill request is valid.
     * 
     * @param p
     *            the point of the Drill.
     * @return TRUE if the request is valid.
     */
    public boolean checkValid(Point p) {
        // if there is enough money
        if (owner.getBank().getAdjustedBalance() >= getCost()) {
            // if the cell exists in the owner's grid
            if (owner.getGrid().exists(p)) {
                // if the cell in owner's grid belongs to owner
                if (owner.getGrid().getOwner(p) != null)
                    if (owner.getGrid().getOwner(p).equals(owner.getName())) {
                    // if the cell hasn't already been drilled
                    if (!owner.getGrid().isDrilled(p)) {
                        // if the cell hasn't been drilled on yet
                        if (owner.getGrid().isDrilled(p) == false) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns a '#' separated String containing information on oil and gas at
     * this drill point.
     * 
     * @param grid
     *            the grid to check for oil and gas.
     * @return A '#' separated String with oil and gas information.
     */
    public String getData(Grid grid) {
        String toReturn = "oil#";
        toReturn += grid.getOilArray(landP);
        toReturn += "#gas#";
        toReturn += grid.getGasArray(landP);
        return toReturn;
    }

    /**
     * formats the drill for sending over a socket.
     * 
     * @return returns the bid as a string formatted for transfer over the
     *         socket
     */
    @Override
    public String toSocket() {
        return "x#" + landP.x + "#y#" + landP.y;
    }

    /**
     * Returns the point of this Drill
     * 
     * @return The point where this drill is located
     */
    public Point getPoint() {
        Point toReturn = landP;
        return toReturn;
    }

    /*
     * public int getCost() { return drillCost; }
     */
    /**
     * Checks to see if two drill requests are for the same section on the grid.
     * 
     * @param other
     *            the other bid to check with this bid.
     * @return True if the bids are the same, False if they are different
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Drill) {
            if (super.equals(other)) {
                if (landP.equals(((Drill) other).getPoint())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Formats the drill as a string.
     * 
     * @return returns the bid formated as a string.
     */
    @Override
    public String toString() {
        DecimalFormat myFormatter = new DecimalFormat("$###,###,###");
        String output = myFormatter.format(cost);

        return "Drill -- Cost: " + output + " - Land: " + "(" + landP.x + ", "
                + landP.y + ") - " + super.toString();
    }
}

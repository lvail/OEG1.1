package shared.action;

import java.awt.Point;
import java.text.DecimalFormat;

import shared.Operator;

public class SeismicRequest extends Action {

    /**
     * Starting (upper leftmost) coordinate point of this request
     */
    private Point startP;
    /**
     * Ending (lower rightmost) coordinate point of this request
     */
    private Point endP;

    /**
     * Constructor that sets needed information for this request
     * 
     * @param owner
     *            Owner of the request, passed to the superclass
     * @param startP
     *            Starting (upper leftmost) coordinate point.
     * @param endP
     *            Ending (lower rightmost) coordinate point.s
     */
    public SeismicRequest(Operator owner, Point startP, Point endP) {
        super(owner);
        this.startP = startP;
        this.endP = endP;
        this.cost = getSeismicCost();
    }

    /**
     * Constructor that forms a request based on a socket message
     * 
     * @param operator
     *            Owner of the request.
     * @param socketIn
     *            Socket message received that contains SeismicRequest values.
     */
    public SeismicRequest(Operator owner, String in) {
        super(owner);
        String[] split = in.split("#");
        startP = new Point();
        endP = new Point();
        this.startP.x = Integer.parseInt(split[1]);
        this.startP.y = Integer.parseInt(split[3]);
        this.endP.x = Integer.parseInt(split[5]);
        this.endP.y = Integer.parseInt(split[7]);
        this.cost = getSeismicCost();
    }

    /**
     * Check to make sure coordinates are within bounds, and start is before end
     * 
     * @param maxX
     * @param maxY
     * @return
     */
    public boolean checkValid(Point maxP) {
        // if there is enough money
        if (owner.getBank().getAdjustedBalance() >= getCost()) {
            // if start and end x values are in bounds
            if (startP.x >= 0 && endP.x >= 0 && startP.x < maxP.x
                    && endP.x < maxP.x) {
                // if start and end y values are in bounds
                if (startP.y >= 0 && endP.y >= 0 && startP.y < maxP.y
                        && endP.y < maxP.y) {
                    // if at least one start, end difference is zero
                    // (if horizontal or vertical, and diagonal)
                    if (((startP.x - endP.x) == 0)
                            || ((startP.y - endP.y) == 0)) {
                        // if the starting coord is less than the end coord
                        if ((startP.x <= endP.x || startP.x > endP.x)
                                && (startP.y <= endP.y || startP.y > endP.y)) {
                            if (startP.x > endP.x) {
                                int tempX = endP.x;
                                endP.x = startP.x;
                                startP.x = tempX;

                            }
                            if (startP.y > endP.y) {
                                int tempY = endP.y;
                                endP.y = startP.y;
                                startP.y = tempY;

                            }
                            return true;
                        }
                    } else if (Math.abs(startP.x - endP.x) == Math
                            .abs(startP.x - endP.x)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Formats the bid as a string.
     * 
     * @return returns the bid formated as a string.
     */
    @Override
    public String toString() {
        DecimalFormat myFormatter = new DecimalFormat("$###,###,###");
        String output = myFormatter.format(super.cost);
        return "Seismic Request -- Begin Cell: " + "(" + startP.x + ", "
                + startP.y + ")" + " - End Cell: " + "(" + endP.x + ", "
                + endP.y + ")\n    " + "Cost: " + output + " - "
                + super.toString();
    }

    /**
     * formats the bid for sending over a socket.
     * 
     * @return returns the bid as a string formatted for transfer over the
     *         socket
     */
    @Override
    public String toSocket() {
        return "sx#" + startP.x + "#sy#" + startP.y + "#ex#" + endP.x + "#ey#"
                + endP.y;
    }

    /**
     * Returns the number of cells in the desired Seismic Line.
     * 
     * @return The number of cells.
     */
    public int getNumCells() {
        int xLen = Math.abs(startP.x - endP.x);
        int yLen = Math.abs(startP.y - endP.y);
        return (xLen > yLen) ? xLen : yLen;
    }

    /**
     * Returns the starting point for the Seismic Line
     * 
     * @return the starting point of the Seismic Line
     */
    public Point getStartP() {
        return startP;
    }

    /**
     * Returns the ending point for the Seismic Line.
     * 
     * @return the ending point of the Seismic Line.
     */
    public Point getEndP() {
        return endP;
    }

    /**
     * Get the amount of $ that this seismic request will cost the operator.
     * Cost includes initial setup/mobilization cost plus additional cost based
     * on length of survey line per grid cell
     * 
     * @return Cost in $
     */
    private int getSeismicCost() {
        return seismicSetup + (seismicRate * (getNumCells() + 1));
    }

    /**
     * Returns an array of every point represented in this SeismicRequest
     * 
     * @return an array of Points for this request
     */
    public Point[] getPoints() {
        Point[] toReturn;
        int j;
        if (Math.abs(startP.x - endP.x) == Math.abs(startP.y - endP.y)) {
            toReturn = new Point[Math.abs(startP.x - endP.x) + 1];
            j = 0;
            /*
             * if(startP.x>endP.x){ System.out.print("start  "+startP.x+
             * "   end  "+endP.x); int i= startP.x; startP.x = endP.x; endP.x=i;
             * System.out.print("start  "+startP.x+"   end  "+endP.x); }
             * if(startP.y>endP.y){ System.out.print("start  "+startP.y+
             * "   end  "+endP.y); int i= startP.y; startP.y = endP.y; endP.y=i;
             * System.out.print("start  "+startP.y+"   end  "+endP.y); }
             */
            if (endP.x < startP.x) {
                if (endP.y < startP.y) {//
                    System.out.print("startx" + startP.x + "  starty" + startP.y
                            + "   endx" + endP.x + "   endy" + endP.y + "\n");
                    for (; endP.x <= startP.x; endP.x++) {
                        // fillSquare(g,Color.red,x2,y2);
                        toReturn[j++] = new Point(endP.x, endP.y);
                        endP.y++;
                    }
                } else {
                    System.out.print("startx" + startP.x + "  starty" + startP.y
                            + "   endx" + endP.x + "   endy" + endP.y + "\n");
                    for (; startP.x >= endP.x; startP.x--) {
                        // fillSquare(g,Color.red, x1, y1);
                        toReturn[j++] = new Point(startP.x, startP.y);
                        startP.y++;
                    }
                }
            } else {
                if (endP.y < startP.y) {
                    System.out.print("startx" + startP.x + "  starty" + startP.y
                            + "   endx" + endP.x + "   endy" + endP.y + "\n");
                    for (; startP.x <= endP.x; startP.x++) {
                        // fillSquare(g,Color.red,x1,y1);
                        toReturn[j++] = new Point(startP.x, startP.y);
                        startP.y--;
                    }
                } else {//
                    System.out.print("startx" + startP.x + "  starty" + startP.y
                            + "   endx" + endP.x + "   endy" + endP.y + "\n");
                    for (; startP.x <= endP.x; startP.x++) {
                        // fillSquare(g,Color.red, x1, y1);
                        toReturn[j++] = new Point(startP.x, startP.y);
                        startP.y++;
                    }
                }
            }

            // for(int i = 0; i <= Math.abs(startP.x -endP.x); i++) {
            // toReturn[j++] = new Point(startP.x+i,startP.y+i);
            // }
        } else if ((startP.x - endP.x) != 0) {
            toReturn = new Point[Math.abs(startP.x - endP.x) + 1];
            j = 0;
            for (int i = startP.x; i <= endP.x; i++) {
                toReturn[j++] = new Point(i, startP.y);
            }

        } else {
            toReturn = new Point[Math.abs(startP.y - endP.y) + 1];
            j = 0;
            for (int i = startP.y; i <= endP.y; i++) {
                toReturn[j++] = new Point(startP.x, i);
            }
        }
        return toReturn;
    }

    /**
     * Checks to see if this SeismicRequest is equivalent to another Action,
     * namely a SeismicRequest.
     * 
     * @param other
     *            The action to compare to this SeismicRequest
     * @return Returns TRUE if the two Actions are equivalent.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof SeismicRequest) {
            if (super.equals(other)) {
                if (startP.equals(((SeismicRequest) other).getStartP())) {
                    if (endP.equals(((SeismicRequest) other).getEndP())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

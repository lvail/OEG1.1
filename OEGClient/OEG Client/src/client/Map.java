package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import shared.Grid;
import shared.LithologicType;
import shared.action.Bid;
import shared.action.SeismicRequest;

/**
 * testMap
 *
 * @author Joe Schindel
 * @id jschinde
 * @OEG
 */

class Map extends JPanel
                implements ActionListener, MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    private final int minSize = 15;
    private int s, h, w; // size height and width variables
    private int[] xMinCords; // this an array of the x minimum values for each
                             // square on the map
    private int[] yMinCords; // this an array of the y minimum values for each
                             // square on the map
    private int squareSize; // this value is used by several methods, mainly
                            // draw
    private final int BUFFER = 15; // this is the width of the edge of the map
                                   // and always included when drawing a
                                   // fixed-position thing
    private int highestSurfaceElev, lowestSurfaceElev, surfaceElevRange = -1; // the
                                                                              // highest
                                                                              // and
                                                                              // lowest
                                                                              // elevations
                                                                              // on
                                                                              // the
                                                                              // map's
                                                                              // surface,
                                                                              // and
                                                                              // the
                                                                              // difference
                                                                              // between
    private Requests request;
    public Color curBGColor = new Color(240, 240, 240);
    public Color originalGold = new Color(184, 138, 0);
    public Color brighterGold = new Color(218, 167, 0); // biddable 385
    public Color darkestGold = new Color(102, 79, 0);
    public Color lightGreen = new Color(177, 250, 162);
    public Color brightGreen = new Color(66, 184, 66); // owned by me
    public Color red = new Color(184, 66, 66); // owned by someone else
    public Color blue = new Color(0, 153, 218); // selected for seismic
    Color paleBlue = new Color(109, 186, 218); // ghost line color

    /** sMode is the toggle for the seismic line */
    public int sMode = 0;
    int lastX = 0;
    int lastY = 0;
    /** used by clickColor to change the previous square back to default */
    private int oldx = 0, oldy = 0;
    private Image[] rocksMGS;
    private boolean drawRocks;

    /**
     * Constructor that creates a grid the sizes set in the constructor
     * 
     * @param request
     *            The GUI object
     */
    public Map(Requests request) {
        // not being used
        s = 15;
        h = 450;
        w = 450;
        this.request = request;
        initilize();
    }

    /**
     * Constructor that creates a grid based on the size specified. Currently,
     * the size must be a square.
     * 
     * @param request
     *            The GUI object
     * @param p
     *            The size of the grid, represented as a Point.
     */
    public Map(Requests request, Point p) {
        this.request = request;
        s = p.x;
        h = 450;
        w = 450;
        initilize();
    }

    /**
     * Initialize the GUI!
     */
    public void initilize() {

        // This sets h and w to be a square based on the smaller value
        if (h != w) {
            if (h > w)
                h = w;
            else if (h < w)
                w = h;
        }

        // Initialize the layer images so they are retrieved once (not for every
        // cell)
        Grid grid = request.operator.getGrid();
        rocksMGS = new Image[grid.getNumLayers()];
        for (int i = 0; i < grid.getNumLayers(); i++) {
            try {
                System.out.printf("grid: %h\n", grid);
                System.out.printf("getRockArray: %h\n", grid.getRockArray()[0]);
                System.out.printf("imagePath: %h\n",
                                grid.getRockArray()[0].imagePath());
                File rockFile = new File(grid.getRockArray()[i].imagePath());
                rocksMGS[i] = ImageIO.read(rockFile);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        drawRocks = false;

        // this sets square size 30 is the buffer times 2 (15*2)
        squareSize = h / s;// (h - 30)/s;

        // set minimum square size
        if (squareSize < minSize) {
            squareSize = minSize;
            h = w = squareSize * s;
        }

        setSize(w + 60, h + 60);
        setLayout(null);
        setPreferredSize(new Dimension(w + 30, h + 30));

        // set the arrays for the clickable part
        xMinCords = new int[s + 1];
        yMinCords = new int[s + 1];

        // Canvas is important to clickable
        setOpaque(true);
        addMouseListener(this);
        addMouseMotionListener(this);

    }

    /*
     * (non-Javadoc) paints on the canvas, happens automatically
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = BUFFER; // buffers to add space on the right and top sides
        int y = BUFFER;

        drawCompass(g);
        g.setColor(Color.black); // black lines for the grid

        // draw horizontal lines
        for (int i = 0; i <= s; i++) {
            g.drawLine(x, y, x, (BUFFER + (s * squareSize)));
            xMinCords[i] = x;
            x += squareSize;
        }
        x = BUFFER;// reset the values before drawing
        y = BUFFER;
        // draw vertical lines
        for (int i = 0; i <= s; i++) {
            g.drawLine(x, y, (BUFFER + (s * squareSize)), y);
            yMinCords[i] = y;
            y += squareSize;
        }

        // paint all the colors /per mode
        paintColorsMode(g, lastX, lastY);
        setCellImages(g);
    }

    public void drawCompass(Graphics g) {
        g.setFont(new Font("Default", Font.BOLD, g.getFont().getSize()));
        FontMetrics f = g.getFontMetrics();

        g.setColor(Color.black); // black lines for the grid

        g.drawString("N", ((w + BUFFER * 2) / 2) - f.charWidth('N') / 2,
                        BUFFER / 2 + f.getHeight() / 2 - 2);
        g.drawString("S", ((w + BUFFER * 2) / 2) - f.charWidth('S') / 2,
                        (h + BUFFER * 2) - BUFFER / 2 + f.getHeight() / 2 - 2);
        g.drawString("E", (w + BUFFER * 2) - BUFFER / 2 - f.charWidth('E') / 2,
                        ((h + BUFFER * 2) / 2) + f.getHeight() / 2);
        g.drawString("W", BUFFER / 2 - f.charWidth('W') / 2 + 1,
                        ((h + BUFFER * 2) / 2) + f.getHeight() / 2);
    }

    /**
     * getCol returns the column a click is in
     * 
     * @param x
     *            the x cordinate of the click
     * @return the row the click was in
     */
    public int getCol(int x) {
        int col = 0;
        for (int i = 0; i <= s; i++) {
            if (x < xMinCords[i]) {
                col = i - 1;
                break;
            }
        } // end for
        return col;
    }

    /**
     * getRow returns the column a click is in
     * 
     * @param y
     *            the y cordinate of the click
     * @return the row the click was in
     */
    public int getRow(int y) {
        int row = 0;
        for (int i = 0; i <= s; i++) {
            if (y < yMinCords[i]) {
                row = i - 1;
                break;
            }
        } // end for
        return row;
    }

    /**
     * This is what happens when the mouse is clicked!
     * 
     * @param event
     */
    public void onMouseClick(MouseEvent event) {
        int x = 0, y = 0; // after methods will be a value inside the grid

        // This is a check of the cordinates to make sure they are in the grid.
        if (!(event.getX() > (squareSize * s + BUFFER)
                        || event.getY() > squareSize * s + BUFFER
                        || event.getX() < BUFFER || event.getY() < BUFFER)) {
            // if it is then the x and y values get set
            lastX = x = getCol(event.getX());
            lastY = y = getRow(event.getY());
        }

        /*
         * if the current action mode is seismic, alternate the sMode in between
         * start and end
         */
        if (request.buttonGroup.getSelection().getActionCommand().equals("S"))
            sMode = (sMode == 1) ? 0 : 1; // this is used to determine what
                                          // click it is

        // update the cell info box for the current selection
        setCellInfo(lastX, lastY);

        // repaint the map to display new selection
        repaint(); // calls paintComponent();

        // this is what happens when the right
        if (event.isMetaDown()) {
        }
    }

    /**
     * Paint colors on the map based on the current mode indicated by the action
     * radio buttons. Also highlights the current selected cell.
     * 
     * @param g
     *            The Graphics object used to paint colors.
     * @param x
     *            The x coordinate of the current selected cell.
     * @param y
     *            The y coordinate of the current selected cell.
     */
    public void paintColorsMode(Graphics g, int x, int y) {
        Point p = new Point(x, y);
        String rbs = request.buttonGroup.getSelection().getActionCommand(); // radio
                                                                            // button
                                                                            // selected
        colorBiddableLand(g);
        Grid grid = request.operator.getGrid();
        grid.goToSurface();

        if (rbs.equals("B")) { // If the bid radio box is selected

            selectCell(g, new Color(210, 210, 210), p);
            doBid(p);
        }
        else if (rbs.equals("D")) { // if the drill radio box is selected
            colorDrillableLand(g);
            selectCell(g, Color.CYAN, p);
            doDrill(p);
        }
        else if (rbs.equals("S")) { // if the seismic line radio box is selected
            colorSeismicKnowledge(g);

            Point startP = new Point(0, 0);
            Point endP = new Point(0, 0);
            try {
                startP = new Point(request.getStartX(), request.getStartY());
                endP = new Point(request.getEndX(), request.getEndY());
            }
            catch (Exception e) {
            }

            // sMode is toggled with every mouse click in onMouseClick()
            // if sMode indicates setting the start value
            if (sMode == 0) {
                startP = p;
                // display the starting x,y values in their textbox
                request.setStartX(startP.x);
                request.setStartY(startP.y);
                // clear the ending x,y values
                request.setendX("");
                request.setendY("");
                // instruct to select ending cell
                request.setseismicCostField("Click Second Cell.");
                // color the starting selection
                seismicColor(g, startP, startP);
                // if sMode indicates setting the end value
            }
            else {
                endP = p;
                // display the x,y values in their textbox
                request.setendX(endP.x + "");
                request.setendY(endP.y + "");

                // get the cost and display it
                String cost = request.operator.getCostSeismic(startP.x,
                                startP.y, endP.x, endP.y);
                request.setseismicCostField(cost);
                // color the selection
                seismicColor(g, startP, endP);

                /*
                 * if the selection is not valid, display a helpful message.
                 */
                if (cost.equals("Invalid Coordinates.")) {
                    request.setseismicCostField("Problem with Request.");
                }
            }
        }
    }

    public void doDrill(Point p) {
        request.setStartX(p.x);
        request.setStartY(p.y);
    }

    public void doBid(Point p) {
        request.setStartX(p.x);
        request.setStartY(p.y);

    }

    /**
     * This method iterates through each square and colors it if there is no
     * owner
     * 
     * @param g
     *            The Graphics object to use for painting.
     */
    public void colorBiddableLand(Graphics g) {
        String player = Requests.teamName;
        String owner;
        Grid grid = request.operator.getGrid();
        Point p;

        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                p = new Point(j, i);

                owner = grid.getOwner(p);
                if (!grid.exists(p) || owner == null) {
                    // If no one owns it
                    fillSquare(g, brighterGold, p);
                }
                else if (!grid.isDrilled(p) && owner.equals(player)) {
                    // Just haven't drilled yet
                    fillSquare(g, brightGreen, p);
                    System.out.println("y:" + p.y);
                    System.out.println("p.y*squareSize:" + p.y * squareSize);
                }
                else if (grid.isDrilled(p) && owner.equals(player)) {
                    // Player's drilled
                    fillSquare(g, brightGreen, p);
                }
                else {
                    // Someone else owns it
                    fillSquare(g, red, p);
                }
            }
        }

    }

    /**
     * This Colors in a square specified
     * 
     * @param g
     *            The Graphics object to use for painting.
     * @param color
     *            - color to paint
     * @param p
     */
    public void fillSquare(Graphics g, Color color, Point p) {
        int elev;
        double gradient;
        if (surfaceElevRange == -1) { // This will occur twice when a session
                                      // begins; once, because there is no data,
                                      // and once when there finally is data.
            try {
                highestSurfaceElev = request.operator.getGrid()
                                .getHighestSurfaceElev();
                lowestSurfaceElev = request.operator.getGrid()
                                .getLowestSurfaceElev();
                surfaceElevRange = highestSurfaceElev - lowestSurfaceElev;
            }
            catch (NullPointerException e) { // This will occur on the first
                                             // error, when map is created but
                                             // cell data has not yet been sent.
                return;
            }
        }
        elev = request.operator.getGrid().getSurfaceElev(p) - lowestSurfaceElev;
        gradient = (double) elev / (double) surfaceElevRange * .5 + .5;

        if (g == null)
            g = this.getGraphics();
        g.setColor(gradientize(color, gradient));

        g.fillRect(xMinCords[p.x] + 1, yMinCords[p.y] + 1, squareSize - 1,
                        squareSize - 1);

        if (drawRocks)
            drawRocks(g, p);
    }

    /**
     * This outlines the square specified as selected
     * 
     * @param g
     * @param c
     * @param p
     */
    public void selectCell(Graphics g, Color c, Point p) {
        if (c == null)
            g.setColor(new Color(220, 220, 220));
        else
            g.setColor(c);
        g.drawRect(p.x * squareSize + BUFFER + 1, p.y * squareSize + BUFFER + 1,
                        squareSize - 2, squareSize - 2);
        if (p.x != oldx && p.y != oldy)
            repaint();
        oldx = p.x;
        oldy = p.y;
    }

    public void drawRocks(Graphics g, Point p) {
        if (drawRocks) {
            g.drawImage(rocksMGS[request.operator.getGrid().getSurfaceLayer(p)],
                            p.x * squareSize + BUFFER,
                            p.y * squareSize + BUFFER, squareSize, squareSize,
                            null);
        }
    }

    public Color gradientize(Color color, double gradient) {
        int red = (int) (color.getRed() * gradient);
        int green = (int) (color.getGreen() * gradient);
        int blue = (int) (color.getBlue() * gradient);
        return new Color(red, green, blue);
    }

    public void toggleRocks() {
        drawRocks = !drawRocks;
        repaint();
    }

    /**
     * Colors the land if the current player knows seismic information about a
     * cell
     * 
     * @param g
     *            The Graphics object to use for painting.
     */
    public void colorSeismicKnowledge(Graphics g) {
        Point p;
        Grid grid = request.operator.getGrid();
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                p = new Point(j, i);
                if (grid.exists(p)) {
                    if (grid.seismicExists(p)) {
                        Color herp = new Color(82, 61, 0);
                        fillSquare(g, herp, p);
                    }
                    else {
                        Color grey = new Color(240, 240, 240);
                        // fillSquare(g, grey,p);
                    }
                }
                else {
                    Color grey = new Color(240, 240, 240);
                    // fillSquare(g, grey,p);}
                }
            }
        }
    }

    /**
     * This colors in squares that are valid for seismic lines
     * 
     * @param g
     * @param startP
     * @param endP
     */

    public void seismicColor(Graphics g, Point startP, Point endP) {
        colorSeismicKnowledge(g);

        int x1 = startP.x;
        int y1 = startP.y;
        int x2 = endP.x;
        int y2 = endP.y;
        if (startP.equals(endP)) {
            for (int i = 0; i < x1; i++) {// horizontal left ghost
                // fillSquare(g,Color.pink,new Point(i, y1));
                selectCell(g, paleBlue, new Point(i, y1));
            }
            for (int i = (x1 + 1); i < s; i++) {// horizontal right ghost
                selectCell(g, paleBlue, new Point(i, y1));
            }
            for (int i = 0; i < y1; i++) {// vertical up ghost
                selectCell(g, paleBlue, new Point(x1, i));
            }
            for (int i = (y1 + 1); i < s; i++) {// vertical down ghost
                selectCell(g, paleBlue, new Point(x1, i));
            }
            int j = 0;
            for (int i = (x1 - 1); i >= 0; i--) {// diagonal up/left ghost
                j = x1 - i;
                j = y1 - j;
                if (i >= 0 && j >= 0) {
                    selectCell(g, paleBlue, new Point(i, j));
                }
                else
                    break;
            }
            for (int i = (x1 + 1); i <= s; i++) {// diagonal up/right ghost
                j = i - x1;
                j = y1 - j;
                if (i < s && j >= 0) {
                    selectCell(g, paleBlue, new Point(i, j));
                }
                else
                    break;
            }
            for (int i = (x1 + 1); i <= s; i++) {// diagonal down/right ghost
                j = i - x1;
                j = y1 + j;
                if (i < s && j < s) {
                    selectCell(g, paleBlue, new Point(i, j));
                }
                else
                    break;
            }
            for (int i = (x1 - 1); i >= 0; i--) {// diagonal down/left ghost
                j = x1 - i;
                j = y1 + j;
                if (i >= 0 && j < s) {
                    selectCell(g, paleBlue, new Point(i, j));
                }
                else
                    break;
            }
            selectCell(g, null, new Point(x1, y1));
            // Colors Vertical lines
        }
        else if (x1 == x2) {
            if (y1 > y2) {
                for (; y2 <= y1; y2++) {
                    fillSquare(g, blue, new Point(x2, y2));
                }
            }
            else
                for (; y1 <= y2; y1++) {
                    fillSquare(g, blue, new Point(x1, y1));
                }
        }
        // colors Horizontal Lines
        else if (y1 == y2) {
            if (x1 > x2) {
                for (; x2 <= x1; x2++) {// fills in color in cells between x2
                                        // and x1
                    fillSquare(g, blue, new Point(x2, y2));
                }
            }
            else
                for (; x1 <= x2; x1++) {// fills in color in cells between x1
                                        // and x2
                    fillSquare(g, blue, new Point(x1, y2));
                }
        }
        else if (Math.abs(x1 - x2) == Math.abs(y1 - y2)) {
            if (x2 < x1) {
                if (y2 < y1) {//
                    for (; x2 <= x1; x2++) {
                        fillSquare(g, blue, new Point(x2, y2));
                        y2++;
                    }
                }
                else {
                    for (; x1 >= x2; x1--) {
                        fillSquare(g, blue, new Point(x1, y1));
                        y1++;
                    }
                }
            }
            else {
                if (y2 < y1) {
                    for (; x1 <= x2; x1++) {
                        fillSquare(g, blue, new Point(x1, y1));
                        y1--;
                    }
                }
                else {//
                    for (; x1 <= x2; x1++) {
                        fillSquare(g, blue, new Point(x1, y1));
                        y1++;
                    }
                }
            }
        }

    }

    /**
     * @param g1
     *            The Graphics object to use for painting.
     */
    public void paintSeismicKnowledge(Graphics g1) {
        Point p;

        super.paintComponent(g1);
        Grid grid = request.operator.getGrid();
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                p = new Point(j, i);
                if (grid.exists(p)) {
                    Integer seismicInfo[] = grid.getLayerArray(p);

                    if (seismicInfo != null && seismicInfo[0] != 0) {
                        Color herp = new Color(82, 61, 0);
                        // fillSquare(herp,p);
                        g1.setColor(herp);
                        g1.fillRect(xMinCords[p.x] + 1, yMinCords[p.y] + 1,
                                        squareSize - 1, squareSize - 1);
                    }
                    else {
                        Color grey = new Color(240, 240, 240);
                        // fillSquare(grey,p);
                        g1.setColor(grey);
                        g1.fillRect(xMinCords[p.x] + 1, yMinCords[p.y] + 1,
                                        squareSize - 1, squareSize - 1);
                    }
                }
                else {
                    Color grey = new Color(240, 240, 240);
                    // fillSquare(grey,p);
                    g1.setColor(grey);
                    g1.fillRect(xMinCords[p.x] + 1, yMinCords[p.y] + 1,
                                    squareSize - 1, squareSize - 1);
                }
            }
        }
    }

    /**
     * Colors the land that the player can drill on
     * 
     * @param g
     *            The Graphics object to use for painting.
     */
    public void colorDrillableLand(Graphics g) {
        String player = Requests.teamName;
        String owner;
        Point p;
        Grid grid = request.operator.getGrid();
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                p = new Point(j, i);
                if (grid.exists(p)) {
                    owner = grid.getOwner(p);
                    if (owner != null) {
                        if (owner.equals(player)) {
                            Color herp = new Color(46, 184, 0);
                            fillSquare(g, herp, p);
                        }
                        else {
                            Color grey = new Color(240, 240, 240);
                            // fillSquare(g, grey,p.x,p.y);
                        }
                        if (grid.isDrilled(p)) {// light green
                            Color herp = new Color(177, 250, 162);
                            fillSquare(g, herp, p);
                        }
                    }
                    else {
                        Color grey = new Color(240, 240, 240);
                        // fillSquare(g, grey,p.x,p.y);
                    }
                }
                else {
                    Color grey = new Color(240, 240, 240);
                    // fillSquare(g, grey,p.x,p.y);
                }
            }
        }
    }

    public void setCellInfo(int x, int y) {
        // get the grid from operator
        Grid grid = request.operator.getGrid();
        Point p = new Point(x, y);
        int depth = 0;

        // strings to be displayed
        String owner, oil, gas, layers, rocktype;
        // if the grid location exists (not null)
        if (grid.exists(p)) {
            // get the owner name at p
            owner = grid.getOwner(p);
            // if the string is null or empty, display "Not Owned."
            if (owner == null || owner.equals("")) {
                owner = "Not Owned.";
            }

            // get the location of the top layer for seismic and rocktype
            while ((grid.layerExists(p, depth) == false)
                            && depth < grid.getNumLayers())
                depth++;

            // if seismic data exists, display all of it, else display the top
            if (grid.seismicExists(p)) {
                // get the layer (seismic) array at p
                Integer[] layersA = grid.getLayerArray(p);
                layers = grid.removeEmptyFromArray(p, layersA);
            }
            else {
                layers = grid.getLayer(p, depth) + "";
            }

            // if p has been drilled, display all info, else display only the
            // top rock.
            if (grid.isDrilled(p)) {
                // get the rock type array at p
                LithologicType[] rocktypeA = grid.getRockArray(p);
                String[] rocks = LithologicType.toArrayString(rocktypeA);
                rocktype = grid.removeEmptyFromArray(p, rocks);

                // get the oil rate array at p
                Integer[] oilA = grid.getOilArray(p);
                oil = grid.removeEmptyFromArray(p, oilA);

                // get the gas rate array at p
                Integer[] gasA = grid.getGasArray(p);
                gas = grid.removeEmptyFromArray(p, gasA);

            }
            else {
                oil = gas = "No Information.";
                rocktype = grid.getRock(p, depth).getShortName();
            }

            // if the gird location does not exist
        }
        else {
            owner = "Not Owned."; // If the cell does not exist it says not
                                  // owned
            oil = gas = layers = rocktype = "No Information."; // NO INFO FOR
                                                               // YOU!
        }

        request.setCellInfo(owner, oil, gas, layers, rocktype); // sets all of
                                                                // the data
                                                                // inside of
                                                                // requests
    }

    // Method to place images on Map cells. Development incomplete.
    // Kept in case helpful for the future.

    public void setCellImages(Graphics g) {
        Integer oilTemp[];
        Integer gasTemp[];
        LinkedList<Bid> bidQ;
        LinkedList<SeismicRequest> seismicQ;
        BufferedImage img = null;
        // Graphics g = this.getGraphics();
        // g.fillRect(5,5,100,100);

        Grid grid = request.operator.getGrid();
        bidQ = request.operator.getBidQueue();
        seismicQ = request.operator.getSeismicQueue();

        // loop through every cell
        for (int x = 0; x < s; x++) {
            for (int y = 0; y < s; y++) {
                boolean hasResource = false;
                Point p = new Point(x, y);

                if (grid.exists(p)) {
                    oilTemp = grid.getOilArray(p);
                    gasTemp = grid.getGasArray(p);

                    // draw oil image if oil is present
                    for (int ot : oilTemp) {
                        if (ot > 0) {
                            hasResource = true;

                            try {
                                img = ImageIO.read(getClass().getResource(
                                                "/client/images/oil_well_small.gif"));

                                // img = ImageIO.read(new
                                // File("C:/Users/jobotte1/Desktop/oil_small.gif"));

                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            int imgSize = (int) Math.round((25.0 / 45.0
                                            * (double) squareSize));
                            g.drawImage(img, x * squareSize + 16
                                            + squareSize / 2 - imgSize / 2,
                                            y * squareSize + 16 + squareSize / 2
                                                            - imgSize / 2,
                                            imgSize, imgSize, null);

                        }
                    }
                    // draw gas image if gas is present
                    for (int gt : gasTemp) {
                        if (gt > 0) {
                            try {
                                img = ImageIO.read(getClass().getResource(
                                                "/client/images/gas_well_small.gif"));

                                // img = ImageIO.read(new
                                // File("C:/Users/jobotte1/Desktop/oil_small.gif"));

                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            int imgSize = (int) Math.round(
                                            (25.0 / 45.0 * (double) squareSize))
                                            + 4;
                            g.drawImage(img, x * squareSize + 16
                                            + squareSize / 2 - imgSize / 2,
                                            y * squareSize + 16 + squareSize / 2
                                                            - imgSize / 2,
                                            imgSize, imgSize, null);

                        }

                    }

                    /*
                     * //draw bid image if point is being bid on for(Bid b :
                     * bidQ) { Point bidPoint = b.getPoint(); try { img =
                     * ImageIO.read(getClass().getResource(
                     * "/client/images/dollar.gif")); } catch (IOException e){
                     * e.printStackTrace(); } //g.drawImage(img, bidPoint.x,
                     * bidPoint.y, null); int imgSize =
                     * (int)Math.round((25.0/45.0*(double)squareSize))+4;
                     * g.drawImage(img,
                     * bidPoint.x*squareSize+16+squareSize/2-imgSize/2,
                     * bidPoint.y*squareSize+16+squareSize/2-imgSize/2, null); }
                     */

                    /*
                     * //draw empty image if neither is present if (hasResource
                     * == false){ try { img = ImageIO.read(new
                     * File("trunk/src/client/images/empty_small.gif")); } catch
                     * (IOException e){ e.printStackTrace(); } g.drawImage(img,
                     * x, y, null); }
                     */
                }
            }
        }

        /*
         * //draw seismic image if point is requested for(SeismicRequest sr :
         * seismicQ){ int i = 0; Point seismicPoints[] = sr.getPoints(); try {
         * img = ImageIO.read(new
         * File("trunk/src/client/images/seismicline_horizontal_small.gif")); }
         * catch (IOException e){ e.printStackTrace(); } while (seismicPoints[i]
         * != null){ g.drawImage(img,seismicPoints[i].x, seismicPoints[i].y,
         * null); } }
         */
    }

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    /**
     * Use mouseRelease so that clicks can be registered when the mouse is in
     * motion.
     */
    public void mouseReleased(MouseEvent event) {
        onMouseClick(event);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
    }

    @Override
    public void mouseDragged(MouseEvent arg0) {
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
        request.sethoverLabel(getCol(arg0.getX()), getRow(arg0.getY()));
        // set info on hover? --
        // setCellInfo(getCol(arg0.getX()),getRow(arg0.getY()));
    }
}
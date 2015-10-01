package server;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

// import shared.*;
import shared.Grid;
import shared.Messages;
import shared.action.Action;
import shared.action.Bid;
import shared.action.Drill;
import shared.action.Event;
import shared.action.SeismicRequest;

// TODO General: Store all game state information to a file for reloading.

/**
 * Director is responsible for organizing Operators and their game play. It
 * holds and manages the game Grid. It creates and stores new Operators,
 * handling all incoming connections. It handles rounds and processes end of
 * round actions that the operators have requested.
 */
public class Director {

    /** Array holding all the operators that have connected to the server */
    ServerOperator[] operators;

    /** Grid holding all land information */
    Grid grid;

    /** This is the amount of time in minutes that the round takes */
    public static long roundLength;

    /** This is the number of rounds that will take place */
    protected static int numRounds;

    /** The current round number in the simulation */
    protected int curRound = 0;

    protected boolean gameRunning = true;

    protected ServerOperator winner = null;

    /** the number of days that each round represents in this simulation */
    public static int timeStep;

    /** Timer class that executes when the end of round timer completes */
    EndRoundTimer timerHandler;

    /** Number of operators that exist */
    private int numOperators;

    /** The Director's GUI */
    public Manage manage;

    /**
     * Constructor stores the UI object, and handles parsing of the XML game
     * input file.
     * 
     * @param manage
     *            The GUI that the user of the server interacts with to view
     *            game information.
     * @param xmlFile
     *            The XML file that is used to fill all OEG game parameters.
     */
    public Director(Manage manage, File xmlFile) {
        operators = new ServerOperator[150];

        this.manage = manage;

        // load data from XML.
        grid = loadData(xmlFile);

        numOperators = 0;

        // Create the timer handler that will respond when the timer finishes.
        timerHandler = new EndRoundTimer(this, roundLength);
        // Create a recurring timer that goes off every roundLength seconds.
        new Timer().scheduleAtFixedRate(timerHandler, 100, (int) roundLength);

        // Update the UI to show timer and display good status.
        manage.setRoundTimer(roundLength, 1);
        manage.setStatus("Running");
    }

    /**
     * Returns a Grid that is loaded from the parsed XML file.
     * 
     * @param xmlFile
     *            Input XML game file.
     * @return A Grid containing all the land information.
     */
    private Grid loadData(File xmlFile) {
        Grid newGrid = new Grid();
        try {

            // get a SAX XML parser
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();

            // get the handler that will process the XML file and place results
            // in newGrid.
            ParseSimulationXML handler =
                    new ParseSimulationXML(xmlFile, newGrid);

            // if not in test mode, use the parameter file and parse.
            if (!Messages.TESTMODE)
                saxParser.parse(xmlFile, handler);
            else
                // if in test mode, use the file that has been stored in the
                // repository during testing.
                saxParser.parse("trunk/datafiles/sample.xml", handler);

        }
        catch (SAXException e) {
            // if the parsing goes wrong, update UI.
            manage.setStatus(e.getMessage());
            if (Messages.TESTMODE) e.printStackTrace();
            throw new Error("File not valid OEG XML, or CSV not Found.");
        } 
        catch (IOException e) {
            manage.setStatus("Problem with XML file.");
            e.printStackTrace();
        } 
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return newGrid;
    }

    /**
     * Handles a new client connection. Checks if the client already exists.
     * Creates a ServerOperator for the client. Sends needed simulation
     * parameters to the client.
     * 
     * @param client
     *            The socket of the new client.
     * @param name
     *            The team name of the new client.
     */
    public void newClient(ServerMessages client, boolean register, String name,
            String password) {
        if (register && manage.registerDisabled) {
            client.sendWarning(
                    "Registration has been disabled by the Instructor.");
            return;
        }
        ServerOperator addOp;
        int exists = -1;
        // Checks to see if the operator (based on name) already exists
        for (int i = 0; i < numOperators; i++) {
            if (operators[i] == null) continue;
            if (operators[i].getName().equals(name)) {
                exists = i;
            }
        }

        // if the operator is new
        if (exists == -1) {
            System.out.println("reg");
            if (!register) {
                client.sendWarning(
                        "Invalid team name, please use register if you wish to create a team.");
                return;
            }
            // create a new Operator with team name and socket
            addOp = operators[numOperators] =
                    new ServerOperator(this, name, password, client);

            // send the operator current game information
            sendGameInfo(client);

            sendTopOfGrid(addOp);
            // update the UI with the new operator's object
            manage.addOperator(addOp);
            // increment the number of operators
            numOperators++;
        } else {
            addOp = operators[exists];
            System.out.println("con " + addOp.socket);
            if (!addOp.password.equals(password)) {
                client.sendWarning("Invalid password.");
                // client.close();
                return;
            }
            if (addOp.socket != null) {
                // closes the operator's old socket
                addOp.sendWarning("Someone else has connected to this team.");
                addOp.closeSocket();
            }
            // add the new socket to existing operator object
            addOp.setSocket(client);
            // send acknowledgment
            addOp.sendInfo("Welcome Back!");
            // send the operator current game information
            sendGameInfo(client);
            addOp.sendExistingData();
        }

        if (!gameRunning) {
            addOp.sendEndGame(winner);
        }

        // create a login event and add it to the history list for logging.
        Event loginE = new Event(addOp);
        loginE.setEventLogin();
        addOp.addHistory(loginE);
    }

    private void sendTopOfGrid(ServerOperator client) {
        Grid opGrid = client.getGrid();
        opGrid.initializeAll();
        for (int i = 0; i < grid.getDimensions().y; i++) {
            for (int j = 0; j < grid.getDimensions().x; j++) {
                Point p = new Point(i, j);
                int x = 0;
                while (grid.getLayer(p, x) == null && x < grid.getNumLayers())
                    x++;
                opGrid.setLayer(p, grid.getLayer(p, x), x);
                opGrid.setRock(p, grid.getRock(p, x), x);
                opGrid.setLayerExists(p, grid.getLayerExists(p));
                opGrid.setOwner(grid.getOwner(p), p);
            }
        }
        client.sendGrid();
    }

    /**
     * Send current game information to a newly connected client. Game
     * information includes current timer value, grid size, and action cost
     * information.
     * 
     * @param client
     *            The client socket to send the info to.
     */
    private void sendGameInfo(ServerMessages client) {
        // send the operator current timer information.
        client.startTimer(timerHandler.getTimeRemaining(), roundLength,
                curRound, numRounds);
        client.sendGridLimits(grid.getDimensions().x, grid.getDimensions().y,
                grid.getNumLayers(), grid.getAllLayers());
        client.sendBidLimit(Action.getMinBid());
        client.sendSeismicCosts(Action.seismicCostsToSocket());
        client.sendDrillCost(Action.drillCost);
    }

    /**
     * To be called by a director or other unexpexcted source-- has ALL
     * end-round logic
     */
    protected void unscheduledEndRound() {
        System.out.println("Unscheduled End Round called");
        this.setupAuction();
        this.processSeismicRequests();
        this.processDrillRequests();
        if (++curRound > numRounds) {
            this.endGame();
            return;
        }
        // Update UI with new round number.
        manage.setRoundTimer(roundLength, curRound);
        // for every operator, notify end of round, add round event to his log
        // and reset the action queues.
        for (int i = 0; i < numOperators; i++) {
            if (operators[i] == null) continue;
            // send end of round notification to the operator
            operators[i].endRound(roundLength, roundLength, curRound,
                    numRounds);
            System.out.printf("Player %d: %s Notified of End of Round\n", i,
                    operators[i].toString());
        }

        manage.refreshInfo();
    }

    /** Performs needed actions at the end of every round */
    protected void endRound() {
        System.out.println("Ending Round");
        this.setupAuction();
        this.processSeismicRequests();
        this.processDrillRequests();

        // Update UI with new round number.
        manage.setRoundTimer(timerHandler.getTimeRemaining(), curRound);
        // for every operator, notify end of round, add round event to his log
        // and reset the action queues.
        for (int i = 0; i < numOperators; i++) {
            if (operators[i] == null) continue;
            // send end of round notification to the operator
            operators[i].endRound(timerHandler.getTimeRemaining(), roundLength,
                    curRound, numRounds);
            System.out.printf("Player %d: %s Notified of End of Round\n", i,
                    operators[i].toString());
        }

        manage.refreshInfo();
    }

    /**
     * Setup a new auction by gathering Bid queues from the ServerOperators and
     * starting Auction.
     */
    public void setupAuction() {
        // list array for every operator's bid queue
        LinkedList<Bid>[] list = new LinkedList[numOperators];
        // fill the array

        for (int i = 0, j = 0; i < numOperators; i++) {
            if ((operators[i] != null)) {
                System.out.println(operators[j] + ": "
                        + operators[j].getBidQueue().toString());
                list[i] = operators[j++].getBidQueue();
            }
        }
        // start the auction
        Auction a = new Auction(list, grid);
        LinkedList<Bid> winners = a.getResults();
        int h = 0;
        for (Bid w : winners) {
            System.out.println(++h);
            for (int i = 0, j = 0; i < numOperators; i++) {
                if ((operators[i] != null)) operators[i].setOwnership(w);
            }
        }
    }

    /**
     * Process Seismic requests by going through every operator's seismic queue
     * and returning the information from grid.
     */
    public void processSeismicRequests() {
        LinkedList<SeismicRequest> seismic;
        // for every operator
        for (int i = 0; i < numOperators; i++) {
            if (operators[i] == null) continue;
            seismic = operators[i].getSeismicQueue();
            // for every request in the operator's queue
            for (SeismicRequest s : seismic) {
                operators[i].updateBankBalance(0 - s.getCost());
                operators[i].addHistory(s);
                Point[] points = s.getPoints();
                // for every Point in the request
                for (Point p : points) {
                    operators[i].sendSeismicLayer(p, grid.getLayerArray(p));
                }
            }
        }
    }

    /**
     * Process Drill requests by going through every operator's drill queue and
     * returning the results of the drilling.
     */
    public void processDrillRequests() {
        LinkedList<Drill> drill;
        // for every operator
        for (int i = 0; i < numOperators; i++) {
            if (operators[i] == null) continue;
            drill = operators[i].getDrillQueue();
            // for every request in the operator's queue
            for (Drill d : drill) {
                operators[i].updateBankBalance(0 - d.getCost());
                operators[i].addHistory(d);
                Point p = d.getPoint();
                grid.setDrilled(p, true);
                operators[i].sendDrillStuffs(d.toSocket(),
                        grid.cellToSocket(p));
                /*
                 * d.toSocket(), grid.getGasArray(p), grid.getOilArray(p),
                 * grid.getGasSocket(p), grid.getOilSocket(p),
                 * grid.getRockSocket(p), grid.getRockArray(p));
                 */
                operators[i].addIncome(grid.getGasRate(p), grid.getOilRate(p));
            }
        }

    }

    /** To be called at runtime- changes round length */
    public void changeRoundLength(long length) {
        this.roundLength = length;
        timerHandler = new EndRoundTimer(this, roundLength);
        new Timer().scheduleAtFixedRate(timerHandler, 100, (int) roundLength);

    }

    /**
     * Set the time each round will last.
     * 
     * @param length
     *            is the amount of time in MS that each round is
     */
    public static void setRoundTime(long length) {
        roundLength = length;
    }

    /**
     * Set how long the game will last, in number of rounds.
     * 
     * @param number
     *            is the amount of rounds in the simulation
     */
    public static void setNumRounds(int number) {
        numRounds = number;
    }

    public static int getNumRounds() {
        return numRounds;
    }

    /**
     * Set how long each round simulates in real time.
     * 
     * @param amount
     *            is the amount in days that each round represents
     */
    public static void setTimeStep(int amount) {
        timeStep = amount;
    }

    /**
     * Get the game's Grid object.
     * 
     * @return the Grid that holds all information about land in OEG
     */
    public Grid getGrid() {
        return grid;
    }

    /** Update the UI if the server or gameplay is stopped. */
    public void serverStop() {
        manage.setStatus("Stopped");

    }

    public void endGame() {
        timerHandler.cancel();
        ServerOperator winner = getWinner();
        manage.setStatus("Game Over");
        for (int i = 0; i < numOperators; i++)
            operators[i].sendEndGame(winner);
    }

    /**
     * This method is called when the game is ended, and loops through each of
     * the clients and sets the game's winner
     */
    public ServerOperator getWinner() {
        winner = operators[0];
        for (int i = 1; i < numOperators; i++) {
            if (operators[i].getBankBalance() > winner.getBankBalance())
                winner = operators[i];

        }
        return winner;

    }

}

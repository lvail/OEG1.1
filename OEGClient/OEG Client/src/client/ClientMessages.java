
package client;

import java.net.Socket;

import javax.swing.JOptionPane;

import shared.Messages;
import shared.action.Action;
import shared.action.Bid;
import shared.action.Drill;
import shared.action.SeismicRequest;

// Fix the following comment
/**
 * ClientMessages is the Messages child that handles input and output to the
 * socket from the client applet. It parses incoming messages and calls the
 * appropriate method to handle them. It provides methods to send messages out
 * to the server.
 */
public class ClientMessages extends Messages {

    /**
     * The Jframe interface that is used to update based on information that is
     * received.
     */
    private ClientFrame dataFrame;

    /**
     * Constructor that sets the Socket object in the superclass and stores the
     * ClientFrame class that will receive incoming data.
     * 
     * @param frameIn
     *            Jframe that displays information on the GUI
     * @param socketIn
     *            Socket connection to the server.
     */
    public ClientMessages(ClientFrame frameIn, Socket socketIn) {
        super(socketIn);
        super.addChild(this);
        dataFrame = frameIn;
    }

    /**
     * Takes an input string that came from the server and decides what it means
     * and forwards it to a method in ClientApplet or Operator.
     * 
     * @param in
     *            String that needs to be decoded.
     * @param pr
     *            The type of message that is being received.
     */
    public void parse(String in, Prefix pr) {
        // More will be added in this switch statement later...
        switch (pr) {
            case CLOSE:
                close();
                break;
            case STARTTIME:
                String[] timerInfo = in.split("#");
                dataFrame.remaining = 500 + Long.parseLong(timerInfo[0]);
                dataFrame.roundLength = Long.parseLong(timerInfo[1]);
                dataFrame.lastUpdate = System.currentTimeMillis();
                dataFrame.curRound = Integer.parseInt(timerInfo[2]);
                dataFrame.totalRnd = Integer.parseInt(timerInfo[3]);
                dataFrame.resume();
                break;
            case WONBID:
                dataFrame.displayMessage("Won Bid: " + in);
                dataFrame.operator.bidResult(new Bid(in));
                dataFrame.displayMessage("Won Bid: " + new Bid(in));
                break;
            case LOSTBID:
                dataFrame.displayMessage("Lost Bid: " + in);
                dataFrame.operator.bidResult(new Bid(in));
                dataFrame.displayMessage("Lost Bid: " + new Bid(in));
                break;
            case NEWROUND:
                dataFrame.operator.setBankBalance(Integer.parseInt(in));
                dataFrame.resetActionQueue();
                dataFrame.refreshGUI();
                break;
            case BANKBALANCE:
                ((ClientOperator) operator)
                                .updateBankBalance(Integer.parseInt(in));
                break;
            case GRIDLIMITS:
                System.out.println("GRIDLIMITS: " + in);
                String[] xy = in.split("#");
                int[] layers = new int[Integer.parseInt(xy[2])];
                for (int i = 0; i < layers.length; i++)
                    layers[i] = Integer.parseInt(xy[i + 3]);
                // Grid size and each layer is passed
                ((ClientOperator) operator).setGridLimits(
                                Integer.parseInt(xy[0]),
                                Integer.parseInt(xy[1]),
                                Integer.parseInt(xy[2]), layers);
                break;
            case BIDLIMIT:
                Action.setMinBid(Integer.parseInt(in));
                break;
            case SEISMICCOSTS:
                Action.setSeismicCosts(in);
                break;
            case DRILLCOST:
                Action.setDrillCost(Integer.parseInt(in));
                break;
            case SEISMICDATA:
                dataFrame.operator.seismicRequestResult(in);
                break;
            case OIL:
                dataFrame.operator.oilInfo(in);
                break;
            case GAS:
                dataFrame.operator.gasInfo(in);
                break;
            case ROCK:
                dataFrame.operator.rockInfo(in);
            case DRILLRESULT:
                dataFrame.displayMessage("Drill Results Returned.");
                dataFrame.operator.drillRequestResult(in);
                break;
            case CELL:
                dataFrame.operator.addCellInfo(in);
                break;
            case BIDQUEUE:
                Bid newBid = new Bid(dataFrame.operator, in);
                dataFrame.operator.addQueue(newBid);
                break;
            case SEISMICQUEUE:
                SeismicRequest newS =
                                new SeismicRequest(dataFrame.operator, in);
                dataFrame.operator.addQueue(newS);
                break;
            case DRILLQUEUE:
                Drill newDrill = new Drill(dataFrame.operator, in);
                dataFrame.operator.addQueue(newDrill);
                break;
            case LANDOWNER:
                Bid winBid = new Bid(in);
                dataFrame.operator.bidResult(winBid);
                break;
            case ENDGAME:
                dataFrame.displayMessage(in);
                dataFrame.killAll();
                break;
            case SERVER:
                JOptionPane.showMessageDialog(null, in, "Server Message",
                                JOptionPane.ERROR_MESSAGE);
                break;
            case WARNING:
                JOptionPane.showMessageDialog(dataFrame.GUI, in,
                                "Server Message", JOptionPane.ERROR_MESSAGE);
                if (in.indexOf("delete") != -1 || in.indexOf("disabled") != -1)// If
                                                                               // "Deleted"
                                                                               // is
                                                                               // in
                                                                               // message--close
                                                                               // program
                    System.exit(1);
                dataFrame.login.setVisible(true);
                break;
            default:
                System.out.println(
                                "CM Unknown message: " + pr.name() + ": " + in);
                break;
        }
    }

    /**
     * Close the connection by updating the UI and calling super's close method.
     */
    public boolean close() {
        dataFrame.setIP("None");
        return super.close();
    }

    /**
     * Send a login message to the server.
     * 
     * @param name
     *            The name of the team/client.
     */
    public void sendLogin(boolean register, String name, String password) {
        sendMessage(register ? Prefix.REGISTER : Prefix.LOGIN,
                        name + "," + password);
    }

    /**
     * Send an end round request to the server. Only used in development, not
     * production.
     */
    public void sendEndRound() {
        sendMessage(Prefix.TEST, "");
    }

    /**
     * Send a new bid to the server.
     * 
     * @param bid
     *            The bid formatted as a string that is being sent.
     */
    public void sendNewBid(String bid) {
        sendMessage(Prefix.BID, bid);
    }

    /**
     * Send a new seismic request to the server.
     * 
     * @param seismic
     *            The seismic request formatted as a string.
     */
    public void sendNewSeismic(String seismic) {
        sendMessage(Prefix.SEISMICREQUEST, seismic);
    }

    /**
     * Send a new drill request to the server.
     * 
     * @param drill
     *            The drill request formatted as a string.
     */
    public void sendNewDrill(String drill) {
        sendMessage(Prefix.DRILLREQUEST, drill);
    }

    /**
     * Send a remove bid request to the server.
     * 
     * @param bid
     *            The bid to be removed, formatted as a string.
     */
    public void sendRemoveBid(String bid) {
        sendMessage(Prefix.REMOVEBID, bid);
    }

    /**
     * Send a remove seismic request request to the server.
     * 
     * @param seismic
     *            The seismic request to be removed, formatted as a string.
     */
    public void sendRemoveSeismic(String seismic) {
        sendMessage(Prefix.REMOVESEISMIC, seismic);
    }

    /**
     * Send a remove drill request to the server.
     * 
     * @param drill
     *            The drill request to be removed, formatted as a string.
     */
    public void sendRemoveDrill(String drill) {
        sendMessage(Prefix.REMOVEDRILL, drill);
    }

}
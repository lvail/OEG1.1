package client;

import java.net.*;

import shared.action.*;
import shared.*;

import javax.swing.JOptionPane;

/** ClientMessages is the Messages child that handles input and output
 * to the socket from the client applet. It parses incoming messages
 * and calls the appropriate method to handle them. It provides methods
 * to send messages out to the server.
 */
public class ClientMessages extends Messages {
	
	/** The Applet interface that is used to update
	 * based on information that is received. 
	 */
	private ClientApplet applet;
	
	/** Constructor that sets the Socket object in the superclass
	 * and stores the ClientApplet class that will receive incoming data.
	 * @param applet Applet that displays information on the GUI
	 * @param sock Socket connection to the server.
	 */
	public ClientMessages(ClientApplet applet, Socket sock) {
		super(sock);
		super.addChild(this);
		this.applet = applet;
	}
	
	/** Takes an input string that came from the server
	 * and decides what it means and forwards it to a
	 * method in ClientApplet or Operator. 
	 * @param in String that needs to be decoded.
	 * @param pr The type of message that is being received.
	 */
	public void parse(String in, Prefix pr) {
		//More will be added in this switch statement later...
		switch(pr){
			case CLOSE: close();
				break;
			case STARTTIME:
				String[] timerInfo=in.split("#");
				applet.remaining=500+Long.parseLong(timerInfo[0]);
				applet.roundLength=Long.parseLong(timerInfo[1]);				
				applet.lastUpdate=System.currentTimeMillis();
				applet.curRound=Integer.parseInt(timerInfo[2]);
				applet.totalRnd=Integer.parseInt(timerInfo[3]);
				applet.resume();
				break;
			case WONBID:
				//applet.showResult("Won Bid: " + in);
				//applet.operator.bidResult(new Bid(in));
				applet.displayMessage("Won Bid: " + new Bid(in));
				break;
			case LOSTBID:
				//applet.showResult("Lost Bid: "+ in);
				//applet.operator.bidResult(new Bid(in));
				applet.displayMessage("Lost Bid: " + new Bid(in));
				break;
			case NEWROUND:
				applet.operator.setBankBalance(Integer.parseInt(in));
				applet.resetActionQueue();
				applet.refreshGUI();
				break;
			case BANKBALANCE:
				((ClientOperator) operator).updateBankBalance(Integer.parseInt(in));
				break;
			case GRIDLIMITS:
				String[] xy = in.split("#");
				((ClientOperator) operator).setGridLimits(
						Integer.parseInt(xy[0]),
						Integer.parseInt(xy[1]),
						Integer.parseInt(xy[2]));
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
				applet.operator.seismicRequestResult(in);
				break;
			case OIL:
				applet.operator.oilInfo(in);
				break;
			case GAS:
				applet.operator.gasInfo(in);
				break;
			case ROCK:
				applet.operator.rockInfo(in);
			case DRILLRESULT:
				applet.displayMessage("Drill Results Returned.");
				applet.operator.drillRequestResult(in);
				break;
			case CELL:
				applet.operator.addCellInfo(in);
				break;
			case BIDQUEUE:
				Bid newBid = new Bid(applet.operator,in);
				applet.operator.addQueue(newBid);
				break;
			case SEISMICQUEUE:
				SeismicRequest newS = new SeismicRequest(applet.operator,in);
				applet.operator.addQueue(newS);
				break;
			case DRILLQUEUE:
				Drill newDrill = new Drill(applet.operator,in);
				applet.operator.addQueue(newDrill);
				break;
			case LANDOWNER:
				Bid winBid = new Bid(in);
				applet.operator.bidResult(winBid);
				break;
			case ENDGAME:
				applet.displayMessage(in);
				applet.killAll();
				break;
			case SERVER: 
				JOptionPane.showMessageDialog(null, in, "Server Message",
												JOptionPane.ERROR_MESSAGE);
				break;
			case WARNING: 
				JOptionPane.showMessageDialog(applet.GUI,
					    in,
					    "Server Message",
					    JOptionPane.ERROR_MESSAGE);
				if(in.indexOf("delete") != -1 || in.indexOf("disabled") != -1 )//If "Deleted" is in message--close program
					System.exit(1);
				applet.login.setVisible(true);
				break;
			default: System.out.println("CM Unknown message: " + pr.name() + ": " + in);
				break;
		}
	}
	
	/** Close the connection by updating the UI
	 * and calling super's close method.
	 */
	public boolean close() {
		applet.setIP("None");
		return super.close();
	}
	
	/** Send a login message to the server.
	 * @param name The name of the team/client.
	 */
	public void sendLogin(boolean register, String name, String password) {
		sendMessage(register ? Prefix.REGISTER:Prefix.LOGIN,name + "," + password);
	}

	/** Send an end round request to the server.
	 * Only used in development, not production.
	 */
	public void sendEndRound() {
		sendMessage(Prefix.TEST,"");
	}
	
	/** Send a new bid to the server.
	 * @param bid The bid formatted as a string that is being sent.
	 */
	public void sendNewBid(String bid) {
		sendMessage(Prefix.BID, bid);
	}
	
	/** Send a new seismic request to the server.
	 * @param seismic The seismic request formatted as a string.
	 */
	public void sendNewSeismic(String seismic) {
		sendMessage(Prefix.SEISMICREQUEST, seismic);
	}
	
	/** Send a new drill request to the server.
	 * @param drill The drill request formatted as a string.
	 */
	public void sendNewDrill(String drill) {
		sendMessage(Prefix.DRILLREQUEST, drill);
	}
	
	/** Send a remove bid request to the server.
	 * @param bid The bid to be removed, formatted as a string.
	 */
	public void sendRemoveBid(String bid) {
		sendMessage(Prefix.REMOVEBID, bid);
	}
	
	/** Send a remove seismic request request to the server.
	 * @param seismic The seismic request to be removed, formatted as a string.
	 */
	public void sendRemoveSeismic(String seismic) {
		sendMessage(Prefix.REMOVESEISMIC, seismic);
	}
	
	/** Send a remove drill request to the server.
	 * @param drill The drill request to be removed, formatted as a string.
	 */
	public void sendRemoveDrill(String drill) {
		sendMessage(Prefix.REMOVEDRILL, drill);
	}
	
}
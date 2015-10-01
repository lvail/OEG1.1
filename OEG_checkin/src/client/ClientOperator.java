package client;

import java.awt.Point;
import java.text.DecimalFormat;

import shared.action.*;
import shared.*;

/** Performs Operator tasks specific to the client functionality.
 * In particular, this class creates and removes actions such as Bids
 * and Drill requests. It partly manages the action queue and holds
 * the Grid object for this client that matches the ServerOperator's
 * Grid object on the server.
 */ 
public class ClientOperator extends Operator {

	/** The GUI interface through which GUI objects,
	 * such as message printing and the Map are manipulated.
	 */
	private ClientApplet clientApplet;
	
	
	/** Constructor that stores GUI object and passes socket to the super Operator
	 * @param clientApplet The user interface to display information on.
	 * @param name Team name of this operator.
	 * @param server The socket connection to the server.
	 */
	public ClientOperator(ClientApplet clientApplet, String name, ClientMessages server) {
		super(name, server);
		this.clientApplet = clientApplet;
		this.opGrid = new Grid();
	}
	
	/** Create a new Bid from on the client, test that it is
	 * valid, and send the bid to the server if it is valid.
	 * @param x The x location on the Grid.
	 * @param y The y location on the Grid.
	 * @param amount The $ amount of the bid.
	 * @return True if the Bid is valid.
	 */
	
	public boolean makeNewBid(int x, int y, int amount) {
		Bid bid = new Bid(this, amount,new Point(x,y));
		if(!existsQueue(bid)) {
			if(bid.checkValid(bank.getAdjustedBalance(), opGrid.getDimensions())) {
				((ClientMessages) socket).sendNewBid(bid.toSocket());
				addQueue(bid);
				return true;
			}
		}		
			return false;
	}
	
	/** Add an action to the action queue on the GUI,
	 * removing unusable funds from the bank in the process.
	 * @param in The Action object being added to the queue.
	 */
	public void addQueue(Action in) {
		bank.changeUnusable(in.getCost());
		clientApplet.addActionQueue(in);
	}
	
	/** Check if an Action exists in the action queue on the GUI.
	 * @param in The Action object to check in the queue.
	 * @return True if the Action object exists.
	 */
	public boolean existsQueue(Action in) {
		return clientApplet.checkActionQueue(in);
	}
	
	/**Create a new SeismicRequest on the client, test that it is valid,
	 * and send the request to the server
	 * @param startx Starting x coordinate of the request.
	 * @param starty Starting y coordinate of the request.
	 * @param endx Ending x coordinate of the request.
	 * @param endy Ending y coordinate of the request.
	 * @return True if the request is valid.
	 */
	public boolean makeNewSeismicRequest(int startx,int starty,int endx,int endy) {
		SeismicRequest newRequest =
				new SeismicRequest(this,new Point(startx,starty),new Point(endx,endy));
		if(!existsQueue(newRequest)) {
			if(newRequest.checkValid(opGrid.getDimensions())) {
				((ClientMessages) socket).sendNewSeismic(newRequest.toSocket());
				addQueue(newRequest);
				return true;
			}
		}
		return false;
	}
	
	
	/**Create a new drill on the client, test that it is valid,
	 * and send the request to the server
	 * @param landX Starting x coordinate of the request.
	 * @param landY Starting y coordinate of the request.
	 * @return True if the drill is valid.
	 */
	public boolean makeNewDrill(int landX,int landY) {
		Point p = new Point(landX, landY);
		Drill newDrill =
				new Drill(this,p);
		if(!existsQueue(newDrill)) {
			if(newDrill.checkValid(p)) {
				((ClientMessages) socket).sendNewDrill(newDrill.toSocket());
				addQueue(newDrill);
				return true;
			}	
		}
		return false;
	}
	
	/** Remove a request from the action queue. Called from the GUI action queue,
	 * this method notifies the server that the action must be removed and updates
	 * the unusable funds to reflect the additional money.
	 * @param remove The Action object to remove from the action queue.
	 */
	public void removeRequest(Action remove) {
		/* Since a socket message is being sent, the type of Action object
		must be identified */
		if(remove instanceof Bid) {
			((ClientMessages) socket).sendRemoveBid(((Bid) remove).toSocket());
		} else if(remove instanceof SeismicRequest) {
			((ClientMessages) socket).sendRemoveSeismic(((SeismicRequest) remove).toSocket());
		} else if(remove instanceof Drill) {
			((ClientMessages) socket).sendRemoveDrill(((Drill) remove).toSocket());
		}
		
		/* Update the unusable cash balance */
		bank.changeUnusable(-remove.getCost());
	}
	
	
	/**Temporarily create a SeismicRequest so that a cost can be calculated
	 * and returned to the GUI.
	 * @param startx Starting x coordinate of the request.
	 * @param starty Starting y coordinate of the request.
	 * @param endx Ending x coordinate of the request.
	 * @param endy Ending y coordinate of the request.
	 * @return The $ cost as a string, or invalid
	 * message if an incorrect request.
	 */
	public String getCostSeismic(int startx,int starty,int endx,int endy) {
		SeismicRequest newRequest =
				new SeismicRequest(this,new Point(startx,starty),new Point(endx,endy));
		if(newRequest.checkValid(opGrid.getDimensions())) {
			DecimalFormat myFormatter = new DecimalFormat("$###,###,###");
			String output = myFormatter.format(newRequest.getCost());
			return output;
		}
		else
			return "Invalid Coordinates.";
	}
	
	/**Set the grid size on the client for use in checking valid input.
	 * @param xLimit Maximum x (horizontal-right) value
	 * @param yLimit Maximum y (vertical-down) value
	 * @param numLayers The maximum number of layers in the Grid.
	 */
	public void setGridLimits(int xLimit, int yLimit, int numLayers) {
		Point p = new Point(xLimit, yLimit);
		opGrid.setLimits(p, numLayers);
		clientApplet.setMap(p);
	}
	
	
	/**NOT USED IN PRODUCTION VERSION
	 * Unneeded as of 11-7-2013
	 * Used for testing by the TEST button on the UI to
	 * signal end of round on server.
	 */
	public void endRound() {
		((ClientMessages) socket).sendEndRound();
	}

	/** Receive a cell from the server and set the info in the client's Grid.
	 * @param sockIn The string representation of the cell to be added.
	 */
	public void addCellInfo(String sockIn) {
		opGrid.cellFromSocket(sockIn);
		clientApplet.refreshGUI();
	}
	
}

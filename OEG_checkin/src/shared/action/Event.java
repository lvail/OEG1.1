package shared.action;

import shared.*;

public class Event extends Action {
	/**The description of the Action.
	 */
	private String description;
	
	/**Constructor that forms an Event for a given Owner.
	 * @param owner, the owner causing the event.
	 */
	public Event(Operator owner) {
		super(owner);
	}
	
	/**Sets the Event description to "Login"
	 */
	public void setEventLogin() {
		description = "Login";
	}
	
	/**Sets the Event description to "Logout"
	 */
	public void setEventLogout() {
		description = "Logout";
	}
	
	/**Sets the Event description to "Round #" followed by the current round number.
	 */
	public void setEventNewRound(int curRound) {
		description = "Round #" + curRound;
	}
	
	/**Returns the event formatted as a String.
	 */
	public String toString() {
		return "------\n" + description + " - " + super.toString() + "\n-----";
	}
}

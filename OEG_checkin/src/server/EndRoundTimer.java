package server;

import java.awt.Toolkit;
import java.util.TimerTask;

import shared.Messages;

/** EndRoundTimer handles the end of round actions for the game round timer in
 * OEG. run() method is called by Timer when the timer reaches zero. */
public class EndRoundTimer extends TimerTask {
	/** Director that will be used at end of timer. */
	private Director director;
	/** Toolkit used to give an audible system beep on server */
	private Toolkit toolkit;
	/** Length of the timer */
	private long roundLength;

	/** Constructor that sets the Director that will be notified when a timer
	 * ends, and the length of the timer
	 * @param director Director that will be used at end of timer.
	 * @param roundLength Length of timer, in milliseconds. */
	public EndRoundTimer(Director director, long roundLength) {
		toolkit = Toolkit.getDefaultToolkit();
		this.director = director;
		this.roundLength = roundLength;
	}

	/** Callback method called when the timer ends Initiates all actions that
	 * must be taken at the end of a round. */
	public void run() {
		toolkit.beep();
		director.curRound++;

		// Ending the Game
		System.out.println(director.curRound);
		if(director.curRound>Director.numRounds)
		{
			director.gameRunning=false;
			director.endGame();
		}
		
		// if not in testing mode
		if (!Messages.TESTMODE) {
			if (director.gameRunning) {
				director.endRound();
			}
		}
		
	}

	/** Get the time remaining until the next timer ends
	 * @return The remaining time in milliseconds. */
	public long getTimeRemaining() {
		// Total time of round, minus the amount
		// of time that has passed since the last
		// time the timer went off.
		return roundLength
				- (System.currentTimeMillis() - scheduledExecutionTime());
	}

}

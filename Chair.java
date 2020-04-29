//package project2Concurrent;

public class Chair {
	// chair identifier
	int chairId;
	// output-friendly chair identifier
	String chairName;
	// whether or not a player is sitting
	boolean isFull;
	// current player in chair
	int chairOccupant;

	Chair(int id) {
		// identifier from parameter
		this.chairId = id;
		// chair identifier for output
		this.chairName = "C" + (chairId + 1);
		// chairs start empty
		this.isFull = false;
		// empty chair
		this.chairOccupant = -1;
	}
	
	// fills the chair
	public synchronized boolean sit(int playerId) {
		// if chair is already full, can't sit
		if(isFull == true) {
			return false;
		}
			
		// player has sat
		isFull = true;
		// identifies which player has sat
		chairOccupant = playerId;
		
		return true;
	}
		
	// empties the chair
	public void emptyChair() {
		// player leaves the chair
		isFull = false;
		// chair is empty
		chairOccupant = -1;
	}
}
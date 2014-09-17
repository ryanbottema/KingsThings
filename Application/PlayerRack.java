package KAT;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * Class to represent a Player's rack.
 * Uses the Observer pattern to broadcast changes to the PlayerRackGUI.
 */
public class PlayerRack implements Subject {
    private ArrayList<Piece> piecesList; //list of the pieces on the rack.
    private ArrayList<Observer> observers;
    private Player    owner;

    public PlayerRack() {
        piecesList = new ArrayList<Piece>();
        observers = new ArrayList<Observer>();
    }

    /*
     * Method required by Subject interface.
     * Adds an observer to this class' list of observers.
     */
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    /*
     * Method required by Subject interface.
     * Removes an observer from this class' list of observers.
     */
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    /*
     * Method required by Subject interface.
     * Notifies all observers in it's list of observers that this class has changed.
     */
    public void notifyObservers() {
        observers.get(0).update();
    }

    /*
     * Adds a piece to the rack.
     * Notifies all observers that this class has changed.
     */
    public void addPiece(Piece p) {
        piecesList.add(p);
        p.setOwner(owner);
        PlayerBoard.getInstance().updateNumOnRack(owner);
        notifyObservers();
    }

    /*
     * Adds one or more pieces to the rack.
     * Notifies all observers that this class has changed.
     */
    public void addPieces(ArrayList<Piece> p) {
        for (Piece pc : p) {
            piecesList.add(pc);
            pc.setOwner(owner);
        }
        PlayerBoard.getInstance().updateNumOnRack(owner);
        notifyObservers();
    }

    /*
     * Removes a piece from the rack.
     * Notifies all observers that this class has changed.
     */
    public void removePiece(Piece p) {
        piecesList.remove(p);
        PlayerBoard.getInstance().updateNumOnRack(owner);
        notifyObservers();
    }

    /*
     * Removes one or more pieces from the rack.
     * Notifies all observers that this class has changed.
     */
    public void removePieces(ArrayList<Piece> p) {
        piecesList.removeAll(p);
        PlayerBoard.getInstance().updateNumOnRack(owner);
        notifyObservers();
    }

    /*
     * Removes a piece from the rack at the specified index in the list.
     * Notifies all observers that this class has changed.
     */
    public void removePiece(int i) {
        piecesList.remove(i);
        PlayerBoard.getInstance().updateNumOnRack(owner);
        notifyObservers();
    }

    public ArrayList<Piece> getPieces() { return piecesList; }
    public void setPieces(ArrayList<Piece> p) { piecesList = p; }

    /*
     * Simple convenience method to print the list of pieces more elegantly.
     */
    public static ArrayList<String> printList(ArrayList<Piece> pList) {
        ArrayList<String> newList = new ArrayList<String>();
        for (Piece p : pList)
            newList.add(p.getName());
        return newList;
    }

    public void setOwner(Player p) { 
        owner = p;
        for (Piece pc : piecesList) {
            if (pc.getOwner() == null)
                pc.setOwner(owner);
        }
    }
    
    /**
     * Construct a PlayerRack from a HashMap sent from the server
     * @param map attribute names mapped to attribute values
     */
    public PlayerRack( HashMap<String,Object> map ){
    	owner = NetworkGameLoop.getInstance().getPlayer();
    	@SuppressWarnings("unchecked")
		ArrayList<Integer> pIDs = (ArrayList<Integer>)map.get("pIDs");
    	for( Integer pID : pIDs ){
    		@SuppressWarnings("unchecked")
			HashMap<String,Object> piece = (HashMap<String, Object>) map.get(""+pID);
    		this.addPiece(PieceFactory.createPiece(piece));
    	}
    }

	/**
	 * Convert to a HashMap representation
	 * for sending over the network
	 * @return a map of attribute names to attribute values
	 */
	public HashMap<String,Object> toMap(){
		HashMap<String,Object> map = new HashMap<String,Object>();
		ArrayList<Integer> pIDs = new ArrayList<Integer>();
		for( Piece p : piecesList ){
			pIDs.add(p.getPID());
		}
		map.put("pIDs", pIDs);
		if( owner != null ){
			map.put("owner", owner.getName());
		}
		return map;
	}
		

    public Player getOwner() { return owner; }
    
    public int getNumOnRack() { return piecesList.size(); }
}

 package KAT;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.application.Platform;

public class Player
{
    private static Image yellowMarker, redMarker, blueMarker, greenMarker, blackMarker;
    
    private String username;          		// name used to login
    private PlayerRack playerRack;    		// owned pieces not in play
    private ArrayList<Terrain> hexesPieces; // hexes which contain pieces in play
    private ArrayList<Terrain> hexesOwned;  // hexes which are owned by this player
    private ArrayList<Fort> fortsOwned;		// forts owned by this player
    private String controlMarker;     		// path to control marker image
    private int gold;                 		// player's total earned gold
    private Color color;					// Player color
    private Image marker;					// Image of this players terrain marker
    private int numPieceOnRack;				// Number of pieces player has on the rack
    private int numPieceOnBoard;			// Number of pieces player has on board
    private String colorStr;
    private boolean hasCitadel;
    private boolean wildThing = false;
    

    public Player( String username, String color ){
        this.username = username;
        this.playerRack = new PlayerRack();
        this.hexesPieces = new ArrayList<Terrain>();
        this.hexesOwned = new ArrayList<Terrain>();
        this.fortsOwned = new ArrayList<Fort>();
        this.setColor(color);
        this.gold = 0; // perhaps set to 10 ?
        hasCitadel = false;
    }
    
    // Used for wildThings only
    public Player( String username, String color , boolean b){
        this.username = username;
        this.hexesPieces = new ArrayList<Terrain>();
        this.hexesOwned = new ArrayList<Terrain>();
        this.fortsOwned = new ArrayList<Fort>();
        this.wildThing = b;
        this.setColor(color);
        this.gold = 0; // perhaps set to 10 ?
    }

    public Player( String color ){
        this.username = "User";
        this.playerRack = new PlayerRack();
        this.hexesPieces = new ArrayList<Terrain>();
        this.hexesOwned = new ArrayList<Terrain>();
        this.fortsOwned = new ArrayList<Fort>();
        this.setColor(color);
        this.gold = 0;
        hasCitadel = false;
    }

    /**
     * Adds the specified hex if it is not already
     * owned by this player
     */
    public void addHexOwned( Terrain hex ){
        if( !hexesOwned.contains(hex) ){
        	System.out.println("contains(hex)=true");
        	hexesOwned.add(hex);
            hex.setOwner(this);
        } else {
        	Terrain replace = null;
        	for( Terrain t : hexesOwned ){
        		if( t.compareTo(hex) == 0 ){
        			replace = t;
        		}
        	}
        	if( replace != null ){
        		System.out.println("replacing owned hex");
        		hexesOwned.remove(replace);
        		hexesOwned.add(hex);
        	}
        }
    }
    
    public void addHexPiece( Terrain hex ){
        if( !hexesPieces.contains(hex) ){
        	hexesPieces.add(hex);
        } else {
        	Terrain replace = null;
        	for( Terrain t : hexesPieces ){
        		if( t.compareTo(hex) == 0 ){
        			replace = t;
        		}
        	}
        	if( replace != null ){
        		hexesPieces.remove(replace);
        		hexesPieces.add(hex);
        	}
        }
    }
    
    public void removeHexPiece(Terrain hex) {
    	hexesPieces.remove(hex);
    }
    
    /**
     * Removes ownership of the player's hex
     */
    public void removeHex( Terrain hex ){
    	hexesPieces.remove(hex);
        hex.removeControl(username);
        hex.setOwner(null);
    }
    
    /**
     * Removes ownership of the player's hex
     */
    public void removeHexNoOwner( Terrain hex ){
    	hexesOwned.remove(hex);
        hex.removeControl(username);
        hex.setOwner(null);
    }

    /**
     * Either constructs a tower on a terrain hex 
     * or upgrades an already existing fort
     */
    public void constructFort( Terrain hex ){
    	
    	if (hex.getFort() == null) {
    		hex.setFort(new Fort());
    		hex.getFort().setOwner(this);
    		hex.getFort().setLocation(hex.getCoords());
    		
            HashMap<String,Object> map = new HashMap<String,Object>();
            map.put("updateType", "constructFort");
            map.put("tile", hex.toMap());
            map.put("gold", this.getGold());
            NetworkGameLoop.getInstance().postGameState(map);
    	}
    	else {
            if (hex.getFort().getName().equals("Castle")) {
                if (this.calculateIncome() >= 20 && hasCitadel == false) {
                    hex.getFort().upgrade();
                    HashMap<String,Object> map = new HashMap<String,Object>();
                    map.put("updateType", "upgradeFort");
                    map.put("tile", hex.toMap());
                    map.put("gold", this.getGold());
                    NetworkGameLoop.getInstance().postGameState(map);
                    hasCitadel = true;
                }
                else
                    return;
            }
            else {
    		    hex.getFort().upgrade();
                HashMap<String,Object> map = new HashMap<String,Object>();
                map.put("updateType", "upgradeFort");
                map.put("tile", hex.toMap());
                map.put("gold", this.getGold());
                NetworkGameLoop.getInstance().postGameState(map);
            }
        }
    }

    /*
     * Used for loading a premade game from a text file.
     */
    public void addFort(Terrain hex, Fort f) {
        hex.setFort(f);
        hex.getFort().setOwner(this);
        hex.setFortImage();
        f.setLocation(hex.getCoords());
    }

    /**
     * Adds a piece to the specified hex
     * @return false if there was an error adding the piece
     */
    public boolean playPiece( Piece piece, Terrain hex ){

    	if (piece.getType().equals("Creature")) {
        	if (hex.getContents(username) == null || hex.getContents(username).getStack().size() < 10) {
	    		piece.getPieceNode().setVisible(true);
                // ((Creature)piece).setInPlay(true);

	            if (!hexesPieces.contains(hex))
	                hexesPieces.add(hex);
	            else {
	            	Terrain replace = null;
	            	for( Terrain t : hexesPieces ){
	            		if( t.compareTo(hex) == 0 ){
	            			replace = t;
	            		}
	            	}
	            	hexesPieces.remove(replace);
	            	hexesPieces.add(hex);
	            }

	    		piece.setOwner(this);
	    		piece.flipUp();
	    		hex.addToStack(this.username, piece, false);
	        	numPieceOnBoard++;
	        	PlayerBoard.getInstance().updateNumOnBoard(this);
	        	return true;
        	}
    	}
        else if (piece instanceof SpecialIncome) {
            if (((SpecialIncome)piece).isTreasure()) {
                return true;
            }
            else {
                piece.getPieceNode().setVisible(true);
                piece.setOwner(this);
                hex.addToStack(this.username, piece, false);
                if (!hexesPieces.contains(hex))
                    hexesPieces.add(hex);
            	numPieceOnBoard++;
            	PlayerBoard.getInstance().updateNumOnBoard(this);
                PlayerBoard.getInstance().updateGoldIncomePerTurn(this);
            	return true;
            }
        }
    	else if (piece.getType().equals("Special Character")) {
            if (hex.getContents(username) == null || hex.getContents(username).getStack().size() < 10) {
                piece.getPieceNode().setVisible(true);
                piece.setOwner(this);
                hex.addToStack(this.username, piece, false);
                // ((Creature)piece).setInPlay(true);
                if (!hexesPieces.contains(hex))
                    hexesPieces.add(hex);
                numPieceOnBoard++;
                PlayerBoard.getInstance().updateNumOnBoard(this);
                return true;
            }
        } 
        else if (piece.getType().equals("Random Event")) {
            System.out.println("Played a random event " + piece.getName());
            final Piece thePiece = piece;
            //((RandomEvent)thePiece).performAbility();
            System.out.println(piece.getName() + "'s ability finished");
            return true;
        }
        else
    		return false;
    	
        return false;
    }

    /**
     * Adds and arraylist of pieces to the specified hex
     * and adds the hex to the user's list of owned hexes
     * @return false if there was an error adding a piece
     */ 
    public boolean playPieces( ArrayList<Piece> pieces, Terrain hex ){
        boolean success = true;

        for( Piece piece : pieces ){
            if( playPiece(piece, hex) == false ){
                success = false;
            }
        }

        return success;
    }
    
    public boolean playWildPieces( ArrayList<Piece> pieces, Terrain hex) {
    	boolean success = true;
    	
    	for (Piece p : pieces) {
    		
    		Piece another = p;
    		while (!(another instanceof Creature)) 
    			another = TheCup.getInstance().draw(1).get(0);
			if (playPiece(another, hex) == false)
				success = false;
    	}
    	
    	return success;
    }

    /*
     * Calculates the income of the player.
     * --------------------------------------------------
     * 1 gold per each controlled hex
     * 1 gold per combat value of each controlled fort
     * Value of the Special income counters ON THE BOARD
     * 1 gold per controlled special character
     * --------------------------------------------------
     */
    public int calculateIncome() {
        int income = 0;

        income += getHexesOwned().size();
        for (Terrain hex : hexesOwned) {
            if (hex.getFort() != null) {
                income += hex.getFort().getCombatValue();
            }
        }
        for( Terrain hex : hexesPieces ){
        	if (hex.getContents(username) != null) {

	            for( Piece p : hex.getContents(username).getStack() ){
	                if( p.getType().equals("Special Character") ){
	                    income += 1;
	                } else if (p.getType().equals("Special Income")) {
                        income += ((SpecialIncome)p).getValue();
                    }
	            }
        	}
        }
        return income;
    }

    /*
     * Gets and sets
     */
    public String getName(){ return this.username; }
    public PlayerRack getPlayerRack(){ return this.playerRack; }
    public ArrayList<Terrain> getHexesWithPiece(){ return this.hexesPieces; }
    public ArrayList<Terrain> getHexesOwned(){ return this.hexesOwned; }
    public Color getColor() { return this.color; }
    public int getNumPieceOnBoard() { return numPieceOnBoard; }
    public Image getImage() { return marker; }
    public int getGold(){ return this.gold; }
    public boolean hasaCitadel() { return hasCitadel; }
    public void setCitadel(boolean b) { hasCitadel = b; }

    public boolean isWildThing() { return wildThing; }
    public void setName( String username ){ this.username = username; }
    
    public void addGold( int amount ){ 
    	this.gold += amount;
    }
    public void removeGold(int amount) { 
    	this.gold -= amount; 
    }
    public void minusNumPieceOnBoard() { numPieceOnBoard--; }
    public String getColorStr(){ return this.colorStr; }
    
    /**
     * Removes gold from player's income
     * @return same amount specified if there is enough, else -1
     */
    public int spendGold( int amount ){ 
        if( amount <= gold ){
            this.gold -= amount;
            PlayerBoard.getInstance().updateGold(this);
            return amount;
        } else {
            return -1;
        }
    }
    
    public void setGold( int gold ){
    	this.gold = gold;
    	if( GameLoop.getInstance().getPhase() > 0 ){
    		PlayerBoard.getInstance().updateGold(this);
    	}
    }
    
    public void setColor( String color ){
    	this.colorStr = color;
        switch( color ){
            case "BLUE": 
                marker = blueMarker;
                this.color = Color.BLUE;
                break;
            case "GREEN":
            	marker = greenMarker;
                this.color = Color.GREEN;
                break;
            case "RED":
            	marker = redMarker;
                this.color = Color.RED;
                break;
            case "YELLOW":
            	marker = yellowMarker;
                this.color = Color.YELLOW;
                break;
            case "BLACK":
            	marker = blackMarker;
            	this.color = Color.BLACK;
        }
    }
    
    public static void setClassImages () {
    	yellowMarker = new Image("Images/Control_Yellow.png");
    	greenMarker = new Image("Images/Control_Green.png");
    	blueMarker = new Image("Images/Control_Blue.png");
    	redMarker = new Image("Images/Control_Red.png");
    	blackMarker = new Image("Images/Control_Black.png");
    }   
    
    public void flipAllUp() {
    	for (Terrain t : hexesPieces) 
    		t.getContents(username).flipUp();
    }
    
    public void flipAllDown() {
    	for (Terrain t : hexesPieces) 
    		t.getContents(username).flipDown();
    }
}


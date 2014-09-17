package KAT;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;

import java.util.HashMap;

/*
 * Base class used for representing one of the many board pieces (excluding the terrain tiles)
 */

public abstract class Piece implements Comparable<Piece> {
	
	protected static Image attackingSuccessImg;
	protected static Image attackingFailImg;
	protected static Image chargeAttackDoubleSuccessImg;
	protected static Image chargeAttackOneSuccessImg;
	protected static Image chargeAttackDoubleFailImg;
	protected static Image pieceBack;
	protected static Glow glow;

	protected String    type;
	protected String    front; // path to image for front of piece
	protected String    back;  // path to image for back of piece
	protected String    terrainType;
	protected Player    owner;
    protected boolean showPiece;
    protected Image   imageFront, imageBack;
    protected String  name;
    protected boolean doneMoving;
    protected boolean inPlay;	// Used for things like setting up imageViews etc. No point in doing so if the Creature/Special character has yet to be pulled from cup
    private CreatureStack stackedIn;
    protected Group pieceNode;
    protected Shape pieceSelectBorder, pieceCover, pieceBorderOutline;
    protected ImageView pieceImgV, attackResultImgV;
    private   static Integer uniqueCode = 0; // to identify each piece on the server
    protected Integer pID; // each piece should be given a unique pID 
    protected boolean attackMode;
    protected int chargeAttackSuccess;
    protected Image mouseOverImage;
	protected Coord currentLocation;

	/*
	 * Constructors
	 */
    //Default
	public Piece() {
		glow = new Glow();
		type = "";
		front = "";
		back = "";
        terrainType = "";
        showPiece = false;
        attackMode = false;
//        attackSuccess = false;
        inPlay = false;
        pID = uniqueCode++;
	}
	/*
	 * Additional constructor
	 */
	public Piece(String t, String f, String b, String n) {
		type = t;
		front = f;
		back = b;
		terrainType = "";
		showPiece = false;
		inPlay = false;
		attackMode = false;
//		attackSuccess = false;
		name = n;
		glow = new Glow();
        pID = uniqueCode++;
	}
	
	public Piece( HashMap<String,Object> map ){
		this.type = (String)map.get("type");
		this.front = (String)map.get("fIMG");
		this.back = (String)map.get("bIMG");
		this.name = (String)map.get("name");
		this.terrainType = (String)map.get("terrain");
		Integer id = (Integer)map.get("pID");
		if( id != null ){
			this.pID = id;
		}
		terrainType = "";
		glow = new Glow();
		showPiece = false;
		inPlay = false;
//		attackSuccess = false;
		attackMode = false;
	}

	/*
	 * -------------Get/Set methods
	 */
	public void setName(String s) { name = s; }
	public void setType(String s) { type = s; }
	public void setFront(String s) { front = s; }
	public void setBack(String s) { back = s; }
    public void setTerrain( String s ){ terrainType = s; }
    public void setOwner(Player p) { owner = p; }
    public void setPID(int pID){ this.pID = pID; }
    public void setStackedIn(CreatureStack cs) { stackedIn = cs; }
    public void setBluffing( boolean bluff ){ showPiece = bluff; }
    
    public String getName() { return name; }
	public String getType() { return type; }
	public String getFront() { return front; }
	public String getBack() { return back; }
    public String getTerrain() { return terrainType; }
    public boolean isBluffing(){ return showPiece; }
    public int getPID(){ return pID; }
    public Player getOwner() { return owner; }
    public abstract Group getPieceNode();
    public abstract Image getImage();
    public CreatureStack getStackedIn() { return stackedIn; }
    
    
    /*
     * -------------Instance methods
     */

    public Piece getClassInstance() { return this; }
    protected HashMap<String,Object> toMap(){
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("name", name);
        map.put("type", type);
        map.put("pID", pID);
        map.put("fIMG", front);
        map.put("bIMG", back);
        map.put("orientation", (showPiece == true) ? 1 : false);
        map.put("terrain", terrainType);
        return map;
    }

    /*
     * Method to determine if a piece is playable at a certain stage in the game.
     */
    public boolean isPlayable() {
        return false;
    }

    public void uncover() {
		pieceCover.setVisible(false);
		pieceCover.setDisable(true);
	}

	public void cover() {
		pieceCover.setVisible(true);
		pieceCover.setDisable(false);
	}
	
	public void highLight() {
		pieceSelectBorder.setVisible(true);
	}
	public void unhighLight() {
		pieceSelectBorder.setVisible(false);
	}

	public boolean doneMoving() { return doneMoving; }
	
	public static void setClassImages() {
		attackingSuccessImg = new Image("Images/Attacking_Success.png");
		attackingFailImg = new Image("Images/Attacking_Fail.png");
		chargeAttackDoubleSuccessImg = new Image("Images/Attack_ChargeDoubleSuccess.png");
		chargeAttackOneSuccessImg = new Image("Images/Attack_ChargeOneSuccess.png");
		chargeAttackDoubleFailImg = new Image("Images/Attack_ChargeDoubleFail.png");
		pieceBack = new Image("Images/Creature_Back.png");
		
	}
	
	@Override
	public int compareTo( Piece other ){
		if( other.getPID() == this.getPID() ){
			return 0;
		} else {
			return -1;
		}
    }
		
	public Coord getCurrentLocation() { return currentLocation; }
	public void setCurrentLocation(Coord c) { currentLocation = c; }
	
	// This gets overriden for movables
	public boolean canMoveTo(Terrain from, Terrain to) {
		return false;
	}
	
	public void flipDown() {
		if (this instanceof Creature) {
			if (showPiece) {
				showPiece = false;
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		            	pieceImgV.setImage(pieceBack);
		            }
				});
			}
		}
	}
	public void flipUp() {
		if (this instanceof Creature) {
			if (!showPiece) {
				showPiece = true;
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		            	pieceImgV.setImage(imageFront);
		            }
				});
			}
		}
	}
	
	public boolean isSupported() {
		
		if (this instanceof Creature) {
	    	for (Terrain t : owner.getHexesOwned()) {
	    		if (t.getType().equals(terrainType))
	    			return true;
	    	}
	    	for (TerrainLord tl : stackedIn.getTerrainLords()) {
	    		if (tl.getTerrain().equals(terrainType))
	    			return true;
	    	}
	    	return false;
		}
		return true;
    }
	
}

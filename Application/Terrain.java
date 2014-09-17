package KAT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.effect.GlowBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

/*
 * Terrain class
 */
public class Terrain implements Comparable<Terrain> {
    
    private static Image baseTileImageDesert, baseTileImageForest, baseTileImageFrozenWaste, baseTileImageJungle, baseTileImageMountain, baseTileImagePlains, baseTileImageSea, baseTileImageSwamp, baseTileImageUpsideDown;
    private static String imageSet = "01"; // Was trying different images, this will be removed in future.
    private static double sideLength;
    private static double height;
    private static double width;
	private static Group selectAnimView;
	private static boolean displayAnim;		// true will show movement animations, false will not
	private static Hex staticHexClip;
	private static Glow glow;
	private static Shape selectHex;
    
    private String type;
    private boolean occupied; 			//True if another player owns it, otherwise false
    private boolean showTile; 			// Upside down or not
    private Image tileImage;
    private Coord coord;
    private Group hexNode;
    private Hex hexClip;
    private ImageView tileImgV;
    private int moveCost;
    private Shape battleHex;			// Shape used to indicate battle (Red hex)
    private boolean explored;

    private HashMap<String, CreatureStack> contents; // map of usernames to pieces (Creatures)
    
    private ImageView fortImgV;
    private Fort fort;
    
    private Player owner;
    private ImageView ownerMarkerImgV;
    
    private Rectangle cover;
    
    /*
     * Constructors:
     */
    
    public Terrain(String t) {
    	setType(t);
    	showTile = false;
    	explored = false;
        occupied = false;
        displayAnim = true;
        tileImgV = new ImageView();
        
        contents = new HashMap<String,CreatureStack>();
        
//        stackNodes = new HashMap<String, Group>();
//        stacksImgV = new HashMap<String, ImageView>();
//        stacksRec = new HashMap<String, Rectangle>();

        hexClip = new Hex(sideLength * Math.sqrt(3), true);
        hexNode = GroupBuilder.create()
//        		.clip(hexClip)
        		.children(tileImgV)
        		.build();
        
        setTileImage();
        setupEvents();
//		setupStackImageViews();
		setupMarkerImageView();
		setupFortImageView();
		setupCover();
		setupBattleAnim();
    }
    
    public Terrain( HashMap<String,Object> map ){
        this((String)map.get("terrain"));
    	showTile = ((Integer)map.get("tile_orient") == 1) ? true : false;
        String owner = (String)map.get("owner");
        int x = (Integer)map.get("x");
        int y = (Integer)map.get("y");
        int z = (Integer)map.get("z");
        setCoords(new Coord(x, y, z));
        setExplored(true);

        // check for owner
        if( owner != null && !owner.equals("0") ){
            this.occupied = true;
            Player[] players = NetworkGameLoop.getInstance().getPlayers();
            for( Player p : players ){
                if( p.getName().equals(owner) ){
                    setOwner(p);
                    p.addHexOwned(this);
                    break;
                }
            }
        }
    	
        // check for all players and their creatures
        @SuppressWarnings("unchecked")
        ArrayList<String> players = (ArrayList<String>)map.get("players");
        if( players != null ){
            for( String name : players ){       
                @SuppressWarnings("unchecked")
                ArrayList<Integer> pIDs = (ArrayList<Integer>)map.get(name);
                for( Integer pID : pIDs ){
                    HashMap<String,Object> p = (HashMap<String,Object>)map.get(""+pID);
                    Piece piece = PieceFactory.createPiece(p);
                    Integer piece_orient = (Integer)map.get("piece_orient");
                    boolean bluff = false;
                    if( piece_orient != null ){
                    	bluff = (piece_orient == 1) ? false : true;
                    }
                    //addToStack(name, piece, bluff); // TODO implement bluffing
                    for( Player pl : NetworkGameLoop.getInstance().getPlayers() ){
                    	if( pl.getName().equals(name) ){
                    		if( piece != null )
                    			pl.playPiece(piece, this);
                    	}
                    }
                }
            }
        }
        
        // check for forts
        @SuppressWarnings("unchecked")
		HashMap<String,Object> fort = (HashMap<String,Object>)map.get("fort");
        if( fort != null ){
        	setFort(new Fort(fort));
        	setFortImage();
        	this.fort.setOwner(this.owner);
        }
        
        // update GUI
        setTileImage();
        setupEvents();
		setupMarkerImageView();
		setupFortImageView();
		setupCover();
		setupBattleAnim();
    }

    public HashMap<String,Object> toMap(){
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("terrain", type);
        map.put("tile_orient", showTile ? 1 : 0);
        map.put("owner", (owner == null) ? "0" : owner.getName());
        map.put("x", coord.getX());
        map.put("y", coord.getY());
        map.put("z", coord.getZ());
        

        ArrayList<String> players = new ArrayList<String>();
        for( String player : contents.keySet() ){
            players.add(player);
            CreatureStack cStack = contents.get(player);
            ArrayList<Piece> pieces = cStack.getStack();
            ArrayList<Integer> pIDs = new ArrayList<Integer>();
            for( Piece p : pieces ){
                pIDs.add(p.getPID());
                // prob dont need to add actual piece
                map.put(""+p.getPID(), p.toMap());
                map.put("piece_orient", p.isBluffing() ? 1 : 0);
            }
            map.put(player, pIDs);
        }
        map.put("players", players);
        return map;
    }
    
    /* 
     * Get/Set methods
     */
    public boolean isExplored() { return explored; }
    public boolean isOccupied() { return occupied; }
    public String getType() { return type; }
    public Image getImage() { return tileImage; }
    public Group getNode() { return hexNode; }
    public Coord getCoords() { return coord; }
    public Fort getFort() { return fort; }
    public int getMoveCost() { return moveCost; }
    
    public void setFort(Fort f) { fort = f; }
    public void setExplored(boolean b) { explored = b; }

    /**
     * @return a map of usernames to an arraylist of their pieces
     */
    public HashMap<String,CreatureStack> getContents() { return contents; }
    
    /**
     * @return an arraylist of pieces owned by a user
     */
    public CreatureStack getContents( String username ){ return contents.get(username); }
    
    public void setOccupied(boolean b) { occupied = b; }
    public void removeControl(String username) {
    	contents.remove(username);
    	ownerMarkerImgV.setImage(null);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	ownerMarkerImgV.setVisible(false);
            }
        });
    	occupied = false;
    }

    public Player getOwner() { return owner; }

    public void setOwner(Player p) { 
    	owner = p; 
    	occupied = true;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	if (owner != null) {
            		ownerMarkerImgV.setImage(owner.getImage());
            		ownerMarkerImgV.setVisible(true);
            	} else
            		ownerMarkerImgV.setImage(null);
            }
        });
        try { Thread.sleep(50); } catch( Exception e ){ return; }
    }

    public void setType(String s) { 
    	this.type = s.toUpperCase();
    	switch (type) {
    	case "DESERT":
    		tileImage = baseTileImageDesert;
        	moveCost = 1;
    		break;
    	case "FOREST":
    		tileImage = baseTileImageForest;
    		moveCost = 2;
    		break;
    	case "FROZENWASTE":
    		tileImage = baseTileImageFrozenWaste;
        	moveCost = 1;
    		break;
    	case "JUNGLE":
    		tileImage = baseTileImageJungle;
    		moveCost = 2;
    		break;
    	case "MOUNTAINS":
    		tileImage = baseTileImageMountain;
    		moveCost = 2;
    		break;
    	case "PLAINS":
    		tileImage = baseTileImagePlains;
        	moveCost = 1;
    		break;
    	case "SEA":
    		tileImage = baseTileImageSea;
        	moveCost = 1;
    		break;
    	case "SWAMP":
    		tileImage = baseTileImageSwamp;
    		moveCost = 2;
    		break;
    	default: 
    		type = null;
        	moveCost = 5;
    		tileImage = baseTileImageUpsideDown;
    		break;
    	}
    }
    public void setShowTile(boolean s) { showTile = s; }
    public void setTileImage() {
    	
    	if (showTile)
    		tileImgV.setImage(tileImage);
    	else
    		tileImgV.setImage(baseTileImageUpsideDown);
    	tileImgV.setFitHeight(height * 1.01); // 1.01 to compensate for images not overlapping properly
    	tileImgV.setPreserveRatio(true);
    }
     
    public void setFortImage() {
    	if (fort != null) {
    		Platform.runLater(new Runnable() {
                @Override
                public void run() {
            		fortImgV.setImage(fort.getImage());
                }
            });
    	}
    }
    public void setCoords(Coord xyz) { coord = xyz; }
    public void setClip() { hexNode.setClip(hexClip); }
    
    /*
     * - Loads the images for each type of terrain
     * - Also creates a hexClip so that the cover on each terrain is displayed properly
     * - Calculates dimensions
     * - builds static Glow effect and calls setupAnim
     */
    public static void setClassImages() {
    	baseTileImageDesert = new Image("Images/Hex_desert_" + imageSet + ".png");
    	baseTileImageForest = new Image("Images/Hex_forest_" + imageSet + ".png");
    	baseTileImageFrozenWaste = new Image("Images/Hex_frozenwaste_" + imageSet + ".png");
    	baseTileImageJungle = new Image("Images/Hex_jungle_" + imageSet + ".png");
    	baseTileImageMountain = new Image("Images/Hex_mountains_" + imageSet + ".png");
    	baseTileImagePlains = new Image("Images/Hex_plains_" + imageSet + ".png");
    	baseTileImageSea = new Image("Images/Hex_sea_" + imageSet + ".png");
    	baseTileImageSwamp = new Image("Images/Hex_swamp_" + imageSet + ".png");
    	baseTileImageUpsideDown = new Image("Images/Hex_upsidedown_" + imageSet + ".png");
        staticHexClip = new Hex(sideLength * Math.sqrt(3), true);
        height = staticHexClip.getHeightNeeded();
        width = staticHexClip.getWidthNeeded();
    	glow = GlowBuilder.create().build();
		setupAnim();
    }
    public static void setSideLength(double sl) { sideLength = sl; }
   
    public Terrain positionNode(Group bn, double xoff, double yoff) {
    	
    	// Move each hex to the correct position
    	// Returns itself so this can be used in line when populating the game board (see Board.populateGameBoard())
    	hexNode.relocate(1.5 * hexClip.getSideLength() * (coord.getX() + 3) - xoff, - yoff + (6 - coord.getY() + coord.getZ()) * sideLength * Math.sqrt(3)/2 + (Math.sqrt(3)*sideLength)/6);
    	setTileImage();
    	bn.getChildren().add(0, hexNode);
    	return this;
    }
        
    /*
     * The fucntion called when a tile is clicked on
     */
    private void clicked() {
    	System.out.println("TILE POSITION: " + "[" + this.getNode().localToScene(0.0, 0.0) + "," + this.getNode().localToScene(0.0, 0.0) + "]");
        ClickObserver.getInstance().setClickedTerrain(this);
        ClickObserver.getInstance().whenTerrainClicked();
    }
    
    /*
     * On Click: 		calls function clicked() which registers with ClickObserver and calls whenClicked
     * On MouseEntered: Makes the terrain 'glow' with a glow effect
     * On MouseExited: 	Removes the glow effect
     */
    private void setupEvents() { 
    	
    	//terrain is clicked
    	tileImgV.setOnMouseClicked(new EventHandler(){
			@Override
			public void handle(Event event) {
				clicked();
			}
		});
    	tileImgV.setOnMouseEntered(new EventHandler(){
			@Override
			public void handle(Event event) {
				tileImgV.setEffect(glow);
			}
		});
    	tileImgV.setOnMouseExited(new EventHandler(){
			@Override
			public void handle(Event event) {
				tileImgV.setEffect(null);
			}
		});
    }
    
    /*
     * Sets up the GUI for: 
     * - Animation used for a selected terrain (The white hex that pulses in opacity)
     * - Animation used for a battle terrain (The red hex that pulses in opacity)
     */
    private static void setupAnim() {
		
		Hex smallHole = new Hex(height * 0.8, true);
		smallHole.relocate(width/2 - smallHole.getWidthNeeded()/2, height/2 - smallHole.getHeightNeeded()/2);
		selectHex = Path.subtract(staticHexClip, smallHole);
		
		Shape donutHex = selectHex;
		donutHex.setFill(Color.WHITESMOKE);
			
		donutHex.setEffect(new GaussianBlur());
		selectAnimView = GroupBuilder.create()
				.children(donutHex)
				.build();
		
    	final Animation tileSelected = new Transition() {
    	     {
    	         setCycleDuration(Duration.millis(1700));
    	         setCycleCount(INDEFINITE);
    	         setAutoReverse(true);
    	     }
    	     protected void interpolate(double frac) { selectAnimView.setOpacity(frac * 0.8); }
    	};
    	tileSelected.play(); 
	}

    // Moves the selection animation to the clicked terrain
    public void moveAnim () {
    	if (!hexNode.getChildren().contains(selectAnimView))
    		hexNode.getChildren().add(selectAnimView);
    }
    
    /*
     * For added fun, a red border around a battle hex
     */
    private void setupBattleAnim() {
    	final Animation battleHere;
		
		Hex smallHole = new Hex(height * 0.8, true);
		smallHole.relocate(width/2 - smallHole.getWidthNeeded()/2, height/2 - smallHole.getHeightNeeded()/2);
		battleHex = Path.subtract(staticHexClip, smallHole);
		battleHex.setFill(Color.TOMATO);
		battleHex.setEffect(new GaussianBlur());
		
		battleHere = new Transition() {
       	    {
       	        setCycleDuration(Duration.millis(500));
       	        setCycleCount(INDEFINITE);
       	        setAutoReverse(true);
       	    }
       	    protected void interpolate(double frac) { battleHex.setOpacity(frac * 0.8); }
		};

		battleHere.play(); 
    }
    
    // Adds the battle marker to terrain
    public void addBattleHex() {
    	if (!hexNode.getChildren().contains(battleHex))
    		hexNode.getChildren().add(battleHex);
    	if (!GameLoop.getInstance().getBattleGrounds().contains(this.coord))
    		GameLoop.getInstance().getBattleGrounds().add(this.coord);
    }
    
    // Removes battle hex
    public void removeBattleHex() {
		hexNode.getChildren().remove(battleHex);
    }
    
    // All these setup methods setup GUI things. Might merge soon
    private void setupMarkerImageView() {
    	ownerMarkerImgV = ImageViewBuilder.create()
    			.fitHeight(height*19/83)
    			.preserveRatio(true)
    			.mouseTransparent(true)
    			.build();
    	ownerMarkerImgV.relocate(width*0.2, height * 0.99 - height*19/83);
    	hexNode.getChildren().add(ownerMarkerImgV);
    }
    
    private void setupFortImageView() {
    	fortImgV = ImageViewBuilder.create()
    			.fitHeight(height*19/83)
    			.preserveRatio(true)
    			.mouseTransparent(true)
    			.build();
    	fortImgV.relocate(width*0.6, height*0.99 - height*19/83);
    	hexNode.getChildren().add(fortImgV);
    }

    private void setupCover() {
    	cover = RectangleBuilder.create()
    			.height(height)
    			.width(width)
    			.opacity(0.5)
    			.fill(Color.DARKSLATEGRAY)
    			.disable(true)
    			.visible(false)
    			.build();
    	hexNode.getChildren().add(cover);
    }
    
    // Just a calculation method. Finds the (x,y) position within the node for the stack based on how many are in the terrain
    public double[] findPositionForStack(int i) {
    	double offset = 8;
    	double centerX = width/2;
    	double centerY = height/2;
    	double stackHeight = CreatureStack.getWidth() + offset;
    	double x = 0, y = 0;
    	switch (contents.size()) {
			case 1:
				x = centerX - stackHeight/2 + offset/2;
				y = centerY - stackHeight/2;
				break;
			case 2:
				x = centerX + (i-1) * stackHeight + offset/2;
				y = centerY - stackHeight/2;
				break;
			case 3:
				x = centerX - ((double)Math.round(0.51/(i+1))/2 + (i%2))  * stackHeight + offset/2;
				y = centerY + ((double)Math.round((i + 0.1)/2) - 1) * stackHeight - centerY * 0.06;
				break;
			case 4:
				x = centerX - ((i+1)%2 * stackHeight) + offset/2;
				y = centerY + (Math.floor(i/2) - 1) * stackHeight - centerY * 0.06;
				break;
			case 5:
				x = centerX + (i-3) * stackHeight/2 + offset/2;
				y = centerY + (i%2 - 1) * stackHeight - centerY * 0.06 - stackHeight * 0.5;
				break;
			default:
				System.out.println("Case 0!");
				break;
    	}
    	return new double[]{x, y};
    }
    
    /*
     * Adds a creature to a stack.
     * If no stack is in this Terrain, then a new stack is created.
     */
    public void addToStack(String player, Piece c, boolean secretly) {
    	if( contents.get(player) != null ){
    		for( Piece p : contents.get(player).getStack() ){
    			if( c.getPID() == p.getPID() ){
    				return;
    			}
    		}
    		if( contents.get(player).getStack().contains(c) ){
    			return;
    		}
    	}
    	
    	int numOfPrev = contents.size();

    	// If the stack does not exist on the terrain yet, create a new stack at the proper position
    	if (contents.get(player) == null || contents.get(player).isEmpty()) {

    		CreatureStack newStack = new CreatureStack(player, coord);
    		contents.put(player, newStack);
    		if( c.getOwner() != null ) c.getOwner().addHexPiece(this);
    		hexNode.getChildren().add(contents.get(player).getCreatureNode());
    		numOfPrev = 0;
    		int j = 0;
	    	Iterator<String> keySetIterator = contents.keySet().iterator();
	    	while(keySetIterator.hasNext()) {
	    		String key = keySetIterator.next();
	    		if (key.equals(player)) {
	    			System.out.println("break");
	    			break;
	    		}
	    		j++;
	    	}
	    	newStack.getCreatureNode().setTranslateX(findPositionForStack(j)[0]);
			newStack.getCreatureNode().setTranslateY(findPositionForStack(j)[1]);
			
			// If player moved on to empty enemy ground
			if (contents.size() == 1 && fort == null && owner != null && !owner.getName().equals(player)) {
				owner.removeHex(this);
				c.getOwner().addHexOwned(this);
    		}	
			
			// If the player moved onto a hex with another players fort
			if (this.getFort() != null && !this.getFort().getOwner().getName().equals(player)) {
				addBattleHex();
			}
    	}
    	
    	// Makes stack instantly visible if in setup mode (ie, piece played from rack). Or a wildThings stack
    	if (GameLoop.getInstance().getPhase() <= 0 || GameLoop.getInstance().getPhase() == 2 || GameLoop.getInstance().getPhase() == 3 || player.equals(GameLoop.getInstance().getWildThings().getName())) 
    		contents.get(player).getCreatureNode().setVisible(true);
    	
    	// Add the creature to the stack
    	if (!secretly) {
    		if (c instanceof Creature)
                contents.get(player).addCreature((Creature)c);
            else if (c.getType().equals("Special Income"))
                contents.get(player).addIncome((SpecialIncome)c);
        }
    	else
    		contents.get(player).addCreatureNoUpdate((Creature)c);

    	if (numOfPrev != contents.size()) {
	    	int i = 0;
	    	Iterator<String> keySetIterator = contents.keySet().iterator();
	    	while(keySetIterator.hasNext()) {
	    		String key = keySetIterator.next();
				
				if (displayAnim && !(i == contents.size() - 1 && numOfPrev < contents.size())) {
					contents.get(key).moveWithinTerrain(findPositionForStack(i)[0], findPositionForStack(i)[1]);
				} else {
					contents.get(key).getCreatureNode().setTranslateX(findPositionForStack(i)[0]);
					contents.get(key).getCreatureNode().setTranslateY(findPositionForStack(i)[1]);
				}
				i++;
	    	}
			
		}

    	if (contents.size() > 1) 
        	addBattleHex();
    	
    	// If terrain has yet to be explored
		if (!explored) {
			addBattleHex();
			GameLoop.getInstance().getWildThings().addHexOwned(this);
		}
		
    	if( GameLoop.getInstance().isNetworked() ){
    		if( ClickObserver.getInstance().getTerrainFlag().equals("RecruitingThings: PlaceThings") ){
	    		HashMap<String,Object> map = new HashMap<String,Object>();
	    		map.put("updateType", "addPieceToTile");
	    		map.put("tile", this.toMap());
	    		map.put("pID", c.getPID());
	    		NetworkGameLoop.getInstance().postGameState(map);
    		}
    	}
    }
    
    // Removes a single creature from a stack.
    public Creature removeFromStack(String player, Creature c) {
    	contents.get(player).removeCreature(c);
    	
    	if( GameLoop.getInstance().isNetworked() ){
    		if( !ClickObserver.getInstance().getTerrainFlag().equals("Movement: SelectMoveSpot") 
    		&&  !ClickObserver.getInstance().getCreatureFlag().equals("Movement: SelectMovers") ){
	    		HashMap<String,Object> map = new HashMap<String,Object>();
	    		map.put("updateType", "removePieceFromTile");
	    		map.put("tile", this.toMap());
	    		map.put("pID", c.getPID());
	    		NetworkGameLoop.getInstance().postGameState(map);
    		}
    	}
    	
    	return c;
    }
    
    public Creature removeFromStackViaCombat(final String player, Creature c ) {
    	contents.get(player).removeCreature(c);
    	Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	clearTerrainHMViaCombat(player);
            }
    	});
		try { Thread.sleep(100); } catch( Exception e ){ return c; }  
    	return c;
    }
    
    public void clearTerrainHMViaCombat(final String player) {
    	
    	if (contents.get(player).getStack().isEmpty()) {
    		
        	hexNode.getChildren().remove(contents.get(player).getCreatureNode());
    		contents.get(player).getOwner().removeHexPiece(this);
    		contents.remove(player);
    		int i = 0;
	    	Iterator<String> keySetIterator = contents.keySet().iterator();
	    	while(keySetIterator.hasNext()) {
	    		String key = keySetIterator.next();
				
				if (displayAnim) {
					contents.get(key).moveWithinTerrain(findPositionForStack(i)[0], findPositionForStack(i)[1]);
				} else {
					contents.get(key).getCreatureNode().setTranslateX(findPositionForStack(i)[0]);
					contents.get(key).getCreatureNode().setTranslateY(findPositionForStack(i)[1]);
				}
				i++;
	    	}
    	}
    	
    	if (contents.size() <= 1) {
    		removeBattleHex();
    	}
    	// If there is still a stack on this terrain that is different from the one just removed, but
    	// 		there is a fort of the player who just lost a stack
    	if (this.getFort() != null && contents.size() > 0 && !this.getFort().getOwner().getName().equals(player)) {
			addBattleHex();
		}
    }
    
    // If the player has no creatures on this tile, the key-value entry is removed
    public void clearTerrainHM(final String player) {
    	if (contents.get(player).getStack().isEmpty()) {
    		
        	hexNode.getChildren().remove(contents.get(player).getCreatureNode());
    		contents.get(player).getOwner().removeHexPiece(this);
    		contents.remove(player);
    		int i = 0;
	    	Iterator<String> keySetIterator = contents.keySet().iterator();
	    	while(keySetIterator.hasNext()) {
	    		String key = keySetIterator.next();
				
				if (displayAnim) {
					contents.get(key).moveWithinTerrain(findPositionForStack(i)[0], findPositionForStack(i)[1]);
				} else {
					contents.get(key).getCreatureNode().setTranslateX(findPositionForStack(i)[0]);
					contents.get(key).getCreatureNode().setTranslateY(findPositionForStack(i)[1]);
				}
				i++;
	    	}
    	}
    	
    	if (contents.size() <= 1) {
    		removeBattleHex();
    	}
    	// If there is still a stack on this terrain that is different from the one just removed, but
    	// 		there is a fort of the player who just lost a stack
    	if (this.getFort() != null && contents.size() > 0 && !this.getFort().getOwner().getName().equals(player)) {
			addBattleHex();
		}
    }
    
    // Moves a stack from another terrain to this one
    // *Note: Moves happen like so: First the stack is added to the destination terrain (And set to not visible). Then 
    //     	the board animates the stack moving, and when the animation is done, the board deletes the node that was used for
    //		moving, and sets the original to visible. This way the stack can be accessed right away (before animation is done, 
    //		preventing null pointers)
    public int moveStack(Terrain from){
    	int numMoved = 0;
    	int numOfPrev = contents.size();
    	String activePlayer = ClickObserver.getInstance().getActivePlayer().getName();
    	ArrayList<Integer> pIDs = new ArrayList<Integer>();
    	
    	System.out.println("----------------------------------------------------------------");
    	System.out.println("from: " + from);
    	System.out.println("from.getContents(activePlayer):   " + from.getContents(activePlayer));
    	System.out.println("from.getContents(activePlayer).getStack() \n\n  " + from.getContents(activePlayer).getStack());
    	System.out.println("\n\nfrom.getContents(activePlayer).filterCreatures(from.getContents(activePlayer).getStack())  \n\n" + from.getContents(activePlayer).filterCreatures(from.getContents(activePlayer).getStack()));
    	System.out.println("----------------------------------------------------------------");
    	System.out.println("----------------------------------------------------------------");
    	
    	for (int i = from.getContents(activePlayer).filterCreatures(from.getContents(activePlayer).getStack()).size() - 1; i >= 0 ; i--) {
            if (from.getContents(activePlayer).filterCreatures(from.getContents(activePlayer).getStack()).get(i).isAboutToMove()) {
    		    Creature mover = from.removeFromStack(activePlayer, from.getContents(activePlayer).filterCreatures(from.getContents(activePlayer).getStack()).get(i));
    		    mover.setAboutToMove(false);
    		    mover.setRecBorder(false);
    		    mover.move(this);
    		    pIDs.add(mover.getPID());	
    		    addToStack(activePlayer, mover, true);
    		    numMoved++;
    		}
        }
    	if (numOfPrev == 0)
    		contents.get(activePlayer).getCreatureNode().setVisible(false);
		InfoPanel.showTileInfo(from);
    	ClickObserver.getInstance().setTerrainFlag("");
    	
    	if( GameLoop.getInstance().isNetworked() ){
    		HashMap<String,Object> map = new HashMap<String,Object>();
    		map.put("updateType", "moveArmies");
    		map.put("fromTile", from.toMap());
    		map.put("toTile", this.toMap());
    		map.put("pIDs", pIDs);
    		NetworkGameLoop.getInstance().postGameState(map);
    	}
    	
    	return numMoved;
    }
    
    /**
     * Implemented from Comparable interface
     * @return distance to other hex, i.e. 0 if equal, 1 if adjacent or >1
     */
	@Override
	public int compareTo(Terrain other) {
		return this.coord.compareTo(other.getCoords());
	}
	public int compareTo(Coord coord) {
		return this.coord.compareTo(coord);		
	}
	
	// Counts the number of creatures about to move. Used for moving between terrains
	public int countMovers(String player) {
		int movers = 0;
		for (int i = 0; i < contents.get(player).filterCreatures(contents.get(player).getStack()).size(); i++) {
            if (contents.get(player).filterCreatures(contents.get(player).getStack()).get(i).isAboutToMove())
			    movers++;
		}
		return movers;
	}
	
	// These two set the rectangle node that covers and prevents mouse clicks
	public void cover() {
		cover.setVisible(true);
		cover.setDisable(false);
	}
	public void uncover() {
		cover.setVisible(false);
		cover.setDisable(true);
	}
	
	@Override
	public String toString() {
		return "Terrain:\nType: " + type + 
				"\nCoord: " + coord + 
				"\nOccupied: " + occupied + ((occupied) ? " by "+owner.getName() : "") +
				"\nCovered: " + cover.isVisible() + 
				"\nShowTile: "+showTile;
	}
	
	// Covers all pieces on this rack
	public void coverPieces() {
		
		coverFort();
		// cover special incomes TODO
		
		Iterator<String> keySetIterator = contents.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		String key = keySetIterator.next();
    		
    		contents.get(key).applyCovers();
    	}
	}
	
	public void uncoverPieces(String name) {
		if (contents.get(name) != null) {
			for (Piece p : contents.get(name).getStack()) {
				p.uncover();
			}
		}
		if (owner.getName().equals(name))
			uncoverFort();
		// TODO uncover special income

	}
	
	public void coverFort() {
		if (fort != null) {
			fort.cover();
		}
	}
	public void uncoverFort() {
		if (fort != null) {
			fort.uncover();
		}
	}
	
	public void removeFort() {
		if (fort != null)
			Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	hexNode.getChildren().remove(fortImgV);
                }
			});
		fort = null;
	}
	
	// Flips all down
	public void flipPiecesDown() {
		Iterator<String> keySetIterator = contents.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		String key = keySetIterator.next();
    		
    		if (!key.equals(GameLoop.getInstance().getWildThings().getName()))
    			contents.get(key).flipDown();
    	}
	}
	
	// Flips all off.. I mean up
	public void flipPiecesUp() {
		Iterator<String> keySetIterator = contents.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		String key = keySetIterator.next();
    		
    		contents.get(key).flipUp();
    	}
	}
	
	//Flip all down that are not of player p, whos are flipped up
	public void flipUpPlayerDownOthers(Player p) {
		Iterator<String> keySetIterator = contents.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		String key = keySetIterator.next();
    		
    		if (key.equals(p.getName()) || key.equals(GameLoop.getInstance().getWildThings().getName()))
    			contents.get(key).flipUp();
    		else
    			contents.get(key).flipDown();
    	}
	}
}

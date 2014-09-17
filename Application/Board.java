package KAT;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.animation.PathTransitionBuilder;
import javafx.animation.Transition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.RadialGradientBuilder;
import javafx.scene.paint.Stop;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

public class Board {
	
	private static HashMap <Coord, Terrain> terrains;
	private boolean showTiles; //Tiles upside down or not?
	private static Coord[] coordList;

	private static int boardAnimCount;
	private static double smallHexSideLength;
	private static double height;
	private static PathTransition pathTransition;
	private static Hex smallHexClip;
	private static Group nodePT;		// The node that shows the stacks moving from one terrain to another
	private static Group boardNode;
	private static boolean removingBadAdjWaters;
	
	
	/*
	 * Constructors
	 */
 	public Board(BorderPane bp) {
 		
 		System.out.println(height);
 		nodePT =  new Group();
 		nodePT.setDisable(true);
		showTiles = false;
		removingBadAdjWaters = false;
		terrains = new HashMap <Coord, Terrain>();
		showTiles = false;
		boardAnimCount = 0;
		coordList = new Coord[]{
				new Coord(0, 0, 0),
				new Coord(0, 1, -1),new Coord(1, 0, -1),new Coord(1, -1, 0),new Coord(0, -1, 1),new Coord(-1, 0, 1),new Coord(-1, 1, 0),
				new Coord(-1, 2, -1),new Coord(0, 2, -2),new Coord(1, 1, -2),new Coord(2, 0, -2),new Coord(2, -1, -1),new Coord(2, -2, 0),new Coord(1, -2, 1),new Coord(0, -2, 2),new Coord(-1, -1, 2),new Coord(-2, 0, 2),new Coord(-2, 1, 1),new Coord(-2, 2, 0),
				new Coord(-2, 3, -1),new Coord(-1, 3, -2),new Coord(0, 3, -3),new Coord(1, 2, -3),new Coord(2, 1, -3),new Coord(3, 0, -3),new Coord(3, -1, -2),new Coord(3, -2, -1),new Coord(3, -3, 0),new Coord(2, -3, 1),new Coord(1, -3, 2),new Coord(0, -3, 3),new Coord(-1, -2, 3),new Coord(-2, -1, 3),new Coord(-3, 0, 3),new Coord(-3, 1, 2),new Coord(-3, 2, 1),new Coord(-3, 3, 0)
		};

		bp.getChildren().add(boardNode);
		
	}
	
	/*
	 * getters and setters
	 */
 	public static HashMap<Coord, Terrain> getTerrains() { return terrains; }
	public static double getHeight() { return height; }
	
	public static void setTerrains(HashMap<Coord, Terrain> _terrains) { 
		terrains = _terrains;
		updateBoardGUI();
	}
	
	/*
	 * Creates the hex shapes used for clipping for the board pane,
	 * Calculates the child hexes size (terrain peices)
	 * Sets up the hex shaped animation for a selected tile
	 */
	public static void generateHexes() {

 		height = Game.getHeight() * 0.96;
		// Set up large hex that defines the board:
		boardNode = GroupBuilder.create()
				.layoutX(Game.getWidth() * 0.3)
				.layoutY((Game.getHeight() - height)/2)
				.build();
		
		// Calculate small hex size
		smallHexSideLength = (height * 3)/(Math.sqrt(3)*22);
		Terrain.setSideLength(smallHexSideLength);
		smallHexClip = new Hex(smallHexSideLength * Math.sqrt(3), true);

	}

    public static void setTerrainCoords(){
        for( int i=0; i<37; i++ ){
				TileDeck.getInstance();
				terrains.put(coordList[i], 
                        TileDeck.getInstance().getNoRemove(TileDeck.getDeckSize() - i - 1));
				terrains.get(coordList[i]).setCoords(coordList[i]);
        }
//        updateBoardGUI();
    }
    
    /*  ^^  called here maybe? ^^
     * 
     * Update the GUI of all the board pieces
     * 
     *  This says nothing about the following:
     *  
     *  	- Creature stacks
     *  	- Player markers
     *  	- Forts
     *  	- If the terrain is covered or not
     *  	- If the terrain is selected/ battle hexes etc
     *  
     *  	etc, etc, etc
     *  
     *  	(Also, I haven't tested it, sooo yeah.....)
     *  
     */
    public static void updateBoardGUI() {
    	
    	System.out.println("Updating board GUI");
    	boardNode.getChildren().clear();
    	for (int i = 0; i < 37; i++) {
    		Coord c = coordList[i];
    		Terrain t = terrains.get(c);
    		double x = 1.5 * smallHexSideLength * (c.getX() + 3) + smallHexClip.getWidthNeeded();
    		double y = (6 - c.getY() + c.getZ()) * smallHexSideLength * Math.sqrt(3)/2 + (Math.sqrt(3)*smallHexSideLength)/6 + smallHexClip.getHeightNeeded()/4 - boardAnimCount * 1.5;
    		t.getNode().relocate(x-135, y+10); // offsetting x and y as a temporary workarounds
    		boardNode.getChildren().add(t.getNode());
    	}
    	
    	Board.applyClips();
    }
    
    public static void clearBoardGUI() {
    	boardNode.getChildren().clear();
    }
	
	/*
	 * Moves terrain pieces from TileDeck to Board. Sweet anim
	 */
	public static void populateGameBoard() {
//		if( GameLoop.getInstance().isNetworked() && !TileDeck.getInstance().isIn() ){
//			System.out.println("avoiding tiledeck");
//			return;
//		}
		int numHexes;
		if (boardAnimCount == 0) {
			for (int i = 0; i < 37; i++)  {
				terrains.put(coordList[i], TileDeck.getInstance().getNoRemove(TileDeck.getInstance().getDeckSize() - i - 1));
				terrains.get(coordList[i]).setCoords(coordList[i]);
				
			}
		}
		if (boardAnimCount < 37) {
			final Coord tempCoord = coordList[boardAnimCount];
			final double x = - TileDeck.getInstance().getTileDeckNode().getLayoutX() + boardNode.getLayoutX() + 1.5 * smallHexSideLength * (tempCoord.getX() + 3) + smallHexClip.getWidthNeeded();
			final double y = - TileDeck.getInstance().getTileDeckNode().getLayoutY() + boardNode.getLayoutY() + (6 - tempCoord.getY() + tempCoord.getZ()) * smallHexSideLength * Math.sqrt(3)/2 + (Math.sqrt(3)*smallHexSideLength)/6 + smallHexClip.getHeightNeeded()/4 - boardAnimCount * 1.5;
			Path path = new Path();
			path.getElements().add(new MoveTo(smallHexClip.getWidthNeeded()/2, smallHexClip.getHeightNeeded()/2));
			path.getElements().add(new LineTo(x, y));
			pathTransition = PathTransitionBuilder.create()
					.duration(Duration.millis(50))
					.path(path)
					.orientation(PathTransition.OrientationType.NONE)
					.autoReverse(false)
					.cycleCount(1)
					.node(TileDeck.getInstance().getTopTileNoRemove().getNode())
					.onFinished(new EventHandler(){
						@Override
						public void handle(Event event) {
							finishedMove(x, y);
							populateGameBoard();
						}
					})
					.build();
		
			pathTransition.play();
		} else {	
			
			applyClips();

			GameLoop.getInstance().unPause();
			GameLoop.getInstance().setPhase(0);
		}
	}
	
	/*
	 * Called by populateGameBoard. Used to workaround changing non-final objects in eventHandler
	 */
	private static void finishedMove(double x, double y) {
		TileDeck.getInstance().getTopTile().positionNode(boardNode, x - smallHexClip.getWidthNeeded()/2, y - smallHexClip.getHeightNeeded()/2);
		boardAnimCount++;
	}
	
	// Controls the animation of the moving stacks
	public static void animStackMove(final Terrain from, final Terrain to, final String player) {
    	
		double imgVHeight = smallHexClip.getHeightNeeded() * 27/83 * 0.8;
		ImageView imgVPT = ImageViewBuilder.create()
			.image(to.getContents(ClickObserver.getInstance().getActivePlayer().getName()).getStack().get(0).getImage())
			.fitHeight(imgVHeight)
			.preserveRatio(true)
			.build();
		
		nodePT.getChildren().add(imgVPT);
		nodePT.setVisible(true);
		boardNode.getChildren().add(nodePT);
    	PathTransition pt;
    	Path path = new Path();
    	
    	// MoveTo and LineTo, both relative to the boardNode, so there is some math to convert from the smallHexNodes
    	// MoveTo path element that marks the start of the transition
    	Group mover = from.getContents(ClickObserver.getInstance().getActivePlayer().getName()).getCreatureNode();
    	Group moverTo = to.getContents(ClickObserver.getInstance().getActivePlayer().getName()).getCreatureNode();
		from.clearTerrainHM(ClickObserver.getInstance().getActivePlayer().getName());
		
		path.getElements().add(new MoveTo(1.5 * smallHexSideLength * (from.getCoords().getX() + 3) + imgVHeight/2 + mover.getTranslateX(),
				(6 - from.getCoords().getY() + from.getCoords().getZ()) * smallHexClip.getHeightNeeded()/2 + (Math.sqrt(3)*smallHexSideLength)/6 + imgVHeight/2 + mover.getTranslateY()));
		// LineTo path element that marks the end of the transition
		path.getElements().add(new LineTo(1.5 * smallHexSideLength * (to.getCoords().getX() + 3) + imgVHeight/2 + moverTo.getTranslateX(),
				(6 - to.getCoords().getY() + to.getCoords().getZ()) * smallHexClip.getHeightNeeded()/2 + (Math.sqrt(3)*smallHexSideLength)/6 + imgVHeight/2 + moverTo.getTranslateY()));
		
		
		pt = PathTransitionBuilder.create()
			.duration(Duration.millis(1000))
			.path(path)
			.node(nodePT)
			.orientation(PathTransition.OrientationType.NONE)
			.autoReverse(false)
			.cycleCount(1)
			.onFinished(new EventHandler(){
				@Override
				public void handle(Event event) {
					ClickObserver.getInstance().setTerrainFlag("");
					to.getContents(player).getCreatureNode().setVisible(true);
					to.getContents(player).updateImage();
					deleteNodePT();
					ClickObserver.getInstance().getClickedTerrain().uncoverPieces(GameLoop.getInstance().getPlayer().getName());
				}
			})
			.build();
		pt.play();
	}
    
	// Delete the node containing the image of the moving stack animation once the stack has completed its move
	private static void deleteNodePT() { 
		boardNode.getChildren().remove(nodePT);
		nodePT.setVisible(false);
	}
	
	// removes the nodes shading out the terrain
	public static void removeCovers() {
		Iterator<Coord> keySetIterator = terrains.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		Coord key = keySetIterator.next();
    		terrains.get(key).uncover();
    	}
	}
	// covers all terrains
	public static void applyCovers() {
		Iterator<Coord> keySetIterator = terrains.keySet().iterator();
		int i = 0;
    	while(keySetIterator.hasNext()) {
    		Coord key = keySetIterator.next();
    		terrains.get(key).cover();
    		i++;
    	}
	}
	
	// covers all terrains, except the ones in the passed arraylist
	public static void applyCovers(ArrayList<Coord> coords) {
		Iterator<Coord> keySetIterator = terrains.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		Coord key = keySetIterator.next();
    		Terrain t = terrains.get(key);
    		t.cover();
			for (Coord coord : coords) {
				if (t.compareTo(coord) == 0)
					t.uncover();
			}
    	}
	}
	
	// covers all terrains excpet the ones in the array list
	public static void applyCoversT(ArrayList<Terrain> t) {
		applyCovers();
		for (Terrain ter : t)
			ter.uncover();
	}
	
	// covers all terrains, except the ones this creature can move to
	public static void applyCovers(Piece c) {
		Coord currentC = c.getCurrentLocation();
		Terrain currentT = terrains.get(currentC);
		String activePlayer = GameLoop.getInstance().getPlayer().getName();
		int numMovers = currentT.countMovers(activePlayer);
		
		// If current hex has more than one player on it. They cannot move off it
		// If the current hex has yet to be explored. They cannot move of it.
		if ((currentT.getContents().size() > 1 && GameLoop.getInstance().getPhase() != 6) || (!currentT.isExplored() && GameLoop.getInstance().getPhase() != 6)) {
			applyCovers();
			System.out.println("All covered   **********************************************************");
			return;
		}
		
		Iterator<Coord> keySetIterator = terrains.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		Coord key = keySetIterator.next();
    		Terrain t = terrains.get(key);
			if (!c.canMoveTo(ClickObserver.getInstance().getClickedTerrain(), t)) {
				t.cover();
			}
			if (!(t.getContents(activePlayer) == null || numMovers + t.getContents(activePlayer).getStack().size() < 10)) {
				t.cover();
			}
			if (GameLoop.getInstance().getPhase() == 6 && !t.isExplored()) {
				t.cover();
			}
		}
	}
	
	// Covers all terrains that have other players in them
	public static void applyCovers(Player p) {		
		Iterator<Coord> keySetIterator = terrains.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		Coord key = keySetIterator.next();
    		Terrain t = terrains.get(key);
    		Fort f = t.getFort();
    		boolean doCover = false;
    		for (Player pl : GameLoop.getInstance().getPlayers()) {
	    		if (t.getContents().containsKey(pl.getName()) && !pl.getName().equals(p.getName()))
	    			doCover = true;
	    		if (f != null && !f.getOwner().getName().equals(p.getName()))
	    			doCover = true;
    		}
    		if (doCover)
    			t.cover();
    	}
	}
	
	// Returns the Terrain with the required Coord
	public static Terrain getTerrainWithCoord(Coord c) {
		Iterator<Coord> keySetIterator = terrains.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		Coord key = keySetIterator.next();
    		
    		if (key.equals(c)) {
    			return terrains.get(key);
    			
    		}
    		
    	}
    	return null;
	}
	
	// Flips over all the tiles
	public static void showTerrains() {
		
		Iterator<Coord> keySetIterator = terrains.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		Coord key = keySetIterator.next();
    		Terrain t = terrains.get(key);
    		
    		t.setShowTile(true);
    		t.setTileImage();
 
    	}
	}
	
	// Checks if starting spots are sea terrains, or if more than two adjacent are sea terrains
	public static void removeBadWaters() {
		
		Coord[] startSpots = GameLoop.getInstance().getStartingPos();
		Coord badSpot = null;

		// Cycles through each start spot
		for (final Coord spot : startSpots) {

			// Is the starting position a SEA?
			if (terrains.get(spot).getType().equals("SEA")) 
				badSpot = spot;
		}
		final Coord finalBadSpot = badSpot;

		// If there are some bad waters
		if (badSpot != null) {

			// If the tileDeck is not in view yet, slide it in before doing anything
			if (!TileDeck.getInstance().isIn()) {
				TileDeck.getInstance().slideIn(Game.getWidth(), Game.getHeight(), new EventHandler(){
					@Override
					public void handle(Event event) {
						switchBadWater(finalBadSpot);
					}
				});
			} else 
				switchBadWater(finalBadSpot);
		} else 
			GameLoop.getInstance().unPause();
	}
	
	/*
	 * Checks Terrains surrounding players start spot to see if there is at least two land hexes
	 */
	public static void removeBadAdjWaters() {
		
		removingBadAdjWaters = true;
		Player player = ClickObserver.getInstance().getActivePlayer();
		Coord spot = player.getHexesOwned().get(0).getCoords();
        Coord[] adj = spot.getAdjacent();
        final ArrayList<Terrain> badAdjWaters = new ArrayList<Terrain>();
        int numAdj = 0;

        // Count the sea hexes around start spot
        for (Coord c : adj) {
        	if (Board.getTerrainWithCoord(c).getType().equals("SEA")) {
        		badAdjWaters.add(Board.getTerrainWithCoord(c));
        		numAdj++;
        	}
        }        

        // while there is not two land hexes
        if (spot.getNumAdjacent() - numAdj < 2) {

        	ClickObserver.getInstance().setTerrainFlag("Setup: RemoveBadAdjWater");
            Board.applyCovers();
            for (Terrain t : badAdjWaters) {
            	t.uncover();
            }
        } else {
        	GameLoop.getInstance().unPause();
        	ClickObserver.getInstance().setTerrainFlag("");
        }
             
	}

	
	public static void switchBadWater(Coord c) {

		// For top coord in badWater array, remove it and add a new tile from top of the deck
		final Coord theBadCoord = c;
		boardNode.getChildren().remove(terrains.get(theBadCoord).getNode());
		Player ownerBadSpot = null;
		if (terrains.get(c).getOwner() != null) {
			ownerBadSpot = terrains.get(c).getOwner();
			ownerBadSpot.removeHexNoOwner(terrains.get(theBadCoord));
		}
		
		terrains.remove(theBadCoord);
		terrains.put(theBadCoord, TileDeck.getInstance().getNoRemove(TileDeck.getInstance().getDeckSize() - 1));
		terrains.get(theBadCoord).setCoords(theBadCoord);
		terrains.get(theBadCoord).setClip();
		terrains.get(theBadCoord).setShowTile(true);
		terrains.get(theBadCoord).setTileImage();
		if (ownerBadSpot != null)
			ownerBadSpot.addHexOwned(terrains.get(theBadCoord));

		final double x = - TileDeck.getInstance().getTileDeckNode().getLayoutX() + boardNode.getLayoutX() + 1.5 * smallHexSideLength * (theBadCoord.getX() + 3) + smallHexClip.getWidthNeeded();
		final double y = - TileDeck.getInstance().getTileDeckNode().getLayoutY() + boardNode.getLayoutY() + (6 - theBadCoord.getY() + theBadCoord.getZ()) * smallHexSideLength * Math.sqrt(3)/2 + (Math.sqrt(3)*smallHexSideLength)/6 + smallHexClip.getHeightNeeded()/4 - boardAnimCount*1.5;
		Path path = new Path();
		path.getElements().add(new MoveTo(smallHexClip.getWidthNeeded()/2, smallHexClip.getHeightNeeded()/2));
		path.getElements().add(new LineTo(x, y));
		pathTransition = PathTransitionBuilder.create()
				.duration(Duration.millis(500))
				.path(path)
				.orientation(PathTransition.OrientationType.NONE)
				.autoReverse(false)
				.cycleCount(1)
				.node(TileDeck.getInstance().getTopTileNoRemove().getNode())
				.onFinished(new EventHandler(){
					@Override
					public void handle(Event event) {
						finishedMove(x, y);
						terrains.get(theBadCoord).cover();
//						if (ownerBadSpot != null) 
//							ownerBadSpot.addHexOwned(terrains.get(theBadCoord));
						if (removingBadAdjWaters)
							removeBadAdjWaters();
						else 
							removeBadWaters();
					}
				})
				.build();

		pathTransition.play();
	}
	
	public static void applyClips() {
		
		// Turns the clips on
		Iterator<Coord> keySetIterator = terrains.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		Coord key = keySetIterator.next();
    		terrains.get(key).setClip();
    	}
	}
	
	public static Group getBoardNode() { return boardNode; }
}


package KAT;
/*
 * InfoPanel.java:
 * 
 * A panel on the left of the game that shows info on the object which was last clicked:
 * 
 * 		Tiles: Shows the tile type, and a list of Pieces on it
 * 		Players: maybe a small blurb about what they have got on the board etc...
 * 		More stuff:....
 * 
 * 
 */

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradientBuilder;
import javafx.scene.paint.StopBuilder;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.EllipseBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class InfoPanel {

	private static Group infoNode;//, textGroup;
	private static Group fortNode;
	private static Group whereTheWildThingsAre;
	private static ImageView tileImageView, playerImageView, fortImageView, markerImageView;
	private static Image backingJungle, backingMountains, backingPlains, backingSea, backingSwamp, backingForest, backingFrozenWaste, backingDesert;
    private static HBox playerPieceLists;
    private static HBox playerIconsBox;
    private static Text[] listLables;
    private static DropShadow dShadow, dShadow2;
    private static Text terrainTypeText;
    private static Font font;

	private static Coord currentTileCoords;
    private static double width, height, tileHeight;
    private static int numPlayers;
    private static String[] playerNames;
    
    private static HashMap<String, CreatureStack> contents;
    private static HashMap<String, Group> vBoxLists;
    private static HashMap<String, Group> playerIcons;
    private static HashMap<String, Rectangle> playerIconCovers;
    private static ImageView[] playerIconImageVs;
    private static ArrayList<Piece> movers;
    
    private static Terrain currHex;
    private static Fort fort;
	
    /*
	 * Constructors
	 */
	public InfoPanel (BorderPane bp){
		
		movers = new ArrayList<Piece>();
		vBoxLists = new HashMap<String, Group>();
		playerIcons = new HashMap<String, Group>();
		playerIconCovers = new HashMap<String, Rectangle>();
		numPlayers = GameLoop.getInstance().getNumPlayers();
		playerNames = new String[4];
		playerIconImageVs = new ImageView[4];
		for (int i = 0; i < numPlayers; i++)
			playerNames[i] = GameLoop.getInstance().getPlayers()[i].getName();
		
		width = bp.getWidth() * 0.25;
		height = bp.getHeight();
		currentTileCoords = new Coord(-1, -1, -1);
		
		infoNode = GroupBuilder.create()
				.children(RectangleBuilder.create()
						.width(width)
						.height(height)
						.fill(Color.DARKSLATEGRAY)
						.opacity(0.5)
						.build())
				.layoutX(0)
				.layoutY(0)
				.build();
		
		bp.getChildren().add(infoNode);
		setUpImageViews();
        currHex = null;
	}
	
	/*
	 * Displays tile info:
	 * 
	 * Type of terrain is displayed on top.
	 * List of Peices will be displayed
	 * Owner of tile etc
	 */
	public static void showTileInfo(Terrain t) {

		if (t.getCoords() != currentTileCoords) {

			
			currHex = t;
			currentTileCoords = t.getCoords();
			contents = t.getContents();
			terrainTypeText.setText(t.getType());
			terrainTypeText.setLayoutX(width - terrainTypeText.getLayoutBounds().getWidth() - 20);
			
			switch (t.getType()) {
				case "JUNGLE":
					tileImageView.setImage(backingJungle);
					break;
				case "MOUNTAINS":
					tileImageView.setImage(backingMountains);
					break;
				case "FOREST":
					tileImageView.setImage(backingForest);
					break;
				case "DESERT":
					tileImageView.setImage(backingDesert);
					break;
				case "SEA":
					tileImageView.setImage(backingSea);
					break;
				case "PLAINS":
					tileImageView.setImage(backingPlains);
					break;
				case "SWAMP":
					tileImageView.setImage(backingSwamp);
					break;
				case "FROZENWASTE":
					tileImageView.setImage(backingFrozenWaste);
					break;
			}
		}
		// Clear things from infoPanel
		movers.clear();
		markerImageView.setVisible(false);
		fortNode.getChildren().clear();
		whereTheWildThingsAre.getChildren().clear();
		
		// Change to new images
		playerIconsBox.setVisible(true);
		if (t.getOwner() != null) {
			markerImageView.setImage(t.getOwner().getImage());
			markerImageView.setVisible(true);
			if (t.getFort() != null) {
				fort = t.getFort();
				fortNode.getChildren().add(fort.getPieceNode());
			}
		}
		
		// Disables and hides all the boxes before showing the new ones
		playerPieceLists.getChildren().clear();
		playerIconsBox.getChildren().clear();
		
		
		double creatureHeight = width * 0.23;
		
		int j = 0;
		Iterator<String> keySetIterator = contents.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		String key = keySetIterator.next();
    	
    		if (!key.equals(GameLoop.getInstance().getWildThings().getName())) {
	    		vBoxLists.get(key).getChildren().clear();
	    		playerIconsBox.getChildren().add(playerIcons.get(key));
	    		
	    		if (((double)contents.get(key).getStack().size()) * creatureHeight <= height * 0.8) {
	    			for (int i = 0; i < contents.get(key).getStack().size(); i++) {
	    				
		    			vBoxLists.get(key).getChildren().add(contents.get(key).getStack().get(i).getPieceNode());
		    			vBoxLists.get(key).getChildren().get(i).relocate(0, creatureHeight * i);
		    			
		    			if (ClickObserver.getInstance().getActivePlayer().getName().equals(key))
		    				contents.get(key).getStack().get(i).uncover();
		    			else
		    				contents.get(key).getStack().get(i).cover();
		    			if (contents.get(key).getStack().get(i).doneMoving())
		    				contents.get(key).getStack().get(i).cover();
		    		}
	    		} else {	    			
	    			
	    			double offset = (creatureHeight * contents.get(key).getStack().size() - height * 0.80) / (contents.get(key).getStack().size() - 1);
	    			
	    			for (int i = 0; i < contents.get(key).getStack().size(); i++) {
	
		    			vBoxLists.get(key).getChildren().add(contents.get(key).getStack().get(i).getPieceNode());
		    			vBoxLists.get(key).getChildren().get(i).relocate(0, (creatureHeight - offset) * i);
		    			if (ClickObserver.getInstance().getActivePlayer().getName().equals(key))
		    				contents.get(key).getStack().get(i).uncover();
		    			else
		    				contents.get(key).getStack().get(i).cover();
		    			if (contents.get(key).getStack().get(i).doneMoving())
		    				contents.get(key).getStack().get(i).cover();
		    		}
	    		}
	    		playerPieceLists.getChildren().add(vBoxLists.get(key));
	    		
	    		// add wildthings on tile
    		} else { // Wild pieces
    			
    			double availWidth = width - 20;   			
    			double offset;
    			if (((double)contents.get(key).getStack().size()) * creatureHeight > availWidth) 
    				offset = (creatureHeight * contents.get(key).getStack().size() - availWidth) / (contents.get(key).getStack().size() - 1);
    			else
    				offset = 0;
    			
				for (int i = 0; i < contents.get(key).getStack().size(); i++) {
    				whereTheWildThingsAre.getChildren().add(contents.get(key).getStack().get(i).getPieceNode());
    				whereTheWildThingsAre.getChildren().get(i).relocate((creatureHeight - offset) * i, 0);
    				
    			}
    			
    		}

    	}
		
	}
    
	// Should maybe use ClickObserver instead of this?
    public Terrain getCurrHex(){
        return currHex;
    }
	
	/*
	 * Is called on setup of infopanel. Creates image views needed for terrain (Hex)
	 */
	private void setUpImageViews() {
		
		tileHeight = width * 0.3;
		double pieceHeight = width * 0.23;
		double iconHeight = height * 0.2 - tileHeight;
		final Glow glow = new Glow();
		font = Font.loadFont(getClass().getResourceAsStream("/Fonts/ITCBLKAD.TTF"), tileHeight/5);
		
		dShadow = DropShadowBuilder.create()
				.offsetX(5)
				.offsetY(5)
				.radius(5)
				.color(Color.DARKSLATEGRAY)
				.build();
		
		dShadow2 = DropShadowBuilder.create()
				.offsetX(2)
				.offsetY(2)
				.radius(3)
				.color(Color.WHITESMOKE)
				.build();
		
		tileImageView = ImageViewBuilder.create()
				.fitWidth(width)
				.preserveRatio(true)
				.build();
		
		markerImageView = ImageViewBuilder.create()
				.fitHeight(tileHeight/3)
				.preserveRatio(true)
				.effect(dShadow)
				.build();
        
        // Icons above each stack of pieces
        playerIconsBox = HBoxBuilder.create()
        		.layoutY(tileHeight)
        		.visible(false)
        		.build();
        
        playerPieceLists = HBoxBuilder.create()
        		.layoutX(0)
        		.layoutY(height * 0.20)
        		.build();
		
        for (int i = 0; i < numPlayers; i++) {
        	final int j = i;
        	
        	playerIconCovers.put(playerNames[i], RectangleBuilder.create()
        			.height(iconHeight)
					.width(pieceHeight)
					.arcHeight(10)
					.arcWidth(10)
					.fill(Color.DARKSLATEGRAY)
					.opacity(0.5)
					.visible(true)
					.disable(false)
					.build());
        	
        	Group creatureList = new Group();
        	
        	playerIconImageVs[i] = ImageViewBuilder.create()
					.image(GameLoop.getInstance().getPlayers()[i].getImage())
					.fitHeight(pieceHeight * 1.4)
					.fitWidth(pieceHeight * 1.4)
					.layoutY(-pieceHeight * 0.3)
					.layoutX(-pieceHeight * 0.2)
					.clip(RectangleBuilder.create()
							.height(iconHeight)
							.width(pieceHeight)
							.layoutY(+pieceHeight * 0.3)
							.layoutX(+pieceHeight * 0.2)
							.arcHeight(10)
							.arcWidth(10)
							.build()) 
					.build();
        	
        	playerIconImageVs[i].setOnMouseEntered(new EventHandler(){
						@Override
						public void handle(Event event) {
							playerIconImageVs[j].setEffect(glow);
						}
					});
        	playerIconImageVs[i].setOnMouseExited(new EventHandler(){
						@Override
						public void handle(Event event) {
							playerIconImageVs[j].setEffect(null);
						}
					});
			playerIconImageVs[i].setOnMouseClicked(new EventHandler(){
				@Override
				public void handle(Event event) {
					playerIconClicked();
				}
			});
			
        	Rectangle iconBorder = RectangleBuilder.create()
					.width(pieceHeight)
					.height(iconHeight)
					.arcHeight(10)
					.arcWidth(10)
					.stroke(Color.DARKSLATEGRAY)
					.strokeWidth(3)
					.effect(new GaussianBlur(4))
					.clip(RectangleBuilder.create()
        					.height(iconHeight)
        					.width(pieceHeight)
        					.arcHeight(10)
        					.arcWidth(10)
        					.build()) 
					.fill(Color.TRANSPARENT)
					.mouseTransparent(true)
					.build();
			
        	Group iconNode = GroupBuilder.create()
        			.clip(RectangleBuilder.create()
        					.height(iconHeight)
        					.width(pieceHeight)
        					.arcHeight(10)
        					.arcWidth(10)
        					.build())
	        		.build();
        	
        	iconNode.getChildren().addAll(playerIconImageVs[i], iconBorder, playerIconCovers.get(playerNames[i]));
        	
        	playerIcons.put(playerNames[i], iconNode);
        	
        	vBoxLists.put(playerNames[i], creatureList);
        }
		
		fortNode = GroupBuilder.create()
				.effect(dShadow)
				.clip(CircleBuilder.create()
						.radius(tileHeight * 0.93/2)
						.centerX(tileHeight/2)
						.centerY(tileHeight/2)
						.build())
				.layoutX(tileHeight * 0.04)
				.layoutY(tileHeight * 0.04)
				.build();
		
		terrainTypeText = TextBuilder.create()
                .layoutY(tileHeight/5)
                .font(font)
                .fill(LinearGradientBuilder.create()
                        .startY(0)
                        .startX(1)
                        .stops(StopBuilder.create()
                                .color(Color.BLACK)
                                .offset(0.4)
                                .build(),
                            StopBuilder.create()
                                .color(Color.DARKSLATEGRAY)
                                .offset(0)
                                .build())
                        .build())
                .effect(dShadow2)
                .build();
		
		whereTheWildThingsAre = GroupBuilder.create()
				.layoutX(10)
				.layoutY(tileHeight - pieceHeight - 10)
				.build();
        
        infoNode.getChildren().addAll(playerIconsBox, tileImageView, playerPieceLists, fortNode, terrainTypeText, whereTheWildThingsAre, markerImageView);
	}
	
	public static ArrayList<Piece> getMovers() { return movers; }
	
	public static double getWidth() { return width; }
	public static double getTileHeight() { return tileHeight; }
	
	public static void setClassImages() {
		backingJungle = new Image("Images/InfoPanelBacking_Jungle.jpg");
		backingMountains = new Image("Images/InfoPanelBacking_Mountains.jpg");
		backingPlains = new Image("Images/InfoPanelBacking_Plains.jpg");
		backingSea = new Image("Images/InfoPanelBacking_Sea.jpg");
		backingSwamp = new Image("Images/InfoPanelBacking_Swamp.jpg");
		backingForest = new Image("Images/InfoPanelBacking_Forest.jpg");
		backingFrozenWaste = new Image("Images/InfoPanelBacking_FrozenWaste.jpg");
		backingDesert = new Image("Images/InfoPanelBacking_Desert.jpg");
	}
	
	private void playerIconClicked() {
		for (Piece p : currHex.getContents(GameLoop.getInstance().getPlayer().getName()).getStack()) {
			if (p instanceof Creature) {
				System.out.println("In playerIconClicked");
				ClickObserver.getInstance().setClickedCreature((Creature) p);
				ClickObserver.getInstance().whenCreatureClicked();
			}
		}
	}
	
	public static void cover(final String playerName) {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	playerIconCovers.get(playerName).setVisible(true);
        		playerIconCovers.get(playerName).setDisable(false);
            }
        });
		
	}
	public static void uncover(final String playerName) {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	playerIconCovers.get(playerName).setVisible(false);
        		playerIconCovers.get(playerName).setDisable(true);
            }
        });
	}
	
}

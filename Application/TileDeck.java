package KAT;

import java.util.ArrayList;
import java.util.Collections;

import javafx.animation.PathTransition;
import javafx.animation.PathTransitionBuilder;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

public class TileDeck {

	private static ArrayList<Terrain> deck;
	private Group tileDeckNode;
	private PathTransition pathTransition;
	private static TileDeck uniqueInstance;
	private boolean isIn;
	
	public TileDeck(BorderPane bp) {

		isIn = false;
		uniqueInstance = this;
		tileDeckNode = GroupBuilder.create()
				.layoutX(bp.getWidth()*0.75)
				.layoutY(0)
				.build();
		bp.getChildren().add(tileDeckNode);
		
		//Creates arraylist with every tile piece minus the 4 sea tiles set aside
		deck = new ArrayList<Terrain>();
		
		for (int i = 0; i < 7; i++)
			deck.add(new Terrain("Desert"));
		for (int i = 0; i < 7; i++)
			deck.add(new Terrain("Forest"));
		for (int i = 0; i < 6; i++)
			deck.add(new Terrain("FrozenWaste"));
		for (int i = 0; i < 6; i++)
			deck.add(new Terrain("Jungle"));
		for (int i = 0; i < 7; i++)
			deck.add(new Terrain("Mountains"));
		for (int i = 0; i < 7; i++)
			deck.add(new Terrain("Plains"));
		for (int i = 0; i < 5; i++)
			deck.add(new Terrain("Sea"));
		for (int i = 0; i < 7; i++)
			deck.add(new Terrain("Swamp"));
	
		Collections.shuffle(deck);
		
		// Adds each tile to the Node which displays them. Offset slightly to appear like a stack of terrains
		for (int i = 0; i < deck.size(); i++) {
			tileDeckNode.getChildren().add(deck.get(i).getNode());
			deck.get(i).getNode().setLayoutY(- i * 1.5);
		}
	
		slideIn(bp.getWidth(), bp.getHeight(), null);
	}
	
	/*
	 *  Constructor for a pre set map
	 */
	public TileDeck (BorderPane bp, ArrayList<String> tiles) {
		
		uniqueInstance = this;
		tileDeckNode = GroupBuilder.create()
				.layoutX(bp.getWidth()*0.75)
				.layoutY(0)
				.build();
		bp.getChildren().add(tileDeckNode);
		
		deck = new ArrayList<Terrain>();
		
		for (String type : tiles) 
			deck.add(0, new Terrain(type));
		
		for (int i = 0; i < deck.size(); i++) {
			tileDeckNode.getChildren().add(deck.get(i).getNode());
			deck.get(i).getNode().setLayoutY(- i * 1.5);
		}
		
		slideIn(bp.getWidth(), bp.getHeight(), null);
	}
	
	/*
	 * quick animation that slides TileDeck down.
	 */
	public void slideIn(double x, double y, EventHandler eh) {
		//GameLoop.getInstance().pause();
		Path path = new Path();
		path.getElements().add(new MoveTo(0, -200));
		path.getElements().add(new LineTo(0, y*0.15));
		pathTransition = PathTransitionBuilder.create()
				.duration(Duration.millis(1000))
				.path(path)
				.node(tileDeckNode)
				.orientation(PathTransition.OrientationType.NONE)
				.onFinished(eh)
				.autoReverse(true)
				.cycleCount(1)
				.build();
		pathTransition.play();
		isIn = true;
	}
	/*
	 * quick animation that slides TileDeck up and out of view.
	 */
	public void slideOut() {
		pathTransition.setCycleCount(2);
		pathTransition.playFrom(Duration.millis(1000));
		isIn = false;
	}

	
	
	/*
	 * Gets and sets
	 */
	public Terrain getTopTile() { return deck.remove(deck.size() - 1); }
	public Terrain getTopTileNoRemove() { return deck.get(deck.size() - 1); } // Used for accessing something about the tile without removing it from the deck
	public Group getTileDeckNode() { return tileDeckNode; }
	public Terrain getNoRemove(int i) { return deck.get(i); }
	public static TileDeck getInstance() { return uniqueInstance; }
	public static int getDeckSize() { return deck.size(); }
	public int getDeckGUISize() { return tileDeckNode.getChildren().size(); }
	public boolean isIn() { return isIn; }
}

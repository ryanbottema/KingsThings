package KAT;

import java.util.ArrayList;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;

public class CreatureStack {

	private static double width, height;
	
	private ArrayList<Piece> stack;
	private Player owner;
	private Coord currentLocation;
	
	private Group stackNode;
	private ImageView stackImgV;
	private Rectangle stackRecBorder;
	
	private TranslateTransition moveWithin;
	private static GaussianBlur gBlur = new GaussianBlur(3);
	
	/*
	 * --------- Constructors
	 */
	public CreatureStack(Player owner, Coord c) {

		stackNode = new Group();
		this.owner = owner;
		stack = new ArrayList<Piece>();
//		moveWithin = new PathTransition();
		setupImageView();
		currentLocation = c;
	}
	
	public CreatureStack(String owner, Coord c) {
		currentLocation = c;
		stackNode = new Group();
		for (Player p : GameLoop.getInstance().getPlayers()) {
			if (p.getName().equals(owner))
				this.owner = p;
		} 
		if (owner.equals(GameLoop.getInstance().getWildThings().getName()))
			this.owner = GameLoop.getInstance().getWildThings();
		stack = new ArrayList<Piece>();
//		moveWithin = new PathTransition();
		setupImageView();
	}
	
	/*
	 * ---------- Gets and sets
	 */
	public void setOwner(Player p) { owner = p; }
	public ArrayList<Piece> getStack() { return stack; }
	public Player getOwner() { return owner; }
	public Group getCreatureNode() { return stackNode; }
	public static double getWidth() { 
		if (width == 0)
			width = Game.getWidth() * 0.016;
		return width; 
	}
	public Coord getCurrentLocation() { return currentLocation; }
	public void setCurrentLocation(Coord c) { currentLocation = c; }
	
	/*
	 * -------- Instance methods
	 */
	public void addCreature(Creature c) {
		stack.add(0, c);
		c.setStackedIn(this);
		c.setCurrentLocation(currentLocation);
		updateImage();
	}

	/*
	 * Method to filter out all of the Creature-type pieces from the stack.
	 */
	public ArrayList<Creature> filterCreatures(ArrayList<Piece> p) {
		ArrayList<Creature> c = new ArrayList<Creature>();
		for (Piece pc : p) {
			if (pc.getType().equals("Creature") || pc.getType().equals("Special Character"))
				c.add(0,(Creature)pc);
		}
		return c;
	}

	public void addIncome(SpecialIncome s) {
		stack.add(0, s);
		s.setStackedIn(this);
		updateImage();
	}
	
	// Adds a creature to the stack without updating the image (used when board animates moving stacks)
	public void addCreatureNoUpdate(Creature c) {
		c.setStackedIn(this);
		c.setCurrentLocation(currentLocation);
		stack.add(0, c);
	}
	
	public Piece getCreature(int i) {
		return stack.get(i);
	}
	
	public Creature removeCreature(Creature c) {

		stack.remove(c);
		c.setStackedIn(null);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
        		updateImage();
            }
        });
		if (stack.size() == 0) {
			Platform.runLater(new Runnable() {
	            @Override
	            public void run() {
	    			stackNode.getChildren().clear();
	            }
	        });
		}
		return c;
	}
	
	public Creature removeCreature(int i) {

		Piece c = stack.remove(i);
		if (c.getType().equals("Creature")) {
			c.setStackedIn(null);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
        			updateImage();
                }
            });
			if (stack.size() == 0)
				stackNode.getChildren().clear();
			return (Creature)c;
		}
		return null;
	}
	
	// updates the image on the top of the stack (top creature, or more likely upside down creature)
	public void updateImage() {
		if (stack.size() > 0)
			stackImgV.setImage(stack.get(0).getImage());
		else
			stackImgV.setImage(null);
	}
	
	public boolean isEmpty() { return stack.isEmpty(); }
	
	private void setupImageView() {
		
		width = getWidth();
		stackNode.setMouseTransparent(true);
		
		// Creates ImageView
		stackImgV = ImageViewBuilder.create()
				.fitHeight(width)
				.preserveRatio(true)
				.build();
		
		// Create rectangle around stack
		stackRecBorder = RectangleBuilder.create()
				.width(width)
				.height(width)
				.strokeWidth(3)
				.stroke(owner.getColor())
				.strokeType(StrokeType.CENTERED)
				.fill(Color.TRANSPARENT)
				.effect(gBlur)
				.build();
		
		// Add to pieceNode
		stackNode.getChildren().add(0, stackImgV);
		stackNode.getChildren().add(1, stackRecBorder);
		stackNode.setVisible(false);
	}

	// Moves the stack to the correct spot when a new stack is added to the same terrain
	public void moveWithinTerrain(final double x, final double y) {
		
		moveWithin = TranslateTransitionBuilder.create()
				.fromX(stackNode.getTranslateX())
				.fromY(stackNode.getTranslateY())
				.toX(x)
				.toY(y)                    
                .interpolator(Interpolator.EASE_BOTH)
				.node(stackNode)
				.duration(Duration.millis(200))
				.cycleCount(1)
				.build();
	
		moveWithin.play();
	}
	
	// When a creature is clicked from the info panel that has overlapping creatures, this properly displays them
	public void cascade(Piece c) {
		int index = stack.indexOf(c);
		if (index != -1) {
			for (int i = index; i < stack.size(); i++) 
				stack.get(i).getPieceNode().toBack();
			for (int i = index - 1; i >= 0; i--) 
				stack.get(i).getPieceNode().toBack();
		}
	}
	
	public void applyCovers() {
		for (Piece p : stack)
			p.cover();
	}
	
	public void flipDown() {
		for (Piece p : stack)
			p.flipDown();
		updateImage();
	}
	
	public void flipUp() {
		for (Piece p : stack)
			p.flipUp();
		updateImage();
	}
	
	public ArrayList<TerrainLord> getTerrainLords() {
		
		ArrayList<TerrainLord> tLords = new ArrayList<TerrainLord>();
		
		for (Piece p : stack) {
			if (p instanceof TerrainLord) {
				tLords.add((TerrainLord) p);
			}
		}
		return tLords;
	}
	
}

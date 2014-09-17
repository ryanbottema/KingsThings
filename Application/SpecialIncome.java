package KAT;

import javafx.scene.image.Image;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.GaussianBlurBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.StrokeType;

import java.util.HashMap;

public class SpecialIncome extends Piece {
	private int value;
	private boolean treasure;
	private boolean doneMoving;
	protected static Image creature_Back = new Image("Images/Creature_Back.png");

	public SpecialIncome(String front, String back, String name, int val, boolean tr) {
		super("Special Income", front, back, name);
		value = val;
		treasure = tr;
		doneMoving = false;
	}

	public SpecialIncome(String input) {
		separateInput(input);
		setType("Special Income");
		doneMoving = false;
	}

    public SpecialIncome( HashMap<String,Object> map ){
        super(map);
        this.value = (Integer)map.get("value");
        this.treasure = ((Integer)map.get("treasure") == 1) ? true : false;
        this.doneMoving = false;
    }

	private void separateInput(String in) {
		
		
		String[] input = in.split(",");
		setFront(input[1]);
		setBack(input[2]);
		setName(input[3]);
		setValue(Integer.parseInt(input[4]));
		setTreasure((input[5].equals("true")) ? true : false);
		if (input.length < 7 || input[6].equals("nil"))
			setTerrain("");
		else
			setTerrain(input[6]);
	}

	@Override
	public boolean isPlayable() {
		if (GameLoop.getInstance().getPhase() == 0 || GameLoop.getInstance().getPhase() == 3) {
			if (isTreasure())
				return true;
			if (ClickObserver.getInstance().getClickedTerrain().getType().equals(this.getTerrain()))
				return true;
		}
		return false;
	}

	public void setValue(int v) { value = v; }
	public int getValue() { return value; }
	public void setTreasure(boolean b) { treasure = b; }
	public boolean isTreasure() { return treasure; }

	public void setInPlay(boolean b) {
		if (!inPlay && b) {
			setupImageView();
		}
		else if (inPlay && !b) {
			imageFront = null;
			pieceImgV.setImage(null);
			pieceNode.setOnMouseClicked(null);
			pieceNode.getChildren().clear();
		}
		inPlay = b;
	}

	private void setupImageView() {
		// Loads image
		if (front != null && !front.equals(""))
			this.imageFront = new Image(front);
		else
			this.imageFront = creature_Back;
		
		pieceNode = GroupBuilder.create()
				.clip( RectangleBuilder.create()
						.width(InfoPanel.getWidth() * 0.23)
						.height(InfoPanel.getWidth() * 0.23)
						.build()) 
				.build();
		
		// Creates ImageView
		pieceImgV = ImageViewBuilder.create()
				.image(imageFront)
				.fitHeight(InfoPanel.getWidth() * 0.23)
				.preserveRatio(true)
				.build();
		
		// Small outline around creatures
		pieceBorderOutline = RectangleBuilder.create()
				.width(InfoPanel.getWidth() * 0.23)
				.height(InfoPanel.getWidth() * 0.23)
				.strokeWidth(1)
				.strokeType(StrokeType.INSIDE)
				.stroke(Color.BLACK)
				.fill(Color.TRANSPARENT)
				.effect(new GaussianBlur(2))
				.clip( RectangleBuilder.create()
						.width(InfoPanel.getWidth() * 0.23)
						.height(InfoPanel.getWidth() * 0.23)
						.build())
				.disable(true)
				.build();
		
		// Create rectangle around creature
		pieceSelectBorder = RectangleBuilder.create()
				.width(InfoPanel.getWidth() * 0.23)
				.height(InfoPanel.getWidth() * 0.23)
				.strokeWidth(5)
				.strokeType(StrokeType.INSIDE)
				.stroke(Color.WHITESMOKE)
				.fill(Color.TRANSPARENT)
				.effect(new GaussianBlur(5))
				.clip( RectangleBuilder.create()
						.width(InfoPanel.getWidth() * 0.23)
						.height(InfoPanel.getWidth() * 0.23)
						.build())
				.visible(false)
				.disable(true)
				.build();
		
		// Create rectangle to cover image and disable clicks
		pieceCover = RectangleBuilder.create()
				.width(InfoPanel.getWidth() * 0.23)
				.height(InfoPanel.getWidth() * 0.23)
				.fill(Color.DARKSLATEGRAY)
				.opacity(0.5)
				.visible(false)
				.disable(true)
				.build();
		
		// Add to pieceNode
		pieceNode.getChildren().addAll(pieceImgV, pieceBorderOutline, pieceSelectBorder, pieceCover);
		
	}

	@Override
	public String toString() {
		return "Name: " + getName() + "\n" + "Value: " + getValue() + "\n";
	}

    @Override
    public HashMap<String,Object> toMap(){
        HashMap<String,Object> map = super.toMap();
        map.put("value", value);
        map.put("treasure", treasure ? 1 : 0);
        return map;
    }

	@Override
	public Group getPieceNode() { 
		if (!inPlay) 
			setInPlay(true);
		return pieceNode;
	}

	@Override
	public Image getImage() {
		if (!inPlay) 
			setInPlay(true);
		return imageFront;
	}
}

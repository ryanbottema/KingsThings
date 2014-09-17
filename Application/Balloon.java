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

public class Balloon extends MagicEvent {
	public Balloon() {
		super("Images/Magic_Balloon.png", "Images/Creature_Back.png", "Balloon");
		setOwner(null);
		setDescription("Carry any three creatures up to three hexes from where they started the phase!\nSee the leaflet for more info.");
	}

	/*
	 * The Balloon is only playable during the Movement Phase.
	 */
	@Override
	public boolean isPlayable() {
		if (GameLoop.getInstance().getPhase() == 5)
			return true;
		return false;
	}

	/*
	 * The Balloon can carry up to 3 creatures from the same hex up to 3 hexes away. The selected creatures may not move normally this turn.
	 * Place the three creatures and the balloon in the target hex.
	 * 
	 * Only creatures with a combat value less than 4 may be transported (this includes Special Characters).
	 * Creatures that were transported now fight as ranged creatures (in the turn they were transported) unless and until the Balloon is eliminated.
	 * The Balloon is eliminated if it takes a hit in combat.
	 *
	 * The Balloon and its passengers may retreat to any friendly hex within 3 hexes of the battle as long as the Balloon is still in play.
	 *
	 * The Balloon is returned to the cup at the end of the turn in which it was used.
	 */
	public void performAbility() {
		if (TheCup.getInstance().getRemaining().size() != 0)
			TheCup.getInstance().addToCup(this);
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

	private void setupImageView() {
		// Loads image
		if (front != null && !front.equals(""))
			this.imageFront = new Image(front);
		else
			this.imageFront = new Image("Images/Creature_Back.png");
		
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

	public void setInPlay(boolean b) {  // If Creatures are not in play, creates the things needed for them. Vis-versa if it is in play, and is put out of game
		if (!inPlay && b) {
			setupImageView();
		} else if (inPlay && !b) {
			imageFront = null;
			pieceImgV.setImage(null);
			pieceNode.setOnMouseClicked(null);
			pieceNode.getChildren().clear();
		}
		inPlay = b;
	}
}
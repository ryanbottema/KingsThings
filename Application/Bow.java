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

public class Bow extends MagicEvent {
	public Bow() {
		super("Images/Magic_Bow.png", "Images/Creature_Back.png", "Bow");
		setOwner(null);
		setDescription("Place a bow on one of your creatures and turn it into a ranged warrior!\nSee the leaflet for more info.");
	}

	/*
	 * The Bow is only playable during the Combat Phase.
	 */
	@Override
	public boolean isPlayable() {
		if (GameLoop.getInstance().getPhase() == 6)
			return true;
		return false;
	}

	/*
	 * Place the Bow on top of one of your creatures engaged in battle.
	 * This creature now fights as Ranged for the rest of the battle, and its combat value is increased by 1.
	 * All other symbols on the creature are removed and it now only fights as ranged.
	 *
	 * The Bow may be transferred from one creature to another in the same hex during a battle by putting it on top
	 * of a new creature at the beginning of the next round.
	 *
	 * The Bow is eliminated if it takes a hit.
	 * If the creature wielding the Bow is eliminated, the Bow must be given to a new creature at the beginning of the next round.
	 *
	 * The Bow is returned to the cup at the end of the battle in which it's used.
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
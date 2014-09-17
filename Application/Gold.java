package KAT;

import javafx.scene.Group;
import javafx.scene.image.Image;

public class Gold extends Piece {
	int value;

	public Gold() {
		super("Gold", "", "", "Gold");
		value = 0;
	}

	public Gold(int val) {
		super("Gold", "", "", "Gold");
		value = val;
	}

	public Gold(int val, String front, String back) {
		super("Gold", front, back, "Gold");
		value = val;
	}

	@Override
	public Group getPieceNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}
}
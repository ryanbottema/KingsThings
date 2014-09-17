package KAT;

import javafx.scene.image.Image;
import javafx.scene.Group;
import java.util.HashMap;

/*
 * Class to represent a Random Event.
 */
public abstract class RandomEvent extends Piece {
	private String description;
	private Player owner;

	public RandomEvent(String front, String back, String name) {
		super("Random Event", front, back, name);
	}

    public RandomEvent( HashMap<String,Object> map ){
        super(map);
        String owner = (String)map.get("owner");
        Player player = NetworkGameLoop.getInstance().getPlayer();
        if( owner.equals(player.getName()) ){
            this.owner = player;
        }
    }

	@Override
	public String toString() {
		return "Random Event: " + getName() + "\n" + getDescription() + "\n";
	}

    @Override
    public HashMap<String,Object> toMap(){
        HashMap<String,Object> map = super.toMap();
        if( owner != null ){
            map.put("owner", owner.getName());
        } else {
            map.put("owner", "");
        }
        return map;
    }

	/*
	 * A random event is only playable during phase 4 - random event phase.
	 */
	public boolean isPlayable() {
		if (GameLoop.getInstance().getPhase() != 4)
			return false;
		else
			return true;
	}

	public abstract void performAbility();

	public void setDescription(String d) { description = d; }
	public String getDescription() { return description; }

	@Override
	public Image getImage() {
		return null;
	}

	@Override 
	public Group getPieceNode() {
		return null;
	}
}

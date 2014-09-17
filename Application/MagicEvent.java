package KAT;

import javafx.scene.image.Image;
import javafx.scene.Group;
import java.util.HashMap;

/*
 * Class to represent a Magic Event
 */
public abstract class MagicEvent extends Piece {
	private String description;
	private Player owner;

	public MagicEvent(String front, String back, String name) {
		super("Magic Event", front, back, name);
	}

	public MagicEvent(HashMap<String,Object> map){
        super(map);
        String owner = (String)map.get("owner");
        Player player = NetworkGameLoop.getInstance().getPlayer();
        if(owner.equals(player.getName())){
            this.owner = player;
        }
    }

    @Override
    public String toString() {
    	return "Magic Event: " + getName() + "\n" + getDescription() + "\n";
    }

    @Override
    public HashMap<String,Object> toMap(){
        HashMap<String,Object> map = super.toMap();
        if(owner != null){
            map.put("owner", owner.getName());
        } else {
            map.put("owner", "");
        }
        return map;
    }

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

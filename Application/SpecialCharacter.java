//
// SpecialCharacter.java
// kingsandthings/
// @author Brandon Schurman
// 
package KAT;

import java.util.HashMap;

import javafx.application.Platform;

/*
 * TODO:
 * Conform to piece, and implement inherited methods.
 * Create group for click reg etc
 */
public class SpecialCharacter extends Creature implements Combatable, Performable {

    public SpecialCharacter( String front, String back, String name, String terrainType, int combatValue, 
            boolean flying, boolean magic, boolean charging, boolean ranged ){
        super(front, back, name, terrainType, combatValue, flying, magic, charging, ranged);
        setType("Special Character");
    }

    public SpecialCharacter( HashMap<String,Object> map ){
        super(map);
        setCombatValue((Integer)map.get("combatVal"));
        setFlying(((Integer)map.get("flying") == 1) ? true : false);
        setMagic(((Integer)map.get("magic") == 1) ? true : false);
        setCharging(((Integer)map.get("charging") == 1) ? true : false);
        setRanged(((Integer)map.get("ranged") == 1) ? true : false);
    }

    public boolean inflict(){
        //TheCup.getInstance().addToCup(this.getName()); // return to cup
    	return false;
    }

    @Override
    public HashMap<String,Object> toMap(){
        HashMap<String,Object> map = super.toMap();
        map.put("combatVal", getCombatValue());
        map.put("flying", isFlying() ? 1 : 0);
        map.put("magic", isMagic() ? 1 : 0);
        map.put("charging", isCharging() ? 1 : 0);
        map.put("ranged", isRanged() ? 1 : 0);
        return map;
    }

    public void performAbility() { return; }
    public void specialAbility() { return; }
    public boolean hasSpecial() { return false; }
    public boolean hasPerform() { return false; }
    
    public void returnToBank(Terrain hex) {
    	System.out.println(this.name + " is being returned to the bank");
    	System.out.println(this.getStackedIn().getStack());
    	final SpecialCharacter tmp = this;
    	hex.removeFromStack(owner.getName(), (Creature)this);
//    	Platform.runLater(new Runnable() {
//    		@Override
//    		public void run() {
//    			getStackedIn().removeCreature(tmp);
//    		}
//    	});    	
    	SpecialCharView.removeFromPlay(name);
    	this.setOwner(null);
    }

}

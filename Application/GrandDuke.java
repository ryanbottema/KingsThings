package KAT;
// 
// GrandDuke.java
// kingsandthings/
// @author Brandon Schurman
// 
import java.util.ArrayList;

public class GrandDuke extends SpecialCharacter implements Performable
{
    /**
     * CTOR
     */
    public GrandDuke(){
        super("Images/Hero_GrandDuke.png", "Images/Creature_Back.png", "Grand Duke", "", 4, false, false, false, false);
    }

    /**
     * The GrandDuke inflicts one damage to each fort before battle.
     * @param forts - a set of all forts contained on a hex (datatype may change)
     */
    void attack( ArrayList<Fort> forts ){
        for( Fort f : forts ){
            f.inflict();
        }
    }
    
    public void performAbility() { return; }

    public void specialAbility() {
    	Fort f = Board.getTerrainWithCoord(getStackedIn().getCurrentLocation()).getFort();
    	f.inflict();
    }

    public boolean hasSpecial() { return true; }
    public boolean hasPerform() { return false; }
}

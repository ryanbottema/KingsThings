package KAT;
//
// BaronMunchausen.java
// kingsandthings/
// @author Brandon Schurman
//
import java.util.ArrayList;

public class BaronMunchausen extends SpecialCharacter implements Performable
{
    /**
     * CTOR
     */
    public BaronMunchausen(){
        super("Images/Hero_BaronMunchhausen.png", "Images/Creature_Back.png", "Baron Munchhausen", "", 4, false, false, false, false);
        setType("Special Character");
    }

    /**
     * The Baron Munchausen inflicts one damage to each fort before battle.
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

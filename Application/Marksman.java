package KAT;
// 
// Marksman.java
// kingsandthings/
// @author Brandon Schurman
//

public class Marksman extends SpecialCharacter implements Performable
{
    private static int combatValue2 = 2;
    
    public Marksman(){
        super("Images/Hero_Marksman.png", "Images/Creature_Back.png", "Marksman", "", 5, false, false, false, false);
        setType("Special Character");
    }
    
    /**
     * The Marksman can use a lower combat value
     * with the advantage of choosing which enemy creature to attack.
     * This method may need to be tweaked depending on how 
     * the game controller works
     */
    public int getOtherCombatValue(){
        return combatValue2;
    }

    public void performAbility() { return; }

    public void specialAbility() {

    }

    public boolean hasSpecial() { return true; }
    public boolean hasPerform() { return false; }
}

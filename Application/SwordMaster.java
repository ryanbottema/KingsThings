package KAT;
//
// SwordMaster.java
// kingsandthings/
// @author Brandon Schurman
//

public class SwordMaster extends SpecialCharacter implements Performable
{
    /**
     * CTOR
     */
    public SwordMaster(){
        super("Images/Hero_SwordMaster.png", "Images/Creature_Back.png", "Sword Master", "", 4, false, false, false, false);
        setType("Special Character");
    }

    /**
     * SwordMaster cannot take damage on a roll of 2 through 5.
     * Gets saving throw for one hit applied per each combat round
     */
    void inflict( int roll ){
        ;; // TODO dont really understand how this will work yet..
    }

    public void performAbility() { return; }

    public void specialAbility() {

    }

    public boolean hasSpecial() { return true; }
    public boolean hasPerform() { return false; }
}

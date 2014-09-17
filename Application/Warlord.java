package KAT;

public class Warlord extends SpecialCharacter implements Performable{
	public Warlord() {
		super("Images/Hero_Warlord.png", "Images/Creature_Back.png", "Warlord", "", 5, false, false, false, false);
		setType("Special Character");
	}

	/*
	 * Can Get one enemy creature per battle to join his side; use before any combat rounds are fought.
	 */
	public void performAbility() { return; }

	public void specialAbility() {

	}

	public boolean hasSpecial() { return true; }
	public boolean hasPerform() { return false; }
}
package KAT;

public class DustOfDefense extends MagicEvent {
	public DustOfDefense() {
		super("Images/Magic_DustOfDefense.png", "Images/Creature_Back.png", "Dust of Defense");
		setOwner(null);
		setDescription("Cause an attacking army to retreat!\nSee the leaflet for more information");
	}

	/*
	 * Dust Of Defense is only usable during the combat phase when there is an attacker on one of the
	 * owner's hexes.
	 */
	@Override
	public boolean isPlayable() {
		return false;
	}

	/*
	 * Causes the attacking army to retreat from the owner's hex.
	 * It may only be used by the defender.
	 *
	 * Dust of Defense is returned to the cup after it has been used.
	 */
	public void performAbility() {
		if (TheCup.getInstance().getRemaining().size() != 0)
			TheCup.getInstance().addToCup(this);
	}
}
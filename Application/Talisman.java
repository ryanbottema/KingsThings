package KAT;

public class Talisman extends MagicEvent {
	public Talisman() {
		super("Images/Magic_Talisman.png", "Images/Creature_Back.png", "Talisman");
		setOwner(null);
		setDescription("Use the power of the Talisman to strengthen your creatures!\nSee the leaflet for more info");
	}

	/*
	 * Playable any time during a battle.
	 */
	@Override
	public boolean isPlayable() {
		if (GameLoop.getInstance().getPhase() == 6)
			return true;
		return false;
	}

	/*
	 * Every time a hit is applied to one of your creatures during combat after the Talisman has been played, roll a die.
	 * If the value is 2-5, the creature is immune to the hit and is allowed to fight again.
	 * If the value is 1 or 6, the creature is hit normally and the Talisman is immediately returned to the cup.
	 *
	 * The Talisman's effect can be applied to each creature once per Combat Step.
	 * If the creature were to be hit twice in one Combat Step, it is eliminated regardless of the presence of the Talisman.
	 */
	public void performAbility() {
		if (TheCup.getInstance().getRemaining().size() != 0)
			TheCup.getInstance().addToCup(this);
	}
}
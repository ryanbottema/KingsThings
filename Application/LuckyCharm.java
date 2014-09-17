package KAT;

public class LuckyCharm extends MagicEvent {
	public LuckyCharm() {
		super("Images/Magic_LuckyCharm.png", "Images/Creature_Back.png", "Lucky Charm");
		setOwner(null);
		setDescription("Use magic to modify any dice roll!\nSee the leaflet for more info");
	}

	/*
	 * Playable any time a dice is rolled.
	 */
	@Override
	public boolean isPlayable() {
		return false;
	}

	/*
	 * Once a die has been rolled (by any player), the player of the Lucky Charm may either
	 * increase or decrease the value of the roll by one.
	 */
	public void performAbility() {
		if (TheCup.getInstance().getRemaining().size() != 0)
			TheCup.getInstance().addToCup(this);
	}
}
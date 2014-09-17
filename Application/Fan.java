package KAT;

public class Fan extends MagicEvent {
	public Fan() {
		super("Images/Magic_Fan.png", "Images/Creature_Back.png", "Fan");
		setOwner(null);
		setDescription("Use this when another player uses \"Ballon\", \"Dust of Defense\", or \"Weather Control\"!\nSee the leaflet for more information");
	}

	/*
	 * The Fan is only playable when another player uses the Balloon, the Dust of Defense, or Weather Control.
	 */
	@Override
	public boolean isPlayable() {
		return false;
	}

	/*
	 * When used against the Balloon:
	 * Sends the Balloon into any hex adjacent to its target hex (user chooses which).
	 * If it is displaced into a sea hex, the Balloon is lots but its passengers may escape to a friendly adjacent hex (if exists).
	 *
	 * When used against the Dust of Defense:
	 * Negates the ability.
	 *
	 * When used agains Weather Control:
	 * Moves the Black Cloud to an adjacent hex, or removes the Black Cloud from the board.
	 */
	public void performAbility() {
		if (TheCup.getInstance().getRemaining().size() != 0)
			TheCup.getInstance().addToCup(this);
	}
}
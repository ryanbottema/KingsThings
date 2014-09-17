package KAT;

public class Elixir extends MagicEvent {
	public Elixir() {
		super("Images/Magic_Elixir.png", "Images/Creature_Back.png", "Elixir");
		setOwner(null);
		setDescription("Use the Elixir to cleanse your army of Teeniepox or the Plague!\nSee the leaflet for more info");
	}

	/*
	 * Playable any time the owner would be affected by Teeniepox or the Plague.
	 */
	@Override
	public boolean isPlayable() {
		return false;
	}

	/*
	 * The Elixir cancels any effects Teeniepox or the Plague would have against your entire army.
	 *
	 * Elixir then returns to the cup.
	 */
	public void performAbility() {
		if (TheCup.getInstance().getRemaining().size() != 0)
			TheCup.getInstance().addToCup(this);
	}
}
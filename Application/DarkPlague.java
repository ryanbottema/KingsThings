package KAT;

public class DarkPlague extends RandomEvent {
	
	public DarkPlague() {
		super("Images/Event_DarkPlague.png", "Images/Creature_Back.png", "Dark Plague");
		setOwner(null);
		setDescription("Dark Plague causes each player to lose creatures from ALL of their hexes!\nSee the Leaflet for the in-depth rules.");
	}


	/*
	 * For each hex that any player controls, count up the combat values of all cities, villages,
	 * and forts. This hex will then lose that many creatures.
	 *
	 * Cities/Villages/Forts can also be removed if the user wants to keep his/her army.
	 * Cities/Villages count as one creature.
	 * Forts count for as many creatures as they have levels. Can remove one or more levels from a fort
	 * instead of a creature, as one fort level == one creature.
	 */
	@Override
	public void performAbility() {
		GameLoop.getInstance().unPause();
	}
}
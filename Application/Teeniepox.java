package KAT;

public class Teeniepox extends RandomEvent {

	public Teeniepox() {
		super("Images/Event_Teeniepox.png", "Images/Creature_Back.png", "Teeniepox");
		setOwner(null);
		setDescription("You may choose another player and inflict his largest creature stack with Teeniepox!\nSee the Leaflet for the in-depth rules.");
	}

	/*
	 * Choose another player. This player rolls one die.
	 * If the roll is either 1 or 6, Teeniepox has no effect.
	 * If the roll is 2 through 5, find the player's largest stack on the board and remove
	 * the rolled amount of counters from the hex they are on.
	 *
	 * When determining the largest stack, count each village, city, and fort by its combat value.
	 * Creatures and special characters count as one.
	 *
	 * If a tie occurs, the player being affected chooses which one s/he loses.
	 *
	 * If not enough creatures exist on the hex being infected, the player must reduce
	 * forts, cities, and villages to meet the loss requirements.
	 */
	@Override
	public void performAbility() {
		GameLoop.getInstance().unPause();
	}
}
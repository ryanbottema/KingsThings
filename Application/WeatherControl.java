package KAT;

public class WeatherControl extends RandomEvent {
	private String blackCloud;

	public WeatherControl() {
		super("Images/Event_Weather.png", "Images/Creature_Back.png", "Weather Control");
		setOwner(null);
		setDescription("You can cause an opponents hex to have a case of the bad weather!\nSee the Leaflet for the in-depth rules.");
	}

	@Override
	public boolean isPlayable() {
		if (GameLoop.getInstance().getPhase() != 4)
			return false;

		for (Terrain t : getOwner().getHexesWithPiece()) {
			CreatureStack c = t.getContents(getOwner().getName());
			for (int i = 0; i < c.filterCreatures(c.getStack()).size(); i++) {
				if (c.filterCreatures(c.getStack()).get(i).isMagic())
					return true;
			}
		}
		return false;
	}

	/*
	 * Check if the hex you wish to change is within the range of one of your magic creatures, 
	 * i.e. within a distance smaller than your magic creature's combat value.
	 *
	 * Place the BlackCloud marker in the designated hex.
	 * The BlackCloud stays in this hex until someone else uses WeatherControl and moves the cloud to another hex,
	 * or until the hex is captured by another player.
	 *
	 * All counters in the hex with the cloud which are owned by the hex owner have their combat value reduced by one.
	 *
	 * Counters with a combat value of 0 are unable to fight but they are still allowed to take hits in combat.
	 */
	@Override
	public void performAbility() {
		GameLoop.getInstance().unPause();
	}
}
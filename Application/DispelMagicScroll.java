package KAT;

public class DispelMagicScroll extends MagicEvent {
	public DispelMagicScroll() {
		super("Images/Magic_DispelMagic.png", "Images/Creature_Back.png", "Dispel Magic Scroll");
		setOwner(null);
		setDescription("Thwart the use of an enemy player's magic creature or effect!\nSee the leaflet for more info");
	}

	/*
	 * DispelMagicScroll is only useable any time another player uses a magic item
	 * or magic event.
	 */
	@Override
	public boolean isPlayable() {
		return false;
	}

	/*
	 * When activated, the enemy player's magic item has no effect, and his magic creatures now fight in the melee step.
	 *
	 * The scroll affects one magic item and all magic creatures in an enemy force during battle.
	 *
	 * The effects last until the end of battle, but the scroll is returned to the cup as soon as it is played.
	 */
	public void performAbility() {
		if (TheCup.getInstance().getRemaining().size() != 0)
			TheCup.getInstance().addToCup(this);
	}
}
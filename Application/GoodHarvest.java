package KAT;

public class GoodHarvest extends RandomEvent {

	public GoodHarvest() {
		super("Images/Event_GoodHarvest.png", "Images/Creature_Back.png", "Good Harvest");
		setOwner(null);
		setDescription("You may collect gold as if this were the Gold Collection Phase!\nSee the Leaflet for the in-depth rules.");
	}

	/*
	 * The owner of this event can collect gold as if this were the gold collection phase.
	 * Do not collect gold for special income counters (including cities and villages).
	 */
	@Override
	public void performAbility() {
		int income = 0;

		System.out.println(getOwner().calculateIncome() + "=== calculated income");

        income += getOwner().getHexesOwned().size();
        for (Terrain hex : getOwner().getHexesOwned()) {
            if (hex.getFort() != null)
                income += hex.getFort().getCombatValue();
        }
        for( Terrain hex : getOwner().getHexesWithPiece() ){
        	if (hex.getContents(getOwner().getName()) != null) {
	            for( Piece p : hex.getContents(getOwner().getName()).getStack() ){
	                if( p.getType().equals("Special Character") ){
	                    income += 1;
	                }
	            }
        	}
        }
        System.out.println(getOwner().getName() + " just earned " + income + " gold from using Good Harvest");

        getOwner().addGold(income);

        PlayerBoard.getInstance().updateGold(getOwner());

        GameLoop.getInstance().unPause();
	}
}
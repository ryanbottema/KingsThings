package KAT;

import java.util.ArrayList;
import javafx.application.Platform;

public class WillingWorkers extends RandomEvent {

	public WillingWorkers() {
		super("Images/Event_WillingWorkers.png", "Images/Creature_Back.png", "Willing Workers");
		setOwner(null);
		setDescription("You can build a tower or upgrade a fort for free!\nSee the Leaflet for the in-depth rules.");
	}

	/*
	 * Place a tower in any hex you own that doesn't already contain a fort, or increase the level of an existing fort by one.
	 *
	 * A citadel may NOT be created with this event even if all of the citadel requirements are met.
	 */
	@Override
	public void performAbility() {
		//ClickObserver.getInstance().setTerrainFlag("Construction: ConstructFort");
		ClickObserver.getInstance().setActivePlayer(getOwner());
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Board.applyCovers();
                Game.getRackGui().setOwner(getOwner());
                Game.getHelpText().setText(getOwner().getName() + " used Willing Workers. Select a tile to put a fort on!");
            }
        });
        try { Thread.sleep(50); } catch( Exception e ){ return; }
        ArrayList<Terrain> ownedHexes = getOwner().getHexesOwned();

        for (final Terrain t : ownedHexes) {
        	if (t.getOwner().getName().equals(getOwner().getName())) {
        		Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                		t.uncover();
                    }
                });
        	}
        }

        try { Thread.sleep(2000); } catch( Exception e ){ return; }
		Terrain t = ClickObserver.getInstance().getClickedTerrain();

		for (Terrain h : ownedHexes) {
			if (t.compareTo(h) == 0) {
				if (t.getFort() != null) {
					if (t.getFort().getName().equals("Castle"))
						break;
				}
				System.out.println("=== willing workers constructing fort");
				getOwner().constructFort(t);
				t.setFortImage();
			}
		}

		try { Thread.sleep(2000); } catch( Exception e ){ return; }

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Board.removeCovers();
			}
		});

		GameLoop.getInstance().unPause();
	}
}
package KAT;

import javafx.application.Platform;
import java.util.Iterator;
import java.util.ArrayList;

public class Vandals extends RandomEvent {

	public Vandals() {
		super("Images/Event_Vandals.png", "Images/Creature_Back.png", "Vandals");
		setOwner(null);
		setDescription("Select an opponent and reduce the level of one of their forts!\nSee the Leaflet for the in-depth rules.");
	}

	/*
	 * The owner of this piece selects another player. This player then loses one fort level from any fort
	 * that he currently has on the board (this player chooses).
	 *
	 * If the fort is a tower it is eliminated.
	 * If the fort is a castle or a keep it's level is reduced by one.
	 * If the fort is a citadel, nothing happens.
	 */
	@Override
	public void performAbility() {
		ClickObserver.getInstance().setActivePlayer(getOwner());
		ClickObserver.getInstance().setClickedTerrain(null);
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Board.applyCovers();
                Game.getRackGui().setOwner(getOwner());
                Game.getHelpText().setText(getOwner().getName() + " used Vandals. Select a fort to downgrade!");
            }
        });
        try { Thread.sleep(50); } catch( Exception e ){ return; }
        ArrayList<Terrain> hexesToUncover = new ArrayList<Terrain>();

        Iterator<Coord> coordIter = Board.getTerrains().keySet().iterator();
        while (coordIter.hasNext()) {
        	Coord key = coordIter.next();
        	if (Board.getTerrains().get(key).getOwner() != this.getOwner()) {
        		if (Board.getTerrains().get(key).getFort() != null)
        			hexesToUncover.add(Board.getTerrains().get(key));
        	}
        }

        for (final Terrain t : hexesToUncover) {
        	Platform.runLater(new Runnable() {
                @Override
                public void run() {
            		t.uncover();
                }
            });
        }

        while (ClickObserver.getInstance().getClickedTerrain() == null) {
			try { Thread.sleep(100); } catch( Exception e ){ return; }        	
        }

        
		Terrain t = ClickObserver.getInstance().getClickedTerrain();
		try { Thread.sleep(2000); } catch( Exception e ){ return; }

		t.getFort().downgrade();

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
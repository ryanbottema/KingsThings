package KAT;

import java.util.ArrayList;

public class MotherLode extends RandomEvent {

	public MotherLode() {
		super("Images/Event_MotherLode.png", "Images/Creature_Back.png", "Mother Lode");
		setOwner(null);
		setDescription("You can collect LOTS of gold!\nSee the Leaflet for the in-depth rules.");
	}

	/*
	 * The owner collects gold equal to 2 * income from special income counters
	 * (this includes cities and villages).
	 *
	 * If the player owns the Dwarf King, the value of mines are quadrupled.
	 */
	@Override
	public void performAbility() {
		int income = 0;
		boolean hasKing = false;
		ArrayList<Integer> specialIncomeValues = new ArrayList<Integer>();
        
        for( Terrain hex : getOwner().getHexesWithPiece() ){
        	if (hex.getContents(getOwner().getName()) != null) {
	            for( Piece p : hex.getContents(getOwner().getName()).getStack() ){
	            	if (p.getName().equals("Dwarf King"))
	            		hasKing = true;
	                if( p instanceof SpecialIncome ) {
	                	specialIncomeValues.add(((SpecialIncome)p).getValue());
	                }
	            }
        	}
        }
        for (Integer i : specialIncomeValues) {
        	if (hasKing)
        		income += i * 4;
        	else
        		income += i * 2;
        }
        System.out.println(getOwner().getName() + " just received " + income + " gold from using Mother Lode");
        getOwner().addGold(income);
        PlayerBoard.getInstance().updateGold(getOwner());

        GameLoop.getInstance().unPause();
	}
}
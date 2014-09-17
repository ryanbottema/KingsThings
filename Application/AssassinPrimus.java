package KAT;

import javafx.application.Platform;
//
// AssassinPrimus.java
// kingsandthings/
// @author Brandon Schurman
//

public class AssassinPrimus extends SpecialCharacter implements Performable
{
	private Player assassin;
	private Player victim;
	private static boolean returnPiece;
	private static Terrain hex;
	private static Piece assassinPiece;
	private static Piece victimPiece;
    
    public static boolean isReturnPiece() {
		return returnPiece;
	}
	/**
     * CTOR
     */
    public AssassinPrimus(){
        super("Images/Hero_AssassinPrimus.png", "Images/Creature_Back.png", "Assassin Primus", "", 4, false, false, false, false);
        setType("Special Character");
    }

    /**
     * The assassin primus can attack any creature or special character
     * on the board during the special powers phase.
     * @param creature - the character on the board to attack
     */ 
    void attack( Creature creature ){
        creature.inflict();
    }

    public void specialAbility() {
    	assassin = owner;
    	Game.getHelpText().setText("Special Powers Phase: " + assassin.getName() + ", select a hex to plot an assassination on!");
    	ClickObserver.getInstance().setClickedTerrain(null);
    	hex = null;
    	
    	System.out.println(ClickObserver.getInstance().getClickedTerrain());
    	
    	while (ClickObserver.getInstance().getClickedTerrain() == null) {
    		try { Thread.sleep(100); } catch(Exception e) { return; }
    	}
    	
    	hex = ClickObserver.getInstance().getClickedTerrain();
    	java.util.ArrayList<Coord> coords = new java.util.ArrayList<Coord>();
    	coords.add(hex.getCoords());
    	Board.applyCovers(coords);
    	victim = hex.getOwner();
    	System.out.println(victim.getName());
    	
        int thiefRoll = -1;
        int victimRoll = -1;

        
        DiceGUI.getInstance().uncover();

        System.out.println("setting active player to assassin");
        //The initial dice roll. The Thief rolls, and then the Victim rolls
        ClickObserver.getInstance().setActivePlayer(assassin);
        Game.getHelpText().setText(assassin.getName() + " roll the dice!");
        while (thiefRoll == -1) {
            try { Thread.sleep(100); } catch( Exception e ){ return; }
            thiefRoll = Dice.getFinalVal();
        }

        ClickObserver.getInstance().setActivePlayer(victim);

        try { Thread.sleep(1000); } catch( Exception e ){ return; }

        DiceGUI.getInstance().uncover();
        DiceGUI.getInstance().setFaceValue(0);

        //The Victim rolls, trying to get a roll higher than the Thief
        Game.getHelpText().setText(victim.getName() + ", you must roll higher than a " + thiefRoll + " to prevent an assassination!");

        try { Thread.sleep(1000); } catch( Exception e ){ return; }

        while (victimRoll == -1) {
            try { Thread.sleep(100); } catch( Exception e ){ return; }
            victimRoll = Dice.getFinalVal();
        }

        try { Thread.sleep(1000); } catch( Exception e ){ return; }
        
        ClickObserver.getInstance().setActivePlayer(assassin);

        //The Victim rolled higher than the thief. This means the ability failed, nothing else happens
        if (victimRoll > thiefRoll) {
            Game.getHelpText().setText("Congratulations " + victim.getName() + ", the assassination was thwarted!");
            try { Thread.sleep(1000); } catch( Exception e ){ return; }
        }
        //The Thief and the Victim tied. This means each player rolls again
        else if (victimRoll == thiefRoll) {
            Game.getHelpText().setText("You tied! Roll again " + assassin.getName());

            DiceGUI.getInstance().uncover();
            DiceGUI.getInstance().setFaceValue(0);

            try { Thread.sleep(1000); } catch( Exception e ){ return; }

            //First the thief rolls
            thiefRoll = -1;
            while (thiefRoll == -1) {
                try { Thread.sleep(100); } catch( Exception e ){ return; }
                thiefRoll = Dice.getFinalVal();
            }

            try { Thread.sleep(1000); } catch( Exception e ){ return; }
            
            DiceGUI.getInstance().uncover();
            DiceGUI.getInstance().setFaceValue(0);

            //Now the Victim rolls.
            ClickObserver.getInstance().setActivePlayer(victim);
            Game.getHelpText().setText(assassin.getName() + " rolled a " + thiefRoll + ". See if you can beat them this time, " + victim.getName() + "!");
            victimRoll = -1;
            while (victimRoll == -1) {
                try { Thread.sleep(100); } catch( Exception e ){ return; }
                victimRoll = Dice.getFinalVal();
            }

            try { Thread.sleep(1000); } catch( Exception e ){ return; }

            ClickObserver.getInstance().setActivePlayer(assassin);

            //If the Thief beats or ties with the Victim, nothing else happens i.e. the game carries on like normal
            if (thiefRoll >= victimRoll) {
                Game.getHelpText().setText("STALEMATE! Sort of... Congratulations " + victim.getName() + ", the assassination attempt has been thwarted!");
                try { Thread.sleep(1000); } catch( Exception e ){ return; }
            }
            //If the victim beats the thief, Master Thief is returned to the special character collection
            else {
            	Game.getHelpText().setText("Today is a sad day... " + assassin.getName() + "'s Assassin Primus is returning to the rest of the Special Characters!");
            	try { Thread.sleep(2000); } catch( Exception e ){ return; }
            	returnPiece = true;
            }
        }
        else {
        	Game.getHelpText().setText("HUZZAH! " + assassin.getName() + ", you may now pick a piece to assassinate!");
        	
        	//hex.getContents(victim.getName()).
        	
        	try { Thread.sleep(2000); } catch( Exception e ){ return; }
        	
        	ClickObserver.getInstance().setCreatureFlag("Select Assassin Piece");
        	Platform.runLater(new Runnable() {
        		@Override
        		public void run() {
        			for (int i = 0; i < hex.getContents(victim.getName()).getStack().size(); i++)
        				hex.getContents(victim.getName()).getStack().get(i).uncover();
        		}
        	});
        	
        	ClickObserver.getInstance().setClickedCreature(null);
        	
        	while (ClickObserver.getInstance().getClickedPiece() == null) {
        		try { Thread.sleep(100); } catch( Exception e ){ return; }
        	}
        	
        	assassinPiece = ClickObserver.getInstance().getClickedPiece();
        	
        	System.out.println(assassinPiece);
        	
        	Game.getHelpText().setText(victim.getName() + ", you may now choose a piece to be potentially slaughtered");
        	
        	try { Thread.sleep(2000); } catch( Exception e ){ return; }
        	
        	ClickObserver.getInstance().setCreatureFlag("Select Victim Piece");
        	
        	ClickObserver.getInstance().setClickedCreature(null);
        	
        	while (ClickObserver.getInstance().getClickedPiece() == null) {
        		try { Thread.sleep(100); } catch( Exception e ){ return; }
        	}
        	
        	victimPiece = ClickObserver.getInstance().getClickedPiece();
        	
        	System.out.println(victimPiece);
        	
        	Game.getHelpText().setText("And now " + assassin.getName() + " can roll to determine which piece gets brutally murdered");
        	
        	try { Thread.sleep(2000); } catch( Exception e ){ return; }
        	DiceGUI.getInstance().setFaceValue(0);
        	DiceGUI.getInstance().uncover();
        	
        	thiefRoll = -1;
            while (thiefRoll == -1) {
                try { Thread.sleep(100); } catch( Exception e ){ return; }
                thiefRoll = Dice.getFinalVal();
            }
            
            if (thiefRoll < 4) {
            	Game.getHelpText().setText(assassin.getName() + "'s choice has been assassinated!");
            	hex.removeFromStack(victim.getName(), (Creature)assassinPiece);
            	Platform.runLater(new Runnable() {
            		@Override
            		public void run() {
            			InfoPanel.showTileInfo(hex);
            		}
            	});
            	try { Thread.sleep(2000); } catch( Exception e ){ return; }
            }
            else if (thiefRoll > 3) {
            	Game.getHelpText().setText(victim.getName() + "'s choice has been assassinated!");
            	hex.removeFromStack(victim.getName(), (Creature)victimPiece);
            	Platform.runLater(new Runnable() {
            		@Override
            		public void run() {
            			InfoPanel.showTileInfo(hex);
            		}
            	});
            	try { Thread.sleep(2000); } catch( Exception e ){ return; }
            }        	
        }
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                DiceGUI.getInstance().setFaceValue(0);
                DiceGUI.getInstance().cover();
                Board.removeCovers();
            }
        });
        GameLoop.getInstance().unPause();
    }
    
    public static void setAssassinPiece(Piece p) { assassinPiece = p; }
    public static void setVictimPiece(Piece p) { victimPiece = p; }

    public void performAbility() {
        return;
    }

    public boolean hasSpecial() { return true; }
    public boolean hasPerform() { return false; }
}

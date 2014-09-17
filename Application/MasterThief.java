package KAT;
//
// MasterThief.java
// kingsandthings/
// @author Brandon Schurman
//

import javafx.application.Platform;
import java.util.Random;

public class MasterThief extends SpecialCharacter implements Performable
{
    private static Player victim;
    private static Player thief;
    private static boolean returnPiece;
    
    public static boolean isReturnPiece() {
		return returnPiece;
	}

	/**
     * CTOR 
     */
    public MasterThief(){
        super("Images/Hero_MasterThief.png", "Images/Creature_Back.png", "Master Thief", "", 4, false, false, false, false);
        setType("Special Character");
        victim = null;
    }

    public static void stealGold(){
    	thief.addGold(victim.getGold());
    	victim.removeGold(victim.getGold());
    	PlayerBoard.getInstance().updateGold(thief);
    	PlayerBoard.getInstance().updateGold(victim);
    	//PlayerBoard.getInstance().coverButtons(victim);
    	GameLoop.getInstance().unPause();
    }

    public static void stealRandomCounter(){
    	Random rand = new Random();
    	int index = rand.nextInt(victim.getPlayerRack().getPieces().size());
    	thief.getPlayerRack().addPiece(victim.getPlayerRack().getPieces().get(index));
    	victim.getPlayerRack().removePiece(index);
    	//PlayerBoard.getInstance().coverButtons(victim);
    	GameLoop.getInstance().unPause();
    }

    public void performAbility() { return; }

    public void specialAbility() {
    	System.out.println("==STACKED IN" + getStackedIn().getStack());
        ClickObserver.getInstance().setPlayerFlag("Master Thief: SelectingPlayerToStealFrom");
        thief = owner;
        int thiefRoll = -1;
        int victimRoll = -1;

        Game.getHelpText().setText("Special Powers Phase: " + thief.getName() + ", select which player to steal from.");
        DiceGUI.getInstance().uncover();

        while (GameLoop.getInstance().getPaused()) {
            try { Thread.sleep(100); } catch(Exception e) { return; }
        }

        ClickObserver.getInstance().setPlayerFlag("");

        //The initial dice roll. The Thief rolls, and then the Victim rolls
        ClickObserver.getInstance().setActivePlayer(thief);
        Game.getHelpText().setText(thief.getName() + " roll the dice!");
        while (thiefRoll == -1) {
            try { Thread.sleep(100); } catch( Exception e ){ return; }
            thiefRoll = Dice.getFinalVal();
        }

        ClickObserver.getInstance().setActivePlayer(victim);

        try { Thread.sleep(1000); } catch( Exception e ){ return; }

        DiceGUI.getInstance().uncover();
        DiceGUI.getInstance().setFaceValue(0);

        //The Victim rolls, trying to get a roll higher than the Thief
        Game.getHelpText().setText(victim.getName() + ", you must roll higher than a " + thiefRoll + " to prevent your gold or a counter from being stolen!");

        try { Thread.sleep(1000); } catch( Exception e ){ return; }

        while (victimRoll == -1) {
            try { Thread.sleep(100); } catch( Exception e ){ return; }
            victimRoll = Dice.getFinalVal();
        }

        try { Thread.sleep(1000); } catch( Exception e ){ return; }
        
        ClickObserver.getInstance().setActivePlayer(thief);

        //The Victim rolled higher than the thief. This means the ability failed, nothing else happens
        if (victimRoll > thiefRoll) {
            Game.getHelpText().setText("Congratulations " + victim.getName() + ", your gold and counters are safe!");
            try { Thread.sleep(1000); } catch( Exception e ){ return; }
        }
        //The Thief and the Victim tied. This means each player rolls again
        else if (victimRoll == thiefRoll) {
            Game.getHelpText().setText("You tied! Roll again " + thief.getName());

            DiceGUI.getInstance().uncover();
            DiceGUI.getInstance().setFaceValue(0);

            try { Thread.sleep(1000); } catch( Exception e ){ return; }

            //First the thief rolls
            thiefRoll = -1;
            while (thiefRoll == -1) {
                try { Thread.sleep(100); } catch( Exception e ){ return; }
                thiefRoll = Dice.getFinalVal();
            }

            DiceGUI.getInstance().uncover();
            DiceGUI.getInstance().setFaceValue(0);

            try { Thread.sleep(1000); } catch( Exception e ){ return; }

            //Now the Victim rolls.
            ClickObserver.getInstance().setActivePlayer(victim);
            Game.getHelpText().setText(thief.getName() + " rolled a " + thiefRoll + ". See if you can beat them this time, " + victim.getName() + "!");
            victimRoll = -1;
            while (victimRoll == -1) {
                try { Thread.sleep(100); } catch( Exception e ){ return; }
                victimRoll = Dice.getFinalVal();
            }

            try { Thread.sleep(1000); } catch( Exception e ){ return; }

            ClickObserver.getInstance().setActivePlayer(thief);

            //If the Thief beats or ties with the Victim, nothing else happens i.e. the game carries on like normal
            if (thiefRoll >= victimRoll) {
                Game.getHelpText().setText("STALEMATE! Sort of... Congratulations " + victim.getName() + ", your gold and counters are safe!");
                try { Thread.sleep(1000); } catch( Exception e ){ return; }
            }
            //If the victim beats the thief, Master Thief is returned to the special character collection
            else {
            	Game.getHelpText().setText("Today is a sad day... " + thief.getName() + "'s Master Thief is returning to the rest of the Special Characters!");
            	try { Thread.sleep(2000); } catch( Exception e ){ return; }
            	returnPiece = true;
            }
        }
        //If the thief wins, he gets to choose to either steal all of the victim's gold, or steal a random counter from their rack.
        else if (thiefRoll > victimRoll) {
            Game.getHelpText().setText("HUZZAH! " + thief.getName() + ", you may now steal either all of " + victim.getName() + "'s gold, or a random counter from their rack! Choose wisely... or not!");
            try { Thread.sleep(1000); } catch( Exception e ){ return; }

            PlayerBoard.getInstance().uncoverButtons(victim);
            
            GameLoop.getInstance().pause();
            
            while (GameLoop.getInstance().getPaused()) {
            	try { Thread.sleep(100); } catch(Exception e) { return; }
            }
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                DiceGUI.getInstance().setFaceValue(0);
                DiceGUI.getInstance().cover();
            }
        });
        GameLoop.getInstance().unPause();
    }

    public boolean hasSpecial() { return true; }
    public boolean hasPerform() { return false; }

    public static void setVictim(Player p) { victim = p; }
}

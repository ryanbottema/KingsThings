package KAT;

import javafx.application.Platform;

public class Defection extends RandomEvent {

    public Defection() {
        super("Images/Event_Defection.png", "Images/Creature_Back.png", "Defection");
        setOwner(null);
        setDescription("You may immediately add a special character to your forces!\nSee the Leaflet for the in-depth rules.");
    }

    /*
     * Decide which special character you want to control (can already be on the board under control
     * of another player, or can be in the bank).
     *
     * The player wanting the special character rolls two dice against another player. If his/her roll is higher,
     * the special character is immediately moved to any hex under his/her control.
     *
     * Gold may not be used to modify this roll.
     */
    @Override
    public void performAbility() {
        GameLoop.getInstance().pause();
        SpecialCharView.setCurrentPlayer(getOwner());
        int systemRoll = -1;
        int ownerRoll = -1;
        System.out.println("+++using the defection random event");
        DiceGUI.getInstance().uncover();
        DiceGUI.getInstance().setFaceValue(0);
        Game.getHelpText().setText(getOwner().getName() + " has used the Random Event Defection!");
        
        try { Thread.sleep(2000); } catch(Exception e) { return; }

        System.out.println("dice was uncovered");

        System.out.println(getOwner().getName());
        
        System.out.println("game text was set");

        
        ClickObserver.getInstance().setActivePlayer(getOwner());
        System.out.println("waiting for the user to click the dice"); 

        while (systemRoll == -1) {
            try { Thread.sleep(100); } catch( Exception e ){ return; }
            systemRoll = Dice.getFinalVal();
        }

        System.out.println(systemRoll + " is what the user rolled");     

        

        DiceGUI.getInstance().uncover();
        DiceGUI.getInstance().setFaceValue(0);

        Game.getHelpText().setText(getOwner().getName() + ", you must roll higher than " + systemRoll + " to successfully steal a Special Character");

        try { Thread.sleep(2000); } catch(Exception e) { return; }

        while (ownerRoll == -1) {
            try { Thread.sleep(100); } catch( Exception e ){ return; }
            ownerRoll = Dice.getFinalVal();
        }

        if (ownerRoll > systemRoll) {
            Game.getHelpText().setText("Congratulations, you rolled higher! Select the Special Character you would like to steal!");
            SpecialCharView.setDefection(true);
            SpecialCharView.getSpecialButton().activate();
            System.out.println("waiting for the user to press recruit");
            System.out.println(SpecialCharView.getRecruitPressed() ? "true" : "false");
            SpecialCharView.setRecruitPressed(false);
            while (!SpecialCharView.getRecruitPressed()) {
                try { Thread.sleep(100); } catch( Exception e ){ return; }
            }
            System.out.println("user pressed recruit");
        }

        else {
            Game.getHelpText().setText("Sorry friend! No free special character for you!");
            DiceGUI.getInstance().setFaceValue(0);
            DiceGUI.getInstance().cover();
        }

        try { Thread.sleep(2000); } catch(Exception e) { return; }

        SpecialCharView.getSpecialButton().deactivate();
        GameLoop.getInstance().unPause();
    }
}
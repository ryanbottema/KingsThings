package KAT;

import javafx.scene.layout.BorderPane;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;

import java.util.ArrayList;

import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SpecialCharView {
    private static GameButton specialCharButton, recruitButton, cancelButton;
    private static ArrayList<String> characterList;
    private static ArrayList<String> charactersInPlay;
    private static ArrayList<ImageView> images;
    private static GridPane characterGrid;
    private SpecialCharacterFactory factory;
    private String selectedCharacter;
    private static Player currentPlayer;
    private ImageView selectedImage;
    private int rolledValue;
    private static boolean defectionUsed;
    private static boolean recruitPressed;

    public SpecialCharView(BorderPane bp) {
        rolledValue = 0;
        factory = new SpecialCharacterFactory();
        characterList = new ArrayList<String>();
        images = new ArrayList<ImageView>();
        charactersInPlay = new ArrayList<String>();
        selectedCharacter = "";
        defectionUsed = false;
        recruitPressed = false;
        specialCharButton = new GameButton(180, 35, Game.getWidth()-380, 160, "Special Characters", characterHandler);
        recruitButton = new GameButton(100, 35, Game.getWidth()-380, 160, "Recruit", recruitHandler);
        cancelButton = new GameButton(80, 35, Game.getWidth()-280, 160, "Cancel", cancelHandler);
        
        recruitButton.hide();
        cancelButton.hide();
        specialCharButton.deactivate();

        characterGrid = new GridPane();
        characterGrid.setVgap(5);
        characterGrid.setHgap(5);

        setupGrid();

        characterList.add("Arch Cleric");
        characterList.add("Arch Mage");
        characterList.add("Assassin Primus");
        characterList.add("Baron Munchhausen");
        characterList.add("Deerhunter");
        characterList.add("Desert Master");
        characterList.add("Dwarf King");
        characterList.add("Elf Lord");
        characterList.add("Forest King");
        characterList.add("Ghaogh II");
        characterList.add("Grand Duke");
        characterList.add("Ice Lord");
        characterList.add("Jungle Lord");
        characterList.add("Lord Of The Eagles");
        characterList.add("Marksman");
        characterList.add("Master Thief");
        characterList.add("Mountain King");
        characterList.add("Plains Lord");
        characterList.add("Sir Lance-A-Lot");
        characterList.add("Swamp King");
        characterList.add("Sword Master");
        characterList.add("Warlord");       

        characterGrid.setVisible(false);
        characterGrid.relocate(bp.getWidth()-565, 10);

        bp.getChildren().addAll(characterGrid, specialCharButton.getNode(), recruitButton.getNode(), cancelButton.getNode());
    }

    private void setupGrid() {
        images.add(new ImageView(new Image("Images/Hero_ArchCleric.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_ArchMage.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_AssassinPrimus.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_BaronMunchhausen.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_Deerhunter.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_DesertMaster.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_DwarfKing.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_ElfLord.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_ForestKing.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_GhaoghII.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_GrandDuke.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_IceLord.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_JungleLord.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_LordOfTheEagles.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_Marksman.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_MasterThief.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_MountainKing.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_PlainsLord.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_SirLance-A-Lot.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_SwampKing.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_SwordMaster.png", 44, 44, false, false)));
        images.add(new ImageView(new Image("Images/Hero_Warlord.png", 44, 44, false, false)));
        images.add(new ImageView());
        images.add(new ImageView());

        for (int i = 0; i < 22; i++) {
            images.get(i).addEventHandler(MouseEvent.MOUSE_CLICKED, imageHandler);
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                characterGrid.add(images.get(j+8*i), j, i);
            }
        }
    }

    /*
     * Event Handler for the Special Characters button.
     * Shows the grid of available special characters.
     * Shows the Recruit and Cancel buttons.
     */
    EventHandler<MouseEvent> characterHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            specialCharButton.hide();
            recruitButton.show();
            recruitButton.activate();
            cancelButton.show();
            cancelButton.activate();
            characterGrid.setVisible(true);
            enableAll();
            recruitPressed = false;
        }
    };

    /*
     * Event Handler for the Recruit Button.
     * Hides the grid of available characters.
     * Shows the Special Characters button.
     *
     * The Player selects the character they would like the recruit. They then press Recruit and the Dice becomes available to roll.
     * If the dice roll is less than the combat value of the character, they do not recruit that character.
     * Else, the character is placed on their currently selected tile.
     */
    EventHandler<MouseEvent> recruitHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            System.out.println("RECRUITING " + selectedCharacter);
            final SpecialCharacter creatureToRecruit = factory.createSpecialCharacter(selectedCharacter);
            rolledValue = Dice.getFinalVal();
            System.out.println(creatureToRecruit);
            System.out.println(rolledValue);
            System.out.println(creatureToRecruit.getCombatValue());

            if (defectionUsed) {
                Game.getHelpText().setText("Successfully recruited " + selectedCharacter);
                Terrain selectedHex = ClickObserver.getInstance().getClickedTerrain();
                creatureToRecruit.setOwner(currentPlayer);
                currentPlayer.playPiece(creatureToRecruit, selectedHex);
                if (creatureToRecruit.hasPerform())
                    creatureToRecruit.performAbility();
                selectedImage.setOpacity(0.2);
                charactersInPlay.add(selectedCharacter);
                selectedCharacter = "";
                selectedImage = null;
                defectionUsed = false;
            }
                        
            else if (rolledValue < -3) {
               Game.getHelpText().setText("Failed to recruit " + selectedCharacter);
            }
            else {
                Game.getHelpText().setText("Successfully recruited " + selectedCharacter);
                creatureToRecruit.setOwner(currentPlayer);
                             
                currentPlayer.playPiece(creatureToRecruit, ClickObserver.getInstance().getClickedTerrain());
                if (creatureToRecruit.hasPerform())
                    creatureToRecruit.performAbility();
                selectedImage.setOpacity(0.2);
                charactersInPlay.add(selectedCharacter);
                selectedCharacter = "";
                selectedImage = null;
            }
            recruitButton.hide();
            recruitButton.deactivate();
            cancelButton.hide();
            cancelButton.deactivate();
            specialCharButton.show();
            specialCharButton.deactivate();
            characterGrid.setVisible(false);
            DiceGUI.getInstance().setFaceValue(0);
            recruitPressed = true;
        }
    };

    /*
     * Event Handler for the Recruit Button.
     * Hides the grid of available characters.
     */
    EventHandler<MouseEvent> cancelHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            recruitButton.hide();
            recruitButton.deactivate();
            cancelButton.hide();
            cancelButton.deactivate();
            specialCharButton.show();
            specialCharButton.activate();
            characterGrid.setVisible(false);
            selectedCharacter = "";
            DiceGUI.getInstance().setFaceValue(0);
            recruitPressed = false;
        }
    };

    /*
     * Event Handler for each of the images. When an image is clicked, it sets the opacity to 50% signifying that it has been selected and then
     * updates the Strings and ImageViews accordingly.
     */
    EventHandler<MouseEvent> imageHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            if (images.get(images.indexOf((ImageView)e.getSource())).getOpacity() == 0.5)
                enableAll();
            else if (images.get(images.indexOf((ImageView)e.getSource())).getOpacity() == 1.0) {
                enableAll();
                images.get(images.indexOf((ImageView)e.getSource())).setOpacity(0.5);
                selectedCharacter = characterList.get(images.indexOf((ImageView)e.getSource()));
                selectedImage = (ImageView)e.getSource();

                
            }
        }
    };

    //enables all images (sets opacity to 100%)
    public void enableAll() {
        for (int i = 0; i < images.size()-2; i++) {
            if (images.get(i).getOpacity() != 0.2)
                images.get(i).setOpacity(1.0);
        }
    }

    public static void setCurrentPlayer(Player p) { currentPlayer = p; }
    public static Player getCurrentPlayer() { return currentPlayer; }
    
    public static void removeFromPlay(String name) {
    	charactersInPlay.remove(name);
    	images.get(characterList.indexOf(name)).setOpacity(1.0);
    }

	public static ArrayList<String> getCharactersInPlay() { return charactersInPlay; }

    public static GameButton getSpecialButton() { return specialCharButton; }
    public static GridPane getCharacterGrid() { return characterGrid; }
    public static void setDefection(boolean b) { defectionUsed = b; }
    public static boolean getRecruitPressed() { return recruitPressed; }
    public static void setRecruitPressed(boolean b) { recruitPressed = b; }
}
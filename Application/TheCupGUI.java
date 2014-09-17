package KAT;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.control.TextField;
import java.lang.Character;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/*
 * Class for the GUI portion of the cup. Needs to be cleaned up.
 */
public class TheCupGUI {
    private ImageView            cupImage; //Image representing the cup.
    private VBox                 cupBox, cupVBoxRecruit; //VBox to hold all of the components
    private HBox                 cupHBoxDraw, cupHBoxRecruit;
    private TheCup               cup; //One instance of the cup
    private static GameButton    drawButton, freeButton, paidButton;
    private static TextField     textField; //used for specifying how many pieces to draw from the cup
    private static PlayerRackGUI rackG;
    private static GameLoop      gameLoop;
    private static boolean       paidPressed, freePressed;

    public TheCupGUI(BorderPane bp, PlayerRackGUI rg) {
        cupBox = new VBox(5);
        cupVBoxRecruit = new VBox(5);
        cupHBoxDraw = new HBox(5);
        cupHBoxRecruit = new HBox(5);
        paidPressed = false;

        cup = TheCup.getInstance();
        gameLoop = GameLoop.getInstance();

        rackG = rg;

        draw(bp);
        
        bp.getChildren().add(cupBox);
    }


    /*
     * Method to show all of the GUI components of the cup.
     */
    private void draw(BorderPane bp) {
        //Displays the cup
        cupImage = new ImageView(new Image("Images/Dtopnica_chalice.png", 100,100,false,false));

        textField = new TextField();
        textField.setPromptText("How many?");
        textField.setPrefColumnCount(5);
        textField.setMinSize(90, 20);

        drawButton = new GameButton(65, 25, "Draw", drawHandler);
        drawButton.deactivate();
        

        freeButton = new GameButton(50, 45, "Free", null);
        freeButton.getImgV().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                gameLoop.setFree(true);
                gameLoop.setPaid(false);
                freeButton.setDisable(true);
                textField.setDisable(true);
                paidPressed = false;
                freePressed = true;
            }
        });
        freeButton.deactivate();

        paidButton = new GameButton(50, 45, "Paid", null);
        paidButton.getImgV().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                gameLoop.setPaid(true);
                gameLoop.setFree(false);
                paidButton.setDisable(true);
                textField.setDisable(false);
                paidPressed = true;
            }
        });
        paidButton.deactivate();

        cupHBoxDraw.getChildren().addAll(textField, drawButton.getNode());
        cupVBoxRecruit.getChildren().addAll(freeButton.getNode(), paidButton.getNode());
        cupHBoxRecruit.getChildren().addAll(cupImage, cupVBoxRecruit);

        cupBox.relocate(bp.getWidth() - 175, 50);
        cupBox.getChildren().addAll(cupHBoxRecruit, cupHBoxDraw);
    }

    //Handles when the user presses the draw button.
    EventHandler<MouseEvent> drawHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            if (e.getClickCount() == 1) {
                if (paidPressed && freePressed)
                    drawButton.setDisable(true);
                ArrayList<Piece> strList = new ArrayList<Piece>();
                if (paidPressed) {
                    if (sanitizeText(textField.getText()) * 5 > rackG.getOwner().getGold()) {
                        textField.setText("" + (rackG.getOwner().getGold() / 5));
                        rackG.getOwner().removeGold(sanitizeText(textField.getText()) * 5);
                    }
                    else {
                        rackG.getOwner().removeGold(sanitizeText(textField.getText()) * 5);
                    }
                }
                strList = cup.draw(sanitizeText(textField.getText()));
                textField.setText("");
                textField.setDisable(true);
                rackG.getOwner().getPlayerRack().addPieces(strList);
                if( GameLoop.getInstance().isNetworked() ){
                	ArrayList<Integer> pIDs = new ArrayList<Integer>();
                	for( Piece p : strList ){
                		pIDs.add(p.getPID());
                	}
                	HashMap<String,Object> map = new HashMap<String,Object>();
                	map.put("updateType", "removeFromCup");
                	map.put("pIDs", pIDs);
                	map.put("updateGold", true);
                	map.put("gold", rackG.getOwner().getGold());
                	NetworkGameLoop.getInstance().postGameState(map);
                }
            }
        }
    };

    /*
     * Method to update the various buttons depending on what phase the game is in.
     */
    public static void update() {
        if (gameLoop.getPhase() == 3) {
            drawButton.activate();
            freeButton.activate();
            if (rackG.getOwner().getGold() >= 5)
                paidButton.activate();
            else
                paidButton.deactivate();
        }
        else {
        	System.out.println("===deactivating buttons");
            drawButton.deactivate();
            paidButton.deactivate();
            freeButton.deactivate();
        }
    }

    /*
     * Method to set the text in the input field.
     */
    public static void setFieldText(String s) { 
        textField.setDisable(true);
        textField.setText(s);
    }

    /*
     * Method to sanitize the value of a string and convert it to an int.
     */
    private int sanitizeText(String s) {
        int val = 0;
        if (s.equals(""))
            return 0;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i)))
                return 0;
        }
        val = Integer.parseInt(s);
        if (val > 10)
            return 10;
        return val;
    }
}

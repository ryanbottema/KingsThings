package KAT;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.paint.Color;
import javafx.scene.layout.GridPane;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.collections.FXCollections;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;

public class DiceGUI {
	
    private static DiceGUI uniqueInstance;
//    private BorderPane borPane;
    private Rectangle mainPiece, cover, highlighter;
    private GridPane valueGrid;
    private Circle[][] valueDots;
    private Group diceGroup;
    private double xPos, yPos, width, height;

    
    private DiceGUI() {
    	
    	xPos = InfoPanel.getWidth() + 10;
    	yPos = Game.getHeight() * 0.05;
    	width = Game.getWidth() * 0.1;
    	height = Game.getHeight() * 0.1;
        
        draw();
    }

    public void draw() {

        mainPiece = RectangleBuilder.create()
                .width(75)
                .height(75)
                .fill(Color.WHITE)
                .stroke(Color.BLACK)
                .strokeType(StrokeType.INSIDE)
                .arcHeight(10)
                .arcWidth(10)
                .onMouseClicked(handlerOne)
                .onMouseEntered(mouseEnter)
                .onMouseExited(mouseExit)
                .build();
        
        highlighter = RectangleBuilder.create()
                .width(75)
                .height(75)
                .fill(Color.TRANSPARENT)
                .stroke(Color.WHITESMOKE)
                .strokeWidth(3)
                .effect(new GaussianBlur())
                .arcHeight(10)
                .arcWidth(10)
                .mouseTransparent(true)
                .visible(false)
                .build();
        
        cover = RectangleBuilder.create()
                .width(75)
                .height(75)
                .fill(Color.DARKSLATEGRAY)
                .opacity(0.5)
                .arcHeight(10)
                .arcWidth(10)
                .disable(false)
                .build();

        valueGrid = new GridPane();

        valueDots = new Circle[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
            	valueDots[i][j] = new Circle(5.0);
            	valueDots[i][j].setFill(Color.BLACK);
            	valueDots[i][j].setMouseTransparent(true);
            }
        }

        valueGrid.setMouseTransparent(true);
        valueGrid.setLayoutX(8);
        valueGrid.getColumnConstraints().add(new ColumnConstraints(23));
        valueGrid.getRowConstraints().add(new RowConstraints(25));
        valueGrid.getColumnConstraints().add(new ColumnConstraints(23));
        valueGrid.getRowConstraints().add(new RowConstraints(25));
        valueGrid.getColumnConstraints().add(new ColumnConstraints(23));
        valueGrid.getRowConstraints().add(new RowConstraints(25));
        
        diceGroup = GroupBuilder.create()
        		.children(mainPiece, valueGrid, highlighter, cover)
        		.layoutX(xPos)
        		.layoutY(yPos)
        		.build();
        
        Game.getRoot().getChildren().add(diceGroup);

        initDots();
    }

    private void update() {
        diceGroup.setVisible(true);
        setFaceValue(0);
    }

    public static DiceGUI getInstance() {
        if (uniqueInstance == null)
            uniqueInstance = new DiceGUI();
        return uniqueInstance;
    }

    private void initDots() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
            	valueGrid.add(valueDots[i][j],i,j);
            	valueDots[i][j].setVisible(false);
            }
        }
    }

    private void hideDots() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
            	valueDots[i][j].setVisible(false);
            }
        }
    }

    public void setFaceValue(int val) {
        switch (val) {
            case 1: 
                hideDots();
                valueDots[1][1].setVisible(true);
                break;
                    

            case 2:
                hideDots();
                valueDots[1][0].setVisible(true);
                valueDots[1][2].setVisible(true);
                break;

            case 3: 
                hideDots();
                valueDots[0][0].setVisible(true);
                valueDots[1][1].setVisible(true);
                valueDots[2][2].setVisible(true);
                break;

            case 4: 
                hideDots();
                valueDots[0][0].setVisible(true);
                valueDots[2][0].setVisible(true);
                valueDots[0][2].setVisible(true);
                valueDots[2][2].setVisible(true);
                break;

            case 5: 
                hideDots();
                valueDots[0][0].setVisible(true);
                valueDots[2][0].setVisible(true);
                valueDots[1][1].setVisible(true);
                valueDots[0][2].setVisible(true);
                valueDots[2][2].setVisible(true);
                break;

            case 6:
                hideDots();
                valueDots[0][0].setVisible(true);
                valueDots[0][1].setVisible(true);
                valueDots[0][2].setVisible(true);
                valueDots[2][0].setVisible(true);
                valueDots[2][1].setVisible(true);
                valueDots[2][2].setVisible(true);
                break;

            default:
                hideDots();
                break;
        }
    }

    EventHandler<MouseEvent> handlerOne = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            Dice.roll();
        }
    };
    
    EventHandler<MouseEvent> mouseEnter = new EventHandler<MouseEvent>() {
    	 @Override
         public void handle(MouseEvent e) {
             highlighter.setVisible(true);
         }
    };
    
    EventHandler<MouseEvent> mouseExit = new EventHandler<MouseEvent>() {
   	 @Override
        public void handle(MouseEvent e) {
            highlighter.setVisible(false);
        }
    };

    public void cover() {
    	cover.setVisible(true);
    	cover.setDisable(false);
    }
    
    public void uncover() {
    	cover.setVisible(false);
    	cover.setDisable(true);
    }
}
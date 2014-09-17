package KAT;

import java.util.HashMap;
import java.util.Iterator;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.effect.GlowBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradientBuilder;
import javafx.scene.paint.StopBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;

public class PlayerBoard {
	
	private static Glow glow;
	private static double height;
	private static double width;
	private static Group playerBoardNode;
	private static double nameFontSize;
	private static double dataFontSize;
	private Font nameFont;
	private Font dataFont;

	private static HashMap<String, Player> players;
	private static HashMap<String, PlayerDisplay> playerDisplay;
	
	private double playerHeight;
	
	private static PlayerBoard uniqueInstance;
	
	public PlayerBoard() {
		
		glow = GlowBuilder.create().build();
		
		height = Game.getHeight()*0.6;
		width = Game.getWidth()*0.25;
		playerHeight = height/4;
		
		// Font stuff
		nameFontSize = Math.min(width*0.1, height*0.1);
		dataFontSize = Math.min(width*0.1, height*0.03);
		nameFont = Font.loadFont(getClass().getResourceAsStream("/Fonts/ITCBLKAD.TTF"), nameFontSize);
		dataFont = Font.loadFont(getClass().getResourceAsStream("/Fonts/Cardinal.ttf"), dataFontSize);
		
		players = new HashMap<String, Player>();
		playerDisplay = new HashMap<String, PlayerDisplay>();

		playerBoardNode = GroupBuilder.create()
				.layoutX(Game.getWidth() - width)
				.layoutY(Game.getHeight() * 0.3)
				.build();
		
		for (Player p : GameLoop.getInstance().getPlayers()) {
			new PlayerDisplay(p);
			updateGold(p);
		}
		
		Game.getRoot().getChildren().add(playerBoardNode);
		
	}
	
	
	public static PlayerBoard getInstance(){
        if(uniqueInstance == null){
            uniqueInstance = new PlayerBoard();
        }
        return uniqueInstance;
    }
	
	public void updateGold( Player player ){
		if (!player.isWildThing()) {
	        int gold = player.getGold();
	        String str = "Gold:  ";
	        if( gold < 100 ){
	            str += "0";
	        } 
	        if( gold < 10 ){
	            str += "0";
	        }
	        str += gold;
	        playerDisplay.get(player.getName()).updateGold(str);
		}
        return;
    }
	
	public void updateNumOnRack(Player p) {
		if (!p.isWildThing()) {
			int num = p.getPlayerRack().getNumOnRack();
			String str = "On Rack:  " + num;
	        playerDisplay.get(p.getName()).updateNumOnRack(str);
		}
		
	}
	
	public void updateNumOnRack(Player p, int num) {
		String str = "On Rack:  " + num;
        playerDisplay.get(p.getName()).updateNumOnRack(str);
		
	}
	
	public void updateNumOnRacks() {
		for (Player p : GameLoop.getInstance().getPlayers())
			updateNumOnRack(p);
	}
	
	public void updateGoldIncomePerTurn(Player p ) {
		if (!p.isWildThing()) {
			int num = p.calculateIncome();
			String str = "GPT:  " + num;
			playerDisplay.get(p.getName()).updateGoldIncomePerTurn(str);
		}
	}
	
	public void updateNumOnBoard(Player p) {
		if (!p.isWildThing()) {
			int num = p.getNumPieceOnBoard();
			String str = "On Board:  " + num;
			playerDisplay.get(p.getName()).updateNumOnBoard(str);
		}
	}

	private class PlayerDisplay {
		
		Player owner;
		String name;
		String gold;
		String numOnRack;
		
		Text nameGUI;
		Text goldGUI;
		Text numOnRackGUI;
		Text goldIncomePerTurnGUI;
		Text numOnBoardGUI;
		ImageView icon;
		Group playerGroup;
		Rectangle highlighter;
		Rectangle backing;
		Rectangle cover;
		VBox dataBox, buttonBox;
		GameButton goldButton, counterButton;
		
		private PlayerDisplay(Player p) {
			name = p.getName();
			owner = p;
			numOnRack = "" + p.getPlayerRack().getNumOnRack();

			goldButton = new GameButton(75, 30, "Gold", null);
			goldButton.hide();
            counterButton = new GameButton(75, 30, "Counter", null);
            counterButton.hide();
			
            nameGUI = TextBuilder.create()
                    .text(name)
                    .layoutX(width * 0.02)
                    .layoutY(height * 0.25/4)
                    .font(nameFont)
            		.mouseTransparent(true)
                    .fill(LinearGradientBuilder.create()
                            .startY(0)
                            .startX(1)
                            .stops(StopBuilder.create()
                                    .color(Color.BLACK)
                                    .offset(1)
                                    .build(),
                                StopBuilder.create()
                                    .color(Color.DARKSLATEGRAY)
                                    .offset(0)
                                    .build())
                            .build())
                    .build();
            
            goldGUI = TextBuilder.create()
                    .text("Gold:  000")
                    .font(dataFont)
            		.mouseTransparent(true)
                    .build();
            
            goldIncomePerTurnGUI = TextBuilder.create()
                    .text("GPT:  0")
                    .font(dataFont)
            		.mouseTransparent(true)
                    .build();
            
            numOnRackGUI = TextBuilder.create()
            		.text("On Rack:  0")
                    .font(dataFont)
            		.mouseTransparent(true)
                    .build();
            
            numOnBoardGUI = TextBuilder.create()
            		.text("On Board:  0")
            		.font(dataFont)
            		.mouseTransparent(true)
            		.build();
            
            dataBox = VBoxBuilder.create()
            		.layoutX(width * 0.3)
            		.layoutY(10)
            		.spacing(height*0.005)
            		.children(goldGUI, goldIncomePerTurnGUI, numOnRackGUI, numOnBoardGUI)
            		.mouseTransparent(true)
            		.build();
            
           	buttonBox = VBoxBuilder.create()
            		.layoutX(width * 0.5)
            		.layoutY(10)
            		.spacing(height*0.005)
            		.children(goldButton.getNode(), counterButton.getNode())
            		.mouseTransparent(false)
            		.build();

            icon = ImageViewBuilder.create()
                    .image(owner.getImage())
                    .preserveRatio(true)
                    .fitHeight(playerHeight - 10 - nameGUI.getLayoutBounds().getHeight())
                    .layoutY(nameGUI.getLayoutBounds().getHeight())
                    .layoutX(10)
            		.mouseTransparent(true)
                    .effect(DropShadowBuilder.create()
                            .offsetX(5)
                            .offsetY(5)
                            .radius(5)
                            .color(Color.DARKSLATEGRAY)
                            .build())
                    .build();
            
            highlighter = RectangleBuilder.create()
            		.height(height/4)
            		.width(width)
            		.fill(Color.TRANSPARENT)
            		.arcHeight(height * 0.05)
            		.arcWidth(width * 0.05)
            		.strokeWidth(3)
            		.strokeType(StrokeType.INSIDE)
            		.stroke(Color.DARKSLATEGRAY)
            		.effect(new GaussianBlur(3))
            		.mouseTransparent(true)
            		.build();
            
            backing = RectangleBuilder.create()
            		.height(height/4)
            		.width(width)
            		.fill(Color.DARKSLATEGRAY)
            		.arcHeight(height * 0.05)
            		.arcWidth(width * 0.05)
            		.opacity(0.5)
            		.build();
            
            cover = RectangleBuilder.create()
            		.height(height/4)
            		.width(width)
            		.fill(Color.DARKSLATEGRAY)
            		.opacity(0.5)
            		.arcHeight(height * 0.05)
            		.arcWidth(width * 0.05)
            		.visible(false)
            		.disable(true)
            		.build();
            
            playerGroup = GroupBuilder.create()
            		.layoutX(0)
            		.layoutY(playerDisplay.size() * height/4)
            		.clip(RectangleBuilder.create()
            				.width(width)
            				.height(height/4)
            				.build())
                    .build();
            
            playerGroup.getChildren().addAll(backing, icon, nameGUI, dataBox, highlighter, cover, buttonBox);
            playerBoardNode.getChildren().add(playerGroup);
            
            playerDisplay.put(p.getName(), this);
            
            setupEvents();
		}
		
		private Rectangle getHighlighter() { return highlighter; }
		private Rectangle getBacking() { return backing; }
		
		private void updateGold(String s) { goldGUI.setText(s); }
		private void updateNumOnRack(String s) { numOnRackGUI.setText(s); }
		private void updateGoldIncomePerTurn(String s) { goldIncomePerTurnGUI.setText(s); }
		private void updateNumOnBoard(final String s) { 
			Platform.runLater(new Runnable() {
                @Override
                public void run() {
        			numOnBoardGUI.setText(s);
                }
			}); 
		}
		private void cover() {
			cover.setVisible(true);
			cover.setDisable(false);
		}
		private void uncover() {
			cover.setVisible(false);
			cover.setDisable(true);
		}
		private void uncoverButtons() {
			goldButton.show();
			goldButton.activate();
			counterButton.show();
			counterButton.activate();
		}
		private void coverButtons() {
			goldButton.hide();
			goldButton.deactivate();
			counterButton.hide();
			counterButton.deactivate();
		}
		
		private void setupEvents() {
	    		
			backing.setOnMouseClicked(new EventHandler(){
				@Override
				public void handle(Event event) {
					ClickObserver.getInstance().setClickedPlayer(owner);
					ClickObserver.getInstance().whenPlayerClicked();
				}
			});
			backing.setOnMouseEntered(new EventHandler(){
				@Override
				public void handle(Event event) {
					playerGroup.setEffect(glow);
					getHighlighter().setStroke(Color.WHITESMOKE);
				}
			});
			backing.setOnMouseExited(new EventHandler(){
				@Override
				public void handle(Event event) {
					playerGroup.setEffect(null);
					getHighlighter().setStroke(Color.DARKSLATEGRAY);
				}
			});

			goldButton.setOnAction(new EventHandler() {
				@Override
				public void handle(Event e) {
					coverButtons();
					MasterThief.stealGold();
				}
			});
	    	
	    	counterButton.setOnAction(new EventHandler() {
				@Override
				public void handle(Event e) {
					coverButtons();
					MasterThief.stealRandomCounter();
				}
			});
		}
	}
	
	public void applyCovers() {
		
		Iterator<String> keySetIterator = playerDisplay.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		String key = keySetIterator.next();
    		
    		playerDisplay.get(key).cover();
    	}
	}
	
	public void removeCovers() {
		Iterator<String> keySetIterator = playerDisplay.keySet().iterator();
    	while(keySetIterator.hasNext()) {
    		String key = keySetIterator.next();
    		
    		playerDisplay.get(key).uncover();
    	}
	}
	
	public void uncover(Player p) {
		if (!p.isWildThing()) 
			playerDisplay.get(p.getName()).uncover();
	}
	public void cover(Player p) {
		if (!p.isWildThing()) 
			playerDisplay.get(p.getName()).cover();
	}
	public void uncoverButtons(Player p) {
		if (!p.isWildThing()) 
			playerDisplay.get(p.getName()).uncoverButtons();
	}
	public void coverButtons(Player p) {
		if (!p.isWildThing()) 
			playerDisplay.get(p.getName()).coverButtons();
	}
	
	public Rectangle getPlayerClickRec(Player p) { 
		if (!p.isWildThing()) {
			return playerDisplay.get(p.getName()).getBacking(); 
		} else
			return null;
	}
}

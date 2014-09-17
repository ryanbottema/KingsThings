package KAT;
//
// Fort.java
// kingsandthings/
// @author Brandon Schurman
//
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;

import java.util.HashMap;

import javafx.scene.effect.DropShadow;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradientBuilder;
import javafx.scene.paint.StopBuilder;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.EllipseBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;

public class Fort extends Piece implements Combatable {
	
	private static Image tower, keep, castle, citadel;
	private static Font font;
	
    private int combatValue;
    private boolean neutralized;
    private boolean magic;
    private boolean ranged;
    private double height;
    private Text combatValueIndicator;
    private Coord location;

    public Fort(){
        super("frontimg", "backimg", "Fort", "");
        if (font == null)
        	font = Font.loadFont(getClass().getResourceAsStream("/Fonts/ITCBLKAD.TTF"), InfoPanel.getTileHeight()*0.6);
        this.name = "Tower";
        this.magic = false;
        this.neutralized = false;
        this.ranged = false;
        this.combatValue = 1;
        this.imageFront = tower;
        setupNode();
    }

    public Fort(String in) {
        super("frontimg", "backimg", "Fort", "");
        if (font == null)
        	font = Font.loadFont(getClass().getResourceAsStream("/Fonts/ITCBLKAD.TTF"), InfoPanel.getTileHeight()*0.6);
        String[] input = in.split(",");
        name = input[0];
        magic = (input[1].equals("true")) ? true : false;
        neutralized = (input[2].equals("true")) ? true : false;
        ranged = (input[3].equals("true")) ? true : false;
        combatValue = Integer.parseInt(input[4]);
        if (input[5].equals("Tower"))
            imageFront = tower;
        else if (input[5].equals("Keep"))
            imageFront = keep;
        if (input[5].equals("Castle"))
            imageFront = castle;
        else if (input[5].equals("Citadel"))
            imageFront = citadel;
        setupNode();
    }

    public Fort( HashMap<String,Object> map ){
        super(map);
        if (font == null)
        	font = Font.loadFont(getClass().getResourceAsStream("/Fonts/ITCBLKAD.TTF"), InfoPanel.getTileHeight()*0.6);
        this.combatValue = (Integer)map.get("combatVal");
        this.neutralized = ((Integer)map.get("neutralized") == 1) ? true : false;
        this.magic = false;
        this.ranged = false;
        switch( combatValue ){
        case 1:
            neutralized = false;
            this.imageFront = tower;
            name = "Tower";
            break;
        case 2:
            name = "Keep";
            this.imageFront = keep;
            break;
        case 3:
            name = "Castle";
            ranged = true;
            this.imageFront = castle;
            break;
        case 4:
            name = "Citadel";
            ranged = false;
            this.imageFront = citadel;
            magic = true;
            break;
        }

        setupNode();
        setupEvents();
    }

    @Override
    public HashMap<String,Object> toMap(){
        HashMap<String,Object> map = super.toMap();
        map.put("neutralized", neutralized ? 1 : 0);
        map.put("magic", magic ? 1 : 0);
        map.put("ranged", ranged ? 1 : 0);
        return map;
    }
        
    public boolean inflict(){
        if( combatValue > 0 ){
            --combatValue;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	combatValueIndicator.setFill(Color.DARKRED);
                }
            });
        }
        if( combatValue == 0 ){
            neutralized = true;
        } 
        setCombatIndicator();
        return neutralized;
    }

    public void upgrade(){

        switch( name ){
            case "Tower":
                neutralized = false;
                name = "Keep";
                this.imageFront = keep;
                break;
            case "Keep":
                name = "Castle";
                this.imageFront = castle;
                break;
            case "Castle":
                name = "Citadel";
                ranged = true;
                this.imageFront = citadel;
                break;
            case "Citadel":
                break;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	pieceImgV.setImage(imageFront);
            }
        });
        healFort();
        setCombatIndicator();
    }

    public void downgrade(){

        switch( name ){
            case "Tower":
            	Board.getTerrainWithCoord(location).removeFort();
                break;
            case "Keep":
                name = "Tower";
                this.imageFront = tower;
                break;
            case "Castle":
                name = "Keep";
                ranged = false;
                this.imageFront = keep;
                break;
            case "Citadel":   // Cant be downgraded
                break;
        }
        healFort();
        setCombatIndicator();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	pieceImgV.setImage(imageFront);
                Board.getTerrainWithCoord(location).setFortImage();
            }
        });
    }

    public int getCombatValue(){ return combatValue; }
    public boolean isRanged(){ return ranged; }
    public boolean isMagic(){ return magic; }
    public boolean isCharging(){ return false; }
    public boolean isFlying(){ return false; }
    public Image getImage(){ return imageFront; }

	public void setAttackResult(boolean b) {
    	if (b)
    		attackResultImgV.setImage(attackingSuccessImg);
    	else
    		attackResultImgV.setImage(attackingFailImg);
    	attackResultImgV.setVisible(true);
	}

	public void resetAttack() {
		attackResultImgV.setVisible(false);
	}
    
    public static void setClassImages() {
    	tower = new Image("Images/Fort_Tower.png");
    	keep = new Image("Images/Fort_Keep.png");
    	castle = new Image("Images/Fort_Castle.png");
    	citadel = new Image("Images/Fort_Citadel.png");
    	
    }

	@Override
	public Group getPieceNode() { return pieceNode; }
	
	private void setupEvents() {
		pieceImgV.setOnMouseClicked(new EventHandler(){
			@Override
			public void handle(Event event) {
				clicked();
			}
		});
		pieceImgV.setOnMouseEntered(new EventHandler(){
			@Override
			public void handle(Event event) {
				pieceImgV.setEffect(glow);
			}
		});
		pieceImgV.setOnMouseExited(new EventHandler(){
			@Override
			public void handle(Event event) {
				pieceImgV.setEffect(null);
			}
		});
	}
	
	// Temp setupNode method. Forts currently dont need this, but I had it here for de-bugging reasons
	private void setupNode() {
		
		height = InfoPanel.getTileHeight() * 0.92;
		double radius = height/2;
		
		DropShadow dShadow = DropShadowBuilder.create()
				.offsetX(5)
				.offsetY(5)
				.radius(5)
				.color(Color.DARKSLATEGRAY)
				.build();
		
		// Creates ImageView
		pieceImgV = ImageViewBuilder.create()
				.image(imageFront)
				.fitHeight(height * 0.70)
				.layoutX(height * 0.15)
				.layoutY(height * 0.15)
				.preserveRatio(true)
				.effect(dShadow)
				.build();
		
		// Small outline around creatures
		pieceBorderOutline = CircleBuilder.create()
				.radius(radius)
				.strokeWidth(1)
				.strokeType(StrokeType.INSIDE)
				.stroke(Color.BLACK)
				.fill(Color.TRANSPARENT)
				.effect(new GaussianBlur(2))
				.mouseTransparent(true)
				.centerX(radius)
				.centerY(radius)
				.build();
		
		// Create shape around selected fort
		pieceSelectBorder = CircleBuilder.create()
				.radius(radius)
				.strokeWidth(5)
				.strokeType(StrokeType.INSIDE)
				.stroke(Color.WHITESMOKE)
				.fill(Color.TRANSPARENT)
				.effect(new GaussianBlur(5))
				.visible(false)
				.disable(true)
				.centerX(radius)
				.centerY(radius)
				.build();
		
		// Create shape to cover image and disable clicks
		pieceCover = CircleBuilder.create()
				.radius(radius)
				.fill(Color.DARKSLATEGRAY)
				.opacity(0.5)
				.visible(true)
				.disable(false)
				.centerX(radius)
				.centerY(radius)
				.build();
		
		attackResultImgV = ImageViewBuilder.create()
				.preserveRatio(true)
				.fitHeight(height)
				.visible(false)
				.mouseTransparent(true)
				.build();
		
		combatValueIndicator = TextBuilder.create()
		        .font(font)
		        .layoutY(height*0.75)
		        .layoutX(height * 0.8)
		        .effect(DropShadowBuilder.create()
		        		.offsetX(2)
		        		.offsetY(2)
		        		.color(Color.WHITESMOKE)
		        		.radius(2)
		        		.build())
		        .fill(Color.BLACK)
		        .mouseTransparent(true)
		        .build();
		
		pieceNode = GroupBuilder.create()
				.children(pieceImgV, pieceBorderOutline, pieceSelectBorder, combatValueIndicator, pieceCover, attackResultImgV)
				.build();
		
		setCombatIndicator();
		setupEvents();
	}
	
	private void clicked() { 
		ClickObserver.getInstance().setClickedFort(this);
		ClickObserver.getInstance().whenFortClicked();
	}
	
	public void setCombatIndicator() {
		final String com = String.valueOf(this.getCombatValue());
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	combatValueIndicator.setText(com);
            }
		});
	}
	
	public void healFort() {
		neutralized = false;
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	combatValueIndicator.setFill(Color.BLACK);
            }
		});
		if (name.equals("Tower"))
			combatValue = 1;
		else if (name.equals("Keep"))
			combatValue = 2;
		else if (name.equals("Castle")) {
			combatValue = 3;
			ranged = true;
		} else {
			combatValue = 4;
			ranged = false;
			magic = true;
		}
	}
	
	public Coord getLocation() { return location; }
	public void setLocation(Coord c) { location = c; }
}

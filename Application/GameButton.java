package KAT;

import java.io.File;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.effect.GlowBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradientBuilder;
import javafx.scene.paint.StopBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.util.Duration;

public class GameButton {

	private static Image defaultImage;
	
	private Group buttonNode;
	private Text text;
	private String textString;
	private ImageView imgV;
	private boolean active;
	private Rectangle clip;
	private Rectangle cover;
	private Rectangle border;
	private double width, height;
	private double posX, posY;
	private double maxFont;
	private DropShadow dShadow;
	
	final static Glow glow = GlowBuilder.create().build();
	
	/*
	 * Constructors
	 */
	public GameButton () {
		width = 100;
		height = 50;
		textString = " ";
		active = false;
		setupGUI();
	}
	public GameButton(double w, double h, double x, double y, String t, EventHandler eh) {
		width = w;
		height = h;
		textString = t;
		active = false;
		posX = x;
		posY = y;
		maxFont = Math.min(Math.min(w*0.8, h*0.8), 30);
		setupGUI();
		imgV.setOnMouseClicked(eh);
	}
	public GameButton(double w, double h, String t, EventHandler eh) {
		width = w;
		height = h;
		textString = t;
		active = false;
		maxFont = Math.min(Math.min(w*0.8, h*0.8), 30);
		setupGUI();
		imgV.setOnMouseClicked(eh);
	}
	
	private void setupGUI() {
		
		//Font theFont = Font.loadFont(getClass().getResourceAsStream("ITCBLKAD.TTF"), maxFont);
		Font theFont = Font.loadFont(getClass().getResourceAsStream("/Fonts/ITCBLKAD.TTF"), maxFont);
		
		if (defaultImage == null) 
			defaultImage = new Image("Images/ButtonBacking.jpg");
		
		final Animation buttonAnim = new Transition() {
	   	     {
		         setCycleDuration(Duration.millis(1000));
		         setCycleCount(INDEFINITE);
		         setAutoReverse(true);
		         
		     }
		     protected void interpolate(double frac) { dShadow.setSpread(0.8 - frac * 0.3); }
		};;
		
		dShadow = DropShadowBuilder.create()
				.radius(20)
				.color(Color.WHITESMOKE)
				.spread(0.8)
				.build();
		
		text = TextBuilder.create()
				.text(textString)
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
				.effect(dShadow)
				.visible(true)
				.font(theFont)
				.build();
		
		clip = RectangleBuilder.create()
				.width(width)
				.height(height)
				.arcHeight(20)
				.arcWidth(20)
				.build();
		
		cover = RectangleBuilder.create()
				.width(width)
				.height(height)
				.fill(Color.DARKSLATEGRAY)
				.opacity(0.5)
				.visible(false)
				.disable(true)
				.build();
		
		border = RectangleBuilder.create()
				.width(width)
				.height(height)
				.arcHeight(20)
				.arcWidth(20)
				.stroke(Color.BLACK)
				.strokeWidth(3)
				.fill(Color.TRANSPARENT)
				.mouseTransparent(true)
				.effect(new GaussianBlur(2))
				.build();
				
		imgV = ImageViewBuilder.create()
				.image(defaultImage)
				.preserveRatio(false)
				.fitHeight(height)
				.fitWidth(width)
				.onMouseEntered(new EventHandler(){
					@Override
					public void handle(Event event) {
						imgV.setEffect(glow);
				    	buttonAnim.play(); 
					}
				})
				.onMouseExited(new EventHandler(){
					@Override
					public void handle(Event event) {
						imgV.setEffect(null);
						buttonAnim.stop();
						dShadow.setSpread(0.8);
					}
				}) 
				.build();
		
		buttonNode = GroupBuilder.create()
				.clip(clip)
				.layoutX(posX)
				.layoutY(posY)
				.build();
		
		text.relocate(clip.getWidth()/2 - text.getLayoutBounds().getWidth()/2, clip.getHeight()/2 - text.getLayoutBounds().getHeight()/2);
		buttonNode.getChildren().add(imgV);
		buttonNode.getChildren().add(border);
		buttonNode.getChildren().add(text);
		buttonNode.getChildren().add(cover);
				
	}
	
	/*
	 * Gets and Sets
	 */
	public boolean getActive() { return active; }
	public Group getNode() { return buttonNode; }
	public double getWidth() { return width; }
	public double getHeight() { return height; }
	public double[] getPosition() { return new double[] {posX, posY}; }
	public ImageView getImgV() { return imgV; }
	
	public void activate() {
		active = true;
		cover.setVisible(false);
		cover.setDisable(true);
	}
	public void deactivate() {
		active = false;
		cover.setVisible(true);
		cover.setDisable(false);
	}
	public void setText(String s) {
		textString = s;
		text = TextBuilder.create()
				.text(textString)
				.mouseTransparent(true)
				.build();
	}

	public void hide() {
		buttonNode.setVisible(false);
	}

	public void show() {
		buttonNode.setVisible(true);
		setDisable(active);
	}
	
	public void position(double x, double y) {
		posX = x;
		posY = y;
	}
	
	public void setOnAction(EventHandler eh) {
		imgV.setOnMouseClicked(eh);
	}
	
	public void setDisable(boolean b) {
		if (b)
			deactivate();
		else
			activate();
	}
	
}

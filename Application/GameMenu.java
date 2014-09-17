package KAT;

import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradientBuilder;
import javafx.scene.paint.StopBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.scene.control.ComboBox;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class GameMenu {

	private static GameMenu uniqueInstance;
	private static ArrayList<GameButton> startMenuButtons; 				// Initial screen buttons: "Online play", "Local Play", "Exit"
	private static ArrayList<GameButton> onlineMenuButtons;				// Buttons that buttons will change to for online menu
	private static ArrayList<GameButton> localMenuButtons;				// Buttons that local game menu will use
	private static ArrayList<InputField> onlineInputFields;	// TextFields that textFields will change to during 'online play' menu
	private static ArrayList<InputField> localInputFields;		// TextFields that local game menu will use
	private static ArrayList<InputField> startInputFields;		// TextFields that start menu will use
	
	private static ArrayList<GameButton> buttons;						// The currently used buttons
	private static ArrayList<InputField> inputFields;			// The currently used TextFields
	
	private static DropShadow dShadow = DropShadowBuilder.create()
					.radius(3)
					.color(Color.WHITESMOKE)
					.offsetX(1)
					.offsetY(1)
					.build();
	
	private double width, height;
	private Rectangle clip;
	private ImageView backingImgV;
	private Group menuNode;
	private Font labelFont;
	private Group fieldList;
	private Rectangle fieldListBacking;
	private Rectangle fieldListBorder;
	
	
	public GameMenu() {

		buttons = new ArrayList<GameButton>();
		inputFields = new ArrayList<InputField>();
		
		startMenuButtons = new ArrayList<GameButton>();
		startInputFields = new ArrayList<InputField>();
		onlineMenuButtons = new ArrayList<GameButton>();
		onlineInputFields = new ArrayList<InputField>();
		localMenuButtons = new ArrayList<GameButton>();
		localInputFields = new ArrayList<InputField>();
		
		width = Game.getWidth() * 0.8;
		height = Game.getHeight() * 0.8;

		double fontSize = height * 0.06;
		labelFont = Font.loadFont(getClass().getResourceAsStream("/Fonts/ITCBLKAD.TTF"), fontSize);				
		
		clip = RectangleBuilder.create()
				.width(width)
				.height(height)
				.arcHeight(height * 0.05)
				.arcWidth(width * 0.05)
				.build();
		
		fieldListBacking = RectangleBuilder.create()
				.arcHeight(20)
				.width(0)
				.arcWidth(20)
				.fill(Color.DARKSLATEGRAY)
				.opacity(0.5)
				.build();
		
		fieldListBorder = RectangleBuilder.create()
				.arcHeight(20)
				.arcWidth(20)
				.width(0)
				.visible(false)
				.fill(Color.TRANSPARENT)
				.stroke(Color.DARKSLATEGRAY)
				.strokeWidth(3)
				.build();
		
		fieldList = GroupBuilder.create()
				.layoutX(width * 0.3)
				.layoutY(height * 0.5)
				.children(fieldListBacking, fieldListBorder)
				.build();
		
		backingImgV = ImageViewBuilder.create()
				.image(new Image("Images/RackCover.jpg"))
				.preserveRatio(false)
				.fitHeight(height)
				.fitWidth(width)
				.build();
		
		menuNode = GroupBuilder.create()
				.clip(clip)
				.children(backingImgV, fieldList)
				.layoutX(Game.getWidth()/2 - width/2)
				.layoutY(height * 0.1/0.8)
				.build();
		
		setupStuff();
		buttons = startMenuButtons;
		updateMenu();
	}

	public Group getNode() { return menuNode; }
	public ArrayList<GameButton> getButtons() { return buttons; }
	
	public static GameMenu getInstance() {
		if(uniqueInstance == null){
            uniqueInstance = new GameMenu();
        }
        return uniqueInstance;
	}
	
//	public void addMainButton(double w, double h, double x, double y, String t, EventHandler eh) {
//		buttons.add(new GameButton(w, h, x, y, t, eh));
//		if (buttons.size() == 1)
//			buttons.get(0).position(width*0.6, height * 0.5);
//		else
//			buttons.get(buttons.size() - 1).position(width*0.6, buttons.get(buttons.size() - 2).getPosition()[1] + buttons.get(buttons.size() - 2).getHeight());
//	}
	
	public void updateMenu() {
		for (GameButton b : buttons) {
			menuNode.getChildren().add(b.getNode());
		}
		int i;
		for (i = 0; i < inputFields.size(); i++) {
			InputField ifwl = inputFields.get(i);
			ifwl.position(i*40);
			fieldList.getChildren().add(ifwl.getNode());
		}
		if (i > 0)
			fieldListBorder.setVisible(true);
		fieldListBacking.setWidth(300);
		fieldListBacking.setHeight(i*50);
		fieldListBorder.setWidth(300);
		fieldListBorder.setHeight(i*50);
		
		System.out.println("input fields stuff");
		System.out.println(inputFields);
		System.out.println(onlineInputFields);
		
	}
	
	public void removeStuff() {
		for (GameButton b : buttons) {
			menuNode.getChildren().remove(b.getNode());
		}
		for (InputField ifwl : inputFields) {
			fieldList.getChildren().remove(ifwl.getNode());
		}
		fieldListBacking.setWidth(0);
		fieldListBacking.setHeight(0);
		fieldListBorder.setWidth(0);
		fieldListBorder.setHeight(0);
		fieldListBorder.setVisible(false);
	}
	
	public void deleteStuff() {
		buttons.clear();
		startMenuButtons.clear();
		localMenuButtons.clear();
		onlineMenuButtons.clear();
		onlineInputFields.clear();
		localInputFields.clear();
		buttons = null;
		startMenuButtons = null;
		localMenuButtons = null;
		onlineMenuButtons = null;
		onlineInputFields = null;
		localInputFields = null;
	}
	
	private void setupStuff() {
		
		/*
		 * Starting menu buttons:
		 * - Play Online
		 * - Play local
		 * - Exit
		 */
		startMenuButtons.add(new GameButton(200, 50, width*0.6, height * 0.5, 
                    "Play Online", new EventHandler(){
			@Override
			public void handle(Event event) {
				removeStuff();
//				Game.getRoot().getChildren().remove(menuNode);
                Game.getUniqueInstance().setNetwork(true);
//				Game.getUniqueInstance().createGame(); 
                inputFields = onlineInputFields;
                buttons = onlineMenuButtons;
                updateMenu();
			}
		}));
		startMenuButtons.add(new GameButton(200, 50, width*0.6, height * 0.5 + 50, 
                    "Local Game", new EventHandler(){
			@Override
			public void handle(Event event) {
				removeStuff();
				buttons = localMenuButtons;
				inputFields = localInputFields;
				updateMenu();
                Game.getUniqueInstance().setNetwork(false);
			}
		}));
		startMenuButtons.add(new GameButton(200, 50, width*0.6, height * 0.5 + 100, "Exit", new EventHandler(){
			@Override
			public void handle(Event event) {
				Game.getUniqueInstance().exit(); 
			}
		}));
		
		/*
		 * Online play menu buttons
		 * 
		 * - Play
		 * - Back
		 * 
		 */
		onlineMenuButtons.add(new GameButton(200, 50, width*0.6, height * 0.5, "Play", new EventHandler(){
			@Override
			public void handle(Event event) {
                String name = "", password = "";
                int numPlayers = 0;
                for( InputField f : onlineInputFields ){
                    if( f.getLabel().equals("Name:        ") ){
                        if( !f.getText().equals("") ){
                            name = f.getText();
                        } else {
                            System.out.println("Enter a Username");
                            Stage dialog = showDialog("Error: enter your username");
                            return;
                        }
                    } else if( f.getLabel().equals("Password: ") ){
                        if( !f.getText().equals("") ){
                            password = f.getText();
                        } else {
                            System.out.println("Enter a password");
                            // return;
                        }
                    } else if( f.getLabel().equals("Players:    ") ){
                        if( !f.getText().equals("") ){
                            numPlayers = Integer.parseInt(f.getText());
                        } else {
                            System.out.println("Select number of player ");
                            Stage dialog = showDialog("Error: select the number of players to play with");
                            return;
                        }
                    } else {
                        System.err.println("unrecognized InputField label");
                        return;
                    }

                }
                final Stage dialog1 = showDialog("Connecting to Kings and Things Server...");
                final int _numPlayers = numPlayers;
                final String _name = name;
                new Thread(new Runnable(){
                	@Override 
                	public void run(){
                		boolean error = !NetworkGameLoop.getInstance().connect();
                        System.out.println("Connecting to server ...");
                        
                        long strtTime = System.currentTimeMillis();
                        long currTime = System.currentTimeMillis();
                        while( currTime-strtTime < 2000 ){
                        	currTime = System.currentTimeMillis();
                        }
                        
                        if( error ){
                        	Platform.runLater(new Runnable(){
                            	@Override
                            	public void run(){
                            		dialog1.close();	
                            		Stage dialog2 = showDialog("There was a problem connecting to the server");
                            	}
                            }); 
                        	return;
                        }
                        
                        Platform.runLater(new Runnable(){
                        	@Override
                        	public void run(){
                        		NetworkGameLoop.getInstance().setGameSize(_numPlayers);
                                Game.getUniqueInstance().setNetwork(true);
                                NetworkGameLoop.getInstance().setPlayer(new Player(_name,"YELLOW"));
                                NetworkGameLoop.getInstance().setLocalPlayer(onlineInputFields.get(0).getText());
                				removeStuff();
                				Game.getRoot().getChildren().remove(menuNode);
                				deleteStuff();
                				Game.getUniqueInstance().createGame();
                				dialog1.close();	
                        	}
                        }); 
                	}
                }).start();
                
			}
		}));
		onlineMenuButtons.add(new GameButton(200, 50, width*0.6, height * 0.5 + 50, "Back", new EventHandler(){
			@Override
			public void handle(Event event) {
				removeStuff();
				buttons = startMenuButtons;
				inputFields = startInputFields;
				updateMenu();
			}
		}));
		
		/*
		 * Online play menu text Fields
		 * 
		 * - Name
		 * - Password 
         * - Number of Players
		 * 
		 */
		onlineInputFields.add(new inputFieldWithLabel("Name:        ", width * 0.3, height * 0.5));
        String tip = "Enter your account password. "
                   + "If you are a new user, this will create your password";
        onlineInputFields.add(new PasswordFieldWithLabel("Password: ", 125, tip));
        String[] values = { "2", "3", "4" };
        tip = "Select the number of players "
            + "that you wish to play online with";
        onlineInputFields.add(new ComboBoxWithLabel("Players:    ", values, 130, tip));
		
		
		/*
		 * Local play menu buttons
		 * 
		 * - Play
		 * - Back
		 * 
		 */
		localMenuButtons.add(new GameButton(200, 50, width*0.6, height * 0.5, "Play", new EventHandler(){
			@Override
			public void handle(Event event) {
				removeStuff();
				Game.getRoot().getChildren().remove(menuNode);
				deleteStuff();
                Game.getUniqueInstance().setNetwork(false);
				Game.getUniqueInstance().createGame();
			}
		}));
		localMenuButtons.add(new GameButton(200, 50, width*0.6, height * 0.5 + 50, "Back", new EventHandler(){
			@Override
			public void handle(Event event) {
				removeStuff();
				buttons = startMenuButtons;
				updateMenu();
			}
		}));
		localMenuButtons.add(new GameButton(200, 50, width*0.6-200, height*0.5 + 100, "Load File One", new EventHandler() {
			@Override
			public void handle(Event e) {
				removeStuff();
				Game.getRoot().getChildren().remove(menuNode);
				deleteStuff();
				Game.getUniqueInstance().setNetwork(false);
				Game.getUniqueInstance().createGame("PreparedFileOne.txt");
			}
		}));
		localMenuButtons.add(new GameButton(200, 50, width*0.6, height*0.5 + 100, "Load File Two", new EventHandler() {
			@Override
			public void handle(Event e) {
				removeStuff();
				Game.getRoot().getChildren().remove(menuNode);
				deleteStuff();
				Game.getUniqueInstance().setNetwork(false);
				Game.getUniqueInstance().createGame("PreparedFileTwo.txt");
			}
		}));
		localMenuButtons.add(new GameButton(200, 50, width*0.6+200, height*0.5 + 100, "Load File Three", new EventHandler() {
			@Override
			public void handle(Event e) {
				removeStuff();
				Game.getRoot().getChildren().remove(menuNode);
				deleteStuff();
				Game.getUniqueInstance().setNetwork(false);
				Game.getUniqueInstance().createGame("PreparedFileThree.txt");
			}
		}));
		
		/*
		 * Local play menu textFields
		 * 
		 * 
		 */
		
	}
	
	public Stage showDialog( String msg ){
		Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
    	Text t1 = new Text(msg); // why wont this show up ? :S!
    	t1.setVisible(true);
    	t1.setFont(new Font(20));
    	VBox box = new VBox();
    	box.setPadding(new Insets(20, 20, 20, 20));
    	box.getChildren().add(t1);
    	Scene scene = new Scene(box);
    	dialog.setScene(scene);
    	dialog.show();
		return dialog;
	}

    private abstract class InputField {
        protected Text label;
        protected double xPos;
        protected double yPos;
        protected String labelName;
        protected double fieldWidth;
        protected HBox node;
        
        public InputField( String s, double x, double y ){
            xPos = x;
            yPos = y;
            labelName = s;
			
            label = TextBuilder.create()
					.text(labelName)
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
					.font(labelFont)
					.build();
			label.setLayoutY(label.getLayoutBounds().getHeight()/2);
        }
		
        public HBox getNode() { return node; }
        public String getLabel(){ return labelName; }
		public abstract String getText();
		
		public void position(double d) {
			if( node != null ){
                node.relocate(0, d);
            }
		}
		
    }
	
	private class inputFieldWithLabel extends InputField {
		
		private TextField textField;
		
		public inputFieldWithLabel(String s, double x, double y) {
            super(s, x+50, y);
            fieldWidth = 230;

			textField = TextFieldBuilder.create()
					.layoutX(width - 150)
					.prefWidth(130)
					.build();
			textField.setLayoutY(textField.getLayoutBounds().getHeight()/2);
			
			node = HBoxBuilder.create()
					.children(label, textField)
					.alignment(Pos.CENTER_RIGHT)
					.layoutX(xPos)
					.layoutY(yPos)
					.build();
					
			
		}
        @Override
        public String getText(){ return textField.getCharacters().toString(); }
	}
	
    private class ComboBoxWithLabel extends InputField {
		
        private ObservableList<String> options;
        private ComboBox comboBox;
        private String selected; 
		
		public ComboBoxWithLabel( String s, String[] values, double w, String tip ){
			super(s, 1, 1);
            this.fieldWidth = w;
			this.options = javafx.collections.FXCollections.observableArrayList();
			for( int i=0; i<values.length; i++ ){
				options.add(values[i]);
			}
			this.selected = "";
			comboBox = new ComboBox(options);
			comboBox.setLayoutX(width - fieldWidth);
			comboBox.setPrefWidth(fieldWidth);
			comboBox.setLayoutY(comboBox.getLayoutBounds().getHeight()/2);
            comboBox.setPromptText("Number of Players");
            comboBox.setTooltip(new Tooltip(tip));
            comboBox.valueProperty().addListener( new ChangeListener<String>(){
                @Override
                public void changed( ObservableValue ov, String t, String t1 ){
                    selected = t1;
                }
            });	
			node = HBoxBuilder.create()
					.children(label, comboBox)
					.alignment(Pos.CENTER_RIGHT)
					.layoutX(xPos)
					.layoutY(yPos)
					.build();			
		}
        @Override
        public String getText(){ return this.selected; }
    }
    
    private class PasswordFieldWithLabel extends InputField 
    {
        private PasswordField passwordField; 
		
		public PasswordFieldWithLabel( String s, double w, String tip ){
			super(s, 1, 1);
            this.fieldWidth = w;
			this.passwordField = new PasswordField();
			passwordField.setLayoutX(width - fieldWidth);
			passwordField.setPrefWidth(fieldWidth);
			passwordField.setLayoutY(passwordField.getLayoutBounds().getHeight()/2);
            passwordField.setTooltip(new Tooltip(tip));
            passwordField.setPromptText("N/A for this version");
			node = HBoxBuilder.create()
					.children(label, passwordField)
					.alignment(Pos.CENTER_RIGHT)
					.layoutX(xPos)
					.layoutY(yPos)
					.build();			
		}
        @Override
        public String getText(){ return passwordField.getCharacters().toString(); }
    }
}

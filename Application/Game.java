package KAT;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;


public class Game extends Application {

    private static BorderPane root;
    private static double width, height;
    
    private static InfoPanel infoPan;
    private static Text helpText;
    private static Board hexBoard;
    private static PlayerBoard playerBoard;
    private static PlayerRackGUI rackG;
    private static GameButton doneButton;
    private static GameButton playAgainButton;
    private static GameMenu menu;
    private static Font gameFont;
    
    private static Thread gameLoopThread;
    private static Game uniqueInstance;
    private static Stage uniqueStage;
    
    private static Image[] playerIcons;
    private static Text[] playerNames;
    private static Text[] playerGold;

    private static ArrayList<Player> playerList;
    
    /*
     * Gets and Sets
     */
    public static InfoPanel getInfoPanel(){ return infoPan; }
    public static Text getHelpText(){ return helpText; }
    public static Board getBoard() { return hexBoard; }
    public static PlayerRackGUI getRackGui() { return rackG; }
    public static Image[] getPlayerIcons(){ return playerIcons; }
    public static Text[] getPlayerNames(){ return playerNames; }
    public static Text[] getPlayerGold(){ return playerGold; }
    public static GameButton getDoneButton(){ return doneButton; }
    public static GameButton getPlayAgainButton(){ return playAgainButton; }
    public static double getWidth() { return width; }
    public static double getHeight() { return height; }
    public static BorderPane getRoot() { return root; }
    public static Game getUniqueInstance() { return uniqueInstance; }
    public static Font getFont() { return gameFont; }
    public static Stage getStage() { return uniqueStage; }
    public static ArrayList<Player> getPlayers() { return playerList; }
    
    // Can change these accordingly for testing and what not
    private static boolean network = false;	
    private static boolean startingMenu = true;
    private static boolean runGameLoop;

	@Override
	public void start(Stage primaryStage) {
		
		uniqueInstance = this;
		uniqueStage = primaryStage;
//		uniqueStage.setFullScreen(true);
		
		width = Screen.getPrimary().getVisualBounds().getWidth();
		height = Screen.getPrimary().getVisualBounds().getHeight() * 0.95;
		
		root = new BorderPane();
		Scene scene = new Scene(root,width,height);
		uniqueStage.setScene(scene);
		uniqueStage.show();
		
		// Import the game pictures.
		Board.generateHexes();
		Player.setClassImages();
		Terrain.setClassImages();
		Fort.setClassImages();
		Piece.setClassImages();
		InfoPanel.setClassImages();
		
		// Background image
		root.getChildren().add(ImageViewBuilder.create()
				.image(new Image("Images/GameBackground.jpg"))
				.preserveRatio(false)
				.fitHeight(height)
				.fitWidth(width)
				.opacity(0.5)
				.build());
				
		
		// TODO finish starting menu for creating game
		if (startingMenu) {
			menu = GameMenu.getInstance();
			root.getChildren().add(menu.getNode());
		} else {
			createGame();
		}
	}

    public void setNetwork( boolean _network ){
        network = _network;
    }

    public void start(){
        runGameLoop = true;
    }

    public void stop(){
        runGameLoop = false;
        GameLoop.getInstance().stop();
        if (gameLoopThread != null)
        	gameLoopThread.interrupt();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    // To launch a new  game menu (where players enter etc)
    public static void newGame() {
    	
    }

    // Launches a game with preloaded settings.
    public static void createGame(String startConditionFile) {
        GameLoop.setNetworked(network);
        GameLoop.getInstance().setPhase(-2);

        ArrayList<String> lines =  new ArrayList<String>();
        int i = 0;
        ArrayList<String> tiles = new ArrayList<String>();
        ArrayList<String> playerOneCoords = new ArrayList<String>();
        ArrayList<String> playerTwoCoords = new ArrayList<String>();
        ArrayList<String> playerThreeCoords = new ArrayList<String>();
        ArrayList<String> playerFourCoords = new ArrayList<String>();

        ArrayList<String> playerOneTowers = new ArrayList<String>();
        ArrayList<String> playerTwoTowers = new ArrayList<String>();
        ArrayList<String> playerThreeTowers = new ArrayList<String>();
        ArrayList<String> playerFourTowers = new ArrayList<String>();

        ArrayList<String> playerOnePieces = new ArrayList<String>();
        ArrayList<String> playerTwoPieces = new ArrayList<String>();
        ArrayList<String> playerThreePieces = new ArrayList<String>();
        ArrayList<String> playerFourPieces = new ArrayList<String>();

        ArrayList<String> playerOneRack = new ArrayList<String>();
        ArrayList<String> playerTwoRack = new ArrayList<String>();
        ArrayList<String> playerThreeRack = new ArrayList<String>();
        ArrayList<String> playerFourRack = new ArrayList<String>();
        BufferedReader inFile;


        try {
            helpText = new Text("initializing...");
            helpText.setFont(new Font(15));
            root.getChildren().add(helpText);
            helpText.relocate(width*0.25 , 0);

            doneButton = new GameButton(75, 35, width*0.25 + 5, height - 40, "Done", null);
            doneButton.deactivate();
            playAgainButton = new GameButton(75, 35, width*0.25 + 5, height - 80, "Again?", null);
            playAgainButton.deactivate();
            playAgainButton.hide();
            root.getChildren().addAll(doneButton.getNode(), playAgainButton.getNode());
            
            playerList = new ArrayList<Player>();
            if( !network ){
                Player user  = new Player("User1", "YELLOW");
                Player user2 = new Player("User2", "BLUE");
                Player user3 = new Player("User3", "GREEN");
                Player user4 = new Player("User4", "RED");
                playerList.add(user);
                playerList.add(user2);
                playerList.add(user3);
                playerList.add(user4);
                GameLoop.getInstance().setPlayers(playerList);
            } else {

            }

            inFile = new BufferedReader(new FileReader(System.getProperty("user.dir") + File.separator + startConditionFile));
            String line = null;
            while ((line = inFile.readLine()) != null) {
                lines.add(line);
                i++;
            }
            inFile.close();
            for (int j = 0; j < i; j++) {
                if (lines.get(j).equals("###Terrains###")) {
                    for (int k = j+1; k < lines.indexOf("###Player One Tiles###"); k++) {
                        tiles.add(lines.get(k));
                    }
                }
                if (lines.get(j).equals("###Player One Tiles###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Two Tiles###"); k++) {
                        playerOneCoords.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Two Tiles###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Three Tiles###"); k++) {
                        playerTwoCoords.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Three Tiles###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Four Tiles###"); k++) {
                        playerThreeCoords.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Four Tiles###")) {
                    for (int k = j+1; k < lines.indexOf("###Player One Towers###"); k++) {
                        playerFourCoords.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player One Towers###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Two Towers###"); k++) {
                        playerOneTowers.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Two Towers###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Three Towers###"); k++) {
                        playerTwoTowers.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Three Towers###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Four Towers###"); k++) {
                        playerThreeTowers.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Four Towers###")) {
                    for (int k = j+1; k < lines.indexOf("###Player One Pieces###"); k++) {
                        playerFourTowers.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player One Pieces###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Two Pieces###"); k++) {
                        playerOnePieces.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Two Pieces###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Three Pieces###"); k++) {
                        playerTwoPieces.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Three Pieces###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Four Pieces###"); k++) {
                        playerThreePieces.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Four Pieces###")) {
                    for (int k = j+1; k < lines.indexOf("###Player One Rack###"); k++) {
                        playerFourPieces.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player One Rack###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Two Rack###"); k++) {
                        playerOneRack.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Two Rack###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Three Rack###"); k++) {
                        playerTwoRack.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Three Rack###")) {
                    for (int k = j+1; k < lines.indexOf("###Player Four Rack###"); k++) {
                        playerThreeRack.add(lines.get(k));
                        j = k;
                    }
                }
                if (lines.get(j).equals("###Player Four Rack###")) {
                    for (int k = j+1; k < lines.indexOf("###END OF FILE###"); k++) {
                        playerFourRack.add(lines.get(k));
                        j = k;
                    }
                }
            }
            System.out.println(playerOneTowers);
            
            hexBoard = new Board(root);
            TileDeck theDeck = new TileDeck(root, tiles);
            if( network ){
                Board.setTerrainCoords();
                GameLoop.getInstance().setPlayers(null);
                Player player = GameLoop.getInstance().getPlayer();
                System.out.println(player.getName());
                playerList.add(player);
            }
            playerBoard = PlayerBoard.getInstance();
            infoPan = new InfoPanel(root);
            rackG = new PlayerRackGUI(root, playerList, infoPan);
            
            for (int n = 0; n <playerList.size(); n++) {
                playerList.get(n).getPlayerRack().registerObserver(rackG);
            }
            System.out.println("player racks initialized");
            TheCupGUI theCup = new TheCupGUI(root, rackG);
            DiceGUI.getInstance();
            new Dice();
            SpecialCharView specialChar = new SpecialCharView(root);
            
            System.out.println("initializing game");
            GameLoop.getInstance().initGame(uniqueInstance);
            Board.populateGameBoard();
            Board.showTerrains();

            for (String s : playerOneCoords) {
                playerList.get(0).addHexOwned(Board.getTerrainWithCoord(new Coord(s)));
                Board.getTerrainWithCoord(new Coord(s)).setExplored(true);
            }
            for (String s : playerTwoCoords) {
                playerList.get(1).addHexOwned(Board.getTerrainWithCoord(new Coord(s)));
                Board.getTerrainWithCoord(new Coord(s)).setExplored(true);
            }
            for (String s : playerThreeCoords) {
                playerList.get(2).addHexOwned(Board.getTerrainWithCoord(new Coord(s)));
                Board.getTerrainWithCoord(new Coord(s)).setExplored(true);
            }
            for (String s : playerFourCoords) {
                playerList.get(3).addHexOwned(Board.getTerrainWithCoord(new Coord(s)));
                Board.getTerrainWithCoord(new Coord(s)).setExplored(true);
            }

            for (String s : playerOneTowers) {
                String[] input = s.split(" ");
                playerList.get(0).addFort(Board.getTerrainWithCoord(new Coord(input[0])), new Fort(input[1]));
            }
            for (String s : playerTwoTowers) {
                String[] input = s.split(" ");
                playerList.get(1).addFort(Board.getTerrainWithCoord(new Coord(input[0])), new Fort(input[1]));
            }
            for (String s : playerThreeTowers) {
                String[] input = s.split(" ");
                playerList.get(2).addFort(Board.getTerrainWithCoord(new Coord(input[0])), new Fort(input[1]));
            }
            for (String s : playerFourTowers) {
                String[] input = s.split(" ");
                playerList.get(3).addFort(Board.getTerrainWithCoord(new Coord(input[0])), new Fort(input[1]));
            }

            for (String s : playerOnePieces) {
                String[] input = s.split("~");
                playerList.get(0).playPiece(new Creature(input[1]), Board.getTerrainWithCoord(new Coord(input[0])));
            }
            for (String s : playerTwoPieces) {
                String[] input = s.split("~");
                playerList.get(1).playPiece(new Creature(input[1]), Board.getTerrainWithCoord(new Coord(input[0])));
            }
            for (String s : playerThreePieces) {
                String[] input = s.split("~");
                playerList.get(2).playPiece(new Creature(input[1]), Board.getTerrainWithCoord(new Coord(input[0])));
            }
            for (String s : playerFourPieces) {
                String[] input = s.split("~");
                playerList.get(3).playPiece(new Creature(input[1]), Board.getTerrainWithCoord(new Coord(input[0])));
            }

            for (String s : playerOneRack) {
                String[] tokens = s.split(",");
                if (tokens[0].equals("Creature"))
                    playerList.get(0).getPlayerRack().addPiece(new Creature(s));
                else if (tokens[0].equals("Income"))
                    playerList.get(0).getPlayerRack().addPiece(new SpecialIncome(s));
                else if (tokens[0].equals("RandomEvent"))
                    playerList.get(0).getPlayerRack().addPiece(RandomEventFactory.createRandomEvent(tokens[1]));
            }
            for (String s : playerTwoRack) {
                String[] tokens = s.split(",");
                System.out.println(tokens[0] + tokens[1]);
                if (tokens[0].equals("Creature"))
                    playerList.get(1).getPlayerRack().addPiece(new Creature(s));
                else if (tokens[0].equals("Income"))
                    playerList.get(1).getPlayerRack().addPiece(new SpecialIncome(s));
                else if (tokens[0].equals("RandomEvent"))
                    playerList.get(1).getPlayerRack().addPiece(RandomEventFactory.createRandomEvent(tokens[1]));
            }
            for (String s : playerThreeRack) {
                String[] tokens = s.split(",");
                if (tokens[0].equals("Creature"))
                    playerList.get(2).getPlayerRack().addPiece(new Creature(s));
                else if (tokens[0].equals("Income"))
                    playerList.get(2).getPlayerRack().addPiece(new SpecialIncome(s));
                else if (tokens[0].equals("RandomEvent"))
                    playerList.get(2).getPlayerRack().addPiece(RandomEventFactory.createRandomEvent(tokens[1]));
            }
            for (String s : playerFourRack) {
                String[] tokens = s.split(",");
                if (tokens[0].equals("Creature"))
                    playerList.get(3).getPlayerRack().addPiece(new Creature(s));
                else if (tokens[0].equals("Income"))
                    playerList.get(3).getPlayerRack().addPiece(new SpecialIncome(s));
                else if (tokens[0].equals("RandomEvent"))
                    playerList.get(3).getPlayerRack().addPiece(RandomEventFactory.createRandomEvent(tokens[1]));
            }

            for (Player p : playerList)
                playerBoard.updateGoldIncomePerTurn(p);

            PlayerRackGUI.disableAll();

            // execute playGame method in a background thread 
            // as to not block main GUI thread
            uniqueInstance.start();
            
            System.out.println("starting game..");
            gameLoopThread = new Thread(new Runnable(){
                public void run(){
                    while( runGameLoop ){ 
                        GameLoop.getInstance().playGame();
                    }
                }
            });
            gameLoopThread.start();
            
            // stop the gameLoopThread if the close button is pressed
            uniqueStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
                @Override
                public void handle( WindowEvent event ){
                    uniqueInstance.stop();
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
            uniqueInstance.stop();
        }

    }
    
    public static void createGame() {
        GameLoop.setNetworked(network);	
        
		try {
            helpText = new Text("initializing...");
            helpText.setFont(new Font(15));
            root.getChildren().add(helpText);
            helpText.relocate(width*0.25 , 0);

            doneButton = new GameButton(75, 35, width*0.25 + 5, height - 40, "Done", null);
            doneButton.deactivate();
            playAgainButton = new GameButton(75, 35, width*0.25 + 5, height - 80, "Again?", null);
            playAgainButton.deactivate();
            playAgainButton.hide();
            root.getChildren().addAll(doneButton.getNode(), playAgainButton.getNode());
            
            java.util.ArrayList<Player> playerList = new java.util.ArrayList<Player>();
            if( !network ){
                Player user  = new Player("User1", "YELLOW");
                Player user2 = new Player("User2", "BLUE");
                Player user3 = new Player("User3", "GREEN");
                Player user4 = new Player("User4", "RED");
                playerList.add(user);
                playerList.add(user2);
                playerList.add(user3);
                playerList.add(user4);
			    GameLoop.getInstance().setPlayers(playerList);
            }
			
			hexBoard = new Board(root);
			TileDeck theDeck = new TileDeck(root);
            if( network ){
                Board.setTerrainCoords();
                GameLoop.getInstance().setPlayers(null);
                Player player = GameLoop.getInstance().getPlayer();
                System.out.println(player.getName());
                playerList.add(player);
            }
			playerBoard = PlayerBoard.getInstance();
			infoPan = new InfoPanel(root);
			rackG = new PlayerRackGUI(root, playerList, infoPan);
			
            for (int i = 0; i <playerList.size(); i++) {
                playerList.get(i).getPlayerRack().registerObserver(rackG);
            }
            System.out.println("player racks initialized");
			TheCupGUI theCup = new TheCupGUI(root, rackG);
            SpecialCharView specialChar = new SpecialCharView(root);
			DiceGUI.getInstance();
			new Dice();
            
            specialChar.setCurrentPlayer(playerList.get(0));
			
            System.out.println("initializing game");
			GameLoop.getInstance().initGame(uniqueInstance);
			//rackG.generateButtons();

            // execute playGame method in a background thread 
            // as to not block main GUI thread
			uniqueInstance.start();
            System.out.println("starting game..");
			gameLoopThread = new Thread(new Runnable(){
                public void run(){
                    while( runGameLoop ){ 
                        GameLoop.getInstance().playGame();
                    }
                }
            });
			gameLoopThread.start();
            
            // stop the gameLoopThread if the close button is pressed
			uniqueStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
                @Override
                public void handle( WindowEvent event ){
                	uniqueInstance.stop();
                }
            });
			
		} catch(Exception e) {
			e.printStackTrace();
			uniqueInstance.stop();
		}
    }
    
    public static void loadGame() {
    	
    }
    public static void exit() {
    	Platform.exit();
    }
}

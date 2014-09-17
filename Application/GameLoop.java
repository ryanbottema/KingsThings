package KAT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.application.Platform;

/*
 * Class for handling the Game Loop and various game phases.
 * Uses the singleton class pattern.
 */
public class GameLoop {
    private Player[] playerList; //list of the different players in the game.
    private static GameLoop uniqueInstance; //unique instance of the GameLoop class
    private static Game GUI;
    private static boolean networked = false;
    protected static Player wildThings;
    private int phaseNumber; //int to keep track of which phase the game is on.
    private TheCup cup;
    private Player player;
    private Player playerClicked;
    private Piece pieceClicked;
    private boolean isPaused, freeClicked, paidClicked, doneClicked;
    private int numPlayers = 0;
    private PlayerRackGUI rackG;
    private Coord[] startingPos;
    private String localPlayer;
    protected ArrayList<Coord> battleGrounds;
    private boolean syncronizer;
    private ArrayList<Player> victorList;
    private boolean playAgainClicked;
    private Piece randomEvent;
    private boolean randomEventFlag;

    /*
     * Constructor.
     */
    protected GameLoop() {
    	battleGrounds = new ArrayList<Coord>();
        victorList = new ArrayList<Player>();
        phaseNumber = 0;
        cup = TheCup.getInstance();
        freeClicked = false;
        paidClicked = false;
        doneClicked = false;
        playAgainClicked = false;
        randomEventFlag = false;
        cup.initCup();
        // playerList = new Player[4];
    }

    /*
     * returns a unique instance of the GameLoop class, unless one already exists.
     */
    public static GameLoop getInstance(){
        if( networked ){
            return NetworkGameLoop.getInstance();
        }
        if(uniqueInstance == null){
            uniqueInstance = new GameLoop();
        }
        return uniqueInstance;
    }

    public static void setNetworked( boolean _networked ){
        networked = _networked;
    }

    public void setPlayers(ArrayList<Player> player) {
        int i = 0;
        playerList = new Player[4];
        
        // Create starting spots, will change this for fewer players in future
        Coord[] validPos = {  new Coord(2,-3,1),new Coord(2,1,-3),new Coord(-2,3,-1),new Coord(-2,-1,3) };
        startingPos = validPos;
        
        System.out.println(player);
        
        this.player = player.get(0);
        for (Player p : player) {
            playerList[i] = p;
            playerList[i].addGold(10);
            playerList[i].getPlayerRack().setOwner(playerList[i]);
            if (phaseNumber != -2)
                playerList[i].getPlayerRack().setPieces(cup.drawInitialPieces(10));
            System.out.println(playerList[i].getName() + ": "+ PlayerRack.printList(playerList[i].getPlayerRack().getPieces()));
            i++;
            numPlayers++;
       }
    }
//    public void addPlayer(Player p) {
//    	if (playerList == null || playerList.length == 0) {
//    		numPlayers = 1;
//    		playerList = new Player[1];
//    		playerList[0] = p;
//    	} else {
//	    	Player[] tempPlayerList = new Player[playerList.length + 1];
//	    	for (int i = 0; i < playerList.length; i++) {
//	    		tempPlayerList[i] = playerList[i];
//	    	}
//	    	tempPlayerList[tempPlayerList.length - 1] = p;
//	    	playerList = tempPlayerList;
//	    	for (Player delete : tempPlayerList) {
//	    		delete = null;
//	    	}
//	    	numPlayers++;
//    	}
//    }

    /*
     * The first thing done in the game.
     *
     * (1) Initialize the Bank
     * (2) Initializes the Cup
     * (3) Determines which side of the special character pieces will be used.
     * (4) Populate the game board
     * (5) Determine Player starting positions
     * (6) Players pick their hexes.
     * (7) Players take 10 gold, 1 tower from the bank, 10 things from the cup.
     * (8) Players can exchange their "things" for ones drawn from the cup.
     * (9) Prepare the terrain deck.
     */
    public void initGame(Game GUI) {
        rackG = GUI.getRackGui();
        
        this.GUI = GUI;
//        setupListeners();
        pause();
        if (phaseNumber != -2)
            phaseNumber = -1;
        ClickObserver.getInstance().setTerrainFlag("Setup: deal");
        setButtonHandlers();
        PlayerBoard.getInstance().updateNumOnRacks();
        wildThings = new Player("wildThings", "BLACK", true);
    }
    
    public void addStartingHexToPlayer(){
        
        Terrain t = ClickObserver.getInstance().getClickedTerrain();
        
        if( t == null ){
            //System.out.println("Select a hex");     
        } else {
            Coord coords = t.getCoords();
            boolean valid = false;
            for( int i=0; i<startingPos.length; i++ ){
                if( !t.isOccupied() &&  startingPos[i].equals(coords)){
                     valid = true;

                     break;
                }
            }
            if( valid ){
                player.addHexOwned(t);
                t.setExplored(true);
                t.setOwner(player);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                    	PlayerBoard.getInstance().updateGoldIncomePerTurn(player);
                    }
                });
                //System.out.println("selected "+t.getType());
            }
        }
    }
    
    public void addHexToPlayer(){
        Terrain t = ClickObserver.getInstance().getClickedTerrain();
        ArrayList<Terrain> hexes = player.getHexesOwned();
       
        boolean valid = false;
        for( Terrain h : hexes ){
            if( t.compareTo(h) == 1 &&  !t.isOccupied() ){
                valid = true;
                break;
            }
        }
        if( valid ){
            player.addHexOwned(t);
            t.setExplored(true);
            t.setOwner(player);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	PlayerBoard.getInstance().updateGoldIncomePerTurn(player);
                }
            });
            unPause();
        }
    }

    public void recruitSpecials() {
        if (doneClicked)
            unPause();
    }

    public void playThings() {
        if (doneClicked) {
            unPause();
        }
    }

    public void useRandoms() {
        if (doneClicked)
            unPause();
    }

    public void constructFort() {
        if (phaseNumber == 7) {
            if (doneClicked)
                unPause();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                PlayerRackGUI.disableAll();
            }
        });
        final Terrain t = ClickObserver.getInstance().getClickedTerrain();
        ArrayList<Terrain> hexes = player.getHexesOwned();

        for( Terrain h : hexes ){
            if( t.compareTo(h) == 0 ){
                // during setup phase, players are given a tower for free
                if( phaseNumber != 0 ){
                    player.spendGold(5);
                } 
                player.constructFort(t);
                unPause();
                break;
            }
        }
        System.out.println(player.calculateIncome());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                t.setFortImage();
            	PlayerBoard.getInstance().updateGold(player);
            	PlayerBoard.getInstance().updateGoldIncomePerTurn(player);
            }
        });
    }

    private void setupPhase() {
        // prompt each player to select their initial starting position
        ClickObserver.getInstance().setTerrainFlag("Setup: SelectStartTerrain");
        for (final Player p : playerList) {
        	
            this.player = p;
            player.flipAllUp();
            ClickObserver.getInstance().setActivePlayer(this.player);
            pause();
            
            // Cover all terrains, uncover starting pos ones
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    GUI.getRackGui().setOwner(player);
                	Board.applyCovers();
                	for (Coord spot : startingPos) {
                		if (!Board.getTerrainWithCoord(spot).isOccupied())
                			Board.getTerrainWithCoord(spot).uncover();
                	}
                    GUI.getHelpText().setText("Setup Phase: " + p.getName() 
                            + ", select a valid hex to start your kingdom.");
                }
            });
            while( isPaused ){
                int num = p.getHexesOwned().size();
                if( num == 1 ){
                    unPause();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                        	PlayerBoard.getInstance().updateGold(player);
                        }
                    });
                    System.out.println("done");
                }
                try { Thread.sleep(100); } catch( Exception e ){ return; }
            }
            player.flipAllDown();
        }
        pause();
        
        // Now that all players have selected starting spots, flip over all terrains
        // *Note:  Not sure I understand the rules with regards to this, but I think this is right
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	 Board.showTerrains();
                 Board.removeBadWaters();
            }
        });
        while( isPaused ){
            try { Thread.sleep(100); } catch( Exception e ){ return; }
        }
        
        // Check if player has at least two land hexes around starting spot
        for( final Player p : playerList ) {
            this.player = p;

            player.flipAllUp();
            ClickObserver.getInstance().setActivePlayer(this.player);
            pause();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    GUI.getHelpText().setText(p.getName() 
                            + ", select a water hex to replace with from deck");
                    Board.removeBadAdjWaters();
                }
            });
            while( isPaused ){
                try { Thread.sleep(100); } catch( Exception e ){ return; }
            }

            player.flipAllDown();
        }
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
    			TileDeck.getInstance().slideOut();
            }
        });

        
        // next prompt each player to select an adjacent hex
        ClickObserver.getInstance().setTerrainFlag("Setup: SelectTerrain");
        // loop 2 times so each player adds 2 more hexes
        for( int i=0; i<2; i++ ){
            for( final Player p : playerList ) {
                this.player = p;

                player.flipAllUp();
                ClickObserver.getInstance().setActivePlayer(this.player);
                pause();
                
                final ArrayList<Terrain> ownedHexes = player.getHexesOwned();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Board.applyCovers();
                    }
                });
            	for (Terrain t1 : ownedHexes) {
            		Iterator<Coord> keySetIterator = Board.getTerrains().keySet().iterator();
                	while(keySetIterator.hasNext()) {
                		Coord key = keySetIterator.next();
                		final Terrain t2 = Board.getTerrains().get(key);
                		if (t2.compareTo(t1) == 1 && !t2.isOccupied() && !t2.getType().equals("SEA")) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                        			t2.uncover();
                                }
                            });
                		}
                	}
            	}
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        GUI.getRackGui().setOwner(player);
                        GUI.getHelpText().setText("Setup Phase: " + p.getName() 
                                + ", select an adjacent hex to add to your kingdom.");
                    }
                });
                // forces the GameLoop thread to wait until unpaused
                while( isPaused ){
                    try { Thread.sleep(100); } catch( Exception e ){ return; }
                }
                player.flipAllDown();
            }
        }
        // prompt each player to place their first tower
        ClickObserver.getInstance().setTerrainFlag("Construction: ConstructFort");
        for( final Player p : playerList ) {
            this.player = p;
            
            player.flipAllUp();
            ClickObserver.getInstance().setActivePlayer(this.player);
            pause();
            
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Board.applyCovers();
                    GUI.getRackGui().setOwner(player);
                    GUI.getHelpText().setText("Setup Phase: " + p.getName() 
                            + ", select one of your tiles to place a tower.");
                }
            });
            
            // sleeps to avoid null pointer (runLater is called before player.getHexesOwned() below)
            try { Thread.sleep(50); } catch( Exception e ){ return; }
            ArrayList<Terrain> ownedHexes = player.getHexesOwned();
            
            for (final Terrain t : ownedHexes) {
            	
            	if (t.getOwner().getName().equals(player.getName())) { 
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                    		t.uncover();
                        }
                    });
            	}
            }
            while( isPaused ){
                try { Thread.sleep(100); } catch( Exception e ){ return; }
            }
            player.flipAllDown();
        }
        // allow players to add some or all things to their tiles.
        ClickObserver.getInstance().setTerrainFlag("RecruitingThings: PlaceThings");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GUI.getDoneButton().setDisable(false);
            }
        });
        for (final Player p : playerList) {
            this.player = p;
            player.flipAllUp();
            doneClicked = false;
            ClickObserver.getInstance().setClickedTerrain(p.getHexesOwned().get(2));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	ClickObserver.getInstance().whenTerrainClicked();
                    GUI.getRackGui().setOwner(player);
                    Board.applyCovers();
                    GUI.getHelpText().setText("Setup Phase: " + p.getName()
                            + ", place some or all of your things on a tile you own.");
                }
            });
            ClickObserver.getInstance().setActivePlayer(this.player);
            pause();
            ArrayList<Terrain> ownedHexes = player.getHexesOwned();
            for (final Terrain t : ownedHexes) {
            	if (t.getOwner().getName().equals(player.getName())) {
	                Platform.runLater(new Runnable() {
	                    @Override
	                    public void run() {
	                		t.uncover();
	                    }
	                });
            	}
            }
            
            while (isPaused) {
                try { Thread.sleep(100); } catch(Exception e) { return; }
            }
            player.flipAllDown();
        }
        ClickObserver.getInstance().setTerrainFlag("");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Board.removeCovers();
            }
        });
    }

    /*
     * Used when loading one of the 3 premade game files. It probably doesn't have to wait 17 seconds, but on my virtual machine it does because
     * it's stupidly slow haha
     */
    private void loadingPhase() {
        try { Thread.sleep(10000); } catch(InterruptedException e) { return; }
        ClickObserver.getInstance().setTerrainFlag("");
    }

    /*
     * Each player in the game MUST do this phase.
     * Calculates the amount of gold that each player earns this turn.
     */
    private void goldPhase() {
        System.out.println("In the gold collection phase");
        GUI.getHelpText().setText("Gold Collection phase: income collected.");
        for (int i = 0; i < 4; i++) {
            playerList[i].addGold(playerList[i].calculateIncome());
            final int j = i;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    PlayerBoard.getInstance().updateGold(playerList[j]);
                    PlayerBoard.getInstance().updateGoldIncomePerTurn(playerList[j]);
                }
            });
        }
        try { Thread.sleep(2000); } catch( InterruptedException e ){ return; }
    }

    /*
     * Optional.
     * Players can attempt to recruit one special character.
     */
    private void recruitSpecialsPhase() {
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	DiceGUI.getInstance().uncover();
                GUI.getDoneButton().setDisable(false);
            }
        });
        
        for (final Player p : playerList) {
            this.player = p;
            player.flipAllUp();
            ClickObserver.getInstance().setActivePlayer(p);

            ClickObserver.getInstance().setTerrainFlag("");
            ClickObserver.getInstance().setClickedTerrain(player.getHexesOwned().get(0));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	ClickObserver.getInstance().whenTerrainClicked();
                }
            });
            
            SpecialCharView.setCurrentPlayer(p);
            SpecialCharView.getSpecialButton().activate();
            SpecialCharView.getCharacterGrid().setVisible(false);
            doneClicked = false;
            ClickObserver.getInstance().setTerrainFlag("RecruitingSpecialCharacters");

            pause();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    GUI.getHelpText().setText(player.getName() + ", Try Your Luck At Recruiting A Special Character!");
                    GUI.getRackGui().setOwner(player);
                }
            });

            while (isPaused) {
                while (!doneClicked) {
                    try { Thread.sleep(100); } catch( Exception e ){ return; }
                }
                try { Thread.sleep(100); } catch( Exception e ){ return; }
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    DiceGUI.getInstance().setFaceValue(0);
                    SpecialCharView.getCharacterGrid().setVisible(false);
                    Board.removeCovers();
                }
            });
            player.flipAllDown();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	DiceGUI.getInstance().cover();
            }
        });
    }

    /*
     * Players MUST do this.
     * Draw free things from the cup.
     * Buy paid recruits.
     * Trade unwanted things from their rack.
     * Place things on the board.
     */
    private void recruitThingsPhase() {

        ClickObserver.getInstance().setTerrainFlag("RecruitingThings: PlaceThings");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GUI.getDoneButton().setDisable(false);
            }
        });
        int numToDraw = 0;
        boolean flag;
        
        for (final Player p : playerList) {
            doneClicked = false;
            this.player = p;
            player.flipAllUp();
            ClickObserver.getInstance().setActivePlayer(player);
            flag = true;
            pause();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    GUI.getHelpText().setText("Recruitment Phase: " + p.getName()
                            + ", draw free/paid Things from The Cup, then click 'done'");
                    GUI.getRackGui().setOwner(player);
                    TheCupGUI.update();
                }
            });
            
            while (isPaused) {
                while (!doneClicked) {
                    if (freeClicked) {
                        if (flag) {
                            System.out.println(player.getName() + " -clicked free");
                            numToDraw = (int)Math.ceil(player.getHexesOwned().size() / 2.0);
                            System.out.println(numToDraw + " -num to draw");
                            final int finNumToDraw = numToDraw;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    TheCupGUI.setFieldText(""+finNumToDraw);
                                }
                            });
                            flag = false;
                        }
                    }
                    if (paidClicked) {
                        flag = true;
                        if (flag) {
                        	Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                	PlayerBoard.getInstance().updateGold(player);
                                }
                            });
                            flag = false;
                        }
                    }
                    try { Thread.sleep(100); } catch( Exception e ){ return; }
                }
                try { Thread.sleep(100); } catch( Exception e ){ return; }
            }
            player.flipAllDown();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GUI.getDoneButton().setDisable(true);
            }
        });

        ClickObserver.getInstance().setTerrainFlag("");
    }

    /*
     * Optional.
     * Each player can play ONE random event from their rack.
     */
    private void randomEventPhase() {
        ClickObserver.getInstance().setTerrainFlag("RandomEvents");
        ArrayList<Piece> randomEvents = new ArrayList<Piece>();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Game.getHelpText().setText("Random Event Phase. Players may now play 1 Random Event from their racks.");
            }
        });

        try { Thread.sleep(2000); } catch(Exception e) { return; }

        for (Player p : playerList) {
            player = p;
            doneClicked = false;
            ClickObserver.getInstance().setActivePlayer(player);

            pause();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    TheCupGUI.update();
                    Game.getRackGui().setOwner(player);
                }
            });

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Game.getHelpText().setText(player.getName() + ", you may now play one of your Random Events.");
                }
            });

            while (isPaused) {
                while (!doneClicked) {
                    if (randomEventFlag) {
                        System.out.println(randomEvent.getName());
                        ((RandomEvent)randomEvent).performAbility();
                        break;
                    }
    
                    try { Thread.sleep(100); } catch( Exception e ){ return; }
                }
                try { Thread.sleep(100); } catch( Exception e ){ return; }
            }
            randomEventFlag = false;
            randomEvent = null;
        }
        ClickObserver.getInstance().setTerrainFlag("");  
    }

    /*
     * Optional.
     * Players may attempt to move their counters around the board.
     */
    private void movementPhase() {
        ClickObserver.getInstance().setCreatureFlag("");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GUI.getDoneButton().setDisable(false);
                TheCupGUI.update();
                Board.removeCovers();
            }
        });
        for (Player p : playerList) {
        	player = p;
            player.flipAllUp();
	        ClickObserver.getInstance().setActivePlayer(player);
	        ClickObserver.getInstance().setCreatureFlag("Movement: SelectMovers");
	        InfoPanel.uncover(player.getName());
	        if (p.getHexesWithPiece().size() > 0) {
	        	ClickObserver.getInstance().setClickedTerrain(p.getHexesWithPiece().get(0));
	        }
	        pause();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	ClickObserver.getInstance().whenTerrainClicked();
        	        GUI.getHelpText().setText("Movement Phase: " + player.getName()
                            + ", Move your armies");
        	        Game.getRackGui().setOwner(player);
                }
            });
	        
	        while (isPaused) {
            	try { Thread.sleep(100); } catch( Exception e ){ return; }  
	        }
	        InfoPanel.cover(player.getName());
            player.flipAllDown();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GUI.getDoneButton().setDisable(true);
            }
        });
        ClickObserver.getInstance().setCreatureFlag("");
    }

    /*
     * Optional, unless combat is declared on you.
     * Players may explore or fight battles.
     * 
     * List of important Data and description. Hope this helps keep the ideas clear:
     * 
     * 		- (boolean)								exploring		> True if the terrain has yet to be explored
     * 		- (Terrain) 							battleGround:	> The Terrain in which the combat is taking place
     * 		- (ArrayList<Player>) 					combatants:		> List of players with pieces on battleGround (Must fight if so)
     * 		- (HashMap<String, ArrayList<Piece>>) 	attackingPieces	> HashMap of players names to the pieces they have in battle 
     * 																  (including forts and city/village)
     * 		- (HashMap<String, Player>) 			toAttacks		> HashMap of players names to the Player they want to attack.
     * 																  Important for multiple players on a single Terrain, where each
     * 																  round each player can select a different player to attack
     * 		- (HashMap<String, ArrayList<Piece>>) 	successAttacks	> HashMap of player names to the pieces they have that had 
     * 																  successful attack. Changes each phase (From magic to ranged etc)
     * 		- (ArrayList<Piece>) 					phaseThings		> List of pieces that are to be used during a particular phase (ie magic
     * 																  , ranged etc). This is used to keep track of what pieces the gui should
     * 																  set up to be selected, and then dice rolled. 
     * 																  For example: 
     * 																  beginning of magic phase, phaseThings will be empty. First combatant has 
     * 																  two magic creatures which are added to phaseThings. They then roll for one, 
     * 																  it is removed, and then roll the other, which is removed. Now that phaseThings
     * 																  is of size 0, the next player repeats.
     * 		- (HashMap<String, ArrayList<Piece>>)	toInflict		> HashMap of players name to pieces they selected to take hits.
     * 
     * 
     * Combat testing checklist:
     * 
     * 		- Exploring: 	- one player enters and rolls a 1 or 6								\ - cannot test now, for testing, 1 or 6 still triggers exploration
     * 		 				- one player enters and retreats									X - currently does something weird to pieces and they are unable to be uncovered again 								
     * 		 				- one player enters and wins via combat
     * 						- two players enter, one wins to move on to exploring combat
     * 						- two players enter, both retreat
     * 						- two players enter, both are defeated (kill each other)
     * 
     * 
     *
     * 		
     */
    private void combatPhase() {
    	
    	pause();
    	Dice.setFinalValMinusOne();

    	// Go through each battle ground a resolve each conflict
    	for (Coord c : battleGrounds) {
    		
        	ClickObserver.getInstance().setTerrainFlag("");
        	
        	System.out.println("Entering battleGround");
    		final Terrain battleGround = Board.getTerrainWithCoord(c);
    		
    		// find the owner of terrain for post combat
        	Player owner = battleGround.getOwner();
        	
    		// simulate a click on the first battleGround, cover all other terrains
    		ClickObserver.getInstance().setClickedTerrain(battleGround);
    		Platform.runLater(new Runnable() {
                @Override
                public void run() {
            		ClickObserver.getInstance().whenTerrainClicked();
            		Board.applyCovers();
            		ClickObserver.getInstance().getClickedTerrain().uncover();
                	ClickObserver.getInstance().setTerrainFlag("Disabled");
                }
            });
    		
    		// Get the fort
    	    Fort battleFort = battleGround.getFort();  
    		
    		// List of players to battle in the terrain
    		ArrayList<Player> combatants = new ArrayList<Player>();
    		
    		// List of pieces that can attack (including forts, city/village)
    		HashMap<String, ArrayList<Piece>> attackingPieces = new HashMap<String, ArrayList<Piece>>();
    		
    		System.out.println(battleGround.getContents().keySet());
    		
    		Iterator<String> keySetIterator = battleGround.getContents().keySet().iterator();
	    	while(keySetIterator.hasNext()) {
	    		String key = keySetIterator.next();
	    		
    			combatants.add(battleGround.getContents().get(key).getOwner());
    			attackingPieces.put(battleGround.getContents().get(key).getOwner().getName(), (ArrayList<Piece>) battleGround.getContents().get(key).getStack().clone()); 
    			
	    	}
	    	
	    	
	    	
	    	// if the owner of the terrain has no pieces, just a fort or city/village
			if (!combatants.contains(battleGround.getOwner()) && battleFort != null) {
				combatants.add(battleGround.getOwner());
				attackingPieces.put(battleGround.getOwner().getName(), new ArrayList<Piece>());
			}

    		// add forts and city/village to attackingPieces
    		if (battleFort != null) {
    			attackingPieces.get(battleGround.getOwner().getName()).add(battleFort);
    		}
    		
    		keySetIterator = attackingPieces.keySet().iterator();
	    	while (keySetIterator.hasNext()) {
	    		String key = keySetIterator.next();
	    		
	    		for (Piece p : attackingPieces.get(key)) {
	    			if (p.getName().equals("Baron Munchhausen") || p.getName().equals("Grand Duke")) {
	    				if (p.getOwner() != battleGround.getOwner())
	    					((Performable)p).specialAbility();
	    			}
	    				
	    		}
	    	}
    		// TODO implement city/village
//    		if (City and village stuff here)
    		System.out.println(combatants);
    		
    		boolean exploring = (!battleGround.isExplored() && battleGround.getContents().size() == 1);
    		// Fight until all attackers are dead, or until the attacker becomes the owner of the hex
    		while (combatants.size() > 1 || exploring) {
    			
    /////////////Exploration
            	// Check if this is an exploration battle:
        		// Must fight other players first
    			boolean fightingWildThings = false;
            	if (exploring) {

    				// Set the battleGround explored
    				battleGround.setExplored(true);
            	
            		String exploringPlayer = null;
            		Iterator<String> keySetIter = battleGround.getContents().keySet().iterator();
        	    	while(keySetIter.hasNext()) {
        	    		String key = keySetIter.next();
        	    		exploringPlayer = key;
        	    	}
            		player = battleGround.getContents(exploringPlayer).getOwner();
            		player.flipAllUp();
        	    	
            		// Get user to roll die to see if explored right away
    				Platform.runLater(new Runnable() {
    	                @Override
    	                public void run() {
    	                	DiceGUI.getInstance().uncover();
    	                	GUI.getHelpText().setText("Attack phase: " + player.getName() 
    	                			+ ", roll the die. You need a 1 or a 6 to explore this terrain without a fight!");
    	                }
    				});
    				
                	// Dice is set to -1 while it is 'rolling'. This waits for the roll to stop, ie not -1
    				int luckyExplore = -1;
    				while (luckyExplore == -1) {
    					try { Thread.sleep(100); } catch( Exception e ){ return; }
    					luckyExplore = Dice.getFinalVal();
    				}
    				
    				// If success TODO FIX this 
    				if (luckyExplore == 1 || luckyExplore == 6) {
    					
    					// Cover die. Display msg
    					Platform.runLater(new Runnable() {
    		                @Override
    		                public void run() {
    		                	DiceGUI.getInstance().cover();
    		                	GUI.getHelpText().setText("Attack phase: Congrats!" + player.getName() 
    		                			+ "!, You get the terrain!");
    		                }
    					});
    					try { Thread.sleep(1000); } catch( Exception e ){ return; }
    					exploring = false;
    					break;
    					
    				} else { // Else failure. Must fight or bribe
    					
    					fightingWildThings = true;
    					
    					// Cover die. Display msg
    					Platform.runLater(new Runnable() {
    		                @Override
    		                public void run() {
    		                	DiceGUI.getInstance().cover();
    		                	battleGround.coverPieces();
    		                	GUI.getHelpText().setText("Attack phase: Boooo!" + player.getName() 
    		                			+ "!, You have to bribe, or fight for your right to explore!");
    		                }
    					});
    					try { Thread.sleep(1000); } catch( Exception e ){ return; }
    					
    					// add luckyExplore amount of pieces to terrain under wildThing player
    					final ArrayList<Piece> wildPieces = TheCup.getInstance().draw(luckyExplore);
    						
    					// Update the infopanel with played pieces. Active done button
    					Platform.runLater(new Runnable() {
    		                @Override
    		                public void run() {
    		                	wildThings.playWildPieces(wildPieces, battleGround);
    		                    GUI.getDoneButton().setDisable(false);
    		                    battleGround.coverPieces();
    		                	InfoPanel.showTileInfo(battleGround);
    		                    
    		                }
    					});
    					try { Thread.sleep(100); } catch( Exception e ){ return; }
    					
    					//////Bribing here
    					pause();
    					ClickObserver.getInstance().setCreatureFlag("Combat: SelectCreatureToBribe");
    					
    					// Uncover the pieces that the player can afford to bribe
    					// canPay is false if there are no Pieces that the user can afford to bribe
    					boolean canPay = false;
    					for (final Piece p : battleGround.getContents(wildThings.getName()).getStack()) {
    						if (((Combatable)p).getCombatValue() <= player.getGold()) {
    							canPay = true;
    							Platform.runLater(new Runnable() {
    				                @Override
    				                public void run() {
    				                	p.uncover();
    				                }
    							});
    						}
    					}
    					try { Thread.sleep(50); } catch( Exception e ){ return; }
    					
    					// Continue looping until there are no more pieces user can afford to bribe, or user hits done button
    					while (canPay && isPaused) {
    						
    						Platform.runLater(new Runnable() {
	    		                @Override
	    		                public void run() {
	    		                	GUI.getHelpText().setText("Attack phase: " + player.getName() 
	    		                			+ ", Click on creatures you would like to bribe");
	    		                }
    						});
    						
    						// wait for user to hit done, or select a piece
    						pieceClicked = null;
    						while(pieceClicked == null && isPaused) {
    							try { Thread.sleep(100); } catch( Exception e ){ return; }
    						}
    						if (pieceClicked != null) {
    							
    							// spend gold for bribing. Remove clicked creature
    							player.spendGold(((Combatable)pieceClicked).getCombatValue());
    							((Combatable)pieceClicked).inflict();
    							
    							// cover pieces that are too expensive
    							Platform.runLater(new Runnable() {
    				                @Override
    				                public void run() {
    				                	GUI.getHelpText().setText("Attack phase: " + player.getName() 
    			                			+ " bribed " + pieceClicked.getName());
    				                	InfoPanel.showTileInfo(battleGround);
    				                	if (battleGround.getContents(wildThings.getName()) != null) {
	    				                    for (Piece p : battleGround.getContents(wildThings.getName()).getStack()) {
	    				                    	if (((Combatable)p).getCombatValue() > player.getGold())
	    				                    		p.cover();
	    				                    }
    				                	}
    				                }
    							});
    							try { Thread.sleep(100); } catch( Exception e ){ return; }
    							
    							// Check if there are any pieces user can afford to bribe and set canPay to true if so
    							canPay = false;
    							if (battleGround.getContents(wildThings.getName()) == null || battleGround.getContents(wildThings.getName()).getStack().size() == 1)
    								break;
    							else {
	    							for (final Piece p : battleGround.getContents(wildThings.getName()).getStack()) {
	    								if (((Combatable)p).getCombatValue() <= player.getGold())  {
	    									canPay = true;
	    									break;
	    								}
	    							}
    							}
    							try { Thread.sleep(100); } catch( Exception e ){ return; }
    						}
    					}
    					Platform.runLater(new Runnable() {
			                @Override
			                public void run() {
			                	GUI.getHelpText().setText("Attack phase: " + player.getName() 
		                			+ " done bribing");
			                }
    					});
    					System.out.println("Made it past bribing");
    					try { Thread.sleep(1000); } catch( Exception e ){ return; }
    					ClickObserver.getInstance().setCreatureFlag("Combat: SelectPieceToAttackWith");
    					
    					// Done bribing, on to fighting
    					
    					// find another player to control wildThings and move on to regular combat
    					// to be used later 
    					Player explorer = player;
    					Player wildThingsController = null;
    					for (Player p : playerList) {
    						if (!p.getName().equals(player.getName()))
    							wildThingsController = p;
    					}
    					
    					// If wild things still has pieces left:
    					if (battleGround.getContents().containsKey(wildThings.getName())) {
	    					combatants.add(battleGround.getContents().get(wildThings.getName()).getOwner());
	    	    			attackingPieces.put(wildThings.getName(), (ArrayList<Piece>) battleGround.getContents().get(wildThings.getName()).getStack().clone()); 
    					}
    				}
    				
    				// cover pieces again
    				Platform.runLater(new Runnable() {
    	                @Override
    	                public void run() {
    	                	battleGround.coverPieces();
    	                }
    				});
    				
    				player.flipAllDown();
            	} // end if (exploring)
    			
    			System.out.println("combatants.size() > 1   : " + combatants.size());
    			System.out.println(combatants);
    			
    			// This hashMap keeps track of the player to attack for each  player
    			HashMap<String, Player> toAttacks = new HashMap<String, Player>();

				// each player selects which other player to attack in case of more than two combatants
    			if (combatants.size() > 2) {
    				
    				for (final Player p : combatants) {
    					if (!p.isWildThing()) {
	        	    		pause();
	        	    		ClickObserver.getInstance().setPlayerFlag("Attacking: SelectPlayerToAttack");
	        	    		player = p;
		                	Platform.runLater(new Runnable() {
		    	                @Override
		    	                public void run() {
		    	                	PlayerBoard.getInstance().applyCovers();
		    	                	battleGround.coverPieces();
		    	        	        GUI.getHelpText().setText("Attack phase: " + player.getName()
		    	                            + ", select which player to attack");
			        	    	}
		    	            });
		                	for (final Player pl : combatants) {
		                		if (!pl.getName().equals(player.getName())) {
		                			Platform.runLater(new Runnable() {
		    	    	                @Override
		    	    	                public void run() {
		    	    	                	PlayerBoard.getInstance().uncover(pl);
		    	    	                }
		                			});
		                		}
		                	}
		                	while (isPaused) {
		                     	try { Thread.sleep(100); } catch( Exception e ){ return; }  
		         	        }
		                	
		                	// ClickObserver sets playerClicked, then unpauses. This stores what player is attacking what player
		                	toAttacks.put(p.getName(), playerClicked);
	        	    		ClickObserver.getInstance().setPlayerFlag("");
		                	
	        	    	} 
    				}
    				combatants.remove(wildThings);
    				PlayerBoard.getInstance().removeCovers();
    				
        	    } else { // Only two players fighting
        	    	
        	    	for (Player p1 : combatants) {
        	    		for (Player p2 : combatants) {
        	    			if (!p1.getName().equals(p2.getName())) {
        	        	    	toAttacks.put(p1.getName(), p2);
        	    			}
        	    		}
        	    	}
        	    }
    			
 ///////////////////Call out bluffs here:
    			battleGround.flipPiecesUp();
    			for (final Player p : combatants) {
    				
    				// Make sure not wildThings
    				if (!p.isWildThing()) {
    					
    					ArrayList <Piece> callOuts = new ArrayList<Piece>();
    					
    					for (final Piece ap : attackingPieces.get(p.getName())) {
    						
    						// If not supported the gtfo
    						if (!ap.isSupported()) {

    							callOuts.add(ap);
    							((Combatable)ap).inflict();
    							try { Thread.sleep(250); } catch( Exception e ){ return; }  
    							Platform.runLater(new Runnable() {
	    	    	                @Override
	    	    	                public void run() {
	    	    	                	InfoPanel.showTileInfo(battleGround);
	    	    	                	GUI.getHelpText().setText("Attack phase: " + p.getName()
			    	                            + " lost their " + ap.getName() + " in a called bluff!");
	    	    	                }
	                			});
    							try { Thread.sleep(250); } catch( Exception e ){ return; }  
    						}
    					}
    					for (Piece co : callOuts) {
    						attackingPieces.get(p.getName()).remove(co);
    					}
						if (attackingPieces.get(p.getName()).size() == 0)
							attackingPieces.remove(p.getName());
    				}
    			}
    			
    			// Check for defeated armies:
				// - find player with no more pieces on terrain
				// - remove any such players from combatants
				// - if only one combatant, end combat
    			Player baby = null;
    			while (combatants.size() != attackingPieces.size()) {
					for (Player pl : combatants) {
						if (!attackingPieces.containsKey(pl.getName())) {
							baby = pl;
						}
					}
					combatants.remove(baby);
    			}
    			if (combatants.size() == 1) {
    				exploring = (!battleGround.isExplored() && battleGround.getContents().size() == 1);
    				continue;
    			}
    			
    			// Set up this HashMap that will store successful attacking pieces
    			HashMap<String, ArrayList<Piece>> successAttacks = new HashMap<String, ArrayList<Piece>>();
    			// Set up this HashMap that will store piece marked to get damage inflicted
				HashMap<String, ArrayList<Piece>> toInflict = new HashMap<String, ArrayList<Piece>>();
    			for (Player p : combatants) {
    				
    				successAttacks.put(p.getName(), new ArrayList<Piece>());
    				toInflict.put(p.getName(), new ArrayList<Piece>());
    			}
    			
    			// Array List of pieces that need to be used during a particular phase
				final ArrayList<Piece> phaseThings = new ArrayList<Piece>();
				
				// Notify next phase, wait for a second
				Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	battleGround.coverPieces();
	                	GUI.getHelpText().setText("Attack phase: Next phase: Magic!");
	                }
	            });
				
				// Pause for 2 seconds between phases
				try { Thread.sleep(2000); } catch( Exception e ){ return; }
				
/////////////////////// Magic phase
    			for (final Player pl : combatants) {
    				
    				player = pl;
    				// Cover all pieces
    				Platform.runLater(new Runnable() {
    	                @Override
    	                public void run() {
    	            		battleGround.coverPieces();
    	                }
    	            });
    				
    				// For each piece, if its magic. Add it to the phaseThings array
    				for (Piece p : attackingPieces.get(pl.getName())) {
    					if (p instanceof Combatable && ((Combatable)p).isMagic() && !(p instanceof Fort && ((Fort)p).getCombatValue() == 0)) 
    						phaseThings.add(p);
    				}
    				
    				// uncover magic pieces for clicking
    				if (phaseThings.size() > 0) {
	    				Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	    				for (Piece mag : phaseThings) 
	            					mag.uncover();
	    	                }
	    	            });
    				}


					
					System.out.println("----------------------------------------------------------------");
					System.out.println("attackingPieces.size(): " + attackingPieces.size());
					System.out.println("attackingPieces.keySet(): " + attackingPieces.keySet());
					Iterator<String> keySetIte = battleGround.getContents().keySet().iterator();
        	    	while(keySetIte.hasNext()) {
        	    		String key = keySetIte.next();

    					System.out.println("key: " + key);
    					System.out.println("attackingPieces.get(key).size():\n" + attackingPieces.get(key).size());
    					System.out.println("attackingPieces.get(key):\n" + attackingPieces.get(key));
        	    	}
					System.out.println("----------------------------------------------------------------");
					
    				// Have user select a piece to attack with until there are no more magic pieces
    				while (phaseThings.size() > 0) {
    					
    					// Display message prompting user to select a magic piece
    					Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	GUI.getHelpText().setText("Attack phase: " + player.getName() + ", select a magic piece to attack with");
	    	                }
	    	            });
    					
    					
    					
    					// Wait for user to select piece
        				pieceClicked = null;
        				ClickObserver.getInstance().setCreatureFlag("Combat: SelectPieceToAttackWith");
        				ClickObserver.getInstance().setFortFlag("Combat: SelectPieceToAttackWith");
    					while (pieceClicked == null) { try { Thread.sleep(100); } catch( Exception e ){ return; } }
	    				ClickObserver.getInstance().setCreatureFlag("");
	    				ClickObserver.getInstance().setFortFlag("");
	    				
	    				// hightlight piece that was selected, uncover the die to use, display message about rolling die
    					Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	pieceClicked.highLight();
	    	                	DiceGUI.getInstance().uncover();
	    	                	GUI.getHelpText().setText("Attack phase: " + player.getName() 
	    	                			+ ", roll the die. You need a " + ((Combatable)pieceClicked).getCombatValue() + " or lower");
	    	                }
    					});
    					
	                	// Dice is set to -1 while it is 'rolling'. This waits for the roll to stop, ie not -1
    					int attackStrength = -1;
    					while (attackStrength == -1) {
    						try { Thread.sleep(100); } catch( Exception e ){ return; }
    						attackStrength = Dice.getFinalVal();
    					}
						
    					// If the roll was successful. Add to successfulThings Array, and change it image. prompt Failed attack
    					if (attackStrength <= ((Combatable)pieceClicked).getCombatValue()) {
    						
    						successAttacks.get(player.getName()).add(pieceClicked);
    						Platform.runLater(new Runnable() {
    	    	                @Override
    	    	                public void run() {
    	    						((Combatable)pieceClicked).setAttackResult(true);
    	    						pieceClicked.cover();
    	    						pieceClicked.unhighLight();
    	    	                	GUI.getHelpText().setText("Attack phase: Successful Attack!");
    	    	                }
        					});
    						
    					} else { // else failed attack, update image, prompt Failed attack
    						Platform.runLater(new Runnable() {
		    	                @Override
		    	                public void run() {
		    						((Combatable)pieceClicked).setAttackResult(false);
		    						pieceClicked.cover();
    	    						pieceClicked.unhighLight();
		    	                	GUI.getHelpText().setText("Attack phase: Failed Attack!");
		    	                }
	    					});
    					}
    					
    					// Pause to a second for easy game play, remove the clicked piece from phaseThings
    					try { Thread.sleep(1000); } catch( Exception e ){ return; }
    					phaseThings.remove(pieceClicked);
    				}
    			}

				// For each piece that had success, player who is being attacked must choose a piece
    			// Gets tricky here. Will be tough for Networking :(
    			for (Player pl : combatants) {
    				
    				// Active player is set to the player who 'pl' is attack based on toAttack HashMap
    				player = toAttacks.get(pl.getName()); // 'defender'
    				final String plName = pl.getName();
    				
    				// For each piece of pl's that has a success (in successAttacks)
    				int i = 0;
    				for (final Piece p : successAttacks.get(plName)) {

    					// If there are more successful attacks then pieces to attack, break after using enough attacks
    					if (i >= attackingPieces.get(player.getName()).size()) {
    						break;
    					}
    					
    					// Display message, cover other players pieces, uncover active players pieces
	    				Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	GUI.getHelpText().setText("Attack phase: " + player.getName() + ", Select a Piece to take a hit from " + plName + "'s " + p.getName());
	    	                	battleGround.coverPieces();
	    	                	battleGround.uncoverPieces(player.getName());
	    	                }
						});
    					try { Thread.sleep(100); } catch( Exception e ){ return; }
    					
	    				// Cover pieces already choosen to be inflicted. Wait to make sure runLater covers pieces already selected
	    				for (final Piece pi : toInflict.get(player.getName())) {
	    					if (!((pi instanceof Fort) && ((Fort)pi).getCombatValue() > 0)) {
		    					Platform.runLater(new Runnable() {
			    	                @Override
			    	                public void run() {
				    					pi.cover();
			    	                }
								});
	    					}
	    				}//TODO here is where a pause might be needed
	    				
	    				// Wait for user to select piece
    					pieceClicked = null;
	    				ClickObserver.getInstance().setCreatureFlag("Combat: SelectPieceToGetHit");
	    				ClickObserver.getInstance().setFortFlag("Combat: SelectPieceToGetHit");
    					while (pieceClicked == null) { try { Thread.sleep(100); } catch( Exception e ){ return; } }
    					ClickObserver.getInstance().setCreatureFlag("");
	    				ClickObserver.getInstance().setFortFlag("");
		    			
    					// Add to arrayList in HashMap of player to mark for future inflict. Cover pieces
    					toInflict.get(player.getName()).add(pieceClicked);
    					Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	battleGround.coverPieces();
	    	                }
						});
    					i++;
    					try { Thread.sleep(100); } catch( Exception e ){ return; }
	    				
    				}
    				// Clear successful attacks for next phase
    				successAttacks.get(pl.getName()).clear();
    			}
    			
    			// Remove little success and failure images, inflict if necessary
    			for (Player pl : combatants) {
    				
    				// Change piece image of success or failure to not visible
    				for (Piece p : attackingPieces.get(pl.getName())) 
    					((Combatable)p).resetAttack();
    				
					// inflict return true if the piece is dead, and removes it from attackingPieces if so
    				// Inflict is also responsible for removing from the CreatureStack
    				for (Piece p : toInflict.get(pl.getName())) {
						if (((Combatable)p).inflict()) 
							attackingPieces.get(pl.getName()).remove(p);
						if (attackingPieces.get(pl.getName()).size() == 0)
							attackingPieces.remove(pl.getName());
    				}
    				
    				// Clear toInflict for next phase
    				toInflict.get(pl.getName()).clear();
    			}

				// Update the InfoPanel gui for changed/removed pieces
				Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
    					InfoPanel.showTileInfo(battleGround);
    					battleGround.coverPieces();
	                }
				});
    			
    			// Check for defeated armies:
				// - find player with no more pieces on terrain
				// - remove any such players from combatants
				// - if only one combatant, end combat
    			baby = null;
    			while (combatants.size() != attackingPieces.size()) {
					for (Player pl : combatants) {
						if (!attackingPieces.containsKey(pl.getName())) {
							baby = pl;
						}
					}
					combatants.remove(baby);
    			}
    			if (combatants.size() == 1) {
    				exploring = (!battleGround.isExplored() && battleGround.getContents().size() == 1);
    				continue;
    			}
    			
				// Notify next phase, wait for a second
				Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	            		battleGround.coverPieces();
	                	GUI.getHelpText().setText("Attack phase: Next phase: Ranged!");
	                }
	            });
				
				// Pause for 2 seconds between phases
				try { Thread.sleep(2000); } catch( Exception e ){ return; }
				
				
				
//////////////////// Ranged phase
				for (final Player pl : combatants) {
    				
    				player = pl;
    				// Cover all pieces
    				Platform.runLater(new Runnable() {
    	                @Override
    	                public void run() {
    	            		battleGround.coverPieces();
    	                }
    	            });
    				
    				// For each piece, if its ranged. Add it to the phaseThings array
    				for (Piece p : attackingPieces.get(pl.getName())) {
    					if (p instanceof Combatable && ((Combatable)p).isRanged() && !(p instanceof Fort && ((Fort)p).getCombatValue() == 0)) 
    						phaseThings.add(p);
    				}
    				
    				// uncover ranged pieces for clicking
    				if (phaseThings.size() > 0) {
	    				Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	    				for (Piece ran : phaseThings) 
	            					ran.uncover();
	    	                }
	    	            });
    				}
    				
    				// Have user select a piece to attack with until there are no more ranged pieces
    				while (phaseThings.size() > 0) {
    					
    					// Display message prompting user to select a ranged piece
    					Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	GUI.getHelpText().setText("Attack phase: " + player.getName() + ", select a ranged piece to attack with");
	    	                }
	    	            });

    					// Wait for user to select piece
        				pieceClicked = null;
        				ClickObserver.getInstance().setCreatureFlag("Combat: SelectPieceToAttackWith");
        				ClickObserver.getInstance().setFortFlag("Combat: SelectPieceToAttackWith");
    					while (pieceClicked == null) { try { Thread.sleep(100); } catch( Exception e ){ return; } }
	    				ClickObserver.getInstance().setCreatureFlag("");
	    				ClickObserver.getInstance().setFortFlag("");
	    				
	    				// hightlight piece that was selected, uncover the die to use, display message about rolling die
    					Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	pieceClicked.highLight();
	    	                	DiceGUI.getInstance().uncover();
	    	                	GUI.getHelpText().setText("Attack phase: " + player.getName()
	    	                            + ", roll the die. You need a " + ((Combatable)pieceClicked).getCombatValue() + " or lower");
	    	                }
    					});
    					
	                	// Dice is set to -1 while it is 'rolling'. This waits for the roll to stop, ie not -1
    					int attackStrength = -1;
    					while (attackStrength == -1) {
    						try { Thread.sleep(100); } catch( Exception e ){ return; }
    						attackStrength = Dice.getFinalVal();
    					}
						
    					// If the roll was successful. Add to successfulThings Array, and change it image. prompt Failed attack
    					if (attackStrength <= ((Combatable)pieceClicked).getCombatValue()) {
    						
    						successAttacks.get(player.getName()).add(pieceClicked);
    						Platform.runLater(new Runnable() {
    	    	                @Override
    	    	                public void run() {
    	    						((Combatable)pieceClicked).setAttackResult(true);
    	    						pieceClicked.cover();
    	    						pieceClicked.unhighLight();
    	    	                	GUI.getHelpText().setText("Attack phase: Successful Attack!");
    	    	                }
        					});
    						
    					} else { // else failed attack, update image, prompt Failed attack
    						Platform.runLater(new Runnable() {
		    	                @Override
		    	                public void run() {
		    						((Combatable)pieceClicked).setAttackResult(false);
		    						pieceClicked.cover();
    	    						pieceClicked.unhighLight();
		    	                	GUI.getHelpText().setText("Attack phase: Failed Attack!");
		    	                }
	    					});
    					}
    					
    					// Pause to a second for easy game play, remove the clicked piece from phaseThings
    					try { Thread.sleep(1000); } catch( Exception e ){ return; }
    					phaseThings.remove(pieceClicked);
    				}
    			}

				// For each piece that had success, player who is being attacked must choose a piece
    			// Gets tricky here. Will be tough for Networking :(
    			for (Player pl : combatants) {
    				
    				// Active player is set to the player who 'pl' is attack based on toAttack HashMap
    				player = toAttacks.get(pl.getName());
    				final String plName = pl.getName();
    				
    				// For each piece of pl's that has a success (in successAttacks)
    				int i = 0;
    				for (final Piece p : successAttacks.get(plName)) {

    					// If there are more successful attacks then pieces to attack, break after using enough attacks
    					if (i >= attackingPieces.get(player.getName()).size()) {
    						break;
    					}
    					
    					// Display message, cover other players pieces, uncover active players pieces
	    				Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	GUI.getHelpText().setText("Attack phase: " + player.getName() + ", Select a Piece to take a hit from " + plName + "'s " + p.getName());
	    	                	battleGround.coverPieces();
	    	                	battleGround.uncoverPieces(player.getName());
	    	                }
						});
    					
	    				// Cover pieces already choosen to be inflicted. Wait to make sure runLater covers pieces already selected
	    				for (final Piece pi : toInflict.get(player.getName())) {
	    					if (!((pi instanceof Fort) && ((Fort)pi).getCombatValue() > 0)) {
		    					Platform.runLater(new Runnable() {
			    	                @Override
			    	                public void run() {
				    					pi.cover();
			    	                }
								});
	    					}
	    				}//TODO here is where a pause might be needed
	    				
	    				// Wait for user to select piece
    					pieceClicked = null;
	    				ClickObserver.getInstance().setCreatureFlag("Combat: SelectPieceToGetHit");
	    				ClickObserver.getInstance().setFortFlag("Combat: SelectPieceToGetHit");
    					while (pieceClicked == null) { try { Thread.sleep(100); } catch( Exception e ){ return; } }
    					ClickObserver.getInstance().setFortFlag("");
    					ClickObserver.getInstance().setCreatureFlag("");
		    			
    					// Add to arrayList in HashMap of player to mark for future inflict. Cover pieces
    					toInflict.get(player.getName()).add(pieceClicked);
    					Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	battleGround.coverPieces();
	    	                }
						});
    					try { Thread.sleep(100); } catch( Exception e ){ return; }
	    				i++;
    				}
    				// Clear successful attacks for next phase
    				successAttacks.get(pl.getName()).clear();
    			}
    			
    			// Remove little success and failure images, inflict if necessary
    			for (Player pl : combatants) {
    				
    				// Change piece image of success or failure to not visible
    				for (Piece p : attackingPieces.get(pl.getName())) 
    					((Combatable)p).resetAttack();
    				
					// inflict return true if the piece is dead, and removes it from attackingPieces if so
    				// Inflict is also responsible for removing from the CreatureStack
    				for (Piece p : toInflict.get(pl.getName())) {
						if (((Combatable)p).inflict()) 
							attackingPieces.get(pl.getName()).remove(p);
						if (attackingPieces.get(pl.getName()).size() == 0)
							attackingPieces.remove(pl.getName());
    				}
    				
    				// Clear toInflict for next phase
    				toInflict.get(pl.getName()).clear();
    			}

				// Update the InfoPanel gui for changed/removed pieces
				Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
    					InfoPanel.showTileInfo(battleGround);
    					battleGround.coverPieces();
	                }
				});
    			
    			// Check for defeated armies:
				// - find player with no more pieces on terrain
				// - remove any such players from combatants
				// - if only one combatant, end combat
				baby = null;
    			while (combatants.size() != attackingPieces.size()) {
					for (Player pl : combatants) {
						if (!attackingPieces.containsKey(pl.getName())) {
							baby = pl;
						}
					}
					combatants.remove(baby);
    			}
    			if (combatants.size() == 1) {
    				exploring = (!battleGround.isExplored() && battleGround.getContents().size() == 1);
    				continue;
    			}
    			
				// Notify next phase, wait for a second
				Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	            		battleGround.coverPieces();
	                	GUI.getHelpText().setText("Attack phase: Next phase: Melee!");
	                }
	            });
				
				// Pause for 2 seconds between phases
				try { Thread.sleep(2000); } catch( Exception e ){ return; }
				

///////////////////////////// Melee phase
				for (final Player pl : combatants) {
    				
    				player = pl;
    				// Cover all pieces
    				Platform.runLater(new Runnable() {
    	                @Override
    	                public void run() {
    	            		battleGround.coverPieces();
    	                }
    	            });
    				
    				// For each piece, if its melee. Add it to the phaseThings array
    				for (Piece p : attackingPieces.get(pl.getName())) {
    					if (p instanceof Combatable && !(((Combatable)p).isRanged() || ((Combatable)p).isMagic()) && !(p instanceof Fort && ((Fort)p).getCombatValue() == 0)) 
    						phaseThings.add(p);
    				}
    				
    				// uncover melee pieces for clicking
    				if (phaseThings.size() > 0) {
	    				Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	    				for (Piece mel : phaseThings) 
	            					mel.uncover();
	    	                }
	    	            });
    				}
    				try { Thread.sleep(100); } catch( Exception e ){ return; }
    				
    				// Have user select a piece to attack with until there are no more melee pieces
    				while (phaseThings.size() > 0) {
    					
    					// Display message prompting user to select a melee piece
    					Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	GUI.getHelpText().setText("Attack phase: " + player.getName() + ", select a melee piece to attack with");
	    	                }
	    	            });

    					// Wait for user to select piece
        				pieceClicked = null;
        				ClickObserver.getInstance().setCreatureFlag("Combat: SelectPieceToAttackWith");
        				ClickObserver.getInstance().setFortFlag("Combat: SelectPieceToAttackWith");
    					while (pieceClicked == null) { try { Thread.sleep(100); } catch( Exception e ){ return; } }
	    				ClickObserver.getInstance().setCreatureFlag("");
	    				ClickObserver.getInstance().setFortFlag("");
	    				
	    				// Is it a charging piece?
	    				int charger;
	    				if (((Combatable)pieceClicked).isCharging()) {
	    					charger = 2;
	    				} else
	    					charger = 1;
	    				
	    				// do twice if piece is a charger
	    				for (int i = 0; i < charger; i++) {
	    					
		    				// hightlight piece that was selected, uncover the die to use, display message about rolling die
	    					Platform.runLater(new Runnable() {
		    	                @Override
		    	                public void run() {
		    	                	pieceClicked.highLight();
		    	                	DiceGUI.getInstance().uncover();
		    	                	GUI.getHelpText().setText("Attack phase: " + player.getName()
		    	                            + ", roll the die. You need a " + ((Combatable)pieceClicked).getCombatValue() + " or lower");
		    	                }
	    					});
	    					
		                	// Dice is set to -1 while it is 'rolling'. This waits for the roll to stop, ie not -1
	    					int attackStrength = -1;
	    					while (attackStrength == -1) {
	    						try { Thread.sleep(100); } catch( Exception e ){ return; }
	    						attackStrength = Dice.getFinalVal();
	    					}
							
	    					// If the roll was successful. Add to successfulThings Array, and change it image. prompt Failed attack
	    					if (attackStrength <= ((Combatable)pieceClicked).getCombatValue()) {
	    						
	    						successAttacks.get(player.getName()).add(pieceClicked);
	    						Platform.runLater(new Runnable() {
	    	    	                @Override
	    	    	                public void run() {
	    	    						((Combatable)pieceClicked).setAttackResult(true);
	    	    						pieceClicked.cover();
	    	    						pieceClicked.unhighLight();
	    	    	                	GUI.getHelpText().setText("Attack phase: Successful Attack!");
	    	    	                }
	        					});
	    						
	    					} else { // else failed attack, update image, prompt Failed attack
	    						Platform.runLater(new Runnable() {
			    	                @Override
			    	                public void run() {
			    						((Combatable)pieceClicked).setAttackResult(false);
			    						pieceClicked.cover();
	    	    						pieceClicked.unhighLight();
			    	                	GUI.getHelpText().setText("Attack phase: Failed Attack!");
			    	                }
		    					});
	    					}
	    					
	    					// If piece is charging, and it is its first attack, remove the cover again
	    					if (((Combatable)pieceClicked).isCharging() && i == 0) {
	    						Platform.runLater(new Runnable() {
			    	                @Override
			    	                public void run() {
			    	                	pieceClicked.uncover();
			    	                }
	    						});
	    					}
	    					
	    					// Pause to a second for easy game play, remove the clicked piece from phaseThings
	    					try { Thread.sleep(1000); } catch( Exception e ){ return; }
	    				}

    					phaseThings.remove(pieceClicked);
    				}
    			}

				// For each piece that had success, player who is being attacked must choose a piece
    			// Gets tricky here. Will be tough for Networking :(
    			for (Player pl : combatants) {
    				
    				// Active player is set to the player who 'pl' is attack based on toAttack HashMap
    				player = toAttacks.get(pl.getName());
    				final String plName = pl.getName();
    				
    				// For each piece of pl's that has a success (in successAttacks)
    				int i = 0;
    				for (final Piece p : successAttacks.get(plName)) {
    					
    					// If there are more successful attacks then pieces to attack, break after using enough attacks
    					if (i >= attackingPieces.get(player.getName()).size()) {
    						break;
    					}
    					
    					// Display message, cover other players pieces, uncover active players pieces
	    				Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	GUI.getHelpText().setText("Attack phase: " + player.getName() + ", Select a Piece to take a hit from " + plName + "'s " + p.getName());
	    	                	battleGround.coverPieces();
	    	                	battleGround.uncoverPieces(player.getName());
	    	                }
						});
    					
	    				// Cover pieces already choosen to be inflicted. Wait to make sure runLater covers pieces already selected
	    				for (final Piece pi : toInflict.get(player.getName())) {
	    					if (!((pi instanceof Fort) && ((Fort)pi).getCombatValue() > 0)) {
		    					Platform.runLater(new Runnable() {
			    	                @Override
			    	                public void run() {
				    					pi.cover();
			    	                }
								});
	    					}
	    				}//TODO here is where a pause might be needed
	    				
	    				// Wait for user to select piece
    					pieceClicked = null;
	    				ClickObserver.getInstance().setCreatureFlag("Combat: SelectPieceToGetHit");
	    				ClickObserver.getInstance().setFortFlag("Combat: SelectPieceToGetHit");
    					while (pieceClicked == null) { try { Thread.sleep(100); } catch( Exception e ){ return; } }
    					ClickObserver.getInstance().setCreatureFlag("");
    					ClickObserver.getInstance().setFortFlag("");
		    			
    					// Add to arrayList in HashMap of player to mark for future inflict. Cover pieces
    					toInflict.get(player.getName()).add(pieceClicked);
    					Platform.runLater(new Runnable() {
	    	                @Override
	    	                public void run() {
	    	                	battleGround.coverPieces();
	    	                }
						});
    					try { Thread.sleep(100); } catch( Exception e ){ return; }
	    				i++;
    				}
    				// Clear successful attacks for next phase
    				successAttacks.get(pl.getName()).clear();
    			}
    			
    			// Remove little success and failure images, inflict if necessary
    			for (Player pl : combatants) {
    				
    				// Change piece image of success or failure to not visible
    				for (Piece p : attackingPieces.get(pl.getName())) 
    					((Combatable)p).resetAttack();
    				
					// inflict return true if the piece is dead, and removes it from attackingPieces if so
    				// Inflict is also responsible for removing from the CreatureStack
    				for (Piece p : toInflict.get(pl.getName())) {
						if (((Combatable)p).inflict()) 
							attackingPieces.get(pl.getName()).remove(p);
						if (attackingPieces.get(pl.getName()).size() == 0)
							attackingPieces.remove(pl.getName());
    				}
    				
    				// Clear toInflict for next phase
    				toInflict.get(pl.getName()).clear();
    			}

				// Update the InfoPanel gui for changed/removed pieces
				Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
    					InfoPanel.showTileInfo(battleGround);
    					battleGround.coverPieces();
	                }
				});
    			
    			// Check for defeated armies:
				// - find player with no more pieces on terrain
				// - remove any such players from combatants
				// - if only one combatant, end combat
				baby = null;
    			while (combatants.size() != attackingPieces.size()) {
					for (Player pl : combatants) {
						if (!attackingPieces.containsKey(pl.getName())) {
							baby = pl;	
						}
					}
					combatants.remove(baby);
    			}
    			if (combatants.size() == 1) {
    				exploring = (!battleGround.isExplored() && battleGround.getContents().size() == 1);
    				continue;
    			}
    			
				// Notify next phase, wait for a second
				Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	            		battleGround.coverPieces();
	                	GUI.getHelpText().setText("Attack phase: Next phase: Retreat!");
	                }
	            });
				
				// Pause for 2 seconds between phases
				try { Thread.sleep(2000); } catch( Exception e ){ return; }
    			
    			
//////////////////////// Retreat phase
				// Can only retreat to a Terrain that has been explored and has no ememies on it
				
				// Display message, activate done button
				Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	GUI.getHelpText().setText("Attack phase: Retreat some of your armies?");
		                GUI.getDoneButton().setDisable(false);
	                }
	            }); 				
				
				// For each combatant, ask if they would like to retreat
		        for (Player pl : combatants) {
		        	
		        	// Make sure wildThings aren't trying to get away
		        	if (!pl.isWildThing()) {
			        	player = pl;
	        	        InfoPanel.uncover(player.getName());
				        ClickObserver.getInstance().setActivePlayer(player);
				        ClickObserver.getInstance().setCreatureFlag("Combat: SelectRetreaters");
				        
				        // Pause and wait for player to hit done button
				        pause();
			            Platform.runLater(new Runnable() {
			                @Override
			                public void run() {
			                	battleGround.coverPieces();
			                	battleGround.uncoverPieces(player.getName());
			                	battleGround.coverFort();
			        	        GUI.getHelpText().setText("Attack Phase: " + player.getName() + ", You can retreat your armies");
			                }
			            });
				        while (isPaused && battleGround.getContents(player.getName()) != null) {
			            	try { Thread.sleep(100); } catch( Exception e ){ return; }  
				        }	        
				        ClickObserver.getInstance().setTerrainFlag("Disabled");
				        
				        // TODO, maybe an if block here asking user if they would like to attack 
				        System.out.println(attackingPieces + "---BEFORE CLEARING");
				        // Re-populate attackingPieces to check for changes
				        attackingPieces.clear();
				        Iterator<String> keySetIterator2 = battleGround.getContents().keySet().iterator();
				    	while(keySetIterator2.hasNext()) {
				    		String key = keySetIterator2.next();
                            System.out.println(key + "=====key");
			    			attackingPieces.put(battleGround.getContents().get(key).getOwner().getName(), (ArrayList<Piece>) battleGround.getContents().get(key).getStack().clone()); 
				    	}
                        // System.out.println(attackingPieces);
				    	// if the owner of the terrain has no pieces, just a fort or city/village
                        System.out.println("===battleground"+battleGround);
                        System.out.println("===attackingPieces"+attackingPieces);
                        System.out.println(combatants.contains(battleGround.getOwner()) ? "TRUE" : "FALSE");
						if (combatants.contains(battleGround.getOwner()) && battleFort != null) {
                            System.out.println(battleGround + "===battleground");
							attackingPieces.put(battleGround.getOwner().getName(), new ArrayList<Piece>());
                            System.out.println(attackingPieces + "===attacking pieces");
						}
						if (battleFort != null) {
                            System.out.println(battleFort.getName() + "===battlefort");
                            System.out.println(battleFort.getOwner().getName() + "===battlefort's owner");
                            
							attackingPieces.get(battleFort.getOwner().getName()).add(battleFort);
                            System.out.println(attackingPieces.get(battleFort.getOwner().getName()));
						}
	//					if (battleGround city/village)
						// TODO city/village
				        
						
				        // Check if all the players pieces are now gone
				        if (!attackingPieces.containsKey(player.getName())) {
				        	
				        	// Display message, and remove player from combatants
				        	Platform.runLater(new Runnable() {
				                @Override
				                public void run() {
				        	        GUI.getHelpText().setText("Attack Phase: " + player.getName() + " has retreated all of their pieces!");
				                }
				            });
				        	
				        	// If there is only 1 player fighting for the hex, 
				        	if (attackingPieces.size() == 1) 
				        		break;
				        	
				        	// Pause because somebody just retreated
				        	try { Thread.sleep(2000); } catch( Exception e ){ return; }
				        }
				        

	        	        InfoPanel.cover(player.getName());
				        
			        }
		        }
		        
		        // Done button no longer needed
		        Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		                GUI.getDoneButton().setDisable(true);
		            }
		        });
		        ClickObserver.getInstance().setCreatureFlag("");
		        ClickObserver.getInstance().setFortFlag("");
		        
		        // Check for defeated armies:
				// - find player with no more pieces on terrain
				// - remove any such players from combatants
				// - if only one combatant, end combat
    			baby = null;
    			while (combatants.size() != attackingPieces.size()) {
					for (Player pl : combatants) {
						if (!attackingPieces.containsKey(pl.getName())) {
							baby = pl;
						}
					}
					combatants.remove(baby);
    			}
    			if (combatants.size() == 1) {
    				exploring = (!battleGround.isExplored() && battleGround.getContents().size() == 1);
    				continue;
    			}
    			

				exploring = (!battleGround.isExplored() && battleGround.getContents().size() == 1);
				
				// Add wildthings back to combatants if they were removed
				if (battleGround.getContents().containsKey(wildThings.getName()) && !combatants.contains(wildThings))
					combatants.add(wildThings);
				
    		} // end while (combatants.size() > 1 || exploring)
    		battleGround.coverFort();
    		
////////////////// Post Combat
    		
    		// sets player as the winner of the combat
    		// Can be null if battle takes place on a hex owned by nobody, and each player lost all pieces
    		Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	battleGround.removeBattleHex();
                }
    		});
    		
    		if (combatants.size() == 0)
    			player = battleGround.getOwner();
    		else if (combatants.size() == 1 && combatants.get(0).getName().equals(wildThings.getName()))
    			player = null;
    		else
    			player = combatants.get(0);
    		
    		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    		System.out.println("combatants: " + combatants);
    		for (Player p : combatants) {
        		System.out.println("p.getName(): "+ p.getName());
    			
    		}
    		System.out.println("owner: " + owner);
    		System.out.println("combatants.get(0): " + combatants.get(0));
    		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    		
    		
    		// Change ownership of hex to winner
    		boolean ownerChanged = false;
    		if (owner != null && combatants.size() > 0 && !battleGround.getOwner().equals(combatants.get(0))) {
    			battleGround.getOwner().removeHex(battleGround);
    			combatants.get(0).addHexOwned(battleGround);
    			ownerChanged = true;
    		}
			
    		// See if fort is kept or downgraded.
    		if (battleFort != null) {
                if (battleFort.getName().equals("Citadel")) {
                    owner.setCitadel(false);
                    player.setCitadel(true);
                    battleFort.healFort();
                    player.addHexOwned(battleGround);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GUI.getHelpText().setText("Post combat: " + player.getName() + ", you get to keep the fort!");
                        }
                    });
                    checkWinners();
                    return;
                }
    		
				// Get player to click dice to see if fort is kept
				Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	GUI.getHelpText().setText("Post combat: " + player.getName() + ", roll the die to see if the fort is kept or downgraded");
	                	DiceGUI.getInstance().uncover();
	                	InfoPanel.showTileInfo(battleGround);
	                }
	            });
				
				// Dice is set to -1 while it is 'rolling'. This waits for the roll to stop, ie not -1
				int oneOrSixGood = -1;
				while (oneOrSixGood == -1) {
					try { Thread.sleep(100); } catch( Exception e ){ return; }
					oneOrSixGood = Dice.getFinalVal();
				}
				
				// if a 1 or 6, keep fort (Keep it.... not turn it into a keep)
				if (oneOrSixGood == 1 || oneOrSixGood == 6) {
					battleFort.healFort();
					player.addHexOwned(battleGround);
					Platform.runLater(new Runnable() {
		                @Override
		                public void run() {
		                	GUI.getHelpText().setText("Post combat: " + player.getName() + ", you get to keep the fort!");
		                }
					});
				} else {
					battleFort.downgrade();Platform.runLater(new Runnable() {
		                @Override
		                public void run() {
		                	GUI.getHelpText().setText("Post combat: " + player.getName() + ", the fort was destroyed!");
		                }
					});
				}
				
				Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	InfoPanel.showTileInfo(battleGround);
	                }
	            });
				
				try { Thread.sleep(1000); } catch( Exception e ){ return; }
				
				
    		}

    		battleGround.flipPiecesDown();
			// TODO city/village and special incomes if they are kept or lost/damaged 
    		
    		try { Thread.sleep(1000); } catch( Exception e ){ return; }
    	}/// end Post combat

    	Platform.runLater(new Runnable() {
            @Override
            public void run() {
//				InfoPanel.showBattleStats();
            	Board.removeCovers();
            }
		});
    	
		ClickObserver.getInstance().setTerrainFlag("");
		ClickObserver.getInstance().setPlayerFlag("");
		ClickObserver.getInstance().setCreatureFlag("");
		ClickObserver.getInstance().setFortFlag("");
		battleGrounds.clear();
    }

    /*
     * Optional.
     * Each player may build forts.
     */
    private void constructionPhase() {
        for( final Player p : playerList ) {
            this.player = p;
            player.flipAllUp();
            ClickObserver.getInstance().setActivePlayer(this.player);
            pause();

            ClickObserver.getInstance().setTerrainFlag("");
            ClickObserver.getInstance().setClickedTerrain(player.getHexesOwned().get(0));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Board.applyCovers();
                    GUI.getRackGui().setOwner(player);
                    GUI.getHelpText().setText("Construction Phase: " + p.getName() 
                            + ", select one of your tiles to build a new tower, or upgrade an existing one.");
                    ClickObserver.getInstance().whenTerrainClicked();
                }
            });
			try { Thread.sleep(500); } catch( Exception e ){ return; }
            ClickObserver.getInstance().setTerrainFlag("Construction: ConstructFort");
            ArrayList<Terrain> ownedHexes = player.getHexesOwned();
            for (final Terrain t : ownedHexes) {
                if (t.getOwner().getName().equals(player.getName())) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            t.uncover();
                        }
                    });
                }
            }
            while( isPaused ){
                try { Thread.sleep(100); } catch( Exception e ){ return; }
            }
            player.flipAllDown();
        }
        ClickObserver.getInstance().setTerrainFlag("");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Board.removeCovers();
            }
        });
    }

    /*
     * Optional.
     * Master Thief and Assassin Primus may use their special powers.
     */
    private void specialPowersPhase() {
    	SpecialCharacter tmpPiece = null;
    	Terrain theHex = null;

        for (Player p : playerList) {
            pause();
            this.player = p;
            player.flipAllUp();
            ClickObserver.getInstance().setActivePlayer(this.player);
            
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Game.getRackGui().setOwner(player);
                }
            });
            /*
             * Loops through all of the hexes that the player has pieces on.
             * If one of the pieces is either Master Thief or Assassin Primus, perform their
             * special ability.
             */
            for (Terrain hex : player.getHexesWithPiece()) {
                for (Piece pc : hex.getContents(player.getName()).getStack()) {
                    if (pc.getName().equals("Master Thief") || pc.getName().equals("Assassin Primus")) {                    
                        ((Performable)pc).specialAbility();
                        if (MasterThief.isReturnPiece() || AssassinPrimus.isReturnPiece()) {
                        	tmpPiece = (SpecialCharacter)pc;
                        	theHex = hex;
                        	tmpPiece.returnToBank(theHex);
                        	break;
                        }
                    }
                }
            }
            player.flipAllDown();
        }
        ClickObserver.getInstance().setPlayerFlag("");

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                DiceGUI.getInstance().setFaceValue(0);
                DiceGUI.getInstance().cover();
            }
        });
    }

    /* 
     * Mandatory
     * This happens last. The player order gets shifted by 1, i.e. 1st->4th, 2nd->1st, etc.
     */
    private void changeOrderPhase() {
        for (int i = 0; i < 4; i++)
            System.out.print(playerList[i].getName() + " ,");
        Player tmp = playerList[0];
        playerList[0] = playerList[1];
        playerList[1] = playerList[2];
        playerList[2] = playerList[3];
        playerList[3] = tmp;
        System.out.println();
        for (int i = 0; i < 4; i++)
            System.out.print(playerList[i].getName() + " ,");
        System.out.println();
    }

    private boolean checkWinners() {
        System.out.println("CHECKING IF ANYONE HAS WON THE GAME");
        victorList.clear();
        for (Player p : playerList) {
            if (p.hasaCitadel())
                victorList.add(p);
        }
        if (victorList.size() == 1) {
            System.out.println("SOMEONE HAS WON");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Game.getHelpText().setText("CONGRATULATIONS " + victorList.get(0).getName() + "! YOU HAVE SUCCESSFULLY BUILT A CITADEL AND HAVE SUCCEEDED IN WINNING THIS GAME OF KINGS AND THINGS!");
                }
            });
            try { Thread.sleep(3000); } catch(Exception e) { e.printStackTrace(); }
            phaseNumber = 10;
            System.out.println("SETTING THE PHASE NUMBER TO 10---" + phaseNumber);
            return true;
        }
        else if (victorList.size() > 1) {
            String tmp = "";
            for (Player p : victorList)
                tmp = tmp + p.getName() + ",";
            final String winners = tmp;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Game.getHelpText().setText("WOW! " + winners + " have all built a citadel this turn! Looks like this needs to be settled by conquest!");
                }
            });
            try { Thread.sleep(3000); } catch(Exception e) { e.printStackTrace(); }
            return false;
        }
        else
            return false;
    }

    private void endOfGamePhase() {
        System.out.println("END OF GAME PHASE");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Board.applyCovers();
                // Game.getHelpText().setText("Would you like to play again?");
                // Game.getPlayAgainButton().show();
                // Game.getPlayAgainButton().activate();
            }
        });
        
        pause();
        if (playAgainClicked) {
            phaseNumber = -1;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Board.clearBoardGUI();
                    TileDeck td = new TileDeck(Game.getRoot());
                    setPlayers(Game.getPlayers());
                    initGame(Game.getUniqueInstance());
                    setupPhase();
                }
            });            
        }
        while (isPaused) {
            try { Thread.sleep(100); } catch(Exception e) { return; }
        }
    }

    /*
     * Main loop of the game. Uses a phase number to determine which phase the game should be in.
     */
    public void playGame() {
        switch (phaseNumber) {
            case -2:System.out.println(phaseNumber + " loading phase number");
                    loadingPhase();
                    System.out.println("Actually done loading");
                    phaseNumber = 1;
                    System.out.println(phaseNumber + " after loading");
                    break;
            case 0: System.out.println(phaseNumber + " setup phase");
                    setupPhase();
                    doneClicked = false;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GUI.getDoneButton().setDisable(true);
                        }
                    });
                    phaseNumber++;
                    break;
            case 1: System.out.println(phaseNumber + " gold phase");
                    goldPhase();
                    doneClicked = false;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GUI.getDoneButton().setDisable(true);
                        }
                    });
                    phaseNumber++;
                    break;
            case 2: System.out.println(phaseNumber + " recruit specials phase");
                    recruitSpecialsPhase();
                    doneClicked = false;
                    phaseNumber++;
                    break;
            case 3: System.out.println(phaseNumber + " recruit things phase");
                    doneClicked = false;
                    recruitThingsPhase();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GUI.getDoneButton().setDisable(false);
                        }
                    });
                    phaseNumber++;
                    break;
            case 4: System.out.println(phaseNumber + " random event phase");
                    randomEventPhase();
                    doneClicked = false;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GUI.getDoneButton().setDisable(false);
                        }
                    });
                    phaseNumber++;
                    break;
            case 5: System.out.println(phaseNumber + " movement phase");
                    movementPhase();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GUI.getDoneButton().setDisable(true);
                        }
                    });
                    phaseNumber++;
                    break;
            case 6: System.out.println(phaseNumber + " combat phase");
                    combatPhase();
                    phaseNumber++;
                    break;
            case 7: System.out.println(phaseNumber + " construction phase");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GUI.getDoneButton().setDisable(false);
                        }
                    });
                    constructionPhase();
                    if (!checkWinners())
                        phaseNumber++;
                    break;
            case 8: System.out.println(phaseNumber + " special powers phase");
                    specialPowersPhase();
                    phaseNumber++;
                    break;
            case 9: System.out.println(phaseNumber + " change order phase");
                    changeOrderPhase();
                    phaseNumber = 1;
                    break;
            case 10:System.out.println(phaseNumber + " detected a winner");
                    endOfGamePhase();
                    break;
        }
    }
    
    public void pause(){
        isPaused = true;
    }

    public void unPause(){
        isPaused = false;
    }

    void setButtonHandlers(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	GUI.getDoneButton().setOnAction(new EventHandler(){
            		@Override
    				public void handle(Event event) {
                    	doneClicked = true;
	                    if( phaseNumber == 3 ) {
	                        freeClicked = false;
	                        paidClicked = false;
	                        doneClicked = true;
	                        unPause();
	                    }
                		unPause();
                	}
            	});

                Game.getPlayAgainButton().setOnAction(new EventHandler() {
                    @Override
                    public void handle(Event e) {
                        playAgainClicked = true;
                        unPause();
                    }
                });
            }
        });
        
    }

    public void stop(){
        unPause();
    }

    public void setFree(boolean b) { freeClicked = b; }
    public void setPaid(boolean b) { paidClicked = b; }
    public void setStartingPos(Coord[] c) { startingPos = c; }
    public Coord[] getStartingPos() { return startingPos; }
    public int getPhase() { return phaseNumber; }
    public int getNumPlayers() { return numPlayers; }
    public Player[] getPlayers() { return playerList; }
    public Player getPlayer(){ return this.player; }
    public ArrayList<Coord> getBattleGrounds() { return battleGrounds; }
    public boolean getPaused() { return isPaused; }
    public ArrayList<Player> getVictorList() { return victorList; }
    public Player getWildThings() { return wildThings; }
    
    public void setLocalPlayer(String s) { localPlayer = s; }
    public void setPhase(int i) { phaseNumber = i; }
    public void setPlayerClicked(Player p) { playerClicked = p; }
    public void setPieceClicked(Piece p) { pieceClicked = p; }
    public void setRandomEvent(Piece p) { 
        randomEventFlag = true;
        randomEvent = p;
    }
    
    public void setSyncronizer(boolean b) { syncronizer = b; }
    public boolean getSyncronizer() { return syncronizer; }
    public boolean isNetworked(){ return networked; }
    
}

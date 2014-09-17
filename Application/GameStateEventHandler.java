package KAT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Platform;

public class GameStateEventHandler implements EventHandler
{
	@SuppressWarnings("unchecked")
	public boolean handleEvent( Event event ){
        boolean error = false;
        int gameSize = (Integer)event.get("gameSize");
        int numPlayers = (Integer)event.get("numPlayers");
        Player[] players = NetworkGameLoop.getInstance().getPlayers();
        Player localPlayer = NetworkGameLoop.getInstance().getPlayer();
        Player currPlayerTurn = NetworkGameLoop.getInstance().getPlayerTurn();
        Player nextPlayerTurn = null;

        if( numPlayers < 1 ){
            System.err.println("Error: 0 players");
            return error = true;
        }

        // update player list
        for( int i=0; i<numPlayers; i++ ){
            HashMap<String,Object> playerInfo 
                = (HashMap<String,Object>)event.get("Player"+(i+1));
            String username = (String)playerInfo.get("username");
            String color = (String)playerInfo.get("color");
            Player player = new Player(username, color);
            player.setGold((Integer)playerInfo.get("gold"));
            NetworkGameLoop.getInstance().addPlayer(player);
            if( i == 0 ){
                nextPlayerTurn = player;
                NetworkGameLoop.getInstance().setPlayerTurn(player);
                System.out.println("next turn: "+nextPlayerTurn.getName());
            }
            Integer numOnRack = (Integer)playerInfo.get("numOnRack");
            if( numOnRack != null ){
            	if( NetworkGameLoop.getInstance().getPhase() > 0 ){
            		PlayerBoard.getInstance().updateNumOnRack(player, numOnRack);
            	}
            }
        }

        // only update the game if players have changed turns
        if( nextPlayerTurn == null 
        ||  nextPlayerTurn.getName() == null ){
            System.err.println("error, nextPlayerTurn not found");
            return !(error = true);
        }
        if( nextPlayerTurn.getName().equals(currPlayerTurn.getName()) ){
            return !(error = false);
        }
        
        // update the contents of the cup
        ArrayList<Integer> cupData = (ArrayList<Integer>)event.get("cupData");
        ArrayList<Piece> newCup = new ArrayList<Piece>();

        for( Integer pID : cupData ){
            HashMap<String,Object> map = (HashMap<String,Object>)event.get(""+pID);
            String type = (String)map.get("type");
            if( type.equals("Creature") ){
                newCup.add(new Creature(map));
            } else if( type.equals("SpecialCharacter") ){
                // TODO use SpecialCharacter Factory
                newCup.add(new SpecialCharacter(map));
            } else if( type.equals("Random Event") ){
                // TODO use RandomEventFactory to init
            } else if( type.equals("Magic Event") ){
                // TODO add MagicEventFactory
            } else if( type.equals("Special Income") ){
                // TODO use SpecialIncomeFactory
                newCup.add(new SpecialIncome(map));
            } else {
                System.err.println("error: type not recognized: "+type);
                return error = true;
            }
        }

        TheCup.getInstance().setRemaining(newCup);
        
        /*
        // is more efficient to just erase and reconstruct the cup
        for( Integer pID : cupData ){
        	if( !theCup.containsPiece(pID) ){
        		// data contains a piece not in the cup, add the new one
        		HashMap<String,Object> map = (HashMap<String,Object>)event.get(""+pID);
        		String type = (String)map.get("type");
        		if( type.equals("Creature") ){
        			theCup.addToCup(new Creature(map));
        		} else if( type.equals("SpecialCharacter") ){
        			// TODO add cases for necessary special character. 
                    // This will be tedious...
        			theCup.addToCup(new SpecialCharacter(map));
        		} else if( type.equals("Random Event") ){
        			// TODO add cases for random events
        		} else if( type.equals("Magic Event") ){
        			// TODO add cases for each magic event
        		} else if( type.equals("Special Income") ){
        			theCup.addToCup(new SpecialIncome(map));
        		} else {
        			System.err.println("error: type not recognized: "+type);
        		}
        	} 
        }
        
        // check for any pieces to be removed from the cup 
        // (would like a more efficient way of doing this)
        ArrayList<Piece> theCupPieces = theCup.getRemaining();
        ArrayList<Integer> theCupPIDS = new ArrayList<Integer>();
        for( Piece p : theCupPieces ){
        	theCupPIDS.add(p.getPID());
        }
        for( Integer pID : theCupPIDS ){
        	if( !cupData.contains(pID) ){
        		theCup.removePiece(pID);
        	}
        }
        */
        
        // check for changes in the game board
        // for now just replacing the current one
        ArrayList<HashMap<String,Object>> newBoard 
            = (ArrayList<HashMap<String,Object>>)event.get("board");
        ArrayList<Terrain> tiles = new ArrayList<Terrain>();
        for( HashMap<String,Object> tile : newBoard ){        	
            tiles.add(new Terrain(tile)); // some GUI stuff might not be updating here
        }
        HashMap<Coord,Terrain> terrains = new HashMap<Coord,Terrain>();
        for( Terrain t : tiles ){
        	terrains.put(t.getCoords(), t);
        }
        // set the old game board to the new one
        final HashMap<Coord,Terrain> temp = terrains;
        if( !TileDeck.getInstance().isIn() ){
	        Platform.runLater(new Runnable(){
	            @Override
	            public void run(){
	                Board.setTerrains(temp); // does not show up in GUI :(
	            }
	        });
        }
//        // print the new game board for now to see the new changes in the command line
//        System.out.println(terrains);
        
        /*
        HashMap<Coord,Terrain> curBoard = Board.getTerrains();
        for( Terrain newT : tiles ){
        	Coord coords = newT.getCoords();
        	Terrain curT = curBoard.get(coords);
        	HashMap<String,CreatureStack> contents = curT.getContents();
        	// check if a player has left a hex
        	Terrain leftHex = null;
        	if( contents.keySet().size() > newT.getContents().keySet().size() ){
        		for( String s : newT.getContents().keySet() ){
        			if( !contents.keySet().contains(s) ){
        				curT.addToStack(player, c, secretly)
        			}
        		}
        	}
        	// check if a player has added a new hex
        	
        }
         */
        // check for any pieces not in player rack
        // TODO
        
        // check for any offside special characters
        // TODO

        // lastly, check if this user is up next for their turn
        if( localPlayer.getName().equals(nextPlayerTurn.getName()) ){
            // if they are, unPause and let the GameLoop continue
            NetworkGameLoop.getInstance().unPause();
            NetworkGameLoop.getInstance().getPlayer().flipAllUp();
        } else {
        	ClickObserver.getInstance().setTerrainFlag("Disabled");
        	ClickObserver.getInstance().setCreatureFlag("Disabled");
        	ClickObserver.getInstance().setPlayerFlag("Disabled");
        	ClickObserver.getInstance().setPlayerFlag("Disabled");
        	ClickObserver.getInstance().setFortFlag("Disabled");
        }

        return !error;
    }
}


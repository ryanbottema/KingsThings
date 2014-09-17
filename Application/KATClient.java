package KAT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class KATClient extends Client
{
    public KATClient( String host, int port ){
        super(host, port);
        registerHandler("LOGINSUCCESS", new LoginSuccessEventHandler());
        registerHandler("GAMESTATE", new GameStateEventHandler());
    }
    
    public void postLogin( String username, int gameSize ){
        Message m = new Message("LOGIN", username);
        m.getBody().put("username", username);
        m.getBody().put("gameSize", gameSize);

        try {
            this.oos.writeObject(m);
            this.oos.flush();
        } catch( IOException e ){
            e.printStackTrace();
        } catch( NullPointerException e ){
            e.printStackTrace();
        }
    }

    public void getGameState( String username ){
        Message m = new Message("GETGAMESTATE", username);
        m.getBody().put("username", username);

        try {
            this.oos.writeObject(m);
            this.oos.flush();
        } catch( IOException e ){
            e.printStackTrace();
        } catch( NullPointerException e ){
            e.printStackTrace();
        }
    }

    /**
     * Send the game state to the server
     * @param details should add details of any changes (ex removed pieces from cup) 
     * here for server-side efficiency. Otherwise, pass this parameter as null
     */
    public void postGameState( HashMap<String,Object> details ){
    	Player player = NetworkGameLoop.getInstance().getPlayer();
        Message m = new Message("POSTGAMESTATE", player.getName());
        m.getBody().put("username", player.getName());
        m.getBody().put("phaseNumber", NetworkGameLoop.getInstance().getPhase());
        if( player.getPlayerRack() != null ){
        	m.getBody().put("playerRack", player.getPlayerRack().toMap());
        }
        
        if( details != null ){
        	// add the details of changes made
        	String updateType = (String)details.get("updateType");
        	if( updateType == null ){
        		updateType = "";
        	}
        	m.getBody().put("updateType", updateType);
        	for( String s : details.keySet() ){
        		Object o = details.get(s);
        		m.getBody().put(s, o);
        	}
        	try {
                this.oos.writeObject(m);
                this.oos.flush();
            } catch( IOException e ){
                e.printStackTrace();
            } catch( NullPointerException e ){
                e.printStackTrace();
            }
        	return;
        }
        
        // add contents of the cup
        ArrayList<Piece> theCup = TheCup.getInstance().getRemaining();
        ArrayList<Integer> pIDs = new ArrayList<Integer>();
        for( Piece p : theCup ){
        	pIDs.add(p.getPID());
        	HashMap<String,Object> piece = p.toMap();
        	m.getBody().put(""+p.getPID(), piece);
        	
        }
        m.getBody().put("pIDs", pIDs);

        // add game tiles
        ArrayList<HashMap<String,Object>> board 
            = new ArrayList<HashMap<String,Object>>();
        HashMap<Coord,Terrain> tiles = Board.getTerrains();
        for( Terrain t : tiles.values() ){
            board.add(t.toMap()); 
        }
        m.getBody().put("board", board);
        
        // add special characters
        // TODO 

        try {
            this.oos.writeObject(m);
            this.oos.flush();
        } catch( IOException e ){
            e.printStackTrace();
        } catch( NullPointerException e ){
            e.printStackTrace();
        }
    }
}

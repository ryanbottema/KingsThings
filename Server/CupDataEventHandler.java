package KAT;

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class CupDataEventHandler implements EventHandler
{
    @SuppressWarnings("unchecked")
	public boolean handleEvent( Event event ){
    	try { 
	        String username = (String)event.get("username");
	        int uID = KATDB.getUID(username);
	        int gID = KATDB.getGID(uID);
	        
	        ArrayList<Integer> pIDs = (ArrayList<Integer>)event.get("cupData");
	        for( Integer pID : pIDs ){
	        	HashMap<String,Object> piece = (HashMap<String,Object>)event.get(""+pID);
	        	KATDB.addPiece(gID, piece);
	        }
	        System.out.println("CupData added for game: "+gID);
	        
	        // also get initial game board setup 
	        ArrayList<HashMap<String,Object>> board = (ArrayList<HashMap<String,Object>>)event.get("board");
	        for( HashMap<String,Object> tile : board ){
	        	KATDB.addTile(gID, tile);
	        }      
	        System.out.println("GameBoard added for game: "+gID);
	        
	        KATDB.commit();
    	} catch( NullPointerException e ){
    		e.printStackTrace();
    		return false;
    	}
        return true;
    }
}

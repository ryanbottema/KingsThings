package KAT;

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.sql.*;

public class LoginEventHandler implements EventHandler
{
    public boolean handleEvent( Event event ){
        String username = (String)event.getMap().get("username");
        
        // create verification message to send back to client
        Message m = new Message("LOGINSUCCESS", "SERVER");
        ObjectOutputStream oos = (ObjectOutputStream)event.getMap().get("OUTSTREAM");

        Connection db;
        Statement stmnt;

        try {
            // open db connection
            //Class.forName("org.sqlite.JDBC");
            //db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
        	db = KATDB.getConnection();
            stmnt = db.createStatement();

            // create query string
            String query = "select count(*) from users where username = '"+username+"';";
            
            // validate login with db
            ResultSet rs = stmnt.executeQuery(query);
            rs.next();
            
            int count = rs.getInt("count(*)");
            rs.close();
            stmnt.close();
    
            // user is not in the database
            if( count == 0 ){
                // create  new tuple
             	stmnt = db.createStatement();
                query = "insert into users (username) values ('"+username+"');";
                stmnt.executeUpdate(query);
                stmnt.close();
            }

            System.out.println("User '"+username+"' appeared in table "+count+" times");            
            
            // check if the user is currently playing a game
            int uID = KATDB.getUID(username);
            stmnt = db.createStatement();
            query = "select count(*) from players where uID = '"+uID+"';";
            rs = stmnt.executeQuery(query);
            count = -1;
            count = rs.getInt("count(*)");
            rs.close();
            stmnt.close();
            
            boolean needsCupData = false;
            
            if( count == 0 ){
            	// not in a game, join a new one
            	// joinGame returns false if there was none to join
            	// and a new one needed to be created
            	needsCupData = !KATDB.joinGame(uID, event.getMap()); 
            	
            	if( needsCupData ){
            		KATDB.createGame(uID, event.getMap());
            	}
            } else {
            	m.getBody().put("needsCupData", false);
            	int gID = KATDB.getGID(uID);
            	m.getBody().put("phaseNum", KATDB.getPhaseNumber(gID));
            }
            m.getBody().put("needsCupData", needsCupData);
        } catch( Exception e ){
        	e.printStackTrace();
        }

        try {
            oos.writeObject(m);
            oos.flush();
        } catch( IOException e ){
            e.printStackTrace();
            return false;
        }

        return true;
    }
}

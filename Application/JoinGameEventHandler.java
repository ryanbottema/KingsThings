package KAT;

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.sql.*;

public class JoinGameEventHandler implements EventHandler
{
    public boolean handleEvent( Event event ){
        try {
            String username = (String)event.getMap().get("username"); 
            System.out.println(username+" just joined the game");
        } catch( NullPointerException e ){
            System.out.println("message body does not contain 'username'");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

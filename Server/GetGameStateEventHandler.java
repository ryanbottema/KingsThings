package KAT;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class GetGameStateEventHandler implements EventHandler 
{
	@Override
	public boolean handleEvent( Event event )throws IOException {
		ObjectOutputStream oos = (ObjectOutputStream)event.get("OUTSTREAM");
		int uID = KATDB.getUID((String)event.get("username"));
		int gID = KATDB.getGID(uID);
		
		Message m = new Message("GAMESTATE", "SERVER");
		
		// add the game 
		KATDB.getGameState(m.getBody().getMap(), gID, uID);
		oos.writeObject(m);
		oos.flush();
		
		return true;
	}
}

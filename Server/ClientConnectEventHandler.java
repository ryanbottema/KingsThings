package KAT;

import java.io.IOException;

public class ClientConnectEventHandler implements EventHandler {

	@Override
	public boolean handleEvent(Event event) throws IOException {
		System.out.println("Client connected!");
		return true;
	}
}

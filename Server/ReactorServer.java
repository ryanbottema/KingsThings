package KAT;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.sql.*;

/**
 * An asynchronous, multithreaded server using the Reactor pattern
 */
public class ReactorServer implements EventHandler 
{
    // a map of event handlers with their type as a key
    private HashMap<String,EventHandler> eventHandlers;
    private boolean running;
    private int port;

    ReactorServer( int port ){
        this.eventHandlers = new HashMap<String,EventHandler>();
        this.port = port;
        this.running = false;

        init();
        KATDB.setup();
    }

    public void init(){
    	registerHandler("CONNECT", new ClientConnectEventHandler());
        registerHandler("LOGIN", new LoginEventHandler());
        registerHandler("CUPDATA", new CupDataEventHandler());
        registerHandler("GETGAMESTATE", new GetGameStateEventHandler());
        registerHandler("POSTGAMESTATE", new PostGameStateEventHandler());
    }

    public void stop(){
        running = false;
    }

    public void start(){
        ServerSocket ss = null;
        running = true;

        try {
            ss = new ServerSocket(port);
            System.out.println("\nListening on port: " + port);
            System.out.println();
        } catch (IOException e) {
            stop();
            e.printStackTrace();
        }

        while( running ){
            try {
                final Socket s = ss.accept();

                new Thread(new Runnable(){
                    public void run(){
                        try {
                            service(s);
                        } catch( Exception e ){
                            e.printStackTrace();
                        } finally {
                            try {
                                if( s != null ){
                                    s.close();
                                }
                            } catch( IOException e ){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start(); // execute a new thread for each client
            } catch( Exception e ){
                stop();
                e.printStackTrace();
            }
        }
        try {
            if( ss != null ){
                ss.close();
            }
        } catch( IOException e ){
            e.printStackTrace();
        }
    }

    private void service( final Socket s )throws IOException, ClassNotFoundException {
        boolean connected = true;

        // estabalish IO streams
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        // run loop: listen for messages from client
        while( running && connected ){
        	Message m;
        	try { 
        		m = (Message)ois.readObject();
        		System.out.println("Received Message: " + m);
        	} catch( Exception e ){
        		return;
        	}

            // create a new event
            String type = m.getHeader().getType();
            Event event = new Event(type, m.getBody().getMap());
            event.put("OUTSTREAM", oos);
            
            // dispatch the event to an EventHandler
            boolean error = !handleEvent(event);

            if( error ){
                // should maybe notify client there was an error processing the message
                System.err.println("Error: handling event: "+event);
            }
        }
    }

    public void registerHandler( String type, EventHandler handler ){
        eventHandlers.put(type, handler);
    }

    public void deregisterHandler( String type ){
        eventHandlers.remove(type);
    }

    /**
     * Dispatches an Event to an EventHandler.
     * @return true if successfully handled, false otherwise
     */
    @Override
    public boolean handleEvent( Event event ){
        EventHandler handler = eventHandlers.get(event.getType());
        if( handler != null ){
            try {
                return handler.handleEvent(event);
            } catch( IOException e ){
            	e.printStackTrace();
                return false;
            }
        } else {
        	System.err.println("Error: no handler registered for type: "+event.getType());
            return false;
        }
    }
    
    /**
     * @param args pass args[0] as the port, default is 8888
     */
    public static void main( String args[] ){

        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 60006;

        System.out.println("\nStarting Server: Kings&Things...");

        new ReactorServer(port).start();
    }
}

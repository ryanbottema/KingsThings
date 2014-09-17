//
// Client.java
// kingsandthings/Application/
// @author Brandon Schurman
//
package KAT;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import javafx.stage.Stage;

public class Client implements EventHandler
{
    protected HashMap<String,EventHandler> eventHandlers;
    protected ObjectOutputStream oos;
    protected ObjectInputStream ois;
    private Thread netThread;
    private boolean running;
    private String host;
    private int port;

    public Client( String host, int port ){
        this.eventHandlers = new HashMap<String,EventHandler>();
        this.host = host;
        this.port = port;
        this.running = false;
    }

    /**
     * Connect to server
     * @return false if there was a problem connecting
     */
    public boolean connect(){
    	boolean error = false;
        try { 
            final Socket s = new Socket(host, port);
            this.oos = new ObjectOutputStream(s.getOutputStream());
            this.ois = new ObjectInputStream(s.getInputStream());
            
            this.netThread = new Thread(new Runnable(){
                public void run(){
                    try {
                        running = true;
                        service(s);          
                    } catch( EOFException e ){
                    	running = false;
                    	e.printStackTrace();
                    } catch( SocketException e ){
                        running = false;
                    } catch( IOException e ){
                        e.printStackTrace();
                    } catch( ClassNotFoundException e ){
                        e.printStackTrace();
                    } finally {
                        running = false;
                        try {
                            if( s != null ){
                                s.close();
                            }
                        } catch( IOException e ){
                            e.printStackTrace();
                        }
                        NetworkGameLoop.getInstance().stop();
                    }
                }
            }); 
            netThread.start(); // execute in a background thread
        } catch( ConnectException e ){
        	error = true;
        } catch( Exception e ){
            e.printStackTrace();
            error = true;
        }
        return !error;
    }

    @SuppressWarnings("deprecation")
    public void disconnect(){
        running = false;
        try {
            ois.close(); 
        } catch( IOException e ){
            e.printStackTrace();
        } catch( NullPointerException e ){
        	;;
        } catch( Exception e ){
        	e.printStackTrace();
        }
        if( netThread != null ){
	        netThread.interrupt();
	        if( netThread.isAlive() ){
	            netThread.destroy();
	        }
        }
    }

    public void service( final Socket s )
        throws IOException, ClassNotFoundException, EOFException, SocketException {

        Message m = new Message("CONNECT", "CLIENT");
        oos.writeObject(m);
        oos.flush();
        
        while( running ){
            m = (Message)ois.readObject();
            //System.out.println("Received Message: " + m);

            // create and dispatch a new event
            String type = m.getHeader().getType();
            Event event = new Event(type, m.getBody().getMap());
            event.put("OUTSTREAM", oos);

            // dispatch the event to an event handler
            boolean error = !handleEvent(event);

            if( error ){
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
                System.err.println("Error: no handler registered for type: "
                        + event.getType());
                return false;
            }
        } else {
            return false;
        }
    }
    
    public boolean isConnected(){
    	return this.running;
    }

    public static void main( String args[] ){
        new Client("localhost", 8888).connect();
    }
}

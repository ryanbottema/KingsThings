package KAT;

import java.io.Serializable;

public class Message implements Serializable 
{
    // serializable for networking
    private static final long serialVersionUID = 2048077991813912135L;
    private Header h;
    private Body b;

    Message( String type, String sender ){
        h = new Header(type, sender);
        b = new Body();
    }

    public Header getHeader(){
        return h;
    }

    public Body getBody(){
        return b;
    }

    @Override
    public String toString(){
        return h.toString()+":"+b.toString();
    }
}

package KAT;

import java.io.Serializable;

public class Header implements Serializable 
{
    private static final long serialVersionUID = 5476715863008481311L;
    private String sender;
    private String type;

    Header( String type, String sender ){
        this.type = type;
        this.sender = sender;
    }

    public String getSender(){
        return sender;
    }

    public String getType(){
        return type;
    }

    public void setSender( String sender ){
        this.sender = sender;
    }

    public void setType( String type ){
        this.type = type;
    }

    @Override
    public String toString(){
        return type+":"+sender;
    }
}

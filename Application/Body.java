package KAT;

import java.io.Serializable;
import java.util.HashMap;

public class Body implements Serializable 
{
    private static final long serialVersionUID = -7191344859306833453L;
    private HashMap<String,Object> map;

    Body(){
        map = new HashMap<String, Object>();
    }

    public Object get( String key ){
        return map.get(key);
    }

    public void put( String key, Object value ){
        map.put(key, value);
    }

    public HashMap<String,Object> getMap(){
        return map;
    }

    @Override
    public String toString(){
        return map.toString();
    }
}

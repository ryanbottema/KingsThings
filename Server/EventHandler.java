package KAT;

import java.io.IOException;

public interface EventHandler 
{
    public boolean handleEvent( Event event )throws IOException;
}

package application;

import java.io.IOException;

public interface EventHandler 
{
    public boolean handleEvent( Event event )throws IOException;
}

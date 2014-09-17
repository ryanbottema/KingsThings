package KAT;

/*
 * Public interface used to implement the Observer pattern. All classes that want to receive
 * data  from other classes (class which implement the subject interface) should implement this interface.
 */
public interface Observer {
	public void update();
}
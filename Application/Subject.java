package KAT;

/*
 * Public interface used to implement the Observer pattern. All classes that want to broadcast information
 * to another class should implement this interface.
 */
public interface Subject {
	public void registerObserver(Observer o);
	public void removeObserver(Observer o);
	public void notifyObservers();
}
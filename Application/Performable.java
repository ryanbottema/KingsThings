package KAT;

/*
 * Public Interface that all Special Characters with special abilities must implement.
 */
public interface Performable {
	public void performAbility(); //This ability is used when the character is played.
	public void specialAbility(); //This ability is used in the special ability phase.
	public boolean hasSpecial();
	public boolean hasPerform();
}
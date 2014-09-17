package KAT;
//
// Combatable.java
// kingsandthings/
// @author Brandon Schurman
//

public interface Combatable 
{
    public boolean inflict();
    public int getCombatValue();
    public boolean isMagic();
    public boolean isRanged();
    public boolean isCharging();
    public boolean isFlying();
    public void setAttackResult(boolean b);
    public void resetAttack();
}

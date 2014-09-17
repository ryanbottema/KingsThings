package KAT;

public class DwarfKing extends SpecialCharacter implements Performable {

	public DwarfKing() {
		super("Images/Hero_DwarfKing.png", "Images/Creature_Back.png", "Dwarf King", "", 5, false, false, false, false);
		setType("Special Character");
	}

	/*
	 * Doubles all income from mines.
	 */
	public void performAbility() {
		if (inPlay) {
			for (Terrain t : owner.getHexesWithPiece()) {
				for (Piece p : t.getContents(owner.getName()).getStack()) {
					if (p.getType().equals("Special Income")) {
						if (p.getName().contains("Mine"))
							((SpecialIncome)p).setValue(((SpecialIncome)p).getValue() * 2);
					}
				}
			}
		}
		
		PlayerBoard.getInstance().updateGoldIncomePerTurn(owner);
	}

	public void specialAbility() { return; }

	public boolean hasSpecial() { return false; }
	public boolean hasPerform() { return true; }
}
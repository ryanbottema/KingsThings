package KAT;

import java.util.HashMap;

public class PieceFactory 
{
	public static Piece createPiece( HashMap<String,Object> map ){
		String type = (String)map.get("type");
		
		if( type.equals("Creature") ){
			return new Creature(map);
		} else if( type.equals("SpecialCharacter") ){
			return SpecialCharacterFactory.createSpecialCharacter((String)map.get("name"));
		} else if( type.equals("Special Income") ){
			return SpecialIncomeFactory.createSpecialIncome((String)map.get("name"));
		} else if( type.equals("Random Event") ){
			return RandomEventFactory.createRandomEvent((String)map.get("name"));
		} else if( type.equals("Magic Event") ){
			return MagicEventFactory.createMagicEvent((String)map.get("name"));
		} else {
			System.err.println("Error PieceFactory: type not recognized "+type);
			return null;
		}
	}
}

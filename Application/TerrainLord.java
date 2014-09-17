package KAT;
// 
// TerrainLord.java
// kingsandthings/
// @author Brandon Schurman
//

public class TerrainLord extends SpecialCharacter
{
    /**
     * CTOR
     */
    public TerrainLord( String terrainType ){
        super("", "", "", terrainType, 4, false, false, false, false);
        setType("Special Character");
        
        String name = "";
        String front = ""; // TODO set these to the correct
        String back = "";  // image path in switch below

        switch( terrainType ){
            case "DESERT":
                name = "Desert Master";
                front = "Images/Hero_DesertMaster.png";
                back = "Images/Creature_Back.png";
                break;
            case "FOREST":
                name = "Forest King";
                front = "Images/Hero_ForestKing.png";
                back = "Images/Creature_Back.png";
                break;
            case "FROZENWASTE":
                name = "Ice Lord";
                front = "Images/Hero_IceLord.png";
                back = "Images/Creature_Back.png";
                break;
            case "JUNGLE":
                name = "Jungle Lord";
                front = "Images/Hero_JungleLord.png";
                back = "Images/Creature_Back.png";
                break;
            case "MOUNTAIN":
                name = "Mountain King";
                front = "Images/Hero_MountainKing.png";
                back = "Images/Creature_Back.png";
                break;
            case "PLAINS":
                name = "Plains Lord";
                front = "Images/Hero_PlainsLord.png";
                back = "Images/Creature_Back.png";
                break;
            case "SWAMP":
                name = "Swamp King";
                front = "Images/Hero_SwampKing.png";
                back = "Images/Creature_Back.png";
                break;
            default:
                System.err.println("TerrainLord error: Unrecognized terrainType");
                break;
        }
        // setType("TerrainLord");
        setName(name);
        setFront(front);
        setBack(back);
        setTerrain(terrainType);
    }
}

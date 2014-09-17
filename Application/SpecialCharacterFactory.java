package KAT;

public class SpecialCharacterFactory
{
    public static SpecialCharacter createSpecialCharacter( String name ){
        SpecialCharacter specialCharacter = null;

        if( name.equals("Arch Cleric") ){
            specialCharacter = new SpecialCharacter("Images/Hero_ArchCleric.png", "Images/Creature_Back.png", "Arch Cleric", "", 5, false, true, false, false);
        } 
        else if (name.equals("Arch Mage")) {
        	specialCharacter = new SpecialCharacter("Images/Hero_ArchMage.png", "Images/Creature_Back.png", "Arch Mage", "", 6, false, true, false, false);
        } 
        else if (name.equals("Assassin Primus")) {
        	specialCharacter = new AssassinPrimus();
        } 
        else if (name.equals("Baron Munchhausen")) {
        	specialCharacter = new BaronMunchausen();
        } 
        else if (name.equals("Deerhunter")) {
        	specialCharacter = new SpecialCharacter("Images/Hero_Deerhunter.png", "Images/Creature_Back.png", "Deerhunter", "", 4, false, false, false, false);
        } 
        else if (name.equals("Desert Master")) {
        	specialCharacter = new TerrainLord("DESERT");
        } 
        else if (name.equals("Dwarf King")) {
        	specialCharacter = new DwarfKing();
        } 
        else if (name.equals("Elf Lord")) {
        	specialCharacter = new SpecialCharacter("Images/Hero_ElfLord.png", "Images/Creature_Back.png", "Elf Lord", "", 6, false, false, false, true);
        }
        else if (name.equals("Forest King")) {
        	specialCharacter = new TerrainLord("FOREST");
        }
        else if (name.equals("Ghaogh II")) {
        	specialCharacter = new SpecialCharacter("Images/Hero_GhaoghII.png", "Images/Creature_Back.png", "Ghaogh II", "", 6, true, false, false, false);
        }
        else if (name.equals("Grand Duke")) {
        	specialCharacter = new GrandDuke();
        }
        else if (name.equals("Ice Lord")) {
        	specialCharacter = new TerrainLord("FROZENWASTE");
        }
        else if (name.equals("Jungle Lord")) {
        	specialCharacter = new TerrainLord("JUNGLE");
        }
        else if (name.equals("Lord Of The Eagles")) {
        	specialCharacter = new SpecialCharacter("Images/Hero_LordOfTheEagles.png", "Images/Creature_Back.png", "Lord Of The Eagles", "", 5, true, false, false, false);
        }
        else if (name.equals("Marksman")) {
        	specialCharacter = new Marksman();
        }
        else if (name.equals("Master Thief")) {
        	specialCharacter = new MasterThief();
        }
        else if (name.equals("Mountain King")) {
        	specialCharacter = new TerrainLord("MOUNTAIN");
        }
        else if (name.equals("Plains Lord")) {
        	specialCharacter = new TerrainLord("PLAINS");
        }
        else if (name.equals("Sir Lance-A-Lot")) {
        	specialCharacter = new SpecialCharacter("Images/Hero_SirLance-A-Lot.png", "Images/Creature_Back.png", "Sir Lance-A-Lot", "", 5, false, false, true, false);
        }
        else if (name.equals("Swamp King")) {
        	specialCharacter = new TerrainLord("SWAMP");
        }
        else if (name.equals("Sword Master")) {
        	specialCharacter = new SwordMaster();
        }
        else if (name.equals("Warlord")) {
        	specialCharacter = new Warlord();
        }

        return specialCharacter;
    }
}

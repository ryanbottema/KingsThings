package KAT;

import java.util.HashMap;
import java.util.Iterator;

import javafx.application.Platform;
import javafx.scene.image.Image;

public class ClickObserver {

	private static ClickObserver uniqueInstance;
	
	private Terrain clickedTerrain, previouslyClickedTerrain;
	private Player activePlayer;
	private Piece clickedPiece;
	private Player clickedPlayer;
	private Fort clickedFort;
	
	/*
	 * String terrainFlag is used for determining what state the game is in when a terrain is clicked. 
	 * 
	 * 		""; 									no phase. Default value. 
     * 		"Disabled": 							no effect when clicked
	 * 		"Setup: deal": 							populates board
	 * 		"Setup: SelectStartTerrain":			setup phase. Player picking starting positions
	 * 		"Setup: SelectTerrain":       			player adding a tile
	 * 		"Setup: RemoveBadAdjWater"				player selecting a Sea terrain to replace
	 * 		"RecruitingThings: PlaceThings":		Place things from rack to board
     * 		"Movement: SelectMoveSpot":				Once creatures are selected from infoPanel, select terrain to move to
     * 		"Construction: ConstructFort":      	player picking a tile for construction
	 */
	private String terrainFlag;
	/*
	 * String creatureFlag is used for determining what state the game is in when a creature is clicked. 
	 * 
	 * 		""; 									no phase. Default value. 
	 * 		"Movement: SelectMovers":				Selecting Creatures to move from infoPanel
	 * 		"Combat: SelectPieceToAttackWith":	    Combat. Select creature to attack with
	  * 	"Combat: SelectPieceToGetHit"			Select a piece to get hit
	  * 	"Combat: SelectRetreaters"				Select pieces to retreart
	  * 	"Combat: SelectCreatureToBribe"			Bribing
	 */
	 private String creatureFlag;
	 /*
	  * String playerFlag is used for determining what state the game is in when a player is clicked
	  * 
	  * 	"":										   no phase. Default value
	  * 	"Attacking: SelectPlayerToAttack"	       When more that two combatants on a terrain, select which one to attack
	  *     "Master Thief: SelectingPlayerToStealFrom" When using the Master Thief ability, select the player that you wish to steal from.
	  */
	 private String playerFlag;
	 
	 private String fortFlag;
	
	/*
	 * --------- Constructor
	 */
	private ClickObserver () {
		terrainFlag = "";
		creatureFlag = "";
		playerFlag = "";
		fortFlag = "";
	}
	
	/*
	 * --------- Gets and Sets
	 */
	public Terrain getClickedTerrain() { return clickedTerrain; }
	public Player getActivePlayer() { return activePlayer; }
	public String getCreatureFlag() { return creatureFlag; }
	public String getTerrainFlag() { return terrainFlag; }
	public Piece getClickedPiece() { return clickedPiece; }
	
	public void setClickedTerrain(Terrain t) { 
		previouslyClickedTerrain = clickedTerrain;
		clickedTerrain = t; 
	}
	public void setClickedCreature(Creature c) { 
		clickedPiece = c;
		if (c != null)
			clickedPiece.getPieceNode().toFront();
	}
	public void setClickedPlayer(Player p) { clickedPlayer = p; }
	public void setClickedFort(Fort f) { clickedFort = f; }
	
	public void setFortFlag(String s) { fortFlag = s; }
	public void setTerrainFlag(String s) { terrainFlag = s; }
	public void setCreatureFlag(String s) { creatureFlag = s; }
	public void setPlayerFlag(String s) { playerFlag = s; }
	public void setActivePlayer(Player p) { activePlayer = p; }
    
    
    public static ClickObserver getInstance(){
        if(uniqueInstance == null){
            uniqueInstance = new ClickObserver();
        }
        return uniqueInstance;
    }
	
	public void whenTerrainClicked() {
		switch (terrainFlag) {
		
			case "Setup: SelectStartTerrain":
				GameLoop.getInstance().addStartingHexToPlayer();
				break;
			case "Setup: SelectTerrain":
				GameLoop.getInstance().addHexToPlayer();
				break;
			case "Setup: RemoveBadAdjWater":
				Board.switchBadWater(clickedTerrain.getCoords());
				break;
	        case "Construction: ConstructFort":
	            GameLoop.getInstance().constructFort();
	            InfoPanel.showTileInfo(clickedTerrain);
	            clickedTerrain.moveAnim();
	            break;
	        case "Setup: deal":
	            Board.populateGameBoard();
	            PlayerRackGUI.updateRack();
	            terrainFlag = "";
	        case "Disabled":
	            PlayerRackGUI.updateRack();
	        	clickedTerrain = previouslyClickedTerrain;
	             // disable display of other terrain pieces
	             break;
			case "RecruitingThings: PlaceThings":
				GameLoop.getInstance().playThings(); 
				InfoPanel.showTileInfo(clickedTerrain);
	            clickedTerrain.moveAnim();
	            PlayerRackGUI.updateRack();
				break;
			case "RecruitingSpecials":
				GameLoop.getInstance().recruitSpecials();
				InfoPanel.showTileInfo(clickedTerrain);
				PlayerRackGUI.updateRack();
				break;
			case "RandomEvents":
				GameLoop.getInstance().useRandoms();
				InfoPanel.showTileInfo(clickedTerrain);
				PlayerRackGUI.updateRack();
				break;
			case "Movement: SelectMoveSpot":
				
				terrainFlag = "Disabled";
				if (clickedTerrain.moveStack(previouslyClickedTerrain) != 0) {
					Board.removeCovers();
					Board.animStackMove(previouslyClickedTerrain, clickedTerrain, activePlayer.getName());
					clickedTerrain = previouslyClickedTerrain;
					clickedTerrain.coverPieces();
    				InfoPanel.showTileInfo(clickedTerrain);
					if (creatureFlag.equals("Combat: SelectRetreaters")) {
	                	Board.applyCovers();
	                	clickedTerrain.uncover();
					}
					if( GameLoop.getInstance().isNetworked() ){
						/*
						HashMap<String,Object> map = new HashMap<String,Object>();
						map.put("updateType", "moveArmy");
						ArrayList<Integer> pIDs = new ArrayList<Integer>();
						for( )
						map.put("pIDs", );
						*/
					}
				}
					
				break;
			default:
				InfoPanel.showTileInfo(clickedTerrain);
				clickedTerrain.uncoverPieces(activePlayer.getName());
	            clickedTerrain.moveAnim();
	            PlayerRackGUI.updateRack();
				break;
		}
	}
	
	public void whenCreatureClicked() {
		switch (creatureFlag) {
			case "Movement: SelectMovers":
				((Movable)clickedPiece).toggleAboutToMove();
		        Board.removeCovers();
		        if (clickedPiece instanceof Movable)
		        	System.out.println("Moves left for " + ((Movable)clickedPiece).movesLeft() + clickedPiece.getName());

		        for (Creature c : clickedTerrain.getContents(activePlayer.getName()).filterCreatures(clickedTerrain.getContents(activePlayer.getName()).getStack())) {
		        	if (c.isAboutToMove()) {
		        		Board.applyCovers(c);
		        	}
		        }
		        clickedPiece.getStackedIn().cascade(clickedPiece);
				
				if (clickedTerrain.countMovers(activePlayer.getName()) == 0)
					terrainFlag = "";
				else 
					terrainFlag = "Movement: SelectMoveSpot";
				break;
			case "Combat: SelectPieceToAttackWith":
				clickedPiece.getStackedIn().cascade(clickedPiece);
				GameLoop.getInstance().setPieceClicked(clickedPiece);
				
				if( GameLoop.getInstance().isNetworked() ){
					HashMap<String,Object> map = new HashMap<String,Object>();
					map.put("updateType", "Combat:AttackingPiece");
					map.put("attackingPiece", clickedPiece.getPID());
					NetworkGameLoop.getInstance().postGameState(map);
				}
				break;
			case "Combat: SelectPieceToGetHit":
				clickedPiece.getStackedIn().cascade(clickedPiece);
				GameLoop.getInstance().setPieceClicked(clickedPiece);
				break;
			case "Combat: SelectRetreaters":
				((Movable)clickedPiece).toggleAboutToMove();
		        Board.removeCovers();

		        for (Creature c : clickedTerrain.getContents(activePlayer.getName()).filterCreatures(clickedTerrain.getContents(activePlayer.getName()).getStack())) {
		        	if (c.isAboutToMove()) {
		        		Board.applyCovers(c);
		        		Board.applyCovers(GameLoop.getInstance().getPlayer());
		        	}
		        }
		        clickedPiece.getStackedIn().cascade(clickedPiece);
				
				if (clickedTerrain.countMovers(activePlayer.getName()) == 0) {
					terrainFlag = "";
					Board.applyCovers();
					clickedTerrain.uncover();
				} else 
					terrainFlag = "Movement: SelectMoveSpot";
				break;
			case "Combat: SelectCreatureToBribe":
				clickedPiece.getStackedIn().cascade(clickedPiece);
				GameLoop.getInstance().setPieceClicked(clickedPiece);
			
			case "Select Assassin Piece":
				clickedPiece.getStackedIn().cascade(clickedPiece);
				AssassinPrimus.setAssassinPiece(clickedPiece);
				break;
			
			case "Select Victim Piece":
				clickedPiece.getStackedIn().cascade(clickedPiece);
				AssassinPrimus.setAssassinPiece(clickedPiece);
				break;
				
			default:
				clickedPiece.getStackedIn().cascade(clickedPiece);
				break;
		}
	}

	public void whenPlayerClicked() {
		switch (playerFlag) {
			case "Attacking: SelectPlayerToAttack":
				
				GameLoop.getInstance().setPlayerClicked(clickedPlayer);
				GameLoop.getInstance().unPause();
				
				if( GameLoop.getInstance().isNetworked() ){
					HashMap<String,Object> map = new HashMap<String,Object>();
					map.put("updateType", "Combat:DefendingPlayer");
					map.put("defendingPlayer", clickedPlayer.getName());
					NetworkGameLoop.getInstance().postGameState(map);
				}
				
				break;

			case "Master Thief: SelectingPlayerToStealFrom":
			    MasterThief.setVictim(clickedPlayer);
			    GameLoop.getInstance().unPause();
			    break;
			default:
				break;
		
		}
	}
	
	public void whenFortClicked() {
		switch (fortFlag) {
			case "Combat: SelectPieceToAttackWith":
			
				GameLoop.getInstance().setPieceClicked(clickedFort);
				
				break;
			case "Combat: SelectPieceToGetHit":

				GameLoop.getInstance().setPieceClicked(clickedFort);
				break;
			default:
				break;
	
		}
	}
}

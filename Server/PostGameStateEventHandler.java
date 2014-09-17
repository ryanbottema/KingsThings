package KAT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PostGameStateEventHandler implements EventHandler 
{
	@SuppressWarnings("unchecked")
	@Override
	public boolean handleEvent( Event event )throws IOException {
		boolean error = false;
		
		String updateType = (String)event.get("updateType");
		Integer phaseNum = (Integer)event.get("phaseNumber");
		int uID = KATDB.getUID((String)event.get("username"));
		int gID = KATDB.getGID(uID);
		
		// check for pieces to add/remove to cup
		if( updateType.equals("removeFromCup") ){
			ArrayList<Integer> pIDs = (ArrayList<Integer>)event.get("pIDs");
			for( Integer pID : pIDs ){
				KATDB.removeFromCup(gID, pID);
			}
			KATDB.commit();
		} else if( updateType.equals("addToCup") ){
			ArrayList<Integer> pIDs = (ArrayList<Integer>)event.get("pIDs");
			for( Integer pID : pIDs ){
				KATDB.addToCup(gID, pID);
			}
			KATDB.commit();
		}
		
		// check for updates to game tiles
		if( updateType.equals("addPlayerToTile") ){
			HashMap<String,Object> tile = (HashMap<String,Object>)event.get("tile");
			KATDB.setTileOwner(gID, uID, tile);
		} else if( updateType.equals("addPiecesToTile") ){
			HashMap<String,Object> tile = (HashMap<String,Object>)event.get("tile"); 
			ArrayList<Integer> pIDs = (ArrayList<Integer>)event.get("pIDs");
			for( Integer pID : pIDs ){
				KATDB.addPieceToTileForPlayer(pID, uID, gID, tile);
			}
		} else if( updateType.equals("removePieceFromTile") ){
			HashMap<String,Object> tile = (HashMap<String,Object>)event.get("tile");
			KATDB.removePieceFromTileForPlayer((Integer)tile.get("pID"), uID, gID);
		} else if( updateType.equals("moveStack") ){
			HashMap<String,Object> fromTile = (HashMap<String,Object>)event.get("fromTile");
			HashMap<String,Object> toTile = (HashMap<String,Object>)event.get("toTile");
			ArrayList<Integer> pIDs = (ArrayList<Integer>)event.get("pIDs");
			for( Integer pID : pIDs ){
				KATDB.removePieceFromTileForPlayer(pID, uID, gID);
				KATDB.addPieceToTileForPlayer(pID, uID, gID, toTile);
			}
			if( toTile.get("owner").equals(event.get("username")) ){
				KATDB.setTileOwner(gID, uID, toTile);
			}
		}
		
		// check for any fort construction updates
		if( updateType.equals("constructFort") ){
			HashMap<String,Object> tile = (HashMap<String,Object>)event.get("tile");
			KATDB.constructFort(gID, tile);
		} else if( updateType.equals("upgradeFort") ){
			HashMap<String,Object> tile = (HashMap<String,Object>)event.get("tile");
			KATDB.upgradeFort(gID, tile);
		} else if( updateType.equals("neutralizeFort") ){
			HashMap<String,Object> tile = (HashMap<String,Object>)event.get("tile");
			KATDB.neutralizeFort(gID, tile);
		} else if( updateType.equals("unNeutralizeFort") ){
			HashMap<String,Object> tile = (HashMap<String,Object>)event.get("tile");
			KATDB.unNeutralizeFort(gID, tile);
		}
		
		// check for player's player rack
		HashMap<String,Object> rack = (HashMap<String,Object>)event.get("playerRack");
		if( rack != null ){
			ArrayList<Integer> rackPIDs = (ArrayList<Integer>)rack.get("pIDs");
			ArrayList<Integer> dbPIDs = KATDB.getPIDsInPlayerRack(gID, uID);
			// check for pieces not in db but in player rack
			for( Integer pID : rackPIDs ){
				if( !dbPIDs.contains(pID) ){
					KATDB.addPieceToPlayerRack(gID, uID, pID);
				}
			}
			// check for pieces in db but not in rack
			for( Integer pID : dbPIDs ){
				if( !rackPIDs.contains(pID) ){
					KATDB.removePieceFromPlayerRack(gID, uID, pID);
				}
			}
			KATDB.commit();
		}
		
		// check for off-side special characters
		
		// check if turn should change
		Boolean changeTurns = (Boolean)event.get("changeTurns");
		if( changeTurns != null && changeTurns == true ){
			KATDB.changeTurns(gID);
		} 
		if( updateType.equals("changePlayerOrder") ){
			KATDB.changeTurns(gID); // does the same thing, just again
		}
		
		// check if should update to show all tiles
		Boolean showAllTiles = (Boolean)event.get("showAllTiles");
		if( showAllTiles != null && showAllTiles == true ){
			KATDB.showAllTiles(gID);
		}
		
		// check if gold was collected 
		Integer gold = (Integer)event.get("gold");
		if( gold != null ){
			KATDB.updateGold(gold, uID);
		}
		
		// update phase number in database
		if( phaseNum != null ){
			KATDB.updatePhaseNumber(gID, phaseNum);
		}
		
		return !error;
	}

}

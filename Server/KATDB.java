// 
// KATDB.java
// kingsandthings/Server/
// @author Brandon Schurman
//
package KAT;

import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;


public class KATDB
{
    private static Connection db; // global database connection (for concurrent access)

    public static void setup(){
        // open db connection
        try {
            Class.forName("org.sqlite.JDBC");
            db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            db.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            db.setAutoCommit(false);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            db = null;
        }

        dropTables();
        createTables();
    }

    private static void createTables(){
        try {
            // open a statement
            Statement stmnt = db.createStatement();


            /* create tables (if they do not already exist) */

            String sql;

            // users are identified by their unique uID (or username)
            sql = "create table if not exists users("
                + "uID integer primary key autoincrement,"
                + "username text unique not null);";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // the gID uniquely identifies a single game
            sql = "create table if not exists games("
                + "gID integer primary key autoincrement,"
                + "gameSize integer not null," // number of players allowed to play
                + "numPlayers integer not null," // number of players currently in game 
                + "user1 integer not null," 
                + "user2 integer,"
                + "user3 integer," 
                + "user4 integer,"
                + "phaseNum integer,"
                + "foreign key(user1) references users(uID),"
                + "foreign key(user2) references users(uID),"
                + "foreign key(user3) references users(uID),"
                + "foreign key(user4) references users(uID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // players are users with extra game specific values
            sql = "create table if not exists players("
                + "uID integer not null,"
                + "gID integer not null,"
                + "color text not null,"
                + "gold integer not null,"
                + "primary key(uID, gID),"
                + "foreign key(uID) references users(uID),"
                + "foreign key(gID) references games(gID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // a table of all existing pieces
            sql = "create table if not exists pieces("
                + "pID integer not null,"
                + "gID integer not null," 
                + "type text not null,"
                + "fIMG text,"
                + "bIMG text,"
                + "primary key(pID, gID),"
                + "foreign key(gID) references games(gID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // game board tiles
            sql = "create table if not exists tiles("
                + "tID integer primary key autoincrement,"
                + "gID integer not null,"
                + "x integer not null,"
                + "y integer not null,"
                + "z integer not null,"
                + "terrain text not null," 
                + "tile_orient integer not null," // 1 for face-up, 0 face-down
                + "owner integer," // owner of the tile (could be different from owner of piece)
                + "uID integer," // uID of player that owns piece with pID
                + "pID intteger," // pID of a piece on this tile
                + "piece_orient integer," // pID of a piece on this tile
                + "unique(gID,x,y,z,pID)," 
                + "foreign key(gID) references games(gID),"
                + "foreign key(owner) references users(uID),"
                + "foreign key(uID) references users(uID),"
                + "foreign key(pID) references pieces(pID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // player racks correspond to a user and a game
            sql = "create table if not exists playerRacks("
                + "uID integer not null,"
                + "gID integer not null,"
                + "pID integer not null,"
                + "primary key(uID, gID, pID),"
                + "foreign key(uID) references uses(uID),"
                + "foreign key(gID) references games(gID),"
                + "foreign key(pID) references pieces(pID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // the cup for each game
            sql = "create table if not exists cups("
                + "gID integer,"
                + "pID integer,"
                + "primary key(gID, pID),"
                + "foreign key(gID) references games(gID),"
                + "foreign key(pID) references pieces(pID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // small table to store any special character that are offside
            sql = "create table if not exists offside("
                + "pID primary key,"
                + "uID integer,"
                + "gID integer,"
                + "foreign key(pID) references pieces(pID),"
                + "foreign key(uID) references users(uID),"
                + "foreign key(gID) references games(gID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // a table of all existing creatures 
            sql = "create table if not exists creatures("
                + "pID integer not null,"
                + "gID integer not null,"
                + "name text not null,"
                + "combatVal integer not null,"
                + "flying integer not null," // treated as a boolean (1 or 0)
                + "ranged integer not null,"
                + "magic integer not null,"
                + "charging integer not null,"
                + "terrain text,"
                + "orientation integer," // boolean, 1 means face up
                + "primary key(pID, gID),"
                + "foreign key(pID) references pieces(pID),"
                + "foreign key(gID) references games(gID),"
                + "foreign key(terrain) references tiles(terrain));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // special characters
            sql = "create table if not exists specialCharacters("
                + "pID integer not null,"
                + "gID integer not null,"
                + "name text not null,"
                + "combatVal integer,"
                + "flying integer," // boolean (1 or 0)
                + "ranged integer,"
                + "magic integer,"
                + "charging integer,"
                + "primary key(pID, gID),"
                + "foreign key(gID) references pieces(gID)"
                + "foreign key(pID) references pieces(pID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // special income counters 
            sql = "create table if not exists specialIncomes("
                + "pID integer not null,"
                + "gID integer not null,"
                + "name text not null,"
                + "value integer not null,"
                + "treasure integer not null," // boolean 1 or 0
                + "primary key(pID, gID),"
                + "foreign key(pID) references pieces(pID),"
                + "foreign key(gID) references pieces(gID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // random events
            sql = "create table if not exists randomEvents("
                + "pID integer not null,"
                + "gID integer not null,"
                + "name text not null,"
                + "owner text,"
                + "primary key(pID, gID),"
                + "foreign key(pID) references pieces(pID),"
                + "foreign key(gID) references pieces(gID),"
                + "foreign key(owner) references players(username));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // magic events
            sql = "create table if not exists magicEvents("
                + "pID integer not null,"
                + "gID integer not null,"
                + "name text not null,"
                + "owner text,"
                + "primary key(pID, gID),"
                + "foreign key(pID) references pieces(pID),"
                + "foreign key(gID) references pieces(gID),"
                + "foreign key(owner) references players(username));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // forts are specific to an owner-hex
            sql = "create table if not exists forts("
                + "gID integer not null,"
                + "x integer not null,"
                + "y integer not null,"
                + "z integer not null,"
                + "combatVal integer not null,"
                + "neutralized integer not null," // 1 for true, else 0
                + "primary key(gID,x,y,z),"
                + "foreign key(gID) references games(gID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // table for battles during combat phase
            sql = "create table if not exists battleGrounds("
                + "gID integer not null,"
                + "x integer not null,"
                + "y integer not null,"
                + "z integer not null,"
                + "attackingPiece integer,"
                + "attackingPlayer integer,"
                + "defendingPlayer integer,"
                + "combatPhase text,"
                + "primary key(gID,x,y,z),"
                + "foreign key(gID) references games(gID),"
                + "foreign key(attackingPiece) references pieces(pID),"
                + "foreign key(attackingPlayer) references players(uID),"
                + "foreign key(defendingPlayer) references players(uID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            db.commit();
        } catch( Exception e ){
            e.printStackTrace();
        } 
    }

    private static void dropTables(){
        try { 
            //Class.forName("org.sqlite.JDBC");
            //Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            String sql = "";

            sql = "drop table if exists users;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists games;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists players;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists pieces;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists tiles;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists playerRacks;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists cups;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists offside;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists creatures;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists specialCharacters;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists specialIncomes;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists randomEvents;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists magicEvents;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists forts;";
            stmnt.executeUpdate(sql);

            stmnt.close();
            db.commit();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * Adds a piece to game's cup
     * Note that for efficiency (this method is probably called in a loop)
     * this method does not commit any transactions and KATDB.commit() should be called to do so.
     * @param gID id of game
     * @param piece map containing details of piece
     */
    public static void addPiece( int gID, 
            HashMap<String,Object> piece ){
        try {
            //Class.forName("org.sqlite.JDBC");
            //Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            String sql = "";

            String type = (String)piece.get("type");
            int pID = (Integer)piece.get("pID");

            // add to generic piece table first
            sql = "insert into pieces(pID, gID, type, fIMG, bIMG)"
                + "values("+pID+","+gID+",'"+type
                + "','"+piece.get("fIMG")+"','"+piece.get("bIMG")+"');";
            stmnt.executeUpdate(sql);
            stmnt.close();

            // add to correct table of piece type
            stmnt = db.createStatement();

            if( type.equals("Creature") ){
                sql = "insert into creatures(pID,gID,name,combatVal,"
                    + "terrain,orientation,flying,ranged,charging,magic)"
                    + "values("+pID+","+gID+",'"+piece.get("name")
                    + "',"+piece.get("combatVal")+",'"+piece.get("terrain")
                    + "',0,"+piece.get("flying")+","+piece.get("ranged")
                    + ","+piece.get("charging")+","+piece.get("magic")+");";
                stmnt.executeUpdate(sql);
            } else if( type.equals("SpecialCharacter") || type.equals("TerrainLord") ){
                sql = "insert into specialCharacters(pID,gID,name,combatVal,"
                    + "flying,ranged,charging,magic)"
                    + "values("+pID+","+gID+",'"+piece.get("name")
                    + "',"+piece.get("combatVal")+","+piece.get("flying")
                    + ","+piece.get("ranged")+","+piece.get("charging")
                    + ","+piece.get("magic")+");";
                stmnt.executeUpdate(sql);
            } else if( type.equals("Special Income") ){
                sql = "insert into specialIncomes(pID,gID,name,value,treasure)"
                    + "values("+pID+","+gID+",'"+piece.get("name")
                    + "',"+piece.get("value")+","+piece.get("treasure")+");";
                stmnt.executeUpdate(sql);
            } else if( type.equals("Random Event") ){
                sql = "insert into randomEvents(pID,gID,name,owner)"
                    + "values("+pID+","+gID+",'"+piece.get("name")+"','"+piece.get("owner")+"');";
                stmnt.executeUpdate(sql);
            } else if( type.equals("Magic Event") ){
                sql = "insert into magicEvents(pID,gID,name,owner)"
                    + "values("+pID+","+gID+",'"+piece.get("name")+"','"+piece.get("owner")+"');";
                stmnt.executeUpdate(sql);
            } else {
                System.err.println("Error adding piece to cup: unrecognized piece type: "+type);
            }
            stmnt.close();

            // add to cup
            stmnt = db.createStatement();
            sql = "insert into cups(gID,pID) "
                + "values("+gID+","+pID+");";
            stmnt.executeUpdate(sql);
            stmnt.close();
            //db.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * Adds a tile to a given game.
     * Note: for efficiency (this method is usually called in a loop)
     * this method does not commit any transactions, 
     * KATDB.commit() should be called when needed. 
     * @param gID
     * @param tile
     */
    public static void addTile( int gID, HashMap<String,Object> tile ){
        try {
            Statement stmnt = db.createStatement();
            String sql;

            int x = (Integer)tile.get("x");
            int y = (Integer)tile.get("y");
            int z = (Integer)tile.get("z");
            String terrain = (String)tile.get("terrain");
            int orientation = (Integer)tile.get("tile_orient");

            sql = "insert into tiles(gID,x,y,z,terrain,tile_orient) values("
                + gID+","+x+","+y+","+z+",'"+terrain+"',"+orientation+");";
            stmnt.executeUpdate(sql);

            stmnt.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static int getTID( int gID, int x, int y, int z ){
        int tID = -1;
        try {
            Statement stmnt = db.createStatement();
            String sql = "select * from tiles where "
                + "gID="+gID+" and x="+x+" and y="+y+" and z="+z+";";
            ResultSet rs = stmnt.executeQuery(sql);
            if( rs.next() ){
                tID = rs.getInt("tID");
            } else {
                System.err.println("tID not found for gID "+gID
                        +" and x,y,z "+x+","+y+","+z);
            }
        } catch( Exception e ){
            e.printStackTrace();
        }
        return tID;
    }

    /**
     * Inserts a new tuple to tiles with the given pID
     * Note: does not commit as this is probably called in a loop if a user moved their stacks
     * @param pID
     * @param uID
     * @param gID
     * @param tile
     */
    public static void addPieceToTileForPlayer( int pID, int uID, int gID, HashMap<String,Object> tile ){
    	int owner = -1;
    	if( (String)tile.get("owner") != null ){
    		getUID((String)tile.get("owner"));
    	}
        try {
            Statement stmnt = db.createStatement();
            String sql;

            int x = (Integer)tile.get("x");
            int y = (Integer)tile.get("y");
            int z = (Integer)tile.get("z");
            String terrain = (String)tile.get("terrain");
            int tile_orient = (Integer)tile.get("tile_orient");
            int piece_orient = (Integer)tile.get("piece_orient");

            if( owner != -1 ){
	            sql = "insert into tiles(gID,x,y,z,terrain,tile_orient,owner,uID,pID,piece_orient) values("
	                + gID+","+x+","+y+","+z+",'"+terrain+"',"+tile_orient+","+owner+","+uID+","+pID+","+0+");";
            } else {
	            sql = "insert into tiles(gID,x,y,z,terrain,tile_orient,uID,pID,piece_orient) values("
		                + gID+","+x+","+y+","+z+",'"+terrain+"',"+tile_orient+","+uID+","+pID+","+0+");";
            }
            stmnt.executeUpdate(sql);

            stmnt.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * Remove the tuple in tiles with the given pID
     * Note does not commit as this is usually done in a loop
     * @param pID
     * @param uID
     * @param gID
     */
    public static void removePieceFromTileForPlayer( int pID, int uID, int gID ){
        try {
            Statement stmnt = db.createStatement();
            String sql;

            sql = "delete from tiles where gID="+gID+" and pID="+pID+";";
            stmnt.executeUpdate(sql);

            stmnt.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * Sets the owner of all tiles corresponding to the coords and gID
     * @param gID
     * @param uID
     */
    public static void setTileOwner( int gID, int uID, HashMap<String,Object> tile ){
        try {
            Statement stmnt = db.createStatement();
            int x = (Integer)tile.get("x");
            int y = (Integer)tile.get("y");
            int z = (Integer)tile.get("z");
            String sql = "update tiles set owner="+uID+" "
                + "where gID="+gID+" and x="+x+" and y="+y+" and z="+z+";";
            stmnt.executeUpdate(sql);
            stmnt.close();
            db.commit();
            System.out.println("Ownership of tile "+x+","+y+","+z+" given to uID="+uID);
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void showAllTiles( int gID ){
        try {
            Statement stmnt = db.createStatement();
            String sql = "update tiles set tile_orient="+1+" where gID="+gID+";";
            stmnt.executeUpdate(sql);
            stmnt.close();
            db.commit();
            System.out.println("all tiles are revealed in game with gID="+gID);
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void constructFort( int gID, HashMap<String,Object> fort ){
        try {
            Statement stmnt = db.createStatement();
            int x = (Integer)fort.get("x");
            int y = (Integer)fort.get("y");
            int z = (Integer)fort.get("z");
            String sql = "insert into forts(gID,x,y,z,combatVal,neutralized)"
                + "values("+gID+","+x+","+y+","+z+","+1+","+0+");";
            stmnt.executeUpdate(sql);
            stmnt.close();
            db.commit();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void upgradeFort( int gID, HashMap<String,Object> fort ){
        try {
            Statement stmnt = db.createStatement();
            String sql;
            int x = (Integer)fort.get("x");
            int y = (Integer)fort.get("y");
            int z = (Integer)fort.get("z");
            sql = "select * from forts where "
                + "gID="+gID+" and x="+x+" and y="+y+" and z="+z+";";
            ResultSet rs = stmnt.executeQuery(sql);
            int combatVal = 0;
            if( rs.next() ){
                combatVal = rs.getInt("combatVal");
            } else {
                System.err.println("Error: fort not found for gID="+gID
                        +" and coords"+x+","+y+","+z);
                return;
            }
            rs.close();
            stmnt.close();
            stmnt = db.createStatement();
            sql = "update forts set combatVal="+(combatVal+1)
                + " where gID="+gID+" and x="+x+" and y="+y+" and z="+z+";";
            stmnt.executeUpdate(sql);
            stmnt.close();
            db.commit();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void neutralizeFort( int gID, HashMap<String,Object> fort ){
        try {
            Statement stmnt = db.createStatement();
            String sql;
            int x = (Integer)fort.get("x");
            int y = (Integer)fort.get("y");
            int z = (Integer)fort.get("z");
            sql = "update forts set neutralized="+1
                + "where gID="+gID+" and x="+x+" and y="+y+" and z="+z+";";
            stmnt.executeUpdate(sql);
            stmnt.close();
            db.commit();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void unNeutralizeFort( int gID, HashMap<String,Object> fort ){
        try {
            Statement stmnt = db.createStatement();
            String sql;
            int x = (Integer)fort.get("x");
            int y = (Integer)fort.get("y");
            int z = (Integer)fort.get("z");
            sql = "update forts set neutralized="+0
                + "where gID="+gID+" and x="+x+" and y="+y+" and z="+z+";";
            stmnt.executeUpdate(sql);
            stmnt.close();
            db.commit();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void updatePiece( int gID, HashMap<String,Object> piece ){

    }

    /**
     * Adds a pID to the cup.
     * Does not autocommit as is probably done in a loop
     * @param gID
     * @param pID
     */
    public static void addToCup( int gID, int pID ){
        try {
            Statement stmnt = db.createStatement();
            String sql = "insert into cups(gID, PID)"
                + "values("+gID+","+pID+");";
            stmnt.executeUpdate(sql);
            stmnt.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * Removes a piece from a game's cup
     * Note does not commit, should do so manually
     * @param gID game ID corresponding to the cup
     * @param map details of piece to remove
     */
    public static void removeFromCup( int gID, int pID ){
        try {
            Statement stmnt = db.createStatement();
            String sql = "delete from cups where pID="+pID+" AND gID="+gID+";";
            stmnt.executeUpdate(sql);
            stmnt.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void updateGold( int gold, int uID ){
        try {
            Statement stmnt = db.createStatement();
            String sql = "update players set gold="+gold+" where uID="+uID+";";
            stmnt.executeUpdate(sql);
            db.commit();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * Returns the user ID for a username
     * @param username user's login name
     * @return uID 
     */
    public static int getUID( String username ){
        int uID = -1;
        try { 
            //Class.forName("org.sqlite.JDBC");
            //Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            ResultSet rs;

            String sql = "select * from users where username = '"+username+"';";
            rs = stmnt.executeQuery(sql);
            if( rs.next() ){
                uID = rs.getInt(1);
            }

            rs.close();
            stmnt.close();
            //db.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
        return uID;
    }

    /**
     * retrieves the username corresponding to a given uID
     * @return username
     */
    public static String getUsername( int uID ){
        String username = "";
        try {
            Statement stmnt = db.createStatement();
            String sql = "select * from users where uID='"+uID+"';";
            ResultSet rs = stmnt.executeQuery(sql);
            if( rs.next() ){
                username = rs.getString("username");
            } else {
                System.err.println("Error: user not found with uID = "+uID);
            }
        } catch( Exception e ){
            e.printStackTrace();
        }
        return username;
    }

    /**
     * Returns the game ID for a user (if they are in a game)
     * @param uID id of user
     * @return gID
     */
    public static int getGID( int uID ){
        int gID = -1;
        try {
            //Class.forName("org.sqlite.JDBC");
            //Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            ResultSet rs;

            String sql = "select * from players where uID = '"+uID+"';";
            rs = stmnt.executeQuery(sql);

            if( rs.next() ){
                gID  = rs.getInt("gID");
            } else {
                System.err.println("gID not found for "+uID+" in table players");
            }
            rs.close();
            stmnt.close();
            //db.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
        return gID;
    }

    public static void changeTurns( int gID ){
        try {
            Statement stmnt = db.createStatement();
            // first get the games players
            String sql = "select * from games where gID="+gID+";";
            ResultSet rs = stmnt.executeQuery(sql);
            int[] uIDs;
            if( rs.next() ){
                int numPlayers = rs.getInt("numPlayers");
                uIDs = new int[numPlayers];
                for( int i=0; i<numPlayers; i++ ){
                    String user = "user"+(i+1);
                    uIDs[i] = rs.getInt(user);
                }
            } else {
                System.err.println("Error: gID "+gID+" not found");
                return;
            }
            rs.close();
            stmnt.close();
            // change their order
            for( int i=0; i<uIDs.length-1; i++ ){
                int j = (i == 0) ? uIDs.length-1 : i-1;
                int temp = uIDs[j];
                uIDs[j] = uIDs[i];
                uIDs[i] = temp;
            }
            // now update the games players
            for( int i=0; i<uIDs.length; i++ ){
                stmnt = db.createStatement();
                sql = "update games set user"+(i+1)+"="+uIDs[i]+" where gID="+gID+";";
                stmnt.executeUpdate(sql);
                stmnt.close();
            }
            db.commit();
            System.out.println("changed turns in game "+gID);
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * Creates a new game with a single user
     * @param uID id of first player 
     * @param map details of game (ex number of players)
     */
    public static void createGame( int uID, 
            HashMap<String, Object> map ){
        try { 
            //Class.forName("org.sqlite.JDBC");
            //Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();

            int gameSize = (Integer)map.get("gameSize");

            String sql = "insert into games(gameSize, numPlayers, phaseNum, user1)"
                + "values("+gameSize+","+1+","+0+","+uID+");";
            stmnt.executeUpdate(sql);
            stmnt.close();

            stmnt = db.createStatement();
            sql = "select * from games where User1 = '"+uID+"';";
            ResultSet rs = stmnt.executeQuery(sql);
            rs.next();
            int gID = rs.getInt("gID");
            rs.close();
            stmnt.close();

            stmnt = db.createStatement();
            sql = "insert into players(uID, gID, color, gold)"
                + "values("+uID+","+gID+",'YELLOW',"+10+");";
            stmnt.executeUpdate(sql);
            stmnt.close();
            db.commit();
            //db.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
        System.out.println("new game created!");
    }

    /**
     * Adds a user to a specific game
     * @param uID the id of the user
     * @param gID the id of the specified game
     * @return true if successful, false if no free game to join
     */
    public static boolean joinSpecifcGame( int uID, int gID, 
            HashMap<String,Object> map ){
        try { 
            //Class.forName("org.sqlite.JDBC");
            //Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            ResultSet rs;

            String color = "";

            String sql = "select * from games where"
                + "gID = '"+gID+"';";
            rs = stmnt.executeQuery(sql);

            // check if the game exists
            if( rs.next() ){
                int numPlayers = rs.getInt("numPlayers");
                int gameSize = rs.getInt("gameSize");
                stmnt.close();
                rs.close();

                // check if there is room for another player
                if( numPlayers < gameSize ){
                    numPlayers++;
                    stmnt = db.createStatement();
                    String playerNum = "User"+numPlayers;
                    sql = "update games set "+playerNum+"= "+uID+", numPlayers="+numPlayers
                        + "where gID = "+gID+";"; 
                    stmnt.executeUpdate(sql);
                    stmnt.close();
                    // decide color for player
                    switch( numPlayers ){
                        case 2:
                            color = "BLUE";
                            break;
                        case 3:
                            color = "GREEN";
                            break;
                        case 4:
                            color = "RED";
                            break;
                    }
                } else {
                    //db.close();
                    return false;
                }
            } else {
                rs.close();
                stmnt.close();
                //db.close();
                return false;
            }


            // add the user to the players table
            sql = "insert into players(uID, gID, color, gold)"
                + "values("+uID+","+gID+",'"+color+"',"+10+");";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();
            db.commit();
            //db.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Adds a user to any free game
     * @param uID id for user
     * @param gameSize the number of players user would like to play with
     * @return true if added to a game, false if created a new game
     */
    public static boolean joinGame( int uID, 
            HashMap<String,Object> map ){
        boolean found = false;
        try { 
            //Class.forName("org.sqlite.JDBC");
            //Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            ResultSet rs;

            int gameSize = (Integer)map.get("gameSize");
            String color = "";

            String sql = "select * from games where gameSize = '"+gameSize+"';";
            rs = stmnt.executeQuery(sql);
            System.out.println("checking for a free game");
            // find the first open game with less players than the gameSize
            while( rs.next() ){
                int numPlayers = rs.getInt("numPlayers");
                int gID = rs.getInt("gID");
                System.out.println("checking game "+gID+"...");

                // check if there is room for another player
                if( numPlayers < gameSize ){
                    rs.close();
                    stmnt.close();
                    numPlayers++;
                    stmnt = db.createStatement();
                    String playerNum = "user"+numPlayers;
                    sql = "update games set "+playerNum+"="+uID+", numPlayers="+numPlayers
                        + " where gID="+gID+";"; 
                    System.out.println(sql);
                    stmnt.executeUpdate(sql);
                    stmnt.close();

                    // decide color for player
                    switch( numPlayers ){
                        case 2:
                            color = "BLUE";
                            break;
                        case 3:
                            color = "GREEN";
                            break;
                        case 4:
                            color = "RED";
                            break;
                    }

                    // add the user to the players table
                    stmnt = db.createStatement();
                    sql = "insert into players(uID, gID, color, gold)"
                        + "values("+uID+","+gID+",'"+color+"',"+10+");";
                    stmnt.executeUpdate(sql);
                    stmnt.close();
                    found = true;
                    System.out.println("joining game with gID: "+gID);
                    break;
                } 
            }
            if( !found ){
                rs.close();
                stmnt.close();
                System.out.println("no free games to join");
            }
            db.commit();
        } catch( Exception e ){
            e.printStackTrace();
        }
        return found;
    }

    public static ArrayList<Integer> getPIDsInPlayerRack( int gID, int uID ){
        ArrayList<Integer> pIDs = new ArrayList<Integer>();
        try {
            Statement stmnt = db.createStatement();
            String sql = "select * from playerRacks where gID="+gID
                + " and uID="+uID+";";
            ResultSet rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                pIDs.add(rs.getInt("pID"));
            }
        } catch( Exception e ){
            e.printStackTrace();
        }
        return pIDs;
    }

    public static void addPieceToPlayerRack( int gID, int uID, int pID ){
        try {
            Statement stmnt = db.createStatement();
            String sql = "";

            sql = "insert into playerRacks (uID, gID, pID)"
                + "values ("+uID+","+gID+","+pID+");";
            stmnt.executeUpdate(sql);

            stmnt.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void removePieceFromPlayerRack( int gID, int uID, int pID ){
        try {
            Statement stmnt = db.createStatement();
            String sql = "delete from playerRacks where "
                + "gID="+gID+" and uID="+uID+" and pID="+pID+";";

        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static HashMap<String,Object> getPlayerRack( int gID, int uID ){
        HashMap<String,Object> rack = new HashMap<String,Object>();
        ArrayList<Integer> pIDs = new ArrayList<Integer>();
        try {
            Statement stmnt = db.createStatement();
            ResultSet rs;
            String sql;

            // add creatures 
            sql = "select creatures.pID,fIMG,bIMG,orientation,type,name,combatVal,flying,magic,ranged,charging,terrain "
                + "from playerRacks natural join pieces, creatures "
                + "on creatures.pID=pieces.pID where playerRacks.gID='"+gID+"';";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                Integer pID = rs.getInt("pID");
                pIDs.add(pID);
                HashMap<String,Object> creature = new HashMap<String,Object>();
                creature.put("pID", pID);
                creature.put("fIMG",rs.getString("fIMG"));
                creature.put("bIMG", rs.getString("bIMG"));
                creature.put("orientation", rs.getInt("orientation"));
                creature.put("type", rs.getString("type"));
                creature.put("name", rs.getString("name"));
                creature.put("combatVal", rs.getInt("combatVal"));
                creature.put("flying", rs.getInt("flying"));
                creature.put("magic", rs.getInt("magic"));
                creature.put("ranged", rs.getInt("ranged"));
                creature.put("charging", rs.getInt("charging"));
                creature.put("terrain", rs.getString("terrain"));
                rack.put(""+pID, creature);
            }

            // add special characters 
            sql = "select specialCharacters.pID,fIMG,bIMG,type,name,combatVal,flying,magic,ranged,charging "
                + "from playerRacks natural join pieces, specialCharacters "
                + "on specialCharacters.pID=pieces.pID where playerRacks.gID='"+gID+"';";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                Integer pID = rs.getInt("pID");
                pIDs.add(pID);
                HashMap<String,Object> specChar = new HashMap<String,Object>();
                specChar.put("pID", pID);
                specChar.put("fIMG",rs.getString("fIMG"));
                specChar.put("bIMG", rs.getString("bIMG"));
                specChar.put("type", rs.getString("type"));
                specChar.put("name", rs.getString("name"));
                specChar.put("combatVal", rs.getInt("combatVal"));
                specChar.put("flying", rs.getInt("flying"));
                specChar.put("magic", rs.getInt("magic"));
                specChar.put("ranged", rs.getInt("ranged"));
                specChar.put("charging", rs.getInt("charging"));
                rack.put(""+pID, specChar);
            }

            // add random events
            sql = "select randomEvents.pID,fIMG,bIMG,type,name,owner "
                + "from playerRacks natural join pieces, randomEvents "
                + "on randomEvents.pID=pieces.pID where playerRacks.gID='"+gID+"';";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                Integer pID = rs.getInt("pID");
                pIDs.add(pID);
                HashMap<String,Object> ranEvent = new HashMap<String,Object>();
                ranEvent.put("pID", pID);
                ranEvent.put("fIMG", rs.getString("fIMG"));
                ranEvent.put("bIMG", rs.getString("bIMG"));
                ranEvent.put("type", rs.getString("type"));
                ranEvent.put("name", rs.getString("name"));
                ranEvent.put("owner", rs.getString("owner"));
                rack.put(""+pID, ranEvent);
            }

            // add magic events
            sql = "select magicEvents.pID,fIMG,bIMG,type,name,owner "
                + "from playerRacks natural join pieces, magicEvents "
                + "on magicEvents.pID=pieces.pID where playerRacks.gID='"+gID+"';";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                Integer pID = rs.getInt("pID");
                pIDs.add(pID);
                HashMap<String,Object> magEvent = new HashMap<String,Object>();
                magEvent.put("pID", pID);
                magEvent.put("fIMG", rs.getString("fIMG"));
                magEvent.put("bIMG", rs.getString("bIMG"));
                magEvent.put("type", rs.getString("type"));
                magEvent.put("name", rs.getString("name"));
                magEvent.put("owner", rs.getString("owner"));
                rack.put(""+pID, magEvent);
            }

            // add income counters
            sql = "select specialIncomes.pID,fIMG,bIMG,type,name,value,treasure "
                + " from playerRacks natural join pieces, specialIncomes "
                + "on specialIncomes.pID=pieces.pID where playerRacks.gID='"+gID+"';";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                Integer pID = rs.getInt("pID");
                pIDs.add(pID);
                HashMap<String,Object> income = new HashMap<String,Object>();
                income.put("pID", pID);
                income.put("fIMG", rs.getString("fIMG"));
                income.put("bIMG", rs.getString("bIMG"));
                income.put("type", rs.getString("type"));
                income.put("name", rs.getString("name"));
                income.put("value", rs.getInt("value"));
                income.put("treasure", rs.getInt("treasure"));
                rack.put(""+pID, income);
            }
            rack.put("pIDs", pIDs);
        } catch( Exception e ){
            e.printStackTrace();
        }
        return rack;
    }

    public static void updateCreatureForPlayer( String username,
            HashMap<String,Object> map ){
    }

    public static void updatePhaseNumber( int gID, int phaseNum ){
        try {
            Statement stmnt = db.createStatement();
            String sql = "update games set phaseNum="+phaseNum+" where gID="+gID+";";
            stmnt.executeUpdate(sql);
            stmnt.close();
            db.commit();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static int getPhaseNumber( int gID ){
        int phaseNum = -5;
        try {
            Statement stmnt = db.createStatement();
            String sql = "select * from games where gID="+gID+";";
            ResultSet rs = stmnt.executeQuery(sql);
            if( rs.next() ){
                phaseNum = rs.getInt("phaseNum");
            } else {
                System.err.println("Error no game found for gID="+gID);
            }
            stmnt.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
        return phaseNum;
    }

    /**
     * Adds the details of the game to a HashMap, which may be the Body of a Message
     * @param map the HashMap to add contents to
     * @param gID the id of the game
     * @param uID the id of the user requesting game state
     */
    public static void getGameState( HashMap<String,Object> map, int gID, int uID ){
        try {
            long time = System.currentTimeMillis();
            Statement stmnt = db.createStatement();
            ResultSet rs;
            String sql = "";

            // add details of all users in game (their names and gold)
            stmnt = db.createStatement();
            sql = "select * from games where gID='"+gID+"';";
            rs = stmnt.executeQuery(sql);

            // also add these details about the game
            int gameSize = rs.getInt("gameSize");
            int numPlayers = rs.getInt("numPlayers");
            int phaseNum = rs.getInt("phaseNum");
            map.put("gameSize", gameSize);
            map.put("numPlayers", numPlayers);
            map.put("phaseNum", phaseNum);

            int uIDs[] = new int[numPlayers];
            for( int i=0; i<numPlayers; i++ ){
                uIDs[i] = rs.getInt("User"+(i+1));
            }
            rs.close();
            stmnt.close();

            for( int i=0; i<numPlayers; i++ ){
                stmnt = db.createStatement();
                sql = "select * from players, users where " 
                    + "players.uID = users.uID and users.uID = "+uIDs[i]+";";
                rs = stmnt.executeQuery(sql);
                rs.next();
                HashMap<String,Object> playerInfo = new HashMap<String,Object>();
                playerInfo.put("username", rs.getString("username"));
                playerInfo.put("color", rs.getString("color"));
                playerInfo.put("gold", rs.getInt("gold"));
                map.put("Player"+(i+1), playerInfo);
                rs.close();
                stmnt.close();
                stmnt = db.createStatement();
                sql = "select count(*) from playerRacks where gID="+gID
                	+ " and uID ="+uIDs[i]+";";
                rs = stmnt.executeQuery(sql);
                if( rs.next() ){
                	playerInfo.put("numOnRack", rs.getInt(1));
                }
                stmnt.close();
                rs.close();
            }

            /* next add all items in the cup */
            ArrayList<Integer> cupData = new ArrayList<Integer>();

            // add creatures 
            sql = "select creatures.pID,fIMG,bIMG,orientation,type,name,combatVal,flying,magic,ranged,charging,terrain "
                + "from cups natural join pieces, creatures "
                + "on creatures.pID=pieces.pID where cups.gID='"+gID+"';";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                Integer pID = rs.getInt("pID");
                cupData.add(pID);
                HashMap<String,Object> creature = new HashMap<String,Object>();
                creature.put("pID", pID);
                creature.put("fIMG",rs.getString("fIMG"));
                creature.put("bIMG", rs.getString("bIMG"));
                creature.put("orientation", rs.getInt("orientation"));
                creature.put("type", rs.getString("type"));
                creature.put("name", rs.getString("name"));
                creature.put("combatVal", rs.getInt("combatVal"));
                creature.put("flying", rs.getInt("flying"));
                creature.put("magic", rs.getInt("magic"));
                creature.put("ranged", rs.getInt("ranged"));
                creature.put("charging", rs.getInt("charging"));
                creature.put("terrain", rs.getString("terrain"));
                map.put(""+pID, creature);
            }

            // add special characters 
            sql = "select specialCharacters.pID,fIMG,bIMG,type,name,combatVal,flying,magic,ranged,charging "
                + "from cups natural join pieces, specialCharacters "
                + "on specialCharacters.pID=pieces.pID where cups.gID='"+gID+"';";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                Integer pID = rs.getInt("pID");
                cupData.add(pID);
                HashMap<String,Object> specChar = new HashMap<String,Object>();
                specChar.put("pID", pID);
                specChar.put("fIMG",rs.getString("fIMG"));
                specChar.put("bIMG", rs.getString("bIMG"));
                specChar.put("type", rs.getString("type"));
                specChar.put("name", rs.getString("name"));
                specChar.put("combatVal", rs.getInt("combatVal"));
                specChar.put("flying", rs.getInt("flying"));
                specChar.put("magic", rs.getInt("magic"));
                specChar.put("ranged", rs.getInt("ranged"));
                specChar.put("charging", rs.getInt("charging"));
                map.put(""+pID, specChar);
            }

            // add random events
            sql = "select randomEvents.pID,fIMG,bIMG,type,name,owner "
                + "from cups natural join pieces, randomEvents "
                + "on randomEvents.pID=pieces.pID where cups.gID='"+gID+"';";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                Integer pID = rs.getInt("pID");
                cupData.add(pID);
                HashMap<String,Object> ranEvent = new HashMap<String,Object>();
                ranEvent.put("pID", pID);
                ranEvent.put("fIMG", rs.getString("fIMG"));
                ranEvent.put("bIMG", rs.getString("bIMG"));
                ranEvent.put("type", rs.getString("type"));
                ranEvent.put("name", rs.getString("name"));
                ranEvent.put("owner", rs.getString("owner"));
                map.put(""+pID, ranEvent);
            }

            // add magic events
            sql = "select magicEvents.pID,fIMG,bIMG,type,name,owner "
                + "from cups natural join pieces, magicEvents "
                + "on magicEvents.pID=pieces.pID where cups.gID='"+gID+"';";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                Integer pID = rs.getInt("pID");
                cupData.add(pID);
                HashMap<String,Object> magEvent = new HashMap<String,Object>();
                magEvent.put("pID", pID);
                magEvent.put("fIMG", rs.getString("fIMG"));
                magEvent.put("bIMG", rs.getString("bIMG"));
                magEvent.put("type", rs.getString("type"));
                magEvent.put("name", rs.getString("name"));
                magEvent.put("owner", rs.getString("owner"));
                map.put(""+pID, magEvent);
            }

            // add income counters
            sql = "select specialIncomes.pID,fIMG,bIMG,type,name,value,treasure "
                + " from cups natural join pieces, specialIncomes "
                + "on specialIncomes.pID=pieces.pID where cups.gID='"+gID+"';";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            while( rs.next() ){
                Integer pID = rs.getInt("pID");
                cupData.add(pID);
                HashMap<String,Object> income = new HashMap<String,Object>();
                income.put("pID", pID);
                income.put("fIMG", rs.getString("fIMG"));
                income.put("bIMG", rs.getString("bIMG"));
                income.put("type", rs.getString("type"));
                income.put("name", rs.getString("name"));
                income.put("value", rs.getInt("value"));
                income.put("treasure", rs.getInt("treasure"));
                map.put(""+pID, income);
            }

            map.put("cupData", cupData);

            // now get the user's player rack
            // TODO


            /* add the game board */

            // first add all tiles by their unique x,y,z 
            sql = "select * from tiles where gID="+gID+" group by x,y,z;";
            stmnt = db.createStatement();
            rs = stmnt.executeQuery(sql);
            ArrayList<HashMap<String,Object>> board = new ArrayList<HashMap<String,Object>>();
            while( rs.next() ){
                HashMap<String,Object> tile = new HashMap<String,Object>();
                tile.put("terrain", rs.getString("terrain"));
                tile.put("x", rs.getInt("x"));
                tile.put("y", rs.getInt("y"));
                tile.put("z", rs.getInt("z"));
                tile.put("tile_orient", rs.getInt("tile_orient"));
                tile.put("owner", ""+rs.getInt("owner"));
                board.add(tile);
            }
            rs.close();
            stmnt.close();

            // now add the uIDs, pIDs, and any forts
            for( HashMap<String,Object> tile : board ){
                int x = (Integer)tile.get("x");
                int y = (Integer)tile.get("y");
                int z = (Integer)tile.get("z");
                if( !tile.get("owner").equals("0") ){
                    tile.put("owner", getUsername(Integer.parseInt(""+tile.get("owner"))));
                }
                ArrayList<Integer> tileUIDs = new ArrayList<Integer>(); 
                HashMap<String,ArrayList<Integer>> pIDs = new HashMap<String,ArrayList<Integer>>(); 
                sql = "select * from tiles where gID="+gID+" "
                    + "and x="+x+" and y="+y+" and z="+z+";";
                stmnt = db.createStatement();
                rs = stmnt.executeQuery(sql);

                while( rs.next() ){
                    Integer user = rs.getInt("uID");
                    if( !user.equals(0) ){
                        if( !tileUIDs.contains(user) ){
                            tileUIDs.add(user);
                            pIDs.put(""+user, new ArrayList<Integer>());
                        }
                        pIDs.get(""+user).add(rs.getInt("pID"));
                    }
                }
                rs.close();
                stmnt.close();

                // add any forts contained on this tile
                sql = "select * from forts where gID="+gID
                    + " and x="+x+" and y="+y+" and z="+z+";";
                stmnt = db.createStatement();
                rs = stmnt.executeQuery(sql);

                if( rs.next() ){
                    // does contain a fort
                    HashMap<String,Object> fort = new HashMap<String,Object>();
                    fort.put("combatVal", rs.getInt("combatVal"));
                    fort.put("neutralized", rs.getInt("neutralized"));
                    tile.put("fort", fort);
                } 
                rs.close();
                stmnt.close();

                // now replace the pIDs and uIDs with actual pieces and usernames
                ArrayList<String> userNames = new ArrayList<String>();
                for( Iterator<Integer> it=tileUIDs.iterator(); it.hasNext(); ){
                	Integer user = it.next();
                    String name = getUsername(user);
                    for( Integer pID : pIDs.get(""+user) ){
                        HashMap<String,Object> piece = new HashMap<String,Object>();
                        sql = "select * from pieces where gID="+gID+" and pID="+pID+";";
                        stmnt = db.createStatement();
                        rs = stmnt.executeQuery(sql);
                        String type = "";
                        if( rs.next() ){
                            type = rs.getString("type");
                            piece.put("pID", pID);
                            piece.put("type", type);
                            piece.put("fIMG", rs.getString("fIMG"));
                            piece.put("bIMG", rs.getString("bIMG"));
                        } else {
                            System.err.println("Error: piece not found with gID="+gID+" and pID="+pID);
                        }
                        rs.close();
                        stmnt.close();
                        stmnt = db.createStatement();
                        if( type.equals("Creature") ){
                            sql = "select * from creatures where gID="+gID+" and pID="+pID+";";
                            rs = stmnt.executeQuery(sql);
                            if( rs.next() ){
                                piece.put("name", rs.getString("name"));
                                piece.put("combatVal", rs.getInt("combatVal"));
                                piece.put("flying", rs.getInt("flying"));
                                piece.put("ranged", rs.getInt("ranged"));
                                piece.put("magic", rs.getInt("ranged"));
                                piece.put("charging", rs.getInt("charging"));
                                piece.put("terrain", rs.getString("terrain"));
                                piece.put("orientation", rs.getInt("orientation"));
                                tile.put("piece_orient", rs.getInt("orientation"));
                                rs.close();
                            } else {
                                System.err.println("Error: creature not found with gID="+gID+" and pID="+pID);
                            }
                        } else if( type.equals("SpecialCharacter") || type.equals("TerrainLord") ){
                            sql = "select * from specialCharacters where gID="+gID+" and pID="+pID+";";
                            rs = stmnt.executeQuery(sql);
                            if( rs.next() ){
                                piece.put("name", rs.getString("name"));
                                piece.put("combatVal", rs.getInt("combatVal"));
                                piece.put("flying", rs.getInt("flying"));
                                piece.put("ranged", rs.getInt("ranged"));
                                piece.put("magic", rs.getInt("ranged"));
                                piece.put("charging", rs.getInt("charging"));
                                rs.close();
                            } else {
                                System.err.println("Error: specialCharacter not found with gID="+gID+" and pID="+pID);
                            }
                        } else if( type.equals("Special Income") ){
                            sql = "select * from specialIncomes where gID="+gID+" and pID="+pID+";";
                            rs = stmnt.executeQuery(sql);
                            if( rs.next() ){
                                piece.put("name", rs.getString("name"));
                                piece.put("value", rs.getInt("value"));
                                piece.put("treasure", rs.getInt("treasure"));
                                rs.close();
                            } else {
                                System.err.println("Error: specialIncome not found with gID="+gID+" and pID="+pID);
                            }
                        } else if( type.equals("Random Event") ){
                            sql = "select * from randomEvents where gID="+gID+" and pID="+pID+";";
                            rs = stmnt.executeQuery(sql);
                            if( rs.next() ){
                                piece.put("name", rs.getString("name"));
                                piece.put("owner", rs.getString("owner"));
                                rs.close();
                            } else {
                                System.err.println("Error: randomEvent not found with gID="+gID+" and pID="+pID);
                            }
                        } else if( type.equals("Magic Event") ){
                            sql = "select * from magicEvents where gID="+gID+" and pID="+pID+";";
                            rs = stmnt.executeQuery(sql);
                            if( rs.next() ){
                                piece.put("name", rs.getString("name"));
                                piece.put("owner", rs.getString("owner"));
                                rs.close();
                            } else {
                                System.err.println("Error: magicEvent not found with gID="+gID+" and pID="+pID);
                            }
                        } else {
                            System.err.println("Error retrieving piece: unrecognized piece type: "+type);
                        }
                        stmnt.close();
                        tile.put(""+pID, piece);
                        userNames.add(name);
//                        pIDs.put(name, pIDs.get(""+user)); // add list of pIDs to actual username
//                        pIDs.remove(""+user); // remove list of pIDs corresponding to temporary uID
                        tile.put(name, pIDs.get(""+user));
                    }
                    tile.put("players", userNames);
                }
            }
            map.put("board", board);

            // check for any combat taking place in battleGrounds
            // TODO

            // lastly add any off-side special characters
            // TODO

            //db.close();
            System.out.println("Retrieved game state in "+(System.currentTimeMillis()-time)+" milliseconds");
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void removeGame( int gID ){
        try {
            Statement stmnt = db.createStatement();
            String sql = "delete * from games where gID='"+gID+"';";
            stmnt.executeUpdate(sql);
            db.commit();
        } catch( SQLException e ){
            e.printStackTrace();
        }
    }

    public static Connection getConnection(){ return db; }

    public static void commit(){ 
        try {
            db.commit();
        } catch( SQLException e ){
            e.printStackTrace();
        } 
    }
}

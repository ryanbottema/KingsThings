// 
// KATDB.java
// kingsandthings/Server/
// @author Brandon Schurman
//
package KAT;

import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;


public class KATDB
{
    public static void setup(){
        dropTables();
        createTables();
    }

    private static void createTables(){
        try {
            // open db connection
            Class.forName("org.sqlite.JDBC");
            Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();


            /* create tables (if they do not already exist) */

            String sql;

            // users are identified by their unique username or uID
            sql = "create table if not exists users("
                + "uID integer primary key autoincrement,"
                + "username text unique not null);";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // the gID uniquely identifies a single game
            sql = "create table if not exists games("
                + "gID integer primary key autoincrement,"
                + "gameSize integer not null," // total number of players
                + "user1 integer not null," 
                + "user2 integer,"
                + "user3 integer," 
                + "user4 integer,"
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
                + "gID integer not null,"
                + "x integer not null,"
                + "y integer not null,"
                + "z integer not null,"
                + "terrain text not null,"
                + "orientation integer not null," // 1 for face-up, 0 face-down
                + "uID integer,"
                + "pID integer,"
                + "primary key(gID, x, y, z),"
                + "foreign key(gID) references games(gID),"
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
                + "gID integer primary key,"
                + "pID integer,"
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
                + "primary key(pID),"
                + "foreign key(pID) references pieces(pID),"
                + "foreign key(gID) references games(gID),"
                + "foreign key(terrain) references tiles(terrain));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();

            // special characters
            sql = "create table if not exists specialCharacters("
                + "pID integer not null,"
                + "name text not null,"
                + "combatVal integer,"
                + "flying integer," // boolean (1 or 0)
                + "ranged integer,"
                + "magic integer,"
                + "charging integer,"
                + "primary key(pID),"
                + "foreign key(pID) references pieces(pID));";
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();
            
            // forts are specific to an owner-hex
            sql = "create table if not exists forts("
                + "uID integer not null,"
                + "gID integer not null,"
                + "x integer not null,"
                + "y integer not null,"
                + "z integer not null,"
                + "combatVal integer not null,"
                + "neutralized integer not null," // 1 for true, else 0
                + "primary key(uID, gID, x, y, z),"
                + "foreign key(uID) references users(uID),"
                + "foreign key(gID) references games(gID),"
                + "foreign key(x) references tiles(x),"
                + "foreign key(y) references tiles(y),"
                + "foreign key(x) references tiles(z));"; 
            stmnt = db.createStatement();
            stmnt.executeUpdate(sql);
            stmnt.close();
        
            db.close();
        } catch( Exception e ){
            e.printStackTrace();
        } 
    }
    
    private static void dropTables(){
        try { 
            Class.forName("org.sqlite.JDBC");
            Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            String sql = "";

            /*
            sql = "drop table if exists users;";
            stmnt.executeUpdate(sql);
            */

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

            sql = "drop table if exists gameCreatures;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists gameSpecialCharacters;";
            stmnt.executeUpdate(sql);

            sql = "drop table if exists forts;";
            stmnt.executeUpdate(sql);

            stmnt.close();
            db.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void addPiece( String username, 
            HashMap<String,Object> piece ){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            String sql = "";

            int uID = getUID(username);
            int gID = getGID(uID);
            
            // add to generic piece table first
            sql = "insert into pieces(pID, gID, type, fIMG, bIMG)"
                + "values("+piece.get("pID")+","+gID+",'"+piece.get("type")
                + "','"+piece.get("fIMG")+"','"+piece.get("bIMG")+"');";
            stmnt.executeUpdate(sql);

            // add to correct table of piece type
            if( piece.get("type") == "Creature" ){
                sql = "insert into creatures(pID,gID,name,combatVal,"
                    + "flying,ranged,charging,magic)"
                    + "values("+piece.get("pID")+","+gID+",'"+piece.get("name")
                    + "',"+piece.get("combatVal")+","+piece.get("flying")
                    + ","+piece.get("ranged")+","+piece.get("charging")
                    + ","+piece.get("magic")+");";
                stmnt.executeUpdate(sql);
            } // else if ...
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void addAllPieces( String username, 
            ArrayList<HashMap<String,Object>> pieces ){
        // invoke addPiece on each piece in the list
        for( HashMap<String,Object> piece : pieces ){
            addPiece(username,piece);
        }
    }

    public static void addToCup( String username, 
            HashMap<String,Object> map ){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();

            int uID = getUID(username);
            int gID = getGID(uID);

            String sql = "insert into cups(gID, PID)"
                + "values("+gID+","+map.get("pID")+");";
            stmnt.executeUpdate(sql);

            stmnt.close();
            db.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void removeFromCup( String username, 
            HashMap<String,Object> map ){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();

            String sql = "delete from cups where pID = '"+map.get("pID")+"';";
            stmnt.executeUpdate(sql);

            stmnt.close();
            db.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static int getUID( String username ){
        int uID = -1;
        try { 
            Class.forName("org.sqlite.JDBC");
            Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            ResultSet rs;

            String sql = "select * from users where username = '"+username+"';";
            rs = stmnt.executeQuery(sql);
            rs.next();
            uID = rs.getInt("uID");

            stmnt.close();
            db.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
        return uID;
    }

    public static int getGID( int uID ){
        int gID = -1;
        try {
            Class.forName("org.sqlite.JDBC");
            Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            ResultSet rs;
            
            String sql = "select * from players where uID = '"+uID+"';";
            rs = stmnt.executeQuery(sql);

            if( rs.next() ){
                gID  = rs.getInt("gID");
            } else {
                System.err.println("gID not found for "+uID+" in table players");
            }
        } catch( Exception e ){
            e.printStackTrace();
        }
        return gID;
    }

    public static void addCreatureToPlayerRack( String username, 
            HashMap<String,Object> map ){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection db = DriverManager.getConnection("jdbc:sqlite:KAT.db");
            Statement stmnt = db.createStatement();
            String sql = "";
            ResultSet rs;

            int uID = getUID(username);
            int gID = getGID(uID);

            sql = "insert into playerRacks (uID, gID, pID)"
                + "values ("+uID+","+gID+","+map.get("pID")+");";
            stmnt.executeUpdate(sql);
           
            stmnt.close();
            db.close();
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static void updateCreatureForPlayer( String username,
            HashMap<String,Object> map ){
    }
    
    public static void removeGame( int gID ){
        // something like 'remove * from * where gID = @param gID
    }
}

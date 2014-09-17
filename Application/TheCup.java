package KAT;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

/*
 * Class to represent the game Cup and its functionality.
 * Uses the singleton class pattern.
 */
public class TheCup {
    //An ArrayList of pieces remaining in the cup
    private ArrayList<Piece>      remainingPieces;
    private ArrayList<Piece>      originalPieces;
    //A unique and single instance of this class, retrieved by getInstance()
    private static TheCup         uniqueInstance;

    private TheCup() {
        remainingPieces = new ArrayList<Piece>();
        originalPieces = new ArrayList<Piece>();
    }

    /**
     * @return the single unique instance of this class
     */
    public static TheCup getInstance(){
        if( uniqueInstance == null ){
            uniqueInstance = new TheCup();
        }
        return uniqueInstance;
    }

    public ArrayList<Piece> drawInitialPieces(int numberToDraw) {
        Random rand = new Random();
        ArrayList<Piece> pieces = new ArrayList<Piece>();
        if (remainingPieces.size() == 0) {
            System.out.println("No more pieces left to draw.");
            return null;
        }

        for (int i = 0; i < numberToDraw; i++) {
            int index = rand.nextInt(remainingPieces.size());
            pieces.add(remainingPieces.get(index));
            remainingPieces.remove(index);
        }
        
        // this method is only called when adding directly 
        // to player rack, so notify server of less pieces
        if( GameLoop.getInstance().isNetworked() ){
	        HashMap<String,Object> map = new HashMap<String,Object>();
	        map.put("updateType", "removeFromCup");
	        ArrayList<Integer> pIDs = new ArrayList<Integer>();
	        for( Piece p : pieces ){
	        	pIDs.add(p.getPID());
	        }
	        map.put("pIDs", pIDs);
	        NetworkGameLoop.getInstance().postGameState(map);
        }

        return pieces;
    }

    /*
     * Function to randomly draw pieces from the Cup. Returns an arraylist of the pieces drawn.
     */
    public HashMap<Integer,Integer> drawPieces(int numberToDraw) {
        Random rand = new Random();
        HashMap<Integer,Integer> pieces = new HashMap<Integer,Integer>();
        if (remainingPieces.size() == 0) {
            System.out.println("No more pieces left to draw.");
            return null;
        }

        for (int i = 0; i < numberToDraw; i++) {
            int index = rand.nextInt(remainingPieces.size());
            pieces.put(i,originalPieces.indexOf(remainingPieces.get(index)));
            remainingPieces.remove(index);
        }

        return pieces;
    }

    public ArrayList<Piece> draw(int numberToDraw) {
        Random rand = new Random();
        ArrayList<Piece> pieces = new ArrayList<Piece>();
        if (remainingPieces.size() == 0) {
            System.out.println("No more pieces left to draw");
            return null;
        }

        for (int i = 0; i < numberToDraw; i++) {
            int index = rand.nextInt(remainingPieces.size());
            pieces.add(remainingPieces.get(index));
            remainingPieces.remove(index);
        }

        return pieces;
    }

    /* 
     * This method fills the remainingPieces arraylist with all initial game pieces.
     */
    public void initCup() {
    	remainingPieces.clear();
        originalPieces.clear();

        BufferedReader inFile = null;
        try {
            //File used to read in the different creatures.
            inFile = new BufferedReader(new FileReader(System.getProperty("user.dir") + File.separator + "initCupCreatures.txt"));
            String line = null;
            while ((line = inFile.readLine()) != null) {
                Creature c = new Creature(line);
                remainingPieces.add(c);
                originalPieces.add(c);
            }
            inFile.close();
            //File used to read in the different special incomes.
            inFile = new BufferedReader(new FileReader(System.getProperty("user.dir") + File.separator + "initCupIncome.txt"));
            while ((line = inFile.readLine()) != null) {
                SpecialIncome s = new SpecialIncome(line);
                remainingPieces.add(s);
                originalPieces.add(s);
           }
           inFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found " + inFile);
        } catch (EOFException e) {
            System.out.println("EOF encountered");
        } catch (IOException e) {
            System.out.println("can't read from file");
        }
        //Adding the Random Events to the cup.
        BigJuju bj = new BigJuju();
        DarkPlague dp = new DarkPlague();
        Defection df = new Defection();
        GoodHarvest gh = new GoodHarvest();
        MotherLode ml = new MotherLode();
        Teeniepox tp = new Teeniepox();
        TerrainDisaster td = new TerrainDisaster();
        Vandals v = new Vandals();
        WeatherControl wc = new WeatherControl();
        WillingWorkers ww = new WillingWorkers();
        remainingPieces.add(bj);
        originalPieces.add(bj);
        remainingPieces.add(dp);
        originalPieces.add(dp);
        remainingPieces.add(df);
        originalPieces.add(df);
        remainingPieces.add(gh);
        originalPieces.add(gh);
        remainingPieces.add(ml);
        originalPieces.add(ml);
        remainingPieces.add(tp);
        originalPieces.add(tp);
        remainingPieces.add(td);
        originalPieces.add(td);
        remainingPieces.add(v);
        originalPieces.add(v);
        remainingPieces.add(wc);
        originalPieces.add(wc);
        remainingPieces.add(ww);
        originalPieces.add(ww);

        //Adding the Magic Events to the cup.
        Balloon b = new Balloon();
        Bow bo = new Bow();
        DispelMagicScroll dms = new DispelMagicScroll();
        DustOfDefense dod = new DustOfDefense();
        Elixir el = new Elixir();
        Fan f = new Fan();
        Firewall fw = new Firewall();
        Golem g = new Golem();
        LuckyCharm lc = new LuckyCharm();
        Sword s = new Sword();
        Talisman t = new Talisman();
        remainingPieces.add(b);
        originalPieces.add(b);
        remainingPieces.add(bo);
        originalPieces.add(bo);
        remainingPieces.add(dms);
        originalPieces.add(dms);
        remainingPieces.add(dod);
        originalPieces.add(dod);
        remainingPieces.add(el);
        originalPieces.add(el);
        remainingPieces.add(f);
        originalPieces.add(f);
        remainingPieces.add(fw);
        originalPieces.add(fw);
        remainingPieces.add(g);
        originalPieces.add(g);
        remainingPieces.add(lc);
        originalPieces.add(lc);
        remainingPieces.add(s);
        originalPieces.add(s);
        remainingPieces.add(t);
        originalPieces.add(t);
    }

    /*
     * Method to add a piece to the cup
     */
    public void addToCup(Piece p) {
        remainingPieces.add(p);
        if( GameLoop.getInstance().isNetworked() ){
        	HashMap<String,Object> map = new HashMap<String,Object>();
        	map.put("updateType", "addToCup");
        	ArrayList<Integer> pIDs = new ArrayList<Integer>();
        	pIDs.add(p.getPID());
        	map.put("pIDs", pIDs);
        	NetworkGameLoop.getInstance().postGameState(map);
        }
    }

    public void addToCup(ArrayList<Piece> p) {
        remainingPieces.addAll(p);
        if( GameLoop.getInstance().isNetworked() ){
        	ArrayList<Integer> pIDs = new ArrayList<Integer>();
        	for( Piece piece : p ){
        		pIDs.add(piece.getPID());
        	}
        	HashMap<String,Object> map = new HashMap<String,Object>();
        	map.put("updateType", "addToCup");
        	map.put("pIDs", pIDs);
        	NetworkGameLoop.getInstance().postGameState(map);
        }
    }

    //Might become useful when we start using "things"
    public ArrayList<String> printList(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<String>();
        for(String p: list) {
            newList.add(p);
        }
        return newList;
    }
    
    public boolean containsPiece( int pID ){
    	boolean contains = false;
    	for( Piece p : remainingPieces ){
    		if( p.getPID() == pID ){
    			contains = true;
    			break;
    		}
    	}
    	return contains;
    }
    
    public void removePiece( int pID ){
    	for( Piece p : remainingPieces ){
    		if( p.getPID() == pID ){
    			remainingPieces.remove(p);
    			break;
    		}
    	}
    }

    public ArrayList<Piece> getRemaining() { return remainingPieces; }
    public ArrayList<Piece> getOriginal() { return originalPieces; }
    public void setRemaining( ArrayList<Piece> newCup ){ remainingPieces = newCup; }
}

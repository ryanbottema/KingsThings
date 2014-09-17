package KAT;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;

/*
 * Class to represent Dice
 * 
 * Important note: Once the value of the roll is return to somewhere (Via getFinalVal())
 * 		The finalVal is turned to -1. So it can only be returned once per roll
 */

public class Dice {
	public static int val, finalVal;
	private static Random rand = new Random();
	private static int numRolls;
	private static int stepSpeed;
	private static int step;
	private static Thread rolling;

	public Dice() {
		this.val = 0;
		this.finalVal = -1;
	}

	/*
	 * @return int - random number between 1 and 6
	 */
	public static void roll() {
		
		finalVal = -1;
		numRolls = rand.nextInt(10) + 5;
		stepSpeed = (int)(200/numRolls);
		step = 10;
          
		rolling = new Thread(new Runnable() {
			@Override
            public void run() {
				
				for (int i = 0; i < numRolls; i++) {
					val = rand.nextInt(6) + 1;
					Platform.runLater(new Runnable() {
		                @Override
		                public void run() {
					    	updateGUI();
		                }
		            });
					try { rolling.sleep(step); } catch( Exception e ){ rolling.interrupt(); }
					stepRoll();
				}
				finalVal = val;
				DiceGUI.getInstance().cover();
				rolling.interrupt();
            }
		}, "Dice Thread");
		
		rolling.start();
	}
	
	private static void stepRoll() {
		step += stepSpeed;
	}
	
	private static void updateGUI() {
		DiceGUI.getInstance().setFaceValue(val);
	}
	
	public static int getFinalVal() { 
		int temp = finalVal;
		finalVal = -1;
		return temp;
	}
	
	public static void setFinalValMinusOne() {
		finalVal = -1;
	}
}
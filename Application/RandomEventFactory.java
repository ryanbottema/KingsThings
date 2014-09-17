package KAT;

public class RandomEventFactory
{
    public static RandomEvent createRandomEvent(String name){
        RandomEvent randomEvent = null;

        if(name.equals("BigJuJu")) {
        	randomEvent = new BigJuju();
        } 
        else if (name.equals("DarkPlague")) {
        	randomEvent = new DarkPlague();
        }
        else if (name.equals("Defection")) {
        	randomEvent = new Defection();
        }
        else if (name.equals("GoodHarvest")) {
        	randomEvent = new GoodHarvest();
        }
        else if (name.equals("MotherLode")) {
        	randomEvent = new MotherLode();
        }
        else if (name.equals("Teeniepox")) {
        	randomEvent = new Teeniepox();
        }
        else if (name.equals("TerrainDisaster")) {
        	randomEvent = new TerrainDisaster();
        }
        else if (name.equals("Vandals")) {
        	randomEvent = new Vandals();
        }
        else if (name.equals("WeatherControl")) {
        	randomEvent = new WeatherControl();
        }
        else if (name.equals("WillingWorkers")) {
        	randomEvent = new WillingWorkers();
        }

        return randomEvent;
    }
}

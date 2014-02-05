package geo.core;

/**
 * A Truck instance maintains the information 
 * of a truck used to move minerals from workfaces
 * to dump sites.
 * 
 * @author Dong
 * @version 1.0
 */
public class Truck {
	// 0-indexed truck ID
	private int id;
	// truck name
	private String name;
	// truck moving speed (m/h)
	private float velocity;
	// time consumption of one load of minerals (h)
	private float loadingTime;
	// amount of mineral one truck can load (ton)
	private float payload;
	
	public Truck(int i, String n, float v, float l, float p){
		id = i;
		name = n;
		velocity = v;
		loadingTime = l;
		payload = p;
	}
	
	public float getVelocity(){
		return velocity;
	}
	
	public float getLoadingTime(){
		return loadingTime;
	}
	
	public float getPayLoad(){
		return payload;
	}
	
	public String getTruckName(){
		return name;
	}
}

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
	
	/**
	 * Create a Truck instance by specifying the truck ID, name, velocity, loading time and payload
	 * @param i The ID of the truck.
	 * @param n The name of the truck.
	 * @param v The velocity of the truck.
	 * @param l The loading time of the truck.
	 * @param p The payload of the truck.
	 */
	public Truck(int i, String n, float v, float l, float p){
		id = i;
		name = n;
		velocity = v;
		loadingTime = l;
		payload = p;
	}
	
	/**
	 * Get the velocity of the truck.
	 * @return The velocity of the truck.
	 */
	public float getVelocity(){
		return velocity;
	}
	
	/**
	 * Get the loading time of the truck.
	 * @return The loading time of the truck.
	 */
	public float getLoadingTime(){
		return loadingTime;
	}
	
	/**
	 * Get the payload of the truck.
	 * @return The payload of the truck.
	 */
	public float getPayLoad(){
		return payload;
	}
	
	/**
	 * Get the truck name of the truck.
	 * @return
	 */
	public String getTruckName(){
		return name;
	}
}

package geo.core;

public class Truck {
	private int id;
	private String name;
	private float velocity;
	private float loadingTime;
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

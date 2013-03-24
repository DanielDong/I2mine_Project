package geo.core;

import java.util.Comparator;

public class DUComparator implements Comparator<DistanceUnit> {
	@Override
	public int compare(DistanceUnit du0, DistanceUnit du1) {
		// TODO Auto-generated method stub
		return (du0.distance > du1.distance) ? -1 : ( du0.distance == du1.distance ? 0 : 1);
	}
}

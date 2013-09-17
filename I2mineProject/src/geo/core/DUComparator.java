package geo.core;

import java.util.Comparator;

/**
 * This class implements a custom comparator for {@link DistanceUnit} class instances.
 * 
 * @author Dong
 * @version 1.0
 */
public class DUComparator implements Comparator<DistanceUnit> {
	@Override
	public int compare(DistanceUnit du0, DistanceUnit du1) {
		// TODO Auto-generated method stub
		return (du0.distance > du1.distance) ? 1 : ( du0.distance == du1.distance ? 0 : -1);
	}
}

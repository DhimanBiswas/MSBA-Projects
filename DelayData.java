package FlightDelay;

import com.google.common.collect.Maps;

import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder;
import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

import java.util.Iterator;
import java.util.Map;

public class DelayData {
	
	public static final int FEATURES = 100;
	private static final ConstantValueEncoder interceptEncoder = new ConstantValueEncoder("intercept");
	private static final FeatureVectorEncoder featureEncoder = new StaticWordValueEncoder("feature");

	private RandomAccessSparseVector vector;

	private Map<String, String> fields = Maps.newLinkedHashMap();

	public DelayData(Iterable<String> fieldNames, Iterable<String> values) {
		vector = new RandomAccessSparseVector(FEATURES);
	    Iterator<String> value = values.iterator();
	    interceptEncoder.addToVector("1", vector);
	    for (String name : fieldNames) {
	    	String fieldValue = value.next();
	    	fields.put(name, fieldValue);
	    	if (name.equals("Month") || name.equals("DayofMonth") || name.equals("DayOfWeek") ||
	    			name.equals("CRSDepTime") || name.equals("CRSArrTime") || name.equals("UniqueCarrier") ||
	    			name.equals("FlightNum") ||  name.equals("Origin") || name.equals("Dest") ||
	    			name.equals("Distance")) {
	    				featureEncoder.addToVector(name + ":" + fieldValue, 1, vector);
	    	} 
	    	else if (name.equals("Year") || name.equals("DepTime") || name.equals("ArrTime") || 
	    			name.equals("ActualElapsedTime") || name.equals("CRSElapsedTime") ||
	    			name.equals("ArrDelay") || name.equals("DepDelay") || name.equals("TailNum") ||
	    			name.equals("AirTime") || name.equals("TaxiIn") || name.equals("TaxiOut") ||
	    			name.equals("CancellationCode") || name.equals("CarrierDelay") || name.equals("WeatherDelay") ||
	    			name.equals("NASDelay") || name.equals("SecurityDelay") || name.equals("LateAircraftDelay") || 
	    			name.equals("DepartureDelay") || name.equals("ArrivalDelay") || name.equals("Cancelled") || 
	    			name.equals("Diverted") || name.equals("PartOfDay") || name.equals("PartOfWeek") || name.equals("DelayLabel")) {
	    				// ignore these for vectorizing
	    	} else {
	    		throw new IllegalArgumentException(String.format("Bad field name: %s", name));
	    	}
	    }
	}

	public Vector asVector() {
	    return vector;
	}

	public int getTarget() {
	    return fields.get("DelayLabel").equals("0") ? 0 : 1;
	}
}

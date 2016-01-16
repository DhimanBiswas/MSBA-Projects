package FlightDelay;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.AbstractIterator;
import com.google.common.io.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class DelayDataParser {
	
	private final Splitter onComma = Splitter.on(",").trimResults(CharMatcher.anyOf("\" ;"));
	private String resourceName;

	public DelayDataParser(String resourceName) throws IOException {
		this.resourceName = resourceName;
	}

	//@Override
	public Iterator<DelayData> iterator() {
		try {
			return new AbstractIterator<DelayData>() {
				//ClassLoader cl = this.getClass().getClassLoader();
	    	
				BufferedReader input = new BufferedReader(new InputStreamReader(Resources.getResource(resourceName).openStream()));
				Iterable<String> fieldNames = onComma.split(input.readLine());

				@Override
				protected DelayData computeNext() {
					try {
						String line = input.readLine();
						if (line == null) {
							return endOfData();
						}

					return new DelayData(fieldNames, onComma.split(line));
					}
					catch (IOException e) {
						throw new RuntimeException("Error reading data", e);
					}
				}
	        	};
		} 
		catch (IOException e) {
			throw new RuntimeException("Error reading data", e);
		}
	}
}

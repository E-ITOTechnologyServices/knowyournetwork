
/*
    Copyright (C) 2017 e-ito Technology Services GmbH
    e-mail: info@e-ito.de
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package hr.eito.model.ntv;

import java.util.HashMap;
import java.util.Map;

import hr.eito.kynkite.Buckets;
import hr.eito.kynkite.Buckets2;
import hr.eito.kynkite.JsonData;

/**
 * Encapsulates the Netflow data to be serialized back to the client.
 *
 * @author Hrvoje Zeljko
 */
public class NetflowReturnResult {

	private Map<String, int[]> counts;
	private Info info;

	/**
	 * Construct from a JsonData structure. Extract all necessary data into NetflowReturnResult
	 *
	 * @param jsonData
	 */
	public NetflowReturnResult(final JsonData jsonData) {
		counts = new HashMap<>();
		info = new Info();
		if (jsonData != null) {
			int maxValue = 0;
			final int numberOfDays = 7;
			// Iterate through all weeks and fill the map of weeks
			for (Buckets week : jsonData.getAggregations().getWeek().getBuckets()) {
				// Key name of the week
				String key = week.getKey_as_string();
				// Initialize value array to have all zeroes
				int[] valueArray = new int[numberOfDays];
				// Iterate through days and fill first members of array to days that have values
				for (int j = 0; j < numberOfDays; j++) {
					if(j < week.getDay().getBuckets().size()){
						Buckets2 day = week.getDay().getBuckets().get(j);
						// Check if we have new max value
						if(day.getDoc_count() > maxValue) {
							maxValue = day.getDoc_count();
						}
						valueArray[j] = day.getDoc_count();
					}
				}
				// Create map member for that week
				counts.put(key, valueArray);
			}
			info.setMaximum(maxValue);
			info.setSize(jsonData.getAggregations().getWeek().getBuckets().size());
		}
	}
	
	/**
	 * Getting map of counts
	 */
	public Map<String, int[]> getCounts() {
		return counts;
	}
	
	/**
	 * Getting info part for netflow
	 */
	public Info getInfo() {
		return info;
	}
	
}


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


package hr.eito.model.hostreport;

import hr.eito.model.NodeUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Encapsulates the host report details data to be serialized back to the client.
 *
 * @author Steve Chaplin
 */
public class HostDetailsReportReturnResult {

	private List<HostDetailsReportReturnResultData> data_ = new ArrayList<HostDetailsReportReturnResultData>();

	/**
	 * Construct from a Jackson JsonNode. Whilst this is not strongly typed, it is expedient
	 * from the point of view of creating result sets rapidly.
	 *
	 * @param json the JSON node tree
	 * @param descriptions a map of event name to description
	 * @param links a map of event name to ELK link
	 * @param offsets a map of event name to timestamp offset
	 */
	public HostDetailsReportReturnResult(final JsonNode json,
										 final Map<String, String> descriptions,
										 final Map<String, String> links,
										 final Map<String, Integer> offsets) {
		mapTree_(json, descriptions, links, offsets);
	}

	/**
	 * Get all the host record data.
	 *
	 * @return all the host data records
	 */
	 public List<HostDetailsReportReturnResultData> getData() {
		return data_;
	 }

	 /**
	  * Map the Jackson JsonNode data tree into result data. String values in the result
	  * data will be set to the empty string if the value is missing.
	  *
	  * @param tree the JSON to map
	 * @param descriptions a map of event name to description
	 * @param links a map of event name to ELK link
	 * @param offsets a map of event name to timestamp offset
	  */
	private void mapTree_(final JsonNode tree,
						  final Map<String, String> descriptions,
						  final Map<String, String> links,
						  final Map<String, Integer> offsets) {

		// Null parameters will result in no data being presented.
		if (null == descriptions) return;
		if (null == links) return;
		if (null == offsets) return;
		if (null == tree) return;

		final JsonNode aggregations = tree.get("aggregations");
		if (null == aggregations) return;
		final JsonNode events = aggregations.get("events");
		if (null == events) return;
		final JsonNode buckets = events.get("buckets");
		if (null == buckets) return;

		final Iterator<JsonNode> eventBuckets = buckets.elements();

		while (eventBuckets.hasNext()) {
			final JsonNode event = eventBuckets.next();

			HostDetailsReportReturnResultData hostData = new HostDetailsReportReturnResultData();
			final String eventType = NodeUtils.safeStringValue(event, "key");
			hostData.setEventType(eventType);

			final JsonNode minMax = event.get("minmax");
			if (null != minMax) {
				hostData.setCount(NodeUtils.safeIntValue(minMax, "count"));
				hostData.setFirstDetection(NodeUtils.safeStringValue(minMax, "min_as_string"));
				hostData.setLastDetection(NodeUtils.safeStringValue(minMax, "max_as_string"));

				// Need to set from additional data coming from event description table.
				hostData.setDescription(descriptions.get(eventType));
				hostData.setLink(links.get(eventType));
				hostData.setDateOffset(offsets.get(eventType));
			}

			addRecord(hostData);
		}
	}

	/**
	 * Add a host record to the report result set. Package private for test purposes.
	 *
	 * @param hostData the host record
	 */
	void addRecord(final HostDetailsReportReturnResultData hostData) {
		data_.add(hostData);
	}

	/**
	 * Default constructor. Package private for test purposes.
	 */
	HostDetailsReportReturnResult() {
	}
}


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
 * Encapsulates the host report data to be serialized back to the client.
 *
 * @author Steve Chaplin
 */
public class HostReportReturnResult {

	private Map<String, Integer> severities_ = null;
	private List<HostReportReturnResultData> data_ = new ArrayList<HostReportReturnResultData>();

	/**
	 * Construct from a Jackson JsonNode. Whilst this is not strongly typed, it is expedient
	 * from the point of view of creating result sets rapidly.
	 *
	 * @param severities a map of event name to severity
	 * @param json the JSON node tree
	 */
	public HostReportReturnResult(final JsonNode json, final Map<String, Integer> severities) {
		severities_ = severities;
		mapTree_(json);
	}

	/**
	 * Get all the host record data.
	 *
	 * @return all the host data records
	 */
	 public List<HostReportReturnResultData> getData() {
		return data_;
	 }

	 /**
	  * Map the Jackson JsonNode data tree into result data. String values in the result
	  * data will be set to the empty string if the value is missing.
	  *
	  * @param tree the JSON to map
	  */
	private void mapTree_(final JsonNode tree) {
		// We won't do anything if we weren't given a valid severity list.
		if (null == severities_) return;
		// We won't do anything if we weren't given a valid node tree.
		if (null == tree) return;

		// Careful that the nodes aren't missing.
		final JsonNode aggregations = tree.get("aggregations");
		if (null == aggregations) return;
		final JsonNode hosts = aggregations.get("hosts");
		if (null == hosts) return;
		final JsonNode hostBuckets = hosts.get("buckets");
		if (null == hostBuckets) return;

		final Iterator<JsonNode> hostRows = hostBuckets.elements();

		while (hostRows.hasNext()) {
			final JsonNode row = hostRows.next();
			final HostReportReturnResultData hostData = new HostReportReturnResultData();

			hostData.setHost(NodeUtils.safeStringValue(row, "key"));

			final JsonNode time = row.get("time");
			if (null != time) {
				hostData.setFirstDetection(NodeUtils.safeStringValue(time, "min_as_string"));
				hostData.setLastDetection(NodeUtils.safeStringValue(time, "max_as_string"));
			}

			final JsonNode events = row.get("events");
			if (null != events) {
				final JsonNode eventBuckets = events.get("buckets");
				if (null != eventBuckets) {

					Iterator<JsonNode> eventRows = eventBuckets.elements();

					while (eventRows.hasNext()) {
						JsonNode event = eventRows.next();

						final JsonNode key = event.get("key");
						if (null != key) {

							final String eventName = key.asText();

							// Look up this event name in the reference list to find the associated
							// severity.
							final Integer severity = severities_.get(eventName);
							hostData.addEvent(eventName, severity == null ? 0 : severity);
						}
					}
				}
			}

			addRecord(hostData);
		}
	}

	/**
	 * Add a host record to the report result set. Package private for test purposes.
	 *
	 * @param hostData the host record
	 */
	void addRecord(final HostReportReturnResultData hostData) {
		data_.add(hostData);
	}

	/**
	 * Default constructor. Package private for test purposes.
	 */
	HostReportReturnResult() {
	}
}

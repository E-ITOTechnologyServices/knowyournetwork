
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


package hr.eito.model.cmdb;

import hr.eito.model.NodeUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Encapsulates the CMDB report data to be serialized back to the client.
 *
 * @author Steve Chaplin
 */
public class CMDBReturnResult {

	private List<CMDBReturnResultData> data_ = new ArrayList<CMDBReturnResultData>();

	/**
	 * Construct from a Jackson JsonNode. Whilst this is not strongly typed, it is expedient
	 * from the point of view of creating result sets rapidly.
	 *
	 * @param json the JSON node tree
	 */
	public CMDBReturnResult(final JsonNode json) {
		mapTree_(json);
	}

	/**
	 * Get all the report record data.
	 *
	 * @return all the report data records
	 */
	 public List<CMDBReturnResultData> getData() {
		return data_;
	 }

	 /**
	  * Map the Jackson JsonNode data tree into result data. String values in the result
	  * data will be set to null if the value is missing.
	  *
	  * @param tree the JSON to map
	  */
	private void mapTree_(final JsonNode tree) {
		if (null == tree) return;

		// If the node tree is invalid or we didn't find any nodes, then the array of
		// data will be left empty.
		final JsonNode hitsRoot = tree.get("hits");
		if (null == hitsRoot) return;

		final JsonNode hits = hitsRoot.get("hits");
		if (null == hits) return;

		final Iterator<JsonNode> hitEntries = hits.elements();

		while (hitEntries.hasNext()) {
			final JsonNode hit = hitEntries.next();
			final JsonNode _source = hit.get("_source");
			if (null == _source) continue;

			final JsonNode ipam = _source.get("ipam");
			if (null == ipam) continue;

			final Iterator<JsonNode> entries = ipam.elements();

			while (entries.hasNext()) {
				final JsonNode entry = entries.next();
				final CMDBReturnResultData hostData = new CMDBReturnResultData();

				// Some values are defined as mandatory. If they are not present, then we
				// don't create the record.
				final JsonNode ip = entry.get("ip");
				if (null == ip) continue;
				hostData.setIP(ip.asText());

				final JsonNode mask = entry.get("mask");
				if (null == mask) continue;
				hostData.setMask(mask.asText());

				final JsonNode maskCidr = entry.get("mask_cidr");
				if (null == maskCidr || ! maskCidr.canConvertToInt()) continue;
				hostData.setMaskCidr(maskCidr.asInt());

				final JsonNode source = entry.get("source");
				if (null == source) continue;
				hostData.setSource(source.asText());

				// Following are optional, we will create the record and they may be null.
				hostData.setName(NodeUtils.safeStringValue(entry, "name", null));
				hostData.setCity(NodeUtils.safeStringValue(entry, "city", null));
				hostData.setCountry(NodeUtils.safeStringValue(entry, "country", null));

				addRecord(hostData);
			}
		}
	}

	/**
	 * Add a host record to the report result set. Package private for test purposes.
	 *
	 * @param hostData the host record
	 */
	void addRecord(final CMDBReturnResultData hostData) {
		data_.add(hostData);
	}

	/**
	 * Default constructor. Package private for test purposes.
	 */
	CMDBReturnResult() {
	}
}

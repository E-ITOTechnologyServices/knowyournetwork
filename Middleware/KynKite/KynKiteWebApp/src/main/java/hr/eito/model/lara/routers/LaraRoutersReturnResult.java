
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


package hr.eito.model.lara.routers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import hr.eito.model.NodeUtils;

/**
 * Return data for Lara router IP list
 * <p>Besides list of return data, it contains record statistics
 * 
 * @author Hrvoje
 *
 */
public class LaraRoutersReturnResult {
	
	private int recordsTotal;
	private int recordsFiltered;
	private List<LaraRoutersReturnResultData> data;
	
	/**
	 * Receive list of Lara routers as JSON, extract IPs
	 * 
	 * @param result Lara Routers DB query records
	 */
	public LaraRoutersReturnResult(final JsonNode tree){
		this.data = new ArrayList<>();

		this.recordsFiltered = 0;
		this.recordsTotal = 0;
		
		// We won't do anything if we weren't given a valid node tree.
		if (null == tree) return;

		// Careful that the nodes aren't missing.
		final JsonNode aggregations = tree.get("aggregations");
		if (null == aggregations) return;
		final JsonNode distinctRouters = aggregations.get("distinct_routers");
		if (null == distinctRouters) return;
		final JsonNode distinctRoutersBuckets = distinctRouters.get("buckets");
		if (null == distinctRoutersBuckets) return;

		final Iterator<JsonNode> distinctRoutersRows = distinctRoutersBuckets.elements();

		while (distinctRoutersRows.hasNext()) {
			final JsonNode row = distinctRoutersRows.next();
			final LaraRoutersReturnResultData routerData = new LaraRoutersReturnResultData();

			routerData.setRouterIP(NodeUtils.safeStringValue(row, "key"));

			addRecord(routerData);
			this.recordsFiltered++;
			this.recordsTotal++;
		}
	}
	
	/**
	 * Add a router record to the report result set. Package private for test purposes.
	 *
	 * @param laraRouter the router record
	 */
	private void addRecord(final LaraRoutersReturnResultData laraRouter) {
		data.add(laraRouter);
	}

	/**
	 * @return the recordsTotal
	 */
	public int getRecordsTotal() {
		return recordsTotal;
	}

	/**
	 * @return the recordsFiltered
	 */
	public int getRecordsFiltered() {
		return recordsFiltered;
	}

	/**
	 * @return the data
	 */
	public List<LaraRoutersReturnResultData> getData() {
		return data;
	}
}

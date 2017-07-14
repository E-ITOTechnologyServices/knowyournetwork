
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


package hr.eito.model.syslog;

import java.util.ArrayList;
import java.util.List;

import hr.eito.model.QueryResult;
import hr.eito.model.Hit;

/**
 * Encapsulates a Syslog Router return result set.
 *
 * @author Danijel Soltic
 */
public class SyslogRouterReturnResult {
	
	private int recordsTotal;
	private int recordsFiltered;
	private List<SyslogRouterReturnResultData> data;
	
	/**
	 * Construct a return result set from a query result set.
	 *
	 * @param result the query result set
	 */
	public SyslogRouterReturnResult(final QueryResult<SyslogRouterQueryResultField> result){
		this.data = new ArrayList<SyslogRouterReturnResultData>();
		
		this.recordsFiltered = 0;
		this.recordsTotal = 0;
		
		if(result.getHits() != null && result.getHits().getHits() != null){

			recordsFiltered = result.getHits().getHits().size();
			recordsTotal = result.getHits().getTotal();

			for(Hit<SyslogRouterQueryResultField> hit : result.getHits().getHits()){
				SyslogRouterReturnResultData d = new SyslogRouterReturnResultData();
				
				if(hit.getData() != null){
					d.setCisco_message(hit.getData().getCisco_message());
					d.setRouter(hit.getData().getRouter());
					d.setTimestamp(hit.getData().getTimestamp());
				}
				
				data.add(d);
			}
		}
	}

	/**
	 * Find out how many records could have been returned.
	 *
	 * @return the recordsTotal
	 */
	public int getRecordsTotal() {
		return recordsTotal;
	}

	/**
	 * Find out how many reocrds were returned.
	 *
	 * @return the recordsFiltered
	 */
	public int getRecordsFiltered() {
		return recordsFiltered;
	}

	/**
	 * Get the payload.
	 *
	 * @return the data
	 */
	public List<SyslogRouterReturnResultData> getData() {
		return data;
	}
}

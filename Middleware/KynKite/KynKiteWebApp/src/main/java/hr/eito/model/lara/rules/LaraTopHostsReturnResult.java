
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


package hr.eito.model.lara.rules;

import java.util.ArrayList;
import java.util.List;

import hr.eito.model.QueryResult;
import hr.eito.model.lara.rules.dto.HostsRecord;
import hr.eito.model.Hit;

/**
 * Return data for Lara Rules top hosts
 * <p>Besides list of return data, it contains record statistics
 * 
 * @author Hrvoje
 *
 */
public class LaraTopHostsReturnResult {
	
	private int recordsTotal;
	private int recordsFiltered;
	private List<LaraTopHostsReturnResultData> data;
	
	/**
	 * Receive Lara Top Hosts DB query records, convert to Lara Top Hosts
	 * return records and save to return object
	 * 
	 * @param result Lara Top Hosts DB query records
	 */
	public LaraTopHostsReturnResult(QueryResult<LaraTopHostsQueryResultField> result){
		this.data = new ArrayList<>();
		
		this.recordsFiltered = 0;
		this.recordsTotal = 0;
		
		if(result.getHits() != null && result.getHits().getHits() != null){
			for(Hit<LaraTopHostsQueryResultField> hit : result.getHits().getHits()) {
				LaraTopHostsQueryResultField field = hit.getData();
				if (field != null && field.getTopHosts() != null) {
					for (HostsRecord hostRecord : field.getTopHosts()) {
						data.add(new LaraTopHostsReturnResultData(hostRecord));
					}
					this.recordsFiltered = field.getTopHosts().size();
					this.recordsTotal = field.getTopHosts().size();
				}
			}
		}
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
	public List<LaraTopHostsReturnResultData> getData() {
		return data;
	}
}

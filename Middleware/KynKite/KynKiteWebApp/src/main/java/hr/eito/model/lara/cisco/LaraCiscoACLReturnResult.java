
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


package hr.eito.model.lara.cisco;

import java.util.ArrayList;
import java.util.List;

import hr.eito.model.QueryResult;
import hr.eito.model.Hit;

/**
 * Class to encapsulate Lara Cisco ACL return records and statistics
 * 
 * @author Hrvoje
 *
 */
public class LaraCiscoACLReturnResult {
	
	private int recordsTotal;
	private int recordsFiltered;
	private List<LaraCiscoACLReturnResultData> data;
	
	/**
	 * Receive Lara Cisco ACL DB query records, convert to Lara Cisco ACL
	 * return records and save to return object
	 * 
	 * @param result Lara Cisco DB query records
	 */
	public LaraCiscoACLReturnResult(QueryResult<LaraCiscoACLQueryResultField> result){
		this.data = new ArrayList<LaraCiscoACLReturnResultData>();

		this.recordsFiltered = 0;
		this.recordsTotal = 0;
		
		if(result.getHits() != null && result.getHits().getHits() != null){

			recordsFiltered = result.getHits().getHits().size();
			recordsTotal = result.getHits().getTotal();

			for(Hit<LaraCiscoACLQueryResultField> hit : result.getHits().getHits()){
				data.add(new LaraCiscoACLReturnResultData(hit.getData()));
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
	public List<LaraCiscoACLReturnResultData> getData() {
		return data;
	}
}

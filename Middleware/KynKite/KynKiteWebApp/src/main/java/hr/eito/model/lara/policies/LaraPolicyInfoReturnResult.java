
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


package hr.eito.model.lara.policies;

import java.util.ArrayList;
import java.util.List;

import hr.eito.model.QueryResult;
import hr.eito.model.Hit;

/**
 * Represents all necessary return data for Lara list of policy informations
 * 
 * @author Hrvoje
 *
 */
public class LaraPolicyInfoReturnResult {

	private int recordsTotal;
	private int recordsFiltered;
	private List<LaraPolicyInfoReturnResultData> data;

	public LaraPolicyInfoReturnResult(final QueryResult<LaraPolicyInfoQueryResultField> result){
		data = new ArrayList<LaraPolicyInfoReturnResultData>();

		this.recordsFiltered = 0;
		this.recordsTotal = 0;

		if(result.getHits() != null && result.getHits().getHits() != null){

			recordsFiltered = result.getHits().getHits().size();
			recordsTotal = result.getHits().getTotal();

			for(Hit<LaraPolicyInfoQueryResultField> hit : result.getHits().getHits()){
				LaraPolicyInfoReturnResultData d = new LaraPolicyInfoReturnResultData();
				d.setId(hit.get_id());
				if(hit.getData() != null){
					d.setTimestamp(hit.getData().getTimestamp());
				}
				
				data.add(d);
				
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
	public List<LaraPolicyInfoReturnResultData> getData() {
		return data;
	}
}

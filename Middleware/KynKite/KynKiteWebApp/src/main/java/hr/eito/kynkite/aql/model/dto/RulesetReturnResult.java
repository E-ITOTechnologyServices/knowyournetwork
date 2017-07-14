
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


package hr.eito.kynkite.aql.model.dto;

import java.util.ArrayList;
import java.util.List;

import hr.eito.kynkite.aql.model.Ruleset;

/**
 * Class to encapsulate AQL Ruleset return records and statistics
 * 
 * @author Hrvoje
 *
 */
public class RulesetReturnResult {

	private int recordsTotal;
	private int recordsFiltered;
	private List<RulesetReturnResultData> data;
	
	/**
	 * Receive AQL Ruleset entity records, convert to AQL Ruleset
	 * return records and save to return object
	 * 
	 * @param result AQL Ruleset records
	 */
	public RulesetReturnResult(List<Ruleset> result){
		this.data = new ArrayList<RulesetReturnResultData>();

		this.recordsFiltered = 0;
		this.recordsTotal = 0;
		
		if(result != null) {
			recordsFiltered = result.size();
			recordsTotal = result.size();

			for(Ruleset ruleset : result){
				data.add(new RulesetReturnResultData(ruleset));
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
	public List<RulesetReturnResultData> getData() {
		return data;
	}
	
}

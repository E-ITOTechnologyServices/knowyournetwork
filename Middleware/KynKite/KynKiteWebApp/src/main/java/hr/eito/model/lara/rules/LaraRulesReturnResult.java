
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

import hr.eito.model.Hit;
import hr.eito.model.QueryResult;
import hr.eito.model.lara.rules.dto.RulesetDetails;

/**
 * Return data for Lara Rules
 * <p>Besides list of return data, it contains record statistics
 * 
 * @author Hrvoje
 *
 */
public class LaraRulesReturnResult {
	
	private int recordsTotal;
	private int recordsFiltered;
	private List<LaraRulesReturnResultData> data;
	
	/**
	 * Receive Lara Rules DB query records, convert to Lara Rules
	 * return records and save to return object
	 * 
	 * @param result Lara Rules DB query records
	 */
	public LaraRulesReturnResult(QueryResult<LaraRulesQueryResultField> result){
		this.data = new ArrayList<>();

		this.recordsFiltered = 0;
		this.recordsTotal = 0;
		
		if(result.getHits() != null && result.getHits().getHits() != null) {			
			for(Hit<LaraRulesQueryResultField> hit : result.getHits().getHits()) {
				LaraRulesQueryResultField field = hit.getData();
				if (field != null) {
					if (field.getRules()!=null && field.getRules().getRuleset()!=null) {
						if (field.getRules().getRuleset().getStaticRuleset() != null) {
							for (RulesetDetails rulesetDetails : field.getRules().getRuleset().getStaticRuleset()) {
								data.add(new LaraRulesReturnResultData(rulesetDetails));
							}
							this.recordsFiltered += field.getRules().getRuleset().getStaticRuleset().size();
							this.recordsTotal += field.getRules().getRuleset().getStaticRuleset().size();
						}
						if (field.getRules().getRuleset().getDynamicRuleset() != null) {
							for (RulesetDetails rulesetDetails : field.getRules().getRuleset().getDynamicRuleset()) {
								data.add(new LaraRulesReturnResultData(rulesetDetails));
							}
							this.recordsFiltered += field.getRules().getRuleset().getDynamicRuleset().size();
							this.recordsTotal += field.getRules().getRuleset().getDynamicRuleset().size();
						}
					}			
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
	public List<LaraRulesReturnResultData> getData() {
		return data;
	}
}

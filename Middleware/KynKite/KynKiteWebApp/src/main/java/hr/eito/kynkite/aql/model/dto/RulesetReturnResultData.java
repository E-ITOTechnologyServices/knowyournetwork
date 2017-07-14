
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

import hr.eito.kynkite.aql.model.Ruleset;

/**
 * Encapsulates return record for AQL Ruleset
 * 
 * @author Hrvoje
 *
 */
public class RulesetReturnResultData {

	private Integer id;
	private String rule;
	private String description;
	
	/**
	 * Constructor for creating RulesetReturnResultData based on Ruleset entity
	 * 
	 * @param entity
	 */
	public RulesetReturnResultData(Ruleset entity) {
		if (null != entity) {
			this.id = entity.getId();
			this.rule = entity.getRule();
			this.description = entity.getDescription();
		}
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getRule() {
		return rule;
	}
	
	public String getDescription() {
		return description;
	}
	
}


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


package hr.eito.kynkite.business.manager;

import hr.eito.kynkite.aql.model.AqlParams;
import hr.eito.kynkite.aql.model.dto.RulesetReturnResult;
import hr.eito.model.JsonReturnData;

/**
 * AQL manager interface for managing AQL requests and data
 * 
 * @author Hrvoje
 *
 */
public interface AQLManager {
	
	/**
	 * Manage AQL rules data
	 *
	 * @return list of AQL rules
	 */
	JsonReturnData<RulesetReturnResult> aqlRulesList();
	
	/**
	 * Manage adding new AQL rule
	 *
	 * @return added AQL rule
	 */
	JsonReturnData<RulesetReturnResult> addAqlRule(final AqlParams params);
	
	/**
	 * Manage editing AQL rule
	 *
	 * @return edited AQL rule
	 */
	JsonReturnData<RulesetReturnResult> editAqlRule(final AqlParams params);
	
	/**
	 * Manage deleting AQL rule
	 *
	 * @return success info
	 */
	JsonReturnData<String> deleteAqlRule(final AqlParams params);
	
}

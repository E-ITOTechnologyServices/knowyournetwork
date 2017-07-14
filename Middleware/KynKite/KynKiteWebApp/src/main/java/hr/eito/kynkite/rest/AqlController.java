
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


package hr.eito.kynkite.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hr.eito.kynkite.aql.model.AqlParams;
import hr.eito.kynkite.aql.model.dto.RulesetReturnResult;
import hr.eito.kynkite.business.manager.AQLManager;
import hr.eito.model.JsonReturnData;

/**
 * Rest endpoint for AQL inquiries
 *
 * @author Hrvoje
 *
 */
@RestController
public class AqlController {
	
	@Autowired
	private AQLManager aqlManager;
	
	/**
	 * Get list of AQL rules
	 * 
	 * @return AQL rules data as JSON.
	 */
	@RequestMapping(value = "/aql/rules/list", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<RulesetReturnResult> aqlRulesList() {
		JsonReturnData<RulesetReturnResult> aqlRules = aqlManager.aqlRulesList();
		return aqlRules;
	}
	
	/**
	 * Add new AQL rule
	 * 
	 * @return Added AQL rule as part of JSON
	 */
	@RequestMapping(value = "/aql/rules/add", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<RulesetReturnResult> addAqlRule(@RequestBody AqlParams params) {
		JsonReturnData<RulesetReturnResult> aqlRule = aqlManager.addAqlRule(params);
		return aqlRule;
	}
	
	/**
	 * Edit AQL rule
	 * 
	 * @return edited AQL rule as part of JSON
	 */
	@RequestMapping(value = "/aql/rules/edit", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<RulesetReturnResult> editAqlRule(@RequestBody AqlParams params) {
		JsonReturnData<RulesetReturnResult> aqlRule = aqlManager.editAqlRule(params);
		return aqlRule;
	}
	
	/**
	 * Delete AQL rule
	 * 
	 * @return success info as JSON
	 */
	@RequestMapping(value = "/aql/rules/delete", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<String> deleteAqlRule(@RequestBody AqlParams params) {
		JsonReturnData<String> successInfo = aqlManager.deleteAqlRule(params);
		return successInfo;
	}

}

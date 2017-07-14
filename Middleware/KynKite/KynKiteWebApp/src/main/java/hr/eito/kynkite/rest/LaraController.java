
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

import hr.eito.kynkite.business.manager.LaraManager;
import hr.eito.model.JsonReturnData;
import hr.eito.model.lara.LaraParams;
import hr.eito.model.lara.LaraPolicyInfoParams;
import hr.eito.model.lara.cisco.LaraCiscoACLReturnResult;
import hr.eito.model.lara.policies.LaraPolicyInfoReturnResult;
import hr.eito.model.lara.routers.LaraRoutersReturnResult;
import hr.eito.model.lara.rules.LaraRulesReturnResult;
import hr.eito.model.lara.rules.LaraTopHostsReturnResult;

/**
 * Rest endpoint for LARA inquiries
 *
 * @author Hrvoje
 *
 */
@RestController
public class LaraController {
	
	@Autowired
	private LaraManager laraManager;
	
	/**
	 * Lara Cisco ACL rest method
	 * 
	 * @param parameters to filter data with
	 *
	 * @return LARA Cisco data as JSON.
	 */
	@RequestMapping(value = "/laraCisco", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<LaraCiscoACLReturnResult> laraCisco(@RequestBody LaraParams params) {
		JsonReturnData<LaraCiscoACLReturnResult> laraCiscoJson = this.laraManager.laraCisco(params);
		return laraCiscoJson;
	}
	
	/**
	 * Lara Rules rest method
	 * 
	 * @param parameters to filter data with
	 *
	 * @return LARA Rules data as JSON.
	 */
	@RequestMapping(value = "/laraRules", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<LaraRulesReturnResult> laraRules(@RequestBody LaraParams params) {
		JsonReturnData<LaraRulesReturnResult> laraRulesJson = this.laraManager.laraRules(params);
		return laraRulesJson;
	}
	
	/**
	 * Lara Rules top sources for dynamic hosts
	 * 
	 * @param parameters to filter data with
	 *
	 * @return LARA Rules top sources for dynamic hosts data as JSON.
	 */
	@RequestMapping(value = "/laraRules/topSrcDynamic", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<LaraTopHostsReturnResult> laraTopSrcDynamic(@RequestBody LaraParams params) {
		JsonReturnData<LaraTopHostsReturnResult> laraTopSrcDynamicHosts = this.laraManager.laraTopSrcDynamic(params);
		return laraTopSrcDynamicHosts;
	}
	
	/**
	 * Lara Rules top destinations for dynamic hosts
	 * 
	 * @param parameters to filter data with
	 *
	 * @return LARA Rules top destinations for dynamic hosts data as JSON.
	 */
	@RequestMapping(value = "/laraRules/topDstDynamic", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<LaraTopHostsReturnResult> laraTopDstDynamic(@RequestBody LaraParams params) {
		JsonReturnData<LaraTopHostsReturnResult> laraTopDstDynamicHosts = this.laraManager.laraTopDstDynamic(params);
		return laraTopDstDynamicHosts;
	}
	
	/**
	 * Lara Rules top sources for static hosts
	 * 
	 * @param parameters to filter data with
	 *
	 * @return LARA Rules top sources for static hosts data as JSON.
	 */
	@RequestMapping(value = "/laraRules/topSrcStatic", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<LaraTopHostsReturnResult> laraTopSrcStatic(@RequestBody LaraParams params) {
		JsonReturnData<LaraTopHostsReturnResult> laraTopSrcStaticHosts = this.laraManager.laraTopSrcStatic(params);
		return laraTopSrcStaticHosts;
	}
	
	/**
	 * Lara Rules top destinations for static hosts
	 * 
	 * @param parameters to filter data with
	 *
	 * @return LARA Rules top destinations for static hosts data as JSON.
	 */
	@RequestMapping(value = "/laraRules/topDstStatic", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<LaraTopHostsReturnResult> laraTopDstStatic(@RequestBody LaraParams params) {
		JsonReturnData<LaraTopHostsReturnResult> laraTopDstStaticHosts = this.laraManager.laraTopDstStatic(params);
		return laraTopDstStaticHosts;
	}
	
	/**
	 * Lara router IP list
	 * 
	 * @return result data as JSON.
	 */
	@RequestMapping(value = "/lara/routers", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<LaraRoutersReturnResult> laraRouterList() {
		JsonReturnData<LaraRoutersReturnResult> routerList = this.laraManager.getRouterList();
		return routerList;
	}
	
	/**
	 * Lara policy list
	 * 
	 * @param router IP to get it's policies
	 *
	 * @return result data as JSON.
	 */
	@RequestMapping(value = "/lara/policyListForRouter", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<LaraPolicyInfoReturnResult> laraPolicyList(@RequestBody LaraPolicyInfoParams params) {
		JsonReturnData<LaraPolicyInfoReturnResult> policyList = this.laraManager.getPolicyList(params);
		return policyList;
	}

}

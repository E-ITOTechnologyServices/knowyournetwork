
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


package hr.eito.kynkite.business.dao;

import com.fasterxml.jackson.databind.JsonNode;

import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;
import hr.eito.model.lara.cisco.LaraCiscoACLQueryResultField;
import hr.eito.model.lara.policies.LaraPolicyInfoQueryResultField;
import hr.eito.model.lara.rules.LaraRulesQueryResultField;
import hr.eito.model.lara.rules.LaraTopHostsQueryResultField;

/**
 * DAO for LARA
 *
 * @author Hrvoje
 */
public interface LaraDao {

	/**
	 * Get Lara Cisco ACL data
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the Lara Cisco data
	 */
	JsonReturnData<QueryResult<LaraCiscoACLQueryResultField>> laraCiscoACL(final String policyId);
	
	/**
	 * Get Lara Rules data
	 * 
	 * @param policyId identifier for the policy record
	 * 
	 * @return the Lara Rules data
	 */
	JsonReturnData<QueryResult<LaraRulesQueryResultField>> laraRules(final String policyId);
	
	/**
	 * Get Lara Rules top sources for dynamic hosts
	 * 
	 * @param policyId identifier for the policy record
	 * 
	 * @return the Lara Rules top sources for dynamic hosts data
	 */
	JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopSrcDynamic(final String policyId);
	
	/**
	 * Get Lara Rules top destinations for dynamic hosts
	 * 
	 * @param policyId identifier for the policy record
	 * 
	 * @return the Lara Rules top destinations for dynamic hosts data
	 */
	JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopDstDynamic(final String policyId);
	
	/**
	 * Get Lara Rules top sources for static hosts
	 * 
	 * @param policyId identifier for the policy record
	 * 
	 * @return the Lara Rules top sources for static hosts data
	 */
	JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopSrcStatic(final String policyId);
	
	/**
	 * Get Lara Rules top destinations for static hosts
	 * 
	 * @param policyId identifier for the policy record
	 * 
	 * @return the Lara Rules top destinations for static hosts data
	 */
	JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopDstStatic(final String policyId);
	
	/**
	 * Get Lara router IP list
	 *
	 * @return the Lara router IP list as JSON
	 */
	JsonReturnData<JsonNode> getRouterList();
	
	/**
	 * Get Lara policy list for defined router
	 * 
	 * @param routerIpAddress ip address of the router
	 * 
	 * @return the Lara policy list
	 */
	JsonReturnData<QueryResult<LaraPolicyInfoQueryResultField>> getPolicyList(final String routerIpAddress);
}

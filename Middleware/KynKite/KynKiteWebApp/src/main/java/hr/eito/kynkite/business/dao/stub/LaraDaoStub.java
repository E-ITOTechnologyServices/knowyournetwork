
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


package hr.eito.kynkite.business.dao.stub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import hr.eito.kynkite.business.dao.LaraDao;
import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;
import hr.eito.model.lara.cisco.LaraCiscoACLQueryResultField;
import hr.eito.model.lara.policies.LaraPolicyInfoQueryResultField;
import hr.eito.model.lara.rules.LaraRulesQueryResultField;
import hr.eito.model.lara.rules.LaraTopDstDynamicQueryResultField;
import hr.eito.model.lara.rules.LaraTopDstStaticQueryResultField;
import hr.eito.model.lara.rules.LaraTopHostsQueryResultField;
import hr.eito.model.lara.rules.LaraTopSrcDynamicQueryResultField;
import hr.eito.model.lara.rules.LaraTopSrcStaticQueryResultField;

/**
 * Lara data is fetched from resource test data files, and not Elasticsearch queries
 *
 * @author Hrvoje
 */
@Component
@Profile("test")
public class LaraDaoStub implements LaraDao {
	
	@Autowired
	private StubManager stubManager;
	
	/**
	 * Get dummy, test Lara Cisco ACL data.
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<LaraCiscoACLQueryResultField>> laraCiscoACL(final String policyId) {
		final String location = "lara/cisco/"+policyId;
		return stubManager.reportResults_(location, LaraCiscoACLQueryResultField.class, new TypeReference<QueryResult<LaraCiscoACLQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Lara Rules data.
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<LaraRulesQueryResultField>> laraRules(final String policyId) {
		final String location = "lara/rules/"+policyId;
		return stubManager.reportResults_(location, LaraRulesQueryResultField.class, new TypeReference<QueryResult<LaraRulesQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Lara Rules top sources for dynamic hosts data.
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopSrcDynamic(final String policyId) {
		final String location = "lara/rules/"+policyId+"/topsrcdynamic";
		return stubManager.reportResults_(location, LaraTopHostsQueryResultField.class, new TypeReference<QueryResult<LaraTopSrcDynamicQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Lara Rules top destinations for dynamic hosts data.
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopDstDynamic(final String policyId) {
		final String location = "lara/rules/"+policyId+"/topdstdynamic";
		return stubManager.reportResults_(location, LaraTopHostsQueryResultField.class, new TypeReference<QueryResult<LaraTopDstDynamicQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Lara Rules top sources for static hosts data.
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopSrcStatic(final String policyId) {
		final String location = "lara/rules/"+policyId+"/topsrcstatic";
		return stubManager.reportResults_(location, LaraTopHostsQueryResultField.class, new TypeReference<QueryResult<LaraTopSrcStaticQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Lara Rules top destinations for static hosts data.
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopDstStatic(final String policyId) {
		final String location = "lara/rules/"+policyId+"/topdststatic";
		return stubManager.reportResults_(location, LaraTopHostsQueryResultField.class, new TypeReference<QueryResult<LaraTopDstStaticQueryResultField>>() {});
	}
	
	/**
	 * Stub out the fetching of list of routers query result
	 *
	 * @return the router list data
	 */
	@Override
	public JsonReturnData<JsonNode> getRouterList() {
		final String location = "lara/routers";
		return stubManager.reportData_(location);
	}
	
	/**
	 * Stub out the fetching of list of policies for specified router
	 * 
	 * @param routerIpAddress ip address of the router
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<LaraPolicyInfoQueryResultField>> getPolicyList(final String routerIpAddress) {
		final String location = "lara/policies/"+routerIpAddress;
		return stubManager.reportResults_(location, LaraPolicyInfoQueryResultField.class, new TypeReference<QueryResult<LaraPolicyInfoQueryResultField>>() {});
	}
	
}


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


package hr.eito.kynkite.business.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import hr.eito.kynkite.business.dao.LaraDao;
import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;
import hr.eito.model.elasticquery.Aggs;
import hr.eito.model.elasticquery.AggsDetailsBuilder;
import hr.eito.model.elasticquery.AggsSegment;
import hr.eito.model.elasticquery.AggsTerms;
import hr.eito.model.elasticquery.ElasticQuery;
import hr.eito.model.elasticquery.QueryFieldShortInformation;
import hr.eito.model.elasticquery.QueryLeafClause;
import hr.eito.model.elasticquery.SortDetails;
import hr.eito.model.elasticquery.SortSegment;
import hr.eito.model.elasticquery.utils.AggsTermsName;
import hr.eito.model.elasticquery.utils.QueryClauseName;
import hr.eito.model.elasticquery.utils.SortCardinal;
import hr.eito.model.lara.cisco.LaraCiscoACLQueryResultField;
import hr.eito.model.lara.policies.LaraPolicyInfoQueryResultField;
import hr.eito.model.lara.rules.LaraRulesQueryResultField;
import hr.eito.model.lara.rules.LaraTopDstDynamicQueryResultField;
import hr.eito.model.lara.rules.LaraTopDstStaticQueryResultField;
import hr.eito.model.lara.rules.LaraTopHostsQueryResultField;
import hr.eito.model.lara.rules.LaraTopSrcDynamicQueryResultField;
import hr.eito.model.lara.rules.LaraTopSrcStaticQueryResultField;

/**
 * Implementation of Lara DAO
 * 
 * @author Hrvoje
 *
 */
@Component
@Profile({"dev","prod"})
public class LaraDaoImpl implements LaraDao {
	
	private final static String POLICY_INDEX = "kyn-policy-*";

	@Autowired 
	private DaoManager daoManager;
	
	/**
	 * Lara Cisco ACL data
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "_source": [
	 *                "@timestamp",
	 *                "name",
	 *                "acl"
	 *               ],
	 *   "query": {
	 *   	"term": {
	 *   		"_id": "AVnbFVPhISe-c8hencwD"
	 *   	}
	 *   },
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<LaraCiscoACLQueryResultField>> laraCiscoACL(final String policyId) {
		ElasticQuery query = new ElasticQuery();
		// Adding source part
		query.addSources("@timestamp", "name", "acl");
		// Adding query part
		QueryLeafClause term = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldShortInformation(
						"_id",
						policyId)
		);
		query.setQuery(term);
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		
		return daoManager._getResults2(POLICY_INDEX, query, LaraCiscoACLQueryResultField.class, new TypeReference<QueryResult<LaraCiscoACLQueryResultField>>(){});
	}
	
	/**
	 * Lara Rules data
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "_source": [
	 *                "@timestamp",
	 *                "name",
	 *                "rules"
	 *               ],
	 *   "query": {
	 *   	"term": {
	 *   		"_id": "AVnbFVPhISe-c8hencwD"
	 *   	}
	 *   },
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<LaraRulesQueryResultField>> laraRules(final String policyId) {
		ElasticQuery query = new ElasticQuery();
		// Adding source part
		query.addSources("@timestamp", "name", "rules");
		// Adding query part
		QueryLeafClause term = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldShortInformation(
						"_id",
						policyId)
		);
		query.setQuery(term);
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		
		return daoManager._getResults2(POLICY_INDEX, query, LaraRulesQueryResultField.class, new TypeReference<QueryResult<LaraRulesQueryResultField>>(){});
	}
	
	/**
	 * Lara Rules top sources for dynamic hosts data
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "_source": [
	 *                "@timestamp",
	 *                "name",
	 *                "top_sources_dynamic"
	 *               ],
	 *   "query": {
	 *   	"term": {
	 *   		"_id": "AVnbFVPhISe-c8hencwD"
	 *   	}
	 *   },
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopSrcDynamic(final String policyId) {
		ElasticQuery query = new ElasticQuery();
		// Adding source part
		query.addSources("@timestamp", "name", "top_sources_dynamic");
		// Adding query part
		QueryLeafClause term = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldShortInformation(
						"_id",
						policyId)
		);
		query.setQuery(term);
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		
		return daoManager._getResults2(POLICY_INDEX, query, LaraTopHostsQueryResultField.class, new TypeReference<QueryResult<LaraTopSrcDynamicQueryResultField>>(){});
	}
	
	/**
	 * Lara Rules top destinations for dynamic hosts data
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "_source": [
	 *                "@timestamp",
	 *                "name",
	 *                "top_destinations_dynamic"
	 *               ],
	 *   "query": {
	 *   	"term": {
	 *   		"_id": "AVnbFVPhISe-c8hencwD"
	 *   	}
	 *   },
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopDstDynamic(final String policyId) {
		ElasticQuery query = new ElasticQuery();
		// Adding source part
		query.addSources("@timestamp", "name", "top_destinations_dynamic");
		// Adding query part
		QueryLeafClause term = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldShortInformation(
						"_id",
						policyId)
		);
		query.setQuery(term);
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		
		return daoManager._getResults2(POLICY_INDEX, query, LaraTopHostsQueryResultField.class, new TypeReference<QueryResult<LaraTopDstDynamicQueryResultField>>(){});
	}
	
	/**
	 * Lara Rules top sources for static hosts data
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "_source": [
	 *                "@timestamp",
	 *                "name",
	 *                "top_sources_static"
	 *               ],
	 *   "query": {
	 *   	"term": {
	 *   		"_id": "AVnbFVPhISe-c8hencwD"
	 *   	}
	 *   },
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopSrcStatic(final String policyId) {
		ElasticQuery query = new ElasticQuery();
		// Adding source part
		query.addSources("@timestamp", "name", "top_sources_static");
		// Adding query part
		QueryLeafClause term = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldShortInformation(
						"_id",
						policyId)
		);
		query.setQuery(term);
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		
		return daoManager._getResults2(POLICY_INDEX, query, LaraTopHostsQueryResultField.class, new TypeReference<QueryResult<LaraTopSrcStaticQueryResultField>>(){});
	}
	
	/**
	 * Lara Rules top destinations for static hosts data
	 * 
	 * @param policyId identifier for the policy record
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "_source": [
	 *                "@timestamp",
	 *                "name",
	 *                "top_destinations_static"
	 *               ],
	 *   "query": {
	 *   	"term": {
	 *   		"_id": "AVnbFVPhISe-c8hencwD"
	 *   	}
	 *   },
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<LaraTopHostsQueryResultField>> laraTopDstStatic(final String policyId) {
		ElasticQuery query = new ElasticQuery();
		// Adding source part
		query.addSources("@timestamp", "name", "top_destinations_static");
		// Adding query part
		QueryLeafClause term = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldShortInformation(
						"_id",
						policyId)
		);
		query.setQuery(term);
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		
		return daoManager._getResults2(POLICY_INDEX, query, LaraTopHostsQueryResultField.class, new TypeReference<QueryResult<LaraTopDstStaticQueryResultField>>(){});
	}
	
	/**
	 * Get Lara router IP list
	 *
	 * @return the Lara router IP list as JSON
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 * 		"size": 0,
	 * 		"aggs" : {
	 * 			"distinct_routers" : {
	 * 				"terms" : {
	 * 					"field" : "name"
	 * 				}
	 * 			}
	 * 		}
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<JsonNode> getRouterList() {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(0);
		// Adding aggs part
		Aggs aggs = new Aggs();
		aggs.addAggsSegment(new AggsSegment(
				"distinct_routers",
				new AggsTerms(AggsTermsName.TERMS.getName(), 
						new AggsDetailsBuilder()
						.setField("name")
						.build()
				)
		));
		query.setAggs(aggs);
		return daoManager._getResults(POLICY_INDEX, query, JsonNode.class);
	}
	
	/**
	 * Get Lara policy list for defined router
	 * 
	 * @param routerIpAddress ip address of the router
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "_source": [
	 *   	"@timestamp"
	 *   ],
	 *   "query": {
	 *     "term": {
	 *	     "name": "192.168.9.1" 
	 *     }
	 *   },
	 *   "sort": [
	 *   	{
	 *   		"@timestamp":{ "order":"desc" }
	 *   	}
	 *   ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<LaraPolicyInfoQueryResultField>> getPolicyList(final String routerIpAddress) {
		ElasticQuery query = new ElasticQuery();
		// Adding source part
		query.addSources("@timestamp");
		QueryLeafClause term = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldShortInformation(
						"name",
						routerIpAddress
				)
		);
		query.setQuery(term);
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return daoManager._getResults2(POLICY_INDEX, query, LaraPolicyInfoQueryResultField.class, new TypeReference<QueryResult<LaraPolicyInfoQueryResultField>>(){});
	}

}

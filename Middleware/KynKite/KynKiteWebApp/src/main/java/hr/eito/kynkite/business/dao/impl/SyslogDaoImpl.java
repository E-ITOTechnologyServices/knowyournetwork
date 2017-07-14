
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

import hr.eito.kynkite.business.dao.SyslogDao;
import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;
import hr.eito.model.elasticquery.ElasticQuery;
import hr.eito.model.elasticquery.QueryFieldDetailsBuilder;
import hr.eito.model.elasticquery.QueryClause;
import hr.eito.model.elasticquery.QueryFieldLongInformation;
import hr.eito.model.elasticquery.QueryLeafClause;
import hr.eito.model.elasticquery.QueryTerm;
import hr.eito.model.elasticquery.SortDetails;
import hr.eito.model.elasticquery.SortSegment;
import hr.eito.model.elasticquery.utils.QueryClauseName;
import hr.eito.model.elasticquery.utils.QueryTermName;
import hr.eito.model.elasticquery.utils.SortCardinal;
import hr.eito.model.syslog.SyslogASAQueryResultField;
import hr.eito.model.syslog.SyslogDHCPQueryResultField;
import hr.eito.model.syslog.SyslogIPSQueryResultField;
import hr.eito.model.syslog.SyslogProxyQueryResultField;
import hr.eito.model.syslog.SyslogRouterQueryResultField;
import hr.eito.model.syslog.SyslogVoiceQueryResultField;

/**
 * Implementation of Syslog DAO
 * 
 * @author Hrvoje
 *
 */
@Component
@Profile({"dev","prod"})
public class SyslogDaoImpl implements SyslogDao {
	
	@Autowired 
	private DaoManager daoManager;

	/**
	 * SyslogASA with no parameter.
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp",
	 *                "asa_fw",
	 *                "message"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA() {		
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding source part
		query.addSources("@timestamp", "asa_fw", "message");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogASA(query);
	}

	/**
	 * SyslogASA with a parameter.
	 *
	 * @param param the wildcard message value to search for
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp",
	 *                "asa_fw",
	 *                "message"
	 *               ],
	 *   "query": {
	 *     "wildcard":{
	 *       "message":{
	 *         "value":"10.29.155.65"
	 *       }
	 *     }
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
	public JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA(String param) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"message", 
						new QueryFieldDetailsBuilder()
						.setValue(param)
						.build()
				)
		);
		query.setQuery(wildcard);
		// Adding source part
		query.addSources("@timestamp", "asa_fw", "message");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogASA(query);
	}

	/**
	 * SyslogASA with no query and with datetimerange.
	 * 
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 * 	  "query": {
	 * 			"range": {
	 * 				"@timestamp": {
	 * 					"gte": "2016-12-02T14:32:44.000+01:00",
	 * 					"lte": "2016-12-02T14:32:46.000+01:00"
	 * 				}
	 * 			}
	 * 	  },
	 *    "_source": [
	 *                "@timestamp",
	 *                "asa_fw",
	 *                "message"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA(String dateTimeFrom, String dateTimeTo) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		query.setQuery(range);
		// Adding source part
		query.addSources("@timestamp", "asa_fw", "message");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogASA(query);
	}
	
	/**
	 * SyslogASA with query and with datetimerange.
	 * 
	 * @param param the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *	  "query": {
	 *    		"bool": {
	 *      		"must": [
	 *        			{
	 *          			"wildcard": {
	 *            				"message": {
	 *          					"value": "10.29.155.65"
	 *            				}
	 *          			}
	 *        			},
	 *        			{
	 *          			"range": {
	 *            				"@timestamp": {
	 *              				"gte": "2016-12-08T00:00:00.000",
	 *              				"lte": "2016-12-15T23:59:59.999"
	 *            				}
	 *          			}
	 *        			}
	 *      		]
	 *    		}
	 *    },
	 *    "_source": [
	 *                "@timestamp",
	 *                "asa_fw",
	 *                "message"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA(String param, String dateTimeFrom, String dateTimeTo) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"message", 
						new QueryFieldDetailsBuilder()
						.setValue(param)
						.build()
				)
		);
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		QueryClause bool = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.MUST.getName(),
						wildcard,
						range
				)
		);
		query.setQuery(bool);
		// Adding source part
		query.addSources("@timestamp", "asa_fw", "message");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogASA(query);
	}

	/**
	 * SyslogRouter with no parameter.
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp",
	 *                "cisco_message",
	 *                "router"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter() {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding source part
		query.addSources("@timestamp", "cisco_message", "router");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogRouter(query);
	}

	/**
	 * SyslogRouter with a parameter.
	 *
	 * @param param the wildcard message value to search for
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp",
	 *                "cisco_message",
	 *                "router"
	 *               ],
	 *   "query": {
	 *     "wildcard":{
	 *       "message":{
	 *         "value":"*10.29.155.65*"
	 *       }
	 *     }
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
	public JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter(String param) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"message", 
						new QueryFieldDetailsBuilder()
						.setValue("*"+param+"*")
						.build()
				)
		);
		query.setQuery(wildcard);
		// Adding source part
		query.addSources("@timestamp", "cisco_message", "router");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogRouter(query);
	}

	/**
	 * SyslogRouter with no query and with datetimerange.
	 * 
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 * 	  "query": {
	 * 			"range": {
	 * 				"@timestamp": {
	 * 					"gte": "2016-12-02T14:32:44.000",
	 * 					"lte": "2016-12-02T14:32:46.000"
	 * 				}
	 * 			}
	 * 	  },
	 *    "_source": [
	 *                "@timestamp",
	 *                "cisco_message",
	 *                "router"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter(String dateTimeFrom, String dateTimeTo) {		
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		query.setQuery(range);
		// Adding source part
		query.addSources("@timestamp", "cisco_message", "router");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogRouter(query);
	}
	
	/**
	 * SyslogRouter with query and with datetimerange.
	 * 
	 * @param param the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *	  "query": {
	 *    		"bool": {
	 *      		"must": [
	 *        			{
	 *          			"wildcard": {
	 *            				"message": {
	 *          					"value": "*10.29.155.65*"
	 *            				}
	 *          			}
	 *        			},
	 *        			{
	 *          			"range": {
	 *            				"@timestamp": {
	 *              				"gte": "2016-12-08T00:00:00.000",
	 *              				"lte": "2016-12-15T23:59:59.999"
	 *            				}
	 *          			}
	 *        			}
	 *      		]
	 *    		}
	 *    },
	 *    "_source": [
	 *                "@timestamp",
	 *                "cisco_message",
	 *                "router"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter(String param, String dateTimeFrom, String dateTimeTo) {	
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"message", 
						new QueryFieldDetailsBuilder()
						.setValue("*"+param+"*")
						.build()
				)
		);
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		QueryClause bool = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.MUST.getName(),
						wildcard,
						range
				)
		);
		query.setQuery(bool);
		// Adding source part
		query.addSources("@timestamp", "cisco_message", "router");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogRouter(query);
	}

	/**
	 * SyslogVoice with no parameter.
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp",
	 *                "voicegw",
	 *                "shortmessage"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice() {		
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding source part
		query.addSources("@timestamp", "voicegw", "shortmessage");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogVoice(query);
	}

	/**
	 * SyslogVoice with a parameter.
	 *
	 * @param param the wildcard message value to search for
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp",
	 *                "voicegw",
	 *                "shortmessage"
	 *               ],
	 *   "query": {
	 *     "wildcard":{
	 *       "shortmessage":{
	 *         "value":"*10.29.155.65*"
	 *       }
	 *     }
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
	public JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice(String param) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"shortmessage", 
						new QueryFieldDetailsBuilder()
						.setValue("*"+param+"*")
						.build()
				)
		);
		query.setQuery(wildcard);
		// Adding source part
		query.addSources("@timestamp", "voicegw", "shortmessage");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogVoice(query);
	}

	/**
	 * SyslogVoice with no query and with datetimerange.
	 * 
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 * 	  "query": {
	 * 			"range": {
	 * 				"@timestamp": {
	 * 					"gte": "2016-12-02T14:32:44.000",
	 * 					"lte": "2016-12-02T14:32:46.000"
	 * 				}
	 * 			}
	 * 	  },
	 *    "_source": [
	 *                "@timestamp",
	 *                "voicegw",
	 *                "shortmessage"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice(String dateTimeFrom, String dateTimeTo) {		
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		query.setQuery(range);
		// Adding source part
		query.addSources("@timestamp", "voicegw", "shortmessage");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogVoice(query);
	}
	
	/**
	 * SyslogVoice with query and with datetimerange.
	 * 
	 * @param param the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *	  "query": {
	 *    		"bool": {
	 *      		"must": [
	 *        			{
	 *          			"wildcard": {
	 *            				"shortmessage": {
	 *          					"value": "*10.29.155.65*"
	 *            				}
	 *          			}
	 *        			},
	 *        			{
	 *          			"range": {
	 *            				"@timestamp": {
	 *              				"gte": "2016-12-08T00:00:00.000",
	 *              				"lte": "2016-12-15T23:59:59.999"
	 *            				}
	 *          			}
	 *        			}
	 *      		]
	 *    		}
	 *    },
	 *    "_source": [
	 *                "@timestamp",
	 *                "voicegw",
	 *                "shortmessage"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice(String param, String dateTimeFrom, String dateTimeTo) {	
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"shortmessage", 
						new QueryFieldDetailsBuilder()
						.setValue("*"+param+"*")
						.build()
				)
		);
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		QueryClause bool = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.MUST.getName(),
						wildcard,
						range
				)
		);
		query.setQuery(bool);
		// Adding source part
		query.addSources("@timestamp", "voicegw", "shortmessage");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogVoice(query);
	}

	/**
	 * SyslogIPS with no parameter.
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp",
	 *                "log",
	 *                "IPS_System",
	 *                "Source",
	 *                "Destination",
	 *                "Destinationport"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS() {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding source part
		query.addSources("@timestamp", "log", "IPS_System", "Source", "Destination", "Destinationport");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogIPS(query);
	}

	/**
	 * SyslogIPS with a parameter.
	 *
	 * @param param the wildcard message value to search for
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp",
	 *                "log",
	 *                "IPS_System",
	 *                "Source",
	 *                "Destination",
	 *                "Destinationport"
	 *               ],
	 *   "query": {
	 *     "wildcard":{
	 *       "message":{
	 *         "value":"*10.29.155.65*"
	 *       }
	 *     }
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
	public JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS(String param) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"message", 
						new QueryFieldDetailsBuilder()
						.setValue("*"+param+"*")
						.build()
				)
		);
		query.setQuery(wildcard);
		// Adding source part
		query.addSources("@timestamp", "log", "IPS_System", "Source", "Destination", "Destinationport");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogIPS(query);
	}

	/**
	 * SyslogIPS with no query and with datetimerange.
	 * 
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 * 	  "query": {
	 * 			"range": {
	 * 				"@timestamp": {
	 * 					"gte": "2016-12-02T14:32:44.000",
	 * 					"lte": "2016-12-02T14:32:46.000"
	 * 				}
	 * 			}
	 * 	  },
	 *    "_source": [
	 *                "@timestamp",
	 *                "log",
	 *                "IPS_System",
	 *                "Source",
	 *                "Destination",
	 *                "Destinationport"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS(String dateTimeFrom, String dateTimeTo) {		
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		query.setQuery(range);
		// Adding source part
		query.addSources("@timestamp", "log", "IPS_System", "Source", "Destination", "Destinationport");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogIPS(query);
	}
	
	/**
	 * SyslogIPS with query and with datetimerange.
	 * 
	 * @param param the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *	  "query": {
	 *    		"bool": {
	 *      		"must": [
	 *        			{
	 *          			"wildcard": {
	 *            				"message": {
	 *          					"value": "*10.29.155.65*"
	 *            				}
	 *          			}
	 *        			},
	 *        			{
	 *          			"range": {
	 *            				"@timestamp": {
	 *              				"gte": "2016-12-08T00:00:00.000",
	 *              				"lte": "2016-12-15T23:59:59.999"
	 *            				}
	 *          			}
	 *        			}
	 *      		]
	 *    		}
	 *    },
	 *    "_source": [
	 *                "@timestamp",
	 *                "log",
	 *                "IPS_System",
	 *                "Source",
	 *                "Destination",
	 *                "Destinationport"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS(String param, String dateTimeFrom, String dateTimeTo) {	
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"message", 
						new QueryFieldDetailsBuilder()
						.setValue("*"+param+"*")
						.build()
				)
		);
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		QueryClause bool = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.MUST.getName(),
						wildcard,
						range
				)
		);
		query.setQuery(bool);
		// Adding source part
		query.addSources("@timestamp", "log", "IPS_System", "Source", "Destination", "Destinationport");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogIPS(query);
	}
	
	/**
	 * SyslogDHCP with no parameter.
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp",
	 *                "message",
	 *                "AD-Server"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP() {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding source part
		query.addSources("@timestamp", "message", "AD-Server");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogDHCP(query);
	}
	
	/**
	 * SyslogDHCP with a parameter.
	 *
	 * @param param the wildcard message value to search for
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp",
	 *                "message",
	 *                "AD-Server"
	 *               ],
	 *   "query": {
	 *     "wildcard":{
	 *       "message":{
	 *         "value":"*10.29.155.65*"
	 *       }
	 *     }
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
	public JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP(String param) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"message", 
						new QueryFieldDetailsBuilder()
						.setValue("*"+param+"*")
						.build()
				)
		);
		query.setQuery(wildcard);
		// Adding source part
		query.addSources("@timestamp", "message", "AD-Server");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogDHCP(query);
	}

	/**
	 * SyslogDHCP with no query and with datetimerange.
	 * 
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 * 	  "query": {
	 * 			"range": {
	 * 				"@timestamp": {
	 * 					"gte": "2016-12-02T14:32:44.000",
	 * 					"lte": "2016-12-02T14:32:46.000"
	 * 				}
	 * 			}
	 * 	  },
	 *    "_source": [
	 *                "@timestamp",
	 *                "message",
	 *                "AD-Server"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP(String dateTimeFrom, String dateTimeTo) {		
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		query.setQuery(range);
		// Adding source part
		query.addSources("@timestamp", "message", "AD-Server");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogDHCP(query);
	}
	
	/**
	 * SyslogDHCP with query and with datetimerange.
	 * 
	 * @param param the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *	  "query": {
	 *    		"bool": {
	 *      		"must": [
	 *        			{
	 *          			"wildcard": {
	 *            				"message": {
	 *          					"value": "*10.29.155.65*"
	 *            				}
	 *          			}
	 *        			},
	 *        			{
	 *          			"range": {
	 *            				"@timestamp": {
	 *              				"gte": "2016-12-08T00:00:00.000",
	 *              				"lte": "2016-12-15T23:59:59.999"
	 *            				}
	 *          			}
	 *        			}
	 *      		]
	 *    		}
	 *    },
	 *    "_source": [
	 *                "@timestamp"
	 *                , "message"
	 *                , "AD-Server"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP(String param, String dateTimeFrom, String dateTimeTo) {	
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"message", 
						new QueryFieldDetailsBuilder()
						.setValue("*"+param+"*")
						.build()
				)
		);
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		QueryClause bool = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.MUST.getName(),
						wildcard,
						range
				)
		);
		query.setQuery(bool);
		// Adding source part
		query.addSources("@timestamp", "message", "AD-Server");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogDHCP(query);
	}

	/**
	 * SyslogProxy with no parameter.
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp"
	 *                , "c_ip"
	 *                , "destinationIP"
	 *                , "cs_uri_port"
	 *                , "cs_host"
	 *                , "cs_uri_path"
	 *                , "sc_status"
	 *                , "user_agent_os"
	 *                , "user_agent_name"
	 *                , "user_agent_major"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy() {	
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding source part
		query.addSources("@timestamp", "c_ip", "destinationIP", "cs_uri_port", "cs_host", "cs_uri_path", "sc_status", 
				"user_agent_os", "user_agent_name", "user_agent_major");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogProxy(query);
	}
	
	/**
	 * SyslogProxy with a query.
	 *
	 * @param param the wildcard message value to search for
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *    "_source": [
	 *                "@timestamp"
	 *                , "c_ip"
	 *                , "destinationIP"
	 *                , "cs_uri_port"
	 *                , "cs_host"
	 *                , "cs_uri_path"
	 *                , "sc_status"
	 *                , "user_agent_os"
	 *                , "user_agent_name"
	 *                , "user_agent_major"
	 *               ],
	 *   "query": {
	 *     "wildcard":{
	 *       "c_ip.string":{
	 *         "value":"10.38.106.*"
	 *       }
	 *     }
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
	public JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy(String param) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"c_ip.string", 
						new QueryFieldDetailsBuilder()
						.setValue(param+"*")
						.build()
				)
		);
		query.setQuery(wildcard);
		// Adding source part
		query.addSources("@timestamp", "c_ip", "destinationIP", "cs_uri_port", "cs_host", "cs_uri_path", "sc_status", 
				"user_agent_os", "user_agent_name", "user_agent_major");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogProxy(query);
	}
	
	/**
	 * SyslogProxy with no query and with datetimerange.
	 * 
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 * 	  "query": {
	 * 			"range": {
	 * 				"@timestamp": {
	 * 					"gte": "2016-12-02T14:32:44.000",
	 * 					"lte": "2016-12-02T14:32:46.000"
	 * 				}
	 * 			}
	 * 	  },
	 *    "_source": [
	 *                "@timestamp"
	 *                , "c_ip"
	 *                , "destinationIP"
	 *                , "cs_uri_port"
	 *                , "cs_host"
	 *                , "cs_uri_path"
	 *                , "sc_status"
	 *                , "user_agent_os"
	 *                , "user_agent_name"
	 *                , "user_agent_major"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy(String dateTimeFrom, String dateTimeTo) {		
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		query.setQuery(range);
		// Adding source part
		query.addSources("@timestamp", "c_ip", "destinationIP", "cs_uri_port", "cs_host", "cs_uri_path", "sc_status", 
				"user_agent_os", "user_agent_name", "user_agent_major");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogProxy(query);
	}
	
	/**
	 * SyslogProxy with query and with datetimerange.
	 * 
	 * @param param the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": 100,
	 *	  "query": {
	 *    		"bool": {
	 *      		"must": [
	 *        			{
	 *          			"wildcard": {
	 *            				"c_ip.string": {
	 *          					"value": "10.50.0.17*"
	 *            				}
	 *          			}
	 *        			},
	 *        			{
	 *          			"range": {
	 *            				"@timestamp": {
	 *              				"gte": "2016-12-08T00:00:00.000",
	 *              				"lte": "2016-12-15T23:59:59.999"
	 *            				}
	 *          			}
	 *        			}
	 *      		]
	 *    		}
	 *    },
	 *    "_source": [
	 *                "@timestamp"
	 *                , "c_ip"
	 *                , "destinationIP"
	 *                , "cs_uri_port"
	 *                , "cs_host"
	 *                , "cs_uri_path"
	 *                , "sc_status"
	 *                , "user_agent_os"
	 *                , "user_agent_name"
	 *                , "user_agent_major"
	 *               ],
	 *   "sort": [
	 *             {
	 *               "@timestamp":{ "order":"desc" }
	 *             }
	 *           ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy(String param, String dateTimeFrom, String dateTimeTo) {	
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"c_ip.string", 
						new QueryFieldDetailsBuilder()
						.setValue(param+"*")
						.build()
				)
		);
		QueryLeafClause range = new QueryLeafClause(
				QueryClauseName.RANGE.getName(),
				new QueryFieldLongInformation(
						"@timestamp",
						new QueryFieldDetailsBuilder()
						.setGte(dateTimeFrom)
						.setLte(dateTimeTo)
						.build()
				)
		);
		QueryClause bool = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.MUST.getName(),
						wildcard,
						range
				)
		);
		query.setQuery(bool);
		// Adding source part
		query.addSources("@timestamp", "c_ip", "destinationIP", "cs_uri_port", "cs_host", "cs_uri_path", "sc_status", 
				"user_agent_os", "user_agent_name", "user_agent_major");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		return _getSyslogProxy(query);
	}
	
	/**
	 * Get results from the SyslogASA index.
	 */
	private JsonReturnData<QueryResult<SyslogASAQueryResultField>> _getSyslogASA(final ElasticQuery query) {
		final String index = "kyn-syslog-asa-*";
		return daoManager._getResults2(index, query, SyslogASAQueryResultField.class, new TypeReference<QueryResult<SyslogASAQueryResultField>>(){});
	}

	/**
	 * Get results from the SyslogRouter index.
	 */
	private JsonReturnData<QueryResult<SyslogRouterQueryResultField>> _getSyslogRouter(final ElasticQuery query) {
		final String index = "kyn-syslog-router-*";
		return daoManager._getResults2(index, query, SyslogRouterQueryResultField.class, new TypeReference<QueryResult<SyslogRouterQueryResultField>>(){});
	}

	/**
	 * Get results from the SyslogVoice index.
	 */
	private JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> _getSyslogVoice(final ElasticQuery query) {
		final String index = "kyn-syslog-voice-*";
		return daoManager._getResults2(index, query, SyslogVoiceQueryResultField.class, new TypeReference<QueryResult<SyslogVoiceQueryResultField>>(){});
	}

	/**
	 * Get results from the SyslogIPS index.
	 */
	private JsonReturnData<QueryResult<SyslogIPSQueryResultField>> _getSyslogIPS(final ElasticQuery query) {
		final String index = "kyn-syslog-ips-*";
		return daoManager._getResults2(index, query, SyslogIPSQueryResultField.class, new TypeReference<QueryResult<SyslogIPSQueryResultField>>(){});
	}

	/**
	 * Get results from the SyslogDHCP index.
	 */
	private JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> _getSyslogDHCP(final ElasticQuery query) {
		final String index = "kyn-syslog-dhcp-*";
		return daoManager._getResults2(index, query, SyslogDHCPQueryResultField.class, new TypeReference<QueryResult<SyslogDHCPQueryResultField>>(){});
	}
	
	/**
	 * Get results from the SyslogProxy index.
	 */
	private JsonReturnData<QueryResult<SyslogProxyQueryResultField>> _getSyslogProxy(final ElasticQuery query) {
		final String index = "kyn-syslog-proxy-*";
		return daoManager._getResults2(index, query, SyslogProxyQueryResultField.class, new TypeReference<QueryResult<SyslogProxyQueryResultField>>(){});
	}

}

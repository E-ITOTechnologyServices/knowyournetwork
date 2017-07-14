
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

import hr.eito.kynkite.JsonData;
import hr.eito.kynkite.business.dao.ElasticSearchDao;
import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;
import hr.eito.model.elasticquery.Aggs;
import hr.eito.model.elasticquery.AggsDetailsBuilder;
import hr.eito.model.elasticquery.AggsOrder;
import hr.eito.model.elasticquery.AggsSegment;
import hr.eito.model.elasticquery.AggsTerms;
import hr.eito.model.elasticquery.ElasticQuery;
import hr.eito.model.elasticquery.QueryClause;
import hr.eito.model.elasticquery.QueryFieldDetailsBuilder;
import hr.eito.model.elasticquery.QueryFieldLongInformation;
import hr.eito.model.elasticquery.QueryFieldShortInformation;
import hr.eito.model.elasticquery.QueryLeafClause;
import hr.eito.model.elasticquery.QueryTerm;
import hr.eito.model.elasticquery.SortDetails;
import hr.eito.model.elasticquery.SortSegment;
import hr.eito.model.elasticquery.utils.AggsTermsName;
import hr.eito.model.elasticquery.utils.QueryClauseName;
import hr.eito.model.elasticquery.utils.QueryTermName;
import hr.eito.model.elasticquery.utils.SortCardinal;
import hr.eito.model.hostreport.EventDescription;
import hr.eito.model.newsticker.NewsTickerQueryResultField;

/**
 * @author Danijel Soltic
 *
 */
@Component
@Profile({"dev","prod"})
public class ElasticSearchDaoRepository implements ElasticSearchDao {
	
	@Autowired 
	private DaoManager daoManager;
	
	/*
	 * From Elasticsearch 5 it is not possible to define size:0 in queries under aggs part.
	 * This is a ugly hack to get all aggs records until better solution in Backend.
	 * 2147483647 equals to upper limit for Integer values in ES = 2^31-1
	 */
	private static final int AGGS_SIZE = 2147483647;
		
	/**
	 * CMDB report.
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *   "_source": ["ipam"],
	 *   "query": {
	 *     "term": {
	 *	     "_id": "1.1.1.1" 
	 *     }
	 *   }
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<JsonNode> cmdbReport(final String ipAddress) {
		ElasticQuery query = new ElasticQuery();
		// Adding sources part
		query.addSources("ipam");
		// Adding query part
		QueryLeafClause term = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldShortInformation(
						"_id", 
						ipAddress
				)
		);
		query.setQuery(term);
		return _getCMDB(query, JsonNode.class);
	}

	/**
	 * Host Report.
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *   "size": 0,
	 *   "aggs": {
	 *     "hosts": {
	 *       "terms": {
	 *         "field": "host",
	 *         "size": 1000,
	 *         "order": {
	 *           "cardinal": "desc"
	 *         }
	 *       },
	 *       "aggs": {
	 *         "events": {
	 *           "terms": {
	 *             "field": "use_case",
	 *                      "size": 2147483647
	 *           }
	 *         },
	 *         "cardinal": {
	 *           "cardinality": {
	 *             "field": "use_case"
	 *           }
	 *         },
	 *         "time": {
	 *           "stats": {
	 *             "field": "@timestamp"
	 *           }
	 *         }
	 *       }
	 *     }
	 *   }
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<JsonNode> hostReport() {
		ElasticQuery query = new ElasticQuery();
		
		// Adding size part
		query.setSize(0);
		
		// Adding aggs part		
		AggsTerms aggsTerms1 = new AggsTerms(AggsTermsName.TERMS.getName(), 
				new AggsDetailsBuilder()
				.setField("host")
				.setSize(1000)
				.setAggsOrder(new AggsOrder(SortCardinal.DESC.getExpression()))
				.build());
		AggsTerms aggsTerms2 = new AggsTerms(AggsTermsName.TERMS.getName(), 
				new AggsDetailsBuilder()
				.setField("use_case")
				.setSize(AGGS_SIZE)
				.build());
		AggsTerms aggsCardinality = new AggsTerms(AggsTermsName.CARDINALITY.getName(), 
				new AggsDetailsBuilder()
				.setField("use_case")
				.build());
		AggsTerms aggsStats = new AggsTerms(AggsTermsName.STATS.getName(), 
				new AggsDetailsBuilder()
				.setField("@timestamp")
				.build());
		
		Aggs aggs2 = new Aggs();
		aggs2.addAggsSegment(new AggsSegment("events", aggsTerms2));
		aggs2.addAggsSegment(new AggsSegment("cardinal", aggsCardinality));
		aggs2.addAggsSegment(new AggsSegment("time", aggsStats));
		
		Aggs aggs = new Aggs();
		aggs.addAggsSegment(new AggsSegment("hosts", aggsTerms1, aggs2));
		
		query.setAggs(aggs);
		
		return _getEvents(query, JsonNode.class);
	}

	/**
	 * Host Report with an IP address parameter.
	 *
	 * @param ipAddress the IP address to seach for
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *   "size": 0,
	 *   "query": {
	 *     "wildcard": {
	 *       "host.string": {
	 *         "value": "*192.168.11.160*"
	 *       }
	 *     }
	 *   },
	 *   "aggs": {
	 *     "hosts": {
	 *       "terms": {
	 *         "field": "host",
	 *         "size": 1000,
	 *         "order": {
	 *           "cardinal": "desc"
	 *         }
	 *       },
	 *       "aggs": {
	 *         "events": {
	 *           "terms": {
	 *             "field": "use_case",
	 *                      "size": 2147483647
	 *           }
	 *         },
	 *         "cardinal": {
	 *           "cardinality": {
	 *             "field": "use_case"
	 *           }
	 *         },
	 *         "time": {
	 *           "stats": {
	 *             "field": "@timestamp"
	 *           }
	 *         }
	 *       }
	 *     }
	 *   }
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<JsonNode> hostReport(String ipAddress) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(0);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"host.string", 
						new QueryFieldDetailsBuilder()
						.setValue("*"+ipAddress+"*")
						.build()
				)
		);
		query.setQuery(wildcard);
		// Adding aggs part		
		AggsTerms aggsTerms1 = new AggsTerms(AggsTermsName.TERMS.getName(), 
				new AggsDetailsBuilder()
				.setField("host")
				.setSize(1000)
				.setAggsOrder(new AggsOrder(SortCardinal.DESC.getExpression()))
				.build());
		AggsTerms aggsTerms2 = new AggsTerms(AggsTermsName.TERMS.getName(), 
				new AggsDetailsBuilder()
				.setField("use_case")
				.setSize(AGGS_SIZE)
				.build());
		AggsTerms aggsCardinality = new AggsTerms(AggsTermsName.CARDINALITY.getName(), 
				new AggsDetailsBuilder()
				.setField("use_case")
				.build());
		AggsTerms aggsStats = new AggsTerms(AggsTermsName.STATS.getName(), 
				new AggsDetailsBuilder()
				.setField("@timestamp")
				.build());
		
		Aggs aggs2 = new Aggs();
		aggs2.addAggsSegment(new AggsSegment("events", aggsTerms2));
		aggs2.addAggsSegment(new AggsSegment("cardinal", aggsCardinality));
		aggs2.addAggsSegment(new AggsSegment("time", aggsStats));
		
		Aggs aggs = new Aggs();
		aggs.addAggsSegment(new AggsSegment("hosts", aggsTerms1, aggs2));
		
		query.setAggs(aggs);
		
		return _getEvents(query, JsonNode.class);
	}
	
	/**
	 * Host report with no ip and with datetimerange.
	 * 
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *   "size": 0,
	 * 	  "query": {
	 * 			"range": {
	 * 				"@timestamp": {
	 * 					"gte": "2016-12-02T14:32:44.000",
	 * 					"lte": "2016-12-02T14:32:46.000"
	 * 				}
	 * 			}
	 * 	  },
	 *   "aggs": {
	 *     "hosts": {
	 *       "terms": {
	 *         "field": "host",
	 *         "size": 1000,
	 *         "order": {
	 *           "cardinal": "desc"
	 *         }
	 *       },
	 *       "aggs": {
	 *         "events": {
	 *           "terms": {
	 *             "field": "use_case",
	 *                      "size": 2147483647
	 *           }
	 *         },
	 *         "cardinal": {
	 *           "cardinality": {
	 *             "field": "use_case"
	 *           }
	 *         },
	 *         "time": {
	 *           "stats": {
	 *             "field": "@timestamp"
	 *           }
	 *         }
	 *       }
	 *     }
	 *   }
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<JsonNode> hostReport(String dateTimeFrom, String dateTimeTo) {		
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(0);
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
		// Adding aggs part		
		AggsTerms aggsTerms1 = new AggsTerms(AggsTermsName.TERMS.getName(), 
				new AggsDetailsBuilder()
				.setField("host")
				.setSize(1000)
				.setAggsOrder(new AggsOrder(SortCardinal.DESC.getExpression()))
				.build());
		AggsTerms aggsTerms2 = new AggsTerms(AggsTermsName.TERMS.getName(), 
				new AggsDetailsBuilder()
				.setField("use_case")
				.setSize(AGGS_SIZE)
				.build());
		AggsTerms aggsCardinality = new AggsTerms(AggsTermsName.CARDINALITY.getName(), 
				new AggsDetailsBuilder()
				.setField("use_case")
				.build());
		AggsTerms aggsStats = new AggsTerms(AggsTermsName.STATS.getName(), 
				new AggsDetailsBuilder()
				.setField("@timestamp")
				.build());
		
		Aggs aggs2 = new Aggs();
		aggs2.addAggsSegment(new AggsSegment("events", aggsTerms2));
		aggs2.addAggsSegment(new AggsSegment("cardinal", aggsCardinality));
		aggs2.addAggsSegment(new AggsSegment("time", aggsStats));
		
		Aggs aggs = new Aggs();
		aggs.addAggsSegment(new AggsSegment("hosts", aggsTerms1, aggs2));
		
		query.setAggs(aggs);
		
		return _getEvents(query, JsonNode.class);
	}
	
	/**
	 * Host report with ip and with datetimerange.
	 * 
	 * @param ipAddress  the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *   "size": 0,
	 *	  "query": {
	 *    		"bool": {
	 *      		"must": [
	 *        			{
	 *          			"wildcard": {
	 *            				"host.string": {
	 *          					"value": "*192.168.11.160*"
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
	 *   "aggs": {
	 *     "hosts": {
	 *       "terms": {
	 *         "field": "host",
	 *         "size": 1000,
	 *         "order": {
	 *           "cardinal": "desc"
	 *         }
	 *       },
	 *       "aggs": {
	 *         "events": {
	 *           "terms": {
	 *             "field": "use_case",
	 *                      "size": 2147483647
	 *           }
	 *         },
	 *         "cardinal": {
	 *           "cardinality": {
	 *             "field": "use_case"
	 *           }
	 *         },
	 *         "time": {
	 *           "stats": {
	 *             "field": "@timestamp"
	 *           }
	 *         }
	 *       }
	 *     }
	 *   }
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<JsonNode> hostReport(String ipAddress, String dateTimeFrom, String dateTimeTo) {	
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(0);
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
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"host.string", 
						new QueryFieldDetailsBuilder()
						.setValue("*"+ipAddress+"*")
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
		
		// Adding aggs part		
		AggsTerms aggsTerms1 = new AggsTerms(AggsTermsName.TERMS.getName(), 
				new AggsDetailsBuilder()
				.setField("host")
				.setSize(1000)
				.setAggsOrder(new AggsOrder(SortCardinal.DESC.getExpression()))
				.build());
		AggsTerms aggsTerms2 = new AggsTerms(AggsTermsName.TERMS.getName(), 
				new AggsDetailsBuilder()
				.setField("use_case")
				.setSize(AGGS_SIZE)
				.build());
		AggsTerms aggsCardinality = new AggsTerms(AggsTermsName.CARDINALITY.getName(), 
				new AggsDetailsBuilder()
				.setField("use_case")
				.build());
		AggsTerms aggsStats = new AggsTerms(AggsTermsName.STATS.getName(), 
				new AggsDetailsBuilder()
				.setField("@timestamp")
				.build());
		
		Aggs aggs2 = new Aggs();
		aggs2.addAggsSegment(new AggsSegment("events", aggsTerms2));
		aggs2.addAggsSegment(new AggsSegment("cardinal", aggsCardinality));
		aggs2.addAggsSegment(new AggsSegment("time", aggsStats));
		
		Aggs aggs = new Aggs();
		aggs.addAggsSegment(new AggsSegment("hosts", aggsTerms1, aggs2));
		
		query.setAggs(aggs);
		
		return _getEvents(query, JsonNode.class);
	}

	/**
	 * Host Details Report with an IP address parameter.
	 *
	 * @param ipAddress the IP address to seach for
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * }</pre></p>
     * {
     *   "query": {
     *     "term": {
     *       "host": {
     *         "value": "10.47.9.10"
     *       }
     *     }
     *   },
     *   "size": 0,
     *   "aggs": {
     *     "events": {
     *       "terms": {
     *         "field": "use_case",
	 *         "size": 2147483647
     *       },
     *       "aggs": {
     *         "minmax": {
     *           "stats": {
     *             "field": "@timestamp"
     *           }
     *         }
     *       }
     *     }
     *   }
     * }
	 */
	@Override
	public JsonReturnData<JsonNode> hostDetailsReport(String ipAddress) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(0);
		//Adding query part
		QueryLeafClause term = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldLongInformation(
						"host", 
						new QueryFieldDetailsBuilder()
						.setValue(ipAddress)
						.build()
				)
		);
		query.setQuery(term);
		// Adding aggs part
		Aggs aggs = new Aggs();
		AggsTerms aggsTerms = new AggsTerms(AggsTermsName.TERMS.getName(), 
				new AggsDetailsBuilder()
				.setField("use_case")
				.setSize(AGGS_SIZE)
				.build());
		AggsTerms aggsStats = new AggsTerms(AggsTermsName.STATS.getName(), 
				new AggsDetailsBuilder()
				.setField("@timestamp")
				.build());
		
		Aggs aggs2 = new Aggs();
		aggs2.addAggsSegment(new AggsSegment("minmax", aggsStats));
		
		aggs.addAggsSegment(new AggsSegment("events", aggsTerms, aggs2));
		
		query.setAggs(aggs);

		return _getEvents(query, JsonNode.class);
	}
	
	/**
	 * Host Details report with ip and with datetimerange.
	 * 
	 * @param ipAddress the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *   "size": 0,
	 *	  "query": {
	 *    		"bool": {
	 *      		"must": [
	 *        			{
	 *          			"term": {
	 *            				"host": {
	 *          					"value": "192.168.11.160"
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
     *   "aggs": {
     *     "events": {
     *       "terms": {
     *         "field": "use_case",
	 *         "size": 2147483647
     *       },
     *       "aggs": {
     *         "minmax": {
     *           "stats": {
     *             "field": "@timestamp"
     *           }
     *         }
     *       }
     *     }
     *   }
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<JsonNode> hostDetailsReport(String ipAddress, String dateTimeFrom, String dateTimeTo) {	
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(0);
		//Adding query part
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
		QueryLeafClause term = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldLongInformation(
						"host", 
						new QueryFieldDetailsBuilder()
						.setValue(ipAddress)
						.build()
				)
		);
		QueryClause bool = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.MUST.getName(),
						term,
						range
				)
		);
		query.setQuery(bool);
		
		// Adding aggs part
		Aggs aggs = new Aggs();
		AggsTerms aggsTerms = new AggsTerms(AggsTermsName.TERMS.getName(), 
				new AggsDetailsBuilder()
				.setField("use_case")
				.setSize(AGGS_SIZE)
				.build());
		AggsTerms aggsStats = new AggsTerms(AggsTermsName.STATS.getName(), 
				new AggsDetailsBuilder()
				.setField("@timestamp")
				.build());
		
		Aggs aggs2 = new Aggs();
		aggs2.addAggsSegment(new AggsSegment("minmax", aggsStats));
		
		aggs.addAggsSegment(new AggsSegment("events", aggsTerms, aggs2));
		
		query.setAggs(aggs);

		return _getEvents(query, JsonNode.class);
	}

	/**
	 * Event description query.
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *   "size": 100,
	 *   "_source": [
	 *                "use_case",
	 *                "description",
	 *                "severity",
	 *                "eventid",
	 *                "elk_link",
	 *                "date_offset"
	 *               ]
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<QueryResult<EventDescription>> loadEventDescriptions() {		
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(100);
		// Adding sources part
		query.addSources("use_case", "description", "severity", "eventid", "elk_link", "date_offset");

		return _getEventDescriptions(query);
	}

	/**
	 * Load netflow data by IP query.
	 *
	 * param ipAddress the ip address to get data for.
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *   "size": 0,
	 *   "query": {
	 *     "bool": {
	 *       "must": [ {
	 *         "wildcard": {
	 *           "TCPFlags": {
	 *             "value": "*R*"
	 *           }
	 *         }
	 *       },
	 *       {
	 *         "bool": {
	 *           "should": [ {
	 *             "term": {
	 *               "SourceAddress": {
	 *                 "value": "10.201.2.70"
	 *               }
	 *             }
	 *           },
	 *           {
	 *             "term": {
	 *               "DestinationAddress": {
	 *                 "value": "10.201.2.70"
	 *               }
	 *             }
	 *           } ]
	 *         }
	 *       } ]
	 *     }
	 *   },
	 *   "aggs": {
	 *     "week": {
	 *       "date_histogram": {
	 *         "field": "@timestamp",
	 *         "interval": "1w",
	 *         "format" : "xxxx-'\''W'\''ww"
	 *       },
	 *       "aggs": {
	 *         "day": {
	 *           "date_histogram": {
	 *             "field": "@timestamp",
	 *             "interval": "1d",
	 *             "format": "e"
	 *           }
	 *         }
	 *       }
	 *     }
	 *   }
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<JsonData> netflow(String ipAddress) {
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(0);
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"TCPFlags", 
						new QueryFieldDetailsBuilder()
						.setValue("*R*")
						.build()
				)
		);
		QueryLeafClause termSource = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldLongInformation(
						"SourceAddress", 
						new QueryFieldDetailsBuilder()
						.setValue(ipAddress)
						.build()
				)
		);
		QueryLeafClause termDestination = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldLongInformation(
						"DestinationAddress", 
						new QueryFieldDetailsBuilder()
						.setValue(ipAddress)
						.build()
				)
		);
		QueryClause boolShould = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.SHOULD.getName(),
						termSource,
						termDestination
				)
		);
		QueryClause boolMust = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.MUST.getName(),
						wildcard,
						boolShould
				)
		);
		query.setQuery(boolMust);
		
		// Adding aggs part
		AggsTerms dateHistogramWeek = new AggsTerms(AggsTermsName.DATE_HISTOGRAM.getName(), 
				new AggsDetailsBuilder()
				.setField("@timestamp")
				.setInterval("1w")
				.setFormat("xxxx-'W'ww")
				.build());
		AggsTerms dateHistogramDay = new AggsTerms(AggsTermsName.DATE_HISTOGRAM.getName(), 
				new AggsDetailsBuilder()
				.setField("@timestamp")
				.setInterval("1d")
				.setFormat("e")
				.build());
		Aggs aggs2 = new Aggs();
		aggs2.addAggsSegment(new AggsSegment("day", dateHistogramDay));
		Aggs aggs = new Aggs();
		aggs.addAggsSegment(new AggsSegment("week", dateHistogramWeek, aggs2));
		query.setAggs(aggs);

		return _getNetflow(query, JsonData.class);
	}
	
	/**
	 * Load netflow data by IP query and datetimerange.
	 *
	 * param ipAddress the ip address to get data for.
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 * 
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *   "size": 0,
	 *   "query": {
	 *     "bool": {
	 *       "must": [ {
	 *         "wildcard": {
	 *           "TCPFlags": {
	 *             "value": "*R*"
	 *           }
	 *         }
	 *       },
	 *       {
	 *         "bool": {
	 *           "should": [ {
	 *             "term": {
	 *               "SourceAddress": {
	 *                 "value": "10.201.2.70"
	 *               }
	 *             }
	 *           },
	 *           {
	 *             "term": {
	 *               "DestinationAddress": {
	 *                 "value": "10.201.2.70"
	 *               }
	 *             }
	 *           } ]
	 *         }
	 *       },
	 *       {
	 *         "range": {
	 *            "@timestamp": {
	 *              "gte": "2016-12-08T00:00:00.000",
	 *              "lte": "2016-12-15T23:59:59.999"
	 *            }
	 *         }
	 *       }
	 *       ]
	 *     }
	 *   },
	 *   "aggs": {
	 *     "week": {
	 *       "date_histogram": {
	 *         "field": "@timestamp",
	 *         "interval": "1w",
	 *         "format" : "xxxx-'\''W'\''ww"
	 *       },
	 *       "aggs": {
	 *         "day": {
	 *           "date_histogram": {
	 *             "field": "@timestamp",
	 *             "interval": "1d",
	 *             "format": "e"
	 *           }
	 *         }
	 *       }
	 *     }
	 *   }
	 * }
	 * }</pre></p>
	 */
	@Override
	public JsonReturnData<JsonData> netflow(String ipAddress, String dateTimeFrom, String dateTimeTo) {
		ElasticQuery query = new ElasticQuery();
		
		// Adding size part
		query.setSize(0);
		
		// Adding query part
		QueryLeafClause wildcard = new QueryLeafClause(
				QueryClauseName.WILDCARD.getName(),
				new QueryFieldLongInformation(
						"TCPFlags", 
						new QueryFieldDetailsBuilder()
						.setValue("*R*")
						.build()
				)
		);
		QueryLeafClause termSource = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldLongInformation(
						"SourceAddress", 
						new QueryFieldDetailsBuilder()
						.setValue(ipAddress)
						.build()
				)
		);
		QueryLeafClause termDestination = new QueryLeafClause(
				QueryClauseName.TERM.getName(),
				new QueryFieldLongInformation(
						"DestinationAddress", 
						new QueryFieldDetailsBuilder()
						.setValue(ipAddress)
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
		
		QueryClause boolShould = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.SHOULD.getName(),
						termSource,
						termDestination
				)
		);
		QueryClause boolMust = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.MUST.getName(),
						wildcard,
						boolShould,
						range
				)
		);
		
		query.setQuery(boolMust);
		
		// Adding aggs part
		AggsTerms dateHistogramWeek = new AggsTerms(AggsTermsName.DATE_HISTOGRAM.getName(), 
				new AggsDetailsBuilder()
				.setField("@timestamp")
				.setInterval("1w")
				.setFormat("xxxx-'W'ww")
				.build());
		AggsTerms dateHistogramDay = new AggsTerms(AggsTermsName.DATE_HISTOGRAM.getName(), 
				new AggsDetailsBuilder()
				.setField("@timestamp")
				.setInterval("1d")
				.setFormat("e")
				.build());
		Aggs aggs2 = new Aggs();
		aggs2.addAggsSegment(new AggsSegment("day", dateHistogramDay));
		Aggs aggs = new Aggs();
		aggs.addAggsSegment(new AggsSegment("week", dateHistogramWeek, aggs2));
		query.setAggs(aggs);
		
		return _getNetflow(query, JsonData.class);
	}

	/**
	 * NewsTicker query with no parameter.
	 *
	 * @param size the number of records to return
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": size,
	 *    "_source": [
	 *                "host",
	 *                "text",
	 *                "@timestamp",
	 *                "priority",
	 *                "eventid"
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
	public JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(final int size) {						
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(size);
		// Adding sources part
		query.addSources("@timestamp", "host", "text", "priority", "eventid");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);

		return _getNewsTicker(query);
	}

	/**
	 * NewsTicker query with a parameter.
	 *
	 * @param size the number of records to return
	 * @param param the value to search for
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": size,
	 *    "_source": [
	 *                "host",
	 *                "text",
	 *                "@timestamp",
	 *                "priority",
	 *                "eventid"
	 *               ],
	 *   "query": {
	 *     "match":{
	 *       "_all": "param"
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
	public JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(final int size, String param) {						
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(size);
		// Adding sources part
		query.addSources("@timestamp", "host", "text", "priority", "eventid");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
		// Adding query part
		QueryLeafClause match = new QueryLeafClause(
				QueryClauseName.MATCH.getName(),
				new QueryFieldShortInformation("_all", param)
		);
		query.setQuery(match);

		return _getNewsTicker(query);
	}
	
	/**
	 * NewsTicker with no query and with datetimerange.
	 * 
	 * @param size the number of records to return
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": size,
	 * 	  "query": {
	 * 			"range": {
	 * 				"@timestamp": {
	 * 					"gte": "2016-12-02T14:32:44.000",
	 * 					"lte": "2016-12-02T14:32:46.000"
	 * 				}
	 * 			}
	 * 	  },
	 *    "_source": [
	 *                "host",
	 *                "text",
	 *                "@timestamp",
	 *                "priority",
	 *                "eventid"
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
	public JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(int size, String dateTimeFrom, String dateTimeTo) {		
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(size);
		// Adding sources part
		query.addSources("@timestamp", "host", "text", "priority", "eventid");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
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

		return _getNewsTicker(query);
	}
	
	/**
	 * NewsTicker with query and with datetimerange.
	 * 
	 * @param size the number of records to return 
	 * @param param the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the data returned by the query in JSON format.
	 *
	 * <p>Sample query...
	 * <pre>{@code
	 * {
	 *    "size": size,
	 *	  "query": {
	 *    		"bool": {
	 *      		"must": [
	 *        			{
	 *          			"match": {
	 *          				"_all": "param"
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
	 *                "host",
	 *                "text",
	 *                "@timestamp",
	 *                "priority",
	 *                "eventid"
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
	public JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(int size, String param, String dateTimeFrom, String dateTimeTo) {	
		ElasticQuery query = new ElasticQuery();
		// Adding size part
		query.setSize(size);
		// Adding sources part
		query.addSources("@timestamp", "host", "text", "priority", "eventid");
		// Adding sort part
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails(SortCardinal.DESC.getExpression())
				)
		);
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
		QueryLeafClause match = new QueryLeafClause(
				QueryClauseName.MATCH.getName(),
				new QueryFieldShortInformation("_all", param)
		);
		QueryClause bool = new QueryClause(
				QueryClauseName.BOOL.getName(),
				new QueryTerm(
						QueryTermName.MUST.getName(),
						match,
						range
				)
		);
		query.setQuery(bool);

		return _getNewsTicker(query);
	}

	/**
	 * Get results from the CMDB index.
	 */
	private <T> JsonReturnData<T> _getCMDB(final ElasticQuery query, final Class<T> c) {
		final String index = "kyn-hostinfo";
		return daoManager._getResults(index, query, c);
	}

	/**
	 * Get results from the Events index.
	 */
	private <T> JsonReturnData<T> _getEvents(final ElasticQuery query, final Class<T> c) {
		final String index = "kyn-events-*";
		return daoManager._getResults(index, query, c);
	}

	/**
	 * Get results from the EventDescription index.
	 */
	private JsonReturnData<QueryResult<EventDescription>> _getEventDescriptions(final ElasticQuery query) {
		final String index = "kyn-eventdescription";
		return daoManager._getResults2(index, query, EventDescription.class, new TypeReference<QueryResult<EventDescription>>(){});
	}

	/**
	 * Get results from the NewsTicker index.
	 */
	private JsonReturnData<QueryResult<NewsTickerQueryResultField>> _getNewsTicker(final ElasticQuery query) {
		final String index = "kyn-newsticker-*";
		return daoManager._getResults2(index, query, NewsTickerQueryResultField.class, new TypeReference<QueryResult<NewsTickerQueryResultField>>(){});
	}
	
	/**
	 * Get results from the netflow index.
	 */
	private <T> JsonReturnData<T> _getNetflow(final ElasticQuery query, final Class<T> c) {
		final String index = "kyn-netflow-*";
		return daoManager._getResults(index, query, c);
	}

}


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

import java.net.UnknownHostException;
import java.util.Collections;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;
import hr.eito.model.elasticquery.ElasticQuery;

/**
 * Central DAO managing class
 * 
 * @author Hrvoje
 *
 */
@Component
@Profile({"dev","prod"})
public class DaoManager {
	
	private static final Logger logger = LoggerFactory.getLogger(DaoManager.class);
	RestClient restClient = null;
	
	/**
	 * Connecting to Data source using properties placed in application-*.properties
	 * 
	 * @param isCluster
	 * @param hostName
	 * @param clusterName
	 * @param portNumber
	 */
	@Autowired 
	public DaoManager(
			@Value("${isCluster}") final boolean isCluster,
			@Value("${host.name}") final String hostName, 
			@Value("${cluster.name}") final String clusterName,
			@Value("${port.number}") final int portNumber) {
		super();
		try {
			restClient = ElasticsearchRestClientSingleton.getInstance(hostName, portNumber, "http").getRestClient();
		} catch (Exception e) {
			logger.error("UnknownException",e);
			// Reporting of this to the end user is deferred until search time.
		}
	}	

	/**
	 * Generic method to execute a query and fetch the results.
	 *
	 * @param index Names the index the results are to be obtained from.
	 * @param query Details the query to be execited (in POJO format).
	 *
	 * @return The results of the query as JSON data.
	 */
	public <T> JsonReturnData<T> _getResults(final String index, final ElasticQuery query, final Class<T> c) {
		JsonReturnData<T> result = new JsonReturnData<T>();

		try {
			final String response = _executeSearch(index, query);

			ObjectMapper mapper = new ObjectMapper();
			T queryResult = mapper.readValue(response, c);
			
			result.setContent(queryResult);
			result.setOK();

		} catch (final Exception e) {
			logger.error("Exception", e);
			result.setError(e);
		}

		return result;
	}

	/**
	 * Generic method to execute a query and fetch the results.
	 * <p>The results of this method are templated on the QueryResult type and is the recommended
	 * approach.</p>
	 *
	 * @param index Names the index the results are to be obtained from.
	 * @param query Details the query to be execited (in POJO format).
	 *
	 * @return The results of the query as JSON data.
	 */
	public <T> JsonReturnData<QueryResult<T>> _getResults2(final String index, final ElasticQuery query, final Class<T> c, TypeReference<?> r) {
		JsonReturnData<QueryResult<T>> result = new JsonReturnData<QueryResult<T>>();

		try {
			final String response = _executeSearch(index, query);

			ObjectMapper mapper = new ObjectMapper();
			QueryResult<T> queryResult = mapper.readValue(response, r);
			
			result.setContent(queryResult);
			result.setOK();

		} catch (final Exception e) {
			logger.error("Exception", e);
			result.setError(e);
		}

		return result;
	}

	/**
	 * Execute the specified query against the specified index.
	 *
	 * @param index Names the index the search is to be executed against.
	 * @param query Details the query (in POJO format).
	 *
	 * @return The response to the query as a JSON string.
	 * @throws Exception 
	 *
	 */
	public String _executeSearch(final String index,
								  final ElasticQuery query) throws Exception {
		assert index != null;
		assert query != null;
		if (restClient == null) {
			throw new UnknownHostException("Unknown host");
		}
		
		// Setting the request arguments
		String method = "GET";
		String endpoint = index+"/_search";	
		
		// Map the Java pojo query to json
		ObjectWriter ow = new ObjectMapper().writer();
		String queryAsString = ow.writeValueAsString(query);
		
		HttpEntity entity = new NStringEntity(queryAsString, ContentType.APPLICATION_JSON);
		
		// Making request and getting response
		Response response = restClient.performRequest(method, endpoint, Collections.<String, String>emptyMap(), entity);
		
		// Returning response to the query as a JSON string
		String responseString = EntityUtils.toString(response.getEntity());		
		return responseString;
	}
	
}


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

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

/**
 * Singleton that holds Elasticsearch rest client
 * 
 * @author Hrvoje
 *
 */
public class ElasticsearchRestClientSingleton {
	
	private static ElasticsearchRestClientSingleton instance = null;
	private RestClient restClient = null;
	
	private ElasticsearchRestClientSingleton() {}
	
	/**
	 * Getting instance
	 * 
	 * @param hostName
	 * @param portNumber
	 * 
	 * @return Singleton instance with all necessary data
	 */
	public static ElasticsearchRestClientSingleton getInstance(final String hostName, final int portNumber, final String scheme) {
		if(instance == null) {
			instance = new ElasticsearchRestClientSingleton();
			instance.setRestClient(RestClient.builder(new HttpHost(hostName, portNumber, scheme)).build());
		}
		return instance;
	}
	
	/**
	 * Getting RestClient
	 * 
	 * @return RestClient
	 */
	public RestClient getRestClient() {
		return this.restClient;
	}
	
	/**
	 * Setting rest client
	 * 
	 * @param restClient
	 */
	private void setRestClient(RestClient restClient) {
		this.restClient = restClient;
	}
}

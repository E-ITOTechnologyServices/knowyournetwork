
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

import hr.eito.kynkite.JsonData;
import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;
import hr.eito.model.hostreport.EventDescription;
import hr.eito.model.newsticker.NewsTickerQueryResultField;

/**
 * Database Access Object interface.
 *
 * @author Danijel Soltic, Marko
 */
public interface ElasticSearchDao {
	
	/**
	 * Get Netflow data by IP address.
	 * <p>
	 * I think this is something to do with the "Network Traffic Visualization | Graphs".
	 *
	 * @param ipAddress the IP address for which to get the data
	 *
	 * @return the query results
	 */
	JsonReturnData<JsonData> netflow(String ipAddress);
	
	/**
	 * Get Netflow data by IP address and date time range.
	 * <p>
	 * I think this is something to do with the "Network Traffic Visualization | Graphs".
	 *
	 * @param ipAddress the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the netflow data
	 */
	JsonReturnData<JsonData> netflow(String ipAddress, String dateTimeFrom, String dateTimeTo);

	/**
	 * Get the Host Report data.
	 *
	 * @return host report data
	 */
	JsonReturnData<JsonNode> hostReport();

	/**
	 * Get the Host Report data for the specified IP address.
	 *
	 * @param ipAddress the IP address to search for
	 *
	 * @return host report data
	 */
	JsonReturnData<JsonNode> hostReport(String ipAddress);
	
	/**
	 * Get Host report data for the specified dateTimeFrom and dateTimeTo.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the host report data
	 */
	JsonReturnData<JsonNode> hostReport(String dateTimeFrom, String dateTimeTo);
	
	/**
	 * Get Host report data for the specified query IP, dateTimeFrom and dateTimeTo.
	 *
	 * @param ipAddress  the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the host report data
	 */
	JsonReturnData<JsonNode> hostReport(String ipAddress, String dateTimeFrom, String dateTimeTo);

	/**
	 * Get the Host Details report for the specified IP address.
	 *
	 * @param ipAddress the IP address to search for
	 *
	 * @return host details report data
	 */
	JsonReturnData<JsonNode> hostDetailsReport(String ipAddress);
	
	/**
	 * Get Host Details report data for the specified query IP, dateTimeFrom and dateTimeTo.
	 *
	 * @param ipAddress  the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the host report data
	 */
	JsonReturnData<JsonNode> hostDetailsReport(String ipAddress, String dateTimeFrom, String dateTimeTo);

	/**
	 * Get event description information for all available descriptions.
	 *
	 * @return the description information
	 */
	JsonReturnData<QueryResult<EventDescription>> loadEventDescriptions();

	/**
	 * Get a limited number of newsticker records.
	 *
	 * @param size the maximum number of records to be returned
	 *
	 * @return the newsticker data
	 */
	JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(int size);
	
	/**
	 * Get a limited number of newsticker records for a specified search term.
	 *
	 * @param size the maximum number of records to be returned
	 * @param param the search term
	 *
	 * @return the newsticker data
	 */
	JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(int size, String param);
	
	/**
	 * Get limited Newsticker data for the specified dateTimeFrom and dateTimeTo.
	 * 	 
	 * @param size the maximum number of records to be returned
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the host report data
	 */
	JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(int size, String dateTimeFrom, String dateTimeTo);
	
	/**
	 * Get limited Newsticker data for the specified query IP, dateTimeFrom and dateTimeTo.
	 *
	 * @param size the maximum number of records to be returned
	 * @param ipAddress  the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the host report data
	 */
	JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(int size, String ipAddress, String dateTimeFrom, String dateTimeTo);

	/**
	 * Get the CMDB report for the specified IP address.
	 *
	 * @param ipAddress the IP address to look up
	 *
	 * @return CMDB information
	 */
	JsonReturnData<JsonNode> cmdbReport(final String ipAddress);
	
}

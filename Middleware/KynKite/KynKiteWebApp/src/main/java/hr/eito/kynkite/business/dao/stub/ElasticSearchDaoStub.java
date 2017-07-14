
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

import hr.eito.kynkite.JsonData;
import hr.eito.kynkite.business.dao.ElasticSearchDao;
import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;
import hr.eito.model.hostreport.EventDescription;
import hr.eito.model.newsticker.NewsTickerQueryResultField;

/**
 * Stubbed out implementation of ElasticSearch query methods. This loads data from files that are
 * stored in specific locations depending on the query type and parameters supplied.
 *
 * @author Danijel Soltic
 */
@Component
@Profile("test")
public class ElasticSearchDaoStub implements ElasticSearchDao {
	
	@Autowired
	private StubManager stubManager;

	/**
	 * Stub out the fetching of event description data.
	 *
	 * @return the report data
	 */
	@Override
	public JsonReturnData<QueryResult<EventDescription>> loadEventDescriptions() {
		final String location = "eventdescriptions";

		return stubManager.reportResults_(location, EventDescription.class, new TypeReference<QueryResult<EventDescription>>() {});
	}

	/**
	 * Stub out the fetching of netflow data by IP.
	 *
	 * @param ipAddress the IP address
	 *
	 * @return the report data
	 */
	@Override
	public JsonReturnData<JsonData> netflow(final String ipAddress) {
		final String location = "byip/" + ipAddress;
		return stubManager.reportJsonData_(location, JsonData.class);
	}
	
	/**
	 * Stub out the fetching of netflow data by IP and date time range.
	 *
	 * @param ipAddress the IP address
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 * 
	 * @return the report data
	 */
	@Override
	public JsonReturnData<JsonData> netflow(final String ipAddress, String dateTimeFrom, String dateTimeTo) {
		final String location = "byip/" + ipAddress + "/" 
					+ stubManager.dateForFolderName(dateTimeFrom) 
					+ "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportJsonData_(location, JsonData.class);
	}

	/**
	 * Stub out the fetching of host report data.
	 *
	 * @return the report data
	 */
	@Override
	public JsonReturnData<JsonNode> hostReport() {
		final String location = "hostreport";

		return stubManager.reportData_(location);
	}

	/**
	 * Stub out the fetching of host report data for a specified IP address.
	 *
	 * @param ipAddress the ip address for which to fetch the report data
	 *
	 * @return the report data
	 */
	@Override
	public JsonReturnData<JsonNode> hostReport(String ipAddress) {
		final String location = "hostreport/" + ipAddress;
		return stubManager.reportData_(location);
	}
	
	/**
	 * Get dummy, test Host report data for the specified date time range.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<JsonNode> hostReport(String dateTimeFrom, String dateTimeTo) {
		final String location = "hostreport/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportData_(location);
	}
	
	/**
	 * Get dummy, test Host report data for the specified query and date time range.
	 *
	 * @param ipAddress the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<JsonNode> hostReport(String ipAddress, String dateTimeFrom, String dateTimeTo) {
		final String location = "hostreport/" + ipAddress + "/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportData_(location);
	}

	/**
	 * Stub out the fetching of host details report data for a specified IP address.
	 *
	 * @param ipAddress the ip address for which to fetch the report data
	 *
	 * @return the report data
	 */
	@Override
	public JsonReturnData<JsonNode> hostDetailsReport(final String ipAddress) {
		final String location = "hostdetailsreport/" + ipAddress;

		return stubManager.reportData_(location);
	}
	
	/**
	 * Get dummy, test Host Details report data for the specified query and date time range.
	 *
	 * @param ipAddress the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<JsonNode> hostDetailsReport(String ipAddress, String dateTimeFrom, String dateTimeTo) {
		final String location = "hostdetailsreport/" + ipAddress + "/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportData_(location);
	}

	/**
	 * Stub out the fetching of CMDB report data for a specified IP address.
	 *
	 * @param ipAddress the ip address for which to fetch the report data
	 *
	 * @return the report data
	 */
	@Override
	public JsonReturnData<JsonNode> cmdbReport(final String ipAddress) {
		final String location = "cmdbreport/" + ipAddress;

		return stubManager.reportData_(location);
	}

	/**
	 * Get dummy, test Newsticker data.
	 *
	 * @param size the number of records to return
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(final int size) {
		final String location = "newsticker/" + size;
		return stubManager.reportResults_(location, NewsTickerQueryResultField.class, new TypeReference<QueryResult<NewsTickerQueryResultField>>() {});
	}

	/**
	 * Get dummy, test Newsticker data.
	 *
	 * @param size the number of records to return
	 * @param query the query string to search for
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(final int size, final String query) {
		final String location = "newsticker/" + size + "/" + query;
		return stubManager.reportResults_(location, NewsTickerQueryResultField.class, new TypeReference<QueryResult<NewsTickerQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Newsticker data for the specified date time range.
	 *
	 * @param size the number of records to return
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(int size, String dateTimeFrom, String dateTimeTo) {
		final String location = "newsticker/" + size + "/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, NewsTickerQueryResultField.class, new TypeReference<QueryResult<NewsTickerQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Newsticker data for the specified query and date time range.
	 *
	 * @param size the number of records to return
	 * @param ipAddress the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<NewsTickerQueryResultField>> newsticker(int size, String ipAddress, String dateTimeFrom, String dateTimeTo) {
		final String location = "newsticker/" + size + "/" + ipAddress + "/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, NewsTickerQueryResultField.class, new TypeReference<QueryResult<NewsTickerQueryResultField>>() {});
	}

}

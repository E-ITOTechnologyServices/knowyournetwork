
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

import hr.eito.kynkite.business.dao.SyslogDao;
import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;
import hr.eito.model.syslog.SyslogASAQueryResultField;
import hr.eito.model.syslog.SyslogDHCPQueryResultField;
import hr.eito.model.syslog.SyslogIPSQueryResultField;
import hr.eito.model.syslog.SyslogProxyQueryResultField;
import hr.eito.model.syslog.SyslogRouterQueryResultField;
import hr.eito.model.syslog.SyslogVoiceQueryResultField;

/**
 * Syslog data is fetched from resource test data files, and not Elasticsearch queries
 *
 * @author Hrvoje
 */
@Component
@Profile("test")
public class SyslogDaoStub implements SyslogDao {
	
	@Autowired
	private StubManager stubManager;
	
	/**
	 * Get dummy, test Syslog Router data.
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter() {
		final String location = "syslog/router";

		return stubManager.reportResults_(location, SyslogRouterQueryResultField.class, new TypeReference<QueryResult<SyslogRouterQueryResultField>>() {});
	}

	/**
	 * Get dummy, test Syslog Router data for the specified query.
	 *
	 * @param query the query parameter
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter(final String query) {
		final String location = "syslog/router/" + query;

		return stubManager.reportResults_(location, SyslogRouterQueryResultField.class, new TypeReference<QueryResult<SyslogRouterQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog Router data for the specified date time range.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter(String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/router/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogRouterQueryResultField.class, new TypeReference<QueryResult<SyslogRouterQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog Router data for the specified query and date time range.
	 *
	 * @param query the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter(String query, String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/router/" + query + "/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogRouterQueryResultField.class, new TypeReference<QueryResult<SyslogRouterQueryResultField>>() {});
	}

	/**
	 * Get dummy, test Syslog Voice data.
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice() {
		final String location = "syslog/voice";

		return stubManager.reportResults_(location, SyslogVoiceQueryResultField.class, new TypeReference<QueryResult<SyslogVoiceQueryResultField>>() {});
	}

	/**
	 * Get dummy, test Syslog Voice data for the specified query.
	 *
	 * @param query the query parameter
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice(final String query) {
		final String location = "syslog/voice/" + query;

		return stubManager.reportResults_(location, SyslogVoiceQueryResultField.class, new TypeReference<QueryResult<SyslogVoiceQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog Voice data for the specified date time range.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice(String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/voice/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogVoiceQueryResultField.class, new TypeReference<QueryResult<SyslogVoiceQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog Voice data for the specified query and date time range.
	 *
	 * @param query the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice(String query, String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/voice/" + query + "/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogVoiceQueryResultField.class, new TypeReference<QueryResult<SyslogVoiceQueryResultField>>() {});
	}

	/**
	 * Get dummy, test Syslog ASA data.
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA() {
		final String location = "syslog/asa";

		return stubManager.reportResults_(location, SyslogASAQueryResultField.class, new TypeReference<QueryResult<SyslogASAQueryResultField>>() {});
	}

	/**
	 * Get dummy, test Syslog ASA data for the specified query.
	 *
	 * @param query the query parameter
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA(final String query) {
		final String location = "syslog/asa/" + queryAsValidIP(query);

		return stubManager.reportResults_(location, SyslogASAQueryResultField.class, new TypeReference<QueryResult<SyslogASAQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog ASA data for the specified date time range.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA(String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/asa/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogASAQueryResultField.class, new TypeReference<QueryResult<SyslogASAQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog ASA data for the specified query and date time range.
	 *
	 * @param query the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA(String query, String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/asa/" + query + "/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogASAQueryResultField.class, new TypeReference<QueryResult<SyslogASAQueryResultField>>() {});
	}
	
	/**
	 * For parts which request valid IP
	 * <p>
	 * If the query is empty, we want empty result set path
	 * 
	 * @param query Query expression which will be checked
	 * @return the same query expression if string not empty
	 */
	private String queryAsValidIP(final String query) {
		return query.equals("")?"invalid_ip":query;
	}

	/**
	 * Get dummy, test Syslog IPS data.
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS() {
		final String location = "syslog/ips";

		return stubManager.reportResults_(location, SyslogIPSQueryResultField.class, new TypeReference<QueryResult<SyslogIPSQueryResultField>>() {});
	}

	/**
	 * Get dummy, test Syslog IPS data for the specified query.
	 *
	 * @param query the query parameter
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS(final String query) {
		final String location = "syslog/ips/" + query;

		return stubManager.reportResults_(location, SyslogIPSQueryResultField.class, new TypeReference<QueryResult<SyslogIPSQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog IPS data for the specified date time range.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS(String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/ips/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogIPSQueryResultField.class, new TypeReference<QueryResult<SyslogIPSQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog IPS data for the specified query and date time range.
	 *
	 * @param query the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS(String query, String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/ips/" + query + "/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogIPSQueryResultField.class, new TypeReference<QueryResult<SyslogIPSQueryResultField>>() {});
	}

	/**
	 * Get dummy, test Syslog DHCP data.
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP() {
		final String location = "syslog/dhcp";

		return stubManager.reportResults_(location, SyslogDHCPQueryResultField.class, new TypeReference<QueryResult<SyslogDHCPQueryResultField>>() {});
	}

	/**
	 * Get dummy, test Syslog DHCP data for the specified query.
	 *
	 * @param query the query parameter
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP(String query) {
		final String location = "syslog/dhcp/" + query;

		return stubManager.reportResults_(location, SyslogDHCPQueryResultField.class, new TypeReference<QueryResult<SyslogDHCPQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog DHCP data for the specified date time range.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP(String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/dhcp/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogDHCPQueryResultField.class, new TypeReference<QueryResult<SyslogDHCPQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog DHCP data for the specified query and date time range.
	 *
	 * @param query the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP(String query, String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/dhcp/" + query + "/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogDHCPQueryResultField.class, new TypeReference<QueryResult<SyslogDHCPQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog Proxy data.
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy() {
		final String location = "syslog/proxy";

		return stubManager.reportResults_(location, SyslogProxyQueryResultField.class, new TypeReference<QueryResult<SyslogProxyQueryResultField>>() {});
	}

	/**
	 * Get dummy, test Syslog Proxy data for the specified query.
	 *
	 * @param query the query parameter
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy(String query) {
		final String location = "syslog/proxy/" + query;

		return stubManager.reportResults_(location, SyslogProxyQueryResultField.class, new TypeReference<QueryResult<SyslogProxyQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog Proxy data for the specified date time range.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy(String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/proxy/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogProxyQueryResultField.class, new TypeReference<QueryResult<SyslogProxyQueryResultField>>() {});
	}
	
	/**
	 * Get dummy, test Syslog Proxy data for the specified query and date time range.
	 *
	 * @param query the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo
	 *
	 * @return the dummy report data.
	 */
	@Override
	public JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy(String query, String dateTimeFrom, String dateTimeTo) {
		final String location = "syslog/proxy/" + query + "/" + stubManager.dateForFolderName(dateTimeFrom) + "/" + stubManager.dateForFolderName(dateTimeTo);
		return stubManager.reportResults_(location, SyslogProxyQueryResultField.class, new TypeReference<QueryResult<SyslogProxyQueryResultField>>() {});
	}
	
}

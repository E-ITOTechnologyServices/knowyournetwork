
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

import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;
import hr.eito.model.syslog.SyslogASAQueryResultField;
import hr.eito.model.syslog.SyslogDHCPQueryResultField;
import hr.eito.model.syslog.SyslogIPSQueryResultField;
import hr.eito.model.syslog.SyslogProxyQueryResultField;
import hr.eito.model.syslog.SyslogRouterQueryResultField;
import hr.eito.model.syslog.SyslogVoiceQueryResultField;

/**
 * Syslog Database Access Object interface.
 *
 * @author Hrvoje
 */
public interface SyslogDao {
	
	/**
	 * Get Syslog ASA data.
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA();

	/**
	 * Get Syslog ASA data for the specified query parameter.
	 *
	 * @param query the query parameter
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA(String query);
	
	/**
	 * Get Syslog ASA data for the specified dateTimeFrom and dateTimeTo.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA(String dateTimeFrom, String dateTimeTo);
	
	/**
	 * Get Syslog ASA data for the specified query IP, dateTimeFrom and dateTimeTo.
	 *
	 * @param query the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogASAQueryResultField>> syslogASA(String query, String dateTimeFrom, String dateTimeTo);
	
	
	/**
	 * Get Syslog Router data.
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter();

	/**
	 * Get Syslog Router data for the specified query parameter.
	 *
	 * @param query the query parameter
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter(String query);
	
	/**
	 * Get Syslog Router data for the specified dateTimeFrom and dateTimeTo.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter(String dateTimeFrom, String dateTimeTo);
	
	/**
	 * Get Syslog Router data for the specified query IP, dateTimeFrom and dateTimeTo.
	 *
	 * @param query  the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogRouterQueryResultField>> syslogRouter(String query, String dateTimeFrom, String dateTimeTo);
	
	
	/**
	 * Get Syslog Voice data.
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice();

	/**
	 * Get Syslog Voice data for the specified query parameter.
	 *
	 * @param query the query parameter
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice(String query);
	
	/**
	 * Get Syslog Voice data for the specified dateTimeFrom and dateTimeTo.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice(String dateTimeFrom, String dateTimeTo);
	
	/**
	 * Get Syslog Voice data for the specified query IP, dateTimeFrom and dateTimeTo.
	 *
	 * @param query  the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> syslogVoice(String query, String dateTimeFrom, String dateTimeTo);
	
	
	/**
	 * Get Syslog IPS data.
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS();

	/**
	 * Get Syslog IPS data for the specified query parameter.
	 *
	 * @param query the query parameter
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS(String query);
	
	/**
	 * Get Syslog IPS data for the specified dateTimeFrom and dateTimeTo.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS(String dateTimeFrom, String dateTimeTo);
	
	/**
	 * Get Syslog IPS data for the specified query IP, dateTimeFrom and dateTimeTo.
	 *
	 * @param query  the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogIPSQueryResultField>> syslogIPS(String query, String dateTimeFrom, String dateTimeTo);
	
	
	/**
	 * Get Syslog DHCP data.
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP();

	/**
	 * Get Syslog DHCP data for the specified query parameter.
	 *
	 * @param query the query parameter
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP(String query);
	
	/**
	 * Get Syslog DHCP data for the specified dateTimeFrom and dateTimeTo.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP(String dateTimeFrom, String dateTimeTo);
	
	/**
	 * Get Syslog DHCP data for the specified query IP, dateTimeFrom and dateTimeTo.
	 *
	 * @param query  the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> syslogDHCP(String query, String dateTimeFrom, String dateTimeTo);
	
	/**
	 * Get Syslog Proxy data.
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy();

	/**
	 * Get Syslog Proxy data for the specified query parameter.
	 *
	 * @param query the query parameter
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy(String query);
	
	/**
	 * Get Syslog Proxy data for the specified dateTimeFrom and dateTimeTo.
	 *
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy(String dateTimeFrom, String dateTimeTo);
	
	/**
	 * Get Syslog Proxy data for the specified query IP, dateTimeFrom and dateTimeTo.
	 *
	 * @param query  the query parameter
	 * @param dateTimeFrom
	 * @param dateTimeTo 
	 *
	 * @return the syslog data
	 */
	JsonReturnData<QueryResult<SyslogProxyQueryResultField>> syslogProxy(String query, String dateTimeFrom, String dateTimeTo);

}

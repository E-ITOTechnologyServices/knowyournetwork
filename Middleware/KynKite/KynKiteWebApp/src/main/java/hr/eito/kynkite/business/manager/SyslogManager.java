
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


package hr.eito.kynkite.business.manager;

import hr.eito.model.JsonReturnData;
import hr.eito.model.syslog.SyslogASAReturnResult;
import hr.eito.model.syslog.SyslogDHCPReturnResult;
import hr.eito.model.syslog.SyslogIPSReturnResult;
import hr.eito.model.syslog.SyslogParams;
import hr.eito.model.syslog.SyslogProxyQueryResultField;
import hr.eito.model.syslog.SyslogProxyReturnResultData;
import hr.eito.model.syslog.SyslogReturnResult;
import hr.eito.model.syslog.SyslogRouterReturnResult;
import hr.eito.model.syslog.SyslogVoiceReturnResult;

/**
 * Interface specification for the Syslog manager
 *
 * @author Hrvoje
 */
public interface SyslogManager {

	/**
	 * Fetch Syslog ASA data using the specified query parameter.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return JSON report data
	 */
	JsonReturnData<SyslogASAReturnResult> syslogASA(SyslogParams params);

	/**
	 * Fetch Syslog Router data using the specified query parameter.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return JSON report data
	 */
	JsonReturnData<SyslogRouterReturnResult> syslogRouter(SyslogParams params);

	/**
	 * Fetch Syslog Voice data using the specified query parameter.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return JSON report data
	 */
	JsonReturnData<SyslogVoiceReturnResult> syslogVoice(SyslogParams params);

	/**
	 * Fetch Syslog IPS data using the specified query parameter.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return JSON report data
	 */
	JsonReturnData<SyslogIPSReturnResult> syslogIPS(SyslogParams params);

	/**
	 * Fetch Syslog DHCP data using the specified query parameter.
	 *
	 * @param params Parameters to be received in POST request
	 *
	 * @return JSON report data
	 */
	JsonReturnData<SyslogDHCPReturnResult> syslogDHCP(SyslogParams params);

	/**
	 * Fetch Syslog Proxy data using the parameters
	 *
	 * @param params Parameters to be received in POST request
	 *
	 * @return JSON report data
	 */
	JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData,SyslogProxyQueryResultField>> syslogProxy(SyslogParams params);
}


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


package hr.eito.kynkite.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hr.eito.kynkite.business.manager.SyslogManager;
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
 * Rest endpoint for Syslog inquiries
 *
 * @author Hrvoje
 *
 */
@RestController
public class SyslogController {
	
	@Autowired
	SyslogManager syslogManager;

	/**
	 * Execute and return data for the parameterized Syslog ASA report.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return Syslog ASA report as JSON
	 */
	@RequestMapping(value = "/syslogASA", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<SyslogASAReturnResult> syslogASA(@RequestBody SyslogParams params) {
		JsonReturnData<SyslogASAReturnResult> result = this.syslogManager.syslogASA(params);
		return result;
	}

	/**
	 * Execute and return data for the parameterized Syslog Router report.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return Syslog Router report as JSON
	 */
	@RequestMapping(value = "/syslogRouter", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<SyslogRouterReturnResult> syslogRouter(@RequestBody SyslogParams params) {
		JsonReturnData<SyslogRouterReturnResult> result = this.syslogManager.syslogRouter(params);
		return result;
	}

	/**
	 * Execute and return data for the parameterized Syslog Voice report.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return Syslog Voice report as JSON
	 */
	@RequestMapping(value = "/syslogVoice", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<SyslogVoiceReturnResult> syslogVoice(@RequestBody SyslogParams params) {
		JsonReturnData<SyslogVoiceReturnResult> result = this.syslogManager.syslogVoice(params);
		return result;
	}

	/**
	 * Execute and return data for the parameterized Syslog DHCP report.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return Syslog DHCP report as JSON
	 */
	@RequestMapping(value = "/syslogDHCP", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<SyslogDHCPReturnResult> syslogDHCP(@RequestBody SyslogParams params) {
		JsonReturnData<SyslogDHCPReturnResult> result = this.syslogManager.syslogDHCP(params);
		return result;
	}

	/**
	 * Execute and return data for the parameterized Syslog IPS report.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return Syslog IPS report as JSON
	 */
	@RequestMapping(value = "/syslogIPS", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<SyslogIPSReturnResult> syslogIPS(@RequestBody SyslogParams params) {
		JsonReturnData<SyslogIPSReturnResult> result = this.syslogManager.syslogIPS(params);
		return result;
	}
	
	/**
	 * Execute and return data for the Syslog Proxy report.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return Syslog Proxy report as JSON
	 */
	@RequestMapping(value = "/syslogProxy", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> syslogProxy(@RequestBody SyslogParams params) {
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result = this.syslogManager.syslogProxy(params);
		return result;
	}
}


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

import hr.eito.kynkite.business.manager.ElasticSearchManager;
import hr.eito.model.JsonReturnData;
import hr.eito.model.cmdb.CMDBParams;
import hr.eito.model.cmdb.CMDBReturnResult;
import hr.eito.model.hostreport.HostDetailsReportParams;
import hr.eito.model.hostreport.HostDetailsReportReturnResult;
import hr.eito.model.hostreport.HostReportParams;
import hr.eito.model.hostreport.HostReportReturnResult;
import hr.eito.model.netflow.NetflowParams;
import hr.eito.model.newsticker.NewsTickerParams;
import hr.eito.model.newsticker.NewsTickerReturnResult;
import hr.eito.model.ntv.NetflowReturnResult;

/**
 * Creates the REST endpoints for the middleware. These are the URLs that the front end calls to get
 * data from ElasticSearch.
 *
 * @author Marko
 *
 */
@RestController
public class ElasticSearchController {

	@Autowired
	ElasticSearchManager elasticsSearchManager;
	
	/**
	 * Method for filtering data by IP address
	 *
	 * @param params the parameters for filtering data
	 *
	 * @return The result 
	 */
	@RequestMapping(value = "/resets", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<NetflowReturnResult> loadDataByIP(@RequestBody NetflowParams params) {
		return elasticsSearchManager.loadDataByIP(params);
	}

	/**
	 * Newsticker POST method. Takes a request body that is JSON.
	 *
	 * @param params the parameters for the query
	 *
	 * @return result data as JSON.
	 */
	@RequestMapping(value = "/newsTicker", method = RequestMethod.POST, headers = "Accept=application/json")
	JsonReturnData<NewsTickerReturnResult> newsticker(@RequestBody NewsTickerParams params) {
		JsonReturnData<NewsTickerReturnResult> newsTickerJson = this.elasticsSearchManager.newsticker(params);
		return newsTickerJson;
	}

	/**
	 * Execute and return data for the parameterized CMDB report.
	 *
	 * @param params the parameters for the query
	 *
	 * @return the CMDB report results
	 */
	@RequestMapping(value = "/cmdbReport", method = RequestMethod.POST, headers = "Accept=application/json")
	JsonReturnData<CMDBReturnResult> cmdbReport(@RequestBody CMDBParams params) {
		return elasticsSearchManager.cmdbReport(params);
	}
	
	/**
	 * HostReport POST method. Takes a request body that is JSON.
	 *
	 * @param params the parameters for the query
	 *
	 * @return result data as JSON.
	 */
	@RequestMapping(value = "/hostReport", method = RequestMethod.POST, headers = "Accept=application/json")
	JsonReturnData<HostReportReturnResult> hostReport(@RequestBody HostReportParams params) {
		JsonReturnData<HostReportReturnResult> hostReportJson = elasticsSearchManager.hostReport(params);
		return hostReportJson;
	}

	/**
	 * HostDetailsReport POST method. Takes a request body that is JSON.
	 *
	 * @param params the parameters for the query
	 *
	 * @return result data as JSON.
	 */
	@RequestMapping(value = "/hostDetailsReport", method = RequestMethod.POST, headers = "Accept=application/json")
	JsonReturnData<HostDetailsReportReturnResult> hostDetailsReport(@RequestBody HostDetailsReportParams params) {
		JsonReturnData<HostDetailsReportReturnResult> hostDetailsReportJson = elasticsSearchManager.hostDetailsReport(params);
		return hostDetailsReportJson;
	}
}

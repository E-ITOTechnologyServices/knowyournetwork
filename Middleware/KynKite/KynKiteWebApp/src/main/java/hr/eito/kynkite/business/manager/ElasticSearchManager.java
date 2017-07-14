
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
 * Interface specification for the Manager. This specifies the interface of any Impl that claims to
 * be able to return JSON report results.
 *
 * @author Danijel Soltic
 */
public interface ElasticSearchManager {

	/**
	 * Fetch the CMDB report data.
	 *
	 * @param params
	 *
	 * @return the search results
	 */
	JsonReturnData<CMDBReturnResult> cmdbReport(final CMDBParams params);

	/**
	 * Fetch host report data for a specified parameters
	 *
	 * @param params
	 *
	 * @return the search results
	 */
	JsonReturnData<HostReportReturnResult> hostReport(final HostReportParams params);

	/**
	 * Fetch host details report data for a specified IP address.
	 *
	 * @param params parameters to get data with
	 *
	 * @return the search results
	 */
	JsonReturnData<HostDetailsReportReturnResult> hostDetailsReport(final HostDetailsReportParams params);

	/**
	 * Fetch netflow information for specific parameter set
	 * <p>
	 * Feeds "Network Traffic Visualisation | Graphs".
	 *
	 * @param params encapsulated parameters
	 *
	 * @return netflow data for specified paramters
	 */
	JsonReturnData<NetflowReturnResult> loadDataByIP(NetflowParams params);

	/**
	 * Fetch newsticker data.
	 *
	 * @param params encapsulated parameters
	 *
	 * @return the newsticker report results
	 */
	JsonReturnData<NewsTickerReturnResult>  newsticker(final NewsTickerParams params);
}

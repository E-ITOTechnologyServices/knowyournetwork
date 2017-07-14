
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hr.eito.kynkite.business.manager.ReportManager;
import hr.eito.model.JsonReturnData;
import hr.eito.model.reports.ReportsParams;
import hr.eito.model.reports.ReportsReturnResult;
import hr.eito.model.reports.ReportsSingleParams;

/**
 * Rest endpoint for Reporting
 *
 * @author Hrvoje
 *
 */
@RestController
@RequestMapping(value = "/reports")
public class ReportController {
	
	@Autowired
	ReportManager reportManager;
	
	/**
	 * Create report Docker container
	 * 
	 * @param params Parameters needed for report creation
	 * 
	 * @return Response with report stream and headers for download
	 */
	@RequestMapping(value = "/createReport", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<String> createReportContainer(@RequestBody ReportsParams params) {
		return reportManager.createReport(params);
	}
	
	/**
	 * Get report by name
	 * 
	 * @return Response with report stream and headers for download
	 */
	@RequestMapping(value = "/getReportByName", method = RequestMethod.POST)
	public ResponseEntity<ByteArrayResource> getReportByName(@RequestBody ReportsSingleParams params) {
		return reportManager.getReportByName(params.getReportName());
	}
	
	/**
	 * Get list of reports from Docker volume
	 * 
	 * @return result data as JSON.
	 */
	@RequestMapping(value = "/getList", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<ReportsReturnResult> getReportList() {
		JsonReturnData<ReportsReturnResult> reportList = this.reportManager.getReportList();
		return reportList;
	}

}


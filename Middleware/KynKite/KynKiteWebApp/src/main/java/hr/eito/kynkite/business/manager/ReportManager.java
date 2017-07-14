
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

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import hr.eito.model.JsonReturnData;
import hr.eito.model.reports.ReportsParams;
import hr.eito.model.reports.ReportsReturnResult;

/**
 * Interface specification for the Docker manager
 *
 * @author Hrvoje
 */
public interface ReportManager {
	
	/**
	 * Create report by starting docker container
	 * 
	 * @param params Parameters for creating report
	 * 
	 * @return JSON info entity
	 */
	JsonReturnData<String> createReport(final ReportsParams params);
	
	/**
	 * Getting the PDF report by report name
	 * 
	 * @param reportName report name
	 * 
	 * @return Response with report stream and appropriate headers
	 */
	ResponseEntity<ByteArrayResource> getReportByName(final String reportName);
	
	/**
	 * Manage getting list of reports from Docker volume
	 *
	 * @return the report name list
	 */
	JsonReturnData<ReportsReturnResult> getReportList();
	
}

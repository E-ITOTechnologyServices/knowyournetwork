
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hr.eito.kynkite.business.dao.DockerDao;
import hr.eito.kynkite.utils.CustomError;
import hr.eito.model.DateTimeRange;
import hr.eito.model.JsonReturnData;
import hr.eito.model.ModelDateConverter;
import hr.eito.model.reports.ReportsParams;
import hr.eito.model.reports.ReportsReturnResult;

/**
 * Report manager implementation
 *
 * @author Hrvoje
 */
@Component
public class ReportManagerImpl implements ReportManager {
	
	private ModelDateConverter modelDateConverter = new ModelDateConverter();
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private static final String PATH_TO_REPORTS = "src/files/reports/";
	
	@Autowired 
	private DockerDao dockerManager;
	
	@Autowired
	FileManager fileManager;
	
	/**
	 * Full process of creating and getting report
	 * 
	 * @param params Parameters for creating report
	 * 
	 * @return JSON info entity
	 */
	@Override
	public JsonReturnData<String> createReport(final ReportsParams params) {
		// If parameters missing
		if (params==null) {
			JsonReturnData<String> parametersMissing = new JsonReturnData<>();
			parametersMissing.setErrorMessage(CustomError.PARAMETERS_MISSING.getErrorMessage());
			return parametersMissing;
		}
		// Setting the parameters variables
		String maxDestSyn = params.getMaxDestSyn();
		String maxDestIpPing = params.getMaxDestIpPing();
		DateTimeRange dateTimeRange = params.getDateTimeRange();
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange==null || dateTimeRange.isBroken()) {
			JsonReturnData<String> errorReportInfo = new JsonReturnData<>();
			errorReportInfo.setErrorMessage(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
			return errorReportInfo;
		}
		
		JsonReturnData<String> createReportInfo = new JsonReturnData<>();
		
		try {
			// Preparing report name
			String reportName = this.generateReportName(dateTimeRange.getDateTimeFrom(), dateTimeRange.getDateTimeTo());
			
			if (!this.checkExistenceOfReport(reportName)) {
				// Preparing startdate and enddate
				String startDate = modelDateConverter.toMidnightDateString(dateTimeRange.getDateTimeFrom());
				String endDate = modelDateConverter.toMidnightDateString(dateTimeRange.getDateTimeTo());
				
				// Create container
				String entryPointCommand = "/Reports/run.sh";
		        String containerName = "kyn_reports_" + modelDateConverter.toTimeSuffix(new Date());
		        String mountPoint = "/out";
		        String imageName = "kyn/reports";
		        String volumeName = "kyn_ReportsData";
		        dockerManager.createReportContainer(containerName, mountPoint, imageName, volumeName, 
		        		entryPointCommand, startDate, endDate, maxDestSyn, maxDestIpPing, reportName);
		        
		        // Checking if report is created
		        if(!this.checkExistenceOfReport(reportName)) {
		        	createReportInfo.setErrorMessage(CustomError.REPORT_NOT_CREATED.getErrorMessage());
		        } else {
		        	createReportInfo.setOK();
		        }
			} else {
				createReportInfo.setErrorMessage(CustomError.REPORT_ALREADY_EXISTS.getErrorMessage());
			}
		} catch (Exception e) {
			createReportInfo.setError(e);
		}
		
		return createReportInfo;
	}
	
	/**
	 * Getting the PDF report by report name
	 * 
	 * @param reportName report name
	 * 
	 * @return Response with report stream and appropriate headers
	 */
	@Override
	public ResponseEntity<ByteArrayResource> getReportByName(final String reportName) {
		File file = this.fileManager.getFile(PATH_TO_REPORTS + reportName);
		
		ResponseEntity<ByteArrayResource> response;
		try {
			FileInputStream fis = new FileInputStream(file);			
			try {
				// Set up response header
				HttpHeaders headers = new HttpHeaders();
		  		headers.setContentType(MediaType.parseMediaType("application/pdf"));
		  		headers.add("Access-Control-Allow-Origin", "*");
		  		headers.add("Access-Control-Allow-Methods", "GET, POST, PUT");
		  		headers.add("Access-Control-Allow-Headers", "Content-Type");
				headers.add("Content-Disposition", "attachment; filename=" + reportName);
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");
				
				// Set content length in header
				headers.setContentLength(file.length());
				
				// Create byte array and save file content into it
				byte [] content = new byte[(int)file.length()];
		        IOUtils.read(fis, content);
				
		        // Create ByteArrayResource based on byte array content
				ByteArrayResource byteArrayResource = new ByteArrayResource(content);
				
				// Create response entity with resource and headers
				response = new ResponseEntity<>(byteArrayResource, headers, HttpStatus.OK);
			} finally {
				if (fis!=null) {
					fis.close();
				}
			}
		} catch(IOException e) {
			response = this.getResponseError(e.getMessage());
		}
		
		return response;
	}
	
	/**
	 * Manage getting list of all reports
	 *
	 * @return the report name list
	 */
	@Override
	public JsonReturnData<ReportsReturnResult> getReportList() {
		// Get list of current reports
		List<String> listOfFileNames = this.getListOfPdfReports();
		
		return new JsonReturnData<ReportsReturnResult>(new ReportsReturnResult(listOfFileNames));
	}
	
	/**
	 * Method for generating report name
	 * 
	 * @param dateTimeFrom 
	 * 
	 * @return report name
	 */
	private String generateReportName(Date dateTimeFrom, Date dateTimeTo) {
		// Get report name base out of dates
	    String reportName = new StringBuilder().append("report_")
	    		.append(modelDateConverter.toOrderedDateString(dateTimeFrom))
	    		.append("_")
	    		.append(modelDateConverter.toOrderedDateString(dateTimeTo))
	    		.append(".pdf")
	    		.toString();
	    
		return reportName;
	}
	
	/**
	 * Encapsulate error in readable JSON format and stream it in a reponse entity
	 * 
	 * @param errorMessage
	 * 
	 * @return Response entity as JSON
	 */
	private ResponseEntity<ByteArrayResource> getResponseError(String errorMessage) {
		// Preparing the JSON return object
		JsonReturnData<String> jsonReturnData = new JsonReturnData<>();
		jsonReturnData.setErrorMessage(errorMessage);
		
		// Converting JSON return object to String
		String returnString;
		try {
			returnString = objectMapper.writeValueAsString(jsonReturnData);
		} catch (JsonProcessingException e) {
			returnString = errorMessage;
		}
		
		// Preparing readable response headers
		HttpHeaders headers = new HttpHeaders();
  		headers.setContentType(MediaType.parseMediaType("application/json"));
  		headers.add("Access-Control-Allow-Headers", "Content-Type");
		
  		try {
	  		// Create input stream out of String
			InputStream stream = new ByteArrayInputStream(returnString.getBytes(StandardCharsets.UTF_8));			
			try {
				// Create byte array and save String stream content into it
				byte [] content = new byte[returnString.length()];
		        IOUtils.read(stream, content);
				
		        // Create ByteArrayResource based on byte array content
				ByteArrayResource byteArrayResource = new ByteArrayResource(content);
				
				// Create response entity with resource and headers
				ResponseEntity<ByteArrayResource> response = new ResponseEntity<>(byteArrayResource, headers, HttpStatus.OK);
				return response;
			} finally {
				if (stream!=null) {
					stream.close();
				}
			}
  		} catch (IOException ioe) {
  			return null;
  		}
	}
	
	/**
	 * Checking if report with name reportName is in Volume
	 * 
	 * @param reportName name of the report
	 * 
	 * @return true if report exists
	 */
	private boolean checkExistenceOfReport(String reportName) {
		// Get list of current reports
		List<String> listOfFileNames = this.getListOfPdfReports();
		// Check if report with name reportName exists
		for(String name : listOfFileNames) {
			if (StringUtils.equals(reportName, name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Getting list of Reports which are only .pdf files
	 * 
	 * @return list of .pdf reports
	 */
	private List<String> getListOfPdfReports() {
		final String fileExtension = ".pdf";
		List<String> listOfFileNames = fileManager.getListOfFileNames(PATH_TO_REPORTS);
		return listOfFileNames.stream()
				.filter(line -> line.endsWith(fileExtension))
				.collect(Collectors.toList()); 
	}
	
}

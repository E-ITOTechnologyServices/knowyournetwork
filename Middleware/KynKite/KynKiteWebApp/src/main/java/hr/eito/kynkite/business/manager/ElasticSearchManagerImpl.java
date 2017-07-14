
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import hr.eito.kynkite.JsonData;
import hr.eito.kynkite.business.dao.ElasticSearchDao;
import hr.eito.kynkite.utils.CustomError;
import hr.eito.model.DateTimeRange;
import hr.eito.model.Hit;
import hr.eito.model.JsonReturnData;
import hr.eito.model.ModelDateConverter;
import hr.eito.model.QueryResult;
import hr.eito.model.cmdb.CMDBParams;
import hr.eito.model.cmdb.CMDBReturnResult;
import hr.eito.model.hostreport.EventDescription;
import hr.eito.model.hostreport.HostDetailsReportParams;
import hr.eito.model.hostreport.HostDetailsReportReturnResult;
import hr.eito.model.hostreport.HostReportParams;
import hr.eito.model.hostreport.HostReportReturnResult;
import hr.eito.model.netflow.NetflowParams;
import hr.eito.model.newsticker.NewsTickerParams;
import hr.eito.model.newsticker.NewsTickerQueryResultField;
import hr.eito.model.newsticker.NewsTickerReturnResult;
import hr.eito.model.ntv.NetflowReturnResult;

/**
 * Manager implementation that fetches data from ElasticSearch. This is the production
 * implementation.
 *
 * @author Danijel Soltic
 */
@Component
public class ElasticSearchManagerImpl implements ElasticSearchManager {
	
	private ModelDateConverter modelDateConverter = new ModelDateConverter();

	@Autowired
	ElasticSearchDao elasticSearchDao;
	
	/**
	 * CMDB report. This is a report by IP address for the specified IP address.
	 *
	 * @param params parameters to query with
	 *
	 * @return the report data. If no parameters set return Unknown error result set
	 */
	@Override
	public JsonReturnData<CMDBReturnResult> cmdbReport(final CMDBParams params) {
		final JsonReturnData<JsonNode> results;
		if (params==null || !params.hasAnyParameter()) {
			results = new JsonReturnData<>();
		} else {
			results = elasticSearchDao.cmdbReport(params.getIpAddress()); 
		}
		if(results.isOK()) {
			return new JsonReturnData<CMDBReturnResult>(new CMDBReturnResult(results.getContent()));
		}

		return new JsonReturnData<CMDBReturnResult>(results.getErrorMessage());
	}

	/**
	 * Parameterized Host report. This is the summary host report providing severity, event list and first and
	 * last detection time for the specified IP address.
	 *
	 * @param params parameters to query with
	 *
	 * @return the report data
	 */
	@Override
	public JsonReturnData<HostReportReturnResult> hostReport(final HostReportParams params) {
		JsonReturnData<JsonNode> queryResultRD = null;
		
		// Setting the parameters variables
		String ipAddress = null;
		DateTimeRange dateTimeRange = null;
		if (params!=null) {
			ipAddress = params.getIpAddress();
			dateTimeRange = params.getDateTimeRange();
		}		
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange!=null && dateTimeRange.isBroken()) {
			return new JsonReturnData<HostReportReturnResult>(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
		}
		
		// Check which Dao method to call
		if (params==null) {
			// parameters not set, return as no filter
			queryResultRD = this.elasticSearchDao.hostReport(); 
		} else if (ipAddress==null || ipAddress.isEmpty()) {
			if (dateTimeRange==null) {
				// no query, no dateTimeRange
				queryResultRD = this.elasticSearchDao.hostReport(); 
			} else {
				// no query, YES dateTimeRange
				queryResultRD = this.elasticSearchDao.hostReport(
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		} else {
			if (dateTimeRange==null) {
				// YES query, no dateTimeRange
				queryResultRD = this.elasticSearchDao.hostReport(ipAddress);
			} else {
				// YES query, YES dateTimeRange
				queryResultRD = this.elasticSearchDao.hostReport(ipAddress,
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		}

		// If none of the cases is covered - set unknown error
		if (queryResultRD==null) {			
			queryResultRD = new JsonReturnData<>();
		}

		return hostReportData_(queryResultRD);
	}

	/**
	 * Build host report return data based on query data.
	 *
	 * @param results the result data from the query
	 *
	 * @return host report data
	 */
	private JsonReturnData<HostReportReturnResult> hostReportData_(final JsonReturnData<JsonNode> results) {
		// Populate the severity map for later use.
		final Map<String, Integer> severities = getSeverities_();

		if(results.isOK()) {
			return new JsonReturnData<HostReportReturnResult>(new HostReportReturnResult(results.getContent(), severities));
		}

		return new JsonReturnData<HostReportReturnResult>(results.getErrorMessage());
	}

	/**
	 * Get a map of event name to severity value.
	 *
	 * @return map of each event name to its associated severity
	 */
	private Map<String, Integer> getSeverities_() {

		Map<String, Integer> severities = new HashMap<String, Integer>();

		final JsonReturnData<QueryResult<EventDescription>> descriptionsRD = this.elasticSearchDao.loadEventDescriptions();
		if (descriptionsRD.isOK()) {
			final QueryResult<EventDescription> descriptions = descriptionsRD.getContent();
			if (null != descriptions &&
				null != descriptions.getHits() &&
				null != descriptions.getHits().getHits()) {

				for (Hit<EventDescription> hit : descriptions.getHits().getHits()) {
					severities.put(hit.getData().getUse_case(), hit.getData().getSeverity());
				}
			}
		}

		return severities;
	}

	/**
	 * Parameterized Host Details report. This is the host details report providing and first and
	 * last detection time for each event type for the specified IP address.
	 *
	 * @param params encapsulated parameters
	 *
	 * @return the report data
	 */
	@Override
	public JsonReturnData<HostDetailsReportReturnResult> hostDetailsReport(final HostDetailsReportParams params) {
		JsonReturnData<JsonNode> results = null;
		
		// Setting the parameters variables
		String ipAddress = null;
		DateTimeRange dateTimeRange = null;
		if (params!=null) {
			ipAddress = params.getIpAddress();
		    dateTimeRange = params.getDateTimeRange();
		}
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange!=null && dateTimeRange.isBroken()) {
		    return new JsonReturnData<HostDetailsReportReturnResult>(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
		}
		
		// Check which Dao method to call
		if (params==null) {
		    // parameters not set, return as unknown default error
			return new JsonReturnData<HostDetailsReportReturnResult>();
		} else if (ipAddress==null || ipAddress.isEmpty()) {
			return new JsonReturnData<HostDetailsReportReturnResult>(CustomError.IP_MISSING.getErrorMessage());
		} else {
			// Check validity of an IP address
			if (!InetAddressValidator.getInstance().isValid(ipAddress)) {
				return new JsonReturnData<HostDetailsReportReturnResult>(CustomError.IP_INVALID.getErrorMessage());
			}
		    if (dateTimeRange==null) {
		        // YES query, no dateTimeRange
		    	results = this.elasticSearchDao.hostDetailsReport(ipAddress);
		    } else {
		        // YES query, YES dateTimeRange
		    	results = this.elasticSearchDao.hostDetailsReport(ipAddress,
		                modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
		                modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
		    }
		}		

		// If none of the cases is covered - set unknown error
		if (results==null) {			
			results = new JsonReturnData<>();
		}

		if(results.isOK()) {

			JsonReturnData<QueryResult<EventDescription>> eventDescriptionRD = this.elasticSearchDao.loadEventDescriptions();
			
			// Make all this work correctly - should be a tuple.
			Map<String, String> descriptions = new HashMap<String, String>();
			Map<String, String> links = new HashMap<String, String>();
			Map<String, Integer> offsets = new HashMap<String, Integer>();

			if(eventDescriptionRD.isOK() &&
			   eventDescriptionRD.getContent() != null &&
			   eventDescriptionRD.getContent().getHits() != null &&
			   eventDescriptionRD.getContent().getHits().getHits() != null){

				QueryResult<EventDescription> eventDescriptionResult = eventDescriptionRD.getContent();

				// Map of description for easier access
				for(Hit<EventDescription> hit : eventDescriptionResult.getHits().getHits()){
					descriptions.put(hit.getData().getUse_case(), hit.getData().getDescription());
				}

				// Map of ELK link for easier access
				for(Hit<EventDescription> hit : eventDescriptionResult.getHits().getHits()){
					links.put(hit.getData().getUse_case(), hit.getData().getLink());
				}

				// Map of ELK timestamp offsets for easier access
				for(Hit<EventDescription> hit : eventDescriptionResult.getHits().getHits()){
					offsets.put(hit.getData().getUse_case(), hit.getData().getDateOffset());
				}
			}

			return new JsonReturnData<HostDetailsReportReturnResult>(new HostDetailsReportReturnResult(results.getContent(), descriptions, links, offsets));
		}

		return new JsonReturnData<HostDetailsReportReturnResult>(results.getErrorMessage());
	}

	/**
	 * Fetch netflow information for specific parameter set
	 *
	 * @param params encapsulated parameters
	 *
	 * @return netflow data for specified parameters
	 */
	@Override
	public JsonReturnData<NetflowReturnResult> loadDataByIP(NetflowParams params) {
		JsonReturnData<JsonData> netflowRawReturn = new JsonReturnData<>();

		// Setting the parameters variables
		String ipAddress = null;
		DateTimeRange dateTimeRange = null;
		if (params!=null) {
			ipAddress = params.getIpAddress();
			dateTimeRange = params.getDateTimeRange();
		}
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange!=null && dateTimeRange.isBroken()) {
			return new JsonReturnData<NetflowReturnResult>(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
		}
		
		if (ipAddress==null || ipAddress.isEmpty()) {
			// IP address is missing, return with appropriate message
			return new JsonReturnData<NetflowReturnResult>(CustomError.IP_MISSING.getErrorMessage());
		} else {
			// Check validity of an IP address
			if (!InetAddressValidator.getInstance().isValid(ipAddress)) {
				return new JsonReturnData<NetflowReturnResult>(CustomError.IP_INVALID.getErrorMessage());
			}
			if (dateTimeRange==null) {
				// YES query, no dateTimeRange
				netflowRawReturn = this.elasticSearchDao.netflow(ipAddress);
			} else {
				// YES query, YES dateTimeRange
				netflowRawReturn = this.elasticSearchDao.netflow(ipAddress,
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		}
		
		if(netflowRawReturn.isOK()) {
			return new JsonReturnData<NetflowReturnResult>(new NetflowReturnResult(netflowRawReturn.getContent()));
		}

		return new JsonReturnData<NetflowReturnResult>(netflowRawReturn.getErrorMessage());
	}

	/**
	 * Execute a newsticker query based on the supplied parameters.
	 *
	 * @param params the query parameters
	 *
	 * @return the JSON enccoded results
	 */
	@Override
	public JsonReturnData<NewsTickerReturnResult> newsticker(final NewsTickerParams params) {
		JsonReturnData<QueryResult<NewsTickerQueryResultField>> queryResultRD = null;

		// Setting the parameters variables
		String ipAddress = null;
		DateTimeRange dateTimeRange = null;
		final int size;
		if (params!=null) {
			ipAddress = params.getQuery();
			dateTimeRange = params.getDateTimeRange();
			size = params.getSize();
		} else {
			NewsTickerParams params_induced = new NewsTickerParams();
			size = params_induced.getSize();
		}
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange!=null && dateTimeRange.isBroken()) {
			return new JsonReturnData<NewsTickerReturnResult>(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
		}

		// Check which Dao method to call
		if (ipAddress==null || ipAddress.isEmpty()) {
			if (dateTimeRange==null) {
				// no query, no dateTimeRange
				queryResultRD = this.elasticSearchDao.newsticker(size);
			} else {
				// no query, YES dateTimeRange
				queryResultRD = this.elasticSearchDao.newsticker(size,
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		} else {
			if (dateTimeRange==null) {
				// YES query, no dateTimeRange
				queryResultRD = this.elasticSearchDao.newsticker(size, ipAddress);
			} else {
				// YES query, YES dateTimeRange
				queryResultRD = this.elasticSearchDao.newsticker(size, ipAddress,
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		}
		
		// If none of the cases is covered - set unknown error
		if (queryResultRD==null) {			
			queryResultRD = new JsonReturnData<>();
		}

		if(queryResultRD.isOK()) {
			return new JsonReturnData<NewsTickerReturnResult>(new NewsTickerReturnResult(queryResultRD.getContent()));
		}

		return new JsonReturnData<NewsTickerReturnResult>(queryResultRD.getErrorMessage());
	}

}

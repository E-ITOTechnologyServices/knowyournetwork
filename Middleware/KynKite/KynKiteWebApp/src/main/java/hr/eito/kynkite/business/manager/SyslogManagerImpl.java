
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hr.eito.kynkite.business.dao.SyslogDao;
import hr.eito.kynkite.utils.CustomError;
import hr.eito.model.DateTimeRange;
import hr.eito.model.JsonReturnData;
import hr.eito.model.ModelDateConverter;
import hr.eito.model.QueryResult;
import hr.eito.model.syslog.SyslogASAQueryResultField;
import hr.eito.model.syslog.SyslogASAReturnResult;
import hr.eito.model.syslog.SyslogDHCPQueryResultField;
import hr.eito.model.syslog.SyslogDHCPReturnResult;
import hr.eito.model.syslog.SyslogIPSQueryResultField;
import hr.eito.model.syslog.SyslogIPSReturnResult;
import hr.eito.model.syslog.SyslogParams;
import hr.eito.model.syslog.SyslogProxyQueryResultField;
import hr.eito.model.syslog.SyslogProxyReturnResultData;
import hr.eito.model.syslog.SyslogReturnResult;
import hr.eito.model.syslog.SyslogRouterQueryResultField;
import hr.eito.model.syslog.SyslogRouterReturnResult;
import hr.eito.model.syslog.SyslogVoiceQueryResultField;
import hr.eito.model.syslog.SyslogVoiceReturnResult;

/**
 * Syslog manager implementation
 *
 * @author Hrvoje
 */
@Component
public class SyslogManagerImpl implements SyslogManager {
	
	private ModelDateConverter modelDateConverter = new ModelDateConverter();
	
	@Autowired
	SyslogDao syslogDao;

	/**
	 * Get Syslog ASA report data from ElasticSearch for the specified search term.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return JSON report data
	 */
	@Override
	public JsonReturnData<SyslogASAReturnResult> syslogASA(SyslogParams params) {
		JsonReturnData<QueryResult<SyslogASAQueryResultField>> queryResultRD = null;
		
		// Setting the parameters variables
		String query = null;
		DateTimeRange dateTimeRange = null;
		if (params!=null) {
			query = params.getQuery();
			dateTimeRange = params.getDateTimeRange();
		}		
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange!=null && dateTimeRange.isBroken()) {
			return new JsonReturnData<SyslogASAReturnResult>(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
		}
		
		// Check which Dao method to call
		if (params==null) {
			// parameters not set, return as no filter
			queryResultRD = this.syslogDao.syslogASA(); 
		} else if (query==null || query.isEmpty()) {
			if (dateTimeRange==null) {
				// no query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogASA(); 
			} else {
				// no query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogASA(
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		} else {
			if (dateTimeRange==null) {
				// YES query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogASA(query);
			} else {
				// YES query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogASA(query,
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		}

		// If none of the cases is covered - set unknown error
		if (queryResultRD==null) {			
			queryResultRD = new JsonReturnData<>();
		}
		
		if(queryResultRD.isOK()) {
			return new JsonReturnData<SyslogASAReturnResult>(new SyslogASAReturnResult(queryResultRD.getContent()));
		}

		return new JsonReturnData<SyslogASAReturnResult>(queryResultRD.getErrorMessage());
	}
	
	/**
	 * Get Syslog Router report data from ElasticSearch for the specified search term.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return JSON report data
	 */
	@Override
	public JsonReturnData<SyslogRouterReturnResult> syslogRouter(SyslogParams params) {
		JsonReturnData<QueryResult<SyslogRouterQueryResultField>> queryResultRD = null;
		
		// Setting the parameters variables
		String query = null;
		DateTimeRange dateTimeRange = null;
		if (params!=null) {
			query = params.getQuery();
			dateTimeRange = params.getDateTimeRange();
		}		
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange!=null && dateTimeRange.isBroken()) {
			return new JsonReturnData<SyslogRouterReturnResult>(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
		}
		
		// Check which Dao method to call
		if (params==null) {
			// parameters not set, return as no filter
			queryResultRD = this.syslogDao.syslogRouter(); 
		} else if (query==null || query.isEmpty()) {
			if (dateTimeRange==null) {
				// no query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogRouter(); 
			} else {
				// no query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogRouter(
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		} else {
			if (dateTimeRange==null) {
				// YES query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogRouter(query);
			} else {
				// YES query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogRouter(query,
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		}

		// If none of the cases is covered - set unknown error
		if (queryResultRD==null) {			
			queryResultRD = new JsonReturnData<>();
		}
		
		if(queryResultRD.isOK()) {
			return new JsonReturnData<SyslogRouterReturnResult>(new SyslogRouterReturnResult(queryResultRD.getContent()));
		}

		return new JsonReturnData<SyslogRouterReturnResult>(queryResultRD.getErrorMessage());
	}
	
	/**
	 * Get Syslog Voice report data from ElasticSearch for the specified search term.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return JSON report data
	 */
	@Override
	public JsonReturnData<SyslogVoiceReturnResult> syslogVoice(SyslogParams params) {
		JsonReturnData<QueryResult<SyslogVoiceQueryResultField>> queryResultRD = null;
		
		// Setting the parameters variables
		String query = null;
		DateTimeRange dateTimeRange = null;
		if (params!=null) {
			query = params.getQuery();
			dateTimeRange = params.getDateTimeRange();
		}		
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange!=null && dateTimeRange.isBroken()) {
			return new JsonReturnData<SyslogVoiceReturnResult>(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
		}
		
		// Check which Dao method to call
		if (params==null) {
			// parameters not set, return as no filter
			queryResultRD = this.syslogDao.syslogVoice(); 
		} else if (query==null || query.isEmpty()) {
			if (dateTimeRange==null) {
				// no query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogVoice(); 
			} else {
				// no query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogVoice(
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		} else {
			if (dateTimeRange==null) {
				// YES query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogVoice(query);
			} else {
				// YES query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogVoice(query,
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		}

		// If none of the cases is covered - set unknown error
		if (queryResultRD==null) {			
			queryResultRD = new JsonReturnData<>();
		}
		
		if(queryResultRD.isOK()) {
			return new JsonReturnData<SyslogVoiceReturnResult>(new SyslogVoiceReturnResult(queryResultRD.getContent()));
		}

		return new JsonReturnData<SyslogVoiceReturnResult>(queryResultRD.getErrorMessage());
	}

	/**
	 * Get Syslog IPS report data from ElasticSearch for the specified search term.
	 *
	 * @param params Parameters sent to filter data
	 *
	 * @return JSON report data
	 */
	@Override
	public JsonReturnData<SyslogIPSReturnResult> syslogIPS(SyslogParams params) {
		JsonReturnData<QueryResult<SyslogIPSQueryResultField>> queryResultRD = null;
		
		// Setting the parameters variables
		String query = null;
		DateTimeRange dateTimeRange = null;
		if (params!=null) {
			query = params.getQuery();
			dateTimeRange = params.getDateTimeRange();
		}		
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange!=null && dateTimeRange.isBroken()) {
			return new JsonReturnData<SyslogIPSReturnResult>(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
		}
		
		// Check which Dao method to call
		if (params==null) {
			// parameters not set, return as no filter
			queryResultRD = this.syslogDao.syslogIPS(); 
		} else if (query==null || query.isEmpty()) {
			if (dateTimeRange==null) {
				// no query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogIPS(); 
			} else {
				// no query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogIPS(
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		} else {
			if (dateTimeRange==null) {
				// YES query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogIPS(query);
			} else {
				// YES query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogIPS(query,
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		}

		// If none of the cases is covered - set unknown error
		if (queryResultRD==null) {			
			queryResultRD = new JsonReturnData<>();
		}
		
		if(queryResultRD.isOK()) {
			return new JsonReturnData<SyslogIPSReturnResult>(new SyslogIPSReturnResult(queryResultRD.getContent()));
		}

		return new JsonReturnData<SyslogIPSReturnResult>(queryResultRD.getErrorMessage());
	}

	/**
	 * Get Syslog DHCP report data from ElasticSearch for the specified search term.
	 *
	 * @param params parameters to form query with
	 *
	 * @return JSON report data
	 */
	@Override
	public JsonReturnData<SyslogDHCPReturnResult> syslogDHCP(SyslogParams params) {
		JsonReturnData<QueryResult<SyslogDHCPQueryResultField>> queryResultRD = null;
		
		// Setting the parameters variables
		String query = null;
		DateTimeRange dateTimeRange = null;
		if (params!=null) {
			query = params.getQuery();
			dateTimeRange = params.getDateTimeRange();
		}		
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange!=null && dateTimeRange.isBroken()) {
			return new JsonReturnData<SyslogDHCPReturnResult>(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
		}
		
		// Check which Dao method to call
		if (params==null) {
			// parameters not set, return as no filter
			queryResultRD = this.syslogDao.syslogDHCP(); 
		} else if (query==null || query.isEmpty()) {
			if (dateTimeRange==null) {
				// no query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogDHCP(); 
			} else {
				// no query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogDHCP(
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		} else {
			if (dateTimeRange==null) {
				// YES query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogDHCP(query);
			} else {
				// YES query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogDHCP(query,
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		}

		// If none of the cases is covered - set unknown error
		if (queryResultRD==null) {			
			queryResultRD = new JsonReturnData<>();
		}
		
		if(queryResultRD.isOK()) {
			return new JsonReturnData<SyslogDHCPReturnResult>(new SyslogDHCPReturnResult(queryResultRD.getContent()));
		}

		return new JsonReturnData<SyslogDHCPReturnResult>(queryResultRD.getErrorMessage());
	}
	
	/**
	 * Get Syslog Proxy report data from ElasticSearch for the specified search term.
	 ** <p>
	 * Get query results with parameter from Elasticsearch, map it to SyslogProxyReturnResultData inside SyslogReturnResult template
	 * 
	 * @param params parameters to form query with
	 *
	 * @return JSON report data
	 */
	@Override
	public JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData,SyslogProxyQueryResultField>> syslogProxy(SyslogParams params) {
		JsonReturnData<QueryResult<SyslogProxyQueryResultField>> queryResultRD = null;
		
		// Setting the parameters variables
		String query = null;
		DateTimeRange dateTimeRange = null;
		if (params!=null) {
			query = params.getQuery();
			dateTimeRange = params.getDateTimeRange();
		}		
		
		// If something is wrong with dateTimeRange - return error message
		if (dateTimeRange!=null && dateTimeRange.isBroken()) {
			return new JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData,SyslogProxyQueryResultField>>(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage());
		}
		
		// Check which Dao method to call
		if (params==null) {
			// parameters not set, return as no filter
			queryResultRD = this.syslogDao.syslogProxy(); 
		} else if (query==null || query.isEmpty()) {
			if (dateTimeRange==null) {
				// no query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogProxy(); 
			} else {
				// no query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogProxy(
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		} else {
			if (dateTimeRange==null) {
				// YES query, no dateTimeRange
				queryResultRD = this.syslogDao.syslogProxy(query);
			} else {
				// YES query, YES dateTimeRange
				queryResultRD = this.syslogDao.syslogProxy(query,
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeFrom()), 
						modelDateConverter.toUTCDateTime(dateTimeRange.getDateTimeTo()));
			}
		}

		// If none of the cases is covered - set unknown error
		if (queryResultRD==null) {			
			queryResultRD = new JsonReturnData<>();
		}
		
		if(queryResultRD.isOK()) {
			try {
				return new JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData,SyslogProxyQueryResultField>>(
						new SyslogReturnResult<>(queryResultRD.getContent(), SyslogProxyReturnResultData.class, SyslogProxyQueryResultField.class));
			} catch (Exception e) {
				return new JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData,SyslogProxyQueryResultField>>(e.getMessage());
			}
		}

		return new JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData,SyslogProxyQueryResultField>>(
				queryResultRD.getErrorMessage());
	}
}


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


package hr.eito.kynkite.business.dao.stub;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hr.eito.model.JsonReturnData;
import hr.eito.model.QueryResult;

/**
 * Helper class to provide execution of stub Data Access Object
 * 
 * @author Hrvoje
 */
@Component
@Profile("test")
public class StubManager {	
	
	/**
	 * Get report test data from the specified location.
	 *
	 * @param location the location of the test data
	 * @param r
	 *
	 * @return the resultant report data
	 */
	public <T> JsonReturnData<T> reportJsonData_(String location, Class<T> r) {
		JsonReturnData<T> result = new JsonReturnData<>();

		try {
			final String json = reportDataString_(location);

			ObjectMapper mapper = new ObjectMapper();
			T queryResult = mapper.readValue(json, r);
			
			result.setContent(queryResult);
			result.setOK();
			
		} catch (final Exception e) {
			//logger.error("Failed to get test data from <" + location + "> ", e.toString());
			result.setError(e);
		}

		return result;
	}

	/**
	 * Get report test data from the specified location.
	 *
	 * @param location the location of the test data
	 * @param c the class type of the result payload
	 * @param r type reference of the result payload
	 *
	 * @return the resultant report data
	 */
	public <T> JsonReturnData<QueryResult<T>> reportResults_(String location, final Class<T> c, TypeReference<?> r) {
		JsonReturnData<QueryResult<T>> result = new JsonReturnData<QueryResult<T>>();

		location = "/data/" + location + "/result.json";

		try {
			StringBuffer sb = new StringBuffer();
			
			sb.append(FileLoader.loadFileContent(location));
			
			ObjectMapper mapper = new ObjectMapper();
			QueryResult<T> queryResult = mapper.readValue(sb.toString(), r);

			result.setContent(queryResult);
			result.setOK();
			
		} catch (final Exception e) {
			//logger.error("Failed to get test data from <" + location + "> ", e.toString());
			result.setError(e);
		}

		return result;
	}
	
	/**
	 * Get report test data from the specified location.
	 *
	 * @param location the location of the test data
	 *
	 * @return the resultant report data
	 */
	public JsonReturnData<JsonNode> reportData_(String location) {
		JsonReturnData<JsonNode> result = new JsonReturnData<JsonNode>();

		try {
			final String json = reportDataString_(location);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode queryResult = mapper.readValue(json, JsonNode.class);
			
			result.setContent(queryResult);
			result.setOK();
			
		} catch (final Exception e) {
			//logger.error("Failed to get test data from <" + location + "> ", e.toString());
			result.setError(e);
		}

		return result;
	}

	/**
	 * Get report test data from the specified location as a string of JSON.
	 *
	 * @param location the location of the test data
	 *
	 * @return the resultant report data
	 */
	public String reportDataString_(String location) {
		location = "/data/" + location + "/result.json";
		
		StringBuffer sb = new StringBuffer();

		sb.append(FileLoader.loadFileContent(location));

		return sb.toString();
	}
	
	/**
	 * For folder naming purposes remove all special characters from date string
	 *
	 * @param date timestamp from which we remove characters
	 *
	 * @return String without some characters
	 */
	public String dateForFolderName(final String date) {
		return date.replace("-", "").replace(":", "").replace(".", "").replace("+", "");
	}

}

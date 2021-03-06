
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


/**
 * 
 */
package hr.eito.model.syslog;

import java.util.ArrayList;
import java.util.List;

import hr.eito.model.Hit;
import hr.eito.model.QueryResult;

/**
 * Represents all necessary return data
 * <p>
 * The class is templated with Syslog query result item class
 * and with Syslog destination item class.
 * Besides list of return data, it contains record statistics
 * 
 * @author Hrvoje
 *
 */
public class SyslogReturnResult<T, E> {
		
	private int recordsTotal;
	private int recordsFiltered;
	private List<T> data;  
	
	/**
	 * Calculates necessary counts and maps query items to destination items one by one
	 * 
	 * @param result contains Syslog query results
	 * @param classDest class type of the result element - instance mapped from instance of source class
	 * @param classSrc class type of the query (source) element - instance mapping into instance of destination class
	 * @throws Exception throw possible from generating constructor based on classDest
	 */
	public SyslogReturnResult(QueryResult<E> result, Class<T> classDest, Class<E> classSrc) throws Exception {
		this.data = new ArrayList<T>();

		this.recordsFiltered = 0;
		this.recordsTotal = 0;
		
		if(result.getHits() != null && result.getHits().getHits() != null){

			recordsFiltered = result.getHits().getHits().size();
			recordsTotal = result.getHits().getTotal();

			for(Hit<E> hit : result.getHits().getHits()) {
				data.add(classDest.getConstructor(classSrc).newInstance(hit.getData()));
			}
		}
	}

	/**
	 * @return the recordsTotal
	 */
	public int getRecordsTotal() {
		return recordsTotal;
	}

	/**
	 * @return the recordsFiltered
	 */
	public int getRecordsFiltered() {
		return recordsFiltered;
	}

	/**
	 * @return the data
	 */
	public List<T> getData() {
		return data;
	}

}


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


package hr.eito.model.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to encapsulate report names records and statistics
 * 
 * @author Hrvoje
 *
 */
public class ReportsReturnResult {
	
	private int recordsTotal;
	private int recordsFiltered;
	private List<String> data;
	
	/**
	 * Receive list of file names as String and add them to data
	 * 
	 * @param result File names records
	 */
	public ReportsReturnResult(List<String> result){
		this.data = new ArrayList<String>();

		this.recordsFiltered = 0;
		this.recordsTotal = 0;
		
		if(result != null){

			recordsFiltered = result.size();
			recordsTotal = result.size();

			for(String reportName : result){
				data.add(reportName);
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
	public List<String> getData() {
		return data;
	}
}

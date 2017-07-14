
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


package hr.eito.model.syslog;

import java.util.ArrayList;
import java.util.List;

import hr.eito.model.QueryResult;
import hr.eito.model.Hit;

/**
 * @author Marko
 *
 */
public class SyslogIPSReturnResult {
	
	private int recordsTotal;
	private int recordsFiltered;
	private List<SyslogIPSReturnResultData> data;
	
	public SyslogIPSReturnResult(QueryResult<SyslogIPSQueryResultField> result){
		this.data = new ArrayList<SyslogIPSReturnResultData>();

		this.recordsFiltered = 0;
		this.recordsTotal = 0;
		
		if(result.getHits() != null && result.getHits().getHits() != null){

			recordsFiltered = result.getHits().getHits().size();
			recordsTotal = result.getHits().getTotal();

			for(Hit<SyslogIPSQueryResultField> hit : result.getHits().getHits()){
				data.add(new SyslogIPSReturnResultData(hit.getData()));
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
	public List<SyslogIPSReturnResultData> getData() {
		return data;
	}
}

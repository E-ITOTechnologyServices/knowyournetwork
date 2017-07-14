
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


package hr.eito.model.newsticker;

import java.util.ArrayList;
import java.util.List;

import hr.eito.model.QueryResult;
import hr.eito.model.Hit;

/**
 * Encapsulates the newsticker report return results.
 *
 * @author Marko
 */
public class NewsTickerReturnResult {

	private int recordsTotal;
	private int recordsFiltered;
	private List<NewsTickerReturnResultData> data;
	
	/**
	 * Construct from a query result.
	 *
	 * @param result the query result
	 */
	public NewsTickerReturnResult(final QueryResult<NewsTickerQueryResultField> result){
		this.data = new ArrayList<NewsTickerReturnResultData>();
		
		this.recordsFiltered = 0;
		this.recordsTotal = 0;

		if(result.getHits() != null && result.getHits().getHits() != null){

			this.recordsFiltered = result.getHits().getHits().size();
			this.recordsTotal = result.getHits().getTotal();

			for(Hit<NewsTickerQueryResultField> hit : result.getHits().getHits()){
				data.add(new NewsTickerReturnResultData(hit.getData()));
			}
		}
	}

	/**
	 * Get the total number of records that could have been returned by the query.
	 *
	 * @return total number of records
	 */
	public int getRecordsTotal() {
		return recordsTotal;
	}

	/**
	 * Get the actual number of records returned by the query - and stored in this result set.
	 *
	 * @return the number of records in this result set
	 */
	public int getRecordsFiltered() {
		return recordsFiltered;
	}

	/**
	 * Get the payload data for the query.
	 *
	 * @return the data
	 */
	public List<NewsTickerReturnResultData> getData() {
		return data;
	}
}

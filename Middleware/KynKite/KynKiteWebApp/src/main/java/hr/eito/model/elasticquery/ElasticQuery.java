
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


package hr.eito.model.elasticquery;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import hr.eito.model.elasticquery.serializer.ElasticQuerySerializer;

/**
 * Class where ElasticSearch query begins
 * 
 * @author Hrvoje
 *
 */
@JsonSerialize(using = ElasticQuerySerializer.class)
public class ElasticQuery {
	
	private Integer size;
	private IQueryClause query;
	private List<String> source;
	private List<SortSegment> sort;
	private Aggs aggs;
	
	/**
	 * Adding source items (fields of query)
	 * 
	 * @param sourceItems
	 */
	public void addSources(final String... sourceItems) {
		if (source==null) {
			source = new ArrayList<>();
		}
		for (final String sourceItem: sourceItems) {
			this.source.add(sourceItem);
		}
	}
	
	/**
	 * Adding field to sort query with
	 * 
	 * @param sortSegment information about sort field name and direction (asc, desc)
	 */
	public void addSort(final SortSegment sortSegment) {
		if (sort==null) {
			sort = new ArrayList<>();
		}
		this.sort.add(sortSegment);
	}
	
	/**
	 * @return size of the return records
	 */
	public Integer getSize() {
		return size;
	}
	/**
	 * @param size of the return records
	 */
	public void setSize(Integer size) {
		this.size = size;
	}
	
	/**
	 * @return list of fields to show recodrd with
	 */
	public List<String> getSource() {
		return source;
	}
	
	/**
	 * @return sort
	 */
	public List<SortSegment> getSort() {
		return sort;
	}
	
	/**
	 * @return starting query clause
	 */
	public IQueryClause getQuery() {
		return query;
	}
	/**
	 * @param query setting starting query clause
	 */
	public void setQuery(IQueryClause query) {
		this.query = query;
	}
	
	/**
	 * @return starting aggs clause
	 */
	public Aggs getAggs() {
		return aggs;
	}
	/**
	 * @param aggs setting aggs clause
	 */
	public void setAggs(Aggs aggs) {
		this.aggs = aggs;
	}
		
}

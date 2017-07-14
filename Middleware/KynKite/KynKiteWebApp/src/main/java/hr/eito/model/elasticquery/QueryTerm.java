
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

import hr.eito.model.elasticquery.serializer.QueryTermSerializer;

/**
 * Class to encapsulate logical expression term
 * <p>must, should...
 * 
 * @author Hrvoje
 *
 */
@JsonSerialize(using = QueryTermSerializer.class)
public class QueryTerm {
	
	private String name;
	private List<IQueryClause> queryClauseList;
	
	/**
	 * Creating the term based on all necessary properties
	 * 
	 * @param name of the logical expression (must, should)
	 * @param queryClauses array of IQueryClause instances of which expression consists
	 */
	public QueryTerm(final String name, final IQueryClause... queryClauses) {
		this.name = name;
		this.queryClauseList = new ArrayList<>();
		for (IQueryClause queryClause : queryClauses) {
			this.queryClauseList.add(queryClause);
		}
	}
	
	public String getName() {
		return name;
	}
	public List<IQueryClause> getQueryClauseList() {
		return queryClauseList;
	}
	
}

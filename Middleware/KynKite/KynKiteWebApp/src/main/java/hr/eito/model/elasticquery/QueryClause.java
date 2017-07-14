
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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import hr.eito.model.elasticquery.serializer.QueryClauseSerializer;

/**
 * Class that encapsulates grouping query clause
 * <p>Example is bool query clause which can make logical expressions
 *
 * @author Hrvoje
 */
@JsonSerialize(using = QueryClauseSerializer.class)
public class QueryClause implements IQueryClause {
	
	private String name;
	private QueryTerm term;
	
	/**
	 * Creating Query clause with necessary name and QueryTerm instance
	 * 
	 * @param name of the query clause
	 * @param term logical expression which consists of leaf query clauses
	 */
	public QueryClause(final String name, final QueryTerm term) {
		this.name = name;
		this.term = term;
	}
	
	public String getName() {
		return name;
	}
	public QueryTerm getTerm() {
		return term;
	}
	
}

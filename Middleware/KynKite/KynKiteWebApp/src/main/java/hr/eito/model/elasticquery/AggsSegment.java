
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

/**
 * Class that encapsulates aggs segment of which Aggs consists
 *
 * @author Hrvoje
 */
public class AggsSegment {
	
	private String identifier;
	private List<IAggsClause> aggsClauseList;
	
	/**
	 * Create AggsSegment receiving all necessary properties
	 * 
	 * @param identifier identifies the AggsSegment
	 * @param aggsClauses Clauses of which aggsSegment consists
	 */
	public AggsSegment(final String identifier, final IAggsClause...aggsClauses) {
		this.identifier = identifier;
		aggsClauseList = new ArrayList<>();
		for (IAggsClause aggsClause : aggsClauses) {
			aggsClauseList.add(aggsClause);
		}
	}
	
	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	/**
	 * @return the list of clauses
	 */
	public List<IAggsClause> getAggsClauseList() {
		return aggsClauseList;
	}	
	
}


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

import hr.eito.model.elasticquery.serializer.AggsTermsSerializer;

/**
 * Class that encapsulates aggs terms of which AggsSegment consists
 *
 * @author Hrvoje
 */
@JsonSerialize(using = AggsTermsSerializer.class)
public class AggsTerms implements IAggsClause {
	
	private String aggsTerm;
	private AggsDetails aggsDetails;
	
	/**
	 * Creating AggsTerms based on all necessary information
	 * 
	 * @param aggsTerm name of the term
	 * @param aggsDetails all necessary properties of a term
	 */
	public AggsTerms(final String aggsTerm, final AggsDetails aggsDetails) {
		this.aggsTerm = aggsTerm;
		this.aggsDetails = aggsDetails;
	}
	
	/**
	 * @return the name of the term
	 */
	public String getAggsTerm() {
		return aggsTerm;
	}
	/**
	 * @return all term details
	 */
	public AggsDetails getAggsDetails() {
		return aggsDetails;
	}
	
}

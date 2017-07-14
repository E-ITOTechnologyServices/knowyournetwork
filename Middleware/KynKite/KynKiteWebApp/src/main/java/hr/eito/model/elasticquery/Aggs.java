
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

import hr.eito.model.elasticquery.serializer.AggsSerializer;

/**
 * Class that encapsulates aggs part of the Elastic query
 *
 * @author Hrvoje
 */
@JsonSerialize(using = AggsSerializer.class)
public class Aggs implements IAggsClause {
	
	private List<AggsSegment> aggsSegmentList;
	
	/**
	 * Adding AggsSegment to the list.
	 * <p>Initialize list on first insert
	 * 
	 * @param aggsSegment
	 */
	public void addAggsSegment(final AggsSegment aggsSegment) {
		if (aggsSegmentList==null) {
			aggsSegmentList = new ArrayList<>();
		}
		aggsSegmentList.add(aggsSegment);
	}

	/**
	 * @return the list of inserted AggsSegment objects
	 */	
	public List<AggsSegment> getAggsSegmentList() {
		return aggsSegmentList;
	}
	
}

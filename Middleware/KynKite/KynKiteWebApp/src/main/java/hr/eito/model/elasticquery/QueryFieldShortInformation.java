
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

import hr.eito.model.elasticquery.serializer.QueryFieldShortInformationSerializer;

/**
 * Class that encapsulates field information including name and parameter
 * 
 * @author Hrvoje
 *
 */
@JsonSerialize(using = QueryFieldShortInformationSerializer.class)
public class QueryFieldShortInformation extends QueryFieldInformation {
	
	private String param;
	
	public QueryFieldShortInformation(final String fieldName, final String param) {
		super(fieldName);
		this.param = param;
	}
	
	public String getParam() {
		return param;
	}
	
}


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


package hr.eito.model;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Converts from Json incoming datetime expression as string
 * to java.util.Date version. If the pattern do not match or any
 * other exception comes out it saves the date as null reference
 *
 * @author Hrvoje
 */
public class DateDeserializer extends StdDeserializer<Date> {

	private static final long serialVersionUID = 1L;
	ModelDateConverter modelDateConverter = new ModelDateConverter();
	
	/**
	 * Default constructor is required.
	 */
	public DateDeserializer() { 
        this(null); 
    } 
 
	/**
	 * We are serializing a Date type.
	 */
    public DateDeserializer(Class<Date> vc) { 
        super(vc); 
    }
 
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		return modelDateConverter.toUtilDate(jp.getText());
	}
	
}

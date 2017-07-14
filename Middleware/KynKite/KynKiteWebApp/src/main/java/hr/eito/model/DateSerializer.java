
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


/*
 *
 */
package hr.eito.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Used throughout the model when dates are serialized out. It performs
 * date conversion from the input format to the required output format. As
 * a result, this could be considered a lazy conversion as it is done as
 * late as possible. In the model used in KYN, input dates are marked with
 * a timestamp to indicate locality. When we output a serialized version
 * destined for the front end, we convert to UTC and pass only date plus
 * hours and minutes.
 *
 * @author Steve Chaplin
 */
public class DateSerializer extends StdSerializer<String> {

	ModelDateConverter dc_ = new ModelDateConverter();

	/**
	 * Default constructor is required.
	 */
	public DateSerializer() {
        this(null);
    }

	/**
	 * We are serializing a String type.
	 */
	public DateSerializer(Class<String> t) {
        super(t);
    }

	/**
	 * Serialize a value of the supported type.
	 */
	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider arg2)
			throws IOException, JsonProcessingException {

		gen.writeString(dc_.toSpecificTimezoneDateTime(value));
	}
}

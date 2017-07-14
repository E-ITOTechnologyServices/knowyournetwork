
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for serializing long value of bytes to human readable
 * format String consisting of value and unit (e.g. 74.2 MB)
 *
 * @author Hrvoje
 */
public class ByteSerializer extends StdSerializer<String> {

	ModelByteConverter modelByteConverter = new ModelByteConverter();

	/**
	 * Default constructor is required.
	 */
	public ByteSerializer() {
        this(null);
    }

	/**
	 * We are serializing a String type.
	 */
	public ByteSerializer(Class<String> t) {
        super(t);
    }

	/**
	 * Serialize a value of the supported type.
	 */
	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider arg2)
			throws IOException, JsonProcessingException {
		gen.writeString(modelByteConverter.humanReadableByteCount(value));
	}
}

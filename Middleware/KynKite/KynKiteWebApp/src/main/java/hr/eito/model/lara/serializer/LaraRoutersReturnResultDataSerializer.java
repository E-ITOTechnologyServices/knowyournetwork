
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


package hr.eito.model.lara.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import hr.eito.model.lara.routers.LaraRoutersReturnResultData;

/**
 * Class to serialize LaraRoutersReturnResultData instances into valid JSON
 * 
 * @author Hrvoje
 */
public class LaraRoutersReturnResultDataSerializer extends StdSerializer<LaraRoutersReturnResultData> {
	
	/**
	 * Default constructor is required.
	 */
	public LaraRoutersReturnResultDataSerializer() {
        this(null);
    }
	
	/**
	 * We are serializing a LaraRoutersReturnResultData type.
	 */
	public LaraRoutersReturnResultDataSerializer(Class<LaraRoutersReturnResultData> t) {
        super(t);
    }
	
	/**
	 * Serialize a value of the supported type.
	 * 
	 * <p>Sample output...
	 * <pre>{@code
	 * "1.1.1.1"
	 * }</pre></p>
	 */
	@Override
	public void serialize(LaraRoutersReturnResultData value, JsonGenerator jgen, SerializerProvider arg2)
			throws IOException, JsonProcessingException {
		
		jgen.writeObject(value.getRouterIP());
	}
}

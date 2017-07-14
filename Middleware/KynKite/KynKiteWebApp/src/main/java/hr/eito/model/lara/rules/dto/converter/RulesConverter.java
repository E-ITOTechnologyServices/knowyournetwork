
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


package hr.eito.model.lara.rules.dto.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import hr.eito.model.lara.rules.dto.Rules;

/**
 * Converter class for String to Rules class JSON processing.
 * <p>Used for Json String deserialization into Rules class
 *
 * @author Hrvoje
 */
public class RulesConverter implements Converter<String, Rules> {
	
	private static final Logger logger = LoggerFactory.getLogger(RulesConverter.class);

	/**
	 * Overriding method with converting logic
	 * <p>In case of any exception convert to null
	 * 
	 * @param value String value to be converted
	 * @return rules object to be converted to
	 */
	@Override
    public Rules convert(String value) {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Rules rules = null;
		try {
			rules = objectMapper.readValue(value, Rules.class);
		} catch (Exception e) {
			logger.error("Mapping exception!", e);
		}
        return rules;
    }

    /**
	 * Overriding method for defining input type
	 */
    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(String.class);
    }

    /**
	 * Overriding method for defining output type
	 */
    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(Rules.class);
    }
}

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


/**
 * 
 */
package hr.eito.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the DateDeserializer.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class DateDeserializerTest {

	/**
	 * Runs before the tests start.
	 */
	@BeforeClass
	public static void testStart() {}
	
	/**
	 * Runs after the tests end.
	 */
	@AfterClass
	public static void testEnd() {}

	/**
	 * Test default construction.
	 */
	@Test
	public void testDefaultConstructor() {
		DateDeserializer dds = new DateDeserializer();
		Assert.assertNotNull(dds);
	}

	/**
	 * String construction.
	 */
	@Test
	public void testStringCtor() {
		DateDeserializer dds = new DateDeserializer(Date.class);
		Assert.assertNotNull(dds);
	}

	/**
	 * Test value deserialization.
	 */
	@Test
	public void testValueSerialization() throws IOException, JsonProcessingException {
		DateDeserializer dds = new DateDeserializer(Date.class);		
		ObjectMapper mapper = new ObjectMapper();
		
		String source = "2017-01-02 13:15:45.123";
		String json = "{\"value\":\"" + source + "\"}";
		
		InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonParser parser = mapper.getFactory().createParser(stream);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        // ugly hack to move pointer to value inside json
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        Date deserializedDate = dds.deserialize(parser, ctxt);
        
        Assert.assertEquals(source, utilDateToString(deserializedDate));
	}
	
	/**
	 * Utility to convert java.util.date to string
	 *
	 * @param source the value to parse
	 */
	private String utilDateToString(final Date source) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return formatter.format(source);
	}
}

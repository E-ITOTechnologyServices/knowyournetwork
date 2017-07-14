
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

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import hr.eito.model.lara.rules.dto.HostsRecord;

/**
 * Tests the TopHosts list converter
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class TopHostsConverterTest {
		
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
	 * Test getting input type
	 */
	@Test
	public void testGettingInputType() {
		TopHostsConverter topHostsConverter = new TopHostsConverter();
		TypeFactory typeFactory = TypeFactory.defaultInstance();
		JavaType javaType = topHostsConverter.getInputType(typeFactory);
		Assert.assertEquals(javaType, typeFactory.constructType(String.class));
	}
	
	/**
	 * Test getting output type
	 */
	@Test
	public void testGettingOutputType() {
		TopHostsConverter topHostsConverter = new TopHostsConverter();
		TypeFactory typeFactory = TypeFactory.defaultInstance();
		JavaType javaType = topHostsConverter.getOutputType(typeFactory);
		Assert.assertEquals(javaType, typeFactory.constructCollectionLikeType(List.class, HostsRecord.class));
	}

	/**
	 * Test successful conversion
	 * <p>Case with empty JSON Array to convert
	 */
	@Test
	public void testConvert_01() {
		TopHostsConverter topHostsConverter = new TopHostsConverter();
		String value = "[]";
		List<HostsRecord> hostsRecords = topHostsConverter.convert(value);
		Assert.assertNotNull(hostsRecords);
		Assert.assertEquals(0, hostsRecords.size());
	}
	
	/**
	 * Test successful conversion
	 * <p>Case with full data conversion
	 */
	@Test
	public void testConvert_02() {
		String value = "[{\"no\":1,\"ip\":\"192.168.12.153\",\"count\":36082,\"size\":62200000},"
				+ "{\"no\":2,\"ip\":\"192.168.12.142\",\"count\":15186,\"size\":24500000}]";
		TopHostsConverter topHostsConverter = new TopHostsConverter();
		
		List<HostsRecord> hostsRecords = topHostsConverter.convert(value);
		Assert.assertNotNull(hostsRecords);
		Assert.assertEquals(2, hostsRecords.size());	
		
		HostsRecord hostsRecord = hostsRecords.get(0);
		Assert.assertNotNull(hostsRecord);
		Assert.assertEquals(1, hostsRecord.getRank());
		Assert.assertEquals("192.168.12.153", hostsRecord.getIpAddress());
		Assert.assertEquals(36082, hostsRecord.getHits());
		Assert.assertEquals(62200000, hostsRecord.getSize());
	}
	
	/**
	 * Test unsuccessful conversion
	 * <p>Case with full data conversion of invalid input json
	 */
	@Test
	public void testConvert_03() {
		String value = "{\"no\":1,\"ip\":\"192.168.12.153\",\"count\":36082,\"size\":62200000},"
				+ "{\"no\":2,\"ip\":\"192.168.12.142\",\"count\":15186,\"size\":24500000}";
		TopHostsConverter topHostsConverter = new TopHostsConverter();		
		List<HostsRecord> hostsRecords = topHostsConverter.convert(value);
		Assert.assertNull(hostsRecords);
	}

}


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


package hr.eito.model.syslog;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the SyslogIPSReturnResultData.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class SyslogIPSReturnResultDataTest {

	/**
	 * Runs before the tests start.
	 */
	@BeforeClass
	public static void testStart() { }
	
	/**
	 * Runs after the tests end.
	 */
	@AfterClass
	public static void testEnd() { }

	/**
	 * Test constructing with a null value.
	 */
	@Test
	public void testNull() {
		SyslogIPSReturnResultData d = new SyslogIPSReturnResultData(null);

		Assert.assertNull(d.getDestination());
		Assert.assertNull(d.getTimestamp());
		Assert.assertNull(d.getIps_system());
		Assert.assertNull(d.getLog());
		Assert.assertNull(d.getSource());
		Assert.assertNull(d.getDestinationport());
	}

	/**
	 * Test expected values are returned.
	 */
	@Test
	public void testExpected() {

		final String destination = "destination";
		final String timestamp = "2016-12-22T10:11:12.000+01:00";
		final String ipsSystem = "IPS System";
		final String log = "this is the log";
		final String source = "this is the source";
		final String destinationPort = "100";

		SyslogIPSQueryResultField f = new SyslogIPSQueryResultField();
		f.setDestination(destination);
		f.setTimestamp(timestamp);
		f.setIps_system(ipsSystem);
		f.setLog(log);
		f.setSource(source);
		f.setDestinationport(destinationPort);

		SyslogIPSReturnResultData d = new SyslogIPSReturnResultData(f);
		Assert.assertEquals(destination, d.getDestination());
		Assert.assertEquals(timestamp, d.getTimestamp());
		Assert.assertEquals(ipsSystem, d.getIps_system());
		Assert.assertEquals(log, d.getLog());
		Assert.assertEquals(source, d.getSource());
		Assert.assertEquals(destinationPort, d.getDestinationport());
	}
}

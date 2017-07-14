
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
 * Tests the SyslogDHCPReturnResultData.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class SyslogDHCPReturnResultDataTest {

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
		SyslogDHCPReturnResultData d = new SyslogDHCPReturnResultData(null);

		Assert.assertNull(d.getMessage());
		Assert.assertNull(d.getTimestamp());
		Assert.assertNull(d.getAd_Server());
	}

	/**
	 * Test expected values are returned.
	 */
	@Test
	public void testExpected() {

		final String message = "message";
		final String timestamp = "2016-12-22T10:11:12.000+01:00";
		final String adServer = "AD Server";

		SyslogDHCPQueryResultField f = new SyslogDHCPQueryResultField();
		f.setMessage(message);
		f.setTimestamp(timestamp);
		f.setAd_server(adServer);

		SyslogDHCPReturnResultData d = new SyslogDHCPReturnResultData(f);
		Assert.assertEquals(message, d.getMessage());
		Assert.assertEquals(timestamp, d.getTimestamp());
		Assert.assertEquals(adServer, d.getAd_Server());
	}
}


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
package hr.eito.model.syslog;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the SyslogProxyReturnResultData
 * 
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class SyslogProxyReturnResultDataTest {

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
	 * Testing constructor receiving null value
	 */
	@Test
	public void testNull() {
		SyslogProxyReturnResultData data = new SyslogProxyReturnResultData(null);

		Assert.assertNull(data.getTimestamp());
		Assert.assertNull(data.getSource());
		Assert.assertNull(data.getDestination());
		Assert.assertNull(data.getHost());
		Assert.assertNull(data.getPort());
		Assert.assertNull(data.getUri());
		Assert.assertNull(data.getScStatus());
		Assert.assertNull(data.getUserAgentOs());
		Assert.assertNull(data.getUserAgentName());
		Assert.assertNull(data.getUserAgentMajor());
	}

	/**
	 * Test expected values are returned.
	 */
	@Test
	public void testExpected() {

		final String timestamp = "2016-12-02T14:32:45.000Z";
		final String destination = "173.194.32.183";
		final String source = "10.110.149.227";
		final String host = "www.google.de";
		final String port = "443";
		final String uri = "/ads/user-lists/1072409767/";
		final String scStatus = "200";
		final String userAgentOs = "Windows 7";
		final String userAgentName = "Firefox";
		final String userAgentMajor = "50";

		SyslogProxyQueryResultField field = new SyslogProxyQueryResultField();
		field.setTimestamp(timestamp);
		field.setDestination(destination);
		field.setSource(source);
		field.setHost(host);
		field.setPort(port);
		field.setUri(uri);
		field.setScStatus(scStatus);
		field.setUserAgentOs(userAgentOs);
		field.setUserAgentName(userAgentName);
		field.setUserAgentMajor(userAgentMajor);

		SyslogProxyReturnResultData data = new SyslogProxyReturnResultData(field);
		Assert.assertEquals(timestamp, data.getTimestamp());
		Assert.assertEquals(destination, data.getDestination());
		Assert.assertEquals(source, data.getSource());
		Assert.assertEquals(host, data.getHost());
		Assert.assertEquals(port, data.getPort());
		Assert.assertEquals(uri, data.getUri());
		Assert.assertEquals(scStatus, data.getScStatus());
		Assert.assertEquals(userAgentOs, data.getUserAgentOs());
		Assert.assertEquals(userAgentName, data.getUserAgentName());
		Assert.assertEquals(userAgentMajor, data.getUserAgentMajor());
	}
}

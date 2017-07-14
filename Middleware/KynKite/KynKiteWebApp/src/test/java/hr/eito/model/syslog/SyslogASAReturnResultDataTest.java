
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
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the SyslogASAReturnResultData.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class SyslogASAReturnResultDataTest {

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
	 * Test that timestamps with offset work.
	 */
	@Test
	public void testDefault() {
		SyslogASAReturnResultData d = new SyslogASAReturnResultData();

		Assert.assertNull(d.getMessage());
		Assert.assertNull(d.getAsa_fw());
		Assert.assertNull(d.getTimestamp());
	}

	/**
	 * Test expected values are returned.
	 */
	@Test
	public void testExpected() {
		SyslogASAReturnResultData d = new SyslogASAReturnResultData();

		final String message = "This is a test message";
		d.setMessage(message);
		Assert.assertEquals(message, d.getMessage());

		final String asa_fw = "ASA Firewall";
		d.setAsa_fw(asa_fw);
		Assert.assertEquals(asa_fw, d.getAsa_fw());

		// Note, this does not test the JSON serialization.
		final String timestamp = "2016-12-13T10:35:46.123+01:00";
		d.setTimestamp(timestamp);
		Assert.assertEquals(timestamp, d.getTimestamp());
	}
}

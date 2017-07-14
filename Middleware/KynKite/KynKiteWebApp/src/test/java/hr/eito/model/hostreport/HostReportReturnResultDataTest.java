
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
package hr.eito.model.hostreport;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the HostReportReturnResultData bean.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class HostReportReturnResultDataTest {

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
	 * Test that the default params are what's expected.
	 */
	@Test
	public void checkDefault() {
		HostReportReturnResultData d = new HostReportReturnResultData();

		Assert.assertEquals("", d.getHost());
		Assert.assertEquals("", d.getEvents());
		Assert.assertEquals(0,  d.getSeverity());
		Assert.assertEquals("", d.getFirstDetection());
		Assert.assertEquals("", d.getLastDetection());
	}

	/**
	 * Test that setting the timestamps works.
	 */
	@Test
	public void checkTimestamps() {
		HostReportReturnResultData d = new HostReportReturnResultData();

		final String first = "2016-12-15T11:34:56.000+01:00";
		final String last  = "2015-12-15T11:34:56.000+01:00";

		d.setFirstDetection(first);
		d.setLastDetection(last);

		Assert.assertEquals(first, d.getFirstDetection());
		Assert.assertEquals(last,  d.getLastDetection());
	}

	/**
	 * Test that setting the host works.
	 */
	@Test
	public void checkHost() {
		HostReportReturnResultData d = new HostReportReturnResultData();

		final String host = "10.11.12.13";

		d.setHost(host);

		Assert.assertEquals(host, d.getHost());
	}

	/**
	 * Test that setting the event and severity works. This is the only complex test here!
	 */
	@Test
	public void checkEventSeverity() {
		HostReportReturnResultData d = new HostReportReturnResultData();

		addAndCheck_(d, "event1", 1, "event1", 1);
		addAndCheck_(d, "event2", 2, "event1, event2", 3);
		addAndCheck_(d, "event3", 12, "event1, event2, event3", 15);
		// Now add the same event again - should not change the severity.
		addAndCheck_(d, "event1", 1, "event1, event2, event3", 15);

	}

	private void addAndCheck_(final HostReportReturnResultData d,
							  final String event,
							  final int severity,
							  final String expectedEvents,
							  final int expectedSeverity) {

		d.addEvent(event, severity);
		Assert.assertEquals(expectedEvents, d.getEvents());
		Assert.assertEquals(expectedSeverity, d.getSeverity());

	}
}

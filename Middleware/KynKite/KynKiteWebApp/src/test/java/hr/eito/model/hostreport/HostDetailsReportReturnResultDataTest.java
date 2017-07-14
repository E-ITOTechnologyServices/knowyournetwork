
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
 * Tests the HostDetailsReportReturnResultData bean.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class HostDetailsReportReturnResultDataTest {

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
	 * Test that the default values are what is expected.
	 */
	@Test
	public void checkDefault() {
		HostDetailsReportReturnResultData d = new HostDetailsReportReturnResultData();

		Assert.assertEquals("", d.getEventType());
		Assert.assertEquals(0, d.getNumber());
		Assert.assertEquals("", d.getFirstDetection());
		Assert.assertEquals("", d.getLastDetection());
		Assert.assertEquals("", d.getDescription());
		Assert.assertEquals("", d.getLink());
		Assert.assertEquals(15, d.getDateOffset());
	}

	/**
	 * Test that setting values on the object work.
	 */
	@Test
	public void checkSetters() {

		final String eventType = "hello";
		final int count = 5;
		final String first = "2016-12-15T11:34:56.000+01:00";
		final String last  = "2015-12-15T11:34:56.000+01:00";
		final String description = "descrip";
		final String elkLink = "http://this.is/the/elk/link";
		final int dateOffset = 22;

		setAndCheck_(eventType, count, first, last, description, elkLink, dateOffset);
	}

	/**
	 * Set values on an object and check we get the expected ones out again. This really is just
	 * checking the getters and setters match.
	 *
	 * @param eventType the eventType to check
	 * @param count the count to check
	 * @param first the first detection time to check
	 * @param last the last detection time to check
	 * @param description the description to check
	 * @param elkLink the ELK link to check
	 * @param dateOffset the date offset to check
	 */
	private void setAndCheck_(final String eventType,
							  final int count,
							  final String first,
							  final String last,
							  final String description,
							  final String elkLink,
							  final int dateOffset) {

		HostDetailsReportReturnResultData d = new HostDetailsReportReturnResultData();

		d.setEventType(eventType);
		d.setCount(count);
		d.setFirstDetection(first);
		d.setLastDetection(last);
		d.setDescription(description);
		d.setLink(elkLink);
		d.setDateOffset(dateOffset);

		Assert.assertEquals(eventType, d.getEventType());
		Assert.assertEquals(count, d.getNumber());
		Assert.assertEquals(first, d.getFirstDetection());
		Assert.assertEquals(last, d.getLastDetection());
		Assert.assertEquals(description, d.getDescription());
		Assert.assertEquals(elkLink, d.getLink());
		Assert.assertEquals(dateOffset, d.getDateOffset());
	}
}

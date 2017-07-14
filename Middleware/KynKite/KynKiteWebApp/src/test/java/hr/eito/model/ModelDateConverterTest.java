
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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the ModelDateConverter.
 * <p>Principally, this tests that parse errors return the empty string.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class ModelDateConverterTest {

	private ModelDateConverter dc_ = new ModelDateConverter();

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
	public void withOffset() {
		checkExpected("2016-12-01T11:00:00.000+01:00", "2016-12-01 10:00");
		checkExpected("2016-12-01T08:00:00.000+00:00", "2016-12-01 08:00");
		checkExpected("2016-12-01T11:00:00.000-01:00", "2016-12-01 12:00");
	}

	/**
	 * Test that timestamps with no colon offset return an empty string.
	 */
	@Test
	public void withNoColonOffset() {
		checkExpected("2016-12-01T11:00:00.000+0100", "");
	}

	/**
	 * Test that timestamps with Zulu offset work.
	 */
	@Test
	public void withZuluOffset() {
		checkExpected("2016-12-01T11:00:00.000Z", "2016-12-01 11:00");
		checkExpected("2016-12-01T11:00:00Z", "2016-12-01 11:00");
	}

	/**
	 * Test that timestamps with no offset return an empty string.
	 */
	@Test
	public void withNoOffset() {
		checkExpected("2016-12-01T11:00:00.000", "");
		checkExpected("2016-12-01T11:00:00", "");
	}

	/**
	 * Test that timestamps with no milliseconds work.
	 */
	@Test
	public void withNoMilliseconds() {
		checkExpected("2016-12-01T11:00:00+01:00", "2016-12-01 10:00");
		checkExpected("2016-12-01T08:00:00+00:00", "2016-12-01 08:00");
		checkExpected("2016-12-01T11:00:00-01:00", "2016-12-01 12:00");
		// Issue #228
		checkExpected("2016-11-29T23:59:02+01:00", "2016-11-29 22:59");
	}

	/**
	 * Utility to check equality. This checks that the value parses successfully and
	 * then checks if the value is as expected.
	 *
	 * @param source the value to try and parse
	 * @param expected the expected value after conversion
	 */
	private void checkExpected(final String source, final String expected) {
		final String result = parseOK(source);
		Assert.assertEquals(expected, result);
	}

	/**
	 * Utility to check a parse works without exception.
	 *
	 * @param source the value to try and parse
	 */
	private String parseOK(final String source) {

		try {
			return c_(source);
		}
		catch (final Exception e) {
			Assert.fail("Did not expect exception parsing <" + source + ">");
		}

		return null;
	}

	/**
	 * Utility to make the conversion succinct.
	 *
	 * @param source the value to parse
	 */
	private String c_(final String source) {
		return dc_.toUTCDateTime(source);
	}
	
	/**
	 * Test toUtilDate with good source
	 */
	@Test
	public void toUtilDateOK() {
		String source = "2017-02-01 12:45:12.333";
		Date date = dc_.toUtilDate(source);
		Assert.assertNotNull(date);
	}
	
	/**
	 * Test toUtilDate with broken source
	 */
	@Test
	public void toUtilDateBroken() {
		String source = "2017-02-01 12:45:12";
		Date date = dc_.toUtilDate(source);
		Assert.assertNull(date);
	}
	
	/**
	 * Test toUTCDateTime with good source
	 */
	@Test
	public void toUTCDateTimeOK() {
		Date date = new Date();
		String dateString = dc_.toUTCDateTime(date);
		Assert.assertFalse(dateString.isEmpty());
	}
	
	/**
	 * Test toUTCDateTime with broken source
	 */
	@Test
	public void toUTCDateTimeBroken() {
		Date date = null;
		String dateString = dc_.toUTCDateTime(date);
		Assert.assertTrue(dateString.isEmpty());
	}
	
	/**
	 * Test toSpecificTimezoneDateTime with good source
	 */
	@Test
	public void toSpecificTimezoneDateTimeOK() {
		String source = "2017-02-01T23:45:12.321Z";
		String dateString = dc_.toSpecificTimezoneDateTime(source);
		Assert.assertEquals("2017-02-02 00:45", dateString);
	}
	
	/**
	 * Test toSpecificTimezoneDateTime with broken source
	 */
	@Test
	public void toSpecificTimezoneDateTimeBroken() {
		String source = "2017-02-01T12:45:12.321";
		String dateString = dc_.toSpecificTimezoneDateTime(source);
		Assert.assertTrue(dateString.isEmpty());
	}
	
	/**
	 * Test toTimeSuffix with good source
	 */
	@Test
	public void toTimeSuffixOK() {
		// Prepare basic data
		int hours = 13;
		int minutes = 12;
		int seconds = 11;
		
		// Manually create expected result out of basic data
		String timeStringExpected = new StringBuilder().append(hours).append(minutes).append(seconds).toString();
		
		// Create source date out of basic data
		Date date = new GregorianCalendar(2014, Calendar.FEBRUARY, 11, 13, 12, 11).getTime();
		
		// Call tested method
		String timeString = dc_.toTimeSuffix(date);
		
		// Assert results
		Assert.assertFalse(timeString.isEmpty());
		Assert.assertEquals(timeStringExpected, timeString);
	}
	
	/**
	 * Test toTimeSuffix with broken source
	 */
	@Test
	public void toTimeSuffixBroken() {
		Date date = null;
		String dateString = dc_.toTimeSuffix(date);
		Assert.assertTrue(dateString.isEmpty());
	}
	
	/**
	 * Test toMidnightDateString with good source
	 */
	@Test
	public void toMidnightDateStringOK() {
		Date date = new Date();
		
		// Call tested method
		String dateString = dc_.toMidnightDateString(date);
		
		// Assert results
		Assert.assertFalse(dateString.isEmpty());
	}
	
	/**
	 * Test toMidnightDateString with broken source
	 */
	@Test
	public void toMidnightDateStringBroken() {
		Date date = null;
		String dateString = dc_.toMidnightDateString(date);
		Assert.assertTrue(dateString.isEmpty());
	}
	
	/**
	 * Test toOrderedDateString with good source
	 */
	@Test
	public void toOrderedDateStringOK() {
		Date date = new Date();
		
		// Call tested method
		String dateString = dc_.toOrderedDateString(date);
		
		// Assert results
		Assert.assertFalse(dateString.isEmpty());
	}
	
	/**
	 * Test toOrderedDateString with broken source
	 */
	@Test
	public void toOrderedDateStringBroken() {
		Date date = null;
		String dateString = dc_.toOrderedDateString(date);
		Assert.assertTrue(dateString.isEmpty());
	}
	
}

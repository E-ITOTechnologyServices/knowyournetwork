
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


package hr.eito.kynkite.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 * Tests the DateConverter.
 * <p>We do not want to spend time testing the underlying Java implementation
 * simply to check that sufficient formats are suppported for our needs. Dates
 * need to contain a timezone for the conversion class to work due to the
 * requirement to convert to UTC.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class DateConverterTest {

	private DateConverter dc_ = new DateConverter();

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
	 * Test that timestamps with no colon offset throw as expected. We don't support
	 * timezone offsets that don't contain a colon.
	 */
	@Test
	public void withNoColonOffset() {
		parseException("2016-12-01T11:00:00.000+0100");
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
	 * Test that timestamps with no offset fails. It is not possible to construct a
	 * UTC timestamp from one that gives no information about its source timezone.
	 */
	@Test
	public void withNoOffset() {
		parseException("2016-12-01T11:00:00.000");
		parseException("2016-12-01T11:00:00");
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
	 * Utility to check a parse exception occurs.
	 *
	 * @param source the value to attempt the parse on
	 */
	private void parseException(final String source) {
		try {
			c_(source);
			Assert.fail("Expected java.time.format.DateTimeParseException");
		}
		catch (final java.time.format.DateTimeParseException e) {
		}
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
	 * Test that timestamp yyyy-MM-dd HH:mm:ss.SSS converts to correct java.util.Date
	 */
	@Test
	public void toUtilDateOK() {
		String source = "2017-02-02 23:15:18.004";
		Date date = parseToUtilDateOK(source);
		Assert.assertEquals(source, utilDateToString(date));		
	}
	
	/**
	 * Test that timestamp yyyy-MM-dd HH:mm:ss.SSS converts to correct java.util.Date
	 * @throws ParseException 
	 */
	@Test(expected=ParseException.class)
	public void toUtilDateException() throws ParseException {
		String source = "2017-02-02 23:15:18";
		parseToUtilDateException(source);		
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
	
	/**
	 * Utility to check a parse works without exception while toUtilDate.
	 *
	 * @param source the value to try and parse
	 */
	private Date parseToUtilDateOK(final String source) {
		try {
			return cToUtilDate_(source);
		}
		catch (final ParseException e) {
			Assert.fail("Did not expect exception parsing <" + source + ">");
		}
		return null;
	}

	/**
	 * Utility to check a parse exception occurs while toUtilDate.
	 *
	 * @param source the value to attempt the parse on
	 * @throws ParseException 
	 */
	private void parseToUtilDateException(final String source) throws ParseException {
		cToUtilDate_(source);
	}

	/**
	 * Utility to make the conversion succinct while toUtilDate.
	 *
	 * @param source the value to parse
	 * @throws ParseException 
	 */
	private Date cToUtilDate_(final String source) throws ParseException {
		return dc_.toUtilDate(source);
	}
	
	/**
	 * Test that java.util.Date converts to timestamp yyyy-MM-dd HH:mm:ss.SSS+01:00 String
	 * @throws ParseException 
	 */
	@Test
	public void toUTCDateTimeStringOK() throws ParseException {
		String source = "2017-02-02T23:15:18.004";
		Date date = stringToUtilDate(source);
		String dateString = parseToUTCDateTimeStringOK(date);
		Assert.assertEquals("2017-02-02T23:15:18.004+01:00", dateString);		
	}
	
	/**
	 * Utility to check a parse works without exception while toUTCDateTimeString.
	 *
	 * @param source the value to try and parse
	 */
	private String parseToUTCDateTimeStringOK(final Date source) {
		return cToUTCDateTimeString_(source);
	}

	/**
	 * Utility to make the conversion succinct while toUTCDateTimeString.
	 *
	 * @param source the value to parse
	 */
	private String cToUTCDateTimeString_(final Date source) {
		return dc_.toUTCDateTimeString(source);
	}
	
	/**
	 * Utility to convert java.util.date to string
	 *
	 * @param source the value to parse
	 * @throws ParseException 
	 */
	private Date stringToUtilDate(final String source) throws ParseException {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		return formatter.parse(source);
	}
	/**
	 * Test converting to specified timezone datetime
	 * <p>When converts to correct value
	 */
	@Test
	public void testToSpecificTimezoneDateTime_parseException() {
		// When UTC time converts to CET time next day
		Assert.assertEquals("2017-03-08 00:10", dc_.toSpecificTimezoneDateTime("2017-03-07T23:10:35.321Z", 1));
		
		// When UTC second format converts to CET
		Assert.assertEquals("2017-03-08 00:10", dc_.toSpecificTimezoneDateTime("2017-03-07T23:10:35.321+00:00", 1));
		
		// When CET converts to UTC
		Assert.assertEquals("2017-03-07 22:10", dc_.toSpecificTimezoneDateTime("2017-03-07T23:10:35.321+01:00", 0));
		
		// When EST converts to CET
		Assert.assertEquals("2017-03-07 09:16", dc_.toSpecificTimezoneDateTime("2017-03-07T03:16:59.321-05:00", 1));
	}
	
	/**
	 * Test converting to specified timezone datetime
	 * <p>When DateTimeParseException exception happens
	 */
	@Test
	public void testToSpecificTimezoneDateTime() {
		final String source = "2017-03-07T09:10:35.321";
		final int hoursOffsetFromUTC = 1;
		try {
			dc_.toSpecificTimezoneDateTime(source, hoursOffsetFromUTC);
			Assert.fail("Expected java.time.format.DateTimeParseException");
		}
		catch (final java.time.format.DateTimeParseException e) {
		}
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
		Date source = null;
		try {
			dc_.toTimeSuffix(source);
			Assert.fail();
		}
		catch (final NullPointerException e) {
		}
	}
	
	/**
	 * Test toMidnightDateString with good source
	 */
	@Test
	public void toMidnightDateStringOK() {
		// Manually create expected result out of basic data
		String dateTimeStringExpected = "2014-02-10T00:00:00+0000";
		
		// Create source date out of basic data
		Date date = new GregorianCalendar(2014, Calendar.FEBRUARY, 10, 0, 0, 0).getTime();
		
		// Call tested method
		String timeString = dc_.toMidnightDateString(date);
		
		// Assert results
		Assert.assertFalse(timeString.isEmpty());
		Assert.assertEquals(dateTimeStringExpected, timeString);
	}
	
	/**
	 * Test toMidnightDateString with broken source
	 */
	@Test
	public void toMidnightDateStringBroken() {
		Date source = null;
		try {
			dc_.toMidnightDateString(source);
			Assert.fail();
		}
		catch (final NullPointerException e) {
		}
	}
	
	/**
	 * Test toOrderedDateString with good source
	 */
	@Test
	public void toOrderedDateStringOK() {
		// Manually create expected result out of basic data
		String dateTimeStringExpected = "20140210";
		
		// Create source date out of basic data
		Date date = new GregorianCalendar(2014, Calendar.FEBRUARY, 10, 0, 0, 0).getTime();
		
		// Call tested method
		String timeString = dc_.toOrderedDateString(date);
		
		// Assert results
		Assert.assertFalse(timeString.isEmpty());
		Assert.assertEquals(dateTimeStringExpected, timeString);
	}
	
	/**
	 * Test toOrderedDateString with broken source
	 */
	@Test
	public void toOrderedDateStringBroken() {
		Date source = null;
		try {
			dc_.toOrderedDateString(source);
			Assert.fail();
		}
		catch (final NullPointerException e) {
		}
	}
	
}

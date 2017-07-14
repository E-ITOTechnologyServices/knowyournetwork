
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Date conversion routines.
 * <p>These routines are based on the Java 8 java.time.* classes. Note that dates need to
 * contain a timezone for this converter to work - it is not optional. This is because of
 * the requirement to return dates as UTC. Only the milliseconds are optional.
 *
 * @author Steve Chaplin
 *
 */
public class DateConverter {

	private static final DateTimeFormatter iDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX");
	private static final DateTimeFormatter oDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	
	// Frontend receiving datetime format
	private static final String feFilterDateTimeFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	// ElasticSearch sending datetime format
	private static final String esFilterDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'+01:00'";
	
	// Time suffix format
	private static final String suffixFilterTimeFormat = "HHmmss";
	
	// Report generation script sending datetime format
	private static final String rgFilterDateTimeFormat = "yyyy-MM-dd'T00:00:00+0000'";
	
	// Report naming dates
	private static final String rnFilterDateTimeFormat = "yyyyMMdd";

	/**
	 * Convert ISO8601 timestamp to specific timezone date time format 'dd-MM-yyyy HH:MI'. This will throw
	 * java.time.format.DateTimeParseException if the timestamp can not be parsed.
	 *
	 * @throws java.time.format.DateTimeParseException
	 *
	 * @param source the ISO8601 timestamp to convert
	 * @param hoursOffsetFromUTC hours offset from UTC
	 *
	 * @return the converted timestamp
	 */
	public String toSpecificTimezoneDateTime(final String source, final int hoursOffsetFromUTC) {
		final OffsetDateTime from = OffsetDateTime.parse(source, iDateTimeFormatter);
		final OffsetDateTime specificTimezoneDateTime = from.withOffsetSameInstant(ZoneOffset.ofHours(hoursOffsetFromUTC));

		return specificTimezoneDateTime.format(oDateTimeFormatter);
	}
	
	/**
	 * Convert ISO8601 timestamp to UTC date time format 'dd-MM-yyyy HH:MI'. This will throw
	 * java.time.format.DateTimeParseException if the timestamp can not be parsed.
	 *
	 * @throws java.time.format.DateTimeParseException
	 *
	 * @param  source the ISO8601 timestamp to convert
	 *
	 * @return the converted timestamp
	 */
	public String toUTCDateTime(final String source) {

		final OffsetDateTime from = OffsetDateTime.parse(source, iDateTimeFormatter);
		final OffsetDateTime utc = from.withOffsetSameInstant(ZoneOffset.UTC);

		return utc.format(oDateTimeFormatter);
	}
	
	/**
	 * Convert yyyy-MM-dd HH:mm:ss.SSS timestamp to java.util.Date. This will throw
	 * java.time.format.DateTimeParseException if the timestamp can not be parsed.
	 *
	 * @throws ParseException
	 *
	 * @param  source the yyyy-MM-dd HH:mm:ss.SSS timestamp to convert
	 *
	 * @return the converted timestamp as java.util.Date
	 */
	public Date toUtilDate(final String source) throws ParseException {
		DateFormat formatter = new SimpleDateFormat(feFilterDateTimeFormat);
		return formatter.parse(source);
	}
	
	/**
	 * Convert from java.util.Date to yyyy-MM-dd HH:mm:ss.SSS
	 *
	 * @param source the java.util.Date to convert
	 *
	 * @return the converted date String or empty String
	 */
	public String toUTCDateTimeString(final Date source) {
		DateFormat formatter = new SimpleDateFormat(esFilterDateTimeFormat);
		return formatter.format(source);
	}
	
	/**
	 * Convert from java.util.Date to HHmmss
	 *
	 * @param source the java.util.Date to convert
	 *
	 * @return the converted date String or empty String
	 */
	public String toTimeSuffix(final Date source) {
		DateFormat formatter = new SimpleDateFormat(suffixFilterTimeFormat);
		return formatter.format(source);
	}
	
	/**
	 * Convert from java.util.Date to yyyy-MM-ddT00:00:00+0000
	 *
	 * @param source the java.util.Date to convert
	 *
	 * @return the converted date String or empty String
	 */
	public String toMidnightDateString(final Date source) {
		DateFormat formatter = new SimpleDateFormat(rgFilterDateTimeFormat);
		return formatter.format(source);
	}
	
	/**
	 * Convert from java.util.Date to yyyyMMdd
	 *
	 * @param source the java.util.Date to convert
	 *
	 * @return the converted date String or empty String
	 */
	public String toOrderedDateString(final Date source) {
		DateFormat formatter = new SimpleDateFormat(rnFilterDateTimeFormat);
		return formatter.format(source);
	}
	
}

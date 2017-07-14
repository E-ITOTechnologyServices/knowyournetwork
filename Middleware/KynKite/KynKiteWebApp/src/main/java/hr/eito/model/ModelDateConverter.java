
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


package hr.eito.model;

import java.text.ParseException;
import java.util.Date;

import hr.eito.kynkite.utils.DateConverter;

/**
 * Model date converter.
 * <p>Encapsulates the behaviour required by the model when converting dates, particularly
 * in the face of parse exceptions.
 *
 * @author Steve Chaplin
 *
 */
public class ModelDateConverter {

	private final DateConverter dc_ = new DateConverter();
	
	/**
	 * Convert ISO8601 timestamp to specific timezone date time format 'dd-MM-yyyy HH:MI'. If the conversion
	 * fails due to a parser error, then we get an empty string as the result.
	 *
	 * @param  source the ISO8601 timestamp to convert
	 *
	 * @return the converted timestamp or an empty string.
	 */
	public String toSpecificTimezoneDateTime(final String source) {
		// Set to CET hours offset from UTC
		int hoursOffsetFromUTC = 1;
		String result = "";
		try {
			result = dc_.toSpecificTimezoneDateTime(source, hoursOffsetFromUTC);
		}
		catch (final java.time.format.DateTimeParseException e) {}
		return result;
	}

	/**
	 * Convert ISO8601 timestamp to UTC date time format 'dd-MM-yyyy HH:MI'. If the conversion
	 * fails due to a parser error, then we get an empty string as the result.
	 *
	 * @param  source the ISO8601 timestamp to convert
	 *
	 * @return the converted timestamp or an empty string.
	 */
	public String toUTCDateTime(final String source) {

		String result = "";

		try {
			result = dc_.toUTCDateTime(source);
		}
		catch (final java.time.format.DateTimeParseException e) {
		}

		return result;
	}
	
	/**
	 * Convert yyyy-MM-dd HH:mm:ss.SSS timestamp as String
	 * to java.util.Date date object. If the convert fails
	 * return null value as the result
	 *
	 * @param source the yyyy-MM-dd HH:mm:ss.SSS timestamp to convert
	 *
	 * @return the converted date object or null
	 */
	public Date toUtilDate(final String source) {
		Date result = null;
		try {
			result = dc_.toUtilDate(source);
		} catch (final ParseException e) {}
		return result;
	}
	
	/**
	 * Convert from java.util.Date to yyyy-MM-dd HH:mm:ss.SSS
	 *
	 * @param source the java.util.Date to convert
	 *
	 * @return the converted date String or empty String
	 */
	public String toUTCDateTime(final Date source) {
		String result = "";
		try {
			result = dc_.toUTCDateTimeString(source);
		} catch (final Exception e) {}
		return result;
	}
	
	/**
	 * Convert from java.util.Date to HHmmss
	 *
	 * @param source the java.util.Date to convert
	 *
	 * @return the converted date String or empty String
	 */
	public String toTimeSuffix(final Date source) {
		String result = "";
		try {
			result = dc_.toTimeSuffix(source);
		} catch (final Exception e) {}
		return result;
	}
	
	/**
	 * Convert from java.util.Date to yyyy-MM-ddT00:00:00+0000
	 *
	 * @param source the java.util.Date to convert
	 *
	 * @return the converted date String or empty String
	 */
	public String toMidnightDateString(final Date source) {
		String result = "";
		try {
			result = dc_.toMidnightDateString(source);
		} catch (final Exception e) {}
		return result;
	}
	
	/**
	 * Convert from java.util.Date to yyyyMMdd
	 *
	 * @param source the java.util.Date to convert
	 *
	 * @return the converted date String or empty String
	 */
	public String toOrderedDateString(final Date source) {
		String result = "";
		try {
			result = dc_.toOrderedDateString(source);
		} catch (final Exception e) {}
		return result;
	}
	
}

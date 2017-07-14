
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


package hr.eito.model.newsticker;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the NewsTickerReturnResultData.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class NewsTickerReturnResultDataTest {

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
	 * Test that passing a null result field is OK.
	 */
	@Test
	public void testNullField() {
		NewsTickerReturnResultData d = new NewsTickerReturnResultData(null);
		Assert.assertNull(d.getHost());
	}

	/**
	 * Test that the package private Data class works. This should really be factored out into a
	 * separate class.
	 */
	@Test
	public void testData() {

		final String host = "a";
		final String text = "b";
		final String timestamp = "2016-12-21T10:20:30.000Z";
		final int priority = 1;
		final String eventId = "2";
		final int expectedType = 100;

		NewsTickerQueryResultField f = makeField(host, text, timestamp, priority, eventId);

		NewsTickerReturnResultData d = new NewsTickerReturnResultData(f);

		Assert.assertEquals(host, d.getHost());
		Assert.assertEquals(text, d.getText());
		Assert.assertEquals(timestamp, d.getTimestamp());
		Assert.assertEquals(priority, d.getPriority());
		Assert.assertEquals(expectedType, d.getType());
	}

	/**
	 * Test an empty eventid.
	 */
	@Test
	public void testEmptyEventId() {

		final String host = "a";
		final String text = "b";
		final String timestamp = "2016-12-21T10:20:30.000Z";
		final int priority = 1;
		final String eventId = "";
		final int expectedType = 0;

		NewsTickerQueryResultField f = makeField(host, text, timestamp, priority, eventId);

		NewsTickerReturnResultData d = new NewsTickerReturnResultData(f);

		Assert.assertEquals(host, d.getHost());
		Assert.assertEquals(text, d.getText());
		Assert.assertEquals(timestamp, d.getTimestamp());
		Assert.assertEquals(priority, d.getPriority());
		Assert.assertEquals(expectedType, d.getType());
	}

	/**
	 * Test a higher value eventid.
	 */
	@Test
	public void testHighEventId() {

		final String host = "a";
		final String text = "b";
		final String timestamp = "2016-12-21T10:20:30.000Z";
		final int priority = 1;
		final String eventId = "6000";
		final int expectedType = 200;

		NewsTickerQueryResultField f = makeField(host, text, timestamp, priority, eventId);

		NewsTickerReturnResultData d = new NewsTickerReturnResultData(f);

		Assert.assertEquals(host, d.getHost());
		Assert.assertEquals(text, d.getText());
		Assert.assertEquals(timestamp, d.getTimestamp());
		Assert.assertEquals(priority, d.getPriority());
		Assert.assertEquals(expectedType, d.getType());
	}

	/**
	 * Test an invalid value eventid.
	 */
	@Test
	public void testInvalidEventId() {

		final String host = "a";
		final String text = "b";
		final String timestamp = "2016-12-21T10:20:30.000Z";
		final int priority = 1;
		final String eventId = "xx";
		final int expectedType = 0;

		NewsTickerQueryResultField f = makeField(host, text, timestamp, priority, eventId);

		NewsTickerReturnResultData d = new NewsTickerReturnResultData(f);

		Assert.assertEquals(host, d.getHost());
		Assert.assertEquals(text, d.getText());
		Assert.assertEquals(timestamp, d.getTimestamp());
		Assert.assertEquals(priority, d.getPriority());
		Assert.assertEquals(expectedType, d.getType());
	}

	/**
	 * Create a NewsTickerQueryResultField with the specified values.
	 *
	 * @param host the host name to set
	 * @param text the text of the newsticker event
	 * @param timestamp the timestamp of the newsticker event
	 * @param priority the priority of the event
	 * @param eventId the event id of the newsticker event
	 *
	 * @return the resultant Field
	 */
	NewsTickerQueryResultField makeField(final String host,
										 final String text,
										 final String timestamp,
										 final int priority,
										 final String eventId) {

		NewsTickerQueryResultField f = new NewsTickerQueryResultField();

		f.setHost(host);
		f.setText(text);
		f.set$timestamp(timestamp);
		f.setPriority(priority);
		f.setEventid(eventId);

		return f;
	}
}

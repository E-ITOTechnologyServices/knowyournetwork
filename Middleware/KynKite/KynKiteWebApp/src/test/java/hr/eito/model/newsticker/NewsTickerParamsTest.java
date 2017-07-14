
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
 * Tests the NewsTickerParams.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class NewsTickerParamsTest {

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
		NewsTickerParams p = new NewsTickerParams();

		final int defaultSize = 5;

		Assert.assertEquals(defaultSize, p.getSize());
		Assert.assertNull(p.getQuery());
	}

	/**
	 * Check that setting some sizes does what's expected.
	 */
	@Test
	public void checkSizes() {
		checkSize(-1, 0);
		checkSize(0, 0);
		checkSize(1, 1);
		checkSize(5, 5);
		checkSize(10, 10);
	}

	/**
	 * Check that setting some queries does what's expected.
	 */
	@Test
	public void checkQueries() {
		checkQuery(null, null);
		checkQuery("", "");
		checkQuery("Hello world", "Hello world");
	}

	/**
	 * Check that a newly created param object stores and returns size correctly.
	 *
	 * @param size the size to test
	 * @param expected the size that we expect to be returned
	 */
	private void checkSize(final int size, final int expected) {
		NewsTickerParams p = new NewsTickerParams();
		p.setSize(size);

		Assert.assertEquals(expected, p.getSize());
	}

	/**
	 * Check that a newly created param object stores and returns queries correctly.
	 *
	 * @param query the query to test
	 * @param expected the query that we expect to be returned
	 */
	private void checkQuery(final String query, final String expected) {
		NewsTickerParams p = new NewsTickerParams();
		p.setQuery(query);

		Assert.assertEquals(expected, p.getQuery());
	}
}

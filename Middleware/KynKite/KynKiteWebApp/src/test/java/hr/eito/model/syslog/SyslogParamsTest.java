
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
 * Tests the SyslogParams.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class SyslogParamsTest {

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
	 * Test the default parameters
	 */
	@Test
	public void testDefault() {
		SyslogParams p = new SyslogParams();
		Assert.assertNull(p.getQuery());
	}

	/**
	 * Testing the setting of query expression on several examples
	 */
	@Test
	public void testQueryExpression() {
		testQueryExpression(null, null);
		testQueryExpression("", "");
		testQueryExpression("something", "something");
	}

	/**
	 * Test that SyslogParams object sets and gets query expression correctly
	 *
	 * @param queryExpression that is being set
	 * @param expectedQueryExpression set query expression must be the same as this
	 */
	private void testQueryExpression(final String queryExpression, final String expectedQueryExpression) {
		SyslogParams p = new SyslogParams();
		p.setQuery(queryExpression);
		Assert.assertEquals(expectedQueryExpression, p.getQuery());
	}
	
	/**
	 * Testing the hasAnyParameter method
	 * <p>
	 * Example with zero parameters set
	 */
	@Test
	public void testHasAnyParameter_01() {
		SyslogParams p = new SyslogParams();
		boolean hasAnyParameter = p.hasAnyParameter();
		Assert.assertEquals(hasAnyParameter, false);
	}
	
	/**
	 * Testing the hasAnyParameter method
	 * <p>
	 * Example with at least one parameter set
	 */
	@Test
	public void testHasAnyParameter_02() {
		SyslogParams p = new SyslogParams();
		p.setQuery("some expression");
		boolean hasAnyParameter = p.hasAnyParameter();
		Assert.assertEquals(hasAnyParameter, true);
	}
}

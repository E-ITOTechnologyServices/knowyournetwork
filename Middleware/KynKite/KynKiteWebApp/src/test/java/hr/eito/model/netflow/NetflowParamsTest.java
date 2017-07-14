
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


package hr.eito.model.netflow;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the NetflowParams.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class NetflowParamsTest {

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
		NetflowParams p = new NetflowParams();
		Assert.assertNull(p.getIpAddress());
	}

	/**
	 * Testing the setting of IP address on several examples
	 */
	@Test
	public void testIpAddress() {
		testIpAddressSettingGetting(null, null);
		testIpAddressSettingGetting("", "");
		testIpAddressSettingGetting("something", "something");
	}

	/**
	 * Test that NetflowParams object sets and gets IP address correctly
	 *
	 * @param ipAddress that is being set
	 * @param expectedIpAddress set query expression must be the same as this
	 */
	private void testIpAddressSettingGetting(final String ipAddress, final String expectedIpAddress) {
		NetflowParams p = new NetflowParams();
		p.setIpAddress(ipAddress);
		Assert.assertEquals(expectedIpAddress, p.getIpAddress());
	}
	
	/**
	 * Testing the hasAnyParameter method
	 * <p>
	 * Example with zero parameters set
	 */
	@Test
	public void testHasAnyParameter_01() {
		NetflowParams p = new NetflowParams();
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
		NetflowParams p = new NetflowParams();
		p.setIpAddress("1.1.1.1");
		boolean hasAnyParameter = p.hasAnyParameter();
		Assert.assertEquals(hasAnyParameter, true);
	}
}

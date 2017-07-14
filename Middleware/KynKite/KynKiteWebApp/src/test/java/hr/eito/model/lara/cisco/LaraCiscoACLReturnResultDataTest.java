
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
package hr.eito.model.lara.cisco;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the LaraCiscoACLReturnResultData.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class LaraCiscoACLReturnResultDataTest {

	/**
	 * Runs before the tests start.
	 */
	@BeforeClass
	public static void testStart() {}
	
	/**
	 * Runs after the tests end.
	 */
	@AfterClass
	public static void testEnd() {}

	/**
	 * Test that default data
	 */
	@Test
	public void testDefault() {
		LaraCiscoACLReturnResultData d = new LaraCiscoACLReturnResultData(null);
		Assert.assertNull(d.getAcl());
	}

	/**
	 * Test expected values are returned.
	 */
	@Test
	public void testExpected() {
		final String timestamp = "2016-11-12T11:43:46.123+01:00";
		final String name = "1.1.1.1";
		final String acl = "This is some random\nCisoc ACl text";
		
		LaraCiscoACLQueryResultField field = new LaraCiscoACLQueryResultField();
		field.setTimestamp(timestamp);
		field.setName(name);
		field.setAcl(acl);
		
		LaraCiscoACLReturnResultData d = new LaraCiscoACLReturnResultData(field);
		Assert.assertEquals(acl, d.getAcl());
	}
}

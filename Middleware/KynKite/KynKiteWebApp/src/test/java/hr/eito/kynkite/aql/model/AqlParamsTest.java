
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


package hr.eito.kynkite.aql.model;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the AqlParams.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class AqlParamsTest {

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
		AqlParams p = new AqlParams();
		Assert.assertNull(p.getId());
		Assert.assertNull(p.getRule());
		Assert.assertNull(p.getDescription());
	}

	/**
	 * Testing the getting and setting
	 */
	@Test
	public void testGetSet() {
		Integer id = 1;
		String rule = "rule wtih end spaces   ";
		String description = "   description with spaces on both ends  ";
		
		AqlParams params = new AqlParams();
		params.setId(id);
		params.setRule(rule);
		params.setDescription(description);
		
		Assert.assertEquals(id, params.getId());
		Assert.assertEquals(rule.trim(), params.getRule());
		Assert.assertEquals(description.trim(), params.getDescription());
	}
	
}

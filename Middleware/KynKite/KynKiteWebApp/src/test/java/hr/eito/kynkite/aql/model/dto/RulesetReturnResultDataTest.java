
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


package hr.eito.kynkite.aql.model.dto;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import hr.eito.kynkite.aql.model.Ruleset;

/**
 * Tests the RulesetReturnResultData.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class RulesetReturnResultDataTest {

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
	 * Test the null parameter in constructor
	 */
	@Test
	public void testNullInConstructor() {
		RulesetReturnResultData p = new RulesetReturnResultData(null);
		Assert.assertNull(p.getId());
		Assert.assertNull(p.getRule());
		Assert.assertNull(p.getDescription());
	}

	/**
	 * Testing the getting
	 */
	@Test
	public void testGeting() {
		Integer id = 1;
		String rule = "rule";
		String description = "description";
		
		Ruleset ruleset = new Ruleset();
		ruleset.setId(id);
		ruleset.setRule(rule);
		ruleset.setDescription(description);
		
		RulesetReturnResultData result = new RulesetReturnResultData(ruleset);
		
		Assert.assertEquals(id, result.getId());
		Assert.assertEquals(rule, result.getRule());
		Assert.assertEquals(description, result.getDescription());
	}
	
}

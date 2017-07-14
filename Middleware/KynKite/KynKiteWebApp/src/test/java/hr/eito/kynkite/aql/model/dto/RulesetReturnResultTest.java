
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

import java.util.ArrayList;
import java.util.List;

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
 * Tests the RulesetReturnResult.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class RulesetReturnResultTest {
	
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
	 * Test with a dummy result set.
	 */
	@Test
	public void testEmptyResults() {
		List<Ruleset> q = new ArrayList<>();

		RulesetReturnResult d = new RulesetReturnResult(q);
		Assert.assertEquals(0, d.getData().size());
		Assert.assertEquals(0, d.getRecordsTotal());
		Assert.assertEquals(0, d.getRecordsFiltered());
	}

	/**
	 * Test with some records
	 */
	@Test
	public void testBrokenResults_00() {
		Ruleset r1 = new Ruleset();
		r1.setId(1);
		r1.setRule("Rule for ruleset 1");
		r1.setDescription("");
		
		Ruleset r2 = new Ruleset();
		r2.setId(2);
		r2.setRule("Rule for ruleset 2");
		r2.setDescription("Description for ruleset 2");
		
		List<Ruleset> q = new ArrayList<>();
		q.add(r1);
		q.add(r2);

		RulesetReturnResult d = new RulesetReturnResult(q);
		Assert.assertEquals(2, d.getData().size());
		Assert.assertEquals(2, d.getRecordsTotal());
		Assert.assertEquals(2, d.getRecordsFiltered());
	}
	
}

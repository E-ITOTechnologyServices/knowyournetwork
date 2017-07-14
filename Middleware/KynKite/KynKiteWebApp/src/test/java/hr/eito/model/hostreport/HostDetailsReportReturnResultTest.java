
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
package hr.eito.model.hostreport;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.HashMap;

/**
 * Tests the HostDetailsReportReturnResult bean.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class HostDetailsReportReturnResultTest {

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
		HostDetailsReportReturnResult r = new HostDetailsReportReturnResult();

		Assert.assertEquals(0, r.getData().size());
	}

	/**
	 * Check that some null parameters are handled as expected.
	 */
	@Test
	public void checkMissingParameters() {

		// Null amps should be tollerate but do nothing.
		HostDetailsReportReturnResult r = new HostDetailsReportReturnResult(null, null, null, null);
		Assert.assertEquals(0, r.getData().size());
		r = new HostDetailsReportReturnResult(null, new HashMap<String, String>(), null, null);
		Assert.assertEquals(0, r.getData().size());
		r = new HostDetailsReportReturnResult(null, new HashMap<String, String>(), new HashMap<String, String>(), null);
		Assert.assertEquals(0, r.getData().size());
		r = new HostDetailsReportReturnResult(null, new HashMap<String, String>(), new HashMap<String, String>(), new HashMap<String, Integer>());
		Assert.assertEquals(0, r.getData().size());
	}

	/**
	 * Check that some broken trees are handled correctly.
	 */
	@Test
	public void checkEmptyTrees()
		throws java.io.IOException {
		Map<String, String> descriptions = new HashMap<String, String>();
		Map<String, String> links = new HashMap<String, String>();
		Map<String, Integer> offsets = new HashMap<String, Integer>();

		ObjectMapper mapper = new ObjectMapper();

		// Check empty json (no aggregations).
		String json = "{}";
		JsonNode tree = mapper.readValue(json, JsonNode.class);
		HostDetailsReportReturnResult r = new HostDetailsReportReturnResult(tree, descriptions, links, offsets);
		Assert.assertEquals(0, r.getData().size());

		// Check aggregations containing no events.
		json = "{ \"aggregations\": {} }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new HostDetailsReportReturnResult(tree, descriptions, links, offsets);
		Assert.assertEquals(0, r.getData().size());

		// Check events containing no buckets.
		json = "{ \"aggregations\": { \"events\": {} } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new HostDetailsReportReturnResult(tree, descriptions, links, offsets);
		Assert.assertEquals(0, r.getData().size());

		// Check event bucket with missing min_max.
		json = "{ \"aggregations\": { \"events\": {\"buckets\":[{\"key\": \"hello\"}]} } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new HostDetailsReportReturnResult(tree, descriptions, links, offsets);
		Assert.assertEquals(1, r.getData().size());
		Assert.assertEquals("hello", r.getData().get(0).getEventType());
	}
}

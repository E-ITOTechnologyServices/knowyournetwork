
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


package hr.eito.model.lara.routers;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the LaraRoutersReturnResult class.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class LaraRoutersReturnResultTest {
	
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Test when null JSON is sent
	 */
	@Test
	public void test_nullTree() {
		LaraRoutersReturnResult laraRoutersReturnResult = new LaraRoutersReturnResult(null);
		Assert.assertNotNull(laraRoutersReturnResult);
		Assert.assertEquals(0, laraRoutersReturnResult.getRecordsFiltered());
		Assert.assertEquals(0, laraRoutersReturnResult.getRecordsTotal());
		Assert.assertEquals(0, laraRoutersReturnResult.getData().size());
	}
	
	/**
	 * Test when JSON aggregations missing
	 */
	@Test
	public void test_aggregationsMissing() {
		String json = "{}";
		JsonNode tree = getJsonNode(json);
		LaraRoutersReturnResult laraRoutersReturnResult = new LaraRoutersReturnResult(tree);
		Assert.assertNotNull(laraRoutersReturnResult);
		Assert.assertEquals(0, laraRoutersReturnResult.getRecordsFiltered());
		Assert.assertEquals(0, laraRoutersReturnResult.getRecordsTotal());
		Assert.assertEquals(0, laraRoutersReturnResult.getData().size());
	}
	
	/**
	 * Test when JSON distinct_routers missing
	 */
	@Test
	public void test_distinctRoutersMissing() {
		String json = "{ \"aggregations\": {} }";
		JsonNode tree = getJsonNode(json);
		LaraRoutersReturnResult laraRoutersReturnResult = new LaraRoutersReturnResult(tree);
		Assert.assertNotNull(laraRoutersReturnResult);
		Assert.assertEquals(0, laraRoutersReturnResult.getRecordsFiltered());
		Assert.assertEquals(0, laraRoutersReturnResult.getRecordsTotal());
		Assert.assertEquals(0, laraRoutersReturnResult.getData().size());
	}
	
	/**
	 * Test when JSON buckets missing
	 */
	@Test
	public void test_bucketsMissing() {
		String json = "{ \"aggregations\": { \"distinct_routers\": {} } }";
		JsonNode tree = getJsonNode(json);
		LaraRoutersReturnResult laraRoutersReturnResult = new LaraRoutersReturnResult(tree);
		Assert.assertNotNull(laraRoutersReturnResult);
		Assert.assertEquals(0, laraRoutersReturnResult.getRecordsFiltered());
		Assert.assertEquals(0, laraRoutersReturnResult.getRecordsTotal());
		Assert.assertEquals(0, laraRoutersReturnResult.getData().size());
	}
	
	/**
	 * Test when JSON buckets empty
	 */
	@Test
	public void test_bucketsEmpty() {
		String json = "{ \"aggregations\": { \"distinct_routers\": {\"buckets\":[]} } }";
		JsonNode tree = getJsonNode(json);
		LaraRoutersReturnResult laraRoutersReturnResult = new LaraRoutersReturnResult(tree);
		Assert.assertNotNull(laraRoutersReturnResult);
		Assert.assertEquals(0, laraRoutersReturnResult.getRecordsFiltered());
		Assert.assertEquals(0, laraRoutersReturnResult.getRecordsTotal());
		Assert.assertEquals(0, laraRoutersReturnResult.getData().size());
	}
	
	/**
	 * Test when JSON has buckets
	 */
	@Test
	public void test_data() {
		String json = "{ \"aggregations\": { \"distinct_routers\": {\"buckets\":[{\"key\": \"1.1.1.1\"}, {\"key\": \"2.2.2.2\"}]} } }";
		JsonNode tree = getJsonNode(json);
		LaraRoutersReturnResult laraRoutersReturnResult = new LaraRoutersReturnResult(tree);
		Assert.assertNotNull(laraRoutersReturnResult);
		Assert.assertEquals(2, laraRoutersReturnResult.getRecordsFiltered());
		Assert.assertEquals(2, laraRoutersReturnResult.getRecordsTotal());
		Assert.assertEquals(2, laraRoutersReturnResult.getData().size());
	}
	
	/**
	 * Helper method to create JsonNode out of Json string and manage exceptions
	 * 
	 * @param json as String - creates JsonNode
	 * @return create JsonNode object
	 */
	private JsonNode getJsonNode(String json) {
		JsonNode tree = null;
		try {
			tree = mapper.readValue(json, JsonNode.class);
		} catch (IOException e) {}
		return tree;
	}
}

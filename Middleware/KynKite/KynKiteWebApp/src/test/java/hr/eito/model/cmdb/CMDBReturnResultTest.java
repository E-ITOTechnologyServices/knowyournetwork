
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


package hr.eito.model.cmdb;

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

/**
 * Tests the CMDBReturnResult bean.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class CMDBReturnResultTest {

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
		CMDBReturnResult r = new CMDBReturnResult();

		Assert.assertEquals(0, r.getData().size());
	}

	/**
	 * Check that some null parameters are handled as expected.
	 */
	@Test
	public void checkMissingParameters() {

		CMDBReturnResult r = new CMDBReturnResult(null);
		Assert.assertEquals(0, r.getData().size());
	}

	/**
	 * Check that some broken trees are handled correctly.
	 */
	@Test
	public void checkEmptyTrees()
		throws java.io.IOException {

		ObjectMapper mapper = new ObjectMapper();

		// Check empty json (no hits).
		String json = "{}";
		JsonNode tree = mapper.readValue(json, JsonNode.class);
		CMDBReturnResult r = new CMDBReturnResult(tree);
		Assert.assertEquals(0, r.getData().size());

		// Check hits containing no hits.
		json = "{ \"hits\": {} }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new CMDBReturnResult(tree);
		Assert.assertEquals(0, r.getData().size());

		// Check hits with no _source.
		json = "{ \"hits\": {\"hits\": [{}] } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new CMDBReturnResult(tree);
		Assert.assertEquals(0, r.getData().size());

		// Check _source with no ipam.
		json = "{ \"hits\": {\"hits\": [{\"_source\":{}}] } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new CMDBReturnResult(tree);
		Assert.assertEquals(0, r.getData().size());

		// Check ipam with no ip
		json = "{ \"hits\": {\"hits\": [{\"_source\":{\"ipam\":[{}]}}] } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new CMDBReturnResult(tree);
		Assert.assertEquals(0, r.getData().size());

		// Check ipam with no mask
		json = "{ \"hits\": {\"hits\": [{\"_source\":{\"ipam\":[{\"ip\": \"1.2.3.4\"}]}}] } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new CMDBReturnResult(tree);
		Assert.assertEquals(0, r.getData().size());

		// Check ipam with no maskCidr
		json = "{ \"hits\": {\"hits\": [{\"_source\":{\"ipam\":[{\"ip\": \"1.2.3.4\",\"mask\":\"255.255.255.255\"}]}}] } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new CMDBReturnResult(tree);
		Assert.assertEquals(0, r.getData().size());

		// Check ipam with invalid maskCidr
		json = "{ \"hits\": {\"hits\": [{\"_source\":{\"ipam\":[{\"ip\": \"1.2.3.4\",\"mask\":\"255.255.255.255\",\"mask_cidr\":\"a\"}]}}] } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new CMDBReturnResult(tree);
		Assert.assertEquals(0, r.getData().size());

		// Check ipam with invalid source
		json = "{ \"hits\": {\"hits\": [{\"_source\":{\"ipam\":[{\"ip\": \"1.2.3.4\",\"mask\":\"255.255.255.255\",\"mask_cidr\":32}]}}] } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new CMDBReturnResult(tree);
		Assert.assertEquals(0, r.getData().size());

		// Finally, check a valid record.
		json = "{ \"hits\": {\"hits\": [{\"_source\":{\"ipam\":[{\"ip\": \"1.2.3.4\",\"mask\":\"255.255.255.255\",\"mask_cidr\":32,\"source\":\"IP Admin\"}]}}] } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new CMDBReturnResult(tree);
		Assert.assertEquals(1, r.getData().size());
	}
}

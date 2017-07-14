
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
package hr.eito.model;

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
 * Tests the NodeUtils.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class NodeUtilsTest {

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
	 * Test default construction.
	 */
	@Test
	public void testDefaultCtor() {
		NodeUtils n = new NodeUtils();
	}

	/**
	 * Test fetching a standard string value from a child node.
	 */
	@Test
	public void testBasicString()
		throws java.io.IOException {

		final String json = "{\"hello\": \"world\"}";

		final JsonNode tree = makeTree(json);

		final String result = NodeUtils.safeStringValue(tree, "hello");
		Assert.assertEquals("world", result);
	}

	/**
	 * Test default string value.
	 */
	@Test
	public void testDefaultString()
		throws java.io.IOException {

		final String json = "{\"hello\": \"world\"}";

		final JsonNode tree = makeTree(json);

		String result = NodeUtils.safeStringValue(tree, "xxx", "yyy");
		Assert.assertEquals("yyy", result);

		result = NodeUtils.safeStringValue(tree, "xxx");
		Assert.assertEquals("", result);
	}

	/**
	 * Test fetching a standard int value from a child node.
	 */
	@Test
	public void testBasicInt()
		throws java.io.IOException {

		final String json = "{\"hello\": 3}";

		final JsonNode tree = makeTree(json);

		final int result = NodeUtils.safeIntValue(tree, "hello");
		Assert.assertEquals(3, result);
	}

	/**
	 * Test default int value.
	 */
	@Test
	public void testDefaultInt()
		throws java.io.IOException {

		final String json = "{\"hello\": 3}";

		final JsonNode tree = makeTree(json);

		int result = NodeUtils.safeIntValue(tree, "xxx");
		Assert.assertEquals(0, result);

		result = NodeUtils.safeIntValue(tree, "xxx", 4);
		Assert.assertEquals(4, result);
	}

	/**
	 * Construct a JsonNode tree from the supplied JSON.
	 *
	 * @param json the json from which to construct the tree
	 *
	 * @return the JsonNode tree resulting from the JSON
	 */
	JsonNode makeTree(final String json)
		throws java.io.IOException {

		ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(json, JsonNode.class);
	}
}


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
 * Tests the HostReportReturnResult bean.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class HostReportReturnResultTest {

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
		HostReportReturnResult r = new HostReportReturnResult();

		Assert.assertEquals(0, r.getData().size());
	}

	/**
	 * Test we get out the record we put in.
	 */
	@Test
	public void checkAdding() {
		HostReportReturnResult r = new HostReportReturnResult();

		checkAdd(r, 1, "10.11.12.13", "event1", 12, "2016-12-13T10:11:12.000Z", "2016-12-13T11:11:12.000Z");
		checkAdd(r, 2, "10.11.12.14", "event2",  5, "2011-12-13T10:11:12.000Z", "2011-12-13T11:11:12.000Z");
		checkAdd(r, 3, "10.11.12.15", "event2, event3",  15, "2012-12-13T10:11:12.000Z", "2013-12-13T11:11:12.000Z");
	}

	/**
	 * Check that some null parameters are handled as expected.
	 */
	@Test
	public void checkMissingParameters() {

		// Null severity map should stop anything happening (including null JsonNode tree).
		HostReportReturnResult r = new HostReportReturnResult(null, null);
		Assert.assertEquals(0, r.getData().size());

		// Null tree should be benign even if there is a severity map.
		Map<String, Integer> severities = new HashMap<String, Integer>();
		r = new HostReportReturnResult(null, severities);
		Assert.assertEquals(0, r.getData().size());
	}

	/**
	 * Check that some broken trees are handled correctly.
	 */
	@Test
	public void checkEmptyTrees()
		throws java.io.IOException {
		Map<String, Integer> severities = new HashMap<String, Integer>();
		severities.put("sev1", 10);

		ObjectMapper mapper = new ObjectMapper();

		// Check empty json (no aggregations).
		String json = "{}";
		JsonNode tree = mapper.readValue(json, JsonNode.class);
		HostReportReturnResult r = new HostReportReturnResult(tree, severities);
		Assert.assertEquals(0, r.getData().size());

		// Check aggregations containing no hosts.
		json = "{ \"aggregations\": {} }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new HostReportReturnResult(tree, severities);
		Assert.assertEquals(0, r.getData().size());

		// Check hosts containing no buckets.
		json = "{ \"aggregations\": { \"hosts\": {} } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new HostReportReturnResult(tree, severities);
		Assert.assertEquals(0, r.getData().size());
	}

	/**
	 * Check that some trees with missing values are handled correctly.
	 */
	@Test
	public void checkMissing()
		throws java.io.IOException {
		Map<String, Integer> severities = new HashMap<String, Integer>();
		severities.put("sev1", 10);

		ObjectMapper mapper = new ObjectMapper();

		// Missing events.
		String json = "{ \"aggregations\": { \"hosts\": { \"buckets\": [{ \"key_as_string\":\"x\"}]} } }";
		JsonNode tree = mapper.readValue(json, JsonNode.class);
		HostReportReturnResult r = new HostReportReturnResult(tree, severities);
		Assert.assertEquals(1, r.getData().size());

		// Missing events bucket.
		json = "{ \"aggregations\": { \"hosts\": { \"buckets\": [{ \"key_as_string\":\"x\", \"events\": {}}]} } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new HostReportReturnResult(tree, severities);
		Assert.assertEquals(1, r.getData().size());

		// Events bucket is present but empty.
		json = "{ \"aggregations\": { \"hosts\": { \"buckets\": [{ \"key_as_string\":\"x\", \"events\": {\"buckets\":[{}]}}]} } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new HostReportReturnResult(tree, severities);
		Assert.assertEquals(1, r.getData().size());

		// Key is present in the events bucket, but it does not match anything in the severity map.
		json = "{ \"aggregations\": { \"hosts\": { \"buckets\": [{ \"key_as_string\":\"x\", \"events\": {\"buckets\":[{\"key\":\"sev2\"}]}}]} } }";
		tree = mapper.readValue(json, JsonNode.class);
		r = new HostReportReturnResult(tree, severities);
		Assert.assertEquals(1, r.getData().size());
	}

	/**
	 * Check that adding and retrieving a host record works.
	 *
	 * @param report the result set to test
	 * @param expectedCount how many records we expect in this report after adding this one
	 * @param host the host name to add
	 * @param events the list of events to set
	 * @param severity the list of events to set
	 * @param start the time of first detection
	 * @param end the time of last detection
	 */
	private void checkAdd(final HostReportReturnResult report,
						  final int expectedCount,
						  final String host,
						  final String events,
						  final int severity,
						  final String start,
						  final String end) {

		HostReportReturnResultData source = makeData_(host, events, severity, start, end);
		report.addRecord(source);

		Assert.assertEquals(expectedCount, report.getData().size());

		HostReportReturnResultData result = report.getData().get(expectedCount - 1);
		checkData_(result, host, events, severity, start, end);
	}

	/**
	 * Make a sample host data record.
	 *
	 * @param host the host name to add
	 * @param events the list of events to set
	 * @param severity the list of events to set
	 * @param start the time of first detection
	 * @param end the time of last detection
	 * 
	 * @return the constructed data record
	 */
	private HostReportReturnResultData makeData_(final String host,
												 final String events,
												 final int severity,
												 final String start,
												 final String end) {
		HostReportReturnResultData result = new HostReportReturnResultData();
		result.setHost(host);
		result.addEvent(events, severity);
		result.setFirstDetection(start);
		result.setLastDetection(end);

		return result;
	}

	/**
	 * Check the supplied data record contains the expected data.
	 *
	 * @param record the data record to check
	 * @param host the host name to add
	 * @param events the list of events to set
	 * @param severity the list of events to set
	 * @param start the time of first detection
	 * @param end the time of last detection
	 */
	private void checkData_(final HostReportReturnResultData record,
							final String host,
							final String events,
							final int severity,
							final String start,
							final String end) {

		Assert.assertEquals(host, record.getHost());
		Assert.assertEquals(events, record.getEvents());
		Assert.assertEquals(severity, record.getSeverity());
		Assert.assertEquals(start, record.getFirstDetection());
		Assert.assertEquals(end, record.getLastDetection());
	}
}

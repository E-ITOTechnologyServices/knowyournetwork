
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
package hr.eito.model.syslog;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.ArrayList;

import hr.eito.model.QueryResult;
import hr.eito.model.Hits;
import hr.eito.model.Hit;

/**
 * Tests the SyslogRouterReturnResult.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class SyslogRouterReturnResultTest {

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
	 * Test with a dummy result set.
	 */
	@Test
	public void testEmptyResults() {

		QueryResult<SyslogRouterQueryResultField> q = new QueryResult<SyslogRouterQueryResultField>();

		SyslogRouterReturnResult d = new SyslogRouterReturnResult(q);

		Assert.assertEquals(0, d.getData().size());
		Assert.assertEquals(0, d.getRecordsTotal());
		Assert.assertEquals(0, d.getRecordsFiltered());
	}

	/**
	 * Test with a broken result set (hits but no sub-hits).
	 */
	@Test
	public void testBrokenResults_00() {
		Hits<SyslogRouterQueryResultField> h = new Hits<SyslogRouterQueryResultField>();

		QueryResult<SyslogRouterQueryResultField> q = new QueryResult<SyslogRouterQueryResultField>();
		q.setHits(h);

		SyslogRouterReturnResult d = new SyslogRouterReturnResult(q);

		Assert.assertEquals(0, d.getData().size());
		Assert.assertEquals(0, d.getRecordsTotal());
		Assert.assertEquals(0, d.getRecordsFiltered());
	}

	/**
	 * Test with broken result set (hits but no data in array).
	 */
	@Test
	public void testBrokenResults_01() {
		List<Hit<SyslogRouterQueryResultField>> hits = new ArrayList<Hit<SyslogRouterQueryResultField>>();
		hits.add(new Hit<SyslogRouterQueryResultField>());

		Hits<SyslogRouterQueryResultField> h = new Hits<SyslogRouterQueryResultField>();
		h.setHits(hits);

		QueryResult<SyslogRouterQueryResultField> q = new QueryResult<SyslogRouterQueryResultField>();
		q.setHits(h);

		SyslogRouterReturnResult d = new SyslogRouterReturnResult(q);

		Assert.assertEquals(1, d.getData().size());
		Assert.assertEquals(0, d.getRecordsTotal());
		Assert.assertEquals(1, d.getRecordsFiltered());
	}
}

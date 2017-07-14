
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


package hr.eito.model.syslog;

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

import hr.eito.model.Hit;
import hr.eito.model.Hits;
import hr.eito.model.QueryResult;

/**
 * Tests the SyslogReturnResult.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class SyslogReturnResultTest {
	
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
	 * @throws Exception 
	 */
	@Test
	public void testEmptyResults() throws Exception {
		QueryResult<SyslogProxyQueryResultField> q = new QueryResult<SyslogProxyQueryResultField>();

		SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField> d = new SyslogReturnResult<>(
				q, SyslogProxyReturnResultData.class, SyslogProxyQueryResultField.class);

		Assert.assertEquals(0, d.getData().size());
		Assert.assertEquals(0, d.getRecordsTotal());
		Assert.assertEquals(0, d.getRecordsFiltered());
	}

	/**
	 * Test with a broken result set (hits but no sub-hits).
	 * @throws Exception 
	 */
	@Test
	public void testBrokenResults_00() throws Exception {
		Hits<SyslogProxyQueryResultField> h = new Hits<SyslogProxyQueryResultField>();

		QueryResult<SyslogProxyQueryResultField> q = new QueryResult<SyslogProxyQueryResultField>();
		q.setHits(h);

		SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField> d = new SyslogReturnResult<>(
				q, SyslogProxyReturnResultData.class, SyslogProxyQueryResultField.class);

		Assert.assertEquals(0, d.getData().size());
		Assert.assertEquals(0, d.getRecordsTotal());
		Assert.assertEquals(0, d.getRecordsFiltered());
	}

	/**
	 * Test with broken result set (hits but no data in array).
	 * @throws Exception 
	 */
	@Test
	public void testBrokenResults_01() throws Exception {
		List<Hit<SyslogProxyQueryResultField>> hits = new ArrayList<Hit<SyslogProxyQueryResultField>>();
		hits.add(new Hit<SyslogProxyQueryResultField>());

		Hits<SyslogProxyQueryResultField> h = new Hits<SyslogProxyQueryResultField>();
		h.setHits(hits);

		QueryResult<SyslogProxyQueryResultField> q = new QueryResult<SyslogProxyQueryResultField>();
		q.setHits(h);

		SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField> d = new SyslogReturnResult<>(
				q, SyslogProxyReturnResultData.class, SyslogProxyQueryResultField.class);

		Assert.assertEquals(1, d.getData().size());
		Assert.assertEquals(0, d.getRecordsTotal());
		Assert.assertEquals(1, d.getRecordsFiltered());
	}
	
	/**
	 * Test invoked NullPointerException results
	 * <p>
	 * Exception when null pointer calls method
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testInvalidInstantiating_01() throws Exception {
		List<Hit<SyslogProxyQueryResultField>> hits = new ArrayList<Hit<SyslogProxyQueryResultField>>();
		hits.add(new Hit<SyslogProxyQueryResultField>());

		Hits<SyslogProxyQueryResultField> h = new Hits<SyslogProxyQueryResultField>();
		h.setHits(hits);

		QueryResult<SyslogProxyQueryResultField> q = new QueryResult<SyslogProxyQueryResultField>();
		q.setHits(h);

		new SyslogReturnResult<>(q, null, SyslogProxyQueryResultField.class);
	}
	
	/**
	 * Test invoked NoSuchMethodException results
	 * <p>
	 * Exception when creating non-existing constructor
	 * @throws Exception 
	 */
	@Test(expected=NoSuchMethodException.class)
	public void testInvalidInstantiating_02() throws Exception {
		List<Hit<SyslogProxyQueryResultField>> hits = new ArrayList<Hit<SyslogProxyQueryResultField>>();
		hits.add(new Hit<SyslogProxyQueryResultField>());

		Hits<SyslogProxyQueryResultField> h = new Hits<SyslogProxyQueryResultField>();
		h.setHits(hits);

		QueryResult<SyslogProxyQueryResultField> q = new QueryResult<SyslogProxyQueryResultField>();
		q.setHits(h);

		new SyslogReturnResult<>(q, SyslogProxyReturnResultData.class, null);
	}
	
	/**
	 * Test invoked Exception results
	 * <p>
	 * Exception
	 * @throws Exception 
	 */
	@Test(expected=Exception.class)
	public void testInvalidInstantiating_03() throws Exception {
		List<Hit<SyslogProxyQueryResultField>> hits = new ArrayList<Hit<SyslogProxyQueryResultField>>();
		hits.add(new Hit<SyslogProxyQueryResultField>());

		Hits<SyslogProxyQueryResultField> h = new Hits<SyslogProxyQueryResultField>();
		h.setHits(hits);

		QueryResult<SyslogProxyQueryResultField> q = new QueryResult<SyslogProxyQueryResultField>();
		q.setHits(h);

		new SyslogReturnResult<>(q, SyslogProxyReturnResultData.class, null);
	}

}


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


package hr.eito.model.elasticquery;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the ElasticQuery class
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class ElasticQueryTest {
		
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
	 * Test getting/setting properties
	 */
	@Test
	public void test() {
		Integer size = 5;
		IQueryClause query = new QueryLeafClause("wildcard", null);
		Aggs aggs = new Aggs();
		ElasticQuery elasticQuery = new ElasticQuery();
		elasticQuery.setSize(size);
		elasticQuery.setQuery(query);
		elasticQuery.setAggs(aggs);
		elasticQuery.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails("desc")
				)
		);
		elasticQuery.addSort(
				new SortSegment(
						"severity",
						new SortDetails("asc")
				)
		);
		elasticQuery.addSources("@timestamp", "port");
		elasticQuery.addSources("host");
		Assert.assertEquals(size, elasticQuery.getSize());
		Assert.assertEquals(query, elasticQuery.getQuery());
		Assert.assertEquals(aggs, elasticQuery.getAggs());
		Assert.assertEquals(2, elasticQuery.getSort().size());
		Assert.assertEquals(3, elasticQuery.getSource().size());
	}

}


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
 * Tests the AggsDetailsBuilder class
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class AggsDetailsBuilderTest {
		
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
	 * Test creating AggsDetails with AggsDetailsBuilder object
	 */
	@Test
	public void test() {
		String field = "field";
		Integer size = 10;
		AggsOrder order = new AggsOrder("desc");
		String interval = "interval";
		String format = "format";
		AggsDetails aggsDetails = new AggsDetailsBuilder()
				.setAggsOrder(order)
				.setField(field)
				.setSize(size)
				.setInterval(interval)
				.setFormat(format)
				.build();
		Assert.assertEquals(field, aggsDetails.getField());
		Assert.assertEquals(size, aggsDetails.getSize());
		Assert.assertEquals(order, aggsDetails.getAggsOrder());
		Assert.assertEquals(interval, aggsDetails.getInterval());
		Assert.assertEquals(format, aggsDetails.getFormat());
	}

}

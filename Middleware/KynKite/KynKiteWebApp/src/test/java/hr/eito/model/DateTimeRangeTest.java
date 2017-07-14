
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


package hr.eito.model;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the DateTimeRange class
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class DateTimeRangeTest {
		
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
	 * Test setting, getting
	 */
	@Test
	public void testSettingGetting() {
		Date dateTimeFrom = new Date();
		Date dateTimeTo = new Date();
		DateTimeRange dateTimeRange = new DateTimeRange();
		dateTimeRange.setDateTimeFrom(dateTimeFrom);
		dateTimeRange.setDateTimeTo(dateTimeTo);
		Assert.assertEquals(dateTimeFrom, dateTimeRange.getDateTimeFrom());
		Assert.assertEquals(dateTimeTo, dateTimeRange.getDateTimeTo());
	}
	
	/**
	 * Test isBroken when expected true
	 */
	@Test
	public void testBrokenYes() {
		Date dateTimeFrom = new Date();
		Date dateTimeTo = new Date();
		DateTimeRange dateTimeRange = new DateTimeRange();
		
		// DateTimeFrom set, dateTimeTo not set
		dateTimeRange.setDateTimeFrom(dateTimeFrom);
		dateTimeRange.setDateTimeTo(null);
		Assert.assertTrue(dateTimeRange.isBroken());
		
		// DateTimeFrom not set, dateTimeTo set
		dateTimeRange.setDateTimeFrom(null);
		dateTimeRange.setDateTimeTo(dateTimeTo);
		Assert.assertTrue(dateTimeRange.isBroken());
		
		// DateTimeFrom not set, dateTimeTo not set
		dateTimeRange.setDateTimeFrom(null);
		dateTimeRange.setDateTimeTo(null);
		Assert.assertTrue(dateTimeRange.isBroken());
	}
	
	/**
	 * Test isBroken when expected false
	 */
	@Test
	public void testBrokenNo() {
		Date dateTimeFrom = new Date();
		Date dateTimeTo = new Date();
		DateTimeRange dateTimeRange = new DateTimeRange();
		
		// DateTimeFrom set, dateTimeTo not set
		dateTimeRange.setDateTimeFrom(dateTimeFrom);
		dateTimeRange.setDateTimeTo(dateTimeTo);
		Assert.assertFalse(dateTimeRange.isBroken());
	}

}

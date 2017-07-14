
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


package hr.eito.model.reports;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import hr.eito.model.DateTimeRange;

/**
 * Tests the ReportsParams.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class ReportsParamsTest {
	
	/**
	 * Test setting and getting properties
	 */
	@Test
	public void test() {
		final String maxDestSyn = "5";
		final String maxDestIpPing = "6";
		DateTimeRange dtr = new DateTimeRange();
		
		ReportsParams params = new ReportsParams();
		params.setMaxDestSyn(maxDestSyn);
		params.setMaxDestIpPing(maxDestIpPing);
		params.setDateTimeRange(dtr);
		
		Assert.assertEquals(maxDestSyn, params.getMaxDestSyn());
		Assert.assertEquals(maxDestIpPing, params.getMaxDestIpPing());
		Assert.assertEquals(dtr, params.getDateTimeRange());
	}

}

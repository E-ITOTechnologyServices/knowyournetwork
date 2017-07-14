
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


package hr.eito.kynkite.rest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import hr.eito.model.JsonReturnData;
import hr.eito.model.reports.ReportsParams;
import hr.eito.model.reports.ReportsReturnResult;
import hr.eito.model.reports.ReportsSingleParams;

/**
 * Tests the ReportController RestController methods.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class ReportControllerTest {

	@Autowired
	ReportController controller;
	
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
	 * Check we have a controller.
	 */
	@Test
	public void testController() {
		Assert.assertNotNull(controller);
	}

	/**
	 * Test creating report Docker container API method
	 */
	@Test
	public void testCreateReportContainer() {
		ReportsParams params = new ReportsParams();
		JsonReturnData<String> result = controller.createReportContainer(params);
		
		Assert.assertFalse(result.isOK());
	}
	
	/**
	 * Test getting report by name API method
	 */
	@Test
	public void testGetReportByName() {
		ReportsSingleParams params = new ReportsSingleParams();
		ResponseEntity<ByteArrayResource> result = controller.getReportByName(params);
		
		Assert.assertNotNull(result.getBody());
	}
	
	/**
	 * Test getting list of reports from Docker volume API method
	 */
	@Test
	public void testGetReportList() {
		JsonReturnData<ReportsReturnResult> result = controller.getReportList();
		
		Assert.assertTrue(result.isOK());
	}
	
}

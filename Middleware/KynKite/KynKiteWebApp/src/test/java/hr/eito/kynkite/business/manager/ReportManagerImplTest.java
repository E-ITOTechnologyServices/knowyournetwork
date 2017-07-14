
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


package hr.eito.kynkite.business.manager;

import java.util.Calendar;
import java.util.GregorianCalendar;

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

import hr.eito.kynkite.utils.CustomError;
import hr.eito.model.DateTimeRange;
import hr.eito.model.JsonReturnData;
import hr.eito.model.reports.ReportsParams;
import hr.eito.model.reports.ReportsReturnResult;

/**
 * Tests the ReportManager.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class ReportManagerImplTest {
	
	@Autowired
	private ReportManager reportManager;

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
	 * Check we have a manager.
	 */
	@Test
	public void testLaraManager() {
		Assert.assertNotNull(reportManager);
	}
	
	/**
	 * Test when parameters are null
	 */
	@Test
	public void testParamsNull() {
		ReportsParams params = null;
		JsonReturnData<String> returnValue = reportManager.createReport(params);
		
		Assert.assertFalse(returnValue.isOK());
		Assert.assertEquals(CustomError.PARAMETERS_MISSING.getErrorMessage(), returnValue.getErrorMessage());
	}
	
	/**
	 * Test when datetimerange is null
	 */
	@Test
	public void testDateTimeRangeNull() {
		ReportsParams params = new ReportsParams();
		params.setDateTimeRange(null);
		JsonReturnData<String> returnValue = reportManager.createReport(params);
		
		Assert.assertFalse(returnValue.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), returnValue.getErrorMessage());
	}
	
	/**
	 * Test when datetimerange is broken
	 */
	@Test
	public void testDateTimeRangeBroken() {
		ReportsParams params = new ReportsParams();
		params.setDateTimeRange(new DateTimeRange());
		JsonReturnData<String> returnValue = reportManager.createReport(params);
		
		Assert.assertFalse(returnValue.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), returnValue.getErrorMessage());
	}
	
	/**
	 * Test when exists report for that week
	 */
	@Test
	public void testAnotherWeekReport() {
		ReportsParams params = new ReportsParams();
		
		// Set date time range from 2017/4/8 to 2017/4/15
		DateTimeRange dtr = new DateTimeRange();
		dtr.setDateTimeFrom(new GregorianCalendar(2017, Calendar.APRIL, 8, 0, 0, 0).getTime());
		dtr.setDateTimeTo(new GregorianCalendar(2017, Calendar.APRIL, 15, 0, 0, 0).getTime());
		params.setDateTimeRange(dtr);
		
		JsonReturnData<String> returnValue = reportManager.createReport(params);
		
		// Find a way to test report creation
		Assert.assertFalse(returnValue.isOK());
		Assert.assertEquals(CustomError.REPORT_ALREADY_EXISTS.getErrorMessage(), returnValue.getErrorMessage());
	}
	
	/**
	 * Test when report for that week not exists
	 */
	@Test
	public void testNewWeekReport() {
		ReportsParams params = new ReportsParams();
		
		// Set date time range from 2017/4/8 to 2017/4/15
		DateTimeRange dtr = new DateTimeRange();
		dtr.setDateTimeFrom(new GregorianCalendar(2017, Calendar.APRIL, 1, 0, 0, 0).getTime());
		dtr.setDateTimeTo(new GregorianCalendar(2017, Calendar.APRIL, 8, 0, 0, 0).getTime());
		params.setDateTimeRange(dtr);
		
		JsonReturnData<String> returnValue = reportManager.createReport(params);
		
		// Find a way to test report creation
		Assert.assertFalse(returnValue.isOK());
		Assert.assertEquals(CustomError.REPORT_NOT_CREATED.getErrorMessage(), returnValue.getErrorMessage());
	}
	
	/**
	 * Test getting report by name
	 */
	@Test
	public void testGettingReport() {
		ResponseEntity<ByteArrayResource> response = reportManager.getReportByName("report_20170408_20170415.pdf");
		
		Assert.assertNotNull(response);
	}
	
	/**
	 * Test getting list of report names
	 */
	@Test
	public void testGettingListOfReportNames() {
		JsonReturnData<ReportsReturnResult> response = reportManager.getReportList();
		
		Assert.assertTrue(response.isOK());
		Assert.assertEquals(1, response.getContent().getRecordsTotal());
		Assert.assertEquals(1, response.getContent().getRecordsFiltered());
	}
	
}

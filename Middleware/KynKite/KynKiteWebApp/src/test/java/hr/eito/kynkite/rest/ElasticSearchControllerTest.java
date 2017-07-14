
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import hr.eito.model.JsonReturnData;
import hr.eito.model.cmdb.CMDBParams;
import hr.eito.model.cmdb.CMDBReturnResult;
import hr.eito.model.hostreport.HostDetailsReportParams;
import hr.eito.model.hostreport.HostDetailsReportReturnResult;
import hr.eito.model.hostreport.HostReportParams;
import hr.eito.model.hostreport.HostReportReturnResult;
import hr.eito.model.newsticker.NewsTickerParams;
import hr.eito.model.newsticker.NewsTickerReturnResult;

/**
 * Tests the ElasticSearchController RestController methods.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class ElasticSearchControllerTest {

	@Autowired
	ElasticSearchController controller;
	
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
	 * TODO: Test method for filtering data by IP address API method
	 */
//	@Test
//	public void testLoadDataByIP() {
//		NetflowParams params = new NetflowParams();
//		params.setIpAddress("");
//		String result = controller.loadDataByIP(params);
//		
//		Assert.assertTrue(result.isOK());
//	}
	
	/**
	 * Test Newsticker API method
	 */
	@Test
	public void testNewsticker() {
		NewsTickerParams params = new NewsTickerParams();
		JsonReturnData<NewsTickerReturnResult> result = controller.newsticker(params);
		
		Assert.assertTrue(result.isOK());
	}
	
	/**
	 * Test Host report API method
	 */
	@Test
	public void testHostReport() {
		HostReportParams params = new HostReportParams();
		JsonReturnData<HostReportReturnResult> result = controller.hostReport(params);
		
		Assert.assertTrue(result.isOK());
	}
	
	/**
	 * Test Host details report API method
	 */
	@Test
	public void testHostDetailsReport() {
		HostDetailsReportParams params = new HostDetailsReportParams();
		JsonReturnData<HostDetailsReportReturnResult> result = controller.hostDetailsReport(params);
		
		Assert.assertFalse(result.isOK());
	}
	
	/**
	 * Test CMDB report API method
	 */
	@Test
	public void testCmdbReport() {
		CMDBParams params = new CMDBParams();
		JsonReturnData<CMDBReturnResult> result = controller.cmdbReport(params);
		
		Assert.assertFalse(result.isOK());
	}
	
}

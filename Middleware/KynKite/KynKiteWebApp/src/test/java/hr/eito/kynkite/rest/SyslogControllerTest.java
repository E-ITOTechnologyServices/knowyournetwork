
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
import hr.eito.model.syslog.SyslogASAReturnResult;
import hr.eito.model.syslog.SyslogDHCPReturnResult;
import hr.eito.model.syslog.SyslogIPSReturnResult;
import hr.eito.model.syslog.SyslogParams;
import hr.eito.model.syslog.SyslogProxyQueryResultField;
import hr.eito.model.syslog.SyslogProxyReturnResultData;
import hr.eito.model.syslog.SyslogReturnResult;
import hr.eito.model.syslog.SyslogRouterReturnResult;
import hr.eito.model.syslog.SyslogVoiceReturnResult;

/**
 * Tests the SyslogController RestController methods.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class SyslogControllerTest {

	@Autowired
	SyslogController controller;
	
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
	 * Testing Syslog ASA API method
	 */
	@Test
	public void testSyslogASA() {
		SyslogParams params = new SyslogParams();
		JsonReturnData<SyslogASAReturnResult> result = controller.syslogASA(params);
		
		Assert.assertTrue(result.isOK());
	}
	
	/**
	 * Testing Syslog Router API method
	 */
	@Test
	public void testSyslogRouter() {
		SyslogParams params = new SyslogParams();
		JsonReturnData<SyslogRouterReturnResult> result = controller.syslogRouter(params);
		
		Assert.assertTrue(result.isOK());
	}
	
	/**
	 * Testing Syslog Voice API method
	 */
	@Test
	public void testSyslogVoice() {
		SyslogParams params = new SyslogParams();
		JsonReturnData<SyslogVoiceReturnResult> result = controller.syslogVoice(params);
		
		Assert.assertTrue(result.isOK());
	}
	
	/**
	 * Testing Syslog DHCP API method
	 */
	@Test
	public void testSyslogDHCP() {
		SyslogParams params = new SyslogParams();
		JsonReturnData<SyslogDHCPReturnResult> result = controller.syslogDHCP(params);
		
		Assert.assertTrue(result.isOK());
	}
	
	/**
	 * Testing Syslog IPS API method
	 */
	@Test
	public void testSyslogIPS() {
		SyslogParams params = new SyslogParams();
		JsonReturnData<SyslogIPSReturnResult> result = controller.syslogIPS(params);
		
		Assert.assertTrue(result.isOK());
	}
	
	/**
	 * Testing Syslog Proxy API method
	 */
	@Test
	public void testSyslogProxy() {
		SyslogParams params = new SyslogParams();
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result = 
				controller.syslogProxy(params);
		
		Assert.assertTrue(result.isOK());
	}
	
}

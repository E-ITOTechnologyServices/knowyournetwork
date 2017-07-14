
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
import hr.eito.model.lara.LaraParams;
import hr.eito.model.lara.LaraPolicyInfoParams;
import hr.eito.model.lara.cisco.LaraCiscoACLReturnResult;
import hr.eito.model.lara.policies.LaraPolicyInfoReturnResult;
import hr.eito.model.lara.routers.LaraRoutersReturnResult;
import hr.eito.model.lara.rules.LaraRulesReturnResult;
import hr.eito.model.lara.rules.LaraTopHostsReturnResult;

/**
 * Tests the LaraManager.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class LaraManagerImplTest {
	
	@Autowired
	private LaraManager laraManager;

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
		Assert.assertNotNull(laraManager);
	}
	
	/**
	 * Test Lara Cisco ACL
	 */
	@Test
	public void testLaraCiscoACL() {
		LaraParams params = new LaraParams();
		params.setPolicyId("policyId");
		JsonReturnData<LaraCiscoACLReturnResult> result = laraManager.laraCisco(params);
		Assert.assertTrue(result.isOK());

		LaraCiscoACLReturnResult content = result.getContent();

		Assert.assertEquals(3, content.getRecordsFiltered());
		Assert.assertEquals(10, content.getRecordsTotal());
	}
	
	/**
	 * Test Lara Rules
	 */
	@Test
	public void testLaraRules() {
		LaraParams params = new LaraParams();
		params.setPolicyId("policyId");
		JsonReturnData<LaraRulesReturnResult> result = laraManager.laraRules(params);
		Assert.assertTrue(result.isOK());

		LaraRulesReturnResult content = result.getContent();

		Assert.assertEquals(5, content.getRecordsFiltered());
		Assert.assertEquals(5, content.getRecordsTotal());

		Assert.assertEquals(110097, content.getData().get(0).getHits());
		Assert.assertEquals(21, content.getData().get(0).getFlow().getSource().getMembers().size());
		Assert.assertEquals("192.168.12.0/23", content.getData().get(0).getFlow().getSource().getMembers().get(0).getIpAddress());
	}
	
	/**
	 * Test Lara Rules top sources for dynamic hosts
	 */
	@Test
	public void testLaraTopSrcDynamic() {
		LaraParams params = new LaraParams();
		params.setPolicyId("policyId");
		JsonReturnData<LaraTopHostsReturnResult> result = laraManager.laraTopSrcDynamic(params);
		Assert.assertTrue(result.isOK());

		LaraTopHostsReturnResult content = result.getContent();

		Assert.assertEquals(5, content.getRecordsFiltered());
		Assert.assertEquals(5, content.getRecordsTotal());

		Assert.assertEquals(1, content.getData().get(0).getRank());
		Assert.assertEquals("192.168.12.121", content.getData().get(0).getIpAddress());
		Assert.assertEquals(58272, content.getData().get(0).getHits());
		Assert.assertEquals("5565744", content.getData().get(0).getSize());
	}
	
	/**
	 * Test Lara Rules top destinations for dynamic hosts
	 */
	@Test
	public void testLaraTopDstDynamic() {
		LaraParams params = new LaraParams();
		params.setPolicyId("policyId");
		JsonReturnData<LaraTopHostsReturnResult> result = laraManager.laraTopDstDynamic(params);
		Assert.assertTrue(result.isOK());

		LaraTopHostsReturnResult content = result.getContent();

		Assert.assertEquals(5, content.getRecordsFiltered());
		Assert.assertEquals(5, content.getRecordsTotal());

		Assert.assertEquals(1, content.getData().get(0).getRank());
		Assert.assertEquals("8.8.8.8", content.getData().get(0).getIpAddress());
		Assert.assertEquals(33879, content.getData().get(0).getHits());
		Assert.assertEquals("2812876", content.getData().get(0).getSize());
	}
	
	/**
	 * Test Lara Rules top sources for static hosts
	 */
	@Test
	public void testLaraTopSrcStatic() {
		LaraParams params = new LaraParams();
		params.setPolicyId("policyId");
		JsonReturnData<LaraTopHostsReturnResult> result = laraManager.laraTopSrcStatic(params);
		Assert.assertTrue(result.isOK());

		LaraTopHostsReturnResult content = result.getContent();

		Assert.assertEquals(0, content.getRecordsFiltered());
		Assert.assertEquals(0, content.getRecordsTotal());

		Assert.assertEquals(0, content.getData().size());
	}
	
	/**
	 * Test Lara Rules top destinations for static hosts
	 */
	@Test
	public void testLaraTopDstStatic() {
		LaraParams params = new LaraParams();
		params.setPolicyId("policyId");
		JsonReturnData<LaraTopHostsReturnResult> result = laraManager.laraTopDstStatic(params);
		Assert.assertTrue(result.isOK());

		LaraTopHostsReturnResult content = result.getContent();

		Assert.assertEquals(0, content.getRecordsFiltered());
		Assert.assertEquals(0, content.getRecordsTotal());

		Assert.assertEquals(0, content.getData().size());
	}
	
	/**
	 * Test Lara list of routers
	 */
	@Test
	public void testLaraRouters() {
		JsonReturnData<LaraRoutersReturnResult> result = laraManager.getRouterList();
		Assert.assertTrue(result.isOK());

		LaraRoutersReturnResult content = result.getContent();

		Assert.assertEquals(1, content.getRecordsFiltered());
		Assert.assertEquals(1, content.getRecordsTotal());
		Assert.assertEquals(1, content.getData().size());
		Assert.assertEquals("192.168.9.1", content.getData().get(0).getRouterIP());
	}
	
	/**
	 * Test Lara list of policies for specific router
	 */
	@Test
	public void testLaraPoliciesList() {
		LaraPolicyInfoParams params = new LaraPolicyInfoParams();
		params.setRouterIpAddress("192.168.9.1");
		JsonReturnData<LaraPolicyInfoReturnResult> result = laraManager.getPolicyList(params);
		Assert.assertTrue(result.isOK());

		LaraPolicyInfoReturnResult content = result.getContent();

		Assert.assertEquals(1, content.getRecordsFiltered());
		Assert.assertEquals(1, content.getRecordsTotal());
		Assert.assertEquals(1, content.getData().size());
		Assert.assertEquals("AVnbFVPhISe-c8hencwD", content.getData().get(0).getId());
		Assert.assertEquals("2017-02-02T15:03:04.000Z", content.getData().get(0).getTimestamp());
	}
	
}

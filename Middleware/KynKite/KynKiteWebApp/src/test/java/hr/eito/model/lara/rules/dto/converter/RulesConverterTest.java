
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


package hr.eito.model.lara.rules.dto.converter;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import hr.eito.model.lara.rules.dto.Flow;
import hr.eito.model.lara.rules.dto.IpAddress;
import hr.eito.model.lara.rules.dto.IpFlowEntity;
import hr.eito.model.lara.rules.dto.Port;
import hr.eito.model.lara.rules.dto.PortFlowEntity;
import hr.eito.model.lara.rules.dto.Rules;
import hr.eito.model.lara.rules.dto.Ruleset;
import hr.eito.model.lara.rules.dto.RulesetDetails;

/**
 * Tests the RulesConverter.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class RulesConverterTest {
		
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
	 * Test getting input type
	 */
	@Test
	public void testGettingInputType() {
		RulesConverter rulesConverter = new RulesConverter();
		TypeFactory typeFactory = TypeFactory.defaultInstance();
		JavaType javaType = rulesConverter.getInputType(typeFactory);
		Assert.assertEquals(javaType, typeFactory.constructType(String.class));
	}
	
	/**
	 * Test getting output type
	 */
	@Test
	public void testGettingOutputType() {
		RulesConverter rulesConverter = new RulesConverter();
		TypeFactory typeFactory = TypeFactory.defaultInstance();
		JavaType javaType = rulesConverter.getOutputType(typeFactory);
		Assert.assertEquals(javaType, typeFactory.constructType(Rules.class));
	}

	/**
	 * Test successful conversion
	 * <p>Case with empty JSON String to convert
	 */
	@Test
	public void testConvert_01() {
		RulesConverter rulesConverter = new RulesConverter();
		String value = "{}";
		Rules rules = rulesConverter.convert(value);
		Assert.assertNotNull(rules);
		Assert.assertNull(rules.getRuleset());
	}
	
	/**
	 * Test successful conversion
	 * <p>Case with empty Ruleset JSON
	 */
	@Test
	public void testConvert_02() {
		String value = new StringBuilder()
				.append("{")
					.append("\"Ruleset\":{}")
				.append("}")
				.toString();
		RulesConverter rulesConverter = new RulesConverter();
		Rules rules = rulesConverter.convert(value);
		Ruleset ruleset = rules.getRuleset();
		Assert.assertNotNull(rules);
		Assert.assertNotNull(ruleset);
		Assert.assertNull(ruleset.getDynamicRuleset());
		Assert.assertNull(ruleset.getStaticRuleset());
	}
	
	/**
	 * Test successful conversion
	 * <p>Case with full data conversion
	 */
	@Test
	public void testConvert_03() {
		String value = "{\"Ruleset\":{\"StaticRuleset\":[],\"DynamicRuleset\":[{},"
				+ "{\"Flow\":{},\"Action\":\"permit\",\"Hits\":23845,\"Bytes\":69400000},"
				+ "{\"Flow\":{"
				+ "\"SRC\":{\"name\":\"g-network-4\",\"members\":[{\"Address\":\"192.168.12.96/27\",\"Mask\":\"255.255.255.224\"},{\"Address\":\"192.168.13.8\",\"Mask\":\"255.255.255.255\"}]},"
				+ "\"DST\":{\"name\":\"g-network-3\",\"members\":[]},"
				+ "\"Service\":{\"name\":\"g-service-2\",\"members\":[{}]}},"
				+ "\"Action\":\"permit\",\"Hits\":14869,\"Bytes\":2600000}]}}";
		RulesConverter rulesConverter = new RulesConverter();
		
		Rules rules = rulesConverter.convert(value);
		Assert.assertNotNull(rules);
		
		Ruleset ruleset = rules.getRuleset();
		Assert.assertNotNull(ruleset);
		
		List<RulesetDetails> staticRuleset = ruleset.getStaticRuleset();
		Assert.assertNotNull(staticRuleset);
		Assert.assertEquals(0, staticRuleset.size());
		
		List<RulesetDetails> dynamicRuleset = ruleset.getDynamicRuleset();
		Assert.assertNotNull(dynamicRuleset);
		Assert.assertEquals(3, dynamicRuleset.size());	
		
		RulesetDetails rulesetDetails1 = dynamicRuleset.get(0);
		Flow flow1 = rulesetDetails1.getFlow();
		String action1 = rulesetDetails1.getAction();
		Assert.assertNotNull(rulesetDetails1);
		Assert.assertNull(flow1);
		Assert.assertNull(action1);
		
		RulesetDetails rulesetDetails2 = dynamicRuleset.get(1);
		Flow flow2 = rulesetDetails2.getFlow();
		String action2 = rulesetDetails2.getAction();
		long hits2 = rulesetDetails2.getHits();
		long bytes2 = rulesetDetails2.getBytes();
		Assert.assertNotNull(rulesetDetails2);
		Assert.assertNotNull(flow2);
		Assert.assertEquals("permit", action2);
		Assert.assertEquals(23845, hits2);
		Assert.assertEquals(69400000, bytes2);
		
		RulesetDetails rulesetDetails3 = dynamicRuleset.get(2);
		Flow flow3 = rulesetDetails3.getFlow();
		String action3 = rulesetDetails3.getAction();
		long hits3 = rulesetDetails3.getHits();
		long bytes3 = rulesetDetails3.getBytes();
		Assert.assertNotNull(rulesetDetails3);
		Assert.assertNotNull(flow3);
		Assert.assertEquals("permit", action3);
		Assert.assertEquals(14869, hits3);
		Assert.assertEquals(2600000, bytes3);
		
		IpFlowEntity source2 = flow2.getSource();
		IpFlowEntity destination2 = flow2.getDestination();
		PortFlowEntity service2 = flow2.getService();
		Assert.assertNull(source2);
		Assert.assertNull(destination2);
		Assert.assertNull(service2);
		
		IpFlowEntity source3 = flow3.getSource();
		String source3name = source3.getName();
		List<IpAddress> source3members = source3.getMembers();
		Assert.assertEquals("g-network-4", source3name);
		Assert.assertEquals(2, source3members.size());
		Assert.assertEquals("192.168.12.96/27", source3members.get(0).getIpAddress());
		Assert.assertEquals("255.255.255.224", source3members.get(0).getMask());
		
		IpFlowEntity destination3 = flow3.getDestination();
		String destination3name = destination3.getName();
		List<IpAddress> destination3members = destination3.getMembers();
		Assert.assertEquals("g-network-3", destination3name);
		Assert.assertEquals(0, destination3members.size());
		
		PortFlowEntity service3 = flow3.getService();
		String service3name = service3.getName();
		List<Port> service3members = service3.getMembers();
		Assert.assertEquals("g-service-2", service3name);
		Assert.assertEquals(1, service3members.size());
		Assert.assertNull(service3members.get(0).getPort());
		Assert.assertNull(service3members.get(0).getProtocol());
	}
	
	/**
	 * Test unsuccessful conversion
	 * <p>Case with full data conversion of invalid input json
	 */
	@Test
	public void testConvert_04() {
		String value = "{\"Ruleset\":{\"StaticRuleset\":[],\"DynamicRuleset\":[{},"
				+ "{\"Flow\":{},\"Action\":\"permit\",\"Hits\":23845,\"Bytes\":69400000},"
				+ "{\"Flow\":{"
				+ "\"SRC\":{\"name\":\"g-network-4\",\"members\":\"0\":{\"Address\":\"192.168.12.96/27\",\"Mask\":\"255.255.255.224\"},\"1\":{\"Address\":\"192.168.13.8\",\"Mask\":\"255.255.255.255\"}},"
				+ "\"DST\":{\"name\":\"g-network-3\",\"members\":[]},"
				+ "\"Service\":{\"name\":\"g-service-2\",\"members\":[{}]}},"
				+ "\"Action\":\"permit\",\"Hits\":14869,\"Bytes\":2600000}]}}";
		RulesConverter rulesConverter = new RulesConverter();
		
		Rules rules = rulesConverter.convert(value);
		Assert.assertNull(rules);
	}

}

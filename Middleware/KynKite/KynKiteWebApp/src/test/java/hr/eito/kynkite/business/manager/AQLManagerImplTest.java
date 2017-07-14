
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

import hr.eito.kynkite.aql.model.AqlParams;
import hr.eito.kynkite.aql.model.dto.RulesetReturnResult;
import hr.eito.kynkite.utils.CustomError;
import hr.eito.model.JsonReturnData;

/**
 * Tests the AQLManagerImpl.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class AQLManagerImplTest {
	
	@Autowired
	private AQLManager aqlManager;

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
	public void testAqlManager() {
		Assert.assertNotNull(aqlManager);
	}
	
	/**
	 * Test getting aql rules list
	 */
	@Test
	public void testAqlRulesList() {
		JsonReturnData<RulesetReturnResult> returnResult = aqlManager.aqlRulesList();
		
		Assert.assertNotNull(returnResult.getContent());
		Assert.assertEquals(3, returnResult.getContent().getRecordsFiltered());
		Assert.assertEquals(3, returnResult.getContent().getRecordsTotal());
	}
	
	/**
	 * Test adding AQL rule
	 */
	@Test
	public void testAddAqlRule() {
		// Test adding empty rule
		AqlParams paramsEmptyRule = new AqlParams();
		paramsEmptyRule.setRule("");
		JsonReturnData<RulesetReturnResult> resultEmptyRule = aqlManager.addAqlRule(paramsEmptyRule);
		
		Assert.assertFalse(resultEmptyRule.isOK());
		Assert.assertEquals(CustomError.AQL_RULE_MISSING.getErrorMessage(), resultEmptyRule.getErrorMessage());
		
		// Test adding existing rule
		AqlParams paramsExistingRule = new AqlParams();
		paramsExistingRule.setRule(aqlManager.aqlRulesList().getContent().getData().get(0).getRule()); // Rule from first ruleset
		JsonReturnData<RulesetReturnResult> resultExistingRule = aqlManager.addAqlRule(paramsExistingRule);
		
		Assert.assertFalse(resultExistingRule.isOK());
		Assert.assertEquals(CustomError.AQL_RULE_ALREADY_EXISTS.getErrorMessage(), resultExistingRule.getErrorMessage());
		
		// Test correct insert
		String newRule = "New Rule";
		String newDescription = "Description for new rule";
		AqlParams paramsCorrectRule = new AqlParams();
		paramsCorrectRule.setRule(newRule);
		paramsCorrectRule.setDescription(newDescription);
		JsonReturnData<RulesetReturnResult> resultCorrectRule = aqlManager.addAqlRule(paramsCorrectRule);
		
		Assert.assertNotNull(resultCorrectRule.getContent());
		Assert.assertEquals(1, resultCorrectRule.getContent().getRecordsFiltered());
		Assert.assertEquals(1, resultCorrectRule.getContent().getRecordsTotal());
		Assert.assertEquals(new Integer(4), resultCorrectRule.getContent().getData().get(0).getId());
		Assert.assertEquals(newRule, resultCorrectRule.getContent().getData().get(0).getRule());
		Assert.assertEquals(newDescription, resultCorrectRule.getContent().getData().get(0).getDescription());
	}
	
	/**
	 * Test editing AQL rule
	 */
	@Test
	public void testEditAqlRule() {
		// Test editing with empty rule
		AqlParams paramsEmptyRule = new AqlParams();
		paramsEmptyRule.setRule("");
		JsonReturnData<RulesetReturnResult> resultEmptyRule = aqlManager.editAqlRule(paramsEmptyRule);
		
		Assert.assertFalse(resultEmptyRule.isOK());
		Assert.assertEquals(CustomError.AQL_RULE_MISSING.getErrorMessage(), resultEmptyRule.getErrorMessage());
		
		// Test editing with existing rule
		AqlParams paramsExistingRule = new AqlParams();
		paramsExistingRule.setRule(aqlManager.aqlRulesList().getContent().getData().get(0).getRule()); // Rule from first ruleset
		JsonReturnData<RulesetReturnResult> resultExistingRule = aqlManager.editAqlRule(paramsExistingRule);
		
		Assert.assertFalse(resultExistingRule.isOK());
		Assert.assertEquals(CustomError.AQL_RULE_ALREADY_EXISTS.getErrorMessage(), resultExistingRule.getErrorMessage());
		
		// Test editing missing rule
		AqlParams paramsMissingRule = new AqlParams();
		paramsMissingRule.setId(99);
		paramsMissingRule.setRule("some rule");
		JsonReturnData<RulesetReturnResult> resultMissingRule = aqlManager.editAqlRule(paramsMissingRule);
		
		Assert.assertFalse(resultMissingRule.isOK());
		Assert.assertEquals(CustomError.AQL_RULESET_MISSING.getErrorMessage(), resultMissingRule.getErrorMessage());
		
		// Test correct edit
		Integer id = 2;
		String rule = "Edit of rule 2";
		String description = "Edited description for rule 2";
		AqlParams paramsCorrectRule = new AqlParams();
		paramsCorrectRule.setId(id);
		paramsCorrectRule.setRule(rule);
		paramsCorrectRule.setDescription(description);
		JsonReturnData<RulesetReturnResult> resultCorrectRule = aqlManager.editAqlRule(paramsCorrectRule);
		
		Assert.assertNotNull(resultCorrectRule.getContent());
		Assert.assertEquals(1, resultCorrectRule.getContent().getRecordsFiltered());
		Assert.assertEquals(1, resultCorrectRule.getContent().getRecordsTotal());
		Assert.assertEquals(new Integer(2), resultCorrectRule.getContent().getData().get(0).getId());
		Assert.assertEquals(rule, resultCorrectRule.getContent().getData().get(0).getRule());
		Assert.assertEquals(description, resultCorrectRule.getContent().getData().get(0).getDescription());
	}
	
	/**
	 * Test deleting AQL rule
	 */
	@Test
	public void testDeleteAqlRule() {
		// Test deleting missing rule
		AqlParams paramsMissingRule = new AqlParams();
		paramsMissingRule.setId(99);
		JsonReturnData<String> resultMissingRule = aqlManager.deleteAqlRule(paramsMissingRule);
		
		Assert.assertFalse(resultMissingRule.isOK());
		Assert.assertEquals(CustomError.AQL_RULESET_MISSING.getErrorMessage(), resultMissingRule.getErrorMessage());

		// Test correct delete
		Integer id = 2;
		AqlParams paramsCorrectRule = new AqlParams();
		paramsCorrectRule.setId(id);
		JsonReturnData<String> resultCorrectRule = aqlManager.deleteAqlRule(paramsCorrectRule);
		
		Assert.assertTrue(resultCorrectRule.isOK());
	}
	
}

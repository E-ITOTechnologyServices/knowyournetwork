
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


package hr.eito.kynkite.aql.dao;

import java.util.List;

import hr.eito.kynkite.aql.model.Ruleset;

/**
 * DAO for Ruleset
 * @author Hrvoje
 *
 */
public interface RulesetDAO {
	
	/**
	 * Inserting Ruleset
	 * 
	 * @param ruleset
	 */
	void insertRuleset(final Ruleset ruleset);
	
	/**
	 * Updating Ruleset
	 * 
	 * @param ruleset
	 */
	void updateRuleset(final Ruleset ruleset);
	
	/**
	 * Deleting Ruleset
	 * 
	 * @param ruleset
	 */
	void deleteRuleset(final Ruleset ruleset);
	
	/**
	 * Getting the Ruleset by id
	 * 
	 * @param id of the Ruleset
	 * 
	 * @return Ruleset
	 */
	Ruleset getById(final Integer id);
	
	/**
	 * Getting all Ruleset records
	 * 
	 * @param username
	 * @return list of Ruleset records
	 */
	List<Ruleset> getAllRuleset();
	
	/**
	 * Getting the Ruleset by unique rule value
	 * 
	 * @param rule text of the rule
	 * 
	 * @return Ruleset
	 */
	Ruleset getRulesetByRule(final String rule);
	
	/**
	 * Getting the Ruleset by unique rule value and id is not sent id
	 * 
	 * @param rule text of the rule
	 * @param id of the rule which is not examined
	 * 
	 * @return Ruleset
	 */
	Ruleset getRulesetByRuleAndNotId(final String rule, final Integer id);

}

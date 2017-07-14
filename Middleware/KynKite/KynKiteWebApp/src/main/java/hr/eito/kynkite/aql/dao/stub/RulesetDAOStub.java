
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


package hr.eito.kynkite.aql.dao.stub;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import hr.eito.kynkite.aql.dao.RulesetDAO;
import hr.eito.kynkite.aql.model.Ruleset;

@Repository
@Profile({"test"})
public class RulesetDAOStub implements RulesetDAO {
	
	private List<Ruleset> repository;
	
	public RulesetDAOStub() {
		repository = new ArrayList<>();
		
		Ruleset r1 = new Ruleset();
		r1.setId(1);
		r1.setRule("Rule for ruleset 1");
		r1.setDescription("");
		
		Ruleset r2 = new Ruleset();
		r2.setId(2);
		r2.setRule("Rule for ruleset 2");
		r2.setDescription("Description for ruleset 2");
		
		Ruleset r3 = new Ruleset();
		r3.setId(3);
		r3.setRule("Rule for ruleset 3");
		r3.setDescription("");
		
		repository.add(r1);
		repository.add(r2);
		repository.add(r3);
	}
	
	@Override
	public List<Ruleset> getAllRuleset() {
		return this.repository;
	}

	@Override
	public Ruleset getRulesetByRule(String rule) {
		for(Ruleset r : repository) {
			if(StringUtils.equals(r.getRule(), rule)) {
				return r;
			}
		}
		return null;
	}
	
	@Override
	public Ruleset getRulesetByRuleAndNotId(String rule, Integer id) {
		for(Ruleset r : repository) {
			if(StringUtils.equals(r.getRule(), rule) && !r.getId().equals(id)) {
				return r;
			}
		}
		return null;
	}

	@Override
	public void insertRuleset(Ruleset ruleset) {
		ruleset.setId(this.nextval());
		repository.add(ruleset);
	}

	@Override
	public Ruleset getById(Integer id) {
		for(Ruleset r : repository) {
			if(r.getId().equals(id)) {
				return r;
			}
		}
		return null;
	}

	@Override
	public void updateRuleset(Ruleset ruleset) {
		for(Ruleset r : repository) {
			if(r.getId().equals(ruleset.getId())) {
				r.setRule(ruleset.getRule());
				r.setDescription(ruleset.getDescription());
			}
		}
	}

	@Override
	public void deleteRuleset(Ruleset ruleset) {
		Ruleset rulesetToRemove = null;
		for(Ruleset r : repository) {
			if(r.getId().equals(ruleset.getId())) {
				rulesetToRemove = r;
			}
		}
		if (rulesetToRemove!=null) {
			repository.remove(rulesetToRemove);
		}
	}
	
	/**
	 * Simulate nextval database method which returns next auto increment id
	 * 
	 * @return next autoincrement id
	 */
	private Integer nextval() {
		return repository.get(repository.size()-1).getId()+1;
	}
	
}

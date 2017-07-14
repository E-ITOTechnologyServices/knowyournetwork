
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


package hr.eito.kynkite.aql.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hr.eito.kynkite.aql.dao.RulesetDAO;
import hr.eito.kynkite.aql.model.Ruleset;

@Repository
@Transactional("aqlData")
@Profile({"dev","prod"})
public class RulesetDAOImpl implements RulesetDAO {
	
	@Autowired
	@Qualifier(value="sessionFactoryAQL")
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Override
	public List<Ruleset> getAllRuleset() {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select * from ruleset order by id")
				.addEntity(Ruleset.class);
		List<Ruleset> rulesets = query.list();
		session.flush();
		
		return rulesets;
	}

	@Override
	public Ruleset getRulesetByRule(String rule) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select * from ruleset where trim(rule) = :rule")
				.addEntity(Ruleset.class)
				.setParameter("rule", rule);
		Ruleset ruleset = (Ruleset) query.uniqueResult();
		session.flush();
		
		return ruleset;
	}
	
	@Override
	public Ruleset getRulesetByRuleAndNotId(String rule, Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select * from ruleset where trim(rule) = :rule and id <> :id")
				.addEntity(Ruleset.class)
				.setParameter("rule", rule)
				.setParameter("id", id);
		Ruleset ruleset = (Ruleset) query.uniqueResult();
		session.flush();
		
		return ruleset;
	}

	@Override
	public void insertRuleset(Ruleset ruleset) {
		Session session = sessionFactory.getCurrentSession();
		session.save(ruleset);
		session.flush();
	}

	@Override
	public Ruleset getById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Ruleset ruleset = (Ruleset) session.get(Ruleset.class, id);
        session.flush();

        return ruleset;
	}

	@Override
	public void updateRuleset(Ruleset ruleset) {
		Session session = sessionFactory.getCurrentSession();
		session.update(ruleset);
		session.flush();
	}

	@Override
	public void deleteRuleset(Ruleset ruleset) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(ruleset);
		session.flush();
	}
	
}

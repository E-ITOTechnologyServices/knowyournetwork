
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


package hr.eito.kynkite;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import hr.eito.model.Shards;

/**
 * 
 * @author Marko
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JsonData {
	
	@XmlElement(required = true)
	private int took;
	
	@XmlElement(required = true)
	private boolean timed_out;
	
	@XmlElement(required = true)
	private Shards _shards;
	
	@XmlElement(required = true)
	private Hits hits;
	
 	@XmlElement(required = true)
	private Aggregations aggregations;

 	
 	
	public int getTook() {
		return took;
	}

	public void setTook(int took) {
		this.took = took;
	}

	public boolean isTimed_out() {
		return timed_out;
	}

	public void setTimed_out(boolean timed_out) {
		this.timed_out = timed_out;
	}

	public Shards get_shards() {
		return _shards;
	}

	public void set_shards(Shards _shards) {
		this._shards = _shards;
	}

	public Hits getHits() {
		return hits;
	}

	public void setHits(Hits hits) {
		this.hits = hits;
	}

	public Aggregations getAggregations() {
		return aggregations;
	}

	public void setAggregations(Aggregations aggregations) {
		this.aggregations = aggregations;
	}

}

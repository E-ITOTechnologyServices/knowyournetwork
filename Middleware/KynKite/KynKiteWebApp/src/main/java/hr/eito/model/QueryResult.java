
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


package hr.eito.model;

import hr.eito.model.Shards;
import hr.eito.model.Hits;

/**
 * Represents an entire result set returned from a query in ElasticSearch.
 * <p>
 * The class is templated on the specific result container (hit), otherwise,
 * fields contained here are common to all queries.
 *
 * @author Steve Chaplin
 *
 */
public class QueryResult<T> {
	
	private int took_;
	private boolean timed_out_;
	private Shards shards_;
	private Hits<T> hits_;
	
	/**
	 * @param took the took to set
	 */
	public void setTook(final int took) {
		took_ = took;
	}

	/**
	 * @param timed_out the timed_out to set
	 */
	public void setTimed_out(final boolean timed_out) {
		timed_out_ = timed_out;
	}

	/**
	 * @param shards the shards to set
	 */
	public void set_shards(final Shards shards) {
		shards_ = shards;
	}

	/**
	 * Get the hits payload.
	 *
	 * @return the hits
	 */
	public Hits<T> getHits() {
		return hits_;
	}

	/**
	 * Set the hits payload.
	 *
	 * @param hits the hits to set
	 */
	public void setHits(final Hits<T> hits) {
		hits_ = hits;
	}
}

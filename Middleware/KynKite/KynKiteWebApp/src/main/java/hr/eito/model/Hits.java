
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

import java.util.List;

import hr.eito.model.Hit;

/**
 * @author Steve Chaplin
 *
 */
public class Hits<T> {
	private int _total;
	private int _maxScore;

	private List<Hit<T>> _hits;

	/**
	 * @return The total.
	 */
	public int getTotal() {
		return _total;
	}

	/**
	 * @param total Set the total.
	 */
	public void setTotal(final int total) {
		_total = total;
	}

	/**
	 * @param maxScore The maximum score.
	 */
	public void setMax_score(final int maxScore) {
		_maxScore = maxScore;
	}

	/**
	 * @return The hits.
	 */
	public List<Hit<T>> getHits() {
		return _hits;
	}

	/**
	 * @param hits The hits to set.
	 */
	public void setHits(List<Hit<T>> hits) {
		_hits = hits;
	}
}


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


/*
 *
 */
package hr.eito.model.newsticker;

import hr.eito.model.Params;

/**
 * Encapsulates the query parameters for newsticker queries.
 * <p>
 * The query size is handled as a native int since it means nothing to
 * have a null size.
 *
 * @author Steve Chaplin
 */
public class NewsTickerParams extends Params {
	private int size_ = 5;
	private String query_;

	/**
	 * Place a limit on the number of records the query should return.
	 *
	 * @param size  The maximum number of records the query should return.
	 */
	public void setSize(final int size) {
		size_ = size;
	}

	/**
	 * The maximum number of records the caller requested should be returned.
	 * <p>
	 * If the caller has attempted to set a negative size, this is considered to
	 * be the same as zero.
	 *
	 * @return the maximum number of records to be returned.
	 */
	public int getSize() {
		if (size_ < 0) return 0;

		return size_;
	}

	/**
	 * Specify the search parameter.
	 * <p>
	 * As far as the newsticker query is concerned this parameter is searched for
	 * in the _all field so it is essentially a search for anything the caller
	 * cares to specify in this field.
	 */
	public void setQuery(final String query) {
		query_ = query;
	}

	/**
	 * Find out the user's specified query parameter.
	 */
	public String getQuery() {
		return query_;
	}
}

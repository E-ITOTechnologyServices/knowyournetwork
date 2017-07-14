
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

/**
 * Contains the shard information returned in an ElasticSeach query.
 *
 * @author Steve Chaplin
 *
 */
public class Shards {
	private int total_;
	private int successful_;
	private int failed_;

	/**
	 * @param total Records the total number of shards accessed during the query.
	 */
	public void setTotal(final int total) {
		total_ = total;
	}

	/**
	 * @param successful Records the number of shards accessed successfully during the query.
	 */
	public void setSuccessful(final int successful) {
		successful_ = successful;
	}

	/**
	 * @param failed Records the number of shards that failed to return results.
	 */
	public void setFailed(final int failed) {
		failed_ = failed;
	}
}

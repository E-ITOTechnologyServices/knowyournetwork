
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

/**
 * Represents a result returned by ElasticSearch.
 * <p>
 * This is intended to map to a single ElasticSearch result hit.
 * The templated type contains the data specific to the query executed. In
 * addition to this, the ElasticSearch meta-data associated with the query
 * hit is also returned.
 *
 * @author Steve Chaplin
 */
public class Hit<T> {
	
	private String _index;
	private String _type;
	private String _id;
	private int _score;
	private T _source;
	private List<Long> _sort;
	
	/**
	 * @param index the name of the ElasticSearch index
	 */
	public void set_index(final String index) {
		_index = index;
	}

	/**
	 * @param type the type of the record
	 */
	public void set_type(final String type) {
		_type = type;
	}

	/**
	 * @return the identity of the returned record
	 */
	public String get_id() {
		return _id;
	}

	/**
	 * @param id the identity of the returned record
	 */
	public void set_id(final String id) {
		_id = id;
	}

	/**
	 * @param score the record score
	 */
	public void set_score(final int score) {
		this._score = score;
	}

	/**
	 * @return result data for the query
	 */
	public T getData() {
		return _source;
	}

	/**
	 * @param source result data for the query
	 */
	public void set_source(final T source) {
		_source = source;
	}

	/**
	 * @param fields fields data returned by some ElasticSearch queries
	 */
	public void setFields(final T fields) {
		_source = fields;
	}

	/**
	 * @param sort order of the record
	 */
	public void setSort(final List<Long> sort) {
		_sort = sort;
	}
}

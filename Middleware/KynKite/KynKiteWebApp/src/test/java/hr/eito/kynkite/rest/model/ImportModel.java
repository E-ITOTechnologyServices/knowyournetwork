
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


/**
 * 
 */
package hr.eito.kynkite.rest.model;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * @author Danijel Soltic
 *
 */
public class ImportModel {

	private String _index;
	private String _type;
	private String _id;
	private int _score;
	@JsonRawValue
	private Object _source;
	/**
	 * @return the _index
	 */
	public String get_index() {
		return _index;
	}
	/**
	 * @param _index the _index to set
	 */
	public void set_index(String _index) {
		this._index = _index;
	}
	/**
	 * @return the _type
	 */
	public String get_type() {
		return _type;
	}
	/**
	 * @param _type the _type to set
	 */
	public void set_type(String _type) {
		this._type = _type;
	}
	/**
	 * @return the _id
	 */
	public String get_id() {
		return _id;
	}
	/**
	 * @param _id the _id to set
	 */
	public void set_id(String _id) {
		this._id = _id;
	}
	/**
	 * @return the _score
	 */
	public int get_score() {
		return _score;
	}
	/**
	 * @param _score the _score to set
	 */
	public void set_score(int _score) {
		this._score = _score;
	}
	/**
	 * @return the _source
	 */
	public Object get_source() {
		return _source;
	}
	/**
	 * @param _source the _source to set
	 */
	public void set_source(Object _source) {
		this._source = _source;
	}
	
}

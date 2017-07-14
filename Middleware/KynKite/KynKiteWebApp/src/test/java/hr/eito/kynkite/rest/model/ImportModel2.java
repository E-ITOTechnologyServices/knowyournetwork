
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

/**
 * @author Danijel Soltic
 *
 */
public class ImportModel2 {

	private Index index;

	/**
	 * @return the index
	 */
	public Index getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(Index index) {
		this.index = index;
	}
	
	public String get_index() {
		return this.index.get_index();
	}
	public String get_type() {
		return this.index.get_type();
	}
	
}
class Index{
	private String _index;
	private String _type;
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
	
}

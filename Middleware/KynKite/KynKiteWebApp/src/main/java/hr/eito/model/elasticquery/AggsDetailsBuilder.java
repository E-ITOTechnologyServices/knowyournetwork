
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


package hr.eito.model.elasticquery;

/**
 * Builder pattern class to build AggsDetails object
 * <p>Makes it easy to create AggsDetails and to maintain
 *
 * @author Hrvoje
 */
public class AggsDetailsBuilder {
	
	private AggsDetails aggsDetails;
	
	/**
	 * Creating Builder and AggsDetails instance
	 */
	public AggsDetailsBuilder() {
		this.aggsDetails = new AggsDetails();
	}
	
	/**
	 * Setting the field property of AggsDetails
	 * 
	 * @param field to send to AggsDetails
	 * @return AgssDetailsBuilder current instance
	 */
	public AggsDetailsBuilder setField(String field) {
		this.aggsDetails.setField(field);
		return this;
	}
	
	/**
	 * Setting the size property of AggsDetails
	 * 
	 * @param size to send to AggsDetails
	 * @return AgssDetailsBuilder current instance
	 */
	public AggsDetailsBuilder setSize(Integer size) {
		this.aggsDetails.setSize(size);
		return this;
	}
	
	/**
	 * Setting the AggsOrder property of AggsDetails
	 * 
	 * @param aggsOrder to send to AggsDetails
	 * @return AgssDetailsBuilder current instance
	 */
	public AggsDetailsBuilder setAggsOrder(AggsOrder aggsOrder) {
		this.aggsDetails.setAggsOrder(aggsOrder);
		return this;
	}
	
	/**
	 * Setting the interval property of AggsDetails
	 * 
	 * @param interval to send to AggsDetails
	 * @return AgssDetailsBuilder current instance
	 */
	public AggsDetailsBuilder setInterval(String interval) {
		this.aggsDetails.setInterval(interval);
		return this;
	}
	
	/**
	 * Setting the format property of AggsDetails
	 * 
	 * @param format to send to AggsDetails
	 * @return AgssDetailsBuilder current instance
	 */
	public AggsDetailsBuilder setFormat(String format) {
		this.aggsDetails.setFormat(format);
		return this;
	}
	
	/**
	 * @return instance of created AggsDetails
	 */
	public AggsDetails build() {
		return this.aggsDetails;
	}
	
}

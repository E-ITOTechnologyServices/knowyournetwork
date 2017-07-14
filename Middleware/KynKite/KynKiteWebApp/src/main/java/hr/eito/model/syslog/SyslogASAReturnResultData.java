
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
package hr.eito.model.syslog;

import hr.eito.model.DateSerializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Danijel Soltic
 *
 */
public class SyslogASAReturnResultData {
	
	private String message;
	private String asa_fw;
	@JsonProperty("@timestamp")
	private String timestamp;

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(final String message) {
		this.message = message;
	}
	/**
	 * @return the asa_fw
	 */
	public String getAsa_fw() {
		return asa_fw;
	}
	/**
	 * @param asa_fw the asa_fw to set
	 */
	public void setAsa_fw(final String asa_fw) {
		this.asa_fw = asa_fw;
	}
	/**
	 * @return the timestamp
	 */
	@JsonSerialize(using = DateSerializer.class)
	public String getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(final String timestamp) {
		this.timestamp = timestamp;
	}
}

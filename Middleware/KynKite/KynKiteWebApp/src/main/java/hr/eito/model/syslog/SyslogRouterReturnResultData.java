
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
public class SyslogRouterReturnResultData {

	@JsonProperty("router")
	private String router;
	@JsonProperty("@timestamp")
	private String timestamp;
	@JsonProperty("cisco_message")
	private String cisco_message;
	
	public SyslogRouterReturnResultData(){
		super();
	}

	/**
	 * @return the router
	 */
	public String getRouter() {
		return router;
	}

	/**
	 * @param router the router to set
	 */
	public void setRouter(final String router) {
		this.router = router;
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
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the cisco_message
	 */
	public String getCisco_message() {
		return cisco_message;
	}

	/**
	 * @param cisco_message the cisco_message to set
	 */
	public void setCisco_message(final String cisco_message) {
		this.cisco_message = cisco_message;
	}
}


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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Marko
 *
 */
public class SyslogDHCPQueryResultField {
	
	private String message;
	@JsonProperty("@timestamp")
	private String timestamp;
	@JsonProperty("AD-Server")
	private String ad_server;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(final String message) {
		this.message = message;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(final String timestamp) {
		this.timestamp = timestamp;
	}
	public String getAd_server() {
		return ad_server;
	}
	public void setAd_server(final String ad_server) {
		this.ad_server = ad_server;
	}
}

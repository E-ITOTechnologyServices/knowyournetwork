
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
 * @author Marko
 *
 */
public class SyslogDHCPReturnResultData {

	private String message;
	@JsonProperty("AD-Server")
	private String ad_Server;
	@JsonProperty("@timestamp")
	private String timestamp;
	
	public SyslogDHCPReturnResultData(SyslogDHCPQueryResultField field) {

		if (null != field) {
			this.timestamp = field.getTimestamp();
			this.message = field.getMessage();
			this.ad_Server = field.getAd_server();
		}
	}

	public String getMessage() {
		return message;
	}

	public String getAd_Server() {
		return ad_Server;
	}

	@JsonSerialize(using = DateSerializer.class)
	public String getTimestamp() {
		return timestamp;
	}
}

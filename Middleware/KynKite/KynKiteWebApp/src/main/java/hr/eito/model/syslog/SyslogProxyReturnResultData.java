
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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import hr.eito.model.DateSerializer;

/**
 * @author Hrvoje
 *
 */
public class SyslogProxyReturnResultData {
	
	@JsonProperty("@timestamp")
	private String timestamp;
	@JsonProperty("c_ip")
	private String source;	
	@JsonProperty("destinationIP")
	private String destination;
	@JsonProperty("cs_uri_port")
	private String port;
	@JsonProperty("cs_host")
	private String host;
	@JsonProperty("cs_uri_path")
	private String uri;
	@JsonProperty("sc_status")
	private String scStatus;
	@JsonProperty("user_agent_os")
	private String userAgentOs;
	@JsonProperty("user_agent_name")
	private String userAgentName;
	@JsonProperty("user_agent_major")
	private String userAgentMajor;
	
	public SyslogProxyReturnResultData(final SyslogProxyQueryResultField field) {
		if (null != field) {
			timestamp = field.getTimestamp();
			source = field.getSource();
			destination = field.getDestination();
			port = field.getPort();
			host = field.getHost();
			uri = field.getUri();
			scStatus = field.getScStatus();
			userAgentOs = field.getUserAgentOs();
			userAgentName = field.getUserAgentName();
			userAgentMajor = field.getUserAgentMajor();
		}
	}
	@JsonSerialize(using = DateSerializer.class)
	public String getTimestamp() {
		return timestamp;
	}
	public String getSource() {
		return source;
	}
	public String getDestination() {
		return destination;
	}
	public String getPort() {
		return port;
	}
	public String getHost() {
		return host;
	}
	public String getUri() {
		return uri;
	}
	public String getScStatus() {
		return scStatus;
	}
	public String getUserAgentOs() {
		return userAgentOs;
	}
	public String getUserAgentName() {
		return userAgentName;
	}
	public String getUserAgentMajor() {
		return userAgentMajor;
	}

}

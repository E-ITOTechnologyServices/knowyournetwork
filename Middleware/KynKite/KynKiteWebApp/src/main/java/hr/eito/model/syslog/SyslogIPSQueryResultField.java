
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
public class SyslogIPSQueryResultField {
	
	@JsonProperty("Destination")
	private String destination;
	@JsonProperty("@timestamp")
	private String timestamp;
	@JsonProperty("IPS_System")
	private String ips_system;
	private String log;
	@JsonProperty("Source")
	private String source;
	@JsonProperty("Destinationport")
	private String destinationport;
	
	
	public String getDestination() {
		return destination;
	}
	public void setDestination(final String destination) {
		this.destination = destination;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(final String timestamp) {
		this.timestamp = timestamp;
	}
	public String getIps_system() {
		return ips_system;
	}
	public void setIps_system(final String ips_system) {
		this.ips_system = ips_system;
	}
	public String getLog() {
		return log;
	}
	public void setLog(final String log) {
		this.log = log;
	}
	public String getSource() {
		return source;
	}
	public void setSource(final String source) {
		this.source = source;
	}
	public String getDestinationport() {
		return destinationport;
	}
	public void setDestinationport(final String destinationport) {
		this.destinationport = destinationport;
	}
}

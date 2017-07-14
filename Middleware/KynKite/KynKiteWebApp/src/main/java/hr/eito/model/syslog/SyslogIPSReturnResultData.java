
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


package hr.eito.model.syslog;

import hr.eito.model.DateSerializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Marko
 */
public class SyslogIPSReturnResultData {

	@JsonProperty("Destination")
	private String destination;
	@JsonProperty("@timestamp")
	private String timestamp;
	@JsonProperty("IPS_System")
	private String ips_system;
	@JsonProperty("log")
	private String log;
	@JsonProperty("Source")
	private String source;
	@JsonProperty("Destinationport")
	private String destinationport;
	
	/**
	 * Construct from a result field.
	 *
	 * @param field the result field from which to take the payload.
	 */
	public SyslogIPSReturnResultData(final SyslogIPSQueryResultField field){

		if (null != field) {
			destination = field.getDestination();
			timestamp = field.getTimestamp();
			ips_system = field.getIps_system();
			log = field.getLog();
			source = field.getSource();
			destinationport = field.getDestinationport();
		}
	}

	/**
	 * Get the result destination.
	 *
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * Get the result timestamp.
	 *
	 * @return the result timestamp
	 */
	@JsonSerialize(using = DateSerializer.class)
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the result IPS system.
	 *
	 * @return the IPS system
	 */
	public String getIps_system() {
		return ips_system;
	}

	/**
	 * Get the log entry.
	 *
	 * @return the log entry
	 */
	public String getLog() {
		return log;
	}

	/**
	 * Get the source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Get the destination port.
	 *
	 * @return the destination port
	 */

	public String getDestinationport() {
		return destinationport;
	}
}


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
 * @author Danijel Soltic
 *
 */
public class SyslogVoiceQueryResultField {
	private String voicegw;
	private String shortmessage;
	@JsonProperty("@timestamp")
	private String timestamp;
	/**
	 * @return the voicegw
	 */
	public String getVoicegw() {
		return voicegw;
	}
	/**
	 * @param voicegw the voicegw to set
	 */
	public void setVoicegw(final String voicegw) {
		this.voicegw = voicegw;
	}
	/**
	 * @return the shortmessage
	 */
	public String getShortmessage() {
		return shortmessage;
	}
	/**
	 * @param shortmessage the shortmessage to set
	 */
	public void setShortmessage(final String shortmessage) {
		this.shortmessage = shortmessage;
	}
	/**
	 * @return the timestamp
	 */
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

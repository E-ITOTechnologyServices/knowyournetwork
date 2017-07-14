
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


package hr.eito.model.hostreport;

import hr.eito.model.DateSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Encapsulates a row of data in the host report. This is event and severity informaton
 * for a specfic host.
 *
 * @author Steve Chaplin
 *
 */
public class HostReportReturnResultData {

	// Subsequent entries in the events list are delimited from the previous.
	private final static String DELIMITER_ = ", ";

	private String host_ = "";
	private int severity_ = 0;
	private String events_ = "";
	private String firstDetection_ = "";
	private String lastDetection_ = "";
	
	/**
	 * Add an event to the list of events in this record and increase the severity.
	 * Events are appended to a comma delimited list. Additional severities are
	 * accumulated.
	 *
	 * @param event the event to add
	 * @param severity the severity of this event
	 */
	public void addEvent(final String event, final int severity){

		// Only observe this event and severity if they have not been seen before.
		if(! events_.contains(event)){
			appendEvent_(event);
			severity_ += severity;
		}
	}
	
	/**
	 * Get the host for this record. If it has not been set, it will be the empty string.
	 *
	 * @return the host
	 */
	public String getHost() {
		return host_;
	}

	/**
	 * Set the host for this record. It should be an IP address or hostname.
	 *
	 * @param host the host to set
	 */
	public void setHost(final String host) {
		host_ = host;
	}

	/**
	 * Get the accumlated severity for this record. If no severity has been set, this will
	 * be zero.
	 *
	 * @return the severity
	 */
	public int getSeverity() {
		return severity_;
	}

	/**
	 * Get the list of events for this record. This is a comma delimited list of events that
	 * have been added.
	 *
	 * @return the events
	 */
	public String getEvents() {
		return events_;
	}

	/**
	 * Get the timestamp of first detection for this host. This is in UTC to the nearest minute.
	 *
	 * @return time of first detection of an event
	 */
	@JsonSerialize(using = DateSerializer.class)
	public String getFirstDetection() {
		return firstDetection_;
	}

	/**
	 * Set the first detection timestamp for this host. This should be in ISO8601 timestamp
	 * format.
	 *
	 * @param firstDetection the firstDetection timestamp
	 */
	public void setFirstDetection(final String firstDetection) {
		firstDetection_ = firstDetection;
	}

	/**
	 * Get the timestamp of last detection for this host. This is in UTC to the nearest minute.
	 *
	 * @return time of last detection of an event
	 */
	@JsonSerialize(using = DateSerializer.class)
	public String getLastDetection() {
		return lastDetection_;
	}
	/**
	 * Set the last detection timestamp for this host. This should be in ISO8601 timestamp
	 * format.
	 *
	 * @param lastDetection the lastDetection timestamp
	 */
	public void setLastDetection(final String lastDetection) {
		lastDetection_ = lastDetection;
	}

	/**
	 * Append an event to the end of our list of events.
	 *
	 * @param event the event text to add
	 */
	private void appendEvent_(final String event) {
		if (0 == events_.length()) {
			events_ = event;
		}
		else {
			events_ += DELIMITER_ + event;
		}
	}
}

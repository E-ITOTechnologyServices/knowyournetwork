
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
 * Encapsulates a row of data in the host details report. This is timestamp and description
 * information for a specific host.
 *
 * @author Steve Chaplin
 *
 */
public class HostDetailsReportReturnResultData {

	private String eventType_ = "";
	private int count_ = 0;
	private String firstDetection_ = "";
	private String lastDetection_ = "";
	private String description_ = "";
	private String elkLink_ = "";
	private int dateOffset_ = 15;
	
	/**
	 * Set the event type
	 *
	 * @param eventType the event to set
	 */
	public void setEventType(final String eventType) {
		eventType_ = eventType;
	}
	
	/**
	 * Get the event type for this record. If it has not been set, it will be the empty string.
	 *
	 * @return the event type
	 */
	public String getEventType() {
		return eventType_;
	}

	/**
	 * Set the count of the number of this event type.
	 *
	 * @param count the count of this event type
	 */
	public void setCount(final int count) {
		count_ = count;
	}
	
	/**
	 * Get the count of the number of events. For legacy reasons, this is known as 'number'.
	 *
	 * @return the count of the number of this event type.
	 */
	public int getNumber() {
		return count_;
	}

	/**
	 * Get the timestamp of first detection of this event type. This is in UTC to the nearest minute.
	 *
	 * @return time of first detection time of this event type
	 */
	@JsonSerialize(using = DateSerializer.class)
	public String getFirstDetection() {
		return firstDetection_;
	}

	/**
	 * Set the first detection timestamp of this event type. This should be in ISO8601 timestamp
	 * format.
	 *
	 * @param firstDetection the first detection timestamp
	 */
	public void setFirstDetection(final String firstDetection) {
		firstDetection_ = firstDetection;
	}
	/**
	 * Get the timestamp of last detection of this event type. This is in UTC to the nearest minute.
	 *
	 * @return time of last detection of an event
	 */
	@JsonSerialize(using = DateSerializer.class)
	public String getLastDetection() {
		return lastDetection_;
	}

	/**
	 * Set the last detection timestamp of this event type. This should be in ISO8601 timestamp
	 * format.
	 *
	 * @param lastDetection the lastDetection timestamp
	 */
	public void setLastDetection(final String lastDetection) {
		lastDetection_ = lastDetection;
	}

	/**
	 * Set the description of this event type.
	 *
	 * @param description the description of this event type
	 */
	public void setDescription(final String description) {
		description_ = description;
	}

	/**
	 * Get the description of this event type.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description_;
	}

	/**
	 * Set the ELK link of this event type. This is a hyperling that decorates the description and
	 * links to Kibana.
	 *
	 * @param link the link text
	 */
	public void setLink(final String link) {
		elkLink_ = link;
	}

	/**
	 * Get the link to decorate the description.
	 *
	 * @return the link
	 */
	public String getLink() {
		return elkLink_;
	}

	/**
	 * Set the date offset. This is a number of minutes each side of the first and last detection
	 * times that should be added as padding before dispatching the ELK link.
	 *
	 * @param offset the offset of this event type
	 */
	public void setDateOffset(final int offset) {
		dateOffset_ = offset;
	}
	
	/**
	 * Get the date offset. This is a number of minutes each side of the first and last detection
	 * times that should be added as padding before dispatching the ELK link.
	 *
	 * @return the date offset
	 */
	public int getDateOffset() {
		return dateOffset_;
	}
}

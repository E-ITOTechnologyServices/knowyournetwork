
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
package hr.eito.model.hostreport;

/**
 * @author Danijel Soltic
 *
 */
public class EventDescription {
	private String use_case;
	private String description;
	private int severity;
	private String eventid;
	private String link_;
	private int offset_ = 15;

	/**
	 * @return the use_case
	 */
	public String getUse_case() {
		return use_case;
	}
	/**
	 * @param use_case the use_case to set
	 */
	public void setUse_case(String use_case) {
		this.use_case = use_case;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the severity
	 */
	public int getSeverity() {
		return severity;
	}
	/**
	 * @param severity the severity to set
	 */
	public void setSeverity(int severity) {
		this.severity = severity;
	}

	/**
	 * @param eventid the eventid to set
	 */
	public void setEventid(String eventid) {
		this.eventid = eventid;
	}
	
	/**
	 * Set the elk_link parameter.
	 *
	 * @param link the link to set
	 */
	public void setElk_link(final String link) {
		link_ = link;
	}

	/**
	 * Get the link parameter.
	 *
	 * @return the link
	 */
	public String getLink() {
		return link_;
	}

	/**
	 * Set the number of minutes either side of the event timestamp that
	 * should be queried via the ELK link.
	 *
	 * @param offset the date offset
	 */
	public void setDate_offset(final int offset) {
		offset_ = offset;
	}

	/**
	 * Get the number of minutes either side of the event timestamp that
	 * should be queried via the ELK link.
	 *
	 * @return the offset
	 */
	public int getDateOffset() {
		return offset_;
	}
}

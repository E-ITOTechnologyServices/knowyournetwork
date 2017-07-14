
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


package hr.eito.model;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Date and time range as parameter
 * <p>
 * Encapsulates date and time range as parameter from FrontEnd
 *
 * @author Hrvoje
 */
public class DateTimeRange {
	
	@JsonDeserialize(using = DateDeserializer.class)
	private Date dateTimeFrom;
	@JsonDeserialize(using = DateDeserializer.class)
	private Date dateTimeTo;
	
	public boolean isBroken() {
		if (this.dateTimeFrom==null && this.dateTimeTo!=null || 
				this.dateTimeFrom!=null && this.dateTimeTo==null || 
				this.dateTimeFrom==null && this.dateTimeTo==null) {
			return true;
		}
		return false;
	}
	public Date getDateTimeFrom() {
		return dateTimeFrom;
	}
	public void setDateTimeFrom(Date dateTimeFrom) {
		this.dateTimeFrom = dateTimeFrom;
	}
	public Date getDateTimeTo() {
		return dateTimeTo;
	}
	public void setDateTimeTo(Date dateTimeTo) {
		this.dateTimeTo = dateTimeTo;
	}
}


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


package hr.eito.model.reports;

import hr.eito.model.DateTimeRange;

/**
 * Query parameters for Reports
 *
 * @author Hrvoje
 */
public class ReportsParams {
	
	private DateTimeRange dateTimeRange;
	private String maxDestSyn = "5";
	private String maxDestIpPing = "6";

	public DateTimeRange getDateTimeRange() {
		return dateTimeRange;
	}
	public void setDateTimeRange(DateTimeRange dateTimeRange) {
		this.dateTimeRange = dateTimeRange;
	}
	public String getMaxDestSyn() {
		return maxDestSyn;
	}
	public void setMaxDestSyn(String maxDestSyn) {
		this.maxDestSyn = maxDestSyn;
	}
	public String getMaxDestIpPing() {
		return maxDestIpPing;
	}
	public void setMaxDestIpPing(String maxDestIpPing) {
		this.maxDestIpPing = maxDestIpPing;
	}
	
}

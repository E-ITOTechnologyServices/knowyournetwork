
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


package hr.eito.model.cmdb;

/**
 * Query parameters for CMDB
 *
 * @author Hrvoje
 */
public class CMDBParams {
	
	private String ipAddress;
	
	/**
	 * Information whether instance has any parameter set
	 *
	 * @return true if any of the parameters is set, false otherwise
	 */
	public boolean hasAnyParameter() {
		return this.ipAddress!=null;
	}

	/**
	 * IP with which the CMDB will be filtered
	 *
	 * @return ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Setting the IP with which the CMDB will be filtered
	 *
	 * @param ipAddress
	 */
	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
	}

}

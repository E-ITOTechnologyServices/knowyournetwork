
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
 * Encapsulates the a CMDB report data record to be serialized back to the client.
 *
 * @author Steve Chaplin
 */
public class CMDBReturnResultData {

	private String ip_ = null;
	private String mask_ = null;
	private int maskCidr_ = 0;
	private String name_ = null;
	private String source_ = null;
	private String city_ = null;
	private String country_ = null;

	/**
	 * Set the IP address of the entry.
	 *
	 * @param ipAddress the ip address to set
	 */
	public void setIP(final String ipAddress) {
		ip_ = ipAddress;
	}

	/**
	 * Get the IP address of the entry.
	 *
	 * @return the ip address
	 */
	public String getIp() {
		return ip_;
	}

	/**
	 * Set the mask of the entry.
	 *
	 * @param mask the mask to set
	 */
	public void setMask(final String mask) {
		mask_ = mask;
	}

	/**
	 * Get the mask of the entry.
	 *
	 * @return the mask
	 */
	public String getMask() {
		return mask_;
	}

	/**
	 * Set the mask cidr of the entry.
	 *
	 * @param maskCidr the mask cidr to set
	 */
	public void setMaskCidr(final int maskCidr) {
		maskCidr_ = maskCidr;
	}

	/**
	 * Get the mask cidr of the entry.
	 *
	 * @return the mask cidr
	 */
	public int getMaskCidr() {
		return maskCidr_;
	}

	/**
	 * Set the name of the entry.
	 *
	 * @param name the name to set
	 */
	public void setName(final String name) {
		name_ = name;
	}

	/**
	 * Get the name of the entry.
	 *
	 * @return the name
	 */
	public String getName() {
		return name_;
	}

	/**
	 * Set the source of the entry.
	 *
	 * @param source the source to set
	 */
	public void setSource(final String source) {
		source_ = source;
	}

	/**
	 * Get the source of the entry.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source_;
	}

	/**
	 * Set the city of the entry.
	 *
	 * @param city the city to set
	 */
	public void setCity(final String city) {
		city_ = city;
	}

	/**
	 * Get the city of the entry.
	 *
	 * @return the city
	 */
	public String getCity() {
		return city_;
	}

	/**
	 * Set the country of the entry.
	 *
	 * @param country the country to set
	 */
	public void setCountry(final String country) {
		country_ = country;
	}

	/**
	 * Get the country of the entry.
	 *
	 * @return the country
	 */
	public String getCountry() {
		return country_;
	}
}

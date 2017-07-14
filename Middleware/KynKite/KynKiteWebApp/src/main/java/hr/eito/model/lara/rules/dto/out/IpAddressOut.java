
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


package hr.eito.model.lara.rules.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;

import hr.eito.model.lara.rules.dto.IpAddress;

/**
 * Ip address as part of Lara rule flow
 * 
 * @author Hrvoje
 *
 */
public class IpAddressOut {
	
	private final static String BROADCAST_ADDRESS = "255.255.255.255";
	
	@JsonProperty("ip_address")
	private String ipAddress;
	@JsonProperty("mask")
	private String mask;
	@JsonProperty("address_type")
	private String addressType;
	
	public IpAddressOut(final IpAddress ipAddress) {
		if (ipAddress!=null) {
			this.ipAddress = ipAddress.getIpAddress();
			this.mask = ipAddress.getMask();
			this.addressType = defineAddressType(ipAddress.getMask());
		}
	}
	
	private String defineAddressType(final String mask) {
		if (!mask.equals(BROADCAST_ADDRESS)) {
			return "network";
		}
		return "host";
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getMask() {
		return mask;
	}
	public void setMask(String mask) {
		this.mask = mask;
	}
	public String getAddressType() {
		return addressType;
	}
	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}	
	
}

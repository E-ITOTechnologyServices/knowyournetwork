
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


package hr.eito.model.lara.rules.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Lara Rules flow class
 * 
 * @author Hrvoje
 *
 */
public class Flow {
	
	@JsonProperty("SRC")
	private IpFlowEntity source;
	@JsonProperty("DST")
	private IpFlowEntity destination;
	@JsonProperty("Service")
	private PortFlowEntity service;
	
	public IpFlowEntity getSource() {
		return source;
	}
	public void setSource(IpFlowEntity source) {
		this.source = source;
	}
	public IpFlowEntity getDestination() {
		return destination;
	}
	public void setDestination(IpFlowEntity destination) {
		this.destination = destination;
	}
	public PortFlowEntity getService() {
		return service;
	}
	public void setService(PortFlowEntity service) {
		this.service = service;
	}

}

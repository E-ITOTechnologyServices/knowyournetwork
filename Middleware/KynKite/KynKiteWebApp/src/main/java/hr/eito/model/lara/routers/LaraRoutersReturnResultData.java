
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


package hr.eito.model.lara.routers;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import hr.eito.model.lara.serializer.LaraRoutersReturnResultDataSerializer;

/**
 * Return entity for Lara router IP list item
 * 
 * @author Hrvoje
 *
 */
@JsonSerialize(using = LaraRoutersReturnResultDataSerializer.class)
public class LaraRoutersReturnResultData {
		
	private String routerIP;

	/**
	 * @return IP of the router in LARA
	 */
	public String getRouterIP() {
		return routerIP;
	}
	/**
	 * @param routerIP IP of the router in Lara
	 */
	public void setRouterIP(String routerIP) {
		this.routerIP = routerIP;
	}
	
}


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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import hr.eito.model.lara.rules.dto.IpAddress;
import hr.eito.model.lara.rules.dto.IpFlowEntity;

/**
 * IpFlow entity class
 * 
 * @author Hrvoje
 *
 */
public class IpFlowEntityOut extends FlowEntityOut {
	
	@JsonProperty("members")
	private List<IpAddressOut> members;
	
	public IpFlowEntityOut(final IpFlowEntity ipFlowEntity) {
		super(ipFlowEntity.getName());
		members = new ArrayList<>();
		for (IpAddress ipAddress : ipFlowEntity.getMembers()) {
			IpAddressOut i = new IpAddressOut(ipAddress);
			this.members.add(i);
		}		
	}

	public List<IpAddressOut> getMembers() {
		return members;
	}
	public void setMembers(List<IpAddressOut> members) {
		this.members = members;
	}

}

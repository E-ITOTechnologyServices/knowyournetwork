
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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import hr.eito.model.ByteSerializer;
import hr.eito.model.lara.rules.dto.IpAddress;
import hr.eito.model.lara.rules.dto.RulesetDetails;

/**
 * Ruleset details with summary sub-statistics
 * <p>Consists of flow through network (Source, Destination, Service)
 * 
 * @author Hrvoje
 *
 */
public class RulesetDetailsOut {
	
	private final static String DEFAULT_ROUTE = "0.0.0.0";
	
	@JsonProperty("flow")
	private FlowOut flow;
	@JsonProperty("action")
	private String action;
	@JsonProperty("hits")
	private long hits;
	@JsonProperty("bytes")
	private long bytes;
	@JsonProperty("raise_incident")
	private boolean raiseIncident = false;
	
	public RulesetDetailsOut(final RulesetDetails rulesetDetails) {
		if (rulesetDetails != null) {
			this.action = rulesetDetails.getAction();
			this.hits = rulesetDetails.getHits();
			this.bytes = rulesetDetails.getBytes();
			this.raiseIncident = this.raiseIncident(rulesetDetails.getFlow().getSource().getMembers()) || 
					this.raiseIncident(rulesetDetails.getFlow().getDestination().getMembers());
			this.flow = new FlowOut(rulesetDetails.getFlow());
		}
	}
	
	private boolean raiseIncident(final List<IpAddress> members) {
		if (members != null) {
			if (members.size()==1) {
				IpAddress singleEndpoint = members.get(0);
				if (!singleEndpoint.getIpAddress().equals(DEFAULT_ROUTE)) {
					return true;
				}
			} else if (members.size()>1) {
				return true;
			}
		}
		return false;
	}
	
	public FlowOut getFlow() {
		return flow;
	}
	public void setFlow(FlowOut flow) {
		this.flow = flow;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public long getHits() {
		return hits;
	}
	public void setHits(long hits) {
		this.hits = hits;
	}
	@JsonSerialize(using = ByteSerializer.class)
	public String getBytes() {
		return String.valueOf(this.bytes);
	}
	public void setBytes(long bytes) {
		this.bytes = bytes;
	}
	public boolean isRaiseIncident() {
		return raiseIncident;
	}
	public void setRaiseIncident(boolean raiseIncident) {
		this.raiseIncident = raiseIncident;
	}

}


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


package hr.eito.kynkite;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Marko
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hits")
public class Hits {

	@XmlElement(required = true)
	private int total;
	@XmlElement(required = true)
	private int max_score;
	@XmlElement(required = true)
	private List<Hits> hits;
	
	public void setTotal(int total) {
		this.total = total;
	}
	public void setMax_score(int max_score) {
		this.max_score = max_score;
	}
	public void setHits(List<Hits> hits) {
		this.hits = hits;
	}
	
	
}

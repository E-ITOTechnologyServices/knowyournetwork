
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


package hr.eito.model.syslog;

import hr.eito.model.Params;

/**
 * Query parameters for Syslog
 *
 * @author Hrvoje
 */
public class SyslogParams extends Params {
	
	private String query;
	
	/**
	 * Information whether instance has any parameter set
	 *
	 * @return true if any of the parameters is set, false otherwise
	 */
	public boolean hasAnyParameter() {
		return this.query!=null;
	}

	/**
	 * Query expression with which the host will be filtered
	 *
	 * @return query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Setting the query expression with which the index query will be filtered
	 *
	 * @param query
	 */
	public void setQuery(final String query) {
		this.query = query;
	}

}

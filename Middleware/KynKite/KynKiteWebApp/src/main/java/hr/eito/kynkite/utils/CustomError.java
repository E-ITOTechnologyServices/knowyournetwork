
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


package hr.eito.kynkite.utils;

/**
 * Enumerator for defining custom error messages
 * used for communication with frontend
 * 
 * @author Hrvoje
 *
 */
public enum CustomError {
	
	INVALID_DATE_TIME_RANGE("Invalid date time range"),
	REPORT_NOT_CREATED("Unknown error - report not created"),
	PARAMETERS_MISSING("Missing parameters"),
	REPORT_ALREADY_EXISTS("Report already exists"),
	
	AQL_RULE_MISSING("Empty rule not allowed"),
	AQL_RULE_ALREADY_EXISTS("Rule already exists"),
	AQL_RULESET_MISSING("AQL rule not found"),
	
	IP_MISSING("IP address missing"),
	IP_INVALID("IP address invalid");
	
	private final String errorMessage;
	
	CustomError(final String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}

}

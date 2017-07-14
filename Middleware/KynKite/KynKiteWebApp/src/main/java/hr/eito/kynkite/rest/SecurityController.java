
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


package hr.eito.kynkite.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hr.eito.model.JsonReturnData;

/**
 * Rest endpoint for Security issues
 *
 * @author Hrvoje
 *
 */
@RestController
public class SecurityController {
	
	/**
	 * Check if user can approach this
	 * 
	 * @return OK status
	 */
	@RequestMapping(value = "/security/checkLogin", method = RequestMethod.POST, headers = "Accept=application/json")
	public JsonReturnData<String> checkLogin() {
		JsonReturnData<String> loginOk = new JsonReturnData<>();
		loginOk.setOK();
		return loginOk;
	}
	
}

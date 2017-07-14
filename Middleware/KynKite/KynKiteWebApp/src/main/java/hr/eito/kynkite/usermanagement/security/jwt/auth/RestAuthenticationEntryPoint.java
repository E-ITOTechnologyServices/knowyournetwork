
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


package hr.eito.kynkite.usermanagement.security.jwt.auth;

/**
 * Created by fan.jin on 2016-11-12.
 */

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import hr.eito.model.JsonReturnData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handling access to secured resource without credentials
 * 
 * @author Hrvoje
 *
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * This is invoked when user tries to access a secured REST resource without supplying any credentials
	 */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
    	// Prepare answer in appropriate JSON format
    	JsonReturnData<String> jsonReturn = new JsonReturnData<>();
    	jsonReturn.setSecurityMessage(authException.getMessage());
    	
    	response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(jsonReturn));
    }
}

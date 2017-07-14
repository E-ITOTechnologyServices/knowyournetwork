
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import hr.eito.model.JsonReturnData;

/**
 * Class that handles the logout process with the overriding method logout
 * 
 * @author Hrvoje
 *
 */
public class JwtLogoutHandler implements LogoutHandler {

	@Value("${jwt.user_cookie}")
    private String USER_COOKIE;

	@Value("${jwt.token_cookie}")
    private String TOKEN_COOKIE;
	
	@Value("${jwt.path_to_cookie}")
    private String PATH_TO_COOKIE;
	
	ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Overriding method for doing all the cleaning stuff - reseting cookies and sending them back
	 */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Erase cookies
        Cookie authCookie = new Cookie(TOKEN_COOKIE, null); // Not necessary, but saves bandwidth.
        authCookie.setPath(PATH_TO_COOKIE);
        authCookie.setMaxAge(0); // Don't set to -1 or it will become a session cookie!

        Cookie userCookie = new Cookie(USER_COOKIE, null);
        userCookie.setPath(PATH_TO_COOKIE);
        userCookie.setMaxAge(0);

        response.addCookie(authCookie);
        response.addCookie(userCookie);
        
        // Prepare answer in appropriate JSON format
    	JsonReturnData<String> jsonReturn = new JsonReturnData<>();
    	jsonReturn.setOK();
    	
    	response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        try {
			response.getWriter().write(objectMapper.writeValueAsString(jsonReturn));
		} catch (Exception e) {}
    }
}

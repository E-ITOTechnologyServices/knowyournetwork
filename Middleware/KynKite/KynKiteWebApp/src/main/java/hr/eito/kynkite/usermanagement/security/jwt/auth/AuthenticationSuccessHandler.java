
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import hr.eito.kynkite.usermanagement.security.jwt.TokenHelper;
import hr.eito.kynkite.usermanagement.security.jwt.UserTokenState;
import hr.eito.model.JsonReturnData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Handles the authentication success event
 * 
 * @author Hrvoje
 *
 */
@Component
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Value("${jwt.expires_in}")
    private String EXPIRES_IN;

	@Value("${jwt.token_cookie}")
    private String TOKEN_COOKIE;

	@Value("${jwt.user_cookie}")
	private String USER_COOKIE;
	
	@Value("${jwt.path_to_cookie}")
    private String PATH_TO_COOKIE;

	@Autowired
	TokenHelper tokenHelper;

	ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Method for preparing everything after successful authentication
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication ) throws IOException, ServletException {
		
		clearAuthenticationAttributes(request);

		// Getting user who just authenticated
		User user = (User) authentication.getPrincipal();
		
		// Creating token value based on username (among other)
		String jws = tokenHelper.generateToken(user.getUsername());

        // Create token auth Cookie
        Cookie authCookie = new Cookie(TOKEN_COOKIE, jws);
		authCookie.setPath(PATH_TO_COOKIE);
		authCookie.setHttpOnly(true);
		authCookie.setMaxAge( Integer.parseInt(this.EXPIRES_IN));
		
		// Create flag Cookie
		Cookie userCookie = new Cookie(USER_COOKIE, user.getUsername());
		userCookie.setPath(PATH_TO_COOKIE);
		userCookie.setMaxAge(Integer.parseInt(this.EXPIRES_IN));
		
		// Add cookie to response
		response.addCookie( authCookie );
		response.addCookie( userCookie );
		
		// Prepare and get JWT in JSON
		UserTokenState userTokenState = new UserTokenState(TOKEN_COOKIE, jws, USER_COOKIE, user.getUsername(), Integer.parseInt(this.EXPIRES_IN));
		// Prepare answer in appropriate JSON format
    	JsonReturnData<UserTokenState> jsonReturn = new JsonReturnData<>();
    	jsonReturn.setContent(userTokenState);
    	jsonReturn.setOK();
		String jwtResponse = objectMapper.writeValueAsString(jsonReturn);
		
		// JWT is also in the response
		response.setContentType("application/json");
		response.getWriter().write(jwtResponse);
	}
	
}

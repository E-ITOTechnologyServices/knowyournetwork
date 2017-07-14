
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


package hr.eito.kynkite.usermanagement.security.jwt;

/**
 * Class that encapsulates all necessary information to return after successful login
 * 
 * @author Hrvoje
 *
 */
public class UserTokenState {

	private String access_token_cookie;
	private String access_token;
	private String username_cookie;
	private String username;
    private long expires_in;

    public UserTokenState(String access_token_cookie, String access_token, String username_cookie, String username, long expires_in) {
    	this.setAccess_token_cookie(access_token_cookie);
        this.access_token = access_token;
        this.expires_in = expires_in;
        this.setUsername_cookie(username_cookie);
        this.username = username;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(long expires_in) {
        this.expires_in = expires_in;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAccess_token_cookie() {
		return access_token_cookie;
	}

	public void setAccess_token_cookie(String access_token_cookie) {
		this.access_token_cookie = access_token_cookie;
	}

	public String getUsername_cookie() {
		return username_cookie;
	}

	public void setUsername_cookie(String username_cookie) {
		this.username_cookie = username_cookie;
	}
	
}

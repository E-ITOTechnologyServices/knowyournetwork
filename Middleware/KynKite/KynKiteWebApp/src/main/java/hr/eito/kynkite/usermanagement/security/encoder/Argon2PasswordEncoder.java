
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


package hr.eito.kynkite.usermanagement.security.encoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Argon2i implementation of Password encoder needed to encode and verify user passwords
 * 
 * @author Hrvoje
 */
public class Argon2PasswordEncoder implements PasswordEncoder {
	
	private final static int N = 65536;
	private final static int r = 2;
	private final static int p = 1;
	
	Argon2 argon2;
	
	public Argon2PasswordEncoder() {
		argon2 = Argon2Factory.create();
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return argon2.hash(r, N, p, rawPassword.toString());
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
	    if (argon2.verify(encodedPassword, rawPassword.toString())) {
	        return true;
	    } else {
	        return false;
	    }
	}
	
}

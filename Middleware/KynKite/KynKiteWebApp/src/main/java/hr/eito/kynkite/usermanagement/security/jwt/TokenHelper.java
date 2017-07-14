
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

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Class which encapsulates saving token, and later receiving information based on saved token
 * 
 * @author Hrvoje
 *
 */
@Component
public class TokenHelper {

	@Value("${jwt.app_name}")
    private String APP_NAME;

	@Value("${jwt.secret}")
    private String SECRET;

	@Value("${jwt.expires_in}")
    private String EXPIRES_IN;

    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    /**
     * Getting the username from claims based on token
     * 
     * @param token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * Generate token based on logged user, app name, generate time, expiration date
     * and sign with defined signature algorithm and secret
     * 
     * @param username
     * @return generated token
     */
    public String generateToken(String username) {
        String jws = Jwts.builder()
                .setIssuer( APP_NAME )
                .setSubject(username)
                .setIssuedAt(generateCurrentDate())
                .setExpiration(generateExpirationDate())
                .signWith( SIGNATURE_ALGORITHM, SECRET )
                .compact();
        return jws;
    }

    /**
     * Getting the claims based on token
     * 
     * @param token
     * @return claims for received token
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(this.SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    /**
     * Helper method for getting number of milliseconds since the UNIX epoch
     * (January 1, 1970 00:00:00 UTC)
     * 
     * @return number of milliseconds since the UNIX epoch
     */
    private long getCurrentTimeMillis() {
    	return Instant.now().getEpochSecond() * 1000;
    }

    /**
     * Helper method for getting current date and time
     * 
     * @return current date and time as util.Date
     */
    private Date generateCurrentDate() {
        return new Date(getCurrentTimeMillis());
    }

    /**
     * Helper method to return date and time when token expires
     * based on current date time + defined expiration period
     * 
     * @return date and time when credentials token expires
     */
    private Date generateExpirationDate() {
        Date expirationDate = new Date(getCurrentTimeMillis() + Integer.parseInt(this.EXPIRES_IN) * 1000);
        return expirationDate;
    }
    
}

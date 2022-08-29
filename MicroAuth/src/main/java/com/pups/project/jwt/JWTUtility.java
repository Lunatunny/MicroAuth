package com.pups.project.jwt;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

public class JWTUtility {
	private String secret = "121212";
	
	public String createToken(String scope) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			long fiveHoursInMillis = 1000 * 60 * 60 * 5;
		    Date expireDate = new Date(System.currentTimeMillis() + fiveHoursInMillis);
		    String token = JWT.create()
		    		.withSubject("MSDProject")
		    		.withIssuer("jeff@austin.com")
		    		.withExpiresAt(expireDate)
		    		.withClaim("scopes", scope)
		    		.sign(algorithm);
		    return token;
		} catch (JWTCreationException exception) {
			return null;
		}
	}
}

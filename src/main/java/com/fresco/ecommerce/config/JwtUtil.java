package com.fresco.ecommerce.config;

import com.fresco.ecommerce.models.Role;
import com.fresco.ecommerce.repo.UserRepo;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fresco.ecommerce.models.User;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtUtil {
	@Value("${jwt.secret}")
	private String secretKey;

	@Value("${jwt.token.validity}")
	private Integer tokenValidity;


	@Autowired
	private UserRepo repo;

	public User getUser(final String token) {
		System.out.println("inside token decoder");
		User userDetails = new User();
		Claims claims = parseClaims(token);
		String subject = (String) claims.get(Claims.SUBJECT);
		userDetails = repo.findByUsername(subject).get();
		return userDetails;
	}

	public String generateToken(User user) {
		return Jwts.builder()
				.setSubject(user.getUsername())
				.claim("roles", user.getRoles().toString())
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + tokenValidity))
				.signWith(SignatureAlgorithm.HS512, secretKey)
				.compact();
	}

	public boolean validateToken(final String token) {
		try {
			Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException ex) {
			System.out.println("JWT expired"+ ex.getMessage());
		} catch (IllegalArgumentException ex) {
			System.out.println("Token is null, empty or only whitespace"+ex.getMessage());
		} catch (MalformedJwtException ex) {
			System.out.println("JWT is invalid"+ex);
		} catch (UnsupportedJwtException ex) {
			System.out.println("JWT is not supported"+ ex);
		} catch (SignatureException ex) {
			System.out.println("Signature validation failed");
		}

		return false;
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
				.setSigningKey(secretKey)
				.parseClaimsJws(token)
				.getBody();
	}
}

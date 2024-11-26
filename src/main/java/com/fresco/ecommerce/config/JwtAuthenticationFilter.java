package com.fresco.ecommerce.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fresco.ecommerce.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import static java.util.Objects.nonNull;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (hasAuthorizationBearer(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = getAccessToken(request);

		if (!jwtUtil.validateToken(token)) {
			filterChain.doFilter(request, response);
			return;
		}

		setAuthenticationContext(token, request);
		filterChain.doFilter(request, response);
	}


	private boolean hasAuthorizationBearer(HttpServletRequest request) {
		String header = request.getHeader("JWT");
		if (header == null || header.isEmpty()) {
			return true;
		}

		return false;
	}

	private String getAccessToken(HttpServletRequest request) {
		String header = request.getHeader("JWT");
		return header;
	}

	private void setAuthenticationContext(String token, HttpServletRequest request) {
		User userDetails = jwtUtil.getUser(token);

		userDetails.getAuthorities().stream().forEach(e -> System.out.println("authorities +"+ e.toString() ));
		UsernamePasswordAuthenticationToken
				authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

		authentication.setDetails(
				new WebAuthenticationDetailsSource().buildDetails(request));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}

/**
 * 
 */
package com.ba.auction.security;

import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ba.users.ApiException;
import com.ba.users.api.DefaultApi;
import com.ba.users.model.UserDetailsResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * @author Preeti Pandey
 *
 */
@RequiredArgsConstructor
@Component
public class AuctionAuthorizationFilter extends OncePerRequestFilter {

	private final DefaultApi defaultApi;

	private final String AUTH_HEADER = "Authorization";
	private final String AUTH_TYPE = "Bearer";

	private String extractAuthorizationHeader(HttpServletRequest request) {
		final String headerValue = request.getHeader(AUTH_HEADER);

		if (headerValue == null || !headerValue.startsWith(AUTH_TYPE)) {
			return null;
		}

		return headerValue.substring(AUTH_TYPE.length()).trim();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String token = extractAuthorizationHeader(request);

		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			defaultApi.getApiClient().setBearerToken(token);
			UserDetailsResponse authenticatedUser = defaultApi.authorizeUserToken();
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					authenticatedUser.getId(), null, authenticatedUser.getScopes().stream()
							.map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			ThreadContext.put("uid", authenticatedUser.getId());

		} catch (ApiException e) {
			if (e.getMessage().equalsIgnoreCase("The token is invalid.")) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
				return;
			}
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching external data");
			return;
		}
		filterChain.doFilter(request, response);
	}

}

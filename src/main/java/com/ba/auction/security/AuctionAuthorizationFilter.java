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

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			UserDetailsResponse authenticatedUser = defaultApi.authorizeUserToken();
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					authenticatedUser.getId(), null, authenticatedUser.getScopes().stream()
							.map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			ThreadContext.put("uid", authenticatedUser.getId());
            ThreadContext.put("privileges", String.join(",", authenticatedUser.getScopes()));

		} catch (ApiException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching external data");
			return;
		}
		filterChain.doFilter(request, response);
	}

}

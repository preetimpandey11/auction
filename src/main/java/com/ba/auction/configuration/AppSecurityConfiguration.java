/**
 * 
 */
package com.ba.auction.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ba.auction.security.AuctionAuthorizationFilter;

import lombok.RequiredArgsConstructor;

/**
 * @author Preeti Pandey
 *
 */
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Configuration(proxyBeanMethods = false)
public class AppSecurityConfiguration {

	private final AuctionAuthorizationFilter auctionAuthorizationFilter;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(configurer -> configurer.requestMatchers("/error", "/swagger-ui/**","/v3/api-docs/**").permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(auctionAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}

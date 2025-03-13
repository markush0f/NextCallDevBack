package com.tableconnect.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
	    return http
	            .csrf(ServerHttpSecurity.CsrfSpec::disable)
	            .authorizeExchange(exchanges -> exchanges
	                    .pathMatchers("/user/**").permitAll()
	                    .pathMatchers("/meeting/**").permitAll()
	                    .pathMatchers("/fallback/**").permitAll()
	                    .anyExchange().authenticated()       
	            )
	            .build();
	}


}
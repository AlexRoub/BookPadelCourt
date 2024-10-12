package com.proservices.bookpadelcourt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.proservices.bookpadelcourt.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CustomUserDetailsService customUserDetailsService;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(CustomUserDetailsService customUserDetailsService,
		JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
		JwtAuthenticationFilter jwtAuthenticationFilter) {

		this.customUserDetailsService = customUserDetailsService;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http,
		PasswordEncoder passwordEncoder,
		UserDetailsService userDetailsService)
		throws Exception {

		return http.getSharedObject(AuthenticationManagerBuilder.class)
			.userDetailsService(customUserDetailsService)
			.passwordEncoder(passwordEncoder)
			.and()
			.build();
	}

	// SecurityFilterChain bean (for Spring Boot 3.x and newer)
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http)
		throws Exception {

		http.cors()
			.and()
			.csrf()
			.disable()
			.exceptionHandling()
			.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No session
			.and()
			.authorizeHttpRequests()
			.antMatchers("/api/auth/**")
			.permitAll() // Allow unauthenticated access to auth endpoints
			.anyRequest()
			.authenticated(); // All other endpoints require authentication

		// Add JWT filter before UsernamePasswordAuthenticationFilter
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder();  // Use BCrypt for password hashing
	}
}

package com.proservices.bookpadelcourt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proservices.bookpadelcourt.config.JwtTokenProvider;
import com.proservices.bookpadelcourt.dto.response.JwtAuthResponse;
import com.proservices.bookpadelcourt.dto.LoginDto;
import com.proservices.bookpadelcourt.dto.UserRegistrationDto;
import com.proservices.bookpadelcourt.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthenticationController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
		// Authenticate user
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
		);

		// Generate JWT token after successful authentication
		String token = jwtTokenProvider.generateToken(authentication);
		return ResponseEntity.ok(new JwtAuthResponse(token));
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDto registrationDto) {
		userService.registerNewUser(registrationDto);
		return ResponseEntity.ok("User registered successfully.");
	}

	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
		try {
			// Authenticate the user using AuthenticationManager
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					loginDto.getUsername(),
					loginDto.getPassword()
				)
			);

			// Generate JWT token after successful authentication
			String token = jwtTokenProvider.generateToken(authentication);

			// Return the token in the response
			return ResponseEntity.ok(new JwtAuthResponse(token));
		} catch (Exception ex) {
			return ResponseEntity.status(401).body("Invalid username or password");
		}
	}

	// Placeholder for future Google/Fingerprint login methods
}

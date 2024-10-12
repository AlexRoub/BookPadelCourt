package com.proservices.bookpadelcourt.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import com.proservices.bookpadelcourt.exceptions.ForbiddenException;
import com.proservices.bookpadelcourt.model.request.AuthenticationRequest;
import com.proservices.bookpadelcourt.model.response.AuthenticationResponse;
import com.proservices.bookpadelcourt.model.dto.UserRegistrationDto;
import com.proservices.bookpadelcourt.service.UserService;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

	private final UserService userService;

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody @Valid final UserRegistrationDto registrationDto) {

		return ResponseEntity.ok(userService.registerNewUser(registrationDto));
	}

	@PostMapping("/register/admin")
	public ResponseEntity<AuthenticationResponse> registerAdmin(@RequestBody @Valid final UserRegistrationDto registrationDto) {

		return ResponseEntity.ok(userService.registerNewAdmin(registrationDto));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody final AuthenticationRequest request) {

		try {
			final var response = userService.authenticate(request);
			return ResponseEntity.ok(response);
		} catch (final ForbiddenException ex) {
			final var response = AuthenticationResponse.builder()
				.errorMessage(ex.getMessage())
				.build();
			return ResponseEntity.status(401)
				.body(response);
		}
	}

	@PostMapping("/refresh-token")
	public void refreshToken(final HttpServletRequest request, final ServletResponse response)
		throws IOException {

		userService.refreshToken(request, response);
	}

	//Same endpoint for deleting admin too
	@DeleteMapping("/delete/{userId}")
	public ResponseEntity<String> deleteUser(@PathVariable final Long userId) {

		try {
			userService.deleteUser(userId);

			return ResponseEntity.ok("User deleted successfully.");
		} catch (final RuntimeException runEx) {
			log.debug("Delete user exception with message: ", runEx.getMessage());
			return ResponseEntity.badRequest()
				.body(runEx.getMessage());
		}
	}

	// Placeholder for future Google/Fingerprint login methods
}

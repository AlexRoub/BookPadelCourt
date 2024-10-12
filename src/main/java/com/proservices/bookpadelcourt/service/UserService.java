package com.proservices.bookpadelcourt.service;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proservices.bookpadelcourt.config.JwtService;
import com.proservices.bookpadelcourt.entity.User;
import com.proservices.bookpadelcourt.enums.Role;
import com.proservices.bookpadelcourt.exceptions.ForbiddenException;
import com.proservices.bookpadelcourt.model.dto.UserRegistrationDto;
import com.proservices.bookpadelcourt.model.request.AuthenticationRequest;
import com.proservices.bookpadelcourt.model.response.AuthenticationResponse;
import com.proservices.bookpadelcourt.repository.TokenRepository;
import com.proservices.bookpadelcourt.repository.UserRepository;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final TokenRepository tokenRepository;
	private final TokenService tokenService;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	@Transactional
	public AuthenticationResponse registerNewUser(@NonNull final UserRegistrationDto registrationDto) {

		checkIfUserExists(registrationDto);

		final var user = User.builder()
			.username(registrationDto.getUsername())
			.email(registrationDto.getEmail())
			.password(passwordEncoder.encode(registrationDto.getPassword()))
			.role(Role.USER)
			.build();

		final var savedUser = userRepository.save(user);

		final var jwtToken = jwtService.generateToken(user);
		final var refreshToken = jwtService.generateRefreshToken(user);

		tokenService.saveUserToken(savedUser, jwtToken);

		return AuthenticationResponse.builder()
			.accessToken(jwtToken)
			.refreshToken(refreshToken)
			.build();
	}

	@Transactional
	public AuthenticationResponse registerNewAdmin(@NonNull final UserRegistrationDto registrationDto) {

		checkIfUserExists(registrationDto);

		final var user = User.builder()
			.username(registrationDto.getUsername())
			.email(registrationDto.getEmail())
			.password(passwordEncoder.encode(registrationDto.getPassword()))
			.role(Role.ADMIN)
			.build();

		final var savedUser = userRepository.save(user);

		final var jwtToken = jwtService.generateToken(user);
		final var refreshToken = jwtService.generateRefreshToken(user);

		tokenService.saveUserToken(savedUser, jwtToken);

		return AuthenticationResponse.builder()
			.accessToken(jwtToken)
			.refreshToken(refreshToken)
			.build();
	}

	@Transactional
	public AuthenticationResponse registerNewSuperAdmin(@NonNull final UserRegistrationDto registrationDto) {

		checkIfUserExists(registrationDto);

		final var user = User.builder()
			.username(registrationDto.getUsername())
			.email(registrationDto.getEmail())
			.password(passwordEncoder.encode(registrationDto.getPassword()))
			.role(Role.SUPER_ADMIN)
			.build();

		final var savedUser = userRepository.save(user);

		final var jwtToken = jwtService.generateToken(user);
		final var refreshToken = jwtService.generateRefreshToken(user);

		tokenService.saveUserToken(savedUser, jwtToken);

		return AuthenticationResponse.builder()
			.accessToken(jwtToken)
			.refreshToken(refreshToken)
			.build();
	}

	public AuthenticationResponse authenticate(final AuthenticationRequest request) {

		final var authToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
		authenticationManager.authenticate(authToken);

		final var user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> ForbiddenException.builder()
				.message("Invalid username or password")
				.build());

		final var jwtToken = jwtService.generateToken(user);
		final var refreshToken = jwtService.generateRefreshToken(user);

		revokeAllUserTokens(user);
		tokenService.saveUserToken(user, jwtToken);

		return AuthenticationResponse.builder()
			.accessToken(jwtToken)
			.refreshToken(refreshToken)
			.build();
	}

	public void refreshToken(final HttpServletRequest request, final ServletResponse response)
		throws IOException {

		final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}

		final var refreshToken = authHeader.substring(7);
		final var userEmail = jwtService.extractUsername(refreshToken);

		if (userEmail != null) {
			final var user = userRepository.findByEmail(userEmail)
				.orElseThrow();
			if (jwtService.isTokenValid(refreshToken, user)) {
				final var accessToken = jwtService.generateToken(user);
				revokeAllUserTokens(user);
				tokenService.saveUserToken(user, accessToken);
				final var authResponse = AuthenticationResponse.builder()
					.accessToken(accessToken)
					.refreshToken(refreshToken)
					.build();
				new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
			}
		}
	}

	@Transactional
	public void deleteUser(@NonNull final Long userId)
		throws RuntimeException {

		final var isExistingUser = checkIfUserExists(userId);

		if (isExistingUser) {
			userRepository.deleteById(userId);
		}

		throw new RuntimeException("Could not find and delete user");
	}

	@Transactional
	private void revokeAllUserTokens(final User user) {

		final var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());

		if (validUserTokens.isEmpty()) {
			return;
		}

		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});

		tokenRepository.saveAll(validUserTokens);
	}

	private boolean checkIfUserExists(final UserRegistrationDto registrationDto) {

		if (userRepository.findByUsername(registrationDto.getUsername())
			.isPresent()) {
			return true;
		}

		if (userRepository.findByEmail(registrationDto.getEmail())
			.isPresent()) {
			return true;
		}

		return false;
	}

	private boolean checkIfUserExists(final Long userId) {

		return userRepository.findById(userId)
			.isPresent();
	}
}

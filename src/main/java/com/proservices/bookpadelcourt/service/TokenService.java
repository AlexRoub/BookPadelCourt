package com.proservices.bookpadelcourt.service;

import org.springframework.stereotype.Service;

import com.proservices.bookpadelcourt.entity.Token;
import com.proservices.bookpadelcourt.entity.User;
import com.proservices.bookpadelcourt.model.TokenType;
import com.proservices.bookpadelcourt.repository.TokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final TokenRepository tokenRepository;

	public void saveUserToken(final User user, final String jwtToken) {

		final var token = Token.builder()
			.user(user)
			.token(jwtToken)
			.tokenType(TokenType.BEARER)
			.expired(false)
			.revoked(false)
			.build();

		tokenRepository.save(token);
	}
}

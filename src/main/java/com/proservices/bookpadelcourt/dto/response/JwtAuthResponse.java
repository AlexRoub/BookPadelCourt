package com.proservices.bookpadelcourt.dto.response;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthResponse {
	private String token;

	public JwtAuthResponse(String token) {
		this.token = token;
	}
}

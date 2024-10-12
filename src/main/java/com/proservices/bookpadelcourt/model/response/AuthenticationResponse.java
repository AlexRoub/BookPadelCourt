package com.proservices.bookpadelcourt.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

	@JsonProperty("accessToken")
	private String accessToken;
	@JsonProperty("refreshToken")
	private String refreshToken;
	@JsonProperty("errorMessage")
	private String errorMessage;
}

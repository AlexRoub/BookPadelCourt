package com.proservices.bookpadelcourt.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CourtBookingRequest {
	private LocalDate date;
	private String timeRange;
	private Integer neededPlayers;
	private String playerLevel;
	private Boolean isForAnotherPlayer;
	private String otherPlayerName;
	private String otherPlayerContact;
}

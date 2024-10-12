package com.proservices.bookpadelcourt.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationDto {
	private Long id;
	private String userFullName;
	private String userPhoneNumber;
	private Long courtId;
	private String courtName;
	private LocalDate reservationDate;
	private String reservationTimeRange;
	private String status;
}

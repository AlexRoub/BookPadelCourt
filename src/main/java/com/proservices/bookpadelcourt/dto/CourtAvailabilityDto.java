package com.proservices.bookpadelcourt.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class CourtAvailabilityDto {
	private Long courtId;
	private LocalDate date;
	private List<String> availableTimeRanges;
}

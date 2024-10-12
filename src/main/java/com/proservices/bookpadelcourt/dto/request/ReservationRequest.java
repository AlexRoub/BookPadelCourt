package com.proservices.bookpadelcourt.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.proservices.bookpadelcourt.enums.SkillLevel;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationRequest {

	@NotNull(message = "Court ID is required")
	private Long courtId;

	@NotNull(message = "User ID is required")
	private Long userId;

	@NotNull(message = "Date is required")
	@Future(message = "Date must be a future date")
	private LocalDate date;

	@NotNull(message = "Start time is required")
	private LocalTime startTime;

	@NotNull(message = "End time is required")
	private LocalTime endTime;

	private Integer playersNeeded;
	private List<SkillLevel> skillLevelNeeded;
}

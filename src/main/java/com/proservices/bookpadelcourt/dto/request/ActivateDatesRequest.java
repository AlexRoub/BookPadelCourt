package com.proservices.bookpadelcourt.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivateDatesRequest {

	@NotNull(message = "Court ID is mandatory")
	private Long courtId;

	@NotNull(message = "Start date is mandatory")
	@Future(message = "Start date must be a future date")
	private LocalDate startDate;

	@NotNull(message = "End date is mandatory")
	@Future(message = "End date must be a future date")
	private LocalDate endDate;
}

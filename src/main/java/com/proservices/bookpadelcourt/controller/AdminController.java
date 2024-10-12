package com.proservices.bookpadelcourt.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proservices.bookpadelcourt.dto.ReservationDto;
import com.proservices.bookpadelcourt.dto.request.DeactivateDatesRequest;
import com.proservices.bookpadelcourt.service.CourtService;
import com.proservices.bookpadelcourt.service.ReservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final CourtService courtService;
	private final ReservationService reservationService;

	@PostMapping("/courts/{courtId}/reservations")
	public ResponseEntity<List<ReservationDto>> getCourtReservations(@PathVariable final Long courtId, @RequestBody final LocalDate date) {

		final List<ReservationDto> reservations = reservationService.getCourtReservations(courtId, date);
		return ResponseEntity.ok(reservations);
	}

	@PostMapping("/courts/cancel/{reservationId}")
	public ResponseEntity<String> cancelReservation(@PathVariable final Long reservationId) {

		final var isSuccess = reservationService.cancelReservation(reservationId);

		return isSuccess
			? ResponseEntity.ok("Reservation canceled successfully.")
			: ResponseEntity.status(HttpStatus.CONFLICT)
				.body("Unable to cancel reservation.");
	}

	@PostMapping("/courts/deactivate")
	public ResponseEntity<String> deactivateCourtDates(@RequestBody final DeactivateDatesRequest deactivateRequest) {

		courtService.deactivateCourt(deactivateRequest);
		return ResponseEntity.ok("Court dates deactivated.");
	}

	@PostMapping("/courts/{courtId}/reactivate/{deactivationId}")
	public ResponseEntity<String> reactivateCourtDates(@PathVariable final Long courtId, @PathVariable final Long deactivationId) {

		courtService.reactivateCourt(courtId, deactivationId);
		return ResponseEntity.ok("Court dates deactivated.");
	}
}

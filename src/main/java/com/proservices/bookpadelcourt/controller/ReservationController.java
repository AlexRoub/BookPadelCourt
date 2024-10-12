package com.proservices.bookpadelcourt.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proservices.bookpadelcourt.model.dto.ReservationDto;
import com.proservices.bookpadelcourt.service.ReservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@GetMapping("/history/{id}")
	public ResponseEntity<List<ReservationDto>> getUserReservationHistory(@PathVariable final String id) {

		List<ReservationDto> reservations = reservationService.getUserReservationHistory(id);
		return ResponseEntity.ok(reservations);
	}

	@GetMapping("/upcoming/{id}")
	public ResponseEntity<List<ReservationDto>> getUpcomingReservations(@PathVariable final String id) {

		List<ReservationDto> reservations = reservationService.getUpcomingReservations(id);
		return ResponseEntity.ok(reservations);
	}

	@DeleteMapping("/{id}/cancel")
	public ResponseEntity<String> cancelReservation(@PathVariable Long id) {

		boolean success = reservationService.cancelReservation(id);
		if (success) {
			return ResponseEntity.ok("Reservation canceled successfully.");
		} else {
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body("Unable to cancel reservation.");
		}
	}
}

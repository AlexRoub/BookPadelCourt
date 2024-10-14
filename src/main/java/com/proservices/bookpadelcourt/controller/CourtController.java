package com.proservices.bookpadelcourt.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proservices.bookpadelcourt.entity.Reservation;
import com.proservices.bookpadelcourt.model.dto.CourtDto;
import com.proservices.bookpadelcourt.model.request.CourtBookingRequest;
import com.proservices.bookpadelcourt.service.CourtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courts")
public class CourtController {

	private final CourtService courtService;

	@GetMapping
	public ResponseEntity<List<CourtDto>> getAllCourts() {

		final var courts = courtService.getAllCourts();

		return ResponseEntity.ok(courts);
	}

	@GetMapping("/{courtId}/availability")
	public ResponseEntity<Map<String, List<LocalTime>>> getCourtAvailability(@PathVariable final Long courtId, @RequestParam final LocalDate date) {

		final List<Reservation> reservations = courtService.getCourtReservationsByDate(courtId, date);

		// Generate the half-hour time slots (from 8:00 AM to 10:00 PM as an example)
		final var startTime = LocalTime.of(8, 0);
		final var endTime = LocalTime.of(22, 0);
		final List<LocalTime> halfHourSlots = generateHalfHourSlots(startTime, endTime);

		// Split into reserved and free slots
		final Map<String, List<LocalTime>> slotMap = splitSlotsIntoFreeAndReserved(halfHourSlots, reservations);

		return ResponseEntity.ok(slotMap);
	}

	@PostMapping("/{id}/book")
	public ResponseEntity<String> bookCourt(@PathVariable final Long id, @RequestBody final CourtBookingRequest bookingRequest) {

		final boolean isSuccess = courtService.bookCourt(id, bookingRequest);

		if (isSuccess) {
			return ResponseEntity.ok("Court booked successfully.");
		} else {
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body("Court not available for the selected time.");
		}
	}
}

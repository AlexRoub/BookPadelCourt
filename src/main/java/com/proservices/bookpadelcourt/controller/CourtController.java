package com.proservices.bookpadelcourt.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proservices.bookpadelcourt.model.dto.CourtAvailabilityDto;
import com.proservices.bookpadelcourt.model.request.CourtBookingRequest;
import com.proservices.bookpadelcourt.model.dto.CourtDto;
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

	@GetMapping("/{id}/availability")
	public ResponseEntity<CourtAvailabilityDto> checkAvailability(@PathVariable final Long id, @RequestParam final LocalDate date) {

		final CourtAvailabilityDto availability = courtService.checkAvailability(id, date);

		return ResponseEntity.ok(availability);
	}

	@PostMapping("/{id}/book")
	public ResponseEntity<String> bookCourt(@PathVariable final Long id, @RequestBody final CourtBookingRequest bookingRequest) {

		final boolean isSuccess = courtService.bookCourt(id, bookingRequest);

		if (isSuccess) {
			return ResponseEntity.ok("Court booked successfully.");
		} else {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Court not available for the selected time.");
		}
	}
}

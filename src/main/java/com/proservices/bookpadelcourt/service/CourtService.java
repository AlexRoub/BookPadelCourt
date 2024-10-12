package com.proservices.bookpadelcourt.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proservices.bookpadelcourt.dto.CourtDto;
import com.proservices.bookpadelcourt.dto.request.DeactivateDatesRequest;
import com.proservices.bookpadelcourt.entity.Court;
import com.proservices.bookpadelcourt.entity.DeactivationDate;
import com.proservices.bookpadelcourt.entity.Reservation;
import com.proservices.bookpadelcourt.entity.User;
import com.proservices.bookpadelcourt.repository.CourtRepository;
import com.proservices.bookpadelcourt.repository.DeactivateDatesRepository;
import com.proservices.bookpadelcourt.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourtService {

	private final CourtRepository courtRepository;
	private final ReservationRepository reservationRepository;
	private final DeactivateDatesRepository deactivateDatesRepository;

	// Get all courts
	public List<CourtDto> getAllCourts() {

		List<Court> courts = courtRepository.findAll();
		return courts.stream()
			.map(court -> CourtDto.builder()
				.id(court.getId())
				.name(court.getName())
				.location(court.getLocation())
				.company(court.getCompany())
				.isActive(court.isActive())
				.createdAt(court.getCreatedAt()
					.toString())
				.build())
			.collect(Collectors.toList());
	}

	// Get a court by ID
	public Optional<CourtDto> getCourtById(Long courtId) {

		return courtRepository.findById(courtId)
			.map(court -> CourtDto.builder()
				.id(court.getId())
				.name(court.getName())
				.location(court.getLocation())
				.company(court.getCompany())
				.isActive(court.isActive())
				.createdAt(court.getCreatedAt()
					.toString())
				.build());
	}

	public void deactivateCourt(DeactivateDatesRequest deactivateRequest) {

		Court court = courtRepository.findById(deactivateRequest.getCourtId())
			.orElseThrow(() -> new RuntimeException("Court not found"));

		final var deactivationDate = DeactivationDate.builder()
			.court(court)
			.startDate(deactivateRequest.getStartDate())
			.endDate(deactivateRequest.getEndDate())
			.build();

		deactivateDatesRepository.save(deactivationDate);
	}

	public void reactivateCourt(Long courtId, Long deactivationId) {

		var court = courtRepository.findById(courtId)
			.orElseThrow(() -> new RuntimeException("Court not found"));

		final var optDeactivationDate = court.getDeactivateDates()
			.stream()
			.filter(d -> d.getId().equals(deactivationId))
			.findFirst();

		if (optDeactivationDate.isPresent()) {
			final var deactivationDate = optDeactivationDate.get();
			deactivateDatesRepository.delete(deactivationDate);
		}
	}

	public List<Court> checkAvailability(LocalDate date, LocalTime startTime, LocalTime endTime) {

		List<Court> allCourts = courtRepository.findAll();
		return allCourts.stream()
			.filter(court -> reservationRepository.isCourtAvailable(court.getId(), date, startTime, endTime))
			.toList();
	}

	public Reservation bookCourt(User user,
		Court court,
		LocalDate date,
		LocalTime startTime,
		LocalTime endTime,
		Integer playersNeeded,
		String skillLevel) {
		// Check if the court is available before booking
		if (!reservationRepository.isCourtAvailable(court.getId(), date, startTime, endTime)) {
			throw new IllegalStateException("Court is not available for the selected time.");
		}

		// Create a new reservation
		Reservation reservation = Reservation.builder()
			.user(user)
			.court(court)
			.date(date)
			.startTime(startTime)
			.endTime(endTime)
			.playersNeeded(playersNeeded)
			.skillLevelNeeded(skillLevel)
			.build();

		// Save the reservation to the database
		return reservationRepository.save(reservation);
	}

	public List<Reservation> getCourtReservations(Long courtId) {

		return reservationRepository.findByCourtId(courtId);
	}
}

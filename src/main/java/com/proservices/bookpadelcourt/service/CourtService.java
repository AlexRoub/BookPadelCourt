package com.proservices.bookpadelcourt.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.proservices.bookpadelcourt.entity.Company;
import com.proservices.bookpadelcourt.model.dto.CourtDto;
import com.proservices.bookpadelcourt.model.request.DeactivateDatesRequest;
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

		final var courts = courtRepository.findAll();
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

	public Map<String, List<LocalTime>> getCourtReservationsByDate(final Long courtId, final LocalDate date) {

		final var reservations = reservationRepository.findByCourtIdAndDate(courtId, date);
		final var court = courtRepository.findById(courtId)
			.orElse(null);

		final var company = court.getCompany();
		final var startTime = company.getDailyOpenedFrom();
		final var endTime = company.getDailyOpenedUntil();
		final var halfHourSlots = generateHalfHourSlots(startTime, endTime);

		return splitSlotsIntoFreeAndReserved(halfHourSlots, reservations);
	}

	private Map<String, List<LocalTime>> splitSlotsIntoFreeAndReserved(final List<LocalTime> halfHourSlots, final List<Reservation> reservations) {

		final List<LocalTime> reservedSlots = new ArrayList<>();
		final List<LocalTime> freeSlots = new ArrayList<>(halfHourSlots);

		for (final var reservation : reservations) {
			final var reservationStart = reservation.getStartTime();
			final var reservationEnd = reservation.getEndTime();

			// Loop through the slots and mark the reserved ones
			for (final var slot : halfHourSlots) {
				if (!slot.isBefore(reservationStart) && slot.isBefore(reservationEnd)) {
					reservedSlots.add(slot);
				}
			}
		}

		// Remove reserved slots from the free slots list
		freeSlots.removeAll(reservedSlots);

		// Create a map to store free and reserved slots
		final Map<String, List<LocalTime>> slotMap = new HashMap<>();
		slotMap.put("reserved", reservedSlots);
		slotMap.put("free", freeSlots);

		return slotMap;
	}

	private List<LocalTime> generateHalfHourSlots(LocalTime startTime, final LocalTime endTime) {

		final List<LocalTime> slots = new ArrayList<>();
		while (startTime.isBefore(endTime)) {
			slots.add(startTime);
			startTime = startTime.plusMinutes(30); // Increment by 30 minutes
		}
		return slots;
	}

	// Get a court by ID
	public Optional<CourtDto> getCourtById(final Long courtId) {

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

	public void deactivateCourt(final DeactivateDatesRequest deactivateRequest) {

		final var court = courtRepository.findById(deactivateRequest.getCourtId())
			.orElseThrow(() -> new RuntimeException("Court not found"));

		final var deactivationDate = DeactivationDate.builder()
			.court(court)
			.startDate(deactivateRequest.getStartDate())
			.endDate(deactivateRequest.getEndDate())
			.build();

		deactivateDatesRepository.save(deactivationDate);
	}

	public void reactivateCourt(final Long courtId, final Long deactivationId) {

		final var court = courtRepository.findById(courtId)
			.orElseThrow(() -> new RuntimeException("Court not found"));

		final var optDeactivationDate = court.getDeactivateDates()
			.stream()
			.filter(d -> d.getId()
				.equals(deactivationId))
			.findFirst();

		if (optDeactivationDate.isPresent()) {
			final var deactivationDate = optDeactivationDate.get();
			deactivateDatesRepository.delete(deactivationDate);
		}
	}

	public List<Court> checkAvailability(final LocalDate date, final LocalTime startTime, final LocalTime endTime) {

		final var allCourts = courtRepository.findAll();
		return allCourts.stream()
			.filter(court -> reservationRepository.isCourtAvailable(court.getId(), date, startTime, endTime))
			.toList();
	}

	public Reservation bookCourt(final User user,
		final Court court,
		final LocalDate date,
		final LocalTime startTime,
		final LocalTime endTime,
		final Integer playersNeeded,
		final String skillLevel) {
		// Check if the court is available before booking
		if (!reservationRepository.isCourtAvailable(court.getId(), date, startTime, endTime)) {
			throw new IllegalStateException("Court is not available for the selected time.");
		}

		// Create a new reservation
		final var reservation = Reservation.builder()
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

	public List<Reservation> getCourtReservations(final Long courtId) {

		return reservationRepository.findByCourtId(courtId);
	}
}

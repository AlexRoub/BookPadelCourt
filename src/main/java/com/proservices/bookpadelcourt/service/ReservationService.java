package com.proservices.bookpadelcourt.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.proservices.bookpadelcourt.dto.ReservationDto;
import com.proservices.bookpadelcourt.dto.request.ReservationRequest;
import com.proservices.bookpadelcourt.entity.Court;
import com.proservices.bookpadelcourt.entity.Reservation;
import com.proservices.bookpadelcourt.enums.Status;
import com.proservices.bookpadelcourt.repository.CourtRepository;
import com.proservices.bookpadelcourt.repository.DeactivateDatesRepository;
import com.proservices.bookpadelcourt.repository.ReservationRepository;
import com.proservices.bookpadelcourt.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

	private final ReservationRepository reservationRepository;
	private final DeactivateDatesRepository deactivateDatesRepository;
	private final CourtRepository courtRepository;
	private final UserRepository userRepository;

	public List<ReservationDto> getCourtReservations(final Long courtId, final LocalDate date) {

		final var reservations = reservationRepository.findByCourtIdAndDate(courtId, date);

		return reservations.stream()
			.map(this::buildReservationDto)
			.collect(Collectors.toList());
	}

	public List<Reservation> getUserReservationHistory(final String userId) {

		return reservationRepository.findByUserId(Long.valueOf(userId));
	}

	public List<Reservation> getUpcomingReservations(final String userId) {

		final var date = LocalDate.now();
		return reservationRepository.findByUserIdAndDateAfter(Long.valueOf(userId), date);
	}

	@Transactional
	public Reservation createReservation(final ReservationRequest reservationRequest) {

		// Retrieve the court
		final var court = courtRepository.findById(reservationRequest.getCourtId())
			.orElse(null);

		if (court == null) {
			return null;
		}

		if (!isCourtAvailable(reservationRequest, court)) {
			return null;
		}

		final var user = userRepository.findById(reservationRequest.getUserId())
			.orElseThrow(() -> new RuntimeException("User not found"));

		final var status = reservationRequest.getPlayersNeeded() > 0
			? Status.PENDING
			: Status.BOOKED;

		final var reservation = Reservation.builder()
			.user(user)
			.court(court)
			.date(reservationRequest.getDate())
			.startTime(reservationRequest.getStartTime())
			.endTime(reservationRequest.getEndTime())
			.playersNeeded(reservationRequest.getPlayersNeeded())
			.skillLevelNeeded(reservationRequest.getSkillLevelNeeded())
			.status(status)
			.createdAt(LocalDate.now())
			.build();

		//TODO add payment implementation before reservation
		//TODO calculate firstly the amount to be paid based on reservation time divided by 4

		return reservationRepository.save(reservation);
	}

	private boolean isCourtAvailable(final ReservationRequest reservationRequest, final Court court) {
		// Check if the court is available during the requested time
		final var isReservationAvailable = reservationRepository.isCourtAvailable(court.getId(),
			reservationRequest.getDate(),
			reservationRequest.getStartTime(),
			reservationRequest.getEndTime());

		// Check if the court is available during the requested time
		final var isCourtDeactivated = deactivateDatesRepository.isCourtDeactivated(court.getId(), reservationRequest.getDate());

		if (!isReservationAvailable || isCourtDeactivated) {
			log.debug("Court is not available during the requested time.");
			return false;
		}
		return true;
	}

	public boolean cancelUserReservation(final Long userId, final Long reservationId) {

		final var optReservation = reservationRepository.findByIdAndUserId(reservationId, userId);
		if (optReservation.isEmpty()) {
			return false;
		}

		final var reservation = optReservation.get();

		// Check if the reservation can still be canceled (e.g., at least 24 hours before the game)
		final var now = LocalDate.now();
		final var isOneDayBefore = reservation.getDate()
			.minusDays(1)
			.isBefore(now);

		if (isOneDayBefore) {
			log.debug("Cannot cancel reservation less than 24 hours before the game.");
			return false;
		}

		reservation.setStatus(Status.CANCELLED);

		try {
			reservationRepository.save(reservation);
			return true;
		} catch (final RuntimeException re) {
			return false;
		}
	}

	public boolean cancelReservation(final Long reservationId) {

		final var optReservation = reservationRepository.findById(reservationId);

		if (optReservation.isEmpty()) {
			return false;
		}

		final var reservation = optReservation.get();
		reservation.setStatus(Status.CANCELLED);

		try {
			reservationRepository.save(reservation);
			return true;
		} catch (final RuntimeException re) {
			return false;
		}
	}

	private ReservationDto buildReservationDto(final Reservation reservation) {

		final var phoneNumber = reservation.getUser()
			.getPhoneNumber();

		return ReservationDto.builder()
			.id(reservation.getId())
			.userFullName(reservation.getUser()
				.getFirstName() + " " + reservation.getUser()
				.getLastName())
			.userPhoneNumber(phoneNumber)
			.courtId(reservation.getCourt()
				.getId())
			.courtName(reservation.getCourt()
				.getName())
			.reservationDate(reservation.getDate())
			.reservationTimeRange(reservation.getStartTime()
				.toString() + " " + reservation.getEndTime()
				.toString())
			.build();
	}
}


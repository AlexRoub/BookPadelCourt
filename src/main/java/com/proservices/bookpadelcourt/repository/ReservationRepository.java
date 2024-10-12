package com.proservices.bookpadelcourt.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proservices.bookpadelcourt.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	List<Reservation> findByCourtId(Long courtId);
	List<Reservation> findByCourtIdAndDate(Long courtId, LocalDate date);
	List<Reservation> findByUserId(Long userId);
	List<Reservation> findByUserIdAndDateAfter(Long userId, LocalDate date);
	Optional<Reservation> findByIdAndUserId(Long reservationId, Long userId);

	// Custom query to check if a court is available (not reserved for the requested time)
	@Query("SELECT CASE WHEN COUNT(r) = 0 THEN TRUE ELSE FALSE END FROM Reservation r " +
		"WHERE r.court.id = :courtId AND r.date = :date AND " +
		"(r.startTime < :endTime AND r.endTime > :startTime)")
	boolean isCourtAvailable(Long courtId, LocalDate date, LocalTime startTime, LocalTime endTime);
}

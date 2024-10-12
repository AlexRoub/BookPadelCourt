package com.proservices.bookpadelcourt.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proservices.bookpadelcourt.entity.Court;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
	List<Court> findAll();

	// Custom query to find available courts (not reserved during the requested time)
	@Query("SELECT c FROM Court c WHERE c.id NOT IN (SELECT r.court.id FROM Reservation r WHERE " +
		"(r.date = :date AND (r.startTime < :endTime AND r.endTime > :startTime)))")
	List<Court> findAvailableCourts(LocalDateTime date, LocalDateTime startTime, LocalDateTime endTime);
}

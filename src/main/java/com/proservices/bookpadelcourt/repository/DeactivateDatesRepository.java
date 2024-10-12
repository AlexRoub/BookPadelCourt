package com.proservices.bookpadelcourt.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proservices.bookpadelcourt.entity.DeactivationDate;

@Repository
public interface DeactivateDatesRepository extends JpaRepository<DeactivationDate, Long> {

	// Custom query to check if a court is unavailable for specific date
	@Query("SELECT CASE WHEN COUNT(dd) = 0 THEN FALSE ELSE TRUE END FROM DeactivationDate dd " +
		"WHERE dd.court.id = :courtId AND" +
		"(dd.startDate < :date AND dd.endDate > :date)")
	boolean isCourtDeactivated(Long courtId, LocalDate date);
}

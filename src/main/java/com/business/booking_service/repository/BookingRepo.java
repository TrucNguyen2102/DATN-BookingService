package com.business.booking_service.repository;

import com.business.booking_service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Integer> {
    Optional<Booking> findById(Integer id);

    List<Booking> findAll();

    List<Booking> findByStatus(String status);

    List<Booking> findByUserId(Integer userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE DATE(b.bookingTime) = :date")
    int countOrdersToday(@Param("date") LocalDate date);


}

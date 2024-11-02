package com.business.booking_service.repository;

import com.business.booking_service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Integer> {
    List<Booking> findAll();

    List<Booking> findByStatus(String status);
}

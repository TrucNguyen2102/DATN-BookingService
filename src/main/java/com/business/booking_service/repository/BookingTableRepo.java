package com.business.booking_service.repository;

import com.business.booking_service.entity.BookingTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingTableRepo extends JpaRepository<BookingTable, Integer> {
}
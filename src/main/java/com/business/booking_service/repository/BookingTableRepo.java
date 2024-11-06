package com.business.booking_service.repository;

import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.entity.BookingTableId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingTableRepo extends JpaRepository<BookingTable, BookingTableId> {
    List<BookingTable> findByBookingId(Integer bookingId);
//    BookingTable findByBookingId(Integer bookingId);

    @Query("SELECT bt.tableId, COUNT(bt.tableId) AS count " +
            "FROM BookingTable bt " +
            "GROUP BY bt.tableId " +
            "ORDER BY count DESC")
    List<Object[]> findMostBookedTables();
}

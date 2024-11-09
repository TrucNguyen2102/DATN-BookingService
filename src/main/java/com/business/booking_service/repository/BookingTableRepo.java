package com.business.booking_service.repository;

import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.entity.BookingTableId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    //    @Query("SELECT bt.tableId FROM BookingTable bt WHERE bt.booking.id = :bookingId")
//    List<Integer> findTableIdsByBookingId(Integer bookingId);

    @Query("SELECT b.id.tableId FROM BookingTable b WHERE b.id.bookingId = :bookingId")
    List<Integer> findTableIdsByBookingId(@Param("bookingId") Integer bookingId);

    List<BookingTable> findByTableId(Integer tableId);

//    @Query("SELECT bt.tableId FROM BookingTable bt WHERE bt.booking.id = :bookingId")
//    Integer findTableIdByBookingId(@Param("bookingId") Integer bookingId);

    boolean existsByTableIdAndBooking_Status(Integer tableId, String status);
}

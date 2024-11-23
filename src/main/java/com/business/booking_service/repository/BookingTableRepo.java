package com.business.booking_service.repository;

import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.entity.BookingTableId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingTableRepo extends JpaRepository<BookingTable, BookingTableId> {
    List<BookingTable> findByBookingId(Integer bookingId);

    // Custom query để tìm theo bookingId
//    @Query("SELECT b FROM BookingTable b WHERE b.id.bookingId = :bookingId")
//    List<BookingTable> findTableIdByBookingId(@Param("bookingId") Integer bookingId);

    Optional<BookingTable> getByBookingId(Integer bookingId);


    @Query("SELECT bt.tableId, COUNT(bt.tableId) AS count " +
            "FROM BookingTable bt " +
            "GROUP BY bt.tableId " +
            "ORDER BY count DESC")
    List<Object[]> findMostBookedTables();


    @Query("SELECT b.id.tableId FROM BookingTable b WHERE b.id.bookingId = :bookingId")
    List<Integer> findTableIdsByBookingId(@Param("bookingId") Integer bookingId);

    List<BookingTable> findByTableId(Integer tableId);



    boolean existsByTableIdAndBooking_Status(Integer tableId, String status);

    BookingTable findByBookingIdAndTableId(Integer bookingId, Integer tableId);

    // Tìm kiếm với khóa composite BookingTableId
    Optional<BookingTable> findById(BookingTableId id);


    @Modifying
    @Query("UPDATE BookingTable bt SET bt.id.tableId = :tableId WHERE bt.id.bookingId = :bookingId")
    void updateTableId(@Param("bookingId") Integer bookingId, @Param("tableId") Integer tableId);

    @Modifying
    @Query("UPDATE BookingTable bt SET bt.tableId = :newTableId WHERE bt.id.bookingId = :bookingId AND bt.id.tableId = :oldTableId")
    void updateTableIds(@Param("bookingId") Integer bookingId,
                       @Param("oldTableId") Integer oldTableId,
                       @Param("newTableId") Integer newTableId);
}

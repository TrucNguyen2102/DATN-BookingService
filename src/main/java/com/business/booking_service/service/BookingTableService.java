package com.business.booking_service.service;

import com.business.booking_service.entity.BookingTable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingTableService {
//    BookingTable getBookingTableByBookingId(Integer bookingId);

   List<BookingTable> getTablesByBookingId(Integer bookingId);
//List<TablePlayDTO> getTablesByBookingId(Integer bookingId);
   List<Object[]> getMostBookedTables();

//   ResponseEntity<String> updateTableStatusByBookingId(Integer bookingId, String tableStatus);

   boolean checkTableConflict(Integer tableId, LocalDateTime requestedBookingTime, LocalDateTime requestedExpiryTime);
}

package com.business.booking_service.service;

import com.business.booking_service.dto.UpdateTableRequest;
import com.business.booking_service.dto.UpdateTablesRequest;
import com.business.booking_service.entity.BookingTable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingTableService {


   List<BookingTable> getTablesByBookingId(Integer bookingId);



   List<Object[]> getMostBookedTables();


   boolean checkTableConflict(Integer tableId, LocalDateTime requestedBookingTime, LocalDateTime requestedExpiryTime);

   ResponseEntity<String> updateBookingTable(UpdateTableRequest request);

   ResponseEntity<String> updateBookingTables(UpdateTablesRequest request);


   List<Integer> getTableIdsByBookingId(Integer bookingId);



}

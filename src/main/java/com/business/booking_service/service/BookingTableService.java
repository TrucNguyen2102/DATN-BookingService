package com.business.booking_service.service;

import com.business.booking_service.dto.TablePlayDTO;
import com.business.booking_service.entity.BookingTable;

import java.util.List;

public interface BookingTableService {
//    BookingTable getBookingTableByBookingId(Integer bookingId);

   List<BookingTable> getTablesByBookingId(Integer bookingId);
//List<TablePlayDTO> getTablesByBookingId(Integer bookingId);
   List<Object[]> getMostBookedTables();
}

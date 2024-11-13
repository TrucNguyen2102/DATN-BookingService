package com.business.booking_service.service;

import com.business.booking_service.dto.UpdateTableRequest;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.exception.ResourceNotFoundException;
import com.business.booking_service.repository.BookingRepo;
import com.business.booking_service.repository.BookingTableRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingTableServiceImpl implements BookingTableService{
    @Autowired
    private BookingTableRepo bookingTableRepo;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private RestTemplate restTemplate; // Inject RestTemplate

    @Autowired
    @Value("${tablePlayService}")  // Lấy URL từ application.properties
    private String tablePlayServiceUrl; // Địa chỉ URL của TablePlay Service


    public BookingTableServiceImpl(BookingTableRepo bookingTableRepo, RestTemplate restTemplate,
                                   @Value("${tablePlayService}") String tablePlayServiceUrl) {
        this.bookingTableRepo = bookingTableRepo;
        this.restTemplate = restTemplate;
        this.tablePlayServiceUrl = tablePlayServiceUrl;
    }

    //lấy ds bàn theo bookingId trong BookingTable
    public List<BookingTable> getTablesByBookingId(Integer bookingId) {
        return bookingTableRepo.findByBookingId(bookingId);
    }

    public List<Object[]> getMostBookedTables() {
        return bookingTableRepo.findMostBookedTables();
    }

    //kiểm tra xung đột bàn
    public boolean checkTableConflict(Integer tableId, LocalDateTime requestedBookingTime, LocalDateTime requestedExpiryTime) {
        List<BookingTable> bookedTables = bookingTableRepo.findByTableId(tableId);

        for (BookingTable bookingTable : bookedTables) {
            Booking existingBooking = bookingTable.getBooking();

            // Kiểm tra nếu trạng thái "Chờ Xác Nhận"
            if ("Chờ Xác Nhận".equals(existingBooking.getStatus())) {
                LocalDateTime existingBookingTime = existingBooking.getBookingTime();
                LocalDateTime existingExpiryTime = existingBooking.getExpiryTime();

                // Kiểm tra chồng lấn thời gian
                if (requestedBookingTime.isBefore(existingBookingTime) && requestedExpiryTime.isAfter(existingExpiryTime)) {
                    return true; // Có xung đột thời gian
                }
            }
        }
        return false; // Không có xung đột
    }


    //update tableId thành mới khi chuyển bàn
    @Transactional
    public ResponseEntity<String> updateBookingTable(UpdateTableRequest request) {
        try {
            // Lấy thông tin booking từ request
            BookingTable bookingTable = bookingTableRepo.getByBookingId(request.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking"));

            // Kiểm tra và cập nhật trạng thái bàn cũ thành 'Trống'
            if (bookingTable.getTableId() != null) {
                // Gửi yêu cầu PUT để cập nhật trạng thái bàn cũ thành 'Trống'
                restTemplate.put(tablePlayServiceUrl, new UpdateTableRequest(bookingTable.getTableId(), "Trống"));
            }

            // Cập nhật tableId trong BookingTable
            bookingTableRepo.updateTableId(request.getBookingId(), request.getTableId()); // Cập nhật tableId mới cho BookingTable

            // Cập nhật trạng thái bàn mới thành 'Đã Đặt'
            restTemplate.put(tablePlayServiceUrl, new UpdateTableRequest(request.getTableId(), "Đã Đặt"));

            return ResponseEntity.ok("Cập nhật bàn cho booking thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật thông tin booking.");
        }
    }


}

package com.business.booking_service.task;//package com.entertainment.booking_service.task;

import com.business.booking_service.dto.UpdateTableRequest;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.repository.BookingRepo;
import com.business.booking_service.repository.BookingTableRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingCleanupTask {
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private BookingTableRepo bookingTableRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Value("${tablePlayService}")  // Lấy URL từ application.properties
    private String tablePlayServiceUrl; // Địa chỉ URL của TablePlay Service

    public BookingCleanupTask() {

    }

    public BookingCleanupTask(BookingRepo bookingRepo, BookingTableRepo bookingTableRepo, RestTemplate restTemplate, @Value("${tablePlayService}") String tablePlayServiceUrl) {
        this.bookingRepo = bookingRepo;
        this.bookingTableRepo = bookingTableRepo;
        this.restTemplate = restTemplate;
        this.tablePlayServiceUrl = tablePlayServiceUrl;
    }

    @Scheduled(cron = "0 * * * * *") // Chạy mỗi phút
    public void cleanUpExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepo.findByStatus("Đã Xác Nhận");

        for (Booking booking : bookings) {
            LocalDateTime expiryTime = booking.getBookingTime().plusMinutes(15);
            if (now.isAfter(expiryTime)) {
                booking.setStatus("Đã Hủy");
                bookingRepo.save(booking);

            }

//            // Lấy `BookingTable` theo `bookingId` để lấy `tableId`
//            BookingTable bookingTable = bookingTableRepo.getByBookingId(booking.getId())
//                    .orElseThrow(() -> new RuntimeException("Không tìm thấy BookingTable cho bookingId: " + booking.getId()));
//
//            Integer tableId = bookingTable.getId().getTableId();
//
//            // Gửi yêu cầu PUT để cập nhật trạng thái của bàn thành "Trống"
//            UpdateTableRequest updateRequest = new UpdateTableRequest(tableId, "Trống");
//            restTemplate.put(tablePlayServiceUrl, updateRequest);
        }
    }
}

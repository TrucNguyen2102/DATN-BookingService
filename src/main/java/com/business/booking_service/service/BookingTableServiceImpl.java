package com.business.booking_service.service;

import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.repository.BookingTableRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingTableServiceImpl implements BookingTableService{
    @Autowired
    private BookingTableRepo bookingTableRepo;

//    @Autowired
//    private final RestTemplate restTemplate;
//
//
//    @Value("${tablePlayService.url}")
//    private String tablePlayServiceUrl;
//
//    public BookingTableServiceImpl(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }


//    public BookingTableServiceImpl(BookingTableRepo bookingTableRepo, RestTemplate restTemplate,
//                                   @Value("${table.service.url}") String tablePlayServiceUrl) {
//        this.bookingTableRepo = bookingTableRepo;
//        this.restTemplate = restTemplate;
//        this.tablePlayServiceUrl = tablePlayServiceUrl;
//    }


    public List<BookingTable> getTablesByBookingId(Integer bookingId) {
        return bookingTableRepo.findByBookingId(bookingId);
    }

    public List<Object[]> getMostBookedTables() {
        return bookingTableRepo.findMostBookedTables();
    }

    // Cập nhật trạng thái bàn theo bookingId
//    public ResponseEntity<String> updateTableStatusByBookingId(Integer bookingId, String tableStatus) {
//        try {
//            // Bước 1: Lấy danh sách tableId từ bảng booking_table theo bookingId
//            List<Integer> tableIds = bookingTableRepo.findTableIdsByBookingId(bookingId);
//            if (tableIds.isEmpty()) {
//                System.err.println("Không có bàn nào được gán cho bookingId: " + bookingId);
//                return ResponseEntity.badRequest().body("Không có bàn nào được gán cho booking này.");
//            }
//
//            // Bước 2: Gọi API của dịch vụ TablePlay để cập nhật trạng thái bàn
//            for (Integer tableId : tableIds) {
//                String url = tablePlayServiceUrl + "/update/" + tableId + "/status?table_status=" +
//                        URLEncoder.encode(tableStatus, StandardCharsets.UTF_8); // Mã hóa tham số table_status
//
//                // Gửi yêu cầu PUT tới dịch vụ TablePlay để cập nhật trạng thái bàn
//                try {
//                    System.out.println("Gửi yêu cầu PUT tới: " + url + " với trạng thái mới: " + tableStatus);
//                    restTemplate.put(url, null);  // Cập nhật trạng thái bàn, không cần body trong PUT request
//                    System.out.println("Cập nhật trạng thái bàn " + tableId + " thành " + tableStatus);
//                } catch (Exception e) {
////                    System.err.println("Lỗi khi cập nhật trạng thái bàn " + tableId + ": " + e.getMessage());
//                    e.printStackTrace();
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật trạng thái bàn.");
//                }
//            }
//
//            return ResponseEntity.ok("Trạng thái bàn đã được cập nhật thành công.");
//        } catch (Exception e) {
//            System.err.println("Lỗi khi xử lý yêu cầu cập nhật trạng thái bàn: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xử lý yêu cầu.");
//        }
//    }

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



}

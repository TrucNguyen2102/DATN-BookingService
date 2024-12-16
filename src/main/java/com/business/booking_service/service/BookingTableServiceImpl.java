package com.business.booking_service.service;

import com.business.booking_service.dto.UpdateTableRequest;
import com.business.booking_service.dto.UpdateTablesRequest;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.entity.BookingTableId;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public BookingTableServiceImpl(BookingTableRepo bookingTableRepo, BookingRepo bookingRepo, RestTemplate restTemplate, @Value("${tablePlayService}") String tablePlayServiceUrl) {
        this.bookingTableRepo = bookingTableRepo;
        this.bookingRepo = bookingRepo;
        this.restTemplate = restTemplate;
        this.tablePlayServiceUrl = tablePlayServiceUrl;
    }

    //lấy ds bàn theo bookingId trong BookingTable
    public List<BookingTable> getTablesByBookingId(Integer bookingId) {
        return bookingTableRepo.findByBookingId(bookingId);
    }


    public List<Object[]> getMostBookedTables(java.sql.Date date) {
        return bookingTableRepo.findMostBookedTables(date);
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


    //update tableId thành mới khi chuyển bàn (1 bàn)
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


    //nhiều bàn
    @Transactional
    public ResponseEntity<String> updateBookingTables(UpdateTablesRequest request) {
        try {
            // Kiểm tra dữ liệu đầu vào
            if (request.getBookingId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking ID không được để trống.");
            }
            if (request.getNewTableIds() == null || request.getNewTableIds().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Danh sách bàn mới không được để trống.");
            }

            // Kiểm tra tính đồng bộ của oldTableIds và newTableIds
            if (request.getOldTableIds().size() != request.getNewTableIds().size()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số lượng bàn cũ và bàn mới không khớp.");
            }

            // Duyệt qua từng bàn cũ và cập nhật với bàn mới tương ứng
            for (int i = 0; i < request.getOldTableIds().size(); i++) {
                Integer oldTableId = request.getOldTableIds().get(i);
                Integer newTableId = request.getNewTableIds().get(i);

                // Lấy bản ghi bàn cũ trong bảng BookingTable
                BookingTable bookingTable = bookingTableRepo.findByBookingIdAndTableId(
                        request.getBookingId(), oldTableId);

                if (bookingTable != null) {
                    // Cập nhật trạng thái của bàn cũ thành "Trống"
                    UpdateTableRequest updateOldTableRequest = new UpdateTableRequest(oldTableId, "Trống");
                    restTemplate.put(tablePlayServiceUrl, updateOldTableRequest);

                    // Cập nhật `tableId` của bàn mới vào bản ghi của bàn cũ
                    System.out.println("Trước khi cập nhật: " + bookingTable.getTableId());
                    bookingTable.setTableId(newTableId);
//                    bookingTableRepo.save(bookingTable);


                    // Cập nhật trạng thái của bàn mới thành "Đã Đặt"
                    UpdateTableRequest updateNewTableRequest = new UpdateTableRequest(newTableId, "Đã Đặt");
                    restTemplate.put(tablePlayServiceUrl, updateNewTableRequest);
                    // Cập nhật vào database bằng phương thức update trong repository
                    bookingTableRepo.updateTableIds(request.getBookingId(), oldTableId, newTableId);
                    System.out.println("Sau khi cập nhật: " + bookingTable.getTableId());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy bản ghi của bàn cũ.");
                }
            }

            // Trả về kết quả thành công
            return ResponseEntity.ok("Cập nhật bàn cho booking thành công!");

        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật thông tin booking: " + e.getMessage());
        }
    }

    public List<Integer> getTableIdsByBookingId(Integer bookingId) {
        try {
            return bookingTableRepo.findTableIdsByBookingId(bookingId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching table IDs by bookingId", e);
        }
    }


//    public List<BookingTable> getTableIdByBookingId(Integer bookingId) {
//        return bookingTableRepo.findTableIdByBookingId(bookingId);
//    }




//    public List<Integer> getTableIdsByBookingId(Integer bookingId) {
//        try {
//            // Giả sử bạn đang thực hiện truy vấn trong cơ sở dữ liệu
//            // Kiểm tra nếu bookingId không hợp lệ hoặc không tìm thấy dữ liệu
//            List<Integer> tableIds = bookingTableRepo.findTableIdsByBookingId(bookingId);
//            return tableIds != null ? tableIds : new ArrayList<>(); // Trả về danh sách trống nếu không có dữ liệu
//        } catch (Exception e) {
//            e.printStackTrace();  // Log chi tiết lỗi nếu có
//            throw new RuntimeException("Error fetching table IDs", e); // Ném lỗi lên
//        }
//    }

    public boolean isTableInBooking(Integer tableId) {
        return bookingTableRepo.existsByTableId(tableId);
    }






}

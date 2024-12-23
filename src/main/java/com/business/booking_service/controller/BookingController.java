package com.business.booking_service.controller;

import com.business.booking_service.dto.*;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.entity.BookingTableId;
import com.business.booking_service.exception.ResourceNotFoundException;
import com.business.booking_service.repository.BookingRepo;
import com.business.booking_service.repository.BookingTableRepo;
import com.business.booking_service.service.BookingService;
import com.business.booking_service.service.BookingTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingTableService bookingTableService;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private BookingTableRepo bookingTableRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tablePlayService_url}")
    private String tablePlayService_url;

    @Value("${userService_url}")
    private String userService_url;

    public BookingController(BookingService bookingService, BookingRepo bookingRepo, BookingTableRepo bookingTableRepo, RestTemplate restTemplate, @Value("${tablePlayService_url}") String tablePlayService_url, @Value("${userService_url}") String userService_url) {
        this.bookingService = bookingService;
        this.bookingRepo = bookingRepo;
        this.bookingTableRepo = bookingTableRepo;
        this.restTemplate = restTemplate;
        this.tablePlayService_url = tablePlayService_url;
        this.userService_url = userService_url;
    }

    @GetMapping("/endpoints")
    public List<Map<String, String>> getEndpoints() {
        return List.of(
                Map.of("service", "booking-service", "method", "POST", "url", "/api/bookings/add"),
                Map.of("service", "booking-service", "method", "POST", "url", "/api/bookings/direct"),
                Map.of("service", "booking-service", "method", "GET", "url", "/api/bookings/check-active"),
                Map.of("service", "booking-service", "method", "GET", "url", "/api/bookings/orders/today"),
                Map.of("service", "booking-service", "method", "GET", "url", "/api/bookings/booking_table/most-booked-tables"),
                Map.of("service", "booking-service", "method", "GET", "url", "/api/bookings/orders/count-tables"),
                Map.of("service", "booking-service", "method", "GET", "url", "/api/bookings/all"),
                Map.of("service", "booking-service", "method", "GET", "url", "/api/bookings/{id}/user"),
                Map.of("service", "booking-service", "method", "PUT", "url", "/api/bookings/update/{id}/status "),
                Map.of("service", "booking-service", "method", "GET", "url", "/api/bookings/booking_table/{id} "),
                Map.of("service", "booking-service", "method", "PUT", "url", "/api/bookings/booking_table/update-table-id "),
                Map.of("service", "booking-service", "method", "PUT", "url", "/api/bookings/booking_table/update-tables "),
                Map.of("service", "booking-service", "method", "DELETE", "url", "/api/bookings/booking_table/delete"),
                Map.of("service", "booking-service", "method", "PUT", "url", "/api/bookings/booking_table/update/{id}/status/paymentProcessing"),
                Map.of("service", "booking-service", "method", "PUT", "url", "/api/bookings/booking_table/update/{bookingId}/status"),
                Map.of("service", "booking-service", "method", "GET", "url", "/api/bookings/booking_table/check-table-used/{tableId}")
        );
    }

//    @PostMapping("/add")
//    public ResponseEntity<String> createBooking(@RequestBody BookingRequest bookingRequest) {
//        try {
//            bookingService.createBooking(bookingRequest);
//            return new ResponseEntity<>("Đặt bàn thành công!", HttpStatus.CREATED);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Có lỗi xảy ra khi đặt bàn.", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
    @PostMapping("/add")
    public ResponseEntity<String> createBooking(@RequestBody BookingRequest bookingRequest) {
        try {
            bookingService.createBooking(bookingRequest);
            return new ResponseEntity<>("Đặt bàn thành công!", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Có lỗi xảy ra khi đặt bàn.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check-active")
    public ResponseEntity<?> checkActiveBookings(@RequestParam Integer userId) {
        try {
            List<Booking> activeBookings = bookingService.findActiveBookingsByUserId(userId);
            if (!activeBookings.isEmpty()) {
                return ResponseEntity.ok(activeBookings);
            }
            return ResponseEntity.ok(Collections.emptyList());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    //khách vãng lai đến đặt trực tiếp
    @PostMapping("/direct")
    public ResponseEntity<?> createDirectBooking(@RequestBody GuestBookingRequest request) {
        try {
            // Gọi API để tạo khách vãng lai
            String userServiceCreateGuestUrl = userService_url + "/create-guest";
            GuestUserRequest guestUserRequest = new GuestUserRequest(request.getFullName(), request.getPhone());
            ResponseEntity<Integer> response = restTemplate.postForEntity(userServiceCreateGuestUrl, guestUserRequest, Integer.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Integer userId = response.getBody(); // Nhận lại userId của khách vãng lai
                LocalDateTime bookingTime = LocalDateTime.now();
                LocalDateTime expiryTime = bookingTime.plusMinutes(15);
                // Tạo Booking
                Booking booking = new Booking();
                booking.setBookingTime(bookingTime);  // Lấy thời gian hiện tại khi tạo booking
                booking.setExpiryTime(expiryTime);
                booking.setUserId(userId);  // Sử dụng userId của người dùng mới tạo
                booking.setStatus("Đã Xác Nhận");

                // Lưu Booking
                booking = bookingRepo.save(booking);

                // Lưu các bàn đã chọn vào BookingTable
                for (Integer tableId : request.getTableIds()) {
                    // Khởi tạo BookingTableId
                    BookingTableId bookingTableId = new BookingTableId();
                    bookingTableId.setBookingId(booking.getId());
                    bookingTableId.setTableId(tableId);

                    // Tạo BookingTable
                    BookingTable bookingTable = new BookingTable();
                    bookingTable.setId(bookingTableId); // Set BookingTableId cho BookingTable
                    bookingTable.setBooking(booking);   // Liên kết Booking với BookingTable
                    bookingTable.setTableId(tableId);   // Set tableId vào BookingTable

                    // Lưu vào bảng booking_table
                    bookingTableRepo.save(bookingTable);
                }

                return ResponseEntity.ok("Đặt bàn thành công");
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Không thể tạo khách vãng lai.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }






    @GetMapping("/all")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        try {
            List<BookingResponseDTO> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    // API lấy thông tin booking theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Integer id) {
        try {
            Optional<Booking> booking = bookingService.getBookingById(id);

            // Kiểm tra xem có tìm thấy booking hay không
            if (booking.isPresent()) {
                return ResponseEntity.ok(booking.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    //tìm user trong booking
    @GetMapping("/{id}/user")
    public ResponseEntity<Integer> getUserIdByBookingId(@PathVariable("id") Integer bookingId) {
        try {
            Integer userId = bookingService.getUserIdByBookingId(bookingId);
            return (userId != null) ? ResponseEntity.ok(userId) : ResponseEntity.notFound().build();
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping("/search")
    public ResponseEntity<List<Booking>> searchBookings(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String status) {
        try {
            // Log tham số nhận được từ frontend
            System.out.println("Tìm kiếm với tên: " + fullName + ", phone: " + phone + ", status: " + status);

            List<Booking> bookings = bookingService.searchBookings(fullName, phone, status);
            System.out.println("Bookings tìm được: " + bookings);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    //khi nhân viên xác nhận đơn
    @PutMapping("/update/{id}/status")
    public ResponseEntity<String> updateBookingStatus(@PathVariable Integer id, @RequestBody BookingStatusUpdateRequest request) {
        // Lấy thông tin booking hiện tại
        Optional<Booking> optionalCurrentBooking = bookingService.getBookingById(id);
        if (!optionalCurrentBooking.isPresent()) {
            return ResponseEntity.badRequest().body("Đơn đặt không tồn tại.");
        }

        Booking currentBooking = optionalCurrentBooking.get(); // Extract the Booking object

        boolean isUpdated = bookingService.updateBookingStatus(id, request.getStatus(), currentBooking.getBookingTime());
        if (isUpdated) {
            return ResponseEntity.ok("Trạng thái đơn đặt đã được cập nhật thành công.");
        } else {
            return ResponseEntity.badRequest().body("Cập nhật trạng thái không thành công.");
        }
    }


    //này là nút in hóa đơn
    //cập nhật trạng thái booking thành Đang Thanh Toán nếu bàn Đang Tiến Hành Thanh Toán hết, ngược lại là Chờ Thanh Toán
    @PutMapping("/booking_table/update/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Integer bookingId) {
        try {
            // Kiểm tra tất cả các bàn thuộc bookingId có trạng thái "Đang Chơi"
            boolean hasPlayingTable = bookingService.checkAnyTableIsPlaying(bookingId);

            if (hasPlayingTable) {
                // Nếu có bàn "Đang Chơi", không thay đổi trạng thái booking
                bookingService.updateBookingStatus(bookingId, "Đã Nhận Bàn");
                return ResponseEntity.ok("Trạng thái booking vẫn là 'Đã Nhận Bàn' do có bàn 'Đang Chơi'.");
            } else {
                // Kiểm tra tất cả các bàn thuộc bookingId có trạng thái "Đang Tiến Hành Thanh Toán" không
                boolean allTablesEmpty = bookingService.checkAllTablesArePaying(bookingId);

                // Nếu tất cả bàn đều "Đang Tiến Hành Thanh Toán", cập nhật trạng thái booking thành "Đang Thanh Toán"
                if (allTablesEmpty) {
                    bookingService.updateBookingStatus(bookingId, "Đang Thanh Toán");
                    return ResponseEntity.ok("Trạng thái booking đã được cập nhật thành công.");
                } else {
                    // Nếu có bàn chưa "Đang Tiến Hành Thanh Toán", cập nhật trạng thái booking thành "Chờ Thanh Toán"
                    bookingService.updateBookingStatus(bookingId, "Chờ Thanh Toán");
                    return ResponseEntity.ok("Trạng thái booking đã được cập nhật thành công thành 'Chờ Thanh Toán'.");
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi cập nhật trạng thái booking.");
        }
    }


    @PutMapping("/booking_table/update/{bookingId}/status/paying")
    public ResponseEntity<?> updateBookingStatusIsPaying(@PathVariable Integer bookingId) {
        try {
            // Kiểm tra tất cả các bàn thuộc bookingId có trạng thái "Đang Chơi"
            boolean hasPlayingTable = bookingService.checkAnyTableIsPlaying(bookingId);

            if (hasPlayingTable) {
                // Nếu có bàn "Đang Chơi", không thay đổi trạng thái booking
                bookingService.updateBookingStatus(bookingId, "Đã Nhận Bàn");
                return ResponseEntity.ok("Trạng thái booking vẫn là 'Đã Nhận Bàn' do có bàn 'Đang Chơi'.");
            } else {
                // Kiểm tra tất cả các bàn thuộc bookingId có trạng thái "Trống" không
                boolean allTablesEmpty = bookingService.checkAllTablesAreEmpty(bookingId);

                // Nếu tất cả bàn đều "Trống", cập nhật trạng thái booking thành "Đã Thanh Toán"
                if (allTablesEmpty) {
                    bookingService.updateBookingStatus(bookingId, "Đã Thanh Toán");
                    return ResponseEntity.ok("Trạng thái booking đã được cập nhật thành công.");
                } else {
                    // Nếu có bàn chưa "Trống", cập nhật trạng thái booking thành "Đang Thanh Toán"
                    bookingService.updateBookingStatus(bookingId, "Đang Thanh Toán");
                    return ResponseEntity.ok("Trạng thái booking đã được cập nhật thành công thành 'Chờ Thanh Toán'.");
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi cập nhật trạng thái booking.");
        }
    }

    //khi ấn lưu để tạo hóa
    //cập nhật trạng thái booking thành Chưa Thanh Toán nếu bàn đang xử lý thanh toán hết, ngược lại là Đã Nhận Bàn
    @PutMapping("/booking_table/update/{bookingId}/status/paymentProcessing")
    public ResponseEntity<?> updateBookingStatusIsPaymentProcessing(@PathVariable Integer bookingId) {
        try {
            // Kiểm tra tất cả các bàn thuộc bookingId có trạng thái "Đang Xử Lý Thanh Toán" không
            boolean allTablesPayment = bookingService.checkAllTablesArePaymentProcessing(bookingId);
            System.out.println("Kết quả kiểm tra tất cả bàn: " + allTablesPayment);

            // Nếu tất cả bàn đều "Đang Xử Lý Thanh Toán", cập nhật trạng thái booking thành "Chưa Thanh Toán"
            if (allTablesPayment) {
                bookingService.updateBookingStatus(bookingId, "Chưa Thanh Toán");
                System.out.println("Cập nhật bookingId: " + bookingId + " thành 'Chưa Thanh Toán'");
                return ResponseEntity.ok("Trạng thái booking đã được cập nhật thành công.");
            } else {
                // Nếu có bàn chưa "Đang Xử Lý Thanh Toán", cập nhật trạng thái booking thành "Đã Nhận Bàn"
                bookingService.updateBookingStatus(bookingId, "Đã Nhận Bàn");
                System.out.println("Cập nhật bookingId: " + bookingId + " thành 'Đã Nhận Bàn'");
                return ResponseEntity.ok("Trạng thái booking vẫn là Đã Nhận Bàn");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi cập nhật trạng thái booking.");
        }
    }






    //    @GetMapping("/history/{userId}")
//    public List<BookingResponseDTO> getUserBookingHistory(@PathVariable Integer userId) {
//        return bookingService.getUserBookingHistory(userId);
//    }
    @GetMapping("/history/{userId}")
    public Page<BookingResponseDTO> getUserBookingHistory(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,  // Trang mặc định là 0
            @RequestParam(defaultValue = "5") int size   // Số bản ghi mỗi trang là 5
    ) {
//        Pageable pageable = PageRequest.of(page, size);
//        return bookingService.getUserBookingHistory(userId, pageable);
        // Thêm sắp xếp theo bookingTime giảm dần
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookingTime"));
        return bookingService.getUserBookingHistory(userId, pageable);
    }

    // API lấy số đơn trong ngày
    @GetMapping("/orders/today")
    public ResponseEntity<Map<String, Integer>> getOrdersToday(@RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        int count = bookingService.getOrdersToday(date);
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // API lấy tổng số đơn theo khoảng thời gian
    @GetMapping("/orders/range")
    public ResponseEntity<Map<String, Object>> getTotalOrders(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            // Chuyển đổi từ String sang LocalDate
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // Chuyển startDate và endDate sang LocalDateTime để tính cả ngày
            LocalDateTime startDateTime = start.atStartOfDay(); // Từ 00:00:00 của ngày bắt đầu
            LocalDateTime endDateTime = end.atTime(23, 59, 59); // Đến 23:59:59 của ngày kết thúc

            // Đếm số lượng đơn đặt trong khoảng thời gian
            int count = bookingRepo.countByBookingTimeBetween(startDateTime, endDateTime);
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

//    @GetMapping("/orders/count-tables")
//    public ResponseEntity<?> countTablesByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//        try {
//            int count = bookingRepo.countTablesByDate(date);
//            return ResponseEntity.ok(Map.of("count", count));
//        }catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//
//    }

//    @GetMapping("/orders/count-tables/range")
//    public ResponseEntity<Map<String, Object>> countTablesByDateRange(
//            @RequestParam String startDate,
//            @RequestParam String endDate) {
//        try {
//            // Chuyển đổi từ String sang LocalDate
//            LocalDate start = LocalDate.parse(startDate);
//            LocalDate end = LocalDate.parse(endDate);
//
//
//            // Đếm số lượng đơn đặt trong khoảng thời gian
//            int count = bookingRepo.countTablesByDateRange(start, end);
//            Map<String, Object> response = new HashMap<>();
//            response.put("count", count);
//            return ResponseEntity.ok(response);
//        }catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//
//    }
    @GetMapping("/orders/count-tables/range")
    public ResponseEntity<?> countTablesByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Chuyển đổi startDate và endDate thành LocalDateTime
            LocalDateTime startDateTime = startDate.atStartOfDay(); // Từ 00:00:00 của ngày bắt đầu
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // Đến 23:59:59 của ngày kết thúc

            // Gọi repository để đếm số bàn
            int count = bookingRepo.countTablesByDateRange(startDateTime, endDateTime);

            // Trả về kết quả
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @GetMapping("/booking_table/{bookingId}")
    public ResponseEntity<List<BookingTable>> getTablesByBookingId(@PathVariable Integer bookingId) {
        System.out.println("Received bookingId: " + bookingId);
        List<BookingTable> tables = bookingTableService.getTablesByBookingId(bookingId);
        System.out.println("Returned tables: " + tables);
        System.out.println("Tables found: " + tables.size()); // Ghi log số lượng bàn chơi
        return new ResponseEntity<>(tables, HttpStatus.OK);
    }


    // Endpoint để lấy số bàn được đặt nhiều nhất
//    @GetMapping("/booking_table/most-booked-tables")
//    public ResponseEntity<?> getMostBookedTables(@RequestParam("date") String date) {
//        try {
//            // Chuyển đổi String -> java.sql.Date
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            java.util.Date parsedDate = sdf.parse(date);
//            java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
//
//            List<Object[]> mostBookedTables = bookingTableService.getMostBookedTables(sqlDate);
//
//            if (mostBookedTables == null || mostBookedTables.isEmpty()) {
//                return ResponseEntity.ok(Collections.singletonMap("message", "No bookings found for the specified date."));
//            }
//
//            // Tìm số lần đặt cao nhất
//            Long maxBookingCount = mostBookedTables.stream()
//                    .map(row -> (Long) row[1])
//                    .max(Long::compareTo)
//                    .orElse(0L);
//
//            // Lọc danh sách các bàn có số lần đặt cao nhất
//            List<Map<String, Object>> result = mostBookedTables.stream()
//                    .filter(row -> ((Long) row[1]).equals(maxBookingCount))
//                    .map(row -> Map.of("tableId", row[0], "bookingCount", row[1]))
//                    .collect(Collectors.toList());
//
//            return ResponseEntity.ok(result);
//
//        } catch (ParseException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Please use 'yyyy-MM-dd'.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching data.");
//        }
//    }

    @GetMapping("/booking_table/most-booked-tables/range")
    public ResponseEntity<?> getMostBookedTables(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            List<Object[]> mostBookedTables = bookingTableService.getMostBookedTables(startDate, endDate);

            if (mostBookedTables == null || mostBookedTables.isEmpty()) {
                return ResponseEntity.ok(Collections.singletonMap("message", "No bookings found for the specified range."));
            }

            // Tìm số lần đặt cao nhất
            Long maxBookingCount = mostBookedTables.stream()
                    .map(row -> ((Number) row[1]).longValue())
                    .max(Long::compareTo)
                    .orElse(0L);

            // Lọc danh sách các bàn có số lần đặt cao nhất
            List<Map<String, Object>> result = mostBookedTables.stream()
                    .filter(row -> ((Number) row[1]).longValue() == maxBookingCount)
                    .map(row -> Map.of("tableId", row[0], "bookingCount", row[1]))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);

        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Please use 'yyyy-MM-dd'.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching data.");
        }
    }




    @DeleteMapping("/booking_table/delete")
    public ResponseEntity<String> deleteBookingTable(@RequestParam Integer bookingId, @RequestParam Integer tableId) {
        try {
            // Tạo BookingTableId từ bookingId và tableId
            BookingTableId bookingTableId = new BookingTableId(bookingId, tableId);

            // Kiểm tra xem bản ghi có tồn tại không
            Optional<BookingTable> bookingTableOptional = bookingTableRepo.findById(bookingTableId);
            if (bookingTableOptional.isPresent()) {
                // Nếu bản ghi tồn tại, tiến hành xóa
                bookingTableRepo.deleteById(bookingTableId);
                return ResponseEntity.ok("Bàn đã được xóa khỏi đơn đặt bàn.");
            } else {
                // Nếu không tìm thấy bản ghi
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy bản ghi để xóa.");
            }
        } catch (Exception e) {
            // Xử lý lỗi
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa bản ghi.");
        }
    }


    @PutMapping("/booking_table/update-table-id")
    public ResponseEntity<String> updateBookingTable(@RequestBody UpdateTableRequest request) {
        try {
            return bookingTableService.updateBookingTable(request);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    @PutMapping("/booking_table/update-tables")
    public ResponseEntity<String> updateBookingTables(@RequestBody UpdateTablesRequest request) {
        try {
            return bookingTableService.updateBookingTables(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật thông tin booking.");
        }
    }


    // API để lấy danh sách tableId từ bookingId
    @GetMapping("/{bookingId}/tables")
    public ResponseEntity<List<Integer>> getTableIdByBookingId(@PathVariable Integer bookingId) {

        List<BookingTable> bookingTables = bookingTableRepo.findByBookingId(bookingId);

        if (bookingTables.isEmpty()) {
            return ResponseEntity.noContent().build(); // Không có bàn nào liên kết với booking
        }

        // Trích xuất danh sách tableId từ các bản ghi BookingTable
        List<Integer> tableIds = bookingTables.stream()
                .map(BookingTable::getTableId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(tableIds); // Trả về danh sách tableId
    }

    // API kiểm tra bàn có trong booking nào không trc khi xóa
//    @GetMapping("/booking_table/check-table-used/{tableId}")
//    public ResponseEntity<Boolean> isTableUsedInBooking(@PathVariable Integer tableId) {
//        try {
//            boolean isUsed = bookingTableService.isTableUsedInBooking(tableId);
//            return ResponseEntity.ok(isUsed); // Trả về true/false
//        }catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//
//    }

    @GetMapping("/booking_table/check-table-used/{tableId}")
   // @GetMapping("/exists/{tableId}")
    public ResponseEntity<Boolean> checkTableInBooking(@PathVariable("tableId")Integer tableId) {
        try {
            boolean exists = bookingTableService.isTableInBooking(tableId);
            return ResponseEntity.ok(exists);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }








}
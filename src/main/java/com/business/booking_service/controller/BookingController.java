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

    public BookingController(BookingService bookingService, BookingRepo bookingRepo, BookingTableRepo bookingTableRepo, RestTemplate restTemplate, @Value("${tablePlayService_url}") String tablePlayService_url) {
        this.bookingService = bookingService;
        this.bookingRepo = bookingRepo;
        this.bookingTableRepo = bookingTableRepo;
        this.restTemplate = restTemplate;
        this.tablePlayService_url = tablePlayService_url;
    }

    @GetMapping("/endpoints")
    public List<Map<String, String>> getEndpoints() {
        return List.of(
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
                Map.of("service", "booking-service", "method", "PUT", "url", "/api/bookings/booking_table/update/{bookingId}/status")
        );
    }

    @PostMapping("/add")
    public ResponseEntity<String> createBooking(@RequestBody BookingRequest bookingRequest) {
        try {
            bookingService.createBooking(bookingRequest);
            return new ResponseEntity<>("Đặt bàn thành công!", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Có lỗi xảy ra khi đặt bàn.", HttpStatus.INTERNAL_SERVER_ERROR);
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

    //cập nhật trạng thái của booking thành Đã Thanh Toán nếu các bàn là Trống
//    @PutMapping("/booking_table/update/{bookingId}/status")
//    public ResponseEntity<String> updateBookingStatusOfTables(@PathVariable Integer bookingId) {
//        try {
//            Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
//            if (bookingOpt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Đơn đặt không tồn tại.");
//            }
//
//            Booking booking = bookingOpt.get();
//
//            // Lấy danh sách BookingTable liên quan
//            List<BookingTable> bookingTables = bookingTableRepo.findByBookingId(bookingId);
//
//
//
//            // Kiểm tra tất cả các bàn có phải "Trống" không
//            boolean allTablesEmpty = bookingTables.stream().allMatch(bookingTable -> {
//                try {
//                    Integer tableId = bookingTable.getTableId();
//                    String tableStatus = restTemplate.getForObject(
//                            tablePlayService_url + "/" + tableId,
//                            String.class
//                    );
//
//                    System.out.println("Table " + tableId + " status: " + tableStatus);
//                    return "Trống".equals(tableStatus);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            });
//
//            // Cập nhật trạng thái Booking
//            if (allTablesEmpty) {
//                booking.setStatus("Đã Thanh Toán");
//            } else {
//                booking.setStatus("Chờ Thanh Toán");
//            }
//
//            bookingRepo.save(booking);
//            return ResponseEntity.ok("Trạng thái của đơn đặt đã được cập nhật.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

//    @PutMapping("/booking_table/update/{bookingId}/status")
//    public ResponseEntity<String> updateBookingStatusOfTables(@PathVariable Integer bookingId) {
//        try {
//            Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
//            if (bookingOpt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Đơn đặt không tồn tại.");
//            }
//
//            Booking booking = bookingOpt.get();
//
//            // Lấy danh sách BookingTable liên quan
//            List<BookingTable> bookingTables = bookingTableRepo.findByBookingId(bookingId);
//
//            // Cập nhật trạng thái bàn trực tiếp
//            for (BookingTable bookingTable : bookingTables) {
//                Integer tableId = bookingTable.getTableId();
//
//                // Lấy trạng thái bàn từ service khác
//                String tableStatus = restTemplate.getForObject(
//                        tablePlayService_url + "/" + tableId,
//                        String.class
//                );
//
//                // Kiểm tra trạng thái bàn và cập nhật nếu cần
//                System.out.println("Table " + tableId + " status: " + tableStatus);
//
//                if ("Đang Chơi".equals(tableStatus)) {
//                    // Cập nhật trạng thái bàn thành "Trống"
//                    restTemplate.put(tablePlayService_url + "/" + tableId + "/status", "Trống");
//                    System.out.println("Bàn " + tableId + " đã được cập nhật trạng thái 'Trống'.");
//                }
//            }
//
//            // Cập nhật trạng thái của Booking
//            boolean allTablesEmpty = bookingTables.stream().allMatch(bookingTable -> {
//                Integer tableId = bookingTable.getTableId();
//                String tableStatus = restTemplate.getForObject(
//                        tablePlayService_url + "/" + tableId,
//                        String.class
//                );
//                return "Trống".equals(tableStatus);
//            });
//
//            // Cập nhật trạng thái Booking
//            if (allTablesEmpty) {
//                booking.setStatus("Đã Thanh Toán");
//            } else {
//                booking.setStatus("Chờ Thanh Toán");
//            }
//
//            bookingRepo.save(booking);
//            return ResponseEntity.ok("Trạng thái của đơn đặt đã được cập nhật.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    //cập nhật trạng thái booking thành Đã Thanh Toán nếu bàn trống hết, ngược lại là Chờ Thanh Toán
    @PutMapping("/booking_table/update/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Integer bookingId) {
        try {
            // Kiểm tra tất cả các bàn thuộc bookingId có trạng thái "Trống" không
            boolean allTablesEmpty = bookingService.checkAllTablesAreEmpty(bookingId);

            // Nếu tất cả bàn đều "Trống", cập nhật trạng thái booking thành "Đã Thanh Toán"
            if (allTablesEmpty) {
                bookingService.updateBookingStatus(bookingId, "Đã Thanh Toán");
                return ResponseEntity.ok("Trạng thái booking đã được cập nhật thành công.");
            } else {
                // Nếu có bàn chưa "Trống", cập nhật trạng thái booking thành "Chờ Thanh Toán"
                bookingService.updateBookingStatus(bookingId, "Chờ Thanh Toán");
                return ResponseEntity.ok("Trạng thái booking đã được cập nhật thành công thành 'Chờ Thanh Toán'.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi cập nhật trạng thái booking.");
        }
    }

    //cập nhật trạng thái booking thành Chưa Thanh Toán nếu bàn đang xử lý thanh toán hết, ngược lại là Đã Nhận Bàn
    @PutMapping("/booking_table/update/{bookingId}/status/paymentProcessing")
    public ResponseEntity<?> updateBookingStatusIsPaymentProcessing(@PathVariable Integer bookingId) {
        try {
            // Kiểm tra tất cả các bàn thuộc bookingId có trạng thái "Trống" không
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
        Pageable pageable = PageRequest.of(page, size);
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

//    @PutMapping("/update/{id}/status")
//    public ResponseEntity<String> updateBookingAndTableStatus(@PathVariable Integer id, @RequestBody TableStatusUpdateRequest request) {
//        boolean isUpdated = bookingService.updateTableStatusThroughBooking(id, request.getStatus());
//        if (isUpdated) {
//            return ResponseEntity.ok("Trạng thái bàn đã được cập nhật thành công.");
//        } else {
//            return ResponseEntity.badRequest().body("Cập nhật trạng thái bàn không thành công.");
//        }
//    }

//    @PutMapping("/update/{id}")
//    public ResponseEntity<String> updateBooking(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
//        try {
//
//        }catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }


    @GetMapping("/orders/count-tables")
    public ResponseEntity<?> countTablesByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            int count = bookingRepo.countTablesByDate(date);
            return ResponseEntity.ok(Map.of("count", count));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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
    @GetMapping("/booking_table/most-booked-tables")
    public ResponseEntity<?> getMostBookedTables(@RequestParam("date") String date) {
        try {
            // Chuyển đổi String -> java.sql.Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = sdf.parse(date);
            java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());

            List<Object[]> mostBookedTables = bookingTableService.getMostBookedTables(sqlDate);

            if (mostBookedTables == null || mostBookedTables.isEmpty()) {
                return ResponseEntity.ok(Collections.singletonMap("message", "No bookings found for the specified date."));
            }

            // Tìm số lần đặt cao nhất
            Long maxBookingCount = mostBookedTables.stream()
                    .map(row -> (Long) row[1])
                    .max(Long::compareTo)
                    .orElse(0L);

            // Lọc danh sách các bàn có số lần đặt cao nhất
            List<Map<String, Object>> result = mostBookedTables.stream()
                    .filter(row -> ((Long) row[1]).equals(maxBookingCount))
                    .map(row -> Map.of("tableId", row[0], "bookingCount", row[1]))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);

        } catch (ParseException e) {
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

    // Endpoint to get tables by booking ID
//    @GetMapping("/booking_table/{bookingId}")
//    public ResponseEntity<?> getTableIdByBookingId(@PathVariable Integer bookingId) {
//        try {
//            System.out.println("Received bookingId: " + bookingId);
//
//            // Lấy danh sách BookingTable liên kết với bookingId
//            List<BookingTable> tables = bookingTableService.getTablesByBookingId(bookingId);
//
//            // Kiểm tra nếu không tìm thấy bàn nào
//            if (tables.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tables found for booking ID: " + bookingId);
//            }
//
//            // Lấy danh sách tableId từ BookingTable
//            List<Integer> tableIds = tables.stream()
//                    .map(table -> table.getId().getTableId())  // Truy xuất tableId từ BookingTableId
//                    .collect(Collectors.toList());
//
//            System.out.println("Tables found: " + tableIds.size());  // Log số lượng bàn
//            return new ResponseEntity<>(tableIds, HttpStatus.OK);
//
//        } catch (Exception e) {
//            e.printStackTrace();  // Log chi tiết lỗi
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
//        }
//    }


//    @GetMapping("/booking_table/{bookingId}")
//    public ResponseEntity<?> getTableIdByBookingId(@PathVariable Integer bookingId) {
//        try {
//            System.out.println("Received bookingId: " + bookingId);
//            List<BookingTable> tables = bookingTableService.getTablesByBookingId(bookingId);
//
//            // Kiểm tra nếu không tìm thấy bàn nào
//            if (tables.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tables found for booking ID: " + bookingId);
//            }
//
//            System.out.println("Returned tables: " + tables);
//            System.out.println("Tables found: " + tables.size());  // Log số lượng bàn chơi
//            return new ResponseEntity<>(tables, HttpStatus.OK);
//
//        } catch (Exception e) {
//            e.printStackTrace();  // Log chi tiết lỗi
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
//        }
//    }

//    @GetMapping("/booking_table/{bookingId}/tables")
//    public ResponseEntity<List<Integer>> getTableIdsByBookingId(@PathVariable Integer bookingId) {
//        try {
//            System.out.println("Received bookingId: " + bookingId);
//            List<Integer> tableIds = bookingTableService.getTableIdsByBookingId(bookingId);
//            return ResponseEntity.ok(tableIds);
//        } catch (Exception e) {
//            e.printStackTrace();  // In ra chi tiết lỗi
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }


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








}
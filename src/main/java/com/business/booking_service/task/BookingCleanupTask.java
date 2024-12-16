package com.business.booking_service.task;//package com.entertainment.booking_service.task;

import com.business.booking_service.dto.NotificationDTO;
import com.business.booking_service.dto.TableUpdateStatus;
import com.business.booking_service.dto.UpdateTableRequest;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.repository.BookingRepo;
import com.business.booking_service.repository.BookingTableRepo;
import com.business.booking_service.service.BookingService;
import com.business.booking_service.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BookingCleanupTask {
    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingTableRepo bookingTableRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Value("${tablePlayService_url}")  // Lấy URL từ application.properties
    private String tablePlayServiceUrl; // Địa chỉ URL của TablePlay Service

    @Autowired
    @Value("${userService_url}")
    private String userServiceUrl;

    @Value("${notificationService_url}")
    private String NOTIFICATION_SERVICE_URL;

    @Autowired
    private EmailService emailService;

    public BookingCleanupTask() {

    }

    public BookingCleanupTask(BookingRepo bookingRepo, BookingService bookingService, BookingTableRepo bookingTableRepo, RestTemplate restTemplate, @Value("${tablePlayService_url}") String tablePlayServiceUrl, @Value("${userService_url}") String userServiceUrl,  @Value("${notificationService_url}") String NOTIFICATION_SERVICE_URL, EmailService emailService) {
        this.bookingRepo = bookingRepo;
        this.bookingService = bookingService;
        this.bookingTableRepo = bookingTableRepo;
        this.restTemplate = restTemplate;
        this.tablePlayServiceUrl = tablePlayServiceUrl;
        this.userServiceUrl = userServiceUrl;
        this.NOTIFICATION_SERVICE_URL = NOTIFICATION_SERVICE_URL;
        this.emailService = emailService;
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

                // Lấy danh sách các BookingTable liên kết với booking để tìm bàn liên quan
                List<BookingTable> bookingTables = bookingTableRepo.findByBooking(booking);

                for (BookingTable bookingTable : bookingTables) {
                    Integer tableId = bookingTable.getTableId();

                    // Gửi yêu cầu đến Service Table để cập nhật trạng thái bàn
                    String url = tablePlayServiceUrl + "/" + tableId + "/status";
                    try {
                        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(new TableUpdateStatus("Trống")), Void.class);
                    } catch (RestClientException e) {
                        // Xử lý lỗi khi không thể gửi yêu cầu đến Service Table
                        e.printStackTrace();
                    }
                }

                sendCancellationEmail(booking);

            }

        }


    }



    // Hàm gửi email thông báo hủy đơn
    private void sendCancellationEmail(Booking booking) {

        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setContent(
                "Hệ thống tự động hủy đơn đặt bàn do đã hết thời gian giữ bàn.");
        notificationDTO.setNotificationType("AUTO_CANCELLATION");
        notificationDTO.setSendAt(LocalDateTime.now());
        notificationDTO.setBookingId(booking.getId());

        Integer userId = bookingService.getUserIdByBookingId(booking.getId());



        if (userId != null) {
            // Lấy email từ User Service
            String user_url = userServiceUrl + "/" + userId + "/email";
            String email = restTemplate.getForObject(user_url, String.class);

            if (email != null) {

                String subject = "TỰ ĐỘNG HỦY";
                String body = "Hệ thống tự động hủy đơn đặt bàn do đã hết thời gian giữ bàn.\n\n" +
                        "Nếu bạn muốn đặt bàn lại, vui lòng thực hiện đặt lại trên trang web của chúng tôi.\n\n" +
                        "Xin lỗi vì sự bất tiện này và cảm ơn bạn đã hiểu.";



                emailService.sendEmail(email, subject, body); // Gửi email
            }
        }

        // Gửi yêu cầu tới Notification Service
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(NOTIFICATION_SERVICE_URL, notificationDTO, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Notification sent successfully");
            } else {
                System.out.println("Failed to send notification");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending notification");
        }
    }
}

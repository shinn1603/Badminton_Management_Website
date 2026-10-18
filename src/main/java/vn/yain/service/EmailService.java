package vn.yain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.yain.entity.Booking;

/**
 * Dependency tối thiểu phục vụ BookingService Ngày 3.
 * Triết sẽ tích hợp hoàn chỉnh phân hệ EmailService (gửi vé, mã OTP, xác nhận giải đấu) vào Ngày 7 theo kế hoạch.
 */
@Service
@Slf4j
public class EmailService {

    public void sendBookingConfirmation(Booking booking) {
        log.info("[EMAIL SERVICE STUB] Đã ghi nhận yêu cầu gửi vé đặt sân cho đơn [{}]",
                booking != null ? booking.getBookingCode() : "N/A");
    }
}

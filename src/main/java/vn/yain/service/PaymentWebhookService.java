package vn.yain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.yain.entity.Booking;
import vn.yain.entity.Payment;
import vn.yain.repository.BookingRepository;
import vn.yain.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service xử lý Webhook thanh toán VietQR / Sepay / Casso (Bảo đảm Idempotency & Đối soát giao dịch)
 */
@Service
@Transactional
@Slf4j
public class PaymentWebhookService {

    @Value("${vietqr.webhook.token:UTE_VIETQR_SECRET_TOKEN_2026}")
    private String webhookToken;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final Pattern BOOKING_CODE_PATTERN = Pattern.compile("(DS|BK)\\d{6}", Pattern.CASE_INSENSITIVE);

    /**
     * Xác thực chữ ký / mã token bảo mật của Webhook
     */
    public boolean authorizeWebhook(String secretHeader, String authHeader) {
        if (webhookToken == null || webhookToken.isBlank()) {
            return true;
        }
        return (secretHeader != null && secretHeader.trim().equals(webhookToken)) ||
               (authHeader != null && authHeader.contains(webhookToken));
    }

    /**
     * Xử lý webhook thanh toán VietQR với cơ chế chống xử lý lặp (Idempotent)
     */
    public Map<String, Object> processVietQRWebhook(String secretHeader, String authHeader, Map<String, Object> webhookData) {
        if (!authorizeWebhook(secretHeader, authHeader)) {
            log.warn("Từ chối Webhook VietQR: Mã xác thực token không hợp lệ!");
            return Map.of("success", false, "error", "Unauthorized webhook caller");
        }

        if (webhookData == null || webhookData.isEmpty()) {
            return Map.of("success", false, "error", "Empty webhook payload");
        }

        String content = (String) webhookData.getOrDefault("content", "");
        if (content == null || content.isBlank()) {
            content = (String) webhookData.getOrDefault("description", "");
        }
        if (content == null) content = "";

        // Trích xuất mã giao dịch từ webhook provider (nếu có)
        String referenceCode = webhookData.get("referenceCode") != null ? webhookData.get("referenceCode").toString() : null;
        if (referenceCode == null && webhookData.get("id") != null) {
            referenceCode = "SEPAY-" + webhookData.get("id").toString();
        }

        // Trích xuất số tiền chuyển khoản
        BigDecimal transferAmount = null;
        Object rawAmount = webhookData.get("transferAmount");
        if (rawAmount == null) rawAmount = webhookData.get("amount");
        if (rawAmount == null) rawAmount = webhookData.get("paymentAmount");
        if (rawAmount != null) {
            try {
                transferAmount = new BigDecimal(rawAmount.toString());
            } catch (Exception ex) {
                log.warn("Không thể chuyển đổi số tiền chuyển khoản: {}", rawAmount);
            }
        }

        // Tìm mã đặt sân trong nội dung chuyển khoản
        Matcher matcher = BOOKING_CODE_PATTERN.matcher(content);
        if (!matcher.find()) {
            // Thử kiểm tra trực tiếp trong payload nếu có trường bookingCode
            String directCode = (String) webhookData.get("bookingCode");
            if (directCode != null && !directCode.isBlank()) {
                matcher = BOOKING_CODE_PATTERN.matcher(directCode);
                if (!matcher.find()) {
                    log.info("Webhook nhận thành công nhưng không tìm thấy mã đơn đặt sân: {}", content);
                    return Map.of("success", true, "message", "Webhook received successfully: No booking code recognized");
                }
            } else {
                log.info("Webhook nhận thành công nhưng không có mã đơn đặt sân hợp lệ: {}", content);
                return Map.of("success", true, "message", "Webhook received successfully: No booking code found");
            }
        }

        String bookingCode = matcher.group(0).toUpperCase();
        Optional<Booking> opt = bookingRepository.findByBookingCode(bookingCode);
        if (opt.isEmpty()) {
            log.warn("Không tìm thấy đơn đặt sân [{}] trong CSDL từ Webhook", bookingCode);
            return Map.of("success", true, "message", "Booking " + bookingCode + " not found in database");
        }

        Booking booking = opt.get();

        // 1. CHỐNG XỬ LÝ LẶP (IDEMPOTENCY CHECK)
        // Nếu đơn đã cọc hoặc đã hoàn thành, bỏ qua việc ghi nhận lại tránh nhân đôi tiền và tích lũy điểm
        if ("Đã cọc 30%".equalsIgnoreCase(booking.getStatus()) ||
            "Đang sử dụng".equalsIgnoreCase(booking.getStatus()) ||
            "Hoàn thành".equalsIgnoreCase(booking.getStatus())) {
            log.info("Bỏ qua thông báo webhook lặp: Đơn [{}] đã ở trạng thái [{}]", bookingCode, booking.getStatus());
            return Map.of(
                    "success", true,
                    "duplicate", true,
                    "bookingCode", bookingCode,
                    "status", booking.getStatus(),
                    "message", "Duplicate webhook ignored: booking already confirmed"
            );
        }

        if ("Đã hủy".equalsIgnoreCase(booking.getStatus())) {
            log.warn("Đơn [{}] đã bị hủy, không thể tự động xác nhận cọc qua webhook", bookingCode);
            return Map.of("success", false, "message", "Booking is already cancelled: " + bookingCode);
        }

        // 2. ĐỐI SOÁT SỐ TIỀN THANH TOÁN (RECONCILIATION)
        BigDecimal expectedDeposit = booking.getDepositAmount() != null && booking.getDepositAmount().compareTo(BigDecimal.ZERO) > 0
                ? booking.getDepositAmount()
                : (booking.getTotalPrice() != null ? booking.getTotalPrice().multiply(new BigDecimal("0.3")) : BigDecimal.ZERO);

        if (transferAmount != null && transferAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (transferAmount.compareTo(expectedDeposit) < 0) {
                log.warn("Cảnh báo đối soát: Số tiền chuyển [{}] nhỏ hơn tiền cọc yêu cầu [{}] cho đơn [{}]",
                        transferAmount, expectedDeposit, bookingCode);
            }
        }

        String txRef = referenceCode != null ? referenceCode : "SEPAY-" + bookingCode;

        // Kiểm tra xem giao dịch thanh toán txRef đã tồn tại chưa
        if (paymentRepository.findByBookingCode(bookingCode).stream()
                .anyMatch(p -> txRef.equalsIgnoreCase(p.getTransactionRef()))) {
            log.info("Giao dịch tham chiếu [{}] đã tồn tại, tránh ghi nhận giao dịch trùng lặp", txRef);
            return Map.of("success", true, "duplicate", true, "message", "Transaction reference already recorded");
        }

        // 3. XÁC NHẬN CỌC & GHI NHẬN THANH TOÁN
        BigDecimal effectiveDeposit = transferAmount != null ? transferAmount : expectedDeposit;
        Booking updated = bookingService.confirmDeposit(bookingCode, effectiveDeposit, "Chuyển khoản VietQR", txRef);

        log.info("Tự động xác nhận cọc thành công qua VietQR Webhook cho đơn [{}], số tiền: {} đ", bookingCode, effectiveDeposit);

        return Map.of(
                "success", true,
                "bookingCode", bookingCode,
                "depositAmount", effectiveDeposit,
                "status", updated.getStatus(),
                "message", "Auto-confirmed deposit for " + bookingCode
        );
    }
}

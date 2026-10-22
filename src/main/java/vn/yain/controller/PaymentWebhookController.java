package vn.yain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.yain.entity.Booking;
import vn.yain.service.BookingService;
import vn.yain.service.PaymentWebhookService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller xử lý Webhook thanh toán VietQR và xác nhận cọc trực tuyến (Day 7)
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentWebhookController {

    @Autowired
    private PaymentWebhookService paymentWebhookService;

    @Autowired
    private BookingService bookingService;

    /**
     * Endpoint nhận Webhook từ cổng thanh toán VietQR / Sepay / Casso
     */
    @PostMapping({"/api/webhook/vietqr", "/webhook/vietqr"})
    public ResponseEntity<?> handleVietQRWebhook(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secretHeader,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> webhookData) {

        Map<String, Object> result = paymentWebhookService.processVietQRWebhook(secretHeader, authHeader, webhookData);
        if (Boolean.FALSE.equals(result.get("success")) && "Unauthorized webhook caller".equals(result.get("error"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint khách hàng hoặc thu ngân xác nhận đã thanh toán tiền cọc 30%
     */
    @PostMapping("/api/bookings/{code}/deposit")
    public ResponseEntity<?> confirmDeposit(@PathVariable String code, @RequestBody(required = false) Map<String, Object> payload) {
        try {
            BigDecimal amount = null;
            String method = "VietQR Pro";
            String txRef = "VQR-" + System.currentTimeMillis() % 1000000;

            if (payload != null) {
                if (payload.get("amount") != null) {
                    amount = new BigDecimal(payload.get("amount").toString());
                }
                if (payload.get("paymentMethod") != null) {
                    method = payload.get("paymentMethod").toString();
                }
                if (payload.get("txRef") != null) {
                    txRef = payload.get("txRef").toString();
                }
            }

            Booking updated = bookingService.confirmDeposit(code, amount, method, txRef);
            return ResponseEntity.ok(Map.of("success", true, "booking", updated));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi xử lý xác nhận cọc: " + e.getMessage()));
        }
    }
}

package vn.yain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.yain.entity.Booking;
import vn.yain.entity.Payment;
import vn.yain.repository.BookingRepository;
import vn.yain.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class PaymentWebhookServiceTest {

    @Autowired
    private PaymentWebhookService paymentWebhookService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Booking createTestBooking(String code, String customerName, String courtCode) {
        Booking b = new Booking();
        b.setBookingCode(code);
        b.setBranchCode("CN01");
        b.setCourtCode(courtCode);
        b.setCustomerName(customerName);
        b.setCustomerPhone("0901234567");
        b.setBookingDate(LocalDate.now().plusDays(2));
        b.setTimeSlot("17:00 - 18:00");
        b.setHourlyPrice(new BigDecimal("100000"));
        b.setTotalPrice(new BigDecimal("100000"));
        b.setDepositAmount(new BigDecimal("30000"));
        b.setStatus("Chờ cọc 30%");
        return bookingRepository.save(b);
    }

    @Test
    @DisplayName("Webhook XL-03: Tu dong xac nhan coc 30% khi nhan webhook hop le tu VietQR")
    void testProcessVietQRWebhook_Success() {
        String code = "DS" + (300000 + (int)(Math.random() * 600000));
        createTestBooking(code, "Khách Hàng Test", "CL01");

        Map<String, Object> webhookData = new HashMap<>();
        webhookData.put("content", "CK " + code + " TIEN COC SAN 30%");
        webhookData.put("transferAmount", 30000);
        webhookData.put("referenceCode", "SEPAY-TEST-" + code);

        Map<String, Object> result = paymentWebhookService.processVietQRWebhook(
                "UTE_VIETQR_SECRET_TOKEN_2026", null, webhookData);

        assertTrue((boolean) result.get("success"));
        assertEquals(code, result.get("bookingCode"));

        Booking updated = bookingRepository.findByBookingCode(code).orElseThrow();
        assertEquals("Đã cọc 30%", updated.getStatus());

        List<Payment> payments = paymentRepository.findByBookingCode(code);
        assertFalse(payments.isEmpty());
        assertEquals("Đặt cọc 30%", payments.get(0).getTransactionType());
    }

    @Test
    @DisplayName("Webhook XL-03: Co che Idempotent - Bo qua xu ly khi webhook bi gui lap lai")
    void testProcessVietQRWebhook_Idempotent_DuplicateIgnored() {
        String code = "DS" + (300000 + (int)(Math.random() * 600000));
        createTestBooking(code, "Khách Hàng Duplicate", "CL01");

        Map<String, Object> webhookData = new HashMap<>();
        webhookData.put("content", "CK " + code + " TIEN COC SAN 30%");
        webhookData.put("transferAmount", 30000);
        webhookData.put("referenceCode", "TX-DUP-" + code);

        Map<String, Object> res1 = paymentWebhookService.processVietQRWebhook(
                "UTE_VIETQR_SECRET_TOKEN_2026", null, webhookData);
        assertTrue((boolean) res1.get("success"));
        assertNull(res1.get("duplicate"));

        Map<String, Object> res2 = paymentWebhookService.processVietQRWebhook(
                "UTE_VIETQR_SECRET_TOKEN_2026", null, webhookData);
        assertTrue((boolean) res2.get("success"));
        assertTrue((boolean) res2.get("duplicate"));
        assertEquals("Duplicate webhook ignored: booking already confirmed", res2.get("message"));

        List<Payment> payments = paymentRepository.findByBookingCode(code);
        assertEquals(1, payments.size());
    }

    @Test
    @DisplayName("Webhook XL-03: Tu choi caller neu sai webhook secret token")
    void testProcessVietQRWebhook_Unauthorized() {
        Map<String, Object> webhookData = Map.of("content", "CK DS123456", "transferAmount", 30000);

        Map<String, Object> result = paymentWebhookService.processVietQRWebhook(
                "WRONG_TOKEN", null, webhookData);

        assertFalse((boolean) result.get("success"));
        assertEquals("Unauthorized webhook caller", result.get("error"));
    }

    @Test
    @DisplayName("MockMvc API: POST /api/webhook/vietqr hoat dong dung spec va doi trang thai CSDL")
    void testMockMvcApiWebhook() throws Exception {
        String code = "DS" + (300000 + (int)(Math.random() * 600000));
        createTestBooking(code, "Khách Hàng MVC", "CL02");

        Map<String, Object> payload = Map.of(
                "content", "CK " + code + " THANH TOAN COC",
                "transferAmount", 30000
        );

        mockMvc.perform(post("/api/webhook/vietqr")
                        .header("X-Webhook-Secret", "UTE_VIETQR_SECRET_TOKEN_2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.bookingCode", is(code)));

        Booking updated = bookingRepository.findByBookingCode(code).orElseThrow();
        assertEquals("Đã cọc 30%", updated.getStatus());
    }

    @Test
    @DisplayName("MockMvc API: POST /api/bookings/{code}/deposit xac nhan coc truc tiep tu giao dien payment.html")
    void testMockMvcApiBookingDeposit() throws Exception {
        String code = "DS" + (300000 + (int)(Math.random() * 600000));
        createTestBooking(code, "Khách Hàng Payment View", "CL03");

        Map<String, Object> payload = Map.of(
                "amount", 30000,
                "paymentMethod", "VietQR Pro",
                "txRef", "VQR-TEST-" + code
        );

        mockMvc.perform(post("/api/bookings/" + code + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.booking.status", is("Đã cọc 30%")));
    }
}

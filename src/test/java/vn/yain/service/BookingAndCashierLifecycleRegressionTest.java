package vn.yain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.yain.entity.*;
import vn.yain.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử hồi quy toàn diện chu trình nghiệp vụ (Thọ - Ngày 8):
 * Đặt sân -> Khóa slot -> Thanh toán cọc 30% -> Check-in -> Gọi dịch vụ POS trừ kho ->
 * Chuyển sân -> Check-out thanh toán nốt xuất hóa đơn -> Chốt ca thu ngân.
 */
@SpringBootTest
@Transactional
class BookingAndCashierLifecycleRegressionTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private PaymentWebhookService paymentWebhookService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Court court1;
    private Court court2;
    private Product drink;

    @BeforeEach
    void setUp() {
        court1 = courtRepository.findByCourtCode("CL_TEST_A").orElseGet(() -> {
            Court c = new Court();
            c.setCourtCode("CL_TEST_A");
            c.setCourtName("Sân Test A");
            c.setCourtType("Tiêu chuẩn");
            c.setBranchCode("CN01");
            c.setStatus("Trống");
            c.setHourlyRate(new BigDecimal("100000"));
            return courtRepository.save(c);
        });

        court2 = courtRepository.findByCourtCode("CL_TEST_B").orElseGet(() -> {
            Court c = new Court();
            c.setCourtCode("CL_TEST_B");
            c.setCourtName("Sân Test B");
            c.setCourtType("VIP");
            c.setBranchCode("CN01");
            c.setStatus("Trống");
            c.setHourlyRate(new BigDecimal("120000"));
            return courtRepository.save(c);
        });

        drink = productRepository.findByProductCode("SP_TEST_WATER").orElseGet(() -> {
            Product p = new Product();
            p.setProductCode("SP_TEST_WATER");
            p.setProductName("Nước Khoáng Test 500ml");
            p.setCategory("Nước uống");
            p.setUnit("Chai");
            p.setUnitPrice(new BigDecimal("15000"));
            p.setCostPrice(new BigDecimal("8000"));
            p.setStockQuantity(50);
            p.setMinQuantity(5);
            p.setBranchCode("CN01");
            return productRepository.save(p);
        });
    }

    @Test
    @DisplayName("E2E Regression: Chu trình toàn vẹn từ đặt sân, cọc VietQR, gọi đồ POS, chuyển sân đến trả sân chốt ca")
    void testEndToEndBookingAndCashierLifecycle() {
        String bookingCode = "DS" + (400000 + (int)(Math.random() * 500000));
        LocalDate today = LocalDate.now().plusDays(3);
        String slot = "18:00 - 19:00";

        // 1. Khách hàng tạo đơn đặt sân
        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setBranchCode("CN01");
        booking.setCourtCode(court1.getCourtCode());
        booking.setCustomerName("Lê Văn Thử Nghiệm");
        booking.setCustomerPhone("0912345678");
        booking.setBookingDate(today);
        booking.setTimeSlot(slot);
        booking.setHourlyPrice(new BigDecimal("100000"));
        booking.setTotalPrice(new BigDecimal("100000"));
        booking.setDepositAmount(new BigDecimal("30000"));
        booking.setStatus("Chờ cọc 30%");
        bookingRepository.saveAndFlush(booking);

        // 2. Chống trùng khung giờ (Slot Overlapping)
        assertTrue(bookingService.isSlotOverlapping(slot, "18:00 - 19:00"));
        assertTrue(bookingService.isSlotOverlapping(slot, "17:00 - 19:00"));
        assertFalse(bookingService.isSlotOverlapping(slot, "19:00 - 20:00"));

        // 3. Webhook VietQR xác nhận thanh toán tiền cọc 30%
        Map<String, Object> webhookData = Map.of(
                "content", "CK " + bookingCode + " TIEN COC 30%",
                "transferAmount", 30000,
                "referenceCode", "SEPAY-E2E-" + bookingCode
        );
        Map<String, Object> webhookRes = paymentWebhookService.processVietQRWebhook(
                "UTE_VIETQR_SECRET_TOKEN_2026", null, webhookData);
        assertTrue(Boolean.TRUE.equals(webhookRes.get("success")));

        Booking afterDeposit = bookingRepository.findByBookingCode(bookingCode).orElseThrow();
        assertEquals("Đã cọc 30%", afterDeposit.getStatus());

        List<Payment> depositPayments = paymentRepository.findByBookingCode(bookingCode);
        assertFalse(depositPayments.isEmpty());
        assertEquals(0, new BigDecimal("30000").compareTo(depositPayments.get(0).getAmount()));

        // 4. Khách hàng tới sân -> Thu ngân Check-in
        Booking checkedIn = bookingService.checkIn(bookingCode);
        assertEquals("Đang sử dụng", checkedIn.getStatus());

        // 5. Khách gọi dịch vụ phát sinh tại POS: 2 chai nước khoáng (2 * 15,000 = 30,000d)
        int initialStock = drink.getStockQuantity();
        Map<String, Object> serviceOrderRes = warehouseService.orderServiceForBooking(bookingCode, drink.getProductCode(), 2);
        assertTrue((boolean) serviceOrderRes.get("success"));

        // Xác minh kho hàng đã bị trừ chính xác 2 chai
        Product updatedDrink = productRepository.findByProductCode(drink.getProductCode()).orElseThrow();
        assertEquals(initialStock - 2, updatedDrink.getStockQuantity());

        // Xác minh tổng tiền đơn đặt sân tăng từ 100,000 lên 130,000d
        Booking afterService = bookingRepository.findByBookingCode(bookingCode).orElseThrow();
        assertEquals(0, new BigDecimal("130000").compareTo(afterService.getTotalPrice()));

        // 6. Khách xin đổi sang Sân B (Court Transfer)
        Map<String, Object> transferRes = bookingService.transferCourt(bookingCode, court2.getCourtCode(), slot);
        assertNotNull(transferRes);
        assertTrue((boolean) transferRes.get("success"));

        // 7. Check-out và thanh toán nốt số tiền còn lại, xuất hóa đơn
        Invoice invoice = bookingService.checkOut(bookingCode, BigDecimal.ZERO, BigDecimal.ZERO, "Tiền mặt", "NV01");
        assertNotNull(invoice);
        assertNotNull(invoice.getInvoiceCode());
        assertEquals("Đã thanh toán", invoice.getStatus());

        Booking completed = bookingRepository.findByBookingCode(bookingCode).orElseThrow();
        assertTrue(completed.getStatus().contains("Hoàn thành"));

        // 8. Chốt ca thu ngân và đối soát doanh thu ca
        Map<String, Object> shiftSummary = shiftService.getShiftSummary("NV01", "CN01");
        assertNotNull(shiftSummary);
        assertTrue(shiftSummary.containsKey("systemCashTotal"));
        assertTrue(shiftSummary.containsKey("systemTransferTotal"));
    }
}

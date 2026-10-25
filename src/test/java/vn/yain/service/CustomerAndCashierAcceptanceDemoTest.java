package vn.yain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.yain.dto.CourtEventDto;
import vn.yain.entity.*;
import vn.yain.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nghiệm thu & Bàn giao toàn diện Ngày 9 (25/10/2026) - Thọ (AI-H)
 * Kịch bản Demo Walkthrough thực tế kiểm thử hai luồng nghiệp vụ cốt lõi:
 * 1. Khách hàng (Customer Flow): Tra cứu sân -> Giữ slot realtime -> Đặt sân -> VietQR Cọc 30% -> Idempotent Webhook -> Lịch sử đặt sân.
 * 2. Thu ngân / POS (Cashier Flow): Ma trận sân -> Check-in QR -> Bán dịch vụ F&B trừ kho -> Đổi sân -> Check-out xuất hóa đơn -> Chốt ca thu ngân & đối soát quỹ.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerAndCashierAcceptanceDemoTest {

    private static final Logger log = LoggerFactory.getLogger(CustomerAndCashierAcceptanceDemoTest.class);

    @Autowired
    private CourtService courtService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private PaymentWebhookService paymentWebhookService;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Court demoCourtA;
    private Court demoCourtB;
    private Product demoDrink;
    private Product demoShuttlecock;

    @BeforeEach
    void setUpDemoData() {
        demoCourtA = courtRepository.findByCourtCode("CL_DEMO_A").orElseGet(() -> {
            Court c = new Court();
            c.setCourtCode("CL_DEMO_A");
            c.setCourtName("Sân Demo A - Tiêu Chuẩn");
            c.setCourtType("Tiêu chuẩn");
            c.setBranchCode("CN01");
            c.setStatus("Trống");
            c.setHourlyRate(new BigDecimal("100000"));
            return courtRepository.save(c);
        });

        demoCourtB = courtRepository.findByCourtCode("CL_DEMO_B").orElseGet(() -> {
            Court c = new Court();
            c.setCourtCode("CL_DEMO_B");
            c.setCourtName("Sân Demo B - VIP");
            c.setCourtType("VIP");
            c.setBranchCode("CN01");
            c.setStatus("Trống");
            c.setHourlyRate(new BigDecimal("120000"));
            return courtRepository.save(c);
        });

        demoDrink = productRepository.findByProductCode("SP_DEMO_REVIVE").orElseGet(() -> {
            Product p = new Product();
            p.setProductCode("SP_DEMO_REVIVE");
            p.setProductName("Nước bù khoáng Revive Chanh Muối");
            p.setCategory("Nước uống");
            p.setUnit("Chai");
            p.setUnitPrice(new BigDecimal("15000"));
            p.setCostPrice(new BigDecimal("8000"));
            p.setStockQuantity(60);
            p.setMinQuantity(5);
            p.setBranchCode("CN01");
            return productRepository.save(p);
        });

        demoShuttlecock = productRepository.findByProductCode("SP_DEMO_SHUTTLE").orElseGet(() -> {
            Product p = new Product();
            p.setProductCode("SP_DEMO_SHUTTLE");
            p.setProductName("Ống cầu lông Yonex AS-50");
            p.setCategory("Dụng cụ");
            p.setUnit("Ống");
            p.setUnitPrice(new BigDecimal("250000"));
            p.setCostPrice(new BigDecimal("180000"));
            p.setStockQuantity(20);
            p.setMinQuantity(2);
            p.setBranchCode("CN01");
            return productRepository.save(p);
        });
    }

    @Test
    @Order(1)
    @DisplayName("Nghiệm thu Luồng 1: Khách hàng trải nghiệm hoàn chỉnh (Tìm sân, giữ slot 10p, đặt cọc VietQR, Webhook và tra cứu)")
    void testCustomerEndToEndAcceptanceDemo() {
        log.info("========== BẮT ĐẦU DEMO LUỒNG KHÁCH HÀNG (CUSTOMER FLOW) ==========");

        // Bước 1: Khách hàng vào trang tra cứu sân tại Chi nhánh Quận 9 (CN01)
        List<Court> availableCourts = courtService.getCourts("CN01");
        assertNotNull(availableCourts, "Danh sách sân không được null");
        assertFalse(availableCourts.isEmpty(), "Chi nhánh CN01 phải có sân hoạt động");
        log.info("-> Bước 1: Khách hàng tìm thấy {} sân khả dụng tại CN01", availableCourts.size());

        // Bước 2: Khách chọn Sân A khung giờ 19:00 - 20:00 và giữ chỗ tạm 10 phút
        String timeSlot = "19:00 - 20:00";
        String customerPhone = "0988776655";
        CourtEventDto holdResult = courtService.holdCourtForBooking(demoCourtA.getCourtCode(), "CN01", timeSlot, "Khách trực tuyến");
        assertNotNull(holdResult);
        assertEquals("COURT_HELD", holdResult.getEventType(), "Giữ chỗ slot tạm thời phải phát sự kiện COURT_HELD");
        assertEquals("Đang giữ chỗ", holdResult.getStatus());
        assertEquals(600, holdResult.getHoldRemainingSeconds());
        log.info("-> Bước 2: Đã giữ chỗ tạm thời sân {} slot {} trong 600s", demoCourtA.getCourtCode(), timeSlot);

        // Bước 3: Khách hàng điền thông tin và xác nhận đặt sân (Hệ thống tính 30% tiền cọc)
        String bookingCode = "DS" + (600000 + (int)(Math.random() * 300000));
        LocalDate bookingDate = LocalDate.now().plusDays(2);

        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setBranchCode("CN01");
        booking.setCourtCode(demoCourtA.getCourtCode());
        booking.setCustomerName("Trần Khách Hàng Demo");
        booking.setCustomerPhone(customerPhone);
        booking.setCustomerEmail("khachhang.demo@utesport.vn");
        booking.setBookingDate(bookingDate);
        booking.setTimeSlot(timeSlot);
        booking.setHourlyPrice(new BigDecimal("100000"));
        booking.setTotalPrice(new BigDecimal("100000"));

        Booking savedBooking = bookingService.createBooking(booking);
        assertNotNull(savedBooking);
        assertEquals("Chờ cọc 30%", savedBooking.getStatus());
        assertEquals(0, new BigDecimal("30000").compareTo(savedBooking.getDepositAmount()), "Tiền cọc phải là 30,000 VND (30%)");
        log.info("-> Bước 3: Tạo đơn đặt sân {} thành công, tổng tiền: 100,000đ, tiền cọc 30%: 30,000đ", bookingCode);

        // Bước 4: Khách quét mã VietQR chuyển khoản 30,000 VND -> Cổng ngân hàng kích hoạt Webhook
        Map<String, Object> vietqrWebhookPayload = Map.of(
                "content", "CK " + bookingCode + " TIEN COC 30%",
                "transferAmount", 30000,
                "referenceCode", "SEPAY-DEMO-ACPT-" + bookingCode,
                "transactionDate", "2026-10-25 09:30:00"
        );
        Map<String, Object> webhookRes = paymentWebhookService.processVietQRWebhook(
                "UTE_VIETQR_SECRET_TOKEN_2026", null, vietqrWebhookPayload);
        assertTrue(Boolean.TRUE.equals(webhookRes.get("success")), "Webhook phải xử lý thành công");

        Booking confirmedBooking = bookingRepository.findByBookingCode(bookingCode).orElseThrow();
        assertEquals("Đã cọc 30%", confirmedBooking.getStatus(), "Trạng thái đơn phải chuyển sang 'Đã cọc 30%'");

        List<Payment> payments = paymentRepository.findByBookingCode(bookingCode);
        assertFalse(payments.isEmpty(), "Phải có bản ghi thanh toán cọc");
        assertTrue(payments.get(0).getMethod().contains("VietQR"), "Phương thức thanh toán phải là VietQR");
        assertEquals(0, new BigDecimal("30000").compareTo(payments.get(0).getAmount()));
        log.info("-> Bước 4: Webhook VietQR xử lý tự động thành công, đơn chuyển trạng thái 'Đã cọc 30%'");

        // Bước 5: Kiểm tra tính Idempotent chống lặp giao dịch (Webhook gọi lại lần 2)
        Map<String, Object> duplicateRes = paymentWebhookService.processVietQRWebhook(
                "UTE_VIETQR_SECRET_TOKEN_2026", null, vietqrWebhookPayload);
        assertTrue(Boolean.TRUE.equals(duplicateRes.get("success")));
        assertTrue(Boolean.TRUE.equals(duplicateRes.get("duplicate")), "Webhook lặp phải được đánh dấu duplicate=true");
        log.info("-> Bước 5: Kiểm tra chống lặp Idempotency an toàn, không sinh duplicate thanh toán");

        // Bước 6: Khách hàng mở trang Lịch sử vé để xem E-Ticket và mã QR Check-in
        List<Booking> customerBookings = bookingRepository.findByCustomerPhone(customerPhone);
        assertFalse(customerBookings.isEmpty());
        assertTrue(customerBookings.stream().anyMatch(b -> b.getBookingCode().equals(bookingCode)));
        log.info("-> Bước 6: Khách hàng tra cứu lịch sử vé thành công, đơn {} sẵn sàng check-in", bookingCode);

        log.info("========== HOÀN TẤT DEMO LUỒNG KHÁCH HÀNG: 100% ĐẠT YÊU CẦU ==========\n");
    }

    @Test
    @Order(2)
    @DisplayName("Nghiệm thu Luồng 2: Thu ngân POS tác nghiệp thực tế (Check-in, đổi sân VIP, bán F&B trừ kho, check-out hóa đơn, chốt ca)")
    void testCashierPosEndToEndAcceptanceDemo() {
        log.info("========== BẮT ĐẦU DEMO LUỒNG THU NGÂN / POS (CASHIER FLOW) ==========");

        // Chuẩn bị đơn đặt sân đã cọc 30,000 VND
        String bookingCode = "DS" + (700000 + (int)(Math.random() * 200000));
        LocalDate today = LocalDate.now();
        String timeSlot = "18:00 - 19:00";

        Booking b = new Booking();
        b.setBookingCode(bookingCode);
        b.setBranchCode("CN01");
        b.setCourtCode(demoCourtA.getCourtCode());
        b.setCustomerName("Nguyễn Văn Thể Thao");
        b.setCustomerPhone("0909123456");
        b.setBookingDate(today);
        b.setTimeSlot(timeSlot);
        b.setHourlyPrice(new BigDecimal("100000"));
        b.setTotalPrice(new BigDecimal("100000"));
        b.setDepositAmount(new BigDecimal("30000"));
        b.setStatus("Đã cọc 30%");
        bookingRepository.saveAndFlush(b);

        // Bản ghi cọc 30k đã thanh toán
        Payment depositPayment = new Payment();
        depositPayment.setPaymentCode("TT-DEMO-" + bookingCode);
        depositPayment.setBookingCode(bookingCode);
        depositPayment.setAmount(new BigDecimal("30000"));
        depositPayment.setMethod("Chuyển khoản VietQR");
        depositPayment.setTransactionType("Đặt cọc 30%");
        depositPayment.setStatus("Thành công");
        depositPayment.setTransactionRef("SEPAY-INIT-" + bookingCode);
        paymentRepository.save(depositPayment);

        // Bước 1: Thu ngân mở lưới 26 sân tại /pos/grid, thấy khách đến quầy và thực hiện Check-in
        Booking checkedIn = bookingService.checkIn(bookingCode);
        assertNotNull(checkedIn);
        assertEquals("Đang sử dụng", checkedIn.getStatus(), "Trạng thái đơn phải chuyển sang 'Đang sử dụng'");
        assertNotNull(checkedIn.getCheckInTime(), "Thời gian check-in phải được ghi nhận");

        Court courtAfterCheckin = courtRepository.findByCourtCode(demoCourtA.getCourtCode()).orElseThrow();
        assertEquals("Đang sử dụng", courtAfterCheckin.getStatus(), "Trạng thái sân phải là 'Đang sử dụng'");
        log.info("-> Bước 1: Check-in đơn {} thành công. Sân {} chuyển sang 'Đang sử dụng'", bookingCode, demoCourtA.getCourtCode());

        // Bước 2: Khách có nhu cầu chuyển sang Sân VIP (Sân B - 120,000đ/giờ)
        Map<String, Object> transferResult = bookingService.transferCourt(bookingCode, demoCourtB.getCourtCode(), timeSlot);
        assertNotNull(transferResult);
        assertTrue((boolean) transferResult.get("success"), "Chuyển sân thi đấu phải thành công");

        Booking transferred = bookingRepository.findByBookingCode(bookingCode).orElseThrow();
        assertEquals(demoCourtB.getCourtCode(), transferred.getCourtCode(), "Sân thi đấu mới phải là Sân B");
        assertEquals(0, new BigDecimal("120000").compareTo(transferred.getTotalPrice()), "Giá sân VIP B sau chuyển sân là 120,000đ");
        log.info("-> Bước 2: Đổi sang sân VIP {} thành công, giá mới cập nhật 120,000đ", demoCourtB.getCourtCode());

        // Bước 3: Trong khi thi đấu trên Sân B, khách gọi 3 chai Revive (3 x 15k = 45k) và 1 ống cầu (250k)
        int initialDrinkStock = demoDrink.getStockQuantity();
        int initialShuttleStock = demoShuttlecock.getStockQuantity();

        Map<String, Object> drinkOrder = warehouseService.orderServiceForBooking(bookingCode, demoDrink.getProductCode(), 3);
        assertTrue((boolean) drinkOrder.get("success"));

        Map<String, Object> shuttleOrder = warehouseService.orderServiceForBooking(bookingCode, demoShuttlecock.getProductCode(), 1);
        assertTrue((boolean) shuttleOrder.get("success"));

        // Xác minh kho hàng bị trừ trực tiếp
        Product updatedDrink = productRepository.findByProductCode(demoDrink.getProductCode()).orElseThrow();
        Product updatedShuttle = productRepository.findByProductCode(demoShuttlecock.getProductCode()).orElseThrow();
        assertEquals(initialDrinkStock - 3, updatedDrink.getStockQuantity(), "Kho Revive phải giảm đúng 3 chai");
        assertEquals(initialShuttleStock - 1, updatedShuttle.getStockQuantity(), "Kho Cầu Yonex phải giảm đúng 1 ống");

        // Tổng tiền đơn sau khi đổi sân VIP (120k) + 45k (nước) + 250k (cầu) = 415,000 VND
        Booking afterFnb = bookingRepository.findByBookingCode(bookingCode).orElseThrow();
        assertEquals(0, new BigDecimal("415000").compareTo(afterFnb.getTotalPrice()), "Tổng tiền đơn phải là 415,000đ");
        log.info("-> Bước 3: Gọi dịch vụ F&B thành công. Kho hàng trừ tự động. Tổng tiền đơn tăng lên 415,000đ");

        // Bước 4: Khách kết thúc buổi chơi, ra quầy trả sân và quyết toán
        // Phí phát sinh thuê vợt: 5,000 VND
        // Tổng phải thanh toán nốt = 415,000 (tổng đơn gồm sân VIP + F&B) + 5,000 (phụ phí) - 30,000 (cọc) = 390,000 VND
        Invoice invoice = bookingService.checkOut(
                bookingCode,
                new BigDecimal("5000"), // extraEquipmentFee
                BigDecimal.ZERO,        // extraProductFee
                "Tiền mặt",
                "THUNGAN_01"
        );

        assertNotNull(invoice);
        assertNotNull(invoice.getInvoiceCode(), "Mã hóa đơn phải được sinh tự động");
        assertTrue(invoice.getInvoiceCode().startsWith("HD"), "Mã hóa đơn phải bắt đầu bằng HD");
        assertEquals("Đã thanh toán", invoice.getStatus());
        assertEquals(0, new BigDecimal("390000").compareTo(invoice.getTotalPayment()), "Số tiền thu nốt tại quầy phải đúng 390,000đ");

        Booking completed = bookingRepository.findByBookingCode(bookingCode).orElseThrow();
        assertTrue(completed.getStatus().contains("Hoàn thành"));

        Court courtAfterCheckout = courtRepository.findByCourtCode(demoCourtB.getCourtCode()).orElseThrow();
        assertEquals("Trống", courtAfterCheckout.getStatus(), "Sân phải được trả về trạng thái 'Trống' sẵn sàng đón khách mới");
        log.info("-> Bước 4: Check-out thành công. Hóa đơn {} xuất với số tiền thu nốt: 370,000đ. Sân đã trở về 'Trống'", invoice.getInvoiceCode());

        // Bước 5: Cuối ca làm việc, thu ngân thực hiện tổng kết và chốt ca (Shift Closing)
        Map<String, Object> shiftSummary = shiftService.getShiftSummary("THUNGAN_01", "CN01");
        assertNotNull(shiftSummary);
        assertNotNull(shiftSummary.get("systemCashTotal"));
        assertNotNull(shiftSummary.get("systemTransferTotal"));

        // Thu ngân kiểm đếm tiền mặt thực tế và bàn giao ca
        BigDecimal actualCashCounted = (BigDecimal) shiftSummary.get("systemCashTotal");
        Map<String, Object> closeShiftReceipt = shiftService.closeShift(
                "THUNGAN_01",
                "CN01",
                actualCashCounted,
                "Bàn giao ca sáng ngày 25/10/2026 - Kiểm quỹ khớp 100%"
        );

        assertNotNull(closeShiftReceipt);
        assertTrue((boolean) closeShiftReceipt.get("success"));
        assertNotNull(closeShiftReceipt.get("handoverCode"));
        assertTrue(closeShiftReceipt.get("handoverCode").toString().startsWith("BG"));
        assertEquals(0, BigDecimal.ZERO.compareTo((BigDecimal) closeShiftReceipt.get("difference")), "Chênh lệch tiền mặt kiểm đếm phải bằng 0");
        assertEquals("CLOSED", closeShiftReceipt.get("status"));
        log.info("-> Bước 5: Chốt ca thu ngân thành công. Mã biên bản bàn giao: {}, Chênh lệch kiểm quỹ: 0đ", closeShiftReceipt.get("handoverCode"));

        log.info("========== HOÀN TẤT DEMO LUỒNG THU NGÂN / POS: 100% ĐẠT YÊU CẦU ==========\n");
    }
}

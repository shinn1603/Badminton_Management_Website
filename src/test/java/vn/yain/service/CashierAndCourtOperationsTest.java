package vn.yain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import vn.yain.entity.*;
import vn.yain.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test xác minh toàn diện luồng nghiệp vụ Thu ngân & Vận hành sân Ngày 4 (XL-03, XL-04, XL-05, XL-06, XL-07).
 * Kiểm tra: Đặt cọc -> Nhận sân -> Dùng dịch vụ F&B -> Đổi sân -> Trả sân & Quyết toán hóa đơn.
 * Kiểm tra chống thao tác lặp và đảm bảo tính nhất quán giữa Booking - Payment - Invoice - Tồn kho.
 */
class CashierAndCourtOperationsTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CourtService courtService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BookingService bookingService;

    @InjectMocks
    private WarehouseService warehouseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Luồng xuyên suốt: Đặt cọc 30% -> Nhận sân (Check-in) -> Bán dịch vụ (F&B) -> Trả sân (Check-out) -> Quyết toán hóa đơn")
    void testFullCashierLifecycle_Deposit_CheckIn_ServiceOrder_CheckOut() {
        Booking booking = new Booking();
        booking.setBookingCode("DS_FULL_FLOW");
        booking.setCourtCode("CL01");
        booking.setBranchCode("CN01");
        booking.setTimeSlot("18:00 - 19:00");
        booking.setTotalPrice(new BigDecimal("100000"));
        booking.setDepositAmount(new BigDecimal("30000"));
        booking.setStatus("Chờ cọc 30%");

        Court court = new Court();
        court.setCourtCode("CL01");
        court.setStatus("Đã đặt trước");

        when(bookingRepository.findByBookingCode("DS_FULL_FLOW")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(courtRepository.findByCourtCode("CL01")).thenReturn(Optional.of(court));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking deposited = bookingService.confirmDeposit("DS_FULL_FLOW", new BigDecimal("30000"), "VietQR Pro", "VQR-FULL-01");
        assertNotNull(deposited);
        assertEquals("Đã cọc 30%", deposited.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));

        Booking checkedIn = bookingService.checkIn("DS_FULL_FLOW");
        assertNotNull(checkedIn);
        assertEquals("Đang sử dụng", checkedIn.getStatus());
        assertNotNull(checkedIn.getCheckInTime());
        assertEquals("Đang sử dụng", court.getStatus());
        verify(courtService, atLeastOnce()).updateCourtStatus("CL01", "Đang sử dụng");

        Product revive = new Product();
        revive.setProductCode("SP_REVIVE");
        revive.setProductName("Revive Chanh Muối");
        revive.setUnitPrice(new BigDecimal("15000"));
        revive.setStockQuantity(20);
        revive.setUnit("chai");

        when(productRepository.findByProductCode("SP_REVIVE")).thenReturn(Optional.of(revive));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> serviceOrder = warehouseService.orderServiceForBooking("DS_FULL_FLOW", "SP_REVIVE", 2);
        assertTrue((boolean) serviceOrder.get("success"));
        assertEquals(18, revive.getStockQuantity());
        assertEquals(new BigDecimal("30000"), serviceOrder.get("itemTotal"));
        assertEquals(new BigDecimal("130000"), booking.getTotalPrice());

        BigDecimal extraEqFee = new BigDecimal("20000");
        Invoice invoice = bookingService.checkOut("DS_FULL_FLOW", extraEqFee, BigDecimal.ZERO, "Tiền mặt", "NVQ01");

        assertNotNull(invoice);
        assertEquals("DS_FULL_FLOW", invoice.getBookingCode());
        assertEquals(new BigDecimal("130000"), invoice.getCourtFee());
        assertEquals(new BigDecimal("20000"), invoice.getEquipmentFee());
        assertEquals(new BigDecimal("30000"), invoice.getDepositPaid());
        assertEquals(new BigDecimal("120000"), invoice.getTotalPayment());
        assertEquals("Đã thanh toán", invoice.getStatus());

        assertEquals("Hoàn thành", booking.getStatus());
        assertNotNull(booking.getCheckOutTime());
        verify(courtService, times(1)).updateCourtStatus("CL01", "Trống");
    }

    @Test
    @DisplayName("Thao tác lặp: Check-in lần 2 trên đơn đang sử dụng phải bị từ chối")
    void testCheckIn_RepeatedOnInUseBooking_ThrowsIllegalStateException() {
        Booking booking = new Booking();
        booking.setBookingCode("DS_IN_USE");
        booking.setStatus("Đang sử dụng");

        when(bookingRepository.findByBookingCode("DS_IN_USE")).thenReturn(Optional.of(booking));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bookingService.checkIn("DS_IN_USE");
        });
        assertTrue(ex.getMessage().contains("hiện đang sử dụng"));
    }

    @Test
    @DisplayName("Thao tác lặp: Check-in trên đơn đã hủy hoặc đã hoàn thành phải báo lỗi")
    void testCheckIn_CancelledOrCompleted_ThrowsIllegalStateException() {
        Booking cancelled = new Booking();
        cancelled.setBookingCode("DS_CANCELLED");
        cancelled.setStatus("Đã hủy");

        when(bookingRepository.findByBookingCode("DS_CANCELLED")).thenReturn(Optional.of(cancelled));

        assertThrows(IllegalStateException.class, () -> bookingService.checkIn("DS_CANCELLED"));

        Booking completed = new Booking();
        completed.setBookingCode("DS_COMPLETED");
        completed.setStatus("Hoàn thành");

        when(bookingRepository.findByBookingCode("DS_COMPLETED")).thenReturn(Optional.of(completed));

        assertThrows(IllegalStateException.class, () -> bookingService.checkIn("DS_COMPLETED"));
    }

    @Test
    @DisplayName("Thao tác lặp: Check-out lần 2 trên đơn đã hoàn thành phải bị từ chối")
    void testCheckOut_RepeatedOnCompletedBooking_ThrowsIllegalStateException() {
        Booking booking = new Booking();
        booking.setBookingCode("DS_COMPLETED");
        booking.setStatus("Hoàn thành");

        when(bookingRepository.findByBookingCode("DS_COMPLETED")).thenReturn(Optional.of(booking));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bookingService.checkOut("DS_COMPLETED", BigDecimal.ZERO, BigDecimal.ZERO, "Tiền mặt", "NVQ01");
        });
        assertTrue(ex.getMessage().contains("đã hoàn tất thanh toán"));
    }

    @Test
    @DisplayName("Chuyển sân trong lúc đang sử dụng: Cập nhật sân cũ về Trống, sân mới thành Đang sử dụng")
    void testCourtTransfer_DuringInUse_TransfersAndUpdatesStatuses() {
        Booking booking = new Booking();
        booking.setBookingCode("DS_TRANSFER_IN_USE");
        booking.setCourtCode("CL01");
        booking.setTimeSlot("18:00 - 19:00");
        booking.setHourlyPrice(new BigDecimal("90000"));
        booking.setTotalPrice(new BigDecimal("90000"));
        booking.setStatus("Đang sử dụng");
        booking.setBookingDate(LocalDate.now());

        Court oldCourt = new Court();
        oldCourt.setCourtCode("CL01");
        oldCourt.setStatus("Đang sử dụng");

        Court targetCourt = new Court();
        targetCourt.setCourtCode("CL06");
        targetCourt.setHourlyRate(new BigDecimal("120000"));
        targetCourt.setStatus("Trống");

        when(bookingRepository.findByBookingCode("DS_TRANSFER_IN_USE")).thenReturn(Optional.of(booking));
        when(courtRepository.findByCourtCode("CL01")).thenReturn(Optional.of(oldCourt));
        when(courtRepository.findByCourtCode("CL06")).thenReturn(Optional.of(targetCourt));
        when(bookingRepository.findByCourtCodeAndBookingDate(eq("CL06"), any())).thenReturn(java.util.Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> res = bookingService.transferCourt("DS_TRANSFER_IN_USE", "CL06", "18:00 - 19:00");

        assertTrue((boolean) res.get("success"));
        assertEquals("CL06", booking.getCourtCode());
        assertEquals("Đang sử dụng", targetCourt.getStatus());
        assertEquals("Trống", oldCourt.getStatus());
        verify(courtService, times(1)).updateCourtStatus("CL01", "Trống");
        verify(courtService, times(1)).updateCourtStatus("CL06", "Đang sử dụng");
    }

    @Test
    @DisplayName("Hủy đặt sân giải phóng sân về Trống và cho phép gọi lặp an toàn")
    void testCancelBooking_ReleasesCourt_Idempotent() {
        Booking booking = new Booking();
        booking.setBookingCode("DS_CANCEL");
        booking.setCourtCode("CL02");
        booking.setStatus("Chờ cọc 30%");

        Court court = new Court();
        court.setCourtCode("CL02");
        court.setStatus("Đã đặt trước");

        when(bookingRepository.findByBookingCode("DS_CANCEL")).thenReturn(Optional.of(booking));
        when(courtRepository.findByCourtCode("CL02")).thenReturn(Optional.of(court));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking cancelled = bookingService.cancelBooking("DS_CANCEL", "Khách đổi lịch");
        assertEquals("Đã hủy", cancelled.getStatus());
        verify(courtService, times(1)).updateCourtStatus("CL02", "Trống");

        Booking cancelledAgain = bookingService.cancelBooking("DS_CANCEL", "Khách gọi lại");
        assertEquals("Đã hủy", cancelledAgain.getStatus());
    }

    @Test
    @DisplayName("Bán hàng F&B: Từ chối khi hết hàng tồn kho, giữ nguyên trạng thái dữ liệu")
    void testServiceOrder_OutOfStock_DoesNotCorruptData() {
        Product grip = new Product();
        grip.setProductCode("SP_GRIP");
        grip.setProductName("Quấn cán Yonex");
        grip.setStockQuantity(2);
        grip.setUnitPrice(new BigDecimal("25000"));

        Booking booking = new Booking();
        booking.setBookingCode("DS_ORDER_ERR");
        booking.setTotalPrice(new BigDecimal("100000"));

        when(productRepository.findByProductCode("SP_GRIP")).thenReturn(Optional.of(grip));
        when(bookingRepository.findByBookingCode("DS_ORDER_ERR")).thenReturn(Optional.of(booking));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            warehouseService.orderServiceForBooking("DS_ORDER_ERR", "SP_GRIP", 5);
        });

        assertTrue(ex.getMessage().contains("Số lượng tồn kho không đủ"));
        assertEquals(2, grip.getStockQuantity());
        assertEquals(new BigDecimal("100000"), booking.getTotalPrice());
        verify(productRepository, never()).save(grip);
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    @DisplayName("Bán hàng F&B: Từ chối khi số lượng đặt mua <= 0")
    void testServiceOrder_InvalidQuantity_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            warehouseService.orderServiceForBooking("DS123", "SP_REVIVE", 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            warehouseService.orderServiceForBooking("DS123", "SP_REVIVE", -3);
        });
    }

    @Test
    @DisplayName("Hủy dịch vụ: Hoàn trả tồn kho và khấu trừ chi phí hợp lệ")
    void testCancelService_Success() {
        Product water = new Product();
        water.setProductCode("SP_WATER");
        water.setProductName("Nước suối");
        water.setUnitPrice(new BigDecimal("10000"));
        water.setStockQuantity(5);

        Booking booking = new Booking();
        booking.setBookingCode("DS_CANCEL_SVC");
        booking.setTotalPrice(new BigDecimal("150000"));

        when(productRepository.findByProductCode("SP_WATER")).thenReturn(Optional.of(water));
        when(bookingRepository.findByBookingCode("DS_CANCEL_SVC")).thenReturn(Optional.of(booking));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = warehouseService.cancelServiceForBooking("DS_CANCEL_SVC", "SP_WATER", 2);

        assertTrue((boolean) result.get("success"));
        assertEquals(7, water.getStockQuantity());
        assertEquals(new BigDecimal("130000"), booking.getTotalPrice());
        verify(productRepository, times(1)).save(water);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Hủy dịch vụ: Từ chối khi số lượng hủy <= 0")
    void testCancelService_InvalidQuantity_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            warehouseService.cancelServiceForBooking("DS123", "SP_WATER", 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            warehouseService.cancelServiceForBooking("DS123", "SP_WATER", -2);
        });
    }
}

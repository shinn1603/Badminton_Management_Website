package vn.yain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.yain.entity.Booking;
import vn.yain.repository.BookingRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test cho ApiController (Bao phu cac API RESTful trong 10 Thiet ke xu ly)
 */
@SpringBootTest
class ApiControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ApiController apiController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private vn.yain.repository.CourtRepository courtRepository;

    @Autowired
    private vn.yain.repository.ProductRepository productRepository;

    @Autowired
    private vn.yain.repository.BranchRepository branchRepository;

    @Autowired
    private vn.yain.repository.PromotionRepository promotionRepository;

    @Autowired
    private vn.yain.repository.UserRepository userRepository;

    @Autowired
    private vn.yain.repository.TournamentRepository tournamentRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(apiController).build();

        if (courtRepository.findByCourtCode("CL01").isEmpty()) {
            vn.yain.entity.Court c1 = new vn.yain.entity.Court();
            c1.setCourtCode("CL01");
            c1.setCourtName("Sân Tiêu Chuẩn 1");
            c1.setCourtType("Tiêu chuẩn");
            c1.setBranchCode("CN01");
            c1.setStatus("Trống");
            c1.setHourlyRate(new BigDecimal("90000"));
            courtRepository.save(c1);
        }
        if (courtRepository.findByCourtCode("CL02").isEmpty()) {
            vn.yain.entity.Court c2 = new vn.yain.entity.Court();
            c2.setCourtCode("CL02");
            c2.setCourtName("Sân Tiêu Chuẩn 2");
            c2.setCourtType("Tiêu chuẩn");
            c2.setBranchCode("CN01");
            c2.setStatus("Trống");
            c2.setHourlyRate(new BigDecimal("90000"));
            courtRepository.save(c2);
        }
        if (productRepository.findByProductCode("P01").isEmpty()) {
            vn.yain.entity.Product p = new vn.yain.entity.Product();
            p.setProductCode("P01");
            p.setProductName("Nước khoáng Lavie 500ml");
            p.setCategory("Nước uống");
            p.setUnit("Chai");
            p.setUnitPrice(new BigDecimal("10000"));
            p.setCostPrice(new BigDecimal("6000"));
            p.setStockQuantity(50);
            p.setMinQuantity(10);
            p.setBranchCode("CN01");
            productRepository.save(p);
        }
    }

    @Test
    @DisplayName("API XL-01: Dang nhap voi tai khoan khong ton tai tra ve 401 Unauthorized")
    void testApiLogin_Unauthorized() throws Exception {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "non_existing_user_9999");
        credentials.put("password", "invalid_password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("API Tra cuu danh sach san theo chi nhanh CN01")
    void testApiGetCourts() throws Exception {
        mockMvc.perform(get("/api/courts?branchCode=CN01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @DisplayName("API XL-02: Khoa slot giu cho truc tuyen va tra ve ma VietQR coc 30%")
    void testApiSlotLock_Success() throws Exception {
        String testCourt = "CL01";
        String testSlot = "06:00 - 07:00";
        LocalDate testDate = LocalDate.now().plusDays(50);
        String dateStr = testDate.toString();

        bookingRepository.findByCourtCodeAndBookingDate(testCourt, testDate)
                .forEach(b -> bookingRepository.delete(b));

        Map<String, Object> payload = new HashMap<>();
        payload.put("branchCode", "CN01");
        payload.put("courtCode", testCourt);
        payload.put("date", dateStr);
        payload.put("timeSlot", testSlot);
        payload.put("customerName", "Nguyen Van Test");
        payload.put("customerPhone", "0908888777");

        mockMvc.perform(post("/api/bookings/slot-lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.bookingCode", startsWith("DS")))
                .andExpect(jsonPath("$.courtCode", is(testCourt)))
                .andExpect(jsonPath("$.timeSlot", is(testSlot)))
                .andExpect(jsonPath("$.depositAmount", notNullValue()))
                .andExpect(jsonPath("$.qrUrl", containsString("vietqr.io")));
    }

    @Test
    @DisplayName("API XL-02: Tu choi khoa slot khi khung gio da co nguoi dat truoc (Chong trung lich)")
    void testApiSlotLock_Conflict() throws Exception {
        String testCourt = "CL02";
        String testSlot = "07:00 - 08:00";
        LocalDate futureDate = LocalDate.now().plusDays(20);

        Booking b = new Booking();
        b.setBookingCode("DS_CONFLICT_TEST_" + System.currentTimeMillis() % 10000);
        b.setBranchCode("CN01");
        b.setCourtCode(testCourt);
        b.setCustomerName("Khach Hang Cu");
        b.setCustomerPhone("0901112223");
        b.setBookingDate(futureDate);
        b.setTimeSlot(testSlot);
        b.setHourlyPrice(new BigDecimal("100000"));
        b.setTotalPrice(new BigDecimal("100000"));
        b.setDepositAmount(new BigDecimal("30000"));
        b.setStatus("Chờ cọc 30%");
        bookingRepository.save(b);

        Map<String, Object> payload = new HashMap<>();
        payload.put("branchCode", "CN01");
        payload.put("courtCode", testCourt);
        payload.put("date", futureDate.toString());
        payload.put("timeSlot", testSlot);
        payload.put("customerName", "Khach Thu Hai");
        payload.put("customerPhone", "0901234567");

        mockMvc.perform(post("/api/bookings/slot-lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("đã có người đặt trước")));
    }

    @Test
    @DisplayName("API XL-03: Webhook VietQR tu dong doi trang thai sang Da coc 30%")
    void testApiWebhookVietQR_AutoConfirm() throws Exception {
        String uniqueBookingCode = "DS" + (200000 + (int)(Math.random() * 700000));
        Booking b = new Booking();
        b.setBookingCode(uniqueBookingCode);
        b.setBranchCode("CN01");
        b.setCourtCode("CL03");
        b.setCustomerName("Khach Webhook");
        b.setCustomerPhone("0905556667");
        b.setBookingDate(LocalDate.now());
        b.setTimeSlot("10:00 - 11:00");
        b.setHourlyPrice(new BigDecimal("100000"));
        b.setTotalPrice(new BigDecimal("100000"));
        b.setDepositAmount(new BigDecimal("30000"));
        b.setStatus("Chờ cọc 30%");
        bookingRepository.save(b);

        Map<String, Object> webhookPayload = new HashMap<>();
        webhookPayload.put("content", "CK " + uniqueBookingCode + " THANH TOAN COC SAN");
        webhookPayload.put("transferAmount", 30000);

        mockMvc.perform(post("/api/webhook/vietqr")
                        .header("X-Webhook-Secret", "UTE_VIETQR_SECRET_TOKEN_2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        Booking updated = bookingRepository.findByBookingCode(uniqueBookingCode).orElseThrow();
        assertEquals("Đã cọc 30%", updated.getStatus());
    }

    @Test
    @DisplayName("API Tra cuu danh muc san pham va nuoc uong (XL-05)")
    void testApiGetProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @DisplayName("API XL-06: Chuyen san linh hoat qua API RESTful")
    void testApiTransferCourt_Success() throws Exception {
        String transferBookingCode = "DS_API_TRF_" + (System.currentTimeMillis() % 10000);
        LocalDate date = LocalDate.now().plusDays(30);

        Booking b = new Booking();
        b.setBookingCode(transferBookingCode);
        b.setBranchCode("CN01");
        b.setCourtCode("CL01");
        b.setCustomerName("Khach Chuyen San");
        b.setCustomerPhone("0903334445");
        b.setBookingDate(date);
        b.setTimeSlot("19:00 - 20:00");
        b.setHourlyPrice(new BigDecimal("90000"));
        b.setTotalPrice(new BigDecimal("90000"));
        b.setStatus("Đang sử dụng");
        bookingRepository.save(b);

        bookingRepository.findByCourtCodeAndBookingDate("CL02", date)
                .forEach(old -> bookingRepository.delete(old));

        Map<String, String> payload = new HashMap<>();
        payload.put("targetCourtCode", "CL02");
        payload.put("newTimeSlot", "19:00 - 20:00");

        mockMvc.perform(post("/api/bookings/" + transferBookingCode + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.newCourtCode", is("CL02")));
    }

    @Test
    @DisplayName("API XL-10: Nhap kho hang hoa phu kien qua API RESTful")
    void testApiInventoryIntake_Success() throws Exception {
        Map<String, Object> intakePayload = new HashMap<>();
        intakePayload.put("supplierName", "Cong Ty TNHH The Thao Viet");
        intakePayload.put("branchCode", "CN01");
        intakePayload.put("staffCode", "NS02");
        intakePayload.put("notes", "Nhap hang dau tuan");

        Map<String, Object> item = new HashMap<>();
        item.put("productCode", "P01");
        item.put("quantity", 10);
        item.put("importPrice", 15000);
        intakePayload.put("items", java.util.Collections.singletonList(item));

        mockMvc.perform(post("/api/inventory/intake")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(intakePayload)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.receipt.receiptCode", startsWith("PNK")));
    }

    @Test
    @DisplayName("API XL-08: Thong ke doanh thu va chot ca thu ngan")
    void testApiShiftEndpoints() throws Exception {
        mockMvc.perform(get("/api/shifts/summary?staffCode=NVQ01&branchCode=CN01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.staffCode", is("NVQ01")))
                .andExpect(jsonPath("$.systemCashTotal", notNullValue()));

        Map<String, Object> closePayload = new HashMap<>();
        closePayload.put("staffCode", "NVQ01");
        closePayload.put("branchCode", "CN01");
        closePayload.put("actualCash", 1000000);
        closePayload.put("notes", "Chot ca ket thuc ngay");

        mockMvc.perform(post("/api/shifts/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(closePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.handoverCode", startsWith("BG")))
                .andExpect(jsonPath("$.status", is("CLOSED")));
    }

    @Test
    @DisplayName("API Cloudinary: Upload anh san va san pham")
    void testApiUploadImage() throws Exception {
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file",
                "court-sample.jpg",
                "image/jpeg",
                "sample court photo bytes".getBytes()
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/upload/image")
                        .file(file)
                        .param("folder", "courts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.url", notNullValue()))
                .andExpect(jsonPath("$.folder", is("courts")));
    }

    @Test
    @DisplayName("API Staff & WorkShifts: Quan ly nhan su va xem lich truc tuan")
    void testApiStaffAndShifts_Success() throws Exception {
        mockMvc.perform(get("/api/staff?branchCode=CN01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));

        Map<String, String> staffPayload = new HashMap<>();
        staffPayload.put("staffCode", "NV-TEST-01");
        staffPayload.put("branchCode", "CN01");
        staffPayload.put("fullName", "Nhan Vien Test");
        staffPayload.put("position", "Thu ngan POS quầy");
        staffPayload.put("phone", "0909999888");
        staffPayload.put("email", "test@utesport.vn");

        mockMvc.perform(post("/api/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staffPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.staff.staffCode", is("NV-TEST-01")));

        mockMvc.perform(get("/api/shifts/week?branchCode=CN01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.schedule.mon", notNullValue()));

        Map<String, String> assignPayload = new HashMap<>();
        assignPayload.put("day", "mon");
        assignPayload.put("shift", "morning");
        assignPayload.put("staffCode", "NV-TEST-01");
        assignPayload.put("duty", "POS");
        assignPayload.put("icon", "POS");
        assignPayload.put("branchCode", "CN01");

        mockMvc.perform(post("/api/shifts/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("API Tournaments: Tra cuu danh sach va dang ky giai dau")
    void testApiTournaments_GetAndRegister() throws Exception {
        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$[0].tournamentCode", notNullValue()));

        Map<String, Object> regPayload = new HashMap<>();
        regPayload.put("tournamentCode", "GD02");
        regPayload.put("tournamentName", "Giai Cau Long Doanh Nghiep & Sinh Vien Cup Thu Duc 2026");
        regPayload.put("teamName", "CLB UTE Masters Test");
        regPayload.put("category", "Doi Nam Nu");
        regPayload.put("player1Name", "Nguyen Van Test");
        regPayload.put("phone", "0908123456");

        mockMvc.perform(post("/api/tournaments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.currentParticipants", notNullValue()))
                .andExpect(jsonPath("$.remainingSlots", notNullValue()));
    }

    @Test
    @DisplayName("Admin: Lay danh sach tai khoan nguoi dung he thong GET /api/admin/users")
    void testGetAdminUsers_ReturnsUserList() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("Manager Dashboard: Thong ke thuc te tu CSDL khong co du lieu gia GET /api/manager/stats")
    void testManagerStats_RealDatabaseCalculations() throws Exception {
        mockMvc.perform(get("/api/manager/stats")
                        .param("branchCode", "CN01")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings", notNullValue()))
                .andExpect(jsonPath("$.todayRevenue", notNullValue()))
                .andExpect(jsonPath("$.occupancyRate", notNullValue()))
                .andExpect(jsonPath("$.totalCustomers", notNullValue()))
                .andExpect(jsonPath("$.trends.bookingTrend", notNullValue()))
                .andExpect(jsonPath("$.trends.revenueTrend", notNullValue()))
                .andExpect(jsonPath("$.trends.occupancyTrend", notNullValue()))
                .andExpect(jsonPath("$.trends.customerTrend", notNullValue()))
                .andExpect(jsonPath("$.chart.labels", notNullValue()))
                .andExpect(jsonPath("$.donut.labels", notNullValue()));
    }

    @Test
    @DisplayName("Director: Xoa chi nhanh thanh cong DELETE /api/director/branches/{branchCode}")
    void testDirectorDeleteBranch() throws Exception {
        vn.yain.entity.Branch b = new vn.yain.entity.Branch();
        b.setBranchCode("CN99");
        b.setBranchName("Chi nhánh Test 99");
        b.setAddress("99 Vo Van Ngan, Thu Duc");
        b.setPhone("0909999999");
        b.setTotalCourts(4);
        b.setStatus("Hoạt động");
        branchRepository.save(b);

        mockMvc.perform(delete("/api/director/branches/CN99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        org.junit.jupiter.api.Assertions.assertTrue(branchRepository.findByBranchCode("CN99").isEmpty());
    }

    @Test
    @DisplayName("Admin: Xoa tai khoan nguoi dung thanh cong DELETE /api/admin/users/{username}")
    void testDeleteAdminUser() throws Exception {
        vn.yain.entity.User u = new vn.yain.entity.User();
        u.setUsername("testuser_delete");
        u.setPassword("123456");
        u.setFullName("Nguyen Van Test Delete");
        u.setRole("POS");
        u.setStatus("Hoạt động");
        u.setBranchCode("CN01");
        userRepository.save(u);

        org.junit.jupiter.api.Assertions.assertTrue(userRepository.findByUsername("testuser_delete").isPresent());

        mockMvc.perform(delete("/api/admin/users/testuser_delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Đã xóa tài khoản [testuser_delete] thành công!")));

        org.junit.jupiter.api.Assertions.assertTrue(userRepository.findByUsername("testuser_delete").isEmpty());
    }

    @Test
    @DisplayName("Admin: Chan xoa tai khoan admin goc")
    void testCannotDeleteAdminSuperUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/admin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Không thể xóa tài khoản Quản trị viên hệ thống tối cao [admin]!")));
    }

    @Test
    @DisplayName("Director: Xoa giai dau thanh cong DELETE /api/director/tournaments/{id}")
    void testDeleteTournament() throws Exception {
        vn.yain.entity.Tournament t = new vn.yain.entity.Tournament();
        t.setTournamentCode("TOUR_TEST_DEL");
        t.setTournamentName("Giải Đấu Test Xóa");
        t.setStatus("Chờ phê duyệt");
        t.setBranchCode("CN01");
        t.setStartDate(java.time.LocalDateTime.now());
        t.setEndDate(java.time.LocalDateTime.now().plusDays(2));
        t = tournamentRepository.save(t);

        mockMvc.perform(delete("/api/director/tournaments/" + t.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Đã xóa giải đấu")));

        org.junit.jupiter.api.Assertions.assertTrue(tournamentRepository.findById(t.getId()).isEmpty());
    }

    @Test
    @DisplayName("Promotion: Tao ma giam gia cho chi nhanh moi tao va ap dung thanh cong khi dat san")
    void testPromotionForNewBranch() throws Exception {
        vn.yain.entity.Branch b = new vn.yain.entity.Branch();
        b.setBranchCode("CN88");
        b.setBranchName("Chi nhánh 88 - Bình Thạnh Mới");
        b.setAddress("88 Dien Bien Phu, Binh Thanh");
        b.setPhone("0988888888");
        b.setTotalCourts(4);
        b.setStatus("Hoạt động");
        branchRepository.save(b);

        String promoPayload = """
            {
                "promoCode": "KM88",
                "promoName": "Ưu đãi khai trương CN88",
                "discountType": "Phần trăm (%)",
                "discountValue": 20,
                "scope": "Chi nhánh 88 - Bình Thạnh Mới",
                "status": "Đang áp dụng",
                "startDate": "2026-01-01",
                "endDate": "2026-12-31"
            }
            """;

        mockMvc.perform(post("/api/director/pricing/promotions")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(promoPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(get("/api/pricing/summary?branchCode=CN88"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.activePromotions[?(@.promoCode == 'KM88')]", notNullValue()));

        String applyPayload = """
            {
                "promoCode": "KM88",
                "branchCode": "CN88",
                "orderAmount": 200000
            }
            """;
        mockMvc.perform(post("/api/promotions/apply")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(applyPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.discountAmount", is(40000)))
                .andExpect(jsonPath("$.finalAmount", is(160000)));

        String applyOtherBranch = """
            {
                "promoCode": "KM88",
                "branchCode": "CN01",
                "orderAmount": 200000
            }
            """;
        mockMvc.perform(post("/api/promotions/apply")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(applyOtherBranch))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));

        promotionRepository.findByPromoCode("KM88").ifPresent(promotionRepository::delete);
        branchRepository.findByBranchCode("CN88").ifPresent(branchRepository::delete);
    }

    @Test
    @DisplayName("Bao mat phan quyen: Quan ly chi nhanh khong duoc phep dieu chinh gia thue san va bi chan 403")
    void testManagerCannotAlterCourtPrice() throws Exception {
        if (userRepository.findByUsername("ql_cn01").isEmpty()) {
            vn.yain.entity.User u = new vn.yain.entity.User();
            u.setUsername("ql_cn01");
            u.setPassword("123456");
            u.setFullName("Trần Phúc Bảo");
            u.setRole("MANAGER");
            u.setBranchCode("CN01");
            userRepository.save(u);
        }

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "ql_cn01",
                null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MANAGER"))
            )
        );

        try {
            String payload = """
                {
                    "courtCode": "CL01",
                    "courtName": "Sân Tiêu Chuẩn 1",
                    "hourlyRate": 50000,
                    "status": "Trống",
                    "branchCode": "CN01"
                }
                """;

            mockMvc.perform(post("/api/courts")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(payload))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Quản lý chi nhánh không được phép điều chỉnh")));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Bao mat phan quyen: Quan ly chi nhanh bi khoa chi xem san co so minh, khong xem hoac xoa san chi nhanh khac")
    void testManagerBranchIsolation() throws Exception {
        if (userRepository.findByUsername("ql_cn01").isEmpty()) {
            vn.yain.entity.User u = new vn.yain.entity.User();
            u.setUsername("ql_cn01");
            u.setPassword("123456");
            u.setFullName("Trần Phúc Bảo");
            u.setRole("MANAGER");
            u.setBranchCode("CN01");
            userRepository.save(u);
        }

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "ql_cn01",
                null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MANAGER"))
            )
        );

        try {
            mockMvc.perform(get("/api/courts?branchCode=CN02"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].branchCode", is("CN01")));

            mockMvc.perform(delete("/api/courts/CL01"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Chỉ Ban Giám Đốc")));

            BigDecimal currentPrice = courtRepository.findByCourtCode("CL01").get().getHourlyRate();
            String validUpdate = String.format("""
                {
                    "courtCode": "CL01",
                    "courtName": "Sân Tiêu Chuẩn 1",
                    "hourlyRate": %s,
                    "status": "Trống",
                    "floorCondition": "Thảm thi đấu Taraflex vừa vệ sinh sạch",
                    "branchCode": "CN01"
                }
                """, currentPrice);

            mockMvc.perform(post("/api/courts")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(validUpdate))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Admin API Permissions: Lay ma tran va cap nhat pham vi da chi nhanh (CN01,CN02)")
    void testAdminPermissionsGetAndPostMultiBranch() throws Exception {
        mockMvc.perform(get("/api/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.branches", notNullValue()))
                .andExpect(jsonPath("$.users", notNullValue()));

        String payload = """
            [
                {
                    "username": "manager",
                    "branchCode": "CN01,CN02"
                }
            ]
            """;

        mockMvc.perform(post("/api/admin/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        vn.yain.entity.User updatedManager = userRepository.findByUsername("manager").orElse(null);
        org.junit.jupiter.api.Assertions.assertNotNull(updatedManager);
        assertEquals("CN01,CN02", updatedManager.getBranchCode());
    }
}

package vn.yain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.yain.controller.ApiController;
import vn.yain.dto.ChatMessageDto;
import vn.yain.entity.Booking;
import vn.yain.entity.Branch;
import vn.yain.entity.Court;
import vn.yain.repository.BookingRepository;
import vn.yain.repository.BranchRepository;
import vn.yain.repository.CourtRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ChatbotAndCustomerSupportTest {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ApiController apiController;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(apiController).build();

        bookingRepository.deleteAll();

        if (branchRepository.findByBranchCode("CN01").isEmpty()) {
            Branch b1 = new Branch();
            b1.setBranchCode("CN01");
            b1.setBranchName("Chi nhánh 1 (Thủ Đức - Số 1 Võ Văn Ngân)");
            b1.setAddress("Số 1 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức");
            b1.setPhone("0903123456");
            b1.setStatus("Hoạt động");
            branchRepository.save(b1);
        }

        Court c1 = courtRepository.findByCourtCode("CL01").orElseGet(Court::new);
        c1.setCourtCode("CL01");
        c1.setCourtName("Sân 1 (VIP Yonex)");
        c1.setCourtType("VIP");
        c1.setBranchCode("CN01");
        c1.setStatus("Trống");
        c1.setHourlyRate(new BigDecimal("140000"));
        courtRepository.save(c1);
    }

    @Test
    @DisplayName("Chatbot: Lời chào mở đầu khi người dùng mới vào cuộc trò chuyện")
    void testChatbotGreeting() {
        ChatMessageDto reply = chatbotService.processMessage("", "test_sess_01");
        assertNotNull(reply);
        assertEquals("bot", reply.getSender());
        assertTrue(reply.getText().contains("Xin chào bạn. Tôi là trợ lý ảo chính thức"));
        assertNotNull(reply.getQuickReplies());
        assertTrue(reply.getQuickReplies().size() >= 3);
    }

    @Test
    @DisplayName("Chatbot: Tư vấn chi nhánh gần nhất theo vị trí địa lý của khách hàng")
    void testBranchRecommendationIntent() {
        ChatMessageDto reply = chatbotService.processMessage("Tôi đang ở đường Hoàng Diệu 2 thì chi nhánh nào gần tôi nhất?", "test_sess_02");
        assertNotNull(reply);
        assertEquals("RECOMMEND_BRANCH", reply.getIntent());
        assertTrue(reply.getText().contains("Chi nhánh 1 (Thủ Đức - Số 1 Võ Văn Ngân)"));
    }

    @Test
    @DisplayName("Chatbot: Tra cứu bảng giá thuê sân theo khung giờ")
    void testPricingLookupIntent() {
        ChatMessageDto reply = chatbotService.processMessage("Bảng giá thuê sân và giờ vàng tính như thế nào?", "test_sess_03");
        assertNotNull(reply);
        assertEquals("PRICING", reply.getIntent());
        assertTrue(reply.getText().contains("Biểu phí thuê sân cầu lông tiêu chuẩn"));
        assertTrue(reply.getText().contains("Giờ Vàng"));
    }

    @Test
    @DisplayName("Chatbot: Tra cứu sân trống theo khung giờ cụ thể và đề xuất đặt sân")
    void testCourtAvailabilitySpecificIntent() {
        ChatMessageDto reply = chatbotService.processMessage("Hôm nay có sân nào trống từ 18h đến 20h không?", "test_sess_04");
        assertNotNull(reply);
        assertEquals("COURT_AVAILABILITY_SPECIFIC", reply.getIntent());
        assertTrue(reply.getText().contains("18:00 - 20:00"));
        assertTrue(reply.getText().contains("Ok đặt sân cho tôi"));
    }

    @Test
    @DisplayName("Chatbot: Xác nhận đặt sân tự động, tạo đơn giữ chỗ và sinh mã VietQR")
    void testBookingExecutionIntent() {
        String sessionId = "test_sess_booking_" + System.currentTimeMillis();

        // 1. Hỏi tìm sân trước
        chatbotService.processMessage("Hôm nay có sân nào trống từ 18h đến 20h không?", sessionId);

        // 2. Xác nhận đặt sân
        ChatMessageDto bookReply = chatbotService.processMessage("Ok đặt sân cho tôi", sessionId);
        assertNotNull(bookReply);
        assertEquals("BOOKING_EXECUTED", bookReply.getIntent());
        assertEquals("OPEN_PAYMENT", bookReply.getActionType());
        assertNotNull(bookReply.getPaymentData());

        String bookingCode = (String) bookReply.getPaymentData().get("bookingCode");
        assertNotNull(bookingCode);
        assertTrue(bookingCode.startsWith("DS"));

        String qrUrl = (String) bookReply.getPaymentData().get("qrUrl");
        assertNotNull(qrUrl);
        assertTrue(qrUrl.contains("vietqr.io"));

        // 3. Tra cứu lại mã đơn vừa tạo
        ChatMessageDto lookupReply = chatbotService.processMessage("Kiểm tra thông tin đơn " + bookingCode, sessionId);
        assertNotNull(lookupReply);
        assertEquals("LOOKUP_RESULT", lookupReply.getIntent());
        assertNotNull(lookupReply.getLookupData());
        assertEquals(bookingCode, lookupReply.getLookupData().get("bookingCode"));
    }

    @Test
    @DisplayName("Chatbot REST API: POST /api/chatbot/message và POST /api/chatbot/reset")
    void testChatbotApiEndpoints() throws Exception {
        Map<String, String> msgReq = Map.of(
                "message", "Quy định cọc tiền sân và chính sách hoàn cọc",
                "sessionId", "api_sess_01"
        );

        mockMvc.perform(post("/api/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sender", is("bot")))
                .andExpect(jsonPath("$.intent", is("POLICY")))
                .andExpect(jsonPath("$.text", containsString("Chính sách đặt cọc")));

        Map<String, String> resetReq = Map.of("sessionId", "api_sess_01");
        mockMvc.perform(post("/api/chatbot/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));
    }

    @Test
    @DisplayName("EmailService: Gửi vé điện tử E-Ticket xác nhận đặt sân với QR Code check-in")
    void testSendBookingTicketEmail() {
        Booking booking = new Booking();
        booking.setBookingCode("DS" + (System.currentTimeMillis() % 1000000));
        booking.setCustomerName("Lê Văn Triết");
        booking.setCustomerPhone("0908889999");
        booking.setBranchCode("CN01");
        booking.setCourtCode("CL01");
        booking.setBookingDate(LocalDate.now());
        booking.setTimeSlot("18:00 - 20:00");
        booking.setTotalPrice(new BigDecimal("280000"));
        booking.setDepositAmount(new BigDecimal("84000"));
        booking.setStatus("Đã cọc 30%");

        assertDoesNotThrow(() -> {
            emailService.sendBookingTicket(booking, "triet.developer@example.com");
        });
    }
}

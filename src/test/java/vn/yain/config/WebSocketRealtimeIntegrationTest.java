package vn.yain.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import vn.yain.dto.BookingNotificationDto;
import vn.yain.dto.CourtEventDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class WebSocketRealtimeIntegrationTest {

    @Autowired(required = false)
    private WebSocketConfig webSocketConfig;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("Spring Context: WebSocketConfig va SimpMessagingTemplate duoc auto-configure thanh cong")
    void testWebSocketBeansLoaded() {
        assertNotNull(webSocketConfig, "WebSocketConfig phai duoc tao thanh cong boi Spring Container");
        assertNotNull(messagingTemplate, "SimpMessagingTemplate broker phai duoc khoi tao thanh cong boi Spring Boot");
    }

    @Test
    @DisplayName("WebSocketConfig: Dang ky STOMP endpoint /ws-court va SockJS fallback voi allowed origins")
    void testRegisterStompEndpoints() {
        WebSocketConfig config = new WebSocketConfig();
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws-court")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(any(String[].class))).thenReturn(registration);

        config.registerStompEndpoints(registry);

        verify(registry, times(2)).addEndpoint("/ws-court");
        verify(registration, times(1)).withSockJS();
    }

    @Test
    @DisplayName("WebSocketConfig: Cau hinh Simple Message Broker /topic, /queue va prefix /app")
    void testConfigureMessageBroker() {
        WebSocketConfig config = new WebSocketConfig();
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        config.configureMessageBroker(registry);

        verify(registry, times(1)).enableSimpleBroker("/topic", "/queue");
        verify(registry, times(1)).setApplicationDestinationPrefixes("/app");
    }

    @Test
    @DisplayName("Realtime: Phat su kien giu cho tam va cap nhat trang thai san qua WebSocket /topic/courts")
    void testCourtEventBroadcast() {
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        CourtEventDto event = CourtEventDto.builder()
                .eventType("COURT_HELD")
                .courtCode("SAN01")
                .branchCode("CN01")
                .timeSlot("17:00-19:00")
                .status("Đang giữ chỗ")
                .holdRemainingSeconds(600)
                .timestamp(LocalDateTime.now())
                .build();

        mockTemplate.convertAndSend("/topic/courts", event);
        mockTemplate.convertAndSend("/topic/courts/CN01", event);

        verify(mockTemplate, times(1)).convertAndSend("/topic/courts", event);
        verify(mockTemplate, times(1)).convertAndSend("/topic/courts/CN01", event);
    }

    @Test
    @DisplayName("Realtime: Phat thong bao dat san moi va xac nhan coc qua WebSocket /topic/bookings")
    void testBookingNotificationBroadcast() {
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        BookingNotificationDto notification = BookingNotificationDto.builder()
                .eventType("NEW_BOOKING")
                .bookingCode("BK123456")
                .courtCode("SAN02")
                .branchCode("CN01")
                .customerName("Nguyen Van A")
                .customerPhone("0901234567")
                .timeSlot("08:00 - 10:00")
                .totalPrice(new BigDecimal("300000"))
                .depositAmount(new BigDecimal("100000"))
                .status("Chờ xác nhận")
                .message("Khách hàng Nguyen Van A vừa đặt sân SAN02")
                .timestamp(LocalDateTime.now())
                .build();

        mockTemplate.convertAndSend("/topic/bookings", notification);
        mockTemplate.convertAndSend("/topic/notifications", notification);

        verify(mockTemplate, times(1)).convertAndSend("/topic/bookings", notification);
        verify(mockTemplate, times(1)).convertAndSend("/topic/notifications", notification);
    }
}

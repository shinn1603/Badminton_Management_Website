package vn.yain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingNotificationDto {
    private String eventType;
    private String bookingCode;
    private String courtCode;
    private String branchCode;
    private String customerName;
    private String customerPhone;
    private String timeSlot;
    private BigDecimal totalPrice;
    private BigDecimal depositAmount;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}

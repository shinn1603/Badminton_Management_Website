package vn.yain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtEventDto {
    private String eventType;
    private String courtCode;
    private String branchCode;
    private String timeSlot;
    private String status;
    private String customerName;
    private String bookingCode;
    private Integer holdRemainingSeconds;
    private LocalDateTime timestamp;
}

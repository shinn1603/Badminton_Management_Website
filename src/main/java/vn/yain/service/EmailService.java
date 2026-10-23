package vn.yain.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import vn.yain.entity.*;
import vn.yain.repository.BranchRepository;
import vn.yain.repository.CourtRepository;
import vn.yain.repository.CustomerRepository;
import vn.yain.repository.UserRepository;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private CourtRepository courtRepository;

    @Autowired(required = false)
    private BranchRepository branchRepository;

    @Autowired(required = false)
    private CustomerRepository customerRepository;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Value("${spring.mail.username:utesport.badminton@gmail.com}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    /**
     * Gửi email HTML bất đồng bộ (Non-blocking)
     */
    public CompletableFuture<Boolean> sendHtmlEmailAsync(String to, String subject, String htmlBody) {
        return CompletableFuture.supplyAsync(() -> sendHtmlEmail(to, subject, htmlBody));
    }

    /**
     * Gửi email HTML đồng bộ, tự động fallback sang chế độ mô phỏng log nếu SMTP chưa cấu hình
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        if (to == null || to.trim().isEmpty() || !to.contains("@")) {
            log.warn("[EMAIL SERVICE] Địa chỉ email nhận không hợp lệ: {}", to);
            return false;
        }

        String recipient = to.trim();

        boolean hasSmtpConfig = mailSender != null && mailPassword != null && !mailPassword.isBlank();

        if (hasSmtpConfig) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromEmail, "UTE Sport Badminton Arena");
                helper.setTo(recipient);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);

                mailSender.send(mimeMessage);
                log.info("[EMAIL SERVICE] Đã gửi email thành công tới: {} | Tiêu đề: {}", recipient, subject);
                return true;
            } catch (Exception e) {
                log.error("[EMAIL SERVICE] Lỗi khi gửi qua SMTP (chuyển sang mô phỏng log): {}", e.getMessage());
            }
        }

        // Chế độ mô phỏng (Development / Offline Logger)
        log.info("================================================================================");
        log.info("[EMAIL SIMULATION - UTE SPORT NOTIFICATION]");
        log.info("From: {} (UTE Sport Badminton Arena)", fromEmail);
        log.info("To: {}", recipient);
        log.info("Subject: {}", subject);
        log.info("Timestamp: {}", java.time.LocalDateTime.now().format(TIME_FMT));
        log.info("Status: SIMULATED_DELIVERED_SUCCESSFULLY");
        log.info("================================================================================");
        return true;
    }

    // =========================================================================
    // 1. GỬI MÃ OTP / THÔNG BÁO QUÊN MẬT KHẨU
    // =========================================================================

    public void sendPasswordResetOtp(String toEmail, String fullName, String otpCode) {
        String safeName = (fullName != null && !fullName.isBlank()) ? fullName : "Quý khách";
        String subject = "🔑 [UTE Sport] Mã xác nhận khôi phục mật khẩu tài khoản: " + otpCode;

        String html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f1f5f9; margin: 0; padding: 24px; color: #1e293b;">
                <div style="max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 14px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.08); border: 1px solid #e2e8f0;">
                    <div style="background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); padding: 28px 24px; text-align: center; border-bottom: 3px solid #10b981;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 22px; font-weight: 800; letter-spacing: 0.5px;">🏸 UTE SPORT BADMINTON ARENA</h1>
                        <p style="color: #94a3b8; margin: 6px 0 0; font-size: 13px;">Hệ Thống Quản Lý Chuỗi Trung Tâm Thể Thao Cao Cấp</p>
                    </div>
                    <div style="padding: 28px 24px;">
                        <h2 style="color: #0f172a; font-size: 18px; margin-top: 0;">Yêu Cầu Đặt Lại Mật Khẩu</h2>
                        <p style="font-size: 14px; line-height: 1.6; color: #334155;">
                            Xin chào <strong>%s</strong>,<br>
                            Hệ thống UTE Sport nhận được yêu cầu khôi phục mật khẩu cho tài khoản đăng ký với email này. Vui lòng sử dụng mã xác nhận bên dưới để hoàn tất:
                        </p>
                        <div style="margin: 24px 0; text-align: center;">
                            <div style="display: inline-block; background: #f8fafc; border: 2px dashed #10b981; border-radius: 12px; padding: 14px 32px; font-size: 32px; font-weight: 800; letter-spacing: 6px; color: #047857;">
                                %s
                            </div>
                            <p style="font-size: 12px; color: #64748b; margin: 8px 0 0;">Mã có hiệu lực trong vòng <strong>10 phút</strong>.</p>
                        </div>
                        <p style="font-size: 13px; line-height: 1.6; color: #64748b; background: #fef2f2; border-left: 4px solid #ef4444; padding: 10px 14px; border-radius: 4px;">
                            ⚠️ <strong>Cảnh báo bảo mật:</strong> Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email hoặc liên hệ ngay hotline <strong>0903.123.456</strong> để khóa tài khoản khẩn cấp.
                        </p>
                    </div>
                    <div style="background: #f8fafc; padding: 16px 24px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #64748b;">
                        © 2026 UTE Sport Badminton Club | Chi nhánh chính: 01 Võ Văn Ngân, TP. Thủ Đức<br>
                        Hotline hỗ trợ: 0903.123.456 - Website: utesport.vn
                    </div>
                </div>
            </body>
            </html>
            """.formatted(safeName, otpCode);

        sendHtmlEmailAsync(toEmail, subject, html);
    }

    public void sendPasswordChangedNotification(String toEmail, String fullName, String newPassword) {
        String safeName = (fullName != null && !fullName.isBlank()) ? fullName : "Quý khách";
        String subject = "🔒 [UTE Sport] Thông báo: Mật khẩu của bạn đã được thay đổi thành công";

        String passNote = (newPassword != null && !newPassword.isBlank())
                ? "<p style=\"background: #ecfdf5; border: 1px solid #10b981; padding: 10px 14px; border-radius: 8px; font-size: 13px; color: #065f46;\">Mật khẩu mới của bạn là: <strong style=\"font-size: 15px;\">" + newPassword + "</strong></p>"
                : "";

        String html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f1f5f9; margin: 0; padding: 24px; color: #1e293b;">
                <div style="max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 14px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.08); border: 1px solid #e2e8f0;">
                    <div style="background: #0f172a; padding: 24px; text-align: center; border-bottom: 3px solid #10b981;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 20px; font-weight: 800;">🏸 UTE SPORT BADMINTON ARENA</h1>
                    </div>
                    <div style="padding: 26px 24px;">
                        <h2 style="color: #047857; font-size: 17px; margin-top: 0;">Cập Nhật Mật Khẩu Thành Công</h2>
                        <p style="font-size: 14px; line-height: 1.6; color: #334155;">
                            Xin chào <strong>%s</strong>,<br>
                            Mật khẩu tài khoản của bạn tại hệ thống UTE Sport đã được thay đổi thành công lúc <strong>%s</strong>.
                        </p>
                        %s
                        <p style="font-size: 13px; color: #475569;">
                            Bây giờ bạn có thể đăng nhập vào ứng dụng và đặt sân bình thường. Nếu bạn không thực hiện thay đổi này, hãy liên hệ ngay với Ban quản lý.
                        </p>
                    </div>
                    <div style="background: #f8fafc; padding: 14px 24px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #64748b;">
                        © 2026 UTE Sport | Hotline hỗ trợ 24/7: 0903.123.456
                    </div>
                </div>
            </body>
            </html>
            """.formatted(safeName, java.time.LocalDateTime.now().format(TIME_FMT), passNote);

        sendHtmlEmailAsync(toEmail, subject, html);
    }

    // =========================================================================
    // 2. GỬI PHIẾU XÁC NHẬN ĐẶT SÂN & VÉ ĐIỆN TỬ (E-TICKET BM-KH02)
    // =========================================================================

    public void sendBookingTicket(Booking booking, String targetEmail) {
        if (booking != null && targetEmail != null && !targetEmail.isBlank()) {
            booking.setCustomerEmail(targetEmail);
        }
        sendBookingConfirmation(booking);
    }

    public void sendBookingConfirmation(Booking booking) {
        if (booking == null) return;

        String toEmail = booking.getCustomerEmail();
        if (toEmail == null || toEmail.isBlank() || !toEmail.contains("@")) {
            if (booking.getCustomerCode() != null && customerRepository != null) {
                Customer c = customerRepository.findByCustomerCode(booking.getCustomerCode()).orElse(null);
                if (c != null && c.getEmail() != null && c.getEmail().contains("@")) {
                    toEmail = c.getEmail();
                }
            }
            if ((toEmail == null || toEmail.isBlank()) && booking.getCustomerPhone() != null && userRepository != null) {
                User u = userRepository.findByPhone(booking.getCustomerPhone()).orElse(null);
                if (u != null && u.getEmail() != null && u.getEmail().contains("@")) {
                    toEmail = u.getEmail();
                }
            }
        }

        if (toEmail == null || toEmail.isBlank() || !toEmail.contains("@")) {
            log.info("[EMAIL SERVICE] Đơn đặt sân [{}] không có email khách hàng, bỏ qua gửi mail.", booking.getBookingCode());
            return;
        }

        Court court = (courtRepository != null && booking.getCourtCode() != null)
                ? courtRepository.findByCourtCode(booking.getCourtCode()).orElse(null) : null;
        String courtName = court != null ? court.getCourtName() : ("Sân " + booking.getCourtCode());
        String courtType = court != null ? court.getCourtType() : "Tiêu chuẩn";

        Branch branch = (branchRepository != null && booking.getBranchCode() != null)
                ? branchRepository.findByBranchCode(booking.getBranchCode()).orElse(null) : null;
        String branchName = branch != null ? branch.getBranchName() : ("Chi nhánh " + booking.getBranchCode());
        String branchAddr = branch != null ? branch.getAddress() : "Khu liên hợp thể thao UTE";

        BigDecimal total = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal deposit = booking.getDepositAmount() != null ? booking.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal remaining = total.subtract(deposit);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        String dateStr = booking.getBookingDate() != null ? booking.getBookingDate().format(DATE_FMT) : "Hôm nay";
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=" + booking.getBookingCode();

        String subject = "🎫 [UTE Sport] Vé điện tử E-Ticket xác nhận đặt sân: " + booking.getBookingCode();

        String html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f1f5f9; margin: 0; padding: 24px; color: #1e293b;">
                <div style="max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.08); border: 1px solid #e2e8f0;">
                    <div style="background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); padding: 24px; text-align: center; border-bottom: 3px solid #10b981;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 21px; font-weight: 800; letter-spacing: 0.5px;">🏸 UTE SPORT BADMINTON ARENA</h1>
                        <p style="color: #10b981; font-weight: 700; margin: 6px 0 0; font-size: 14px; text-transform: uppercase; letter-spacing: 1px;">
                            PHIẾU XÁC NHẬN ĐẶT SÂN & VÉ ĐIỆN TỬ (E-TICKET)
                        </p>
                    </div>
                    <div style="padding: 24px;">
                        <div style="background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 12px; padding: 14px 18px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <span style="font-size: 12px; color: #047857; font-weight: 600; text-transform: uppercase;">MÃ ĐẶT SÂN / CHECK-IN</span><br>
                                <strong style="font-size: 22px; color: #065f46; letter-spacing: 1px;">%s</strong>
                            </div>
                            <div style="text-align: right;">
                                <span style="display: inline-block; background: #10b981; color: #ffffff; font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 20px;">
                                    %s
                                </span>
                            </div>
                        </div>
                        <table style="width: 100%%; border-collapse: collapse; font-size: 13.5px; margin-bottom: 20px;">
                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                <td style="padding: 9px 0; color: #64748b; width: 38%%;">Khách hàng:</td>
                                <td style="padding: 9px 0; font-weight: 600; color: #0f172a;">%s (%s)</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                <td style="padding: 9px 0; color: #64748b;">Chi nhánh:</td>
                                <td style="padding: 9px 0; font-weight: 600; color: #0f172a;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                <td style="padding: 9px 0; color: #64748b;">Địa chỉ cơ sở:</td>
                                <td style="padding: 9px 0; color: #334155;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                <td style="padding: 9px 0; color: #64748b;">Sân thi đấu:</td>
                                <td style="padding: 9px 0; font-weight: 700; color: #047857;">%s <span style="font-weight: normal; font-size: 12px; color: #64748b;">(%s)</span></td>
                            </tr>
                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                <td style="padding: 9px 0; color: #64748b;">Ngày chơi:</td>
                                <td style="padding: 9px 0; font-weight: 700; color: #0f172a;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                <td style="padding: 9px 0; color: #64748b;">Khung giờ thi đấu:</td>
                                <td style="padding: 9px 0; font-weight: 700; color: #2563eb;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                <td style="padding: 9px 0; color: #64748b;">Tổng giá trị đơn:</td>
                                <td style="padding: 9px 0; font-weight: 600; color: #0f172a;">%,d đ</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #f1f5f9; background: #f8fafc;">
                                <td style="padding: 9px 8px; color: #047857; font-weight: 600;">Tiền cọc đã thanh toán (30%%):</td>
                                <td style="padding: 9px 8px; font-weight: 800; color: #047857;">%,d đ <span style="font-size: 11px; font-weight: normal;">(VietQR Pro)</span></td>
                            </tr>
                            <tr style="background: #fffbeb;">
                                <td style="padding: 9px 8px; color: #b45309; font-weight: 600;">Còn lại thanh toán tại quầy (70%%):</td>
                                <td style="padding: 9px 8px; font-weight: 800; color: #b45309;">%,d đ</td>
                            </tr>
                        </table>
                        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px; text-align: center; margin-bottom: 20px;">
                            <p style="margin: 0 0 10px; font-size: 13px; font-weight: 700; color: #0f172a;">MÃ QR CHECK-IN NHẬN SÂN 1 CHẠM</p>
                            <img src="%s" alt="QR Code" style="width: 140px; height: 140px; border: 4px solid #ffffff; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.08); display: inline-block;">
                            <p style="margin: 10px 0 0; font-size: 12px; color: #64748b;">
                                Quý khách vui lòng xuất trình mã QR này tại quầy lễ tân để nhân viên quét nhận sân nhanh chóng.
                            </p>
                        </div>
                        <div style="font-size: 12px; color: #64748b; line-height: 1.5; border-left: 3px solid #3b82f6; padding-left: 10px;">
                            * <strong>Lưu ý:</strong> Quý khách vui lòng có mặt trước giờ chơi 10 phút. Hủy đặt sân trước giờ thi đấu tối thiểu 4 tiếng sẽ được hoàn 100%% tiền cọc tự động theo quy định <strong>QĐ-KH03</strong>.
                        </div>
                    </div>
                    <div style="background: #0f172a; padding: 16px 24px; text-align: center; font-size: 12px; color: #94a3b8;">
                        Cảm ơn quý khách đã tin tưởng lựa chọn UTE Sport!<br>
                        Hotline hỗ trợ: <strong>0903.123.456</strong> | Email: support@utesport.vn
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                booking.getBookingCode(),
                booking.getStatus() != null ? booking.getStatus() : "Đã xác nhận",
                booking.getCustomerName() != null ? booking.getCustomerName() : "Khách hàng",
                booking.getCustomerPhone() != null ? booking.getCustomerPhone() : "",
                branchName,
                branchAddr,
                courtName,
                courtType,
                dateStr,
                booking.getTimeSlot() != null ? booking.getTimeSlot() : "",
                total.longValue(),
                deposit.longValue(),
                remaining.longValue(),
                qrUrl
            );

        sendHtmlEmailAsync(toEmail, subject, html);
    }
}

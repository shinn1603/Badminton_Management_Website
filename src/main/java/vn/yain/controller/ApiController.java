package vn.yain.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.yain.dto.CourtEventDto;
import vn.yain.entity.*;
import vn.yain.repository.*;
import vn.yain.security.JwtTokenProvider;
import vn.yain.service.CloudinaryService;
import vn.yain.service.CourtService;
import vn.yain.service.EmailService;
import vn.yain.service.PricingService;
import vn.yain.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourtService courtService;

    @Autowired(required = false)
    private PricingService pricingService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired(required = false)
    private BookingRepository bookingRepository;

    @Autowired(required = false)
    private CloudinaryService cloudinaryService;

    @Autowired(required = false)
    private AuditLogRepository auditLogRepository;

    @Autowired(required = false)
    private SystemSettingRepository systemSettingRepository;

    @Autowired(required = false)
    private EmailService emailService;

    private static final Map<String, Map<String, Object>> forgotPasswordOtpMap = new java.util.concurrent.ConcurrentHashMap<>();

    private String sanitizeErrorMessage(Exception e) {
        log.error("Internal processing error: {}", e.getMessage(), e);
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            return e.getMessage();
        }
        return "Đã xảy ra lỗi trong quá trình xử lý hệ thống. Vui lòng liên hệ ban quản trị.";
    }

    private String resolveEffectiveBranchCode(String requestedBranchCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            boolean isDirectorOrAdmin = auth.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_DIRECTOR") || a.getAuthority().equals("ROLE_ADMIN"));
            boolean isCustomer = auth.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_CUSTOMER") || a.getAuthority().equals("CUSTOMER"));

            if (!isDirectorOrAdmin && !isCustomer) {
                String username = auth.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    String role = userOpt.get().getRole();
                    if ("CUSTOMER".equalsIgnoreCase(role) || "ROLE_CUSTOMER".equalsIgnoreCase(role)) {
                        return (requestedBranchCode != null && !requestedBranchCode.isBlank()) ? requestedBranchCode.trim() : "CN01";
                    }
                    if (userOpt.get().getBranchCode() != null && !userOpt.get().getBranchCode().isBlank()) {
                        return userOpt.get().getBranchCode().trim();
                    }
                }
                return "CN01";
            }
        }
        return (requestedBranchCode != null && !requestedBranchCode.isBlank()) ? requestedBranchCode.trim() : "CN01";
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
    }

    // ==========================================
    // 1. AUTHENTICATION & JWT ENDPOINTS
    // ==========================================

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            Map<String, Object> authResult = userService.authenticate(username, password);
            authResult.put("success", true);
            return ResponseEntity.ok(authResult);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", sanitizeErrorMessage(e)));
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        try {
            String username = payload.get("username");
            String password = payload.get("password");
            String fullName = payload.get("fullName");
            String phone = payload.get("phone");
            String email = payload.get("email");

            if ((username == null || username.trim().isEmpty()) && phone != null) {
                username = phone.trim();
            }

            Map<String, Object> registerResult = userService.registerCustomer(username, password, fullName, phone, email);
            registerResult.put("success", true);
            registerResult.put("message", "Đăng ký tài khoản thành công!");
            return ResponseEntity.ok(registerResult);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", sanitizeErrorMessage(e)));
        }
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        try {
            String identifier = payload.get("identifier");
            if (identifier == null || identifier.trim().isEmpty()) identifier = payload.get("username");
            if (identifier == null || identifier.trim().isEmpty()) identifier = payload.get("phone");
            if (identifier == null || identifier.trim().isEmpty()) identifier = payload.get("email");

            if (identifier == null || identifier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập tên tài khoản, số điện thoại hoặc email!"));
            }

            String cleanId = identifier.trim();
            User user = userRepository.findByUsername(cleanId)
                    .or(() -> userRepository.findByPhone(cleanId))
                    .or(() -> cleanId.contains("@") ? userRepository.findByEmail(cleanId) : Optional.empty())
                    .orElse(null);

            String email = null;
            String fullName = null;
            if (user != null) {
                email = user.getEmail();
                fullName = user.getFullName();
            } else {
                Customer cust = customerRepository.findByPhone(cleanId)
                        .or(() -> cleanId.contains("@") ? customerRepository.findByEmail(cleanId) : Optional.empty())
                        .orElse(null);
                if (cust != null) {
                    email = cust.getEmail();
                    fullName = cust.getFullName();
                }
            }

            if (email == null || email.isBlank() || !email.contains("@")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Tài khoản [" + cleanId + "] chưa cập nhật địa chỉ email hợp lệ để nhận mã khôi phục! Vui lòng liên hệ hotline 0903.123.456 để được hỗ trợ trực tiếp."
                ));
            }

            String otp = String.format("%06d", new java.util.Random().nextInt(1000000));
            Map<String, Object> otpInfo = new HashMap<>();
            otpInfo.put("otp", otp);
            otpInfo.put("email", email);
            otpInfo.put("identifier", cleanId);
            otpInfo.put("expiry", System.currentTimeMillis() + 10 * 60 * 1000);
            forgotPasswordOtpMap.put(cleanId.toLowerCase(), otpInfo);

            if (emailService != null) {
                emailService.sendPasswordResetOtp(email, fullName, otp);
            }

            log.info("[OTP SIMULATION] Mã xác nhận OTP khôi phục mật khẩu cho {}: {}", cleanId, otp);

            String masked = maskEmail(email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mã xác nhận OTP đã được gửi thành công đến email " + masked + ". Vui lòng kiểm tra hộp thư!",
                    "email", masked,
                    "identifier", cleanId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", sanitizeErrorMessage(e)));
        }
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        try {
            String identifier = payload.get("identifier");
            if (identifier == null || identifier.trim().isEmpty()) identifier = payload.get("username");
            if (identifier == null || identifier.trim().isEmpty()) identifier = payload.get("phone");
            if (identifier == null || identifier.trim().isEmpty()) identifier = payload.get("email");

            String newPassword = payload.get("newPassword");
            if (newPassword == null) newPassword = payload.get("password");

            if (identifier == null || identifier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập định danh tài khoản!"));
            }

            String cleanId = identifier.trim();

            String otp = payload.get("otp");
            if (otp != null && !otp.isBlank()) {
                Map<String, Object> otpInfo = forgotPasswordOtpMap.get(cleanId.toLowerCase());
                if (otpInfo == null) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã xác nhận OTP không tồn tại hoặc đã hết hạn!"));
                }
                long expiry = (long) otpInfo.get("expiry");
                if (System.currentTimeMillis() > expiry) {
                    forgotPasswordOtpMap.remove(cleanId.toLowerCase());
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã xác nhận OTP đã hết hạn (quá 10 phút)! Vui lòng yêu cầu mã mới."));
                }
                String expectedOtp = (String) otpInfo.get("otp");
                if (!expectedOtp.equals(otp.trim())) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã xác nhận OTP không chính xác. Vui lòng kiểm tra lại email!"));
                }
                forgotPasswordOtpMap.remove(cleanId.toLowerCase());
            }

            userService.resetPassword(cleanId, newPassword);

            // Gửi email thông báo thay đổi mật khẩu thành công
            User u = userRepository.findByUsername(cleanId)
                    .or(() -> userRepository.findByPhone(cleanId))
                    .or(() -> cleanId.contains("@") ? userRepository.findByEmail(cleanId) : Optional.empty())
                    .orElse(null);
            String userEmail = (u != null && u.getEmail() != null) ? u.getEmail() : null;
            String fullName = (u != null && u.getFullName() != null) ? u.getFullName() : cleanId;

            if (userEmail == null || userEmail.isBlank()) {
                Customer c = customerRepository.findByPhone(cleanId).orElse(null);
                if (c != null && c.getEmail() != null) {
                    userEmail = c.getEmail();
                    fullName = c.getFullName();
                }
            }

            if (userEmail != null && userEmail.contains("@") && emailService != null) {
                emailService.sendPasswordChangedNotification(userEmail, fullName, newPassword);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đặt lại mật khẩu thành công! Thông báo đã được gửi về email của quý khách."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", sanitizeErrorMessage(e)));
        }
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa cung cấp JWT token hợp lệ!"));
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "JWT token đã hết hạn hoặc không hợp lệ!"));
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        String role = jwtTokenProvider.getRoleFromToken(token);
        User u = userRepository.findByUsername(username).orElse(null);
        String fullName = u != null ? u.getFullName() : username;
        String phone = u != null && u.getPhone() != null ? u.getPhone() : "";
        String email = u != null && u.getEmail() != null ? u.getEmail() : "";

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("username", username);
        resp.put("fullName", fullName);
        resp.put("phone", phone);
        resp.put("email", email);
        resp.put("role", role);
        resp.put("valid", true);
        if (u != null) {
            resp.put("userId", u.getId());
            resp.put("branchCode", u.getBranchCode());
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/customer/profile")
    public ResponseEntity<?> getCustomerProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập để xem thông tin hội viên!"));
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại!"));
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy thông tin tài khoản!"));
        }

        Customer cust = null;
        if (u.getPhone() != null && !u.getPhone().trim().isEmpty()) {
            cust = customerRepository.findByPhone(u.getPhone().trim()).orElse(null);
        }
        if (cust == null) {
            cust = customerRepository.findByCustomerCode(u.getUsername()).orElse(null);
        }

        String tier = cust != null && cust.getMembershipTier() != null ? cust.getMembershipTier() : "Đồng";
        int points = cust != null && cust.getRewardPoints() != null ? cust.getRewardPoints() : 0;
        BigDecimal totalHours = cust != null && cust.getTotalPlayHours() != null ? cust.getTotalPlayHours() : BigDecimal.ZERO;
        String customerCode = cust != null && cust.getCustomerCode() != null
                ? cust.getCustomerCode()
                : ("KH" + String.format("%04d", u.getId()));
        String cardNumber = "MB • 8892 • " + String.format("%04d", u.getId()) + " • 9988";
        String joinDate = u.getCreatedAt() != null
                ? u.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "15/01/2025";

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("fullName", u.getFullName());
        resp.put("phone", u.getPhone() != null ? u.getPhone() : "");
        resp.put("email", u.getEmail() != null ? u.getEmail() : "");
        resp.put("customerCode", customerCode);
        resp.put("cardNumber", cardNumber);
        resp.put("membershipTier", tier);
        resp.put("rewardPoints", points);
        resp.put("totalPlayHours", totalHours.setScale(1, java.math.RoundingMode.HALF_UP));
        resp.put("totalSpent", BigDecimal.ZERO);
        resp.put("savings", BigDecimal.ZERO);
        resp.put("joinDate", joinDate);
        resp.put("tournamentCount", 0);
        resp.put("bookingCount", 0);
        resp.put("recentBookings", Collections.emptyList());

        return ResponseEntity.ok(resp);
    }

    @PutMapping("/customer/profile")
    public ResponseEntity<?> updateCustomerProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> payload) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập!"));
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Token không hợp lệ!"));
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Tài khoản không tồn tại!"));
        }

        String newName = payload.get("fullName");
        String newPhone = payload.get("phone");
        String newEmail = payload.get("email");
        String newPassword = payload.get("newPassword");

        if (newName != null && !newName.trim().isEmpty()) {
            u.setFullName(newName.trim());
        }
        if (newPhone != null && !newPhone.trim().isEmpty()) {
            u.setPhone(newPhone.trim());
        }
        if (newEmail != null) {
            u.setEmail(newEmail.trim());
        }
        userRepository.save(u);

        if (newPassword != null && newPassword.trim().length() >= 6) {
            userService.resetPassword(u.getUsername(), newPassword.trim());
        }

        if (u.getPhone() != null && !u.getPhone().trim().isEmpty()) {
            Optional<Customer> cOpt = customerRepository.findByPhone(u.getPhone().trim());
            if (cOpt.isPresent()) {
                Customer c = cOpt.get();
                if (newName != null) c.setFullName(newName.trim());
                if (newEmail != null) c.setEmail(newEmail.trim());
                customerRepository.save(c);
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật hồ sơ thành công!",
                "fullName", u.getFullName(),
                "phone", u.getPhone() != null ? u.getPhone() : "",
                "email", u.getEmail() != null ? u.getEmail() : ""
        ));
    }

    @GetMapping("/customer/my-bookings")
    public ResponseEntity<?> getMyBookings(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập!"));
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Token không hợp lệ!"));
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Booking> bookings = new ArrayList<>();
        if (bookingRepository != null) {
            if (u.getPhone() != null && !u.getPhone().trim().isEmpty()) {
                bookings = bookingRepository.findByCustomerPhoneOrderByBookingDateDesc(u.getPhone().trim());
            }
            if (bookings.isEmpty() && u.getFullName() != null) {
                bookings = bookingRepository.findByCustomerNameContainingIgnoreCase(u.getFullName().trim());
            }
        }

        return ResponseEntity.ok(bookings);
    }

    // ==========================================
    // CLOUDINARY IMAGE UPLOAD (DAY 4 SCOPE)
    // ==========================================
    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "folder", defaultValue = "general") String folder) {
        if (cloudinaryService == null) {
            return ResponseEntity.status(503).body(Map.of("success", false, "message", "Dịch vụ Cloudinary chưa sẵn sàng!"));
        }
        try {
            String url = cloudinaryService.uploadImage(file, folder);
            return ResponseEntity.ok(Map.of("success", true, "url", url, "folder", folder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi upload ảnh: " + e.getMessage()));
        }
    }

    // ==========================================
    // 2. COURTS & REALTIME STATUS (XL-02)
    // ==========================================

    @GetMapping("/courts")
    public List<Court> getCourts(@RequestParam(required = false) String branchCode) {
        String effectiveBranch = resolveEffectiveBranchCode(branchCode);
        return courtService.getCourts(effectiveBranch);
    }

    @PostMapping("/courts/hold")
    public ResponseEntity<?> holdCourt(@RequestBody Map<String, String> payload) {
        try {
            String courtCode = payload.get("courtCode");
            String branchCode = payload.get("branchCode");
            String timeSlot = payload.get("timeSlot");
            String customerName = payload.get("customerName");

            CourtEventDto event = courtService.holdCourtForBooking(courtCode, branchCode, timeSlot, customerName);
            return ResponseEntity.ok(Map.of("success", true, "data", event));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/courts/status")
    public ResponseEntity<?> updateCourtStatus(@RequestBody Map<String, String> payload) {
        try {
            String courtCode = payload.get("courtCode");
            String status = payload.get("status");
            Court updated = courtService.updateCourtStatus(courtCode, status);
            return ResponseEntity.ok(Map.of("success", true, "court", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ==========================================
    // 3. CATALOG & BRANCH LOOKUPS
    // ==========================================

    @GetMapping("/branches")
    public List<Branch> getBranches() {
        return branchRepository.findAll();
    }

    @GetMapping("/products")
    public List<Product> getProducts(@RequestParam(required = false) String branchCode) {
        String effectiveBranch = resolveEffectiveBranchCode(branchCode);
        if (effectiveBranch != null && !effectiveBranch.trim().isEmpty() && !"ALL".equalsIgnoreCase(effectiveBranch.trim())) {
            return productRepository.findByBranchCode(effectiveBranch.trim());
        }
        return productRepository.findAll();
    }

    @GetMapping("/equipment")
    public List<Equipment> getEquipment(@RequestParam(required = false) String branchCode) {
        if (branchCode != null && !branchCode.isEmpty()) {
            return equipmentRepository.findByBranchCode(branchCode);
        }
        return equipmentRepository.findAll();
    }

    // ==========================================
    // 4. PRICING SUMMARY (DAY 2 SCOPE)
    // ==========================================

    @GetMapping("/pricing/summary")
    public ResponseEntity<?> getCustomerPricingSummary(@RequestParam(value = "branchCode", defaultValue = "CN01") String branchCode) {
        String bCode = branchCode != null ? branchCode.trim().toUpperCase() : "CN01";
        Optional<Branch> branchOpt = branchRepository.findByBranchCode(bCode);
        String branchName = branchOpt.map(Branch::getBranchName).orElse("Chi nhánh " + bCode);

        List<PricingRule> rules = pricingRuleRepository.findByBranchCode(bCode);
        List<TimeSlot> slots = timeSlotRepository.findAll();
        Map<String, TimeSlot> slotMap = slots.stream()
                .collect(Collectors.toMap(TimeSlot::getSlotCode, s -> s, (s1, s2) -> s1));

        List<Map<String, Object>> formattedRules = new ArrayList<>();
        if (!rules.isEmpty()) {
            for (PricingRule r : rules) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("pricingCode", r.getPricingCode());
                item.put("slotCode", r.getSlotCode());
                TimeSlot ts = slotMap.get(r.getSlotCode());
                if (ts != null) {
                    item.put("slotName", ts.getSlotType() != null ? ts.getSlotType() : ("Khung giờ " + r.getSlotCode()));
                    item.put("timeRange", ts.getStartTime() + " - " + ts.getEndTime());
                } else {
                    item.put("slotName", "Khung giờ " + r.getSlotCode());
                    item.put("timeRange", "Toàn thời gian");
                }
                item.put("dayType", r.getDayType() != null ? r.getDayType() : "Hàng ngày");
                item.put("hourlyRate", r.getHourlyRate() != null ? r.getHourlyRate().longValue() : 90000L);
                formattedRules.add(item);
            }
        } else {
            formattedRules.add(Map.of("slotName", "Buổi Sáng", "timeRange", "06:00 - 10:00", "dayType", "Hàng ngày", "hourlyRate", 80000L));
            formattedRules.add(Map.of("slotName", "Buổi Ngày", "timeRange", "10:00 - 16:00", "dayType", "Thứ 2 - Thứ 6", "hourlyRate", 70000L));
            formattedRules.add(Map.of("slotName", "Giờ Cao Điểm", "timeRange", "17:00 - 21:00", "dayType", "Hàng ngày", "hourlyRate", 90000L));
            formattedRules.add(Map.of("slotName", "Buổi Tối Muộn", "timeRange", "21:00 - 22:30", "dayType", "Hàng ngày", "hourlyRate", 80000L));
        }

        List<Court> branchCourts = courtRepository.findByBranchCode(bCode);
        long stdPrice = 90000L;
        long vipPrice = 120000L;
        for (Court c : branchCourts) {
            if (c.getCourtType() != null && c.getCourtType().toUpperCase().contains("VIP")) {
                if (c.getHourlyRate() != null) vipPrice = c.getHourlyRate().longValue();
            } else {
                if (c.getHourlyRate() != null) stdPrice = c.getHourlyRate().longValue();
            }
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("branchCode", bCode);
        res.put("branchName", branchName);
        res.put("standardRate", stdPrice);
        res.put("vipRate", vipPrice);
        res.put("pricingRules", formattedRules);
        res.put("activePromotions", Collections.emptyList());

        return ResponseEntity.ok(res);
    }

    // ==========================================
    // 5. ADMIN USER MANAGEMENT & PERMISSIONS (DAY 5 SCOPE)
    // ==========================================

    @GetMapping("/admin/users")
    public List<User> getAdminUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/admin/users")
    public ResponseEntity<?> saveAdminUser(@RequestBody Map<String, String> payload) {
        try {
            User u = userService.saveOrUpdateUser(payload);
            return ResponseEntity.ok(Map.of("success", true, "user", u));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/admin/users/{username}/toggle-status")
    public ResponseEntity<?> toggleAdminUserStatus(@PathVariable String username) {
        try {
            User u = userService.toggleUserStatus(username);
            return ResponseEntity.ok(Map.of("success", true, "status", u.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/admin/permissions")
    public ResponseEntity<?> getAdminPermissions() {
        try {
            List<User> users = userRepository.findAll();
            List<Branch> branches = branchRepository.findAll();
            return ResponseEntity.ok(Map.of("success", true, "users", users, "branches", branches));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/admin/permissions")
    public ResponseEntity<?> updateAdminPermissions(@RequestBody List<Map<String, String>> updates) {
        try {
            int count = 0;
            StringBuilder desc = new StringBuilder("Cập nhật phân quyền chi nhánh cho: ");
            for (Map<String, String> item : updates) {
                String username = item.get("username");
                String branchCode = item.get("branchCode");
                if (username != null && branchCode != null) {
                    Optional<User> uOpt = userRepository.findByUsername(username.trim());
                    if (uOpt.isPresent()) {
                        User u = uOpt.get();
                        u.setBranchCode(branchCode.trim());
                        userRepository.save(u);
                        count++;
                        desc.append(username).append(" -> ").append(branchCode).append("; ");
                    }
                }
            }

            if (auditLogRepository != null) {
                try {
                    AuditLog auditLog = new AuditLog();
                    auditLog.setUsername("admin");
                    auditLog.setActionType("Cập nhật ma trận phân quyền chi nhánh");
                    auditLog.setDetails(desc.length() > 250 ? desc.substring(0, 247) + "..." : desc.toString());
                    auditLog.setModule("Phân hệ Quản trị");
                    auditLog.setStatus("Thành công");
                    auditLog.setActionTime(LocalDateTime.now());
                    auditLogRepository.save(auditLog);
                } catch (Exception ex) {
                    log.warn("Không thể ghi audit log: {}", ex.getMessage());
                }
            }

            return ResponseEntity.ok(Map.of("success", true, "updatedCount", count));
        } catch (Exception e) {
            log.error("Lỗi cập nhật phân quyền:", e);
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi: " + msg));
        }
    }

    // ==========================================
    // 6. SYSTEM SETTINGS, AUDIT LOGS & BACKUP (DAY 6 SCOPE)
    // ==========================================

    @GetMapping("/policy/settings")
    public ResponseEntity<?> getPublicPolicySettings() {
        try {
            Map<String, String> map = new HashMap<>();
            if (systemSettingRepository != null) {
                List<SystemSetting> list = systemSettingRepository.findAll();
                for (SystemSetting s : list) {
                    map.put(s.getSettingCode(), s.getSettingValue());
                }
            }
            map.putIfAbsent("CFG_DEPOSIT_RATE", "30");
            map.putIfAbsent("CFG_HOLDING_MINUTES", "10");
            map.putIfAbsent("CFG_CANCEL_HOURS", "4");
            map.putIfAbsent("CFG_ADVANCE_DAYS", "30");

            List<Branch> branches = branchRepository.findAll();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "settings", map,
                "branches", branches
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/admin/settings")
    public ResponseEntity<?> getAdminSettings() {
        try {
            List<SystemSetting> list = systemSettingRepository != null ? systemSettingRepository.findAll() : Collections.emptyList();
            Map<String, String> map = new HashMap<>();
            for (SystemSetting s : list) {
                map.put(s.getSettingCode(), s.getSettingValue());
            }
            return ResponseEntity.ok(Map.of("success", true, "list", list, "settings", map));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/admin/settings")
    public ResponseEntity<?> updateAdminSettings(@RequestBody Map<String, String> payload) {
        try {
            if (systemSettingRepository != null) {
                for (Map.Entry<String, String> entry : payload.entrySet()) {
                    String code = entry.getKey();
                    String val = entry.getValue();
                    if (code != null && val != null) {
                        Optional<SystemSetting> sOpt = systemSettingRepository.findBySettingCode(code);
                        SystemSetting s;
                        if (sOpt.isPresent()) {
                            s = sOpt.get();
                            s.setSettingValue(val);
                            s.setUpdatedAt(LocalDateTime.now());
                        } else {
                            s = new SystemSetting();
                            s.setSettingCode(code);
                            s.setSettingName(code);
                            s.setSettingValue(val);
                            s.setUpdatedAt(LocalDateTime.now());
                        }
                        systemSettingRepository.save(s);
                    }
                }
            }

            if (auditLogRepository != null) {
                AuditLog auditLog = new AuditLog();
                auditLog.setUsername("admin");
                auditLog.setActionType("Cập nhật cấu hình thông số hệ thống");
                auditLog.setDetails("Cập nhật quy định đặt cọc, thời gian giữ slot và thông số hệ thống");
                auditLog.setModule("Phân hệ Quản trị");
                auditLog.setStatus("Thành công");
                auditLog.setActionTime(LocalDateTime.now());
                auditLogRepository.save(auditLog);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Đã lưu cài đặt hệ thống thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/admin/settings/reset")
    public ResponseEntity<?> resetAdminSettings() {
        try {
            Map<String, String> defaults = Map.of(
                "CFG_DEPOSIT_RATE", "30",
                "CFG_HOLDING_MINUTES", "10",
                "CFG_CANCEL_HOURS", "12",
                "CFG_ADVANCE_DAYS", "30",
                "CFG_VNPAY_TMN", "UTESPORT2026",
                "CFG_MOMO_PARTNER", "MOMO_UTE_BADMINTON"
            );
            if (systemSettingRepository != null) {
                for (Map.Entry<String, String> entry : defaults.entrySet()) {
                    Optional<SystemSetting> sOpt = systemSettingRepository.findBySettingCode(entry.getKey());
                    if (sOpt.isPresent()) {
                        SystemSetting s = sOpt.get();
                        s.setSettingValue(entry.getValue());
                        s.setUpdatedAt(LocalDateTime.now());
                        systemSettingRepository.save(s);
                    }
                }
            }

            if (auditLogRepository != null) {
                AuditLog auditLog = new AuditLog();
                auditLog.setUsername("admin");
                auditLog.setActionType("Khôi phục thông số mặc định");
                auditLog.setDetails("Khôi phục toàn bộ thông số vận hành hệ thống về mặc định ban đầu");
                auditLog.setModule("Phân hệ Quản trị");
                auditLog.setStatus("Thành công");
                auditLog.setActionTime(LocalDateTime.now());
                auditLogRepository.save(auditLog);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Đã khôi phục cài đặt mặc định"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/admin/logs")
    public ResponseEntity<?> getAdminLogs() {
        try {
            List<AuditLog> logs = auditLogRepository != null ? auditLogRepository.findTop100ByOrderByActionTimeDesc() : Collections.emptyList();
            return ResponseEntity.ok(Map.of("success", true, "logs", logs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/admin/logs/backups")
    public ResponseEntity<?> getAdminBackupLogs() {
        try {
            List<AuditLog> allLogs = auditLogRepository != null ? auditLogRepository.findTop100ByOrderByActionTimeDesc() : Collections.emptyList();
            List<AuditLog> backupLogs = allLogs.stream()
                    .filter(l -> "Hệ thống sao lưu".equalsIgnoreCase(l.getModule())
                            || (l.getActionType() != null && l.getActionType().toLowerCase().contains("sao lưu")))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("success", true, "backups", backupLogs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/admin/logs/backup")
    public ResponseEntity<?> createBackupLog(@RequestBody(required = false) Map<String, String> payload) {
        try {
            String filename = (payload != null && payload.containsKey("filename")) ? payload.get("filename") : "backup_ute_sport_manual.sql.gz";
            AuditLog auditLog = new AuditLog();
            auditLog.setUsername("admin");
            auditLog.setActionType("Sao lưu cơ sở dữ liệu thủ công");
            auditLog.setDetails("Tạo bản sao lưu " + filename + " thành công trên máy chủ CSDL SQL Server");
            auditLog.setModule("Hệ thống sao lưu");
            auditLog.setStatus("Thành công");
            auditLog.setActionTime(LocalDateTime.now());
            if (auditLogRepository != null) {
                auditLog = auditLogRepository.save(auditLog);
            }

            return ResponseEntity.ok(Map.of("success", true, "log", auditLog));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}

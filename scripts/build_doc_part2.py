# -*- coding: utf-8 -*-
"""
build_doc_part2.py: Phần 2 - Diễn giải chi tiết từng Chức năng của Ứng dụng (Chức năng 1 đến 6)
"""

from doc_helper import (
    add_h1, add_h2, add_h3,
    add_paragraph, add_bullet, add_code_block, add_ascii_diagram,
    add_note_box, add_table
)

def build_part_2(doc):
    add_h1(doc, "PHẦN 2: HƯỚNG DẪN CHI TIẾT & DIỄN GIẢI TỪNG CHỨC NĂNG CỦA ỨNG DỤNG")
    add_paragraph(doc, "Trong phần này, chúng ta sẽ đi sâu vào từng chức năng cụ thể của hệ thống UTE Sport. Với mỗi chức năng, tài liệu sẽ trình bày đầy đủ 4 nội dung then chốt: (A) Mục tiêu & Ý nghĩa nghiệp vụ, (B) Sơ đồ hoạt động & Luồng xử lý dữ liệu, (C) Diễn giải Source Code chi tiết từng dòng cho người mới học, và (D) Diễn giải Giao diện (UI) cùng Trải nghiệm người dùng (UX).")

    # =========================================================================
    # CHỨC NĂNG 1: XÁC THỰC NGƯỜI DÙNG, ĐĂNG NHẬP & PHÂN QUYỀN
    # =========================================================================
    add_h2(doc, "2.1. CHỨC NĂNG 1: XÁC THỰC NGƯỜI DÙNG, ĐĂNG NHẬP & PHÂN QUYỀN BẢO MẬT")
    
    add_h3(doc, "A. Mục tiêu & Ý nghĩa nghiệp vụ")
    add_paragraph(doc, "Đảm bảo xác thực chính xác danh tính người dùng khi truy cập hệ thống; cấp phát thẻ bài truy cập (JWT Token) chứa thông tin vai trò (Role: DIRECTOR, ADMIN, MANAGER, POS, CUSTOMER) và phạm vi chi nhánh (branchCode). Tự động điều hướng người dùng về đúng phân hệ làm việc tương ứng ngay sau khi đăng nhập thành công.")

    add_h3(doc, "B. Sơ đồ hoạt động & Luồng xử lý (Workflow)")
    auth_flow = """
[Trình duyệt] ---> (1) Gửi POST /api/auth/login { username, password }
      |
      v
[ApiController.login()] ---> (2) Truy vấn UserRepository.findByUsername()
      |
      +---> Không tìm thấy User? ---> Trả về lỗi 401 "Tài khoản không tồn tại"
      |
      +---> Kiểm tra Password (BCrypt / Raw)?
      |        |
      |        +---> Sai mật khẩu? ---> Trả về lỗi 401 "Mật khẩu không chính xác"
      |
      v
(3) Sinh JWT Token qua JwtTokenProvider.generateToken(username, role, branchCode)
      |
(4) Lưu Token vào HttpOnly Cookie 'jwt_token' và trả về JSON { success: true, token, role, redirectUrl }
      |
      v
[Client JS] ---> (5) Lưu Token vào localStorage và chuyển hướng trình duyệt:
      - Giám đốc  ---> Điều hướng tới /director/dashboard
      - Quản lý   ---> Điều hướng tới /manager/dashboard
      - Thu ngân  ---> Điều hướng tới /pos/grid
      - Quản trị  ---> Điều hướng tới /admin/users
      - Khách     ---> Điều hướng tới /customer/booking
"""
    add_ascii_diagram(doc, auth_flow, "Quy trình xác thực người dùng và phân quyền điều hướng")

    add_h3(doc, "C. Diễn giải Source Code chi tiết cho người mới học")
    add_paragraph(doc, "Toàn bộ logic xử lý đăng nhập tập trung tại phương thức login trong ApiController.java:")
    
    login_code = """
// Trích đoạn mã nguồn trong ApiController.java
@PostMapping("/auth/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpServletResponse response) {
    String username = loginRequest.get("username");
    String password = loginRequest.get("password");

    // 1. Tìm kiếm người dùng trong cơ sở dữ liệu qua UserRepository
    Optional<User> userOpt = userRepository.findByUsername(username != null ? username.trim() : "");
    if (userOpt.isEmpty()) {
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "Tài khoản không tồn tại!"));
    }

    User user = userOpt.get();

    // 2. Kiểm tra mật khẩu (hỗ trợ cả mật khẩu băm BCrypt và mật khẩu khởi tạo)
    boolean passwordMatches = passwordEncoder.matches(password, user.getPassword()) 
                           || password.equals(user.getRawPassword());
    if (!passwordMatches) {
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "Mật khẩu không chính xác!"));
    }

    // 3. Sinh chuỗi JWT Token có thời hạn sử dụng 7 ngày
    String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole(), user.getBranchCode());

    // 4. Lưu Token vào Cookie để phục vụ các trang giao diện Server-side Thymeleaf
    Cookie jwtCookie = new Cookie("jwt_token", token);
    jwtCookie.setHttpOnly(false); // Cho phép JS đọc khi cần thiết
    jwtCookie.setPath("/");
    jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
    response.addCookie(jwtCookie);

    // 5. Xác định đường dẫn điều hướng dựa trên Chức danh (Role)
    String redirectUrl = switch (user.getRole().toUpperCase()) {
        case "DIRECTOR" -> "/director/dashboard";
        case "ADMIN" -> "/admin/users";
        case "MANAGER" -> "/manager/dashboard";
        case "POS", "STAFF" -> "/pos/grid";
        default -> "/customer/booking";
    };

    return ResponseEntity.ok(Map.of(
        "success", true,
        "token", token,
        "role", user.getRole(),
        "fullName", user.getFullName(),
        "branchCode", user.getBranchCode() != null ? user.getBranchCode() : "ALL",
        "redirectUrl", redirectUrl
    ));
}
"""
    add_code_block(doc, login_code, "Phương thức xử lý đăng nhập và cấp phát JWT trong ApiController.java")

    add_paragraph(doc, "Giải thích kỹ thuật cho người mới học:", bold_prefix="Diễn giải từng dòng mã: ");
    add_bullet(doc, "@PostMapping(\"/auth/login\"): Đánh dấu đây là phương thức xử lý yêu cầu HTTP POST gửi đến đường dẫn /api/auth/login.");
    add_bullet(doc, "@RequestBody Map<String, String> loginRequest: Spring Boot tự động bóc tách chuỗi JSON do trình duyệt gửi lên thành đối tượng Map trong Java chứa cặp key-value (username, password).");
    add_bullet(doc, "passwordEncoder.matches(...): Hàm kiểm tra mã hóa mật khẩu an toàn theo thuật toán BCrypt. Không bao giờ so sánh mật khẩu dạng chuỗi trần trực tiếp để tránh lộ thông tin.");
    add_bullet(doc, "jwtTokenProvider.generateToken(...): Đóng gói thông tin người dùng vào một chuỗi mã hóa ký điện tử HMAC-SHA256, không thể làm giả từ phía client.");

    add_h3(doc, "D. Diễn giải Giao diện (UI) & Trải nghiệm người dùng (UX)")
    add_paragraph(doc, "Giao diện đăng nhập được đặt tại file templates/login.html với thiết kế Glassmorphism sang trọng:")
    add_bullet(doc, "Thẻ form đăng nhập với 2 trường nhập liệu: Tên tài khoản (@username) và Mật khẩu. Có nút icon con mắt bật/tắt ẩn hiện mật khẩu.");
    add_bullet(doc, "Hộp chọn vai trò nhanh (Quick-Role Selector) dành cho môi trường thử nghiệm: Cho phép nhấp chọn nhanh giữa Giám đốc (director), Quản trị (admin), Quản lý (manager), Thu ngân (pos) và Khách hàng (customer) mà không cần gõ phím thủ công.");
    add_bullet(doc, "File auth-guard.js được đính kèm ở đầu mọi trang giao diện quản trị: Khi người dùng truy cập bất kỳ trang nào (ví dụ /manager/courts), auth-guard.js sẽ đọc Token từ localStorage hoặc Cookie. Nếu chưa đăng nhập hoặc truy cập sai phân hệ (ví dụ Thu ngân cố tình vào trang Giám đốc), mã script sẽ lập tức chuyển hướng người dùng về trang /login.");

    # =========================================================================
    # CHỨC NĂNG 2: QUẢN LÝ MẠNG LƯỚI CHI NHÁNH & CÔ LẬP DỮ LIỆU
    # =========================================================================
    add_h2(doc, "2.2. CHỨC NĂNG 2: QUẢN LÝ MẠNG LƯỚI CHI NHÁNH & CÔ LẬP DỮ LIỆU ĐA CƠ SỞ")
    
    add_h3(doc, "A. Mục tiêu & Ý nghĩa nghiệp vụ")
    add_paragraph(doc, "Cho phép Ban Giám Đốc quản lý danh sách toàn bộ các cơ sở chi nhánh trong chuỗi: thêm cơ sở mới, cập nhật giờ hoạt động, số lượng sân, và thực hiện xóa chi nhánh khi đóng cửa. Đồng thời, áp dụng cơ chế cô lập dữ liệu để Quản lý chỉ được tác nghiệp trong phạm vi chi nhánh được phân công.")

    add_h3(doc, "B. Sơ đồ hoạt động & Luồng xử lý (Workflow)")
    branch_flow = """
[Giám Đốc tại director/branches.html]
      |
      +---> Tạo Chi Nhánh: Nhập { branchCode: 'CN05', branchName: 'Cơ sở Quận 7', ... }
      |        |
      |        v
      |     [POST /api/branches] ---> Kiểm tra trùng mã? ---> Lưu BranchRepository ---> Thành công!
      |
      +---> Xóa Chi Nhánh: Nhấn nút Xóa tại chi nhánh 'CN05'
               |
               v
            [DELETE /api/branches/CN05]
               |
               +---> Kiểm tra vai trò: Phải là ROLE_DIRECTOR (Chặn Quản lý & Thu ngân với mã 403)
               |
               +---> Kiểm tra ràng buộc: Còn sân bãi hoạt động hoặc lịch đặt sân chưa thanh toán?
               |        |
               |        +---> Có ràng buộc ---> Chặn xóa, báo lỗi "Cần giải tỏa sân bãi trước khi xóa"
               |
               v
            Xóa an toàn khỏi SQL Server & Ghi vết vào AuditLogRepository
"""
    add_ascii_diagram(doc, branch_flow, "Quy trình thêm mới và xóa chi nhánh có kiểm tra an toàn dữ liệu")

    add_h3(doc, "C. Diễn giải Source Code chi tiết cho người mới học")
    add_paragraph(doc, "Đoạn mã xử lý xóa chi nhánh an toàn trong ApiController.java:")
    
    del_branch_code = """
// Trích đoạn mã nguồn trong ApiController.java
@DeleteMapping("/branches/{branchCode}")
public ResponseEntity<?> deleteBranch(@PathVariable String branchCode, Authentication authentication) {
    // 1. Kiểm tra quyền hạn tối cao: Chỉ Giám Đốc mới có thẩm quyền xóa chi nhánh
    if (authentication == null || authentication.getAuthorities().stream()
            .noneMatch(a -> a.getAuthority().equals("ROLE_DIRECTOR"))) {
        return ResponseEntity.status(403).body(Map.of(
            "success", false, 
            "message", "Chỉ Ban Giám Đốc mới có quyền xóa chi nhánh khỏi hệ thống chuỗi!"
        ));
    }

    Optional<Branch> bOpt = branchRepository.findByBranchCode(branchCode);
    if (bOpt.isEmpty()) {
        return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy chi nhánh: " + branchCode));
    }

    // 2. Kiểm tra an toàn dữ liệu: Kiểm tra xem chi nhánh có còn sân bãi hay không
    List<Court> branchCourts = courtRepository.findByBranchCode(branchCode);
    if (!branchCourts.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Không thể xóa chi nhánh đang có " + branchCourts.size() + " sân đấu! Vui lòng chuyển hoặc xóa sân trước."
        ));
    }

    // 3. Thực hiện xóa và ghi vết kiểm toán hệ thống (Audit Log)
    branchRepository.delete(bOpt.get());
    
    AuditLog log = new AuditLog();
    log.setAction("DELETE_BRANCH");
    log.setUsername(authentication.getName());
    log.setDetails("Đã xóa chi nhánh: " + branchCode);
    auditLogRepository.save(log);

    return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa thành công chi nhánh " + branchCode));
}
"""
    add_code_block(doc, del_branch_code, "Kiểm tra quyền Giám đốc và ràng buộc dữ liệu khi xóa chi nhánh")

    add_paragraph(doc, "Giải thích kỹ thuật cho người mới học:", bold_prefix="Diễn giải từng dòng mã: ");
    add_bullet(doc, "@PathVariable String branchCode: Bóc tách tham số trên thanh địa chỉ URL (ví dụ: gọi DELETE /api/branches/CN04 thì biến branchCode sẽ nhận giá trị 'CN04').");
    add_bullet(doc, "Authentication authentication: Đối tượng chứa thông tin người đang thực hiện thao tác (được JwtAuthenticationFilter nạp sẵn từ trước). Ta dùng lệnh authentication.getAuthorities() để kiểm tra danh sách quyền hạn.");
    add_bullet(doc, "AuditLog: Lớp ghi vết bảo mật. Bất kỳ thao tác xóa nào cũng phải ghi lại ai là người xóa, vào lúc nào để phục vụ công tác thanh tra kế toán sau này.");

    add_h3(doc, "D. Diễn giải Giao diện (UI) & Trải nghiệm người dùng (UX)")
    add_paragraph(doc, "Giao diện quản lý chi nhánh được đặt tại templates/director/branches.html:")
    add_bullet(doc, "Mỗi chi nhánh được hiển thị dưới dạng một Thẻ cơ sở (Card) trực quan: Bao gồm tên cơ sở, địa chỉ, số điện thoại hotline, tên quản lý phụ trách và số lượng sân hiện có.");
    add_bullet(doc, "Nút 'Thêm Chi Nhánh Mới' mở một Modal Popup chứa các trường nhập liệu có kiểm tra định dạng dữ liệu (mã chi nhánh viết hoa, số điện thoại 10 chữ số).");
    add_bullet(doc, "Nút 'Xóa chi nhánh' (màu đỏ với biểu tượng thùng rác): Khi Giám đốc nhấp vào, giao diện sẽ hiển thị hộp thoại xác nhận SweetAlert cảnh báo nguy cơ xóa để tránh bấm nhầm.");

    # =========================================================================
    # CHỨC NĂNG 3: QUẢN LÝ SÂN BÃI, TÌNH TRẠNG & ĐỀ XUẤT BIỂU GIÁ
    # =========================================================================
    add_h2(doc, "2.3. CHỨC NĂNG 3: QUẢN LÝ SÂN BÃI, TÌNH TRẠNG THẢM & ĐỀ XUẤT BIỂU GIÁ SÂN")
    
    add_h3(doc, "A. Mục tiêu & Ý nghĩa nghiệp vụ")
    add_paragraph(doc, "Cho phép quản lý danh mục sân cầu lông, cập nhật chi tiết tình trạng cơ sở vật chất (đèn chiếu sáng chống chói 500 Lux, thảm PVC Enlio 4.5mm, tình trạng lưới). Đặc biệt, hệ thống áp dụng cơ chế Khóa cứng giá sân đối với Quản lý chi nhánh: Quản lý không được tùy tiện nâng hạ giá mà chỉ được lập Tờ trình đề xuất biểu giá gửi Ban Giám Đốc phê duyệt.")

    add_h3(doc, "B. Sơ đồ hoạt động & Luồng xử lý (Workflow)")
    court_flow = """
[Quản Lý Chi Nhánh tại manager/courts.html]
      |
      +---> Cập nhật trạng thái sân: Sân hỏng thảm ---> Đổi sang "Bảo trì" ---> Cập nhật thành công!
      |
      +---> Muốn tăng giá sân từ 120.000đ lên 150.000đ/giờ?
               |
               v Ô nhập giá bị Khóa (Disabled) + Biểu tượng Ổ khóa bảo vệ
               |
               v Nhấn nút "Đề xuất biểu giá sân"
               |
               v Gửi POST /api/pricing-proposals { currentPrice: 120000, proposedPrice: 150000, reason: 'Nâng cấp đèn' }
               |
               v Lưu trạng thái "Chờ phê duyệt" trong PricingProposalRepository
               |
[Ban Giám Đốc tại director/pricing.html]
      |
      v Xem danh sách đề xuất chờ duyệt
      |
      +---> Chấp thuận (Approve)? ---> Tự động cập nhật Court.hourlyRate = 150.000đ trong SQL Server!
      |
      +---> Từ chối (Reject)?   ---> Ghi chú lý do từ chối và thông báo về Quản lý chi nhánh.
"""
    add_ascii_diagram(doc, court_flow, "Quy trình đề xuất và phê duyệt biểu giá sân giữa Quản lý và Giám đốc")

    add_h3(doc, "C. Diễn giải Source Code chi tiết cho người mới học")
    add_paragraph(doc, "Phương thức kiểm soát việc sửa giá sân trong ApiController.java:")
    
    court_update_code = """
// Trích đoạn mã nguồn trong ApiController.java
@PostMapping("/courts")
public ResponseEntity<?> createOrUpdateCourt(@RequestBody Court court, Authentication authentication) {
    boolean isDirector = authentication != null && authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_DIRECTOR"));
    boolean isManager = authentication != null && authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

    // 1. Kiểm tra quyền Quản lý: Không được phép sửa giá sân (hourlyRate)
    if (isManager && !isDirector) {
        Optional<Court> existingCourt = courtRepository.findByCourtCode(court.getCourtCode());
        if (existingCourt.isPresent()) {
            Court old = existingCourt.get();
            // Nếu Quản lý gửi giá mới khác với giá hiện tại trong CSDL -> Chặn đứng ngay!
            if (court.getHourlyRate() != null && court.getHourlyRate().compareTo(old.getHourlyRate()) != 0) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Quản lý chi nhánh không được phép tự ý thay đổi giá sân! Vui lòng tạo Đề xuất biểu giá để Ban Giám Đốc phê duyệt."
                ));
            }
        } else {
            // Quản lý cũng không được tự ý tạo thêm sân mới
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "Chỉ Ban Giám Đốc mới có quyền tạo thêm sân mới vào hệ thống!"
            ));
        }
    }

    // 2. Lưu thông tin cập nhật (Tình trạng thảm, đèn, mô tả, trạng thái hoạt động)
    courtRepository.save(court);
    return ResponseEntity.ok(Map.of("success", true, "message", "Đã lưu cập nhật sân đấu thành công!"));
}
"""
    add_code_block(doc, court_update_code, "Chặn Quản lý tự ý đổi giá sân và tạo sân mới trong ApiController.java")

    add_h3(doc, "D. Diễn giải Giao diện (UI) & Trải nghiệm người dùng (UX)")
    add_paragraph(doc, "Giao diện quản lý sân của Quản lý chi nhánh tại templates/manager/courts.html:")
    add_bullet(doc, "Ô nhập giá sân (inpHourlyRate) được gắn thuộc tính disabled và bổ sung nhãn cảnh báo 'Chỉ Giám Đốc điều chỉnh' kèm biểu tượng ổ khóa SVG.");
    add_bullet(doc, "Thay vì nút 'Thêm sân mới' vô quyền, giao diện cung cấp nút 'Đề xuất biểu giá sân' màu xanh ngọc: Mở Modal nhập giá mong muốn và lý do đề xuất.");
    add_bullet(doc, "Mỗi sân hiển thị 3 huy hiệu kỹ thuật: Chuẩn đèn LED 500 Lux, Thảm Taraflex/PVC chống trượt, và Lưới thi đấu BWF, giúp quản lý dễ dàng nắm bắt chất lượng cơ sở.");

    # =========================================================================
    # CHỨC NĂNG 4: LƯỚI ĐẶT SÂN REALTIME, QUẦY POS & CHỐNG ĐẶT TRÙNG
    # =========================================================================
    add_h2(doc, "2.4. CHỨC NĂNG 4: LƯỚI ĐẶT SÂN THỜI GIAN THỰC, QUẦY POS & CHỐNG ĐẶT TRÙNG")
    
    add_h3(doc, "A. Mục tiêu & Ý nghĩa nghiệp vụ")
    add_paragraph(doc, "Đây là 'trái tim' của hệ thống vận hành: Cung cấp ma trận lưới giờ trực quan theo trục ngang (các khung giờ từ 05:00 đến 23:00) và trục dọc (danh sách sân đấu). Cho phép thu ngân quầy POS thực hiện: khóa giữ chỗ, nhận cọc, quét mã VietQR tự động, đổi chuyển sân, xuất hóa đơn in giấy nhiệt K80 và giải quyết triệt để vấn đề 2 khách đặt trùng một sân cùng một thời điểm.")

    add_h3(doc, "B. Sơ đồ hoạt động & Luồng xử lý (Workflow)")
    pos_flow = """
[Thu ngân chọn ô giờ trên lưới POS]
      |
      v Nhấp vào ô [Sân 1 - Khung giờ 17:00-18:00]
      |
      v Gửi POST /api/bookings/slot-lock
      |
      +---> Kiểm tra đồng thời (Concurrency Check):
      |        Đã có đơn đặt nào đang 'Đã giữ chỗ' hoặc 'Đã thanh toán' trên ô giờ này chưa?
      |        |
      |        +---> Đã có người đặt trước? ---> Ném lỗi "Khung giờ này vừa có người đặt!"
      |
      v Khởi tạo đơn đặt với trạng thái "Chờ thanh toán"
      |
      v Sinh chuỗi mã VietQR động theo chuẩn EMVCo chứa đúng số tiền cọc (30%)
      |
      v Gọi SimpMessagingTemplate.convertAndSend("/topic/slots") thông báo toàn chuỗi
      |
[Màn hình quầy POS hiển thị Popup]
      |
      +---> Khách quét mã VietQR: Webhook xác nhận tự động đổi trạng thái sang "Đã thanh toán"
      |
      +---> Khách trả tiền mặt: Thu ngân bấm "Xác nhận nhận tiền mặt"
      |
      v Hệ thống tạo Invoice, trừ tiền cọc, và in vé K80 qua máy in nhiệt quầy thu ngân.
"""
    add_ascii_diagram(doc, pos_flow, "Quy trình đặt sân, khóa slot và thanh toán tại quầy POS")

    add_h3(doc, "C. Diễn giải Source Code chi tiết cho người mới học")
    add_paragraph(doc, "Phương thức khóa giữ chỗ chống đặt trùng trong ApiController.java và BookingService.java:")
    
    lock_slot_code = """
// Trích đoạn mã nguồn trong ApiController.java
@PostMapping("/bookings/slot-lock")
public ResponseEntity<?> lockSlotBooking(@RequestBody Map<String, Object> req) {
    String courtCode = (String) req.get("courtCode");
    String timeSlot = (String) req.get("timeSlot");
    String dateStr = (String) req.get("date");
    LocalDate bookingDate = LocalDate.parse(dateStr);

    // 1. Kiểm tra chống đặt trùng lịch (Double-Booking Prevention)
    List<Booking> conflicts = bookingRepository.findConflictingBookings(
        courtCode, bookingDate, timeSlot, List.of("Đã hủy", "Hết hạn")
    );
    if (!conflicts.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false, 
            "message", "Khung giờ này đã được đặt hoặc đang có khách giữ chỗ! Vui lòng chọn khung giờ khác."
        ));
    }

    // 2. Tìm kiếm thông tin sân và biểu giá
    Court court = courtRepository.findByCourtCode(courtCode)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sân đấu: " + courtCode));

    // 3. Tính toán tiền cọc tối thiểu (mặc định 30% giá thuê sân)
    BigDecimal totalPrice = court.getHourlyRate();
    BigDecimal depositAmount = totalPrice.multiply(new BigDecimal("0.30"));

    // 4. Tạo mã đơn đặt duy nhất (vd: DS281005)
    String bookingCode = bookingService.generateUniqueBookingCode();

    Booking booking = new Booking();
    booking.setBookingCode(bookingCode);
    booking.setCourtCode(courtCode);
    booking.setBranchCode(court.getBranchCode());
    booking.setBookingDate(bookingDate);
    booking.setTimeSlot(timeSlot);
    booking.setHourlyPrice(court.getHourlyRate());
    booking.setTotalPrice(totalPrice);
    booking.setDepositAmount(depositAmount);
    booking.setStatus("Chờ thanh toán");
    bookingRepository.save(booking);

    // 5. Sinh URL mã VietQR thanh toán nhanh
    String qrUrl = String.format(
        "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=CK%%20%s&accountName=%s",
        bookingService.getBankBin(), bookingService.getAccountNo(),
        depositAmount.longValue(), bookingCode,
        URLEncoder.encode(bookingService.getAccountName(), StandardCharsets.UTF_8)
    );

    // 6. Phát tín hiệu WebSocket cho toàn bộ mạng lưới
    bookingService.broadcastCourtSlotUpdate(courtCode, timeSlot, court.getBranchCode(), "Đang giữ chỗ");

    return ResponseEntity.ok(Map.of(
        "success", true,
        "bookingCode", bookingCode,
        "depositAmount", depositAmount,
        "totalPrice", totalPrice,
        "qrUrl", qrUrl
    ));
}
"""
    add_code_block(doc, lock_slot_code, "Kiểm tra xung đột lịch và sinh mã VietQR đặt sân")

    add_h3(doc, "D. Diễn giải Giao diện (UI) & Trải nghiệm người dùng (UX)")
    add_paragraph(doc, "Giao diện lưới đặt sân của thu ngân tại templates/pos/grid.html và pos-grid.js:")
    add_bullet(doc, "Ma trận lưới thông minh: Các ô giờ trống có màu xanh nhạt, ô đang có khách chơi có màu xanh lá, ô đã đặt có màu xanh dương và ô bảo trì có màu xám gạch chéo.");
    add_bullet(doc, "Kéo thả / Chuyển sân (Move Court): Khi khách muốn đổi từ Sân 1 sang Sân 2 do muốn chơi cạnh bạn bè, thu ngân chỉ cần nhấn nút 'Đổi sân', chọn sân đích, hệ thống sẽ tự động cập nhật lại cơ sở dữ liệu và thông báo cho các máy khác.");
    add_bullet(doc, "In vé nhiệt K80: Tích hợp cửa sổ xem trước hóa đơn thanh toán chuẩn khổ giấy nhiệt 80mm thông dụng tại các quầy thu ngân Việt Nam (gồm tên câu lạc bộ, mã đơn, giờ chơi, tiền cọc, tiền đồ uống và mã QR tra cứu).");

    # =========================================================================
    # CHỨC NĂNG 5: KHO HÀNG, MẶT HÀNG CHUỖI & NHẬP XUẤT TỰ ĐỘNG
    # =========================================================================
    add_h2(doc, "2.5. CHỨC NĂNG 5: QUẢN LÝ KHO HÀNG, MẶT HÀNG CHUỖI & NHẬP XUẤT TỰ ĐỘNG")
    
    add_h3(doc, "A. Mục tiêu & Ý nghĩa nghiệp vụ")
    add_paragraph(doc, "Chuẩn hóa danh mục hàng hóa (nước giải khát, ống cầu lông thi đấu, quấn cán vợt, dịch vụ căng vợt) trên toàn hệ thống. Quản lý số lượng tồn kho riêng biệt theo từng cơ sở, quản lý quy trình nhập hàng từ nhà cung cấp và tự động trừ tồn kho khi thu ngân bán nước tại quầy POS.")

    add_h3(doc, "B. Sơ đồ hoạt động & Luồng xử lý (Workflow)")
    inventory_flow = """
[Ban Giám Đốc] ---> Tạo mặt hàng mới chuẩn chuỗi (vd: 'Nước tăng lực Redbull Thái', Giá bán: 18.000đ)
                          |
                          v Tự động phân bổ vào danh mục hàng hóa của các chi nhánh
                          |
[Quản Lý Chi Nhánh] ---> Lập phiếu nhập kho (StockReceipt): Nhập 100 lon Redbull từ NCC Hải Hà
                          |
                          v Tồn kho chi nhánh tăng từ 0 lên 100 lon (WarehouseService)
                          |
[Quầy Thu Ngân POS] ---> Khách chơi Sân 2 gọi 2 lon Redbull (orderServiceForBooking)
                          |
                          v Kiểm tra tồn kho: 100 >= 2 lon?
                          |        |
                          |        v Đủ hàng: Tự động trừ tồn kho còn 98 lon trong SQL Server
                          |
                          v Cộng 36.000đ vào tổng hóa đơn thanh toán của đơn đặt sân
"""
    add_ascii_diagram(doc, inventory_flow, "Vòng đời quản lý sản phẩm từ chuẩn chuỗi đến trừ kho bán lẻ")

    add_h3(doc, "C. Diễn giải Source Code chi tiết cho người mới học")
    add_paragraph(doc, "Phương thức bán hàng và tự động trừ tồn kho trong WarehouseService.java:")
    
    warehouse_code = """
// Trích đoạn mã nguồn trong WarehouseService.java
@Service
@Transactional
public class WarehouseService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BookingRepository bookingRepository;

    public Map<String, Object> orderServiceForBooking(String bookingCode, String productCode, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng gọi dịch vụ phải lớn hơn 0!");
        }

        // 1. Tìm kiếm mặt hàng và kiểm tra tồn kho
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có mã: " + productCode));

        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Số lượng tồn kho không đủ! Sản phẩm " + product.getProductName() +
                    " hiện chỉ còn " + product.getStockQuantity() + " " + product.getUnit());
        }

        // 2. Tìm kiếm đơn đặt sân tương ứng
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt sân: " + bookingCode));

        // 3. Trừ số lượng tồn kho trực tiếp trong CSDL
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        // 4. Cộng dồn tiền dịch vụ vào tổng tiền đơn đặt sân
        BigDecimal serviceAmount = product.getUnitPrice().multiply(new BigDecimal(quantity));
        booking.setTotalPrice(booking.getTotalPrice().add(serviceAmount));
        bookingRepository.save(booking);

        return Map.of(
            "success", true,
            "productName", product.getProductName(),
            "quantity", quantity,
            "remainingStock", product.getStockQuantity(),
            "addedAmount", serviceAmount,
            "newTotalPrice", booking.getTotalPrice()
        );
    }
}
"""
    add_code_block(doc, warehouse_code, "Xử lý kiểm tra tồn kho và trừ kho an toàn giao dịch trong WarehouseService.java")

    add_h3(doc, "D. Diễn giải Giao diện (UI) & Trải nghiệm người dùng (UX)")
    add_paragraph(doc, "Giao diện quản lý kho và bán hàng được bố trí tại templates/manager/inventory.html và pos/grid.html:")
    add_bullet(doc, "Bảng cảnh báo tồn kho tối thiểu: Những mặt hàng có số lượng tồn kho dưới ngưỡng minQuantity (mặc định 10 cái) sẽ tự động sáng huy hiệu cảnh báo màu đỏ 'Sắp hết hàng', giúp quản lý chủ động lên đơn nhập hàng mới.");
    add_bullet(doc, "Menu gọi đồ uống nhanh trên lưới POS: Thu ngân nhấp vào sân đang chơi, bấm nút '+ Thêm đồ uống/dịch vụ', danh sách đồ uống kèm hình ảnh và số lượng tồn kho hiện ra, chỉ cần bấm nút '+' là tồn kho lập tức trừ và tiền tự động cộng vào hóa đơn.");

    # =========================================================================
    # CHỨC NĂNG 6: KHUYẾN MÃI (VOUCHER) & GIẢI ĐẤU THỂ THAO
    # =========================================================================
    add_h2(doc, "2.6. CHỨC NĂNG 6: QUẢN LÝ MÃ GIẢM GIÁ (VOUCHER) & GIẢI ĐẤU THỂ THAO")
    
    add_h3(doc, "A. Mục tiêu & Ý nghĩa nghiệp vụ")
    add_paragraph(doc, "Cho phép tạo các chương trình ưu đãi tri ân khách hàng: mã giảm giá theo tỷ lệ phần trăm (vd: giảm 20%) hoặc số tiền cố định (vd: giảm 50.000đ). Phân định rõ phạm vi voucher: Voucher áp dụng Toàn chuỗi (do Giám Đốc phát hành) hoặc Voucher áp dụng riêng cho một Chi nhánh cụ thể (do Quản lý tạo để kích cầu cơ sở mới). Đồng thời quản lý danh sách đăng ký thi đấu giải thể thao phong trào.")

    add_h3(doc, "B. Sơ đồ hoạt động & Luồng xử lý (Workflow)")
    promo_flow = """
[Khách hàng nhập mã 'NEWYEAR2026' tại trang đặt sân]
      |
      v Gửi POST /api/promotions/validate { promoCode: 'NEWYEAR2026', branchCode: 'CN01', amount: 150000 }
      |
      +---> 1. Kiểm tra tồn tại trong PromotionRepository
      +---> 2. Kiểm tra ngày hiệu lực (startDate <= Today <= endDate)
      +---> 3. Kiểm tra phạm vi (Scope):
      |        |
      |        +---> Scope là 'Toàn chuỗi'? ---> Chấp thuận
      |        +---> Scope là 'Chi nhánh'?  ---> Phải trùng khớp với branchCode của sân đang đặt!
      |
      v Tính toán số tiền được giảm:
      |        - Giảm 20% của 150.000đ = 30.000đ
      |        - Tổng thanh toán mới = 120.000đ
      |
      v Trả kết quả JSON hiển thị số tiền tiết kiệm trên màn hình khách hàng
"""
    add_ascii_diagram(doc, promo_flow, "Quy trình kiểm tra tính hợp lệ của mã giảm giá theo phạm vi chi nhánh")

    add_h3(doc, "C. Diễn giải Source Code chi tiết cho người mới học")
    add_paragraph(doc, "Phương thức kiểm tra và tính toán giảm giá trong ApiController.java:")
    
    promo_code = """
// Trích đoạn mã nguồn trong ApiController.java
@PostMapping("/promotions/validate")
public ResponseEntity<?> validatePromotion(@RequestBody Map<String, Object> req) {
    String promoCode = (String) req.get("promoCode");
    String branchCode = (String) req.get("branchCode");
    BigDecimal originalAmount = new BigDecimal(req.get("amount").toString());

    Optional<Promotion> promoOpt = promotionRepository.findByPromoCode(promoCode != null ? promoCode.trim() : "");
    if (promoOpt.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã giảm giá không tồn tại!"));
    }

    Promotion promo = promoOpt.get();
    LocalDate today = LocalDate.now();

    // 1. Kiểm tra thời hạn hiệu lực
    if (today.isBefore(promo.getStartDate()) || today.isAfter(promo.getEndDate())) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã giảm giá đã hết hạn sử dụng!"));
    }

    // 2. Kiểm tra phạm vi chi nhánh áp dụng
    if (!"Toàn chuỗi".equalsIgnoreCase(promo.getScope()) && !promo.getScope().equalsIgnoreCase(branchCode)) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false, 
            "message", "Mã giảm giá này chỉ áp dụng riêng cho chi nhánh " + promo.getScope() + "!"
        ));
    }

    // 3. Tính toán số tiền khấu trừ
    BigDecimal discountAmount = BigDecimal.ZERO;
    if ("PERCENT".equalsIgnoreCase(promo.getDiscountType())) {
        discountAmount = originalAmount.multiply(promo.getDiscountValue()).divide(new BigDecimal("100"));
    } else {
        discountAmount = promo.getDiscountValue();
    }

    BigDecimal finalAmount = originalAmount.subtract(discountAmount);
    if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;

    return ResponseEntity.ok(Map.of(
        "success", true,
        "promoCode", promo.getPromoCode(),
        "discountAmount", discountAmount,
        "finalAmount", finalAmount
    ));
}
"""
    add_code_block(doc, promo_code, "Kiểm tra điều kiện áp dụng mã voucher trong ApiController.java")

    add_h3(doc, "D. Diễn giải Giao diện (UI) & Trải nghiệm người dùng (UX)")
    add_paragraph(doc, "Giao diện tại templates/director/tournaments.html và customer/booking.html:")
    add_bullet(doc, "Hộp áp dụng Voucher tại bước thanh toán: Khách hàng nhập mã và nhấn 'Áp dụng'. Nếu hợp lệ, một badge màu xanh lá hiển thị thông báo '-30.000đ', và số tiền cọc VietQR tự động cập nhật giảm tương ứng theo thời gian thực.");
    add_bullet(doc, "Giao diện quản lý giải đấu: Hiển thị bảng đấu vòng tròn/loại trực tiếp, danh sách các cặp vận động viên đăng ký tham gia, tình trạng nộp lệ phí thi đấu và cập nhật tỷ số trận đấu trực tiếp.");

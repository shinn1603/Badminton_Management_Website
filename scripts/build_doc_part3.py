# -*- coding: utf-8 -*-
"""
build_doc_part3.py: Phần 2 (tiếp: Chức năng 7, 8, 9) và Phần 3 - Hướng dẫn Cài đặt, Vận hành & Lời khuyên Lập trình
"""

from doc_helper import (
    add_h1, add_h2, add_h3,
    add_paragraph, add_bullet, add_code_block, add_ascii_diagram,
    add_note_box, add_table
)

def build_part_3(doc):
    # =========================================================================
    # CHỨC NĂNG 7: BÁO CÁO DOANH THU & THỐNG KÊ TÀI CHÍNH ĐA CHIỀU
    # =========================================================================
    add_h2(doc, "2.7. CHỨC NĂNG 7: BÁO CÁO DOANH THU & THỐNG KÊ TÀI CHÍNH ĐA CHIỀU")
    
    add_h3(doc, "A. Mục tiêu & Ý nghĩa nghiệp vụ")
    add_paragraph(doc, "Cung cấp bức tranh toàn cảnh về tình hình tài chính của toàn chuỗi cũng như từng cơ sở riêng lẻ. Phân tích doanh thu theo nhiều chiều kích: Doanh thu tiền thuê sân, Doanh thu bán nước/phụ kiện, Tỷ trọng thanh toán chuyển khoản VietQR so với Tiền mặt quầy thu ngân, Tỷ lệ lấp đầy sân theo từng khung giờ trong ngày.")

    add_h3(doc, "B. Sơ đồ hoạt động & Luồng xử lý (Workflow)")
    report_flow = """
[Người dùng truy cập trang Báo cáo]
      |
      v Gửi GET /api/reports/revenue?branchCode=CN01&period=MONTHLY
      |
      +---> Kiểm tra danh tính & Quyền hạn qua resolveEffectiveBranchCode():
      |        |
      |        +---> Giám đốc (DIRECTOR)?  ---> Được xem toàn chuỗi (ALL) hoặc lọc theo từng cơ sở
      |        +---> Quản lý (MANAGER)?   ---> Chỉ được xem doanh thu của chi nhánh mình phụ trách
      |
      v Thực thi các câu lệnh tổng hợp dữ liệu (Aggregation Queries):
      |        - SUM(totalPrice) từ bảng bookings với status = 'Đã thanh toán'
      |        - SUM(serviceAmount) từ các đơn đồ uống phát sinh
      |        - Phân nhóm theo hình thức thanh toán (VietQR vs Tiền mặt)
      |
      v Trả về cấu trúc JSON chứa các chỉ số KPI tài chính
      |
[Giao diện Chart.js hiển thị Biểu đồ]
      - Biểu đồ đường (Line Chart): Xu hướng tăng trưởng doanh thu 30 ngày gần nhất
      - Biểu đồ tròn (Donut Chart): Tỷ trọng đóng góp giữa tiền sân (82%) và đồ uống (18%)
"""
    add_ascii_diagram(doc, report_flow, "Quy trình tổng hợp và phân quyền báo cáo tài chính")

    add_h3(doc, "C. Diễn giải Source Code chi tiết cho người mới học")
    add_paragraph(doc, "Phương thức tổng hợp báo cáo tài chính trong ApiController.java:")
    
    report_code = """
// Trích đoạn mã nguồn trong ApiController.java
@GetMapping("/reports/revenue")
public ResponseEntity<?> getRevenueReport(
        @RequestParam(required = false, defaultValue = "ALL") String branchCode,
        Authentication authentication) {

    // 1. Ép chặt phạm vi chi nhánh của Quản lý để bảo mật dữ liệu doanh thu
    User currentUser = getCurrentUser(authentication);
    String effectiveBranch = resolveEffectiveBranchCode(currentUser, branchCode);

    // 2. Lấy danh sách các đơn đặt sân đã thanh toán thành công
    List<Booking> completedBookings = bookingRepository.findCompletedBookingsByBranch(effectiveBranch);

    BigDecimal totalCourtRevenue = BigDecimal.ZERO;
    BigDecimal totalDepositReceived = BigDecimal.ZERO;
    long cashCount = 0;
    long qrCount = 0;

    for (Booking b : completedBookings) {
        totalCourtRevenue = totalCourtRevenue.add(b.getTotalPrice());
        totalDepositReceived = totalDepositReceived.add(b.getDepositAmount());
        if ("VietQR".equalsIgnoreCase(b.getPaymentMethod())) {
            qrCount++;
        } else {
            cashCount++;
        }
    }

    return ResponseEntity.ok(Map.of(
        "success", true,
        "branchScope", effectiveBranch,
        "totalRevenue", totalCourtRevenue,
        "depositReceived", totalDepositReceived,
        "totalOrders", completedBookings.size(),
        "vietQrTransactions", qrCount,
        "cashTransactions", cashCount
    ));
}
"""
    add_code_block(doc, report_code, "Tổng hợp chỉ số doanh thu và bảo vệ dữ liệu tài chính trong ApiController.java")

    add_h3(doc, "D. Diễn giải Giao diện (UI) & Trải nghiệm người dùng (UX)")
    add_paragraph(doc, "Giao diện báo cáo tại templates/manager/reports.html và templates/director/dashboard.html:")
    add_bullet(doc, "4 Thẻ chỉ số KPI đầu bảng (Stat Cards): Doanh thu thực nhận, Tỷ lệ lấp đầy sân trung bình (Occupancy Rate ~78%), Tổng số lượt khách trong ngày, và Doanh số bán nước giải khát.");
    add_bullet(doc, "Bộ lọc thời gian linh hoạt: Cho phép xem nhanh theo Hôm nay, Tuần này, Tháng này hoặc tùy chọn khoảng ngày từ ngày A đến ngày B.");
    add_bullet(doc, "Xuất dữ liệu: Nút bấm 'Xuất báo cáo Excel' và 'In báo cáo doanh thu ngày' phục vụ công tác nộp sổ sách kế toán cuối ca.");

    # =========================================================================
    # CHỨC NĂNG 8: QUẢN TRỊ TÀI KHOẢN & MA TRẬN PHÂN QUYỀN DỮ LIỆU
    # =========================================================================
    add_h2(doc, "2.8. CHỨC NĂNG 8: QUẢN TRỊ TÀI KHOẢN & MA TRẬN PHÂN QUYỀN DỮ LIỆU 2 TAB")
    
    add_h3(doc, "A. Mục tiêu & Ý nghĩa nghiệp vụ")
    add_paragraph(doc, "Cung cấp công cụ tối thượng cho Quản trị viên hệ thống (ROLE_ADMIN) nhằm: Tạo mới, chỉnh sửa thông tin, đặt lại mật khẩu và khóa tài khoản nhân sự. Đặc biệt, trang sở hữu Ma trận phân quyền 2 Tab tách biệt rõ ràng giữa Phạm vi dữ liệu chi nhánh (Data Scope) và Bảng tra cứu quyền hạn tác nghiệp theo chức danh (RBAC Matrix), hỗ trợ gán linh hoạt một quản lý phụ trách nhiều cơ sở cùng lúc.")

    add_h3(doc, "B. Sơ đồ hoạt động & Luồng xử lý (Workflow)")
    perm_flow = """
[Quản Trị Viên tại admin/permissions.html]
      |
      +---> TAB 1: PHẠM VI DỮ LIỆU CHI NHÁNH (DATA SCOPE MATRIX)
      |        |
      |        v Quản trị viên tick chọn: Nhân sự 'manager' phụ trách cả [CN01] và [CN02]
      |        |
      |        v JavaScript tự động tổng hợp: branchCode = "CN01,CN02"
      |        |
      |        v Tự động kích hoạt gọi POST /api/admin/permissions [{ username: 'manager', branchCode: 'CN01,CN02' }]
      |        |
      |        v Máy chủ lưu vào bảng users trong SQL Server, hiển thị Toast xanh "Đã cập nhật vào CSDL!"
      |        |
      |        v Badge tóm tắt trên dòng lập tức đổi sang: [ 2 cơ sở: CN01, CN02 ]
      |
      +---> TAB 2: BẢNG TRA CỨU QUYỀN HẠN TÁC NGHIỆP (RBAC MATRIX)
               |
               v Bảng ma trận so sánh chi tiết giữa 4 chức danh:
                 - Ban Giám Đốc (DIRECTOR)
                 - Quản Trị Hệ Thống (ADMIN)
                 - Quản Lý Chi Nhánh (MANAGER)
                 - Thu Ngân Quầy POS (POS)
               v Hiển thị trực quan: Toàn quyền (Tick xanh), Theo cơ sở (Khóa vàng), Không được phép (X xám)
"""
    add_ascii_diagram(doc, perm_flow, "Quy trình gán quyền chi nhánh thời gian thực và tra cứu ma trận RBAC")

    add_h3(doc, "C. Diễn giải Source Code chi tiết cho người mới học")
    add_paragraph(doc, "Cơ chế cập nhật phân quyền đa chi nhánh trong ApiController.java và xử lý Checkbox tại permissions.html:")
    
    perm_backend_code = """
// Trích đoạn mã nguồn trong ApiController.java
@PostMapping("/admin/permissions")
public ResponseEntity<?> updateAdminPermissions(@RequestBody List<Map<String, String>> updates) {
    int count = 0;
    for (Map<String, String> item : updates) {
        String username = item.get("username");
        String branchCode = item.get("branchCode"); // Nhận "ALL", "CN01", hoặc "CN01,CN02"

        if (username != null && branchCode != null) {
            Optional<User> uOpt = userRepository.findByUsername(username.trim());
            if (uOpt.isPresent()) {
                User u = uOpt.get();
                u.setBranchCode(branchCode.trim()); // Lưu chuỗi đa chi nhánh vào cột branch_code
                userRepository.save(u);
                count++;
            }
        }
    }
    return ResponseEntity.ok(Map.of("success", true, "updatedCount", count));
}
"""
    add_code_block(doc, perm_backend_code, "Lưu danh sách phân quyền đa chi nhánh vào SQL Server trong ApiController.java")

    perm_frontend_code = """
// Trích đoạn mã nguồn JavaScript trong admin/permissions.html
function handleSingleBranchToggle(username, changedBranch, isChecked) {
    const branchCheckboxes = document.querySelectorAll(`.cb-${username}:checked`);
    const checkedCodes = Array.from(branchCheckboxes).map(cb => cb.getAttribute('data-branch'));

    let newBranchCode = '';
    let isAll = false;

    // Nếu người dùng tick đủ tất cả các chi nhánh -> Tự động quy về 'ALL'
    if (checkedCodes.length === systemBranches.length && systemBranches.length > 0) {
        newBranchCode = 'ALL';
        isAll = true;
    } else {
        newBranchCode = checkedCodes.join(','); // Nối thành chuỗi "CN01,CN02"
    }

    // Tự động lưu ngay lập tức vào CSDL mà không cần bấm nút Lưu
    updateUserScopeLive(username, newBranchCode);
}
"""
    add_code_block(doc, perm_frontend_code, "Thuật toán xử lý Checkbox đa chi nhánh thông minh trên giao diện")

    add_h3(doc, "D. Diễn giải Giao diện (UI) & Trải nghiệm người dùng (UX)")
    add_paragraph(doc, "Giao diện tại templates/admin/permissions.html:")
    add_bullet(doc, "Thanh chuyển đổi Tab cao cấp (Tab Navigation Bar): Chuyển đổi mượt mà giữa Tab 1 (Phạm vi dữ liệu chi nhánh) và Tab 2 (Ma trận quyền hạn tác nghiệp RBAC).");
    add_bullet(doc, "Hệ thống Checkbox thay thế hoàn toàn Radio: Người dùng có thể tick chọn nhiều chi nhánh cùng lúc; có cột 'Toàn chuỗi (ALL)' tự động tích/bỏ tích toàn bộ.");
    add_bullet(doc, "Huy hiệu tóm tắt phạm vi thông minh (Scope Badge): Tự động hiển thị thẻ 'Toàn chuỗi (4 CS)' (xanh dương), '2 cơ sở: CN01, CN02' (xanh lá), hoặc cảnh báo 'Chưa gán chi nhánh' (đỏ nhạt) giúp người quản trị nhận biết ngay lập tức.");

    # =========================================================================
    # CHỨC NĂNG 9: CỔNG ĐẶT SÂN KHÁCH HÀNG & TRỢ LÝ ẢO AI
    # =========================================================================
    add_h2(doc, "2.9. CHỨC NĂNG 9: CỔNG ĐẶT SÂN KHÁCH HÀNG TRỰC TUYẾN & TRỢ LÝ ẢO AI")
    
    add_h3(doc, "A. Mục tiêu & Ý nghĩa nghiệp vụ")
    add_paragraph(doc, "Mang đến cho khách hàng trải nghiệm đặt sân trực tuyến chuẩn 5 sao: tra cứu lịch sân trống trực quan theo từng cơ sở, xem hình ảnh thực tế chất lượng thảm và hệ thống chiếu sáng, chọn khung giờ yêu thích, thanh toán cọc tức thì qua mã VietQR tự động, quản lý lịch sử đặt sân cá nhân và được hỗ trợ 24/7 bởi Trợ lý ảo AI thông minh.")

    add_h3(doc, "B. Sơ đồ hoạt động & Luồng xử lý (Workflow)")
    customer_flow = """
[Khách hàng truy cập /customer/booking]
      |
      +---> Chọn Chi nhánh (vd: Cơ sở 1 - Thủ Đức) & Ngày chơi thể thao
      |        |
      |        v Tải danh sách sân và tình trạng các khung giờ từ 05:00 đến 23:00
      |        |
      |        v Chọn Khung giờ [18:00 - 19:00] trên Sân 2
      |        |
      |        v Nhập thông tin: Họ tên, Số điện thoại và Mã giảm giá (nếu có)
      |        |
      |        v Bấm "Xác nhận đặt sân" ---> Hệ thống tạo đơn và hiển thị Popup VietQR
      |        |
      |        v Khách mở App Ngân hàng (MB, VCB, Techcombank...) quét mã QR
      |        |
      |        v Sau 2 giây: Webhook xác nhận thanh toán thành công ---> Thông báo vé điện tử!
      |
      +---> Trò chuyện với Trợ lý AI (Cửa sổ Chat góc phải màn hình)
               |
               v Khách hỏi: "Sân chi nhánh 1 mở cửa lúc mấy giờ và giá thuê thế nào?"
               |
               v IntentClassificationService phân tích câu hỏi dựa trên intent_dataset.json
               |
               v Trợ lý AI phản hồi ngay lập tức: "Chi nhánh 1 mở cửa từ 06:00 đến 23:00 hàng ngày, giá thuê dao động từ 120.000đ đến 180.000đ/giờ tùy khung giờ ạ!"
"""
    add_ascii_diagram(doc, customer_flow, "Quy trình khách hàng đặt sân trực tuyến và trò chuyện cùng Trợ lý AI")

    add_h3(doc, "C. Diễn giải Source Code chi tiết cho người mới học")
    add_paragraph(doc, "Cơ chế phân tích ý định câu hỏi của Chatbot trong ChatbotService.java và IntentClassificationService.java:")
    
    chatbot_code = """
// Trích đoạn mã nguồn trong ChatbotService.java
@Service
public class ChatbotService {
    @Autowired
    private IntentClassificationService intentService;
    @Autowired(required = false)
    private GeminiAiService geminiAiService;

    public Map<String, Object> handleCustomerMessage(String userMessage, String sessionId) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return Map.of("reply", "Chào bạn! Tôi là Trợ lý AI của UTE Sport. Tôi có thể giúp gì cho bạn hôm nay?");
        }

        // 1. Phân loại ý định người dùng (Intent Recognition) qua từ khóa & tập mẫu dữ liệu
        String intent = intentService.classifyIntent(userMessage);

        String replyText = switch (intent) {
            case "HOI_GIO_MO_CUA" -> "Hệ thống sân cầu lông UTE Sport mở cửa hoạt động từ 06:00 sáng đến 23:00 đêm tất cả các ngày trong tuần (kể cả Lễ, Tết) bạn nhé!";
            case "HOI_GIA_SAN" -> "Biểu giá thuê sân tiêu chuẩn hiện tại là 120.000đ/giờ (khung giờ thường) và 180.000đ/giờ (khung giờ vàng từ 17:00 - 21:00). Bạn có thể bấm vào mục Đặt Sân để xem chi tiết từng sân nhé!";
            case "HOI_CHINH_SACH_HUY" -> "Bạn được phép hủy lịch và hoàn cọc 100% nếu thông báo trước giờ chơi tối thiểu 12 tiếng theo Quy định của UTE Sport ạ.";
            default -> {
                // Nếu câu hỏi phức tạp nằm ngoài mẫu có sẵn, chuyển tiếp sang mô hình Gemini AI thông minh
                if (geminiAiService != null) {
                    yield geminiAiService.generateResponse(userMessage);
                } else {
                    yield "Cảm ơn bạn đã liên hệ! Bạn có thể gọi hotline 0903.123.456 để được nhân viên hỗ trợ trực tiếp nhanh nhất nhé!";
                }
            }
        };

        return Map.of("reply", replyText, "intent", intent);
    }
}
"""
    add_code_block(doc, chatbot_code, "Xử lý hội thoại tự động và phân loại ý định trong ChatbotService.java")

    add_h3(doc, "D. Diễn giải Giao diện (UI) & Trải nghiệm người dùng (UX)")
    add_paragraph(doc, "Giao diện khách hàng tại templates/customer/booking.html:")
    add_bullet(doc, "Bộ chọn cơ sở và ngày chơi trực quan: Có thanh trượt ngày nhanh trong 7 ngày tới, hiển thị rõ thứ trong tuần và biểu tượng thời tiết.");
    add_bullet(doc, "Thư viện ảnh sân thực tế (Court Gallery): Khách có thể phóng to xem ảnh góc rộng của sân, ảnh chi tiết bề mặt thảm sàn và hệ thống đèn để yên tâm tuyệt đối về chất lượng.");
    add_bullet(doc, "Widget Trợ lý ảo AI đính nổi (Floating Chatbox): Nằm ở góc dưới cùng bên phải màn hình, có âm thanh thông báo nhẹ nhàng và hỗ trợ khách hỏi đáp nhanh mọi thắc mắc.");

    # =========================================================================
    # PHẦN 3: HƯỚNG DẪN CÀI ĐẶT, VẬN HÀNH & KINH NGHIỆM PHÁT TRIỂN
    # =========================================================================
    add_h1(doc, "PHẦN 3: HƯỚNG DẪN CÀI ĐẶT, VẬN HÀNH & KINH NGHIỆM PHÁT TRIỂN (DEVELOPER GUIDE)")

    add_h2(doc, "3.1. Cấu hình môi trường phát triển (Development Environment)")
    add_paragraph(doc, "Để biên dịch và chạy dự án thành công trên máy tính cá nhân, bạn cần cài đặt các công cụ sau:")
    add_bullet(doc, "Bản phân phối Eclipse Temurin hoặc Oracle JDK phiên bản 21 LTS trở lên. Thiết lập biến môi trường JAVA_HOME trỏ đúng đường dẫn cài đặt.", bold_prefix="1. Java Development Kit (JDK): ");
    add_bullet(doc, "Dự án đã tích hợp sẵn Maven Wrapper (mvnw.cmd trên Windows hoặc ./mvnw trên Linux/macOS), bạn không bắt buộc phải cài Maven rời.", bold_prefix="2. Apache Maven: ");
    add_bullet(doc, "Cài đặt bản Developer hoặc Express (miễn phí). Cấu hình tài khoản sa với mật khẩu chỉ định trong application.properties. Lưu ý: Khi chạy kiểm thử tự động, dự án sẽ tự kích hoạt CSDL H2 In-Memory tương thích MSSQL hoàn toàn tự động mà không cần cài đặt SQL Server phức tạp.", bold_prefix="3. Hệ quản trị CSDL Microsoft SQL Server: ");

    add_h2(doc, "3.2. Quy trình khởi chạy ứng dụng & Khởi tạo dữ liệu mẫu")
    add_paragraph(doc, "Thực hiện lần lượt các bước sau trong thư mục gốc của dự án:")
    
    run_steps_code = """
# Bước 1: Mở Terminal (PowerShell hoặc Command Prompt) tại thư mục dự án
cd C:\\Users\\ASUS\\Documents\\web\\Spring_Badminton_Website_Project

# Bước 2: Dọn dẹp và biên dịch mã nguồn
.\\mvnw.cmd clean compile

# Bước 3: Khởi chạy ứng dụng Spring Boot
.\\mvnw.cmd spring-boot:run

# Khi terminal hiển thị dòng chữ:
# "Started BadmintonManagementProjectApplication in X.XX seconds"
# Mở trình duyệt và truy cập: http://localhost:8080
"""
    add_code_block(doc, run_steps_code, "Các lệnh biên dịch và khởi chạy máy chủ ứng dụng")

    add_paragraph(doc, "Lớp DatabaseSeeder.java tự động kiểm tra cơ sở dữ liệu khi máy chủ khởi động: Nếu hệ thống chưa có dữ liệu, lớp này sẽ tự động nạp sẵn 4 chi nhánh (CN01 đến CN04), danh sách sân đấu kèm hình ảnh, khung giờ chuẩn, tài khoản giám đốc (director / 123456), quản trị (admin / 123456), quản lý (manager / 123456) và thu ngân (pos / 123456) để bạn có thể sử dụng ngay lập tức mà không cần gõ dữ liệu tay.")

    add_h2(doc, "3.3. Hướng dẫn chạy bộ kiểm thử tự động (Unit & Integration Test)")
    add_paragraph(doc, "Hệ thống sở hữu bộ kiểm thử tự động chuẩn mực gồm 19 kịch bản kiểm thử tích hợp (Integration Tests) trong file ApiControllerTest.java, bao phủ toàn bộ các tình huống nghiệp vụ quan trọng: khóa sân, chống đặt trùng, chặn quản lý tự sửa giá sân, cô lập chi nhánh, và cập nhật phân quyền đa cơ sở:")
    
    test_run_code = """
# Lệnh thực thi toàn bộ 19 bài kiểm thử tự động trong ApiControllerTest:
.\\mvnw.cmd test -Dtest=ApiControllerTest

# Kết quả thực tế đạt chuẩn 100%:
# [INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
"""
    add_code_block(doc, test_run_code, "Lệnh chạy kiểm thử tự động đảm bảo chất lượng phần mềm")

    add_h2(doc, "3.4. Các quy tắc 'sống còn' cho người mới bắt đầu học lập trình Spring Boot")
    add_paragraph(doc, "Dưới đây là 5 bài học xương máu được đúc kết từ quá trình xây dựng dự án UTE Sport giúp bạn nhanh chóng trở thành một kỹ sư phần mềm chuyên nghiệp:")

    add_note_box(doc, 
        "1. QUẢN LÝ GIAO DỊCH VỚI @Transactional: Luôn đặt chú thích @Transactional trên các phương thức nghiệp vụ thực hiện từ 2 thao tác ghi CSDL trở lên (ví dụ: vừa tạo đơn đặt sân, vừa trừ tồn kho đồ uống). Nếu có sự cố xảy ra ở bước 2, Spring sẽ tự động Rollback (hủy bỏ) bước 1, ngăn chặn việc 'sân đã đặt nhưng kho chưa trừ' hoặc 'mất tiền nhưng không có sân'.\n\n"
        "2. NGUYÊN TẮC 'ZERO EMOJI' TRONG GIAO DIỆN CHUYÊN NGHIỆP: Tuyệt đối không chèn emoji (😀, ⚽, 🏸) vào mã nguồn HTML, nhãn nút bấm hay chuỗi phản hồi. Hãy sử dụng hệ thống biểu tượng SVG đồng bộ vector (Feather Icons / Heroicons). Điều này giúp giao diện hiển thị sắc nét trên mọi màn hình Retina và thể hiện đẳng cấp phần mềm thương mại doanh nghiệp.\n\n"
        "3. LUÔN KIỂM SOÁT PHẠM VI DỮ LIỆU TẠI BACKEND: Không bao giờ tin tưởng hoàn toàn dữ liệu do trình duyệt gửi lên! Dù giao diện đã ẩn nút bấm hoặc disable ô nhập giá, người dùng am hiểu công nghệ vẫn có thể mở Postman để gửi API giả mạo. Phải luôn viết mã kiểm tra quyền hạn (Role check và Branch scope check) ngay tại tầng Controller/Service như cách ApiController đã triển khai.\n\n"
        "4. XỬ LÝ CONCURRENCY TRONG THỜI GIAN THỰC: Khi xây dựng ứng dụng đặt chỗ có nhiều người cùng truy cập, luôn nhớ rằng một khung giờ chỉ thuộc về một người duy nhất. Cần kết hợp khóa giao tiếp WebSocket STOMP tức thì cùng điều kiện kiểm tra tồn tại (findConflictingBookings) trong một giao dịch ACID trước khi quyết định cấp sân cho khách hàng.\n\n"
        "5. TÁCH BẠCH RÕ RÀNG GIỮA DTO VÀ THỰC THỂ CSDL (ENTITY): Không nên trả trực tiếp thực thể Entity ra ngoài nếu chứa các trường nhạy cảm như password. Sử dụng @JsonProperty(access = WRITE_ONLY) hoặc các lớp DTO để chỉ đóng gói các thông tin cần thiết trả về cho Client.",
        title="5 NGUYÊN TẮC THIẾT KẾ PHẦN MỀM CHUẨN CÔNG NGHIỆP"
    )

    add_paragraph(doc, "Chúc bạn có những trải nghiệm học tập và phát triển tuyệt vời cùng dự án UTE Sport! Toàn bộ nền móng kiến trúc, quy tắc bảo mật và mã nguồn mẫu mực của dự án này sẽ là hành trang vững chắc trên con đường chinh phục thế giới lập trình phần mềm chuyên nghiệp của bạn.", bold_prefix="Lời kết: ")

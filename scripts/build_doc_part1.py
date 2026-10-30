# -*- coding: utf-8 -*-
"""
build_doc_part1.py: Phần 1 - Tổng quan Hệ thống & Kiến trúc Ứng dụng (Application Architecture)
"""

from doc_helper import (
    add_title, add_subtitle, add_h1, add_h2, add_h3,
    add_paragraph, add_bullet, add_code_block, add_ascii_diagram,
    add_note_box, add_table
)

def build_part_1(doc):
    # =========================================================================
    # TRANG TIÊU ĐỀ & LỜI NÓI ĐẦU
    # =========================================================================
    add_title(doc, "TÀI LIỆU HƯỚNG DẪN XÂY DỰNG & DIỄN GIẢI TOÀN DIỆN\nỨNG DỤNG QUẢN LÝ HỆ THỐNG SÂN CẦU LÔNG UTE SPORT")
    add_subtitle(doc, "Cẩm nang kiến trúc phần mềm, phân tích nghiệp vụ, diễn giải chi tiết Source Code & Giao diện\nDành cho Kỹ sư phần mềm, Lập trình viên mới học & Đội ngũ vận hành hệ thống")
    
    add_paragraph(doc, "Chào mừng bạn đến với tài liệu kỹ thuật toàn diện của dự án UTE Sport - Hệ thống Quản trị & Vận hành chuỗi sân cầu lông thông minh đa cơ sở. Tài liệu này được biên soạn với mục tiêu tối thượng: giúp người đọc - dù là người mới bắt đầu học lập trình Spring Boot hay lập trình viên giàu kinh nghiệm - có thể nắm trọn toàn bộ bức tranh kiến trúc, luồng nghiệp vụ, cấu trúc dữ liệu và cách từng dòng mã nguồn được xây dựng và phối hợp ăn khớp với nhau trong thực tế.", bold_prefix="Lời mở đầu: ")
    
    add_paragraph(doc, "Mọi sơ đồ quy trình, cấu trúc bảng, tên lớp (Class), phương thức (Method) và đoạn mã nguồn trong tài liệu này đều được trích xuất trực tiếp, chính xác 100% từ mã nguồn thực tế của dự án. Sau khi đọc xong tài liệu, bạn sẽ tự tin làm chủ toàn bộ dự án từ backend đến frontend, hiểu rõ tại sao hệ thống lại được thiết kế như vậy và có thể tự tay mở rộng các tính năng mới một cách chuẩn mực.", bold_prefix="Cam kết tài liệu: ")

    # Bảng mục lục tổng quan
    add_h2(doc, "MỤC LỤC TỔNG QUAN TÀI LIỆU")
    toc_headers = ["Phần", "Nội dung trọng tâm", "Mục tiêu đạt được"]
    toc_rows = [
        ["Phần 1", "Tổng quan Hệ thống & Kiến trúc Ứng dụng (Architecture)", "Hiểu kiến trúc phân tầng, bảo mật JWT + RBAC, WebSocket STOMP, mô hình CSDL."],
        ["Phần 2", "Diễn giải chi tiết từng Chức năng (Chức năng 1 đến 9)", "Nắm vững luồng hoạt động, cấu trúc mã nguồn và giao diện của toàn bộ 9 chức năng."],
        ["Phần 3", "Hướng dẫn Cài đặt, Vận hành & Kinh nghiệm phát triển", "Biết cách setup môi trường, chạy kiểm thử tự động (19 Unit Tests) và quy tắc viết code."]
    ]
    add_table(doc, toc_headers, toc_rows, col_widths=[1.0, 3.2, 2.3])

    # =========================================================================
    # PHẦN 1: TỔNG QUAN HỆ THỐNG & KIẾN TRÚC ỨNG DỤNG
    # =========================================================================
    add_h1(doc, "PHẦN 1: TỔNG QUAN HỆ THỐNG & KIẾN TRÚC ỨNG DỤNG (APPLICATION ARCHITECTURE)")

    # 1.1 Bối cảnh & Mục tiêu
    add_h2(doc, "1.1. Bối cảnh dự án, Mục tiêu hệ thống & Phạm vi nghiệp vụ")
    add_paragraph(doc, "Trong thời đại chuyển đổi số thể thao, mô hình vận hành sân cầu lông truyền thống bộc lộ rất nhiều hạn chế: ghi sổ sách tay dễ nhầm lẫn, khách hàng gọi điện đặt sân thường xuyên bị trùng khung giờ (double-booking), việc thu tiền cọc thiếu minh bạch, giá sân biến động theo khung giờ cao điểm khó kiểm soát, và quản lý chuỗi không nắm được doanh thu thời gian thực giữa các chi nhánh.")
    add_paragraph(doc, "Dự án UTE Sport ra đời nhằm giải quyết triệt để các bài toán hóc búa trên thông qua một nền tảng Web Application hiện đại, vận hành theo mô hình chuỗi phân tán kết hợp máy chủ tập trung (Centralized Cloud Platform). Hệ thống phục vụ 5 nhóm đối tượng người dùng với quyền hạn rõ ràng:", bold_prefix="Mục tiêu cốt lõi: ")
    
    add_bullet(doc, "Toàn quyền điều hành toàn bộ chuỗi cơ sở, xem báo cáo doanh thu tổng hợp, duyệt đề xuất điều chỉnh biểu giá sân của quản lý, quản lý danh mục sản phẩm chuẩn chuỗi và quyết định mở mới hoặc đóng chi nhánh.", bold_prefix="1. Ban Giám Đốc (ROLE_DIRECTOR): ");
    add_bullet(doc, "Chịu trách nhiệm về hạ tầng, quản lý tài khoản người dùng, phân quyền phạm vi truy cập dữ liệu chi nhánh (Data Scope Matrix), sao lưu phục hồi dữ liệu và kiểm tra nhật ký kiểm toán (Audit Logs).", bold_prefix="2. Quản Trị Hệ Thống (ROLE_ADMIN): ");
    add_bullet(doc, "Phụ trách vận hành trực tiếp tại một hoặc nhiều chi nhánh được phân công (ví dụ CN01 và CN02). Quản lý danh sách sân, tình trạng thảm/lưới, gửi đề xuất giá, lập phiếu nhập kho vật tư và xem báo cáo tài chính nội bộ cơ sở.", bold_prefix="3. Quản Lý Chi Nhánh (ROLE_MANAGER): ");
    add_bullet(doc, "Tác nghiệp tại quầy lễ tân chi nhánh. Trực tiếp theo dõi lưới sân thời gian thực, khóa giữ chỗ, nhận thanh toán cọc/đủ (tiền mặt hoặc quét VietQR tự động), xuất bán nước/vật tư và in hóa đơn nhiệt K80.", bold_prefix="4. Thu Ngân Quầy POS (ROLE_POS): ");
    add_bullet(doc, "Trải nghiệm cổng đặt sân trực tuyến, tra cứu lịch trống theo từng cơ sở, đặt sân giữ chỗ tức thì, quét mã VietQR ngân hàng tự động đối soát, xem lịch sử đặt chỗ và trò chuyện cùng Trợ lý ảo AI.", bold_prefix="5. Khách Hàng (ROLE_CUSTOMER): ");

    # 1.2 Sơ đồ kiến trúc tổng thể
    add_h2(doc, "1.2. Sơ đồ kiến trúc tổng thể phần mềm (System Architecture)")
    add_paragraph(doc, "Hệ thống UTE Sport được xây dựng theo mô hình Client-Server hiện đại, kết hợp đồng thời cả hai mô thức giao tiếp: RESTful API (HTTP/JSON không trạng thái) phục vụ các tác vụ nghiệp vụ, và STOMP over WebSocket (kết nối 2 chiều duy trì liên tục) phục vụ đồng bộ dữ liệu thời gian thực giữa quầy POS và khách hàng.")

    arch_diagram = """
+----------------------------------------------------------------------------------------------------+
|                                CLIENT TIER (GIAO DIỆN NGƯỜI DÙNG)                                  |
|   +---------------------+   +---------------------+   +---------------------+   +----------------+ |
|   | Portal Khách Hàng   |   | Quầy Thu Ngân POS   |   | Dashboard Quản Lý   |   | Bàn Giám Đốc   | |
|   | (Thymeleaf + Fetch) |   | (Lưới Realtime STOMP)|  | (Cô lập chi nhánh)  |   | & Quản Trị     | |
|   +----------+----------+   +----------+----------+   +----------+----------+   +--------+-------+ |
+--------------|-------------------------|-------------------------|-----------------------|---------+
               | HTTPS (JSON REST)       | WebSocket (STOMP)       | JWT Cookie/Header     |
+--------------v-------------------------v-------------------------v-----------------------v---------+
|                              APPLICATION SERVER TIER (SPRING BOOT 3)                               |
|   +--------------------------------------------------------------------------------------------+   |
|   | 1. BẢO MẬT & ĐIỀU PHỐI (SECURITY & GATEWAY LAYER)                                          |   |
|   |    - JwtAuthenticationFilter (Trích xuất token Bearer từ Header hoặc HttpOnly Cookie)      |   |
|   |    - SecurityFilterChain (Phân quyền RBAC theo URL & Method Security)                     |   |
|   |    - CORS & Exception Handling Filter (Xử lý lỗi đồng nhất trả về JSON chuẩn)             |   |
|   +--------------------------------------------------------------------------------------------+   |
|   | 2. TẦNG ĐIỀU KHIỂN & TRUYỀN THÔNG (CONTROLLER & WEBSOCKET BROKER)                          |   |
|   |    - ApiController (RESTful API cung cấp endpoints cho toàn bộ nghiệp vụ)                  |   |
|   |    - DashboardController / HomeController (Render giao diện Server-side Thymeleaf)         |   |
|   |    - WebSocketMessageBroker (SimpleBroker định tuyến thông điệp qua /topic và /queue)      |   |
|   +--------------------------------------------------------------------------------------------+   |
|   | 3. TẦNG DỊCH VỤ NGHIỆP VỤ (SERVICE LAYER - @Service & @Transactional)                      |   |
|   |    - BookingService: Xử lý khóa slot, chống đặt trùng, tính cọc, tạo VietQR, in bill       |   |
|   |    - CourtService & PricingService: Quản lý sân, tính biểu giá linh hoạt theo giờ vàng    |   |
|   |    - WarehouseService: Quản lý xuất nhập tồn, bán nước quầy POS, trừ kho tự động          |   |
|   |    - UserService & ChatbotService: Quản trị tài khoản, phân tích ý định NLP / Gemini AI   |   |
|   +--------------------------------------------------------------------------------------------+   |
|   | 4. TẦNG TRUY XUẤT DỮ LIỆU (PERSISTENCE LAYER - SPRING DATA JPA)                            |   |
|   |    - 24 JPA Repositories kế thừa JpaRepository<T, ID> với truy vấn tự động                 |   |
|   |    - Cơ chế Auditing, AuditLog ghi lại mọi thao tác quan trọng vào cơ sở dữ liệu           |   |
|   +--------------------------------------------------------------------------------------------+   |
+----------------------------------------------------------------------------------------------------+
                                               | HikariCP Connection Pool (JDBC)
+----------------------------------------------v-----------------------------------------------------+
|                               DATABASE TIER (HỆ THỐNG LƯU TRỮ DỮ LIỆU)                             |
|   - Microsoft SQL Server 2019/2022 (Môi trường Production thực tế với khóa ACID nghiêm ngặt)       |
|   - H2 Database Engine (Chế độ In-Memory tương thích MSSQL Server phục vụ chạy Unit/Integration Test)|
+----------------------------------------------------------------------------------------------------+
"""
    add_ascii_diagram(doc, arch_diagram, "Kiến trúc tổng thể 4 tầng của hệ thống UTE Sport")

    # 1.3 Ngăn xếp công nghệ
    add_h2(doc, "1.3. Ngăn xếp công nghệ (Technology Stack) & Thư viện nòng cốt")
    add_paragraph(doc, "Dự án sử dụng các công nghệ tiêu chuẩn công nghiệp nhằm đảm bảo tính ổn định, tốc độ phản hồi tính bằng mili-giây và khả năng mở rộng quy mô dễ dàng:")

    tech_headers = ["Thành phần", "Công nghệ / Thư viện", "Phiên bản", "Vai trò trong hệ sinh thái dự án"]
    tech_rows = [
        ["Ngôn ngữ cốt lõi", "Java SE (OpenJDK)", "21 / 26 LTS", "Ngôn ngữ backend hướng đối tượng mạnh mẽ, hiệu năng cao và an toàn kiểu dữ liệu."],
        ["Khung ứng dụng", "Spring Boot", "3.4.x / 4.x", "Khung phát triển ứng dụng Java doanh nghiệp tiêu chuẩn số 1 thế giới."],
        ["Bảo mật hệ thống", "Spring Security", "6.x", "Quản lý xác thực người dùng, phân quyền RBAC và kiểm soát truy cập tầng lọc (Filter)."],
        ["Cơ chế cấp token", "JJWT (Java JWT)", "0.11.5", "Sinh mã và kiểm tra chữ ký mã hóa HMAC-SHA256 cho JSON Web Token."],
        ["Giao tiếp dữ liệu", "Spring Data JPA & Hibernate", "6.x", "ORM ánh xạ thực thể Java sang bảng CSDL quan hệ, quản lý giao dịch (@Transactional)."],
        ["Giao tiếp Realtime", "Spring WebSocket & STOMP", "RFC 7118", "Truyền nhận thông điệp thời gian thực giữa trình duyệt quầy POS và máy chủ."],
        ["Kết nối CSDL", "HikariCP Connection Pool", "Default", "Bộ quản lý hồ bơi kết nối JDBC siêu nhanh và tiết kiệm tài nguyên bộ nhớ."],
        ["Hệ quản trị CSDL", "Microsoft SQL Server / H2", "2019+ / 2.4", "Lưu trữ dữ liệu có cấu trúc với ràng buộc toàn vẹn khóa ngoại nghiêm ngặt."],
        ["Công cụ Build", "Apache Maven", "3.9+", "Quản lý thư viện phụ thuộc (dependencies) và quy trình biên dịch đóng gói JAR."],
        ["Giao diện người dùng", "Thymeleaf SSR + Vanilla JS", "HTML5/CSS3", "Hiển thị giao diện máy chủ kết hợp JavaScript bất đồng bộ thuần, không phụ thuộc nặng framework cồng kềnh."],
        ["Cổng thanh toán QR", "VietQR NAPAS 247 API", "v2", "Tự động sinh mã VietQR động chứa số tài khoản, số tiền và nội dung chuyển khoản chuẩn ngân hàng."]
    ]
    add_table(doc, tech_headers, tech_rows, col_widths=[1.4, 1.8, 0.9, 2.4])

    # 1.4 Phân tầng mã nguồn
    add_h2(doc, "1.4. Phân tầng kiến trúc mã nguồn & Luồng tương tác giữa các tầng")
    add_paragraph(doc, "Mã nguồn backend của dự án được tổ chức chặt chẽ theo mẫu kiến trúc phân tầng (Layered Architecture). Mỗi tầng có một trách nhiệm duy nhất (Single Responsibility Principle), đảm bảo mã nguồn dễ bảo trì, dễ viết unit test và tránh việc nghiệp vụ bị rò rỉ sang tầng giao tiếp:")

    add_bullet(doc, "Tập trung tại gói vn.yain.entity. Ánh xạ trực tiếp các bảng trong cơ sở dữ liệu thành các Class Java nhờ chú thích @Entity, @Table, @Id, @Column. Đóng vai trò là mô hình dữ liệu lõi của ứng dụng.", bold_prefix="1. Tầng Thực thể (Entity Layer): ");
    add_bullet(doc, "Tập trung tại gói vn.yain.repository. Kế thừa interface JpaRepository<T, ID>. Nhờ Spring Data JPA, lập trình viên không cần viết các câu lệnh SQL thủ công (SELECT, INSERT, UPDATE) mà vẫn có thể truy vấn dữ liệu thông qua tên phương thức thông minh (vd: findByCourtCode, findAllByOrderByCreatedAtDesc).", bold_prefix="2. Tầng Truy xuất dữ liệu (Repository Layer): ");
    add_bullet(doc, "Tập trung tại gói vn.yain.service. Đây là 'bộ não' của ứng dụng, nơi chứa đựng toàn bộ quy tắc nghiệp vụ, tính toán tiền cọc, kiểm tra chống trùng lịch, quản lý xuất nhập kho. Được đánh dấu bằng chú thích @Service và @Transactional để đảm bảo tính toàn vẹn dữ liệu.", bold_prefix="3. Tầng Dịch vụ nghiệp vụ (Service Layer): ");
    add_bullet(doc, "Tập trung tại gói vn.yain.controller. Đóng vai trò là 'người đón tiếp' các yêu cầu từ bên ngoài. ApiController chịu trách nhiệm xử lý các HTTP Request (GET, POST, PUT, DELETE), kiểm tra tham số đầu vào và trả về JSON chuẩn. DashboardController và HomeController điều hướng render giao diện HTML Thymeleaf.", bold_prefix="4. Tầng Điều phối (Controller Layer): ");
    add_bullet(doc, "Tập trung tại gói vn.yain.security và vn.yain.config. Bao gồm bộ lọc bảo mật JwtAuthenticationFilter, cấu hình phân quyền SecurityConfig, cấu hình giao tiếp WebSocketConfig và khởi tạo dữ liệu DatabaseSeeder.", bold_prefix="5. Tầng Bảo mật & Cấu hình (Security & Config Layer): ");

    # Luồng tương tác
    flow_diagram = """
Client (Browser) 
      | (1) Gửi Request HTTP POST /api/bookings/slot-lock
      v
[JwtAuthenticationFilter] ---> Kiểm tra Token JWT hợp lệ, nạp quyền vào SecurityContext
      | (2) Ủy quyền sang Controller
      v
[ApiController] -----------> Nhận JSON Request, bóc tách tham số (courtCode, timeSlot, date)
      | (3) Gọi phương thức nghiệp vụ
      v
[BookingService] ----------> Thực hiện quy tắc: Sân có đang trống? Đã có ai giữ chỗ chưa?
      | (4) Truy vấn / Ghi dữ liệu
      v
[BookingRepository] -------> Thực thi câu lệnh xuống SQL Server qua Hibernate
      | (5) Trả về thực thể Booking mới tạo
      v
[SimpMessagingTemplate] ---> Broadcast thông báo qua /topic/bookings để toàn bộ các máy khác cập nhật
      | (6) Trả kết quả JSON kèm mã VietQR cho Client
      v
Client (Browser) hiển thị Popup thanh toán và khóa slot màu đỏ trên giao diện
"""
    add_ascii_diagram(doc, flow_diagram, "Luồng luân chuyển dữ liệu tuần tự qua 5 tầng của hệ thống")

    # 1.5 Mô hình thực thể ERD
    add_h2(doc, "1.5. Mô hình thực thể & Cơ sở dữ liệu quan hệ (Entity-Relationship Model)")
    add_paragraph(doc, "Cơ sở dữ liệu của UTE Sport được thiết kế theo chuẩn dạng chuẩn 3 (3NF), đảm bảo không dư thừa dữ liệu, tốc độ truy vấn tối ưu và có các ràng buộc toàn vẹn khóa ngoại (Foreign Key) chặt chẽ giữa các bảng. Dưới đây là bảng đặc tả 15 thực thể nòng cốt:")

    erd_headers = ["Tên Entity (Bảng)", "Khóa chính (PK)", "Khóa ngoại (FK)", "Thuộc tính tiêu biểu", "Ý nghĩa trong nghiệp vụ"]
    erd_rows = [
        ["User (users)", "id (Identity)", "branch_code (logic)", "username, password, role, staffCode, branchCode", "Lưu trữ tài khoản người dùng, chức danh và phạm vi chi nhánh."],
        ["Branch (branches)", "id (Identity)", "Không", "branchCode, branchName, address, phone, totalCourts", "Lưu thông tin các cơ sở trong toàn chuỗi sân cầu lông."],
        ["Court (courts)", "id (Identity)", "branch_code -> branches", "courtCode, courtName, branchCode, hourlyRate, floorCondition", "Lưu trữ từng sân đấu, biểu giá thuê cơ sở và tình trạng thảm."],
        ["TimeSlot (time_slots)", "slot_code (String)", "Không", "slotCode, startTime, endTime, slotType", "Khung giờ chuẩn từ 05:00 đến 23:00 (mỗi slot 1 tiếng)."],
        ["Booking (bookings)", "id (Identity)", "court_code, branch_code", "bookingCode, customerName, timeSlot, depositAmount, status", "Đơn đặt sân của khách hàng, trạng thái thanh toán và tiền cọc."],
        ["Product (products)", "id (Identity)", "branch_code", "productCode, productName, category, unitPrice, costPrice", "Danh mục hàng hóa chuẩn chuỗi và bảng giá niêm yết bán lẻ."],
        ["StockProductDetail", "id (Identity)", "product_id, branch_code", "stockQuantity, minQuantity, branchCode", "Quản lý số lượng tồn kho thực tế của từng mặt hàng tại mỗi chi nhánh."],
        ["StockReceipt (stock_receipts)", "id (Identity)", "branch_code, staff_code", "receiptCode, supplierName, totalAmount, receiptDate", "Phiếu nhập hàng vật tư, nước uống từ nhà cung cấp vào kho chi nhánh."],
        ["Supplier (suppliers)", "id (Identity)", "Không", "supplierCode, supplierName, phone, address, contactPerson", "Danh sách đối tác cung cấp cầu, vợt, nước uống cho chuỗi."],
        ["Invoice (invoices)", "id (Identity)", "booking_code, branch_code", "invoiceCode, totalAmount, discountAmount, paymentMethod", "Hóa đơn thanh toán quầy POS in ra giấy nhiệt K80 cho khách."],
        ["Payment (payments)", "id (Identity)", "booking_code", "paymentCode, amount, transactionNo, status, paymentType", "Giao dịch thanh toán tiền cọc hoặc chuyển khoản ngân hàng."],
        ["PricingProposal", "id (Identity)", "branch_code", "proposalCode, proposerName, currentPrice, proposedPrice", "Tờ trình đề xuất điều chỉnh giá sân của Quản lý gửi Giám đốc."],
        ["Promotion (promotions)", "id (Identity)", "Không", "promoCode, promoName, discountValue, scope, startDate", "Mã giảm giá (voucher) áp dụng toàn chuỗi hoặc theo chi nhánh."],
        ["AuditLog (audit_logs)", "id (Identity)", "Không", "action, username, ipAddress, details, createdAt", "Nhật ký kiểm toán hệ thống ghi vết mọi thao tác can thiệp dữ liệu."],
        ["ChatMessage (chat_messages)", "id (Identity)", "Không", "sender, recipient, message, timestamp, intent", "Lịch sử tin nhắn giữa khách hàng và Trợ lý ảo AI / Hỗ trợ viên."]
    ]
    add_table(doc, erd_headers, erd_rows, col_widths=[1.3, 0.9, 1.2, 1.8, 1.6])

    # 1.6 Cơ chế Bảo mật & Phân quyền
    add_h2(doc, "1.6. Cơ chế Bảo mật & Phân quyền đa cấp (RBAC + Scoped Data Isolation)")
    add_paragraph(doc, "Một trong những điểm sáng vượt trội của dự án UTE Sport chính là kiến trúc bảo mật 2 lớp: Phân quyền vai trò tác nghiệp (Role-Based Access Control - RBAC) kết hợp cùng Cô lập phạm vi dữ liệu chi nhánh (Scoped Data Isolation).")

    add_h3(doc, "1.6.1. Cơ chế Xác thực kép Token JWT (Header Authorization & HttpOnly Cookie)")
    add_paragraph(doc, "Hệ thống hỗ trợ cả hai phương thức truyền mã xác thực JWT nhằm tối ưu cho cả Single Page Application và Server-Side Rendering:")
    add_bullet(doc, "Khách hàng hoặc nhân viên gọi API qua Fetch/AJAX sẽ gửi kèm tiêu đề: Authorization: Bearer <JWT_TOKEN>.", bold_prefix="Cách 1 (API Client): ");
    add_bullet(doc, "Khi người dùng đăng nhập qua form Web thông thường, máy chủ tự động lưu Token vào Cookie trình duyệt mang tên jwt_token. Các lượt duyệt trang tiếp theo sẽ tự động đính kèm Cookie này.", bold_prefix="Cách 2 (Browser Session): ");

    add_paragraph(doc, "Bộ lọc JwtAuthenticationFilter (kế thừa OncePerRequestFilter) được thiết kế thông minh để kiểm tra lần lượt: nếu không có Header Authorization thì sẽ tự động đọc từ Cookie:")
    
    jwt_filter_code = """
// Trích đoạn mã nguồn trong JwtAuthenticationFilter.java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
        throws ServletException, IOException {
    try {
        String jwt = getJwtFromRequest(request); // Đọc từ Header Bearer hoặc Cookie jwt_token
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            String username = tokenProvider.getUsernameFromToken(jwt);
            String role = tokenProvider.getRoleFromToken(jwt);
            
            // Chuẩn hóa tên quyền hạn theo chuẩn Spring Security (ROLE_...)
            String roleName = role != null ? role.trim().toUpperCase() : "CUSTOMER";
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            
            // Thiết lập danh tính người dùng vào SecurityContextHolder
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    } catch (Exception ex) {
        logger.error("Không thể thiết lập xác thực người dùng trong security context", ex);
    }
    filterChain.doFilter(request, response);
}
"""
    add_code_block(doc, jwt_filter_code, "Xác thực JWT và nạp quyền vào SecurityContextHolder")

    add_h3(doc, "1.6.2. Cơ chế Cô lập dữ liệu chi nhánh (Data Scope Isolation)")
    add_paragraph(doc, "Phân quyền theo chức danh (Role) là chưa đủ. Một Quản lý (ROLE_MANAGER) hoặc Thu ngân (ROLE_POS) không được phép xem sân, sửa giá hoặc truy cập dữ liệu của chi nhánh khác. Để giải quyết bài toán này, thực thể User sở hữu trường dữ liệu branchCode với quy tắc:")
    add_bullet(doc, "Được gán cho Giám đốc (DIRECTOR) và Quản trị viên (ADMIN). Khi gọi các API truy vấn sân, doanh thu, kho hàng, hệ thống tự động trả về toàn bộ dữ liệu của chuỗi không bị giới hạn.", bold_prefix="Giá trị 'ALL': ");
    add_bullet(doc, "Được gán cho Quản lý hoặc Thu ngân. Ví dụ: 'CN01' (chỉ phụ trách cơ sở 1) hoặc 'CN01,CN02' (phụ trách đồng thời 2 cơ sở 1 và 2). Hệ thống sẽ bóc tách chuỗi này và chỉ cho phép người dùng xem/thao tác trên các cơ sở hợp lệ.", bold_prefix="Giá trị mã chi nhánh phân tách dấu phẩy: ");

    add_note_box(doc, 
        "Trong ApiController, hàm resolveEffectiveBranchCode(User user, String requestedBranch) đóng vai trò lá chắn bảo vệ: "
        "Nếu người đăng nhập là Quản lý chi nhánh (ROLE_MANAGER), dù trên trình duyệt họ có cố tình thay đổi tham số ?branchCode=CN03, "
        "hàm này sẽ tự động chặn đứng và ép chặt phạm vi về đúng danh sách chi nhánh được phân quyền trong CSDL (ví dụ chỉ CN01). "
        "Điều này ngăn chặn 100% nguy cơ rò rỉ dữ liệu giữa các cơ sở đối thủ trong chuỗi!",
        title="BẢO MẬT ĐA CƠ SỞ (MULTI-TENANT DATA ISOLATION)"
    )

    # 1.7 Cơ chế WebSocket STOMP
    add_h2(doc, "1.7. Cơ chế Giao tiếp thời gian thực (Real-time STOMP WebSocket)")
    add_paragraph(doc, "Một trong những trải nghiệm khó chịu nhất của khách hàng là khi mở trang đặt sân thấy một slot giờ còn trống, nhưng khi nhấn nút đặt thì hệ thống báo lỗi do người khác đã đặt trước đó 5 giây. Để triệt tiêu độ trễ này, UTE Sport tích hợp công nghệ WebSocket STOMP kết nối liên tục:")

    stomp_code = """
// 1. Phía Server: Khi có bất kỳ lượt khóa slot, thanh toán hoặc đổi sân thành công
@Autowired
private SimpMessagingTemplate messagingTemplate;

public void broadcastCourtSlotUpdate(String courtCode, String timeSlot, String branchCode, String newStatus) {
    Map<String, Object> payload = Map.of(
        "type", "SLOT_STATUS_CHANGED",
        "courtCode", courtCode,
        "timeSlot", timeSlot,
        "branchCode", branchCode,
        "status", newStatus,
        "timestamp", System.currentTimeMillis()
    );
    // Bắn thông báo tới toàn bộ các client đang theo dõi kênh /topic/slots
    messagingTemplate.convertAndSend("/topic/slots", payload);
}

// 2. Phía Client (global-hub.js & pos-grid.js): Lắng nghe sự kiện
stompClient.subscribe('/topic/slots', function(message) {
    const data = JSON.parse(message.body);
    // Tự động tìm ô lưới tương ứng trên màn hình và chuyển màu sang ĐỎ (Đã đặt) hoặc VÀNG (Đang giữ chỗ)
    updateSlotCellRealtime(data.courtCode, data.timeSlot, data.status);
});
"""
    add_code_block(doc, stomp_code, "Phát và đón nhận thông điệp STOMP thời gian thực giữa Server và Client")
    add_paragraph(doc, "Nhờ cơ chế này, ngay khi thu ngân tại quầy chi nhánh 1 nhấn chuột chọn sân, toàn bộ màn hình của khách hàng đang truy cập trên điện thoại sẽ lập tức cập nhật trạng thái ô giờ đó sang 'Đang giữ chỗ', loại bỏ hoàn toàn khả năng 2 người đặt đè lên nhau.")

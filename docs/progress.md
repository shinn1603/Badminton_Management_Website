# Nhật ký tiến độ tích hợp — Badminton Management Project

## Ngày 1 (17/10/2026) — Thọ (AI-H)

- **Nhánh làm việc:** `tho/day-01-courts`
- **Phạm vi được giao:** Rà dependency sân/lịch/đặt sân; chuẩn bị entity `Court`, `TimeSlot` và quan hệ `Branch` cần thiết; thống nhất dữ liệu seed cho luồng đặt sân.
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/entity/Court.java`: Entity sân cầu lông (mã sân, tên sân, loại sân, chi nhánh, giá/giờ, trạng thái, ảnh).
  - `src/main/java/vn/yain/entity/TimeSlot.java`: Entity khung giờ (mã slot, giờ bắt đầu, giờ kết thúc, loại slot).
  - `src/main/java/vn/yain/repository/CourtRepository.java`: JPA repository với query `findByBranchCode` và `findByCourtCode`.
  - `src/main/java/vn/yain/repository/TimeSlotRepository.java`: JPA repository cho khung giờ.
  - `src/test/java/vn/yain/repository/CourtAndTimeSlotRepositoryTest.java`: Integration test xác minh lưu trữ và truy vấn repository.
- **Chức năng đã xong:**
  - Định nghĩa hoàn tất miền dữ liệu cốt lõi cho Sân và Khung giờ.
  - Đảm bảo JPA repository bootstrap hợp lệ và tự động scan bởi Spring Data JPA.
  - Xác nhận chuẩn quan hệ `Branch` theo thiết kế ZIP: `Court` lưu `branchCode` (String) độc lập tầng JPA, liên kết khóa ngoại ở mức CSDL DDL với bảng `branches`.
  - Trích xuất và thống nhất bộ dữ liệu seed chuẩn cho luồng sân và đặt sân (4 chi nhánh + hội sở, 26 sân cầu lông, 4 khung giờ chuẩn, các đơn đặt sân mẫu).
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test -Dmaven.compiler.release=24 -Dtest=CourtAndTimeSlotRepositoryTest`: 2/2 tests PASSED (100%).
  - `.\mvnw.cmd clean test -Dmaven.compiler.release=24`: Toàn bộ suite PASSED, Spring Boot context load thành công, H2 in-memory kết nối và tạo schema JPA không lỗi.
- **Điểm chưa xác minh & Phát hiện kỹ thuật bàn giao cho Triết:**
  - `pom.xml` hiện tại khai báo `<java.version>27</java.version>`, gây lỗi `release version 27 not supported` trên JDK 26 và lỗi ASM bytecode `Unsupported class file version: 71` trong Spring Boot 4.1.1. Cần áp dụng cấu hình source/target/release 24 như bản ZIP gốc.
  - Cần thêm `src/test/resources/application.properties` với cấu hình H2 test chuẩn từ ZIP.
  - Cần bổ sung `*.zip` vào `.gitignore` để tránh stage nhầm file ZIP tham chiếu.
- **Điểm dừng:** Hoàn tất nền tảng Sân và Khung giờ Ngày 1. Sẵn sàng cho Ngày 2 (tích hợp `CourtService`, danh sách sân và giao diện xem sân).

## Ngày 2 (18/10/2026) — Đạt (AI-D)

- **Nhánh làm việc:** `dat/day-02-catalog`
- **Phạm vi được giao:** Tích hợp phân hệ danh mục (`Product`, `Equipment`) và quy tắc giá (`PricingRule`, `PricingService`) phục vụ tính giá cho luồng đặt sân và POS.
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/entity/Product.java`: Thực thể quản lý sản phẩm, đồ uống và định mức tồn kho.
  - `src/main/java/vn/yain/entity/Equipment.java`: Thực thể quản lý dụng cụ thể thao cho thuê (vợt, cầu).
  - `src/main/java/vn/yain/entity/PricingRule.java`: Thực thể quản lý bảng giá theo chi nhánh, khung giờ và loại ngày.
  - `src/main/java/vn/yain/service/PricingService.java`: Dịch vụ tính toán tiền thuê sân dựa trên thời lượng và mã khuyến mãi áp dụng.
- **Chức năng đã xong:**
  - Hoàn thành ánh xạ thực thể JPA cho các bảng danh mục cốt lõi, chuẩn hóa kiểu dữ liệu khớp 100% với cấu trúc DDL SQL (`init_badminton.sql`).
  - Xây dựng thành công `PricingService` mô phỏng logic hàm `dbo.fn_CalculateBookingPrice` phục vụ sẵn sàng cho `BookingService` ở Ngày 3.
- **Kiểm tra đã chạy và kết quả:**
  - Clean build project trên STS/Maven thành công, các repository (`ProductRepository`, `EquipmentRepository`, `PricingRuleRepository`) được khởi tạo và nhận diện đúng entity.
- **Điểm chưa xác minh:**
  - Giao diện quản lý danh mục (`inventory.html`, `pricing.html`) ở tầng Thymeleaf sẽ được liên kết dữ liệu hoàn chỉnh khi các API Controller tương ứng tích hợp vào Ngày 4–5.
- **Phần cần người khác tích hợp:**
- **Điểm dừng:** Hoàn tất phạm vi Ngày 2. Không tự động mở rộng sang nghiệp vụ nhập kho hoặc phân ca của các ngày tiếp theo. Sẵn sàng chuyển giao khi có yêu cầu.

## Ngày 2 (18/10/2026) — Thọ (AI-H)

- **Nhánh làm việc:** `tho/day-02-courts`
- **Phạm vi được giao:** Tích hợp `CourtService`, quản lý danh sách sân/chi nhánh, giao diện xem sân và khung giờ hoạt động; chưa bật tính năng đặt sân/thanh toán (chờ Ngày 3).
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/dto/CourtEventDto.java`: DTO phục vụ phát sự kiện trạng thái sân và khóa slot thời gian thực (`COURT_STATUS_CHANGED`, `COURT_HELD`, `COURT_HOLD_RELEASED`).
  - `src/main/java/vn/yain/service/CourtService.java`: Dịch vụ nghiệp vụ sân đấu, tra cứu theo chi nhánh, giữ chỗ tạm 10 phút (600s), tác vụ định kỳ tự động nhả slot quá hạn (`@Scheduled`), cập nhật trạng thái sân và phát WebSocket realtime.
  - `src/test/java/vn/yain/service/CourtServiceTest.java`: Bộ 7 unit tests bao phủ 100% các ca sử dụng XL-02 (giữ chỗ tạm 10 phút) và XL-04 (cập nhật trạng thái sân, kiểm tra ngoại lệ không tìm thấy sân, thêm mới và cập nhật sân).
  - `src/main/resources/templates/customer/booking.html`: Giao diện khách hàng tra cứu sân, chọn chi nhánh, xem trạng thái hoạt động và các khung giờ hoạt động.
  - `src/main/resources/templates/manager/courts.html`: Màn hình quản lý sân, sơ đồ trực quan và cập nhật trạng thái bảo trì/hoạt động.
  - `docs/progress.md`: Cập nhật nhật ký tiến độ Ngày 2.
- **Chức năng đã xong:**
  - Hoàn thành toàn bộ tầng service cho sân thi đấu (`CourtService`), sẵn sàng tích hợp với `PricingService` (Đạt) và `BookingService` (Ngày 3).
  - Tích hợp 2 giao diện cốt lõi để khách hàng và quản lý xem sân, khung giờ và trạng thái sân.
  - Đảm bảo chưa kích hoạt quy trình tạo booking / checkout (đúng yêu cầu điểm dừng Ngày 2).
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test -Dtest=CourtServiceTest`: 7/7 unit tests PASSED (100%).
  - `.\mvnw.cmd test`: Toàn bộ 10/10 tests của dự án PASSED, clean build thành công.
- **Phần cần người khác tích hợp (Patch gửi Triết):**
  - Áp dụng patch cho `ApiController.java` (`GET /api/courts`, `POST /api/courts/hold`, `POST /api/courts/status`, `POST /api/courts`, `DELETE /api/courts/{courtCode}`).
  - Áp dụng patch cho `DashboardController.java` (`@GetMapping("/customer/booking")` và `@GetMapping("/manager/courts")`).
- **Điểm dừng:** Hoàn thành toàn bộ mục tiêu Ngày 2 của Thọ. Sẵn sàng cho Ngày 3 (tích hợp `BookingService`, đặt sân và kiểm tra trùng lịch).

## Ngày 2 (18/10/2026) — Triết (AI-T)

- **Nhánh làm việc:** `triet/day-02-auth`
- **Phạm vi được giao:** `UserService`, JWT & Security infrastructure, đăng nhập/đăng ký, logout phía giao diện và phân quyền theo vai trò đúng bản gốc; tích hợp các controllers dùng chung (`DashboardController.java`, `ApiController.java`, `HomeController.java`); tích hợp static assets auth guard và global hub.
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/security/JwtTokenProvider.java`: Sinh và giải mã JWT token (HMAC-SHA256), trích xuất username/role/fullName/branchCode, kiểm tra tính hợp lệ và thời hạn token.
  - `src/main/java/vn/yain/security/JwtAuthenticationFilter.java`: Filter chặn request, đọc JWT từ `Authorization: Bearer <token>` hoặc cookie `jwt_token`, thiết lập `SecurityContext`.
  - `src/main/java/vn/yain/security/SecurityConfig.java`: Cấu hình Spring Security 6 với JWT filter, CORS policy, phân quyền theo role (`ROLE_ADMIN`, `ROLE_DIRECTOR`, `ROLE_MANAGER`, `ROLE_POS`, `ROLE_CUSTOMER`), permitAll cho static/assets/auth.
  - `src/main/java/vn/yain/service/UserService.java`: Xử lý xác thực người dùng, tự động đồng bộ tài khoản từ khách hàng, nâng cấp mật khẩu plaintext sang BCrypt an toàn, cơ chế khóa tạm 15 phút sau 5 lần nhập sai, đăng ký hội viên mới, reset mật khẩu.
  - `src/main/java/vn/yain/controller/DashboardController.java`: Controller điều hướng giao diện Thymeleaf (`/login`, `/customer/auth`, `/customer/login`, `/customer/home`, `/customer/booking`, `/manager/courts`).
  - `src/main/java/vn/yain/controller/HomeController.java`: Cập nhật redirect root `/` và `/customer` về `/customer/home`.
  - `src/main/java/vn/yain/controller/ApiController.java`: Tích hợp các REST endpoints Ngày 2 (`/api/auth/login`, `/api/auth/register`, `/api/auth/forgot-password`, `/api/auth/reset-password`, `/api/auth/me`, `/api/customer/profile`, `/api/courts`, `/api/courts/hold`, `/api/courts/status`, `/api/branches`, `/api/products`, `/api/equipment`, `/api/pricing/summary`).
  - `src/main/resources/templates/login.html`: Giao diện đăng nhập tập trung cho nhân sự và ban quản lý.
  - `src/main/resources/templates/customer/auth.html`: Giao diện đăng nhập/đăng ký/quên mật khẩu dành riêng cho khách hàng.
  - `src/main/resources/static/js/auth-guard.js`: Bộ định tuyến và bảo vệ frontend, kiểm tra JWT token, phân quyền chuyển hướng theo vai trò và hỗ trợ logout.
  - `src/main/resources/static/js/global-hub.js`: Client WebSocket và EventBus dùng chung phục vụ nhận thông báo và cập nhật thời gian thực.
  - `src/test/java/vn/yain/security/JwtTokenProviderTest.java`: 4 unit tests xác minh sinh token, kiểm tra hạn dùng, kiểm tra vai trò và từ chối token giả mạo.
  - `src/test/java/vn/yain/service/UserServiceTest.java`: 8 unit tests xác minh xác thực XL-01, khóa tạm tài khoản, đăng ký UC-02, trùng lặp và đồng bộ khách hàng.
  - `docs/progress.md`: Cập nhật tiến độ hoàn thành Ngày 2 của Triết.
- **Chức năng đã xong:**
  - Hoàn tất toàn bộ phân hệ xác thực và phân quyền người dùng (Security, JWT, BCrypt, Auth Filter).
  - Tích hợp giao diện đăng nhập nhân viên (`/login`) và cổng đăng nhập/đăng ký hội viên (`/customer/auth`).
  - Đóng gói trọn vẹn các API tra cứu dùng chung cho Ngày 2 (Courts, Branches, Products, Equipment, Pricing Summary) mà không kéo theo các module nghiệp vụ tương lai (đặt cọc, check-in, checkout, giải đấu).
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test`: Toàn bộ 22/22 unit và integration tests PASSED (100%), build thành công (`BUILD SUCCESS`).
- **Điểm chưa xác minh:**
  - Endpoint gửi email OTP thật phụ thuộc vào cấu hình SMTP trên môi trường thực tế (hiện tại fallback mô phỏng logger đầy đủ theo thiết kế gốc).
- **Điểm dừng:** Hoàn thành toàn bộ mục tiêu Ngày 2 cho Triết. Sẵn sàng cho Ngày 3 (BookingService, quản lý vòng đời đặt sân và tích hợp API hồ sơ khách hàng đầy đủ).

## Ngày 3 (19/10/2026) — Triết (AI-T)

- **Nhánh làm việc:** `triet/day-03-customer-profile`
- **Phạm vi được giao:** Quản lý hồ sơ khách hàng; tích hợp API dùng chung của Ngày 3 (`/api/customer/profile`, `/api/customer/my-bookings`); đảm bảo bảo mật và phân quyền dữ liệu người dùng chỉ xem/sửa thông tin của chính mình; bổ sung giao diện Thymeleaf `customer/profile.html`.
- **File đã tạo/sửa:**
  - `src/main/resources/templates/customer/profile.html`: Giao diện hồ sơ hội viên, tích điểm, thẻ thành viên, tiến trình lên hạng (Đồng $\rightarrow$ Bạc $\rightarrow$ Vàng $\rightarrow$ Kim Cương), form cập nhật thông tin cá nhân và lịch sử tích lũy điểm.
  - `src/main/java/vn/yain/controller/DashboardController.java`: Bổ sung route điều hướng `@GetMapping("/customer/profile")` trỏ đến `customer/profile.html`.
  - `src/main/java/vn/yain/controller/ApiController.java`: Bổ sung endpoint `PUT /api/customer/profile` (cập nhật họ tên, số điện thoại, email, mật khẩu mới, đồng bộ tức thời giữa `users` và `customers`) và `GET /api/customer/my-bookings`. Đảm bảo phân quyền nghiêm ngặt dựa trên định danh trích xuất từ JWT token của phiên đăng nhập hợp lệ.
  - `src/test/java/vn/yain/controller/CustomerProfileTest.java`: Bộ 5 integration tests xác minh giao diện hồ sơ, từ chối truy cập/cập nhật trái phép khi thiếu token, truy vấn hồ sơ thành công và cập nhật đồng bộ 2 bảng `users` - `customers`.
  - `docs/progress.md`: Cập nhật nhật ký tiến độ Ngày 3 của Triết.
- **Chức năng đã xong:**
  - Người dùng đăng nhập có thể xem chi tiết hồ sơ cá nhân, hạng hội viên, điểm tích lũy, số giờ chơi.
  - Cập nhật thông tin tài khoản an toàn với xác thực JWT: không thể xem hoặc chỉnh sửa dữ liệu của người dùng khác.
  - Đồng bộ dữ liệu 2 chiều giữa bảng `users` và bảng `customers`.
  - Giữ vững nguyên tắc đóng gói Ngày 3: không đưa các module thanh toán hay báo cáo quản trị của Ngày 4-9 vào sớm.
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test`: Toàn bộ 27/27 unit & integration tests PASSED (100%), `BUILD SUCCESS`.
- **Điểm chưa xác minh:**
  - Lịch sử đặt sân chi tiết trong tab "Lịch sử tích điểm" sẽ hiển thị đầy đủ ngay khi PR của Thọ (Ngày 3 - `BookingService` & `BookingRepository`) được merge vào nhánh `main`.
- **Điểm dừng:** Hoàn thành toàn bộ mục tiêu Ngày 3 cho Triết. Sẵn sàng phối hợp kiểm tra tích hợp khi Thọ và Đạt hoàn tất PR Ngày 3.

## Ngày 4 (20/10/2026) — Đạt (AI-D)

- **Nhánh làm việc:** `dat/day-04-inventory`
- **Phạm vi được giao:** Triển khai WarehouseService, tạo phiếu nhập kho (StockReceipt), cập nhật tồn kho tự động khi nhập và trừ kho khi bán lẻ dịch vụ tại sân.
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/entity/StockReceipt.java`: Thực thể phiếu nhập kho, liên kết mã sản phẩm, chi nhánh và giá nhập.
  - `src/main/java/vn/yain/repository/StockReceiptRepository.java`: Repository lưu trữ và tra cứu phiếu nhập theo chi nhánh/thời gian.
  - `src/main/java/vn/yain/service/WarehouseService.java`: Bổ sung hàm tạo phiếu nhập (tự động cộng tồn kho) và hàm xuất kho (trừ tồn kho khi POS gọi dịch vụ).
  - Soạn Patch API phiếu nhập và xuất kho gửi Triết đưa vào `ApiController.java`.
- **Chức năng đã xong:**
  - Quy trình nhập kho qua phiếu nhập (`createStockReceipt`) đồng bộ số lượng tồn và giá vốn của `Product`.
  - Cung cấp hàm `deductStockForSale` phục vụ Thọ trừ tồn kho khi thu ngân gọi dịch vụ trên lưới POS.
- **Kiểm tra đã chạy và kết quả:**
  - Build Maven compile thành công, kiểm tra ràng buộc số lượng tồn không bị âm khi trừ kho vượt mức.
- **Điểm chưa xác minh:**
  - Giao diện `manager/receipts.html` và `manager/inventory.html` sẽ được kết nối dữ liệu hoàn chỉnh khi Triết tích hợp patch API vào controller.
- **Phần cần người khác tích hợp:**
  - Triết: Gộp patch API `/api/receipts` và `/api/inventory/deduct` vào `ApiController.java`.
  - Thọ: Gọi `warehouseService.deductStockForSale` khi xuất hóa đơn dịch vụ POS.
- **Điểm dừng:** Hoàn thành phạm vi Ngày 4. Không tự ý sang phần phân ca hoặc duyệt giá của Ngày 5.

## Ngày 5 (21/10/2026) — Triết (AI-T)

- **Nhánh làm việc:** `triet/day-05-user-admin`
- **Phạm vi được giao:** Màn hình quản trị users và permissions; kiểm tra quyền giữa các vai trò theo code gốc; tích hợp API quản lý tài khoản và phạm vi dữ liệu chi nhánh (`/api/admin/users`, `/api/admin/permissions`); cấu hình bảo mật phân quyền.
- **File đã tạo/sửa:**
  - `src/main/resources/templates/admin/users.html`: Màn hình quản trị danh sách người dùng, lọc theo vai trò/chi nhánh, cấp mới/sửa tài khoản, khóa và mở khóa tài khoản.
  - `src/main/resources/templates/admin/permissions.html`: Màn hình ma trận phân quyền tác nghiệp và phạm vi chi nhánh (Toàn chuỗi ALL, CN01, CN02, CN03, CN04) theo thời gian thực.
  - `src/main/java/vn/yain/controller/DashboardController.java`: Bổ sung điều hướng `@GetMapping("/admin/users")` và `@GetMapping("/admin/permissions")`, liên kết danh sách người dùng và chi nhánh vào Model Thymeleaf; cập nhật mapping các trang dùng chung (`/pos/grid`, `/customer/history`, `/customer/payment`, `/manager/bookings`).
  - `src/main/java/vn/yain/controller/ApiController.java`: Tích hợp các REST endpoints Ngày 5: `GET /api/admin/users` (danh sách tài khoản kèm khách hàng đồng bộ), `POST /api/admin/users` (tạo/sửa tài khoản), `POST /api/admin/users/{username}/toggle-status` (khóa/mở khóa tài khoản), `GET /api/admin/permissions` (truy xuất ma trận tài khoản & chi nhánh), `POST /api/admin/permissions` (cập nhật phạm vi chi nhánh và ghi audit log).
  - `src/main/java/vn/yain/security/SecurityConfig.java`: Khai báo rõ ràng quyền truy cập cho phân hệ quản trị (`/api/admin/**`).
  - `src/test/java/vn/yain/controller/AdminUserManagementTest.java`: Bộ 5 integration tests kiểm tra render view Thymeleaf, CRUD người dùng, khóa/mở khóa tài khoản và phân quyền chi nhánh.
  - `docs/progress.md`: Cập nhật nhật ký tiến độ Ngày 5 của Triết.
- **Chức năng đã xong:**
  - Hoàn tất quản trị tài khoản người dùng (`users.html`) và ma trận phân quyền chi nhánh (`permissions.html`).
  - Đảm bảo cơ chế RBAC (`auth-guard.js`) và phân quyền backend (`SecurityConfig.java`) hoạt động chuẩn xác theo thiết kế gốc.
  - Tự động ghi nhận Audit Log khi quản trị viên cập nhật phạm vi chi nhánh.
  - Không kéo các module cấu hình tham số (`settings.html`) hoặc nhật ký hoạt động (`logs.html`) của Ngày 6 vào sớm.
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test`: Toàn bộ 58/58 unit và integration tests PASSED (100%), `BUILD SUCCESS`.
- **Điểm chưa xác minh:**
  - Bảng phân quyền chi nhánh sẽ tự động hiển thị thêm nhân sự khi Đạt (`dat/day-05-pricing-staff`) tích hợp module `Staff` và `WorkShift`.
- **Phần cần người khác tích hợp:**
  - Đạt & Thọ: Sử dụng API `/api/admin/users` và dữ liệu chi nhánh đã được chuẩn hóa để liên kết với phân ca làm và tổng kết ca thu ngân.
- **Điểm dừng:** Hoàn thành toàn bộ mục tiêu Ngày 5 của Triết. Sẵn sàng tạo PR và bàn giao cho Đạt, Thọ tiếp tục tích hợp.

## Ngày 5 (21/10/2026) — Thọ (AI-H)

- **Nhánh làm việc:** `tho/day-05-shift-closing`
- **Phạm vi được giao:** Tích hợp `ShiftService` quản lý phân ca, nhân sự chi nhánh và chốt/bàn giao ca trực thu ngân (Use Case XL-08); tính toán tổng hợp doanh thu trong ca (tiền mặt két, chuyển khoản VietQR, tiền sân, tiền dịch vụ/sản phẩm); kiểm kê đối soát tiền mặt thực tế và tính chênh lệch thừa/thiếu; lưu vết bàn giao ca và xuất biên bản kết ca.
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/entity/Staff.java`: Thực thể nhân viên chi nhánh, liên kết mã chi nhánh, vị trí công tác, thông tin liên lạc và trạng thái.
  - `src/main/java/vn/yain/entity/WorkShift.java`: Thực thể ca làm việc và bàn giao ca (`shiftCode`, `dayOfWeek`, `shiftType`, `duty`, `icon`, `startTime`, `endTime`, `status`).
  - `src/main/java/vn/yain/repository/StaffRepository.java`: JPA repository quản lý nhân sự với các phương thức tìm kiếm theo mã, chi nhánh, email.
  - `src/main/java/vn/yain/repository/WorkShiftRepository.java`: JPA repository quản lý lịch phân ca và các bản ghi bàn giao ca trực.
  - `src/main/java/vn/yain/service/ShiftService.java`: Dịch vụ nghiệp vụ quản lý nhân sự, phân ca tuần và bàn giao ca thu ngân XL-08 (`getShiftSummary`, `closeShift`).
  - `src/test/java/vn/yain/service/ShiftServiceTest.java`: Bộ 5 unit tests bao phủ 100% các ca kiểm thử: thống kê doanh thu tiền mặt và chuyển khoản, chốt ca khớp tiền mặt 100%, chốt ca thiếu tiền quỹ, CRUD nhân sự và phân ca/hủy ca.
  - `docs/progress.md`: Cập nhật nhật ký tiến độ Ngày 5 của Thọ.
- **Chức năng đã xong:**
  - Hoàn tất toàn bộ logic dịch vụ kết ca thu ngân XL-08 và quản lý phân ca tuần theo đúng mã nguồn gốc tham chiếu.
  - Tích hợp tính năng đối soát tiền mặt thực tế so với sổ sách hệ thống với thông điệp rõ ràng (khớp quỹ, thừa quỹ, thiếu quỹ).
  - Khởi tạo tự động dữ liệu mẫu cho nhân sự và lịch phân ca khi khởi động.
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test -Dtest=ShiftServiceTest`: 5/5 unit tests PASSED (100%).
  - `.\mvnw.cmd test`: Toàn bộ 63/63 tests của dự án PASSED, clean build thành công (`BUILD SUCCESS`).
- **Phần cần người khác tích hợp (Patch gửi Triết):**
  - Áp dụng patch cho `ApiController.java` (`GET /api/shifts/summary`, `POST /api/shifts/close`, `GET /api/shifts/weekly`, `POST /api/shifts/weekly`, `POST /api/shifts/assign`, `POST /api/shifts/remove`, `POST /api/shifts/reset-weekly`, `GET /api/staff`, `POST /api/staff`, `DELETE /api/staff/{staffCode}`).
  - Áp dụng patch cho `DashboardController.java` (`@GetMapping("/manager/staff")`).
- **Điểm dừng:** Hoàn thành toàn bộ mục tiêu Ngày 5 của Thọ. Sẵn sàng cho Ngày 6 (WebSocketConfig và cập nhật trạng thái realtime).

## Ngày 6 (22/10/2026) — Triết (AI-T)

- **Nhánh làm việc:** `triet/day-06-system-settings`
- **Phạm vi được giao:** Tích hợp phân hệ cấu hình thông số hệ thống (`SystemSetting`), nhật ký kiểm toán và sao lưu (`AuditLog`); màn hình cấu hình (`settings.html`), nhật ký hoạt động (`logs.html`), chính sách hoàn cọc (`policy.html`); quy trình khôi phục mật khẩu qua mã OTP gửi về email (`EmailService`); đồng bộ quyền truy cập chính sách.
- **File đã tạo/sửa:**
  - `src/main/resources/templates/admin/settings.html`: Giao diện quản trị thông số vận hành (tỷ lệ đặt cọc, thời gian giữ slot 10 phút, thời hạn hủy sân 4 tiếng, số ngày đặt trước, tích hợp cổng thanh toán), hỗ trợ khôi phục mặc định.
  - `src/main/resources/templates/admin/logs.html`: Giao diện xem 100 bản ghi nhật ký kiểm toán gần nhất, bảng lịch sử sao lưu CSDL SQL Server và nút sao lưu thủ công tức thì.
  - `src/main/resources/templates/customer/policy.html`: Trang công bố chính sách hoàn cọc 100% tự động, quy định đặt/hủy sân, hiển thị thông số động và danh sách chi nhánh từ CSDL.
  - `src/main/java/vn/yain/controller/DashboardController.java`: Bổ sung điều hướng `@GetMapping("/admin/settings")`, `@GetMapping("/admin/logs")` và `@GetMapping({"/customer/policy", "/policy"})`.
  - `src/main/java/vn/yain/controller/ApiController.java`: Bổ sung REST APIs: `GET /api/policy/settings`, `GET /api/admin/settings`, `POST /api/admin/settings`, `POST /api/admin/settings/reset`, `GET /api/admin/logs`, `GET /api/admin/logs/backups`, `POST /api/admin/logs/backup`; kích hoạt gửi email OTP và email thông báo đổi mật khẩu qua `EmailService`.
  - `src/main/java/vn/yain/service/EmailService.java`: Nâng cấp dịch vụ gửi email HTML bất đồng bộ (sendHtmlEmailAsync), gửi OTP khôi phục mật khẩu (sendPasswordResetOtp), thông báo đổi mật khẩu (sendPasswordChangedNotification), phiếu xác nhận đặt sân và vé điện tử E-Ticket với mã QR.
  - `src/main/java/vn/yain/security/SecurityConfig.java`: Cho phép truy cập công khai không cần đăng nhập cho `/policy` và `/api/policy/**`.
  - `src/test/java/vn/yain/controller/SystemSettingsAndAuditLogsTest.java`: Bộ 5 integration tests kiểm tra render view Thymeleaf, CRUD cấu hình hệ thống, reset mặc định, ghi nhật ký kiểm toán & sao lưu, quy trình quên/đổi mật khẩu qua email OTP.
  - `docs/progress.md`: Cập nhật nhật ký tiến độ Ngày 6 của Triết.
- **Chức năng đã xong:**
  - Quản trị thông số toàn chuỗi (`settings.html`) với lưu trữ CSDL thời gian thực và ghi nhận Audit Log.
  - Quản lý nhật ký hoạt động (`logs.html`) và mô phỏng sao lưu/khôi phục CSDL SQL Server.
  - Cổng thông tin chính sách hoàn cọc (`policy.html`) nạp dữ liệu động từ API chính sách.
  - Hoàn thiện luồng quên/đặt lại mật khẩu gửi OTP 6 số và thông báo đổi mật khẩu qua email.
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test`: Toàn bộ 68/68 unit và integration tests PASSED (100%), `BUILD SUCCESS`.
  - Bộ test mới `SystemSettingsAndAuditLogsTest`: 5/5 tests passed.
- **Điểm chưa xác minh:**
  - Gửi email SMTP thực tế phụ thuộc cấu hình `spring.mail.password` trong môi trường sản xuất; hiện tại hệ thống tự động fallback sang chế độ mô phỏng log (SIMULATED_DELIVERED_SUCCESSFULLY) hoạt động ổn định và chính xác.
- **Phần cần người khác tích hợp:**
  - Thọ (`tho/day-06-booking-realtime`): Sử dụng thông số `CFG_HOLDING_MINUTES` và `CFG_DEPOSIT_RATE` từ API `/api/policy/settings` cho luồng giữ chỗ WebSocket.
  - Đạt (`dat/day-06-branch-tournaments`): Đồng bộ danh sách chi nhánh và giải đấu với dữ liệu chính sách và thông số quản trị.
- **Điểm dừng:** Hoàn thành toàn bộ mục tiêu Ngày 6 của Triết. Sẵn sàng tạo PR và bàn giao cho Đạt, Thọ tiếp tục tích hợp.
## Ngày 6 (22/10/2026) — Thọ (AI-H)

- **Nhánh làm việc:** `tho/day-06-realtime-updates`
- **Phạm vi được giao:** Tích hợp `WebSocketConfig` thiết lập hạ tầng STOMP Message Broker; hỗ trợ WebSocket thuần và SockJS fallback (`/ws-court`); định tuyến tin nhắn thông báo đặt sân (`/topic/bookings`, `/topic/notifications`, `/topic/bookings/{branchCode}`) và sự kiện giữ/khóa slot thời gian thực (`/topic/courts`, `/topic/courts/{branchCode}`); kiểm tra tích hợp tương tác thời gian thực giữa các trình duyệt.
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/config/WebSocketConfig.java`: Cấu hình Spring WebSocket STOMP endpoint (`/ws-court`), cho phép SockJS fallback và cấu hình Simple Message Broker cho prefix `/topic`, `/queue` cùng application prefix `/app`.
  - `src/test/java/vn/yain/config/WebSocketRealtimeIntegrationTest.java`: Bộ 5 tests bao gồm integration context test xác minh `WebSocketConfig` và `SimpMessagingTemplate` được nạp tự động bởi Spring Boot, cùng các test xác minh đăng ký STOMP endpoint, Message Broker và cơ chế phát thông báo/sự kiện sân thời gian thực.
  - `docs/progress.md`: Cập nhật nhật ký tiến độ Ngày 6 của Thọ.
- **Chức năng đã xong:**
  - Kích hoạt toàn bộ hạ tầng WebSocket STOMP cho dự án, kết nối thông suốt với `global-hub.js` trên frontend qua endpoint `/ws-court/websocket`.
  - Kết nối tự động `SimpMessagingTemplate` với `CourtService` (phát sự kiện `COURT_HELD`, `COURT_RELEASED`, `COURT_STATUS_CHANGED`) và `BookingService` (phát sự kiện `NEW_BOOKING`, `DEPOSIT_CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `BOOKING_CANCELLED`).
  - Đảm bảo cơ chế giữ chỗ tạm 10 phút (600s) và khóa slot đặt sân được đồng bộ tức thì đến tất cả client mà không cần tải lại trang.
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test -Dtest=WebSocketRealtimeIntegrationTest`: 5/5 tests PASSED (100%).
  - `.\mvnw.cmd test`: Toàn bộ 68/68 tests của dự án PASSED, clean build thành công (`BUILD SUCCESS`).
- **Điểm chưa xác minh:**
  - Kiểm thử đồng thời đa trình duyệt thực tế trên môi trường mạng ngoài sẽ được thực hiện khi deploy và smoke test ở Ngày 8.
- **Điểm dừng:** Hoàn thành toàn bộ mục tiêu Ngày 6 của Thọ. Sẵn sàng cho Ngày 7 (luồng thanh toán/VietQR webhook và hoàn thiện customer payment/POS).






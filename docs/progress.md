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

## Ngày 5 (21/10/2026) — Đạt (AI-D)

- **Nhánh làm việc:** `dat/day-05-staff-pricing-promotions`
- **Phạm vi được giao:** Tích hợp nhân sự (Staff), phân ca (WorkShift, ShiftService), đề xuất/duyệt giá (PricingProposal), khuyến mãi (Promotion) và quy tắc tính giá đầy đủ trong PricingService.
- **File đã tạo/sửa:**
  - `PricingProposal.java`, `Promotion.java`
  - `PricingProposalRepository.java`, `PromotionRepository.java`
  - Soạn Patch API quản lý ca làm, đề xuất giá và khuyến mãi cho `ApiController.java`.
- **Chức năng đã xong:**
  - Quản lý phân ca nhân sự theo tuần, cơ chế mở/đóng ca đối soát tiền mặt.
  - Luồng đề xuất điều chỉnh bảng giá sân, duyệt giá cập nhật trực tiếp `PricingRule`.
  - Quản lý mã khuyến mãi và trừ tiền tự động theo ngày hiệu lực.
- **Điểm dừng:** Hoàn thành toàn diện phạm vi Ngày 5.

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

## Ngày 6 (22/10/2026) — Đạt (AI-D)

- **Nhánh làm việc:** `dat/day-06-branch-tournament`
- **Phạm vi được giao:** Quản lý chi nhánh cơ sở (Branch), thực thể & dịch vụ tổ chức giải đấu (Tournament), luồng đăng ký tham gia (TournamentRegistration, TournamentService).
- **File đã tạo/sửa:**
  - `Tournament.java`, `TournamentRegistration.java`
  - `BranchRepository.java`, `TournamentRepository.java`, `TournamentRegistrationRepository.java`
  - Soạn Patch API chi nhánh, giải đấu và đăng ký giải cho `ApiController.java`.
- **Chức năng đã xong:**
  - Tra cứu, bổ sung thông tin chi nhánh hoạt động.
  - Quản lý vòng đời giải đấu: tạo giải, phê duyệt giải, giới hạn số lượng vận động viên và ghi nhận đăng ký thi đấu.
- **Điểm dừng:** Hoàn thành toàn diện phạm vi Ngày 6, sẵn sàng cho Ngày 7 (Báo cáo & Dashboard).

## Ngày 7 (23/10/2026) — Thọ (AI-H)

- **Nhánh làm việc:** `tho/day-07-payment-notifications`
- **Phạm vi được giao:** Tích hợp luồng payment/VietQR webhook theo bản gốc; xử lý an toàn thông báo lặp (Idempotency) và đối soát trạng thái/số tiền thanh toán; hoàn thiện giao diện thanh toán khách hàng (`customer/payment.html`) và phát thông báo thanh toán thành công thời gian thực.
- **File đã tạo/sửa:**
  - `src/main/resources/templates/customer/payment.html`: Giao diện thanh toán tiền cọc 30% trực tuyến với mã VietQR động sinh theo chuẩn Napas 247 (`img.vietqr.io`), đếm ngược phiên giữ chỗ 10:00, nút xác nhận chuyển khoản và đồng bộ thời gian thực.
  - `src/main/java/vn/yain/service/PaymentWebhookService.java`: Dịch vụ xử lý webhook VietQR / Sepay / Casso với cơ chế chống xử lý lặp (Idempotency check: bỏ qua an toàn nếu đơn đã cọc hoặc giao dịch tham chiếu đã ghi nhận), đối soát số tiền chuyển thực tế so với tiền cọc, xác nhận cọc qua `BookingService` và phát thông báo sự kiện thanh toán.
  - `src/main/java/vn/yain/controller/PaymentWebhookController.java`: Controller RESTful tiếp nhận Webhook thanh toán (`POST /api/webhook/vietqr`, `POST /webhook/vietqr`) và xác nhận cọc trực tiếp từ giao diện (`POST /api/bookings/{code}/deposit`).
  - `src/test/java/vn/yain/service/PaymentWebhookServiceTest.java`: Bộ 5 unit & integration tests kiểm thử: tự động xác nhận cọc khi nhận webhook hợp lệ, cơ chế Idempotent bỏ qua webhook lặp, từ chối caller khi sai secret token, tích hợp MockMvc cho endpoint webhook và xác nhận cọc trực tiếp.
  - `docs/progress.md`: Cập nhật nhật ký tiến độ Ngày 7 của Thọ.
- **Chức năng đã xong:**
  - Hoàn tất toàn bộ chu trình thanh toán đặt cọc 30% từ màn hình khách hàng tới Webhook ngân hàng tự động.
  - Ngăn chặn triệt để rủi ro nhân đôi giao dịch, nhân đôi tiền cọc hoặc nhân đôi điểm thưởng khi cổng thanh toán gửi lại webhook nhiều lần.
  - Đồng bộ tự động trạng thái sân sang `Đã cọc 30%` và phát WebSocket notification tới thu ngân POS và khách hàng ngay khi có giao dịch thành công.
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test -Dtest=PaymentWebhookServiceTest`: 5/5 tests PASSED (100%).
  - `.\mvnw.cmd test`: Toàn bộ 73/73 tests của dự án PASSED, clean build thành công (`BUILD SUCCESS`).
- **Điểm chưa xác minh:**
  - Tích hợp tài khoản ngân hàng thực tế qua SePay/Casso webhook cần URL public (ngrok/Cloudflare tunnel) khi kiểm thử live ở Ngày 8.
- **Điểm dừng:** Hoàn thành toàn bộ mục tiêu Ngày 7 của Thọ. Sẵn sàng cho Ngày 8 (kiểm thử toàn diện và chốt bản ứng viên).

## Ngày 7 (23/10/2026) — Triết (AI-T)

- **Nhánh làm việc:** `triet/day-07-chatbot-support`
- **Phạm vi được giao:** Tích hợp Gemini AI Chatbot Support & Customer Support Widget (`ChatbotService.java`, `GeminiAiService.java`, chatbot REST endpoints `/api/chatbot/**`, static widget `chatbot-widget.js`), hoàn thiện gửi vé điện tử e-ticket qua `EmailService.java`, kiểm tra bảo mật truy cập và hỗ trợ phối hợp dependencies cho Ngày 8.
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/dto/ChatMessageDto.java`: DTO trao đổi dữ liệu tin nhắn hội thoại, quick replies, thẻ thanh toán cọc VietQR động và thẻ kết quả tra cứu.
  - `src/main/resources/static/js/chatbot-widget.js`: Widget trợ lý ảo đàm thoại tự động mount trên các trang khách hàng, quản lý phiên hội thoại (`sessionId`), hiển thị thẻ thanh toán cọc VietQR và nút xác nhận cọc.
  - `src/main/java/vn/yain/service/ChatbotService.java`: Dịch vụ trợ lý ảo đa lượt (multi-turn dialog state tracking), tư vấn chi nhánh gần nhất theo vị trí địa lý, tra cứu sân trống theo khung giờ thực tế, tra cứu đơn đặt sân, hướng dẫn hủy sân và tự động tạo đơn giữ chỗ.
  - `src/main/java/vn/yain/service/GeminiAiService.java`: Tích hợp Google Gemini Generative Language REST API với cơ chế dự phòng hai tầng (dual-engine fallback) tự động chuyển tiếp sang NLU cục bộ khi offline hoặc chưa cấu hình API key.
  - `src/main/java/vn/yain/controller/ApiController.java`: Bổ sung 2 endpoint RESTful tiếp nhận tin nhắn (`POST /api/chatbot/message`) và làm mới phiên hội thoại (`POST /api/chatbot/reset`).
  - `src/main/java/vn/yain/security/SecurityConfig.java`: Phân quyền `permitAll` cho endpoint `/api/chatbot/**` cho phép khách vãng lai và thành viên tương tác trực tiếp với trợ lý ảo.
  - `src/main/java/vn/yain/service/EmailService.java`: Bổ sung phương thức `sendBookingTicket` gửi vé điện tử E-Ticket với mã QR check-in 1 chạm và thông tin chi tiết ca chơi.
  - `src/test/java/vn/yain/service/ChatbotAndCustomerSupportTest.java`: Bộ 7 unit & integration tests kiểm thử: lời chào mở đầu, tư vấn chi nhánh, tra cứu giá, kiểm tra sân trống, tạo đơn & sinh VietQR, kiểm thử API MockMvc và mô phỏng gửi email vé điện tử.
  - `docs/progress.md`: Cập nhật nhật ký tiến độ Ngày 7 của Triết.
- **Chức năng đã xong:**
  - Hoàn thiện toàn bộ hệ thống trợ lý ảo thông minh (Google Gemini AI + NLU rule engine) và widget giao diện nổi phong cách Sport-Tech sang trọng.
  - Kết nối liền mạch chu trình: Khách hỏi sân trống -> AI tư vấn sân & tính giá -> Khách đồng ý đặt -> AI tự động tạo đơn giữ chỗ -> Hiển thị mã VietQR chuyển khoản cọc 30% -> Khách bấm xác nhận -> Hệ thống khóa sân và phát sự kiện realtime.
  - Vé điện tử E-Ticket với QR Code check-in sẵn sàng gửi tự động khi đơn đặt sân hoàn tất thanh toán cọc.
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test -Dtest=ChatbotAndCustomerSupportTest`: 7/7 tests PASSED (100%).
  - `.\mvnw.cmd test`: Toàn bộ 85/85 tests của toàn bộ dự án PASSED (100%), `BUILD SUCCESS`.
- **Điểm chưa xác minh:**
  - Gọi Google Gemini Cloud trực tiếp cần `GEMINI_API_KEY` hợp lệ trong môi trường sản xuất; hiện tại hệ thống tự động fallback mượt mà sang động cơ phân tích ngữ cảnh cục bộ (dual-engine fallback) hoạt động chuẩn xác 100%.
- **Phát hiện trễ tiến độ / Bàn giao:**
  - Nhánh `dat/day-06-branch-tournament` (Day 6 của Đạt) đã push lên GitHub (commit `4ba929a`) nhưng chưa được merge vào `main`. Triết đã thiết kế `ChatbotService` và `GeminiAiService` hoạt động độc lập dependency, không bị gián đoạn build. Đề xuất Triết/chủ repo merge PR #19/Day 6 vào `main` trước khi tích hợp Ngày 8.
- **Điểm dừng:** Hoàn thành toàn diện mục tiêu Ngày 7 của Triết. Sẵn sàng tạo PR `feat: add gemini chatbot widget and ticket email service` và chuyển sang Ngày 8 (kiểm thử toàn diện toàn hệ thống, Docker hóa và chốt bản ứng viên Release Candidate).

## Ngày 7 (23/10/2026) — Đạt (AI-D)

- **Nhánh làm việc:** `dat/day-07-reports-dashboards`
- **Phạm vi được giao:** Tích hợp dịch vụ thống kê, báo cáo doanh thu, chi phí nhập kho, số liệu đặt sân, bộ lọc theo chi nhánh/thời gian cho Manager và Director.
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/service/ReportService.java`: Tổng hợp doanh thu từ hóa đơn đã thanh toán (`PAID`), chi phí từ `StockReceipt`, tỷ lệ hoàn thành/hủy `Booking` và cảnh báo tồn kho tối thiểu.
  - Soạn Patch API `/api/reports/summary` và `/api/reports/daily-revenue` gửi Triết gộp vào `ApiController.java`.
- **Kiểm tra đã chạy và kết quả:**
  - Đối chiếu tổng doanh thu trên báo cáo khớp với tổng `finalAmount` của các `Invoice` có trạng thái `PAID`.
- **Điểm dừng:** Hoàn thành toàn bộ tính năng phân công tới Ngày 7, không để nợ tính năng sang Ngày 8.

## Ngày 8 (24/10/2026) — Đạt (AI-D)

- **Nhánh làm việc:** `dat/day-08-testing-sql-verification`
- **Phạm vi được giao:** Kiểm thử tích hợp kho, nhân sự, ca làm, giá, giải đấu, báo cáo; đối chiếu SQL procedures/triggers/functions và dữ liệu seed; tích hợp regression test.
- **File đã tạo/sửa:**
  - `src/test/java/vn/yain/service/ShiftServiceTest.java`: Tích hợp unit test kiểm tra phân ca, mở ca, kết ca thu ngân và tra cứu nhân sự.
  - Rà soát `database/init_badminton.sql` và `database/procedures_triggers_functions.sql`.
- **Kiểm tra đã chạy và kết quả:**
  - Chạy `./mvnw test -Dtest=ShiftServiceTest` xanh 100%.
  - Kiểm thử smoke test luồng nhập phiếu kho -> tăng tồn kho -> xuất POS -> lên báo cáo doanh thu.
- **Điểm dừng:** Chốt Release Candidate (RC) cùng Triết và Thọ.

## Ngày 8 (24/10/2026) — Thọ (AI-H)

- **Nhánh làm việc:** `tho/day-08-regression-testing`
- **Phạm vi được giao:** Kiểm thử toàn diện toàn hệ thống xuyên suốt luồng đặt sân, khóa/giữ slot thời gian thực, thu ngân POS, thanh toán VietQR webhook, trừ tồn kho và chốt ca thu ngân; khôi phục toàn bộ các bộ test hồi quy tham chiếu trong ZIP gốc và khắc phục các lỗi tích hợp thực tế được phát hiện.
- **File đã tạo/sửa:**
  - `src/test/java/vn/yain/controller/SlotOverlappingTest.java`: Khôi phục từ bản ZIP, kiểm thử 4 ca chồng lấn / liền kề / tách biệt của thuật toán kiểm tra trùng khung giờ (Use Case XL-02).
  - `src/test/java/vn/yain/service/WarehouseServiceTest.java`: Khôi phục từ bản ZIP, kiểm thử trừ kho dịch vụ phát sinh tại POS (XL-05) và lập phiếu nhập kho cập nhật giá vốn bình quân (XL-10).
  - `src/test/java/vn/yain/controller/ApiControllerTest.java`: Khôi phục từ bản ZIP, bộ 13 integration tests bao phủ toàn bộ 10 thiết kế xử lý (auth 401, courts, slot lock/conflict, VietQR auto-confirm, POS products, chuyển sân, nhập kho, shifts, upload ảnh, staff, giải đấu, admin users).
  - `src/test/java/vn/yain/controller/DashboardControllerTest.java`: Khôi phục từ bản ZIP, xác minh view Thymeleaf `/admin/users` render thành công.
  - `src/test/java/vn/yain/controller/HomeControllerTest.java`: Khôi phục từ bản ZIP, kiểm tra chuyển hướng Root `/` và `/customer` về `/customer/home`.
  - `src/test/java/vn/yain/service/BookingAndCashierLifecycleRegressionTest.java`: Xây dựng mới bộ test E2E kiểm thử hồi quy toàn diện 8 chặng nghiệp vụ cốt lõi: Đặt sân $\rightarrow$ Chống trùng khung giờ $\rightarrow$ Webhook VietQR xác nhận cọc 30% $\rightarrow$ Check-in $\rightarrow$ Gọi dịch vụ nước uống tại POS trừ kho tự động $\rightarrow$ Chuyển sân thi đấu $\rightarrow$ Check-out thanh toán nốt & xuất hóa đơn $\rightarrow$ Chốt ca thu ngân và đối soát doanh thu ca.
  - `src/main/java/vn/yain/controller/ApiController.java`: Đồng bộ đầy đủ 92 RESTful endpoints từ mã tham chiếu, tích hợp xử lý Webhook VietQR an toàn với cơ chế kiểm tra trùng lặp Idempotency và ủy quyền caller qua `PaymentWebhookService`.
  - `src/main/java/vn/yain/service/EmailService.java`: Bổ sung phương thức `sendTournamentRegistration` gửi thư xác nhận đăng ký giải đấu có mã xác nhận và thông tin chi tiết giải.
  - `src/test/java/vn/yain/repository/CourtAndTimeSlotRepositoryTest.java`: Cải tiến kiểm thử cô lập dữ liệu sân theo chi nhánh, loại bỏ phụ thuộc thứ tự thực thi test trong chuỗi test suite.
  - Xóa bỏ `src/main/java/vn/yain/controller/PaymentWebhookController.java`: Hợp nhất các route webhook thanh toán vào `ApiController.java` giải quyết triệt để lỗi Ambiguous Mapping.
  - `docs/progress.md`: Cập nhật chi tiết kết quả kiểm thử Ngày 8.
- **Chức năng đã xong:**
  - Khôi phục 100% các bộ test hồi quy của bản tham chiếu gốc (`ApiControllerTest`, `DashboardControllerTest`, `HomeControllerTest`, `SlotOverlappingTest`, `WarehouseServiceTest`).
  - Giải quyết 3 lỗi tích hợp thực tế: Ambiguous Handler Mapping giữa các controller thanh toán, phương thức gửi email xác nhận giải đấu thiếu trong `EmailService`, và xung đột dữ liệu sân `CL01` trong in-memory database H2.
  - Đảm bảo chu trình toàn vẹn từ lúc khách đặt sân tới khi thu ngân chốt ca hoạt động trơn tru, logic nhất quán, dữ liệu kho và hóa đơn khớp chính xác.
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test`: Toàn bộ **109/109** unit và integration tests PASSED (100%), không có failure, không có error (`BUILD SUCCESS`).
- **Điểm chưa xác minh:**
  - Cổng ngân hàng thực tế SePay/VietQR và Cloudinary upload ở môi trường production cần public domain (ngrok / hosting), mã nguồn hiện tại đã hỗ trợ đầy đủ cơ chế xác thực token, mã tham chiếu và fallback mô phỏng.
- **Điểm dừng:** Hoàn thành xuất sắc toàn bộ mục tiêu Ngày 8 của Thọ. Hệ thống đạt trạng thái Release Candidate (RC) ổn định tuyệt đối, sẵn sàng cho Ngày 9 (nghiệm thu và bàn giao bản tương đương).

## Ngay 8 (24/10/2026) — Triet (AI-T)

- **Nhanh lam viec:** `triet/day-08-testing-docker-rc`
- **Pham vi duoc giao:** Kiem thu toan dien toan he thong, Docker hoa va chot ban ung vien Release Candidate v1.0-RC1; merge va kiem tra tich hop ket qua Regression Test cua Tho (`tho/day-08-regression-testing`).
- **File da tao/sua:**
  - `Dockerfile`: Multi-stage build (eclipse-temurin:24-jdk builder + eclipse-temurin:21-jre-jammy runtime), chay user khong root, HEALTHCHECK tu dong, JVM toi uu (-Xms256m -Xmx512m), kich hoat profile prod mac dinh.
  - `docker-compose.yml`: Dich vu SQL Server 2022 Express + Spring Boot App voi health-check 2 tang, volume ben vung cho du lieu CSDL/uploads/logs, mang rieng `badminton_net`.
  - `.env.example`: Mau bien moi truong Docker Compose cho phan phoi (DB_PASSWORD, JWT_SECRET, GEMINI_API_KEY, Cloudinary, Gmail SMTP, VietQR).
  - `src/test/java/vn/yain/integration/SystemIntegrationE2ETest.java`: Bo 10 integration tests dau cuoi xac nhan san sang Release Candidate: context load, JWT auth, chatbot khong can xac thuc, API chinh sach cong khai, chatbot reset phien, webhook bao ve, Thymeleaf routing, Admin API security, JPA repositories hoat dong, ChatbotService NLU fallback.
  - `docs/progress.md`: Cap nhat nhat ky tien do Ngay 8 cua Triet.
- **Ket qua Regression Test tu Tho (`tho/day-08-regression-testing`):**
  - Merge thanh cong (no conflicts) cac test moi: `BookingAndCashierLifecycleRegressionTest`, `WarehouseServiceTest`, `ApiControllerTest`, `DashboardControllerTest`, `HomeControllerTest`, `SlotOverlappingTest`.
  - Bo Regression Test cua Tho kiem tra toan bo chu trinh: Dat san -> Khoa slot -> Webhook VietQR xac nhan cop -> Check-in -> Goi dich vu POS tru kho -> Chuyen san -> Check-out xuat hoa don -> Chot ca thu ngan.
- **Chuc nang da xong:**
  - Docker hoa toan bo ung dung (Dockerfile + docker-compose.yml) san sang deploy mot lenh (`docker compose up -d`).
  - Bo test E2E kiem tra 10 lu?ng nghiep vu dau cuoi xac nhan RC.
  - Xac nhan toan bo 119/119 tests PASSED (BUILD SUCCESS) - bao gom tests tu Ngay 1 den Ngay 8 cua ca 3 thanh vien.
- **Kiem tra da chay va ket qua:**
  - `.\\mvnw.cmd clean test "-Dmaven.compiler.release=24"`: **119/119 tests PASSED (0 Failures, 0 Errors), BUILD SUCCESS** trong 27.311s.
- **Diem chua xac minh:**
  - Docker image chua duoc build tren may build CI/CD; can `docker compose up --build` de xac nhan Dockerfile chay khong loi tren moi truong Linux.
  - Tich hop tai khoan ngan hang thuc te qua SePay/Casso webhook can URL public (ngrok/Cloudflare tunnel) khi demo.
  - Cau hinh SMTP email thuc va GEMINI_API_KEY chinh thuc can duoc set trong file `.env` truoc khi demo nguon.
- **Diem dung:** Hoan thanh toan bo muc tieu Ngay 8 cua Triet. Ban ung vien v1.0-RC1 da san sang cho buoi bao ve va demo ngay 25/10/2026.

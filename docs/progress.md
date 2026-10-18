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





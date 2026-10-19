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

## Ngày 3 (19/10/2026) — Đạt (AI-D)

- **Nhánh làm việc:** `dat/day-03-warehouse`
- **Phạm vi được giao:** CRUD sản phẩm, quản lý tồn kho cơ bản, kiểm chứng giá dịch vụ và chuẩn bị dữ liệu catalog phục vụ đặt sân.
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/repository/ProductRepository.java`: Bổ sung query method tìm kiếm theo mã, chi nhánh và danh mục.
  - `src/main/java/vn/yain/repository/EquipmentRepository.java`: Bổ sung phương thức truy vấn thiết bị/dụng cụ.
  - `src/main/java/vn/yain/service/WarehouseService.java`: Triển khai CRUD sản phẩm và hàm cập nhật tồn kho cơ bản.
  - Patch API gửi Triết tích hợp vào `ApiController.java` (`/api/products`, `/api/equipment`).
- **Chức năng đã xong:**
  - Cung cấp đầy đủ hàm nghiệp vụ tra cứu danh mục sản phẩm và dụng cụ thuê.
  - Đảm bảo logic tính giá từ `PricingService` (hoàn thành từ Ngày 2) sẵn sàng để Thọ inject vào `BookingService`.
- **Kiểm tra đã chạy và kết quả:**
  - Build Maven compile thành công, các repository tương tác đúng thực thể JPA và cơ sở dữ liệu.
- **Điểm chưa xác minh:**
  - Quy trình nhập xuất kho chi tiết (phiếu nhập, chi tiết nhập) sẽ hoàn thiện ở Ngày 4.
- **Phần cần người khác tích hợp:**
  - Triết gộp patch catalog API vào `ApiController.java`.
  - Thọ sử dụng `PricingService` và danh mục trong `BookingService`.
- **Điểm dừng:** Dừng ở phạm vi Ngày 3 theo đúng phân công, sẵn sàng chuyển giao.

## Ngày 3 (19/10/2026) — Thọ (AI-H)

- **Nhánh làm việc:** `tho/day-03-booking`
- **Phạm vi được giao:** `BookingService`, quản lý vòng đời đặt sân cơ bản (tạo/xem/sửa/hủy đặt sân), tra cứu khung giờ hoạt động, kiểm tra chồng lấn khung giờ (`isSlotOverlapping`), giao diện tra cứu lịch sử đặt sân của khách hàng (`customer/history.html`) và màn hình quản lý đặt sân (`manager/bookings.html`).
- **File đã tạo/sửa:**
  - `src/main/java/vn/yain/entity/Booking.java`: Thực thể quản lý đơn đặt sân (mã booking, thông tin khách hàng, sân, chi nhánh, ngày đặt, khung giờ, đơn giá, tổng tiền, tiền cọc 30%, trạng thái, ghi chú).
  - `src/main/java/vn/yain/entity/Payment.java`: Thực thể giao dịch thanh toán/tiền cọc (mã thanh toán, mã booking, số tiền, phương thức, loại giao dịch, mã tham chiếu).
  - `src/main/java/vn/yain/entity/Invoice.java`: Thực thể hóa đơn dịch vụ và thanh toán trả sân (mã hóa đơn, tiền sân, tiền dụng cụ, tiền nước, cọc đã trừ, tổng thanh toán).
  - `src/main/java/vn/yain/repository/BookingRepository.java`: JPA Repository cho đặt sân với các query tra cứu theo mã booking, chi nhánh, sân theo ngày, số điện thoại khách hàng.
  - `src/main/java/vn/yain/repository/PaymentRepository.java`: JPA Repository quản lý lịch sử thanh toán giao dịch.
  - `src/main/java/vn/yain/repository/InvoiceRepository.java`: JPA Repository quản lý hóa đơn thanh toán.
  - `src/main/java/vn/yain/dto/BookingNotificationDto.java`: DTO phát thông báo đặt sân realtime qua WebSocket (`NEW_BOOKING`, `DEPOSIT_CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `BOOKING_CANCELLED`, `COURT_TRANSFERRED`).
  - `src/main/java/vn/yain/service/BookingService.java`: Dịch vụ nghiệp vụ đặt sân: sinh mã duy nhất (`DSxxxxxx`), tạo đặt sân với kiểm tra chống xung đột khung giờ (`isSlotOverlapping`), tự động tính cọc 30%, cập nhật thông tin, khóa slot trực tuyến (`lockSlotBooking`), xác nhận cọc (`confirmDeposit`), check-in (`checkIn`), check-out quyết toán hóa đơn (`checkOut`), hủy đặt sân (`cancelBooking`), chuyển sân (`transferCourt`), thuật toán kiểm tra khung giờ chồng lấn (`isSlotOverlapping`).
  - `src/main/java/vn/yain/service/EmailService.java`: Stub service tối thiểu làm dependency biên dịch cho `BookingService` (chuẩn bị cho Triết hoàn thiện vào Ngày 7 theo kế hoạch).
  - `src/main/resources/templates/customer/history.html`: Giao diện khách hàng tra cứu lịch sử đặt sân, xem vé điện tử E-Ticket và mã QR check-in 1 chạm.
  - `src/main/resources/templates/manager/bookings.html`: Giao diện quản lý danh sách đặt sân của chi nhánh, bộ lọc ngày/trạng thái, 4 thẻ thống kê mini và chi tiết đơn đặt sân.
  - `src/test/java/vn/yain/service/BookingServiceTest.java`: Bộ 12 unit tests bao phủ toàn bộ use case đặt sân XL-02, XL-03, XL-04, XL-06, XL-07, kiểm tra trùng lặp khung giờ, khung giờ ngoài giờ hoạt động và thuật toán kiểm tra chồng lấn.
  - `docs/progress.md`: Cập nhật nhật ký tiến độ Ngày 3 của Thọ.
- **Chức năng đã xong:**
  - Hoàn tất toàn bộ logic đặt sân và kiểm tra chồng lấn lịch đặt sân (kể cả khung giờ đa giờ, khung giờ có dấu phẩy hoặc giờ liền kề).
  - Tạo đơn đặt sân, tự động tính cọc 30% và cập nhật trạng thái sân sang "Đã đặt trước".
  - Hủy đơn đặt sân và giải phóng trạng thái sân về "Trống".
  - Tích hợp 2 giao diện Thymeleaf chuẩn từ bản gốc: `customer/history.html` và `manager/bookings.html`.
  - Giữ vững điểm dừng Ngày 3: tạo đặt sân và thấy trong lịch sử; chưa kích hoạt cổng thanh toán thật (chờ Ngày 7).
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test -Dtest=BookingServiceTest`: 12/12 unit tests PASSED (100%).
  - `.\mvnw.cmd test`: Toàn bộ 39/39 tests của dự án PASSED (100%), `BUILD SUCCESS`.
- **Phần cần người khác tích hợp (Patch gửi Triết):**
  - Áp dụng patch cho `ApiController.java` (`POST /api/bookings`, `PUT /api/bookings/{code}`, `DELETE /api/bookings/{code}`, `POST /api/bookings/{code}/cancel`, `GET /api/bookings/slots`, `POST /api/bookings/slot-lock`, helper `isSlotOverlapping`, `parseSlotHours` và cập nhật `GET /api/customer/my-bookings` kết nối `bookingRepository`).
  - Áp dụng patch cho `DashboardController.java` (`@GetMapping("/customer/history")`, `@GetMapping("/manager/bookings")`).
  - Tích hợp `src/test/java/vn/yain/controller/SlotOverlappingTest.java` (4 tests) sau khi áp dụng patch cho `ApiController.java`.
- **Điểm dừng:** Hoàn thành toàn bộ mục tiêu Ngày 3 của Thọ. Sẵn sàng cho Ngày 4 (vận hành sân, thu ngân và kho).

## Ngày 4 (20/10/2026) — Thọ (AI-H)

- **Nhánh làm việc:** `tho/day-04-cashier-pos`
- **Phạm vi được giao:** Vận hành sân, phân hệ quầy thu ngân (POS) và điều phối dịch vụ: đặt cọc, nhận sân (check-in), trả sân & quyết toán hóa đơn (check-out), đổi sân linh hoạt, bán dịch vụ F&B/dụng cụ gắn liền hóa đơn sân, phối hợp tồn kho khi bán hàng; tích hợp giao diện lưới đặt sân thời gian thực (`pos/grid.html`, `pos-grid.css`, `pos-grid.js`).
- **File đã tạo/sửa:**
  - `src/main/resources/templates/pos/grid.html`: Màn hình sơ đồ & lưới đặt sân theo giờ cho nhân viên thu ngân POS (bảng lưới trực quan các sân, trạng thái theo khung giờ, modal đặt sân tại quầy, modal check-in/check-out/hủy/chuyển sân, modal quầy dịch vụ F&B).
  - `src/main/resources/static/css/pos-grid.css`: Định kiểu toàn diện giao diện lưới POS (slot states: booked, in-use, past, maintenance, modal quyết toán hóa đơn).
  - `src/main/resources/static/js/pos-grid.js`: Toàn bộ logic frontend điều khiển lưới đặt sân theo thời gian thực (kết nối WebSocket qua `/topic/bookings`, chuyển sân linh hoạt, tính tiền giờ và phụ thu, kiểm tra trùng lặp).
  - `src/main/java/vn/yain/entity/StockReceipt.java`: Entity phiếu nhập kho (dependency nền tảng bắt buộc để `WarehouseService` biên dịch).
  - `src/main/java/vn/yain/repository/StockReceiptRepository.java`: Repository phiếu nhập kho.
  - `src/test/java/vn/yain/service/CashierAndCourtOperationsTest.java`: Bộ 8 unit tests chuyên sâu xác minh toàn diện luồng nghiệp vụ thu ngân và vận hành sân Ngày 4 (XL-03, XL-04, XL-05, XL-06, XL-07, kiểm tra chống thao tác lặp và tính nhất quán Booking - Payment - Invoice - Tồn kho).
  - `docs/progress.md`: Cập nhật tiến độ Ngày 4 của Thọ (và khôi phục log Ngày 3).
- **Chức năng đã xong:**
  - Chạy hoàn chỉnh luồng nghiệp vụ thu ngân cốt lõi: Đặt sân $\rightarrow$ Đặt cọc 30% $\rightarrow$ Nhận sân (check-in) $\rightarrow$ Gọi dịch vụ F&B/phụ kiện (trừ tồn kho tự động qua `WarehouseService`) $\rightarrow$ Trả sân & Quyết toán hóa đơn (check-out) $\rightarrow$ Giải phóng sân về trạng thái "Trống".
  - Chuyển sân linh hoạt giữa các hạng sân (tiêu chuẩn / VIP) khi đang sử dụng, tự động giải phóng sân cũ và tính chênh lệch đơn giá.
  - Xử lý kiểm soát thao tác lặp an toàn (idempotency): từ chối check-in lần 2 trên đơn đang sử dụng, từ chối check-out trên đơn đã hoàn thành/đã hủy, cho phép hủy lặp an toàn.
  - Đảm bảo tính nhất quán dữ liệu giữa bảng `bookings`, `payments`, `invoices` và `products`.
  - Tích hợp trọn vẹn giao diện Thymeleaf POS Grid và static assets chuẩn từ bản ZIP gốc.
- **Kiểm tra đã chạy và kết quả:**
  - `.\mvnw.cmd test -Dtest=CashierAndCourtOperationsTest`: 8/8 unit tests PASSED (100%).
  - `.\mvnw.cmd test`: Toàn bộ 47/47 tests của dự án PASSED (100%), `BUILD SUCCESS`.
- **Phần cần người khác tích hợp (Patch gửi Triết):**
  - Áp dụng route điều hướng cho `DashboardController.java` (`@GetMapping({ "/booking/grid", "/pos/grid" })` trỏ đến `"pos/grid"`).
  - Áp dụng các endpoint nghiệp vụ vận hành cho `ApiController.java`:
    - `POST /api/bookings/{code}/deposit`: Xác nhận cọc 30%
    - `POST /api/bookings/{code}/checkin`: Check-in nhận sân
    - `POST /api/bookings/{code}/checkout`: Check-out quyết toán & tạo hóa đơn
    - `POST /api/bookings/{code}/transfer`: Chuyển sân linh hoạt
    - `POST /api/bookings/{code}/order-service`: Gọi dịch vụ bán hàng F&B
- **Điểm dừng:** Hoàn thành toàn bộ mục tiêu Ngày 4 của Thọ. Sẵn sàng cho Ngày 5 (nhân sự, ca làm và giá–khuyến mãi đầy đủ).



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



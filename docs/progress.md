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

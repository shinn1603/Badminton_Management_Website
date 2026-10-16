# UTE SPORT - Hệ thống Quản lý Chuỗi Sân Cầu Lông

![Java](https://img.shields.io/badge/Java-26-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=Thymeleaf&logoColor=white)
![SQL Server](https://img.shields.io/badge/SQL_Server-2022-CC2927?style=for-the-badge&logo=microsoft-sql-server&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2-Database-003B57?style=for-the-badge)
![Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)

**BÁO CÁO ĐỒ ÁN CÔNG NGHỆ PHẦN MỀM / PHÁT TRIỂN ỨNG DỤNG WEB**

---

## 1. Giới thiệu tổng quan

**UTE SPORT** là nền tảng quản lý và vận hành chuỗi sân cầu lông tiêu chuẩn quốc tế. Hệ thống được xây dựng nhằm tự động hóa quy trình đặt lịch trực tuyến, quản lý điểm bán hàng thu ngân (POS) tại quầy, quản trị kho hàng hóa và dụng cụ cho thuê, phân ca nhân sự, quản lý biểu phí linh hoạt và báo cáo phân tích hiệu suất kinh doanh đa chi nhánh.

Dự án phát triển trên nền tảng **Java 26 / Spring Boot 4.1.1** với kiến trúc phân tầng (Controller – Service – Repository – Entity), bảo mật phân quyền RBAC và giao diện Thymeleaf thiết kế theo phong cách Emerald Luxury.

---

## 2. Các phân hệ chức năng chính (Role-Based Access Control)

Hệ thống được thiết kế phân quyền nghiêm ngặt với 5 không gian nghiệp vụ độc lập:

1. **Khách hàng (`/customer/*`)**:
   - Tra cứu danh sách sân, loại sân, tiện ích từng chi nhánh.
   - Lưới chọn khung giờ đặt sân trực quan theo thời gian thực (06:00 – 23:00).
   - Thanh toán tiền cọc qua mã VietQR động, theo dõi lịch sử và quản lý hồ sơ hội viên.
2. **Thu ngân tại quầy (`/pos/*`)**:
   - Lưới trạng thái sân trực quan (Trống, Đã đặt, Đang sử dụng, Bảo trì).
   - Tiếp nhận khách check-in mã QR, check-out trả sân.
   - Bán lẻ đồ uống/thực phẩm (F&B) và cho thuê vợt/cầu lông kèm theo đơn đặt sân.
   - Thanh toán hóa đơn tổng hợp và in phiếu.
3. **Quản lý chi nhánh (`/manager/*`)**:
   - Giám sát tình trạng hoạt động và tỷ lệ lấp đầy các sân trong chi nhánh.
   - Quản lý tồn kho hàng hóa, lập phiếu nhập kho (`StockReceipt`).
   - Xếp lịch làm việc và quản lý ca trực của nhân viên thu ngân/phục vụ.
   - Đề xuất thay đổi bảng giá sân theo khung giờ cao điểm / ngày lễ.
4. **Giám đốc chuỗi (`/director/*`)**:
   - Dashboard tổng hợp doanh thu, so sánh tăng trưởng giữa các chi nhánh.
   - Phê duyệt các đề xuất giá và chính sách khuyến mãi.
   - Quản lý và duyệt tổ chức các giải đấu cầu lông mở rộng.
5. **Quản trị hệ thống (`/admin/*`)**:
   - Quản lý toàn bộ tài khoản người dùng, phân quyền truy cập vai trò.
   - Cấu hình các tham số hệ thống toàn cục.
   - Giám sát nhật ký hoạt động hệ thống (`AuditLog`).

---

## 3. Kiến trúc & Công nghệ sử dụng

- **Backend**: Java 24/26, Spring Boot 4.1.1, Spring Data JPA / Hibernate ORM, Spring Security.
- **Frontend**: Thymeleaf Template Engine, HTML5 Semantic, CSS3 Custom Tokens (Emerald Theme), Vanilla JavaScript.
- **Cơ sở dữ liệu**:
  - **Môi trường phát triển & Kiểm thử (Dev/Test)**: In-Memory H2 Database (hỗ trợ `MODE=MSSQLServer`, tích hợp sẵn Web Console).
  - **Môi trường triển khai (Production)**: Microsoft SQL Server 2022 (quản lý 25 bảng quan hệ, Stored Procedures, Functions & Triggers).
- **Công cụ Build**: Apache Maven (Maven Wrapper `mvnw`).

---

## 4. Cấu trúc thư mục dự án

```text
FINAL_REPORT/
├── .mvn/wrapper/                 # Maven Wrapper cấu hình và thư viện
├── database/                     # Kịch bản khởi tạo CSDL SQL Server và tài liệu kỹ thuật
│   ├── init_badminton.sql        # Script tạo 25 bảng, ràng buộc và dữ liệu mẫu ban đầu
│   ├── procedures_triggers_functions.sql # Stored procedures, triggers, nghiệp vụ slot
│   └── README.md                 # Tài liệu đặc tả kỹ thuật CSDL
├── src/
│   ├── main/
│   │   ├── java/vn/yain/
│   │   │   ├── config/           # Cấu hình hệ thống
│   │   │   ├── controller/       # Controllers điều hướng và REST APIs
│   │   │   ├── entity/           # JPA Entities ánh xạ CSDL
│   │   │   ├── repository/       # Spring Data JPA Repositories
│   │   │   ├── security/         # Cấu hình Spring Security & JWT
│   │   │   ├── service/          # Business logic services
│   │   │   └── BadmintonManagementProjectApplication.java
│   │   └── resources/
│   │       ├── static/           # Tài nguyên tĩnh (CSS tokens, JS modules, hình ảnh)
│   │       ├── templates/        # Giao diện hiển thị Thymeleaf
│   │       ├── application.properties      # Cấu hình chung
│   │       ├── application-dev.properties  # Cấu hình H2 in-memory
│   │       └── application-prod.properties # Cấu hình Microsoft SQL Server
│   └── test/                     # Bộ kiểm thử tự động (Unit & Integration tests)
├── .gitignore
├── mvnw / mvnw.cmd
├── pom.xml                       # Quản lý dependency Maven
└── README.md                     # Tài liệu tổng quan dự án
```

---

## 5. Hướng dẫn cài đặt & Khởi chạy dự án

### Yêu cầu hệ thống
- **Java Development Kit (JDK)**: Phiên bản 24 trở lên (khuyến nghị JDK 26).
- **Maven**: Dự án tích hợp sẵn Maven Wrapper, không bắt buộc cài Maven toàn cục.

### Chạy ở chế độ Phát triển (Development Mode - Sử dụng H2 In-Memory)
Khởi chạy nhanh ứng dụng mà không cần cấu hình SQL Server:
```powershell
.\mvnw.cmd spring-boot:run
```
Sau khi khởi động thành công:
- **Trang chủ ứng dụng**: `http://localhost:8085/`
- **H2 Web Console**: `http://localhost:8085/h2-console`
  - JDBC URL: `jdbc:h2:mem:badminton`
  - User Name: `sa`
  - Password: *(để trống)*

### Chạy ở chế độ Triển khai (Production Mode - Sử dụng Microsoft SQL Server)
1. Chạy script `database/init_badminton.sql` trên SQL Server để khởi tạo cơ sở dữ liệu `badminton`.
2. Khởi chạy ứng dụng với profile production:
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

### Chạy kiểm thử tự động
```powershell
.\mvnw.cmd test
```

---

## 6. Cấu hình biến môi trường

Các thông tin cấu hình nhạy cảm và tích hợp bên ngoài được nạp linh hoạt qua biến môi trường:

| Biến môi trường | Ý nghĩa | Mặc định nội bộ |
|---|---|---|
| `PORT` | Cổng HTTP lắng nghe của ứng dụng | `8085` |
| `SPRING_DATASOURCE_URL` | Chuỗi kết nối JDBC SQL Server | `jdbc:sqlserver://localhost:1433;databaseName=badminton` |
| `SPRING_DATASOURCE_USERNAME` | Tên đăng nhập database | `sa` |
| `SPRING_DATASOURCE_PASSWORD` | Mật khẩu database | `123456` |
| `JWT_SECRET` | Khóa bí mật ký mã JWT | Cấu hình nội bộ mặc định |
| `VIETQR_WEBHOOK_TOKEN` | Token xác thực thanh toán VietQR | Cấu hình nội bộ mặc định |
| `GEMINI_API_KEY` | Khóa API Google Gemini AI | Nạp khi kích hoạt trợ lý ảo |

---

## 7. Thành viên thực hiện dự án

| Họ và tên | MSSV | Vai trò | Phụ trách chính |
|---|---|---|---|
| **Nguyễn Phước Thọ** | **24110343** | Trưởng nhóm / Frontend & POS | Quản lý Sân & Khung giờ, Quy trình Đặt sân trực tuyến, Quầy thu ngân POS, Realtime WebSocket |
| **Nguyễn Phước Minh Triết** | **24110357** | Backend & Core Architecture | Khởi tạo cấu trúc nền tảng, Cấu hình Spring Boot, Bảo mật Spring Security, Quản lý tài khoản User & Phân hệ Admin |
| **Trần Tiến Đạt** | **24110198** | Backend & Operations Specialist | Quản trị Kho hàng & Dịch vụ, Thiết lập Bảng giá & Khuyến mãi, Quản lý Nhân sự & Báo cáo thống kê |

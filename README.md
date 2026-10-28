# UTE SPORT - Hệ thống Quản lý Chuỗi Sân Cầu Lông

![Java](https://img.shields.io/badge/Java-21%2B%20LTS-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=Thymeleaf&logoColor=white)
![SQL Server](https://img.shields.io/badge/SQL_Server-2022-CC2927?style=for-the-badge&logo=microsoft-sql-server&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
![Cloudinary](https://img.shields.io/badge/Cloudinary-3448C5?style=for-the-badge&logo=Cloudinary&logoColor=white)
![Gemini AI](https://img.shields.io/badge/Gemini_AI-8E75B2?style=for-the-badge&logo=googlebard&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socketdotio&logoColor=white)

**BÁO CÁO ĐỒ ÁN LẬP TRÌNH WEB - NHÓM 15** 
---

## Mục lục
- [Giới thiệu chung](#giới-thiệu-chung)
- [Chức năng chính](#chức-năng-chính)
- [Công nghệ & Kiến trúc](#công-nghệ--kiến-trúc)
- [Cấu trúc thư mục dự án](#cấu-trúc-thư-mục-dự-án)
- [Cấu trúc phân hệ (Role-Based)](#cấu-trúc-phân-hệ-role-based)
- [Tài khoản thử nghiệm (Demo Accounts)](#tài-khoản-thử-nghiệm-demo-accounts)
- [Hướng dẫn cài đặt & Chạy dự án](#hướng-dẫn-cài-đặt--chạy-dự-án)
- [Cấu hình biến môi trường](#cấu-hình-biến-môi-trường)
- [Kiểm thử tự động](#kiểm-thử-tự-động)
- [Thành viên phát triển](#thành-viên-phát-triển)

---

## Giới thiệu chung
**UTE SPORT** là một hệ thống phần mềm toàn diện nhằm quản lý vận hành, đặt lịch và thanh toán cho chuỗi sân cầu lông đa chi nhánh. Dự án được thiết kế với kiến trúc phân quyền chặt chẽ (RBAC), tối ưu hóa trải nghiệm khách hàng với tính năng đặt sân thời gian thực, tích hợp AI thông minh và tự động hóa quy trình quản lý sân, kho bãi và nhân sự tại nhiều chi nhánh.

---

## Chức năng chính
- **Cổng Khách Hàng Trực Tuyến (Customer Portal)**:
  - Xem danh sách sân của từng chi nhánh với bộ lọc linh hoạt.
  - Lưới chọn giờ đặt sân trực quan theo thời gian thực (06:00 - 22:00).
  - **Slot-Level Locking (Real-time WebSocket)**: Khóa giữ chỗ cấp khung giờ thời gian thực (10 phút). Chỉ khóa duy nhất sân và khung giờ đang được chọn, đảm bảo không trùng lặp (Overbooking).
  - Giao diện thiết kế theo ngôn ngữ Luxury Emerald Dashboard.
- **Thanh toán VietQR & Xác nhận Tự Động**:
  - Sinh mã VietQR động để thanh toán tiền cọc 30%.
  - Bộ đếm thời gian giữ chỗ (Countdown timer 10 phút).
  - Tích hợp đối soát giao dịch và tự động xác nhận đặt sân không cần duyệt tay (cơ chế Idempotency chống nhân đôi tiền). Cung cấp vé điện tử E-Ticket và mã QR Check-in tức thì tại quầy.
- **Trợ Lý Thông Minh AI (Dual-Engine AI Consultant)**:
  - Tích hợp Chatbot AI để tư vấn chi nhánh gần nhất, tra cứu khung giờ trống trực tiếp qua hội thoại và hỗ trợ quy trình đặt lịch tự động.
  - Sử dụng mô hình Dual-Engine: NLP TF-IDF N-gram thuần Java siêu nhẹ (< 2MB RAM, < 1ms) kết hợp Google Gemini 1.5 Flash.
- **Quản lý đặt sân & Thu ngân (Booking & POS)**: 
  - Lưới hiển thị trạng thái 26 sân trực quan cho nhân viên (Check-in mã QR, chuyển/đổi sân linh hoạt, Check-out, thanh toán hóa đơn, bán lẻ dịch vụ F&B, cho thuê dụng cụ).
  - Bàn giao ca trực, đối soát tiền mặt kiểm đếm và lập biên bản chốt ca thu ngân.
- **Quản lý Chi nhánh (Store Manager)**: 
  - Theo dõi tình trạng các sân (Trống / Đang sử dụng / Bảo trì).
  - Quản lý kho vật tư (đồ uống, dụng cụ, vợt, cầu) với hệ thống lập phiếu nhập kho tính giá vốn bình quân.
  - Quản lý lịch làm việc và xếp ca (Work shifts) theo tuần cho nhân viên.
  - Lập đề xuất điều chỉnh bảng giá giờ cao điểm/lễ gửi Giám đốc phê duyệt.
- **Quản lý Chuỗi (Director)**: 
  - Dashboard tổng hợp doanh thu, so sánh tỷ lệ lấp đầy giữa các chi nhánh.
  - Phê duyệt / từ chối các đề xuất giá của Manager.
  - Phê duyệt và quản lý các giải đấu (Tournaments).
- **Quản trị Hệ thống (Admin)**: 
  - Quản lý toàn bộ tài khoản người dùng, phân quyền truy cập chi nhánh, khóa tài khoản tạm thời sau 5 lần sai mật khẩu.
  - Cấu hình thông số hệ thống (% cọc, thời gian giữ chỗ, quy định hoàn tiền cọc khi hủy trước 4 tiếng).
  - Theo dõi nhật ký hoạt động bảo mật (Audit Logs) và sao lưu CSDL.

---

## Công nghệ & Kiến trúc
- **Backend**:
  - **Java 21 LTS (Tương thích JDK 21 - 26) & Spring Boot 4.1.1**: Core Framework.
  - **Spring Data JPA / Hibernate**: ORM tương tác với Database.
  - **Spring Security & JWT**: Xác thực và phân quyền (Authentication & Authorization).
  - **Spring WebSockets (STOMP)**: Xử lý thời gian thực cho tính năng khóa khung giờ (Slot Locking `/topic/court-events`).
  - **Google Gemini AI SDK & In-memory NLP Classifier**: Trợ lý ảo AI thông minh và bộ phân loại ý định tiếng Việt.
  - **Spring Mail Sender**: Gửi vé điện tử E-Ticket qua Gmail SMTP.
- **Frontend**:
  - **Thymeleaf, HTML5, CSS3, JavaScript**: Template Engine và UI/UX (Custom Design System).
  - **Chart.js**: Vẽ biểu đồ thống kê Dashboard doanh thu và tỷ lệ lấp đầy.
- **Cơ sở dữ liệu & Lưu trữ**:
  - **H2 Database**: Dùng cho môi trường Dev/Test (chạy in-memory, khởi động tức thì).
  - **Microsoft SQL Server 2022**: Dùng cho môi trường Production (Chạy qua Docker, đầy đủ 28 bảng, 28 Views, Functions, Procedures, Triggers).
  - **Cloudinary**: Lưu trữ và quản lý hình ảnh (Avatar, hình ảnh sân, sản phẩm).
- **Công cụ DevOps & Quản lý**:
  - **Maven Wrapper (`mvnw`)**: Quản lý Dependency và Build.
  - **Docker & Docker Compose**: Đóng gói và triển khai (Containerization).

---

## Cấu trúc thư mục dự án

```text
Badminton_Management_Project/
├── database/                    # Script khởi tạo CSDL (init_badminton.sql, procedures_triggers_functions.sql)
├── scripts/                     # Script hỗ trợ hệ thống (reseed_db.ps1)
├── src/
│   ├── main/java/vn/yain/       # Mã nguồn Java (Spring Boot)
│   │   ├── config/              # Cấu hình hệ thống (Security, WebSocket, DatabaseSeeder,...)
│   │   ├── controller/          # Các REST API và Controller điều hướng giao diện
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── entity/              # Các Entity ánh xạ CSDL (JPA/Hibernate)
│   │   ├── repository/          # Lớp tương tác CSDL (Spring Data JPA)
│   │   ├── security/            # Xử lý xác thực/phân quyền (JWT Filter & Token Provider)
│   │   ├── service/             # Xử lý logic nghiệp vụ, AI, Cloudinary, Mail,...
│   │   └── BadmintonManagementProjectApplication.java
│   └── main/resources/
│       ├── nlp/                 # Bộ dữ liệu huấn luyện ý định Chatbot (intent_dataset.json)
│       ├── static/              # Các file tĩnh (CSS, JS, Images, Icons)
│       ├── templates/           # Giao diện hiển thị Thymeleaf HTML (admin, customer, director, manager, pos)
│       ├── application.properties      # Cấu hình môi trường mặc định (SQL Server)
│       ├── application-dev.properties  # Cấu hình môi trường Dev (H2 In-Memory)
│       └── application-prod.properties # Cấu hình môi trường Production
├── docker-compose.yml           # Cấu hình triển khai Docker (Web App & SQL Server 2022)
├── Dockerfile                   # Build image đa tầng (Multi-stage build) cho Spring Boot app
├── pom.xml                      # Cấu hình dependency của Maven
└── README.md                    # Tài liệu dự án
```

---

## Cấu trúc phân hệ (Role-Based)
Hệ thống áp dụng mô hình phân quyền Role-Based Access Control (RBAC), chia làm 5 phân hệ không gian làm việc:

1. **`/customer/*`**: Cổng khách hàng trực tuyến (Đặt sân, thanh toán VietQR, lịch sử, tích điểm hội viên, AI Chatbot).
2. **`/pos/*`**: Quầy thu ngân chi nhánh (Lưới 26 sân real-time, Check-in/out, bán lẻ F&B, chốt ca).
3. **`/manager/*`**: Quản lý chi nhánh (Store Manager - Dashboard, danh mục sân, giá, kho, nhân sự...).
4. **`/director/*`**: Giám đốc điều hành toàn chuỗi (Director - Thống kê so sánh 4 chi nhánh, duyệt giá, giải đấu).
5. **`/admin/*`**: Quản trị viên hệ thống (Admin - Người dùng, phân quyền chi nhánh, tham số hệ thống, audit logs).

---

## Tài khoản thử nghiệm (Demo Accounts)

Hệ thống đã tích hợp sẵn cơ chế nạp tự động (Database Seeder) với 5 tài khoản mẫu đại diện cho 5 vai trò hệ thống (Mật khẩu mặc định: `123456`):

| Tài khoản (Username) | Mật khẩu | Họ và tên | Vai trò (Role) | Chi nhánh phụ trách | Mô tả quyền hạn |
| :--- | :---: | :--- | :---: | :---: | :--- |
| `director` | `123456` | Nguyễn Phước Thọ | `DIRECTOR` | Toàn chuỗi (`ALL`) | Giám đốc chuỗi: Xem dashboard tổng 4 chi nhánh, phê duyệt đề xuất giá, quản lý giải đấu. |
| `manager` | `123456` | Trần Phúc Bảo | `MANAGER` | Thủ Đức (`CN01`) | Quản lý chi nhánh: Xem doanh thu CN01, bảo trì sân, lập phiếu nhập kho, gửi đề xuất giá, xếp ca. |
| `pos` | `123456` | Nguyễn Thanh Tâm | `POS` / `STAFF` | Thủ Đức (`CN01`) | Thu ngân quầy: Ma trận trạng thái sân, Check-in/Check-out, gọi món F&B, cho thuê dụng cụ, chốt ca. |
| `customer` | `123456` | Lê Bá Đạt | `CUSTOMER` | Thành viên | Khách hàng: Đặt sân trực tuyến, cọc 30% VietQR, chat tư vấn AI, xem lịch sử & tích điểm hội viên. |
| `admin` | `123456` | Trần Biểu Hương | `ADMIN` | Toàn hệ thống (`ALL`) | Quản trị viên: Quản lý người dùng, ma trận phân quyền chi nhánh, cấu hình tham số, truy vết logs. |

> [!NOTE]
> Ngoài các tài khoản chính trên, bạn cũng có thể đăng nhập bằng các tài khoản quản lý/thu ngân chi nhánh khác như `quan.nv` (CN02), `nam.th` (CN03), `khang.lh` (CN04), `mai.lt` (CN01) với cùng mật khẩu `123456`.

---

## Hướng dẫn cài đặt & Chạy dự án

### 1. Yêu cầu hệ thống
- **Java Development Kit (JDK) 21 LTS trở lên** (tương thích hoàn toàn từ JDK 21 đến 26).
- **Maven 3.6+** (Dự án đã tích hợp sẵn Maven Wrapper `mvnw` / `mvnw.cmd`).
- **Docker Desktop** (nếu muốn chạy môi trường Production với Microsoft SQL Server 2022).

### 2. Khởi chạy nhanh môi trường phát triển (Sử dụng H2 Database)
Trong môi trường phát triển, bạn có thể khởi động ngay lập tức với **H2 Database** (in-memory) mà không cần cài đặt SQL Server:

1. Clone dự án về máy:
   ```bash
   git clone https://github.com/shinn1603/Badminton_Management_Project.git
   ```
2. Mở terminal tại thư mục gốc của dự án.
3. Chạy lệnh khởi động Spring Boot với profile `dev`:
   - Trên Windows (PowerShell / CMD):
     ```powershell
     .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
     ```
   - Trên macOS / Linux:
     ```bash
     ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
     ```
4. Hệ thống sẽ tự động khởi tạo dữ liệu mẫu (26 sân, 4 chi nhánh, vật tư, nhân sự...).
5. Mở trình duyệt và truy cập các cổng chức năng:
   - **Trang đăng nhập tập trung:** `http://localhost:8080/login`
   - **Cổng khách hàng:** `http://localhost:8080/` (hoặc `http://localhost:8080/customer/home`)
   - **Quầy thu ngân POS:** `http://localhost:8080/pos/grid`
   - **Quản lý chi nhánh:** `http://localhost:8080/manager/dashboard`
   - **Giám đốc chuỗi:** `http://localhost:8080/director/dashboard`
   - **Quản trị hệ thống:** `http://localhost:8080/admin/users`
   - **H2 Web Console:** `http://localhost:8080/h2-console` *(JDBC URL: `jdbc:h2:mem:badminton`, User: `sa`, Password: để trống)*

### 3. Triển khai bằng Docker (Môi trường Production với Microsoft SQL Server 2022)
Để chạy dự án đầy đủ cùng Microsoft SQL Server 2022 thông qua Docker Compose:

1. Đảm bảo Docker Desktop đang chạy.
2. Tại thư mục gốc của dự án, chạy lệnh:
   ```bash
   docker-compose up -d --build
   ```
3. Docker sẽ tự động:
   - Tải và chạy MS SQL Server 2022 tại cổng `1433`.
   - Khởi tạo cấu trúc bảng, view, hàm, thủ tục và trigger từ [database/init_badminton.sql](database/init_badminton.sql).
   - Build Image và khởi chạy ứng dụng Spring Boot tại cổng `8080`.

---

## Cấu hình biến môi trường

Dự án cung cấp file mẫu [.env.example](.env.example). Bạn có thể cấu hình các biến môi trường khi triển khai thực tế:

| Biến môi trường | Mặc định | Mô tả |
| :--- | :--- | :--- |
| `PORT` | `8080` | Cổng HTTP của ứng dụng |
| `SPRING_DATASOURCE_URL` | `jdbc:sqlserver://localhost:1433;databaseName=badminton;...` | Chuỗi kết nối SQL Server |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Tài khoản CSDL SQL Server |
| `SPRING_DATASOURCE_PASSWORD` | `123456` | Mật khẩu CSDL SQL Server |
| `VIETQR_BANK_BIN` | `970422` (MBBank) | Mã BIN ngân hàng nhận tiền cọc |
| `VIETQR_ACCOUNT_NO` | `0903123456` | Số tài khoản nhận tiền cọc VietQR |
| `VIETQR_ACCOUNT_NAME` | `UTE BADMINTON CLUB` | Tên chủ tài khoản nhận cọc |
| `VIETQR_WEBHOOK_TOKEN` | `UTE_VIETQR_SECRET_TOKEN_2026` | Token xác thực Webhook thanh toán |
| `GEMINI_API_KEY` | *(Tùy chọn)* | Khóa API Google Gemini AI (nếu trống, hệ thống dùng NLP TF-IDF nội bộ) |
| `SPRING_MAIL_USERNAME` | `utesport.badminton@gmail.com` | Email Gmail gửi vé điện tử E-Ticket |
| `SPRING_MAIL_PASSWORD` | *(Mật khẩu ứng dụng 16 ký tự)* | Mật khẩu ứng dụng Gmail SMTP |
| `CLOUDINARY_CLOUD_NAME` | `utesport-arena` | Tên Cloudinary lưu trữ ảnh |
| `JWT_SECRET` | `UTE_BADMINTON_...` | Khóa bí mật ký mã hóa JWT Token |

---

## Kiểm thử tự động

Dự án tích hợp bộ kiểm thử tự động toàn diện (Unit Tests, Integration Tests, MockMvc, Boundary & Concurrency Tests) với **121/121 bài kiểm thử đạt kết quả thành công (100% Passed)**:

```bash
# Chạy bộ kiểm thử trên Windows:
.\mvnw.cmd test

# Chạy bộ kiểm thử trên macOS / Linux:
./mvnw test
```

---

## Thành viên phát triển

| Họ và tên                   | MSSV     |
|-----------------------------|:--------:|
| Nguyễn Phước Thọ            | 24110343 | 
| Nguyễn Phước Minh Triết     | 24133005 |
| Trần Tiến Đạt               | 24133033 |
---

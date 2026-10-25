# UTE SPORT — Hệ thống Quản lý Chuỗi Sân Cầu Lông

![Java](https://img.shields.io/badge/Java-24%2F26-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![SQL Server](https://img.shields.io/badge/SQL_Server-2022-CC2927?style=for-the-badge&logo=microsoftsqlserver&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Tests](https://img.shields.io/badge/Tests-119%20passed-brightgreen?style=for-the-badge)

> **Đồ án môn Công nghệ Phần mềm / Phát triển Ứng dụng Web**  
> Nhóm: Nguyễn Phước Thọ · Nguyễn Phước Minh Triết · Trần Tiến Đạt  
> Phiên bản: **v1.0.0** — Ngày phát hành: 25/10/2026

---

## Mục lục

1. [Giới thiệu tổng quan](#1-giới-thiệu-tổng-quan)
2. [Phân hệ chức năng & RBAC](#2-phân-hệ-chức-năng--rbac)
3. [Kiến trúc hệ thống](#3-kiến-trúc-hệ-thống)
4. [Cấu trúc thư mục](#4-cấu-trúc-thư-mục)
5. [Tài khoản demo](#5-tài-khoản-demo)
6. [Hướng dẫn chạy nhanh (Dev Mode)](#6-hướng-dẫn-chạy-nhanh-dev-mode)
7. [Hướng dẫn chạy với Docker](#7-hướng-dẫn-chạy-với-docker)
8. [Chạy kiểm thử tự động](#8-chạy-kiểm-thử-tự-động)
9. [Biến môi trường](#9-biến-môi-trường)
10. [Thành viên thực hiện](#10-thành-viên-thực-hiện)

---

## 1. Giới thiệu tổng quan

**UTE SPORT** là nền tảng quản lý và vận hành chuỗi sân cầu lông tiêu chuẩn. Hệ thống tự động hóa toàn bộ quy trình:

- **Đặt sân trực tuyến** với mã VietQR thanh toán cọc 30% tự động.
- **Quầy thu ngân POS** check-in/check-out QR, bán lẻ F&B và dụng cụ cho thuê.
- **Quản trị kho** theo dõi tồn kho và nhập hàng.
- **Phân ca nhân sự** xếp lịch và chốt ca với đối soát tiền mặt.
- **Bảng giá linh hoạt** khung giờ vàng / ngày lễ với quy trình duyệt giá.
- **Báo cáo thống kê** doanh thu đa chi nhánh trực quan.
- **Trợ lý ảo AI** tích hợp Google Gemini + NLU fallback cục bộ.
- **Thông báo WebSocket** cập nhật trạng thái sân thời gian thực.

---

## 2. Phân hệ chức năng & RBAC

| Vai trò | URL Prefix | Chức năng chính |
|---|---|---|
| **Khách hàng** | `/customer/*` | Xem sân, đặt lịch, thanh toán VietQR, hồ sơ hội viên, lịch sử |
| **Thu ngân POS** | `/pos/*` | Lưới sân realtime, check-in/out QR, bán F&B, in hóa đơn |
| **Quản lý CN** | `/manager/*` | Tồn kho, lịch nhân sự, đề xuất giá, sơ đồ sân |
| **Giám đốc** | `/director/*` | Dashboard đa CN, duyệt giá, quản lý giải đấu |
| **Admin HT** | `/admin/*` | Tài khoản, phân quyền, cấu hình hệ thống, audit log |

---

## 3. Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────────┐
│                        BROWSER / CLIENT                         │
│         Thymeleaf HTML · Vanilla JS · WebSocket STOMP           │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP / WebSocket
┌────────────────────────────▼────────────────────────────────────┐
│                  SPRING BOOT APPLICATION (port 8080)            │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │ Security     │  │ Controllers  │  │  WebSocketConfig   │    │
│  │ (JWT Filter) │  │ Dashboard    │  │  STOMP /ws-court   │    │
│  │ SecurityConfig│  │ ApiController│  │  SimpMessaging     │    │
│  └──────┬───────┘  └──────┬───────┘  └────────────────────┘    │
│         │                 │                                     │
│  ┌──────▼─────────────────▼─────────────────────────────────┐  │
│  │                   SERVICE LAYER                           │  │
│  │  UserService · BookingService · CourtService              │  │
│  │  PricingService · PaymentWebhookService · ShiftService    │  │
│  │  WarehouseService · TournamentService · ChatbotService    │  │
│  │  GeminiAiService · EmailService · CloudinaryService       │  │
│  └──────────────────────────┬────────────────────────────────┘  │
│                             │ Spring Data JPA                   │
│  ┌──────────────────────────▼────────────────────────────────┐  │
│  │                 REPOSITORY LAYER (20 Repos)               │  │
│  └──────────────────────────┬────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │ JDBC
           ┌─────────────────┴──────────────────┐
           │                                    │
    ┌──────▼──────┐                    ┌────────▼────────┐
    │  H2 In-Mem  │                    │  SQL Server 2022│
    │  (Dev/Test) │                    │  (Production)   │
    └─────────────┘                    └─────────────────┘
```

**Tích hợp bên ngoài:**

| Dịch vụ | Mục đích |
|---|---|
| VietQR / SePay / Casso | Thanh toán cọc 30% và webhook xác nhận tự động |
| Google Gemini AI API | Chatbot tư vấn đặt sân (fallback NLU cục bộ khi offline) |
| Cloudinary CDN | Upload và lưu trữ ảnh sân, avatar hội viên |
| Gmail SMTP | Gửi OTP quên mật khẩu, vé điện tử e-ticket, thông báo |

---

## 4. Cấu trúc thư mục

```text
FINAL_REPORT/
├── .mvn/wrapper/                   # Maven Wrapper
├── database/
│   ├── init_badminton.sql          # 25 bảng + ràng buộc + dữ liệu mẫu
│   ├── procedures_triggers.sql     # Stored Procedures, Triggers, Functions
│   └── README.md                   # Đặc tả kỹ thuật CSDL
├── docs/
│   └── progress.md                 # Nhật ký tiến độ tích hợp 9 ngày
├── src/main/java/vn/yain/
│   ├── config/                     # WebSocketConfig
│   ├── controller/                 # ApiController, DashboardController, HomeController
│   │                               # PaymentWebhookController, GlobalExceptionHandler
│   ├── dto/                        # ChatMessageDto, CourtEventDto
│   ├── entity/                     # 20+ JPA Entities (Court, Booking, Invoice, ...)
│   ├── repository/                 # 20 Spring Data JPA Repositories
│   ├── security/                   # JwtTokenProvider, JwtAuthFilter, SecurityConfig
│   └── service/                    # 12 Business Services
├── src/main/resources/
│   ├── static/css/                 # Emerald Luxury Design Tokens
│   ├── static/js/                  # auth-guard.js, global-hub.js, chatbot-widget.js
│   ├── templates/                  # Thymeleaf views (customer, pos, manager, admin)
│   ├── application.properties      # Cấu hình chung (profile: dev mặc định)
│   ├── application-dev.properties  # H2 in-memory
│   └── application-prod.properties # Microsoft SQL Server 2022
├── src/test/                       # 119 Unit & Integration Tests
├── Dockerfile                      # Multi-stage: JDK 24 build → JRE 21 runtime
├── docker-compose.yml              # SQL Server 2022 + App container
├── .env.example                    # Mẫu biến môi trường Docker
├── pom.xml
└── README.md
```

---

## 5. Tài khoản demo

> **Áp dụng cho môi trường Dev (H2) sau khi ứng dụng khởi động lần đầu.**  
> Dữ liệu mẫu được seed tự động từ `DataInitializer`.

| Vai trò | Username | Mật khẩu | Truy cập |
|---|---|---|---|
| **Admin Hệ thống** | `admin` | `Admin@2026!` | `/admin/*` — toàn quyền |
| **Giám đốc chuỗi** | `director01` | `Director@2026!` | `/director/*` |
| **Quản lý CN01** | `manager01` | `Manager@2026!` | `/manager/*` (CN01) |
| **Thu ngân POS** | `cashier01` | `Cashier@2026!` | `/pos/*` (CN01) |
| **Khách hàng HV** | `thopham` | `Tho@2026!` | `/customer/*` |
| **Khách hàng HV** | `trietng` | `Triet@2026!` | `/customer/*` |

> **Đăng nhập nhân sự:** `http://localhost:8085/login`  
> **Đăng nhập khách hàng / Đăng ký hội viên:** `http://localhost:8085/customer/auth`

---

## 6. Hướng dẫn chạy nhanh (Dev Mode)

### Yêu cầu hệ thống

- **JDK 24+** (khuyến nghị JDK 26) — [Tải tại Adoptium](https://adoptium.net/)
- Không cần cài Maven riêng (dùng `mvnw` tích hợp sẵn)
- Không cần SQL Server (Dev mode dùng H2 In-Memory)

### Bước 1 — Clone dự án

```powershell
git clone https://github.com/shinn1603/Badminton_Management_Website.git
cd Badminton_Management_Website
```

### Bước 2 — Khởi chạy ứng dụng

```powershell
.\mvnw.cmd spring-boot:run
```

> Ứng dụng sẽ khởi động trong ~15 giây. Màn hình sẽ hiển thị:  
> `Started BadmintonManagementProjectApplication in X.XXX seconds`

### Bước 3 — Truy cập

| Đường dẫn | Mô tả |
|---|---|
| `http://localhost:8085/` | Trang chủ |
| `http://localhost:8085/customer/auth` | Đăng nhập / Đăng ký hội viên |
| `http://localhost:8085/login` | Đăng nhập nhân sự / quản lý |
| `http://localhost:8085/h2-console` | H2 Web Console (JDBC: `jdbc:h2:mem:badminton`, user: `sa`) |

---

## 7. Hướng dẫn chạy với Docker

### Yêu cầu

- Docker Desktop (Windows/macOS) hoặc Docker Engine + Compose Plugin (Linux)

### Bước 1 — Chuẩn bị file biến môi trường

```powershell
copy .env.example .env
# Mở .env và điền các giá trị thực (DB_PASSWORD, JWT_SECRET, ...)
```

### Bước 2 — Build và khởi chạy

```powershell
docker compose up --build -d
```

> SQL Server cần ~60-90 giây để khởi động hoàn toàn.  
> Ứng dụng sẽ tự động chờ SQL Server healthy trước khi start.

### Bước 3 — Kiểm tra trạng thái

```powershell
docker compose ps
docker compose logs app --tail=50
```

### Bước 4 — Truy cập

- **Ứng dụng:** `http://localhost:8080/`
- **SQL Server:** `localhost:1433` (dùng SSMS hoặc DBeaver)

### Dừng dịch vụ

```powershell
docker compose down          # Dừng, giữ data
docker compose down -v       # Dừng, xóa toàn bộ data
```

---

## 8. Chạy kiểm thử tự động

```powershell
.\mvnw.cmd clean test "-Dmaven.compiler.release=24"
```

**Kết quả kỳ vọng:**

```
Tests run: 119, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Bộ test theo module (Ngày 1–9)

| Module | Test class | Tests |
|---|---|---|
| Repository (Court, TimeSlot) | `CourtAndTimeSlotRepositoryTest` | 2 |
| Security (JWT) | `JwtTokenProviderTest` | 4 |
| Service (User) | `UserServiceTest` | 8 |
| Service (Court) | `CourtServiceTest` | 7 |
| Controller (Profile) | `CustomerProfileTest` | 5 |
| Controller (Admin Users) | `AdminUserManagementTest` | 5 |
| Controller (Settings & Logs) | `SystemSettingsAndAuditLogsTest` | 5 |
| Config (WebSocket) | `WebSocketRealtimeIntegrationTest` | 5 |
| Service (Booking) | `BookingServiceTest` | 12 |
| Service (POS & Courts) | `CashierAndCourtOperationsTest` | 12 |
| Service (Payment Webhook) | `PaymentWebhookServiceTest` | 5 |
| Service (Shift) | `ShiftServiceTest` | 5 |
| Service (Chatbot & Email) | `ChatbotAndCustomerSupportTest` | 7 |
| Service (Warehouse) | `WarehouseServiceTest` | 3 |
| Service (Regression E2E) | `BookingAndCashierLifecycleRegressionTest` | 1 |
| Controller (API) | `ApiControllerTest` | 10 |
| Controller (Dashboard) | `DashboardControllerTest` | 5 |
| Controller (Home) | `HomeControllerTest` | 3 |
| Controller (Slot Overlapping) | `SlotOverlappingTest` | 5 |
| Integration (E2E System) | `SystemIntegrationE2ETest` | 10 |
| **Tổng cộng** | | **119** |

---

## 9. Biến môi trường

Sao chép `.env.example` thành `.env` và điền giá trị thực trước khi deploy:

| Biến | Mô tả | Mặc định |
|---|---|---|
| `DB_PASSWORD` | Mật khẩu SQL Server `sa` | `UTE@Badminton2026!` |
| `APP_PORT` | Cổng HTTP ứng dụng | `8080` |
| `JWT_SECRET` | Khóa bí mật ký JWT | Chuỗi nội bộ mặc định |
| `GEMINI_API_KEY` | API Key Google Gemini AI | *(để trống → dùng NLU cục bộ)* |
| `CLOUDINARY_CLOUD_NAME` | Tên cloud Cloudinary | `utesport-arena` |
| `CLOUDINARY_API_KEY` | API Key Cloudinary | *(để trống → tắt upload ảnh)* |
| `CLOUDINARY_API_SECRET` | Secret Cloudinary | *(để trống)* |
| `SPRING_MAIL_USERNAME` | Gmail SMTP username | `utesport.badminton@gmail.com` |
| `SPRING_MAIL_PASSWORD` | Gmail App Password | *(để trống → tắt gửi email)* |
| `VIETQR_WEBHOOK_TOKEN` | Secret token webhook ngân hàng | `UTE_VIETQR_SECRET_TOKEN_2026` |
| `VIETQR_BANK_BIN` | BIN ngân hàng nhận tiền | `970422` (MBBank) |
| `VIETQR_ACCOUNT_NO` | Số tài khoản nhận tiền | `0903123456` |
| `VIETQR_ACCOUNT_NAME` | Tên tài khoản nhận | `UTE BADMINTON CLUB` |

---

## 10. Thành viên thực hiện

| Họ và tên | MSSV | Vai trò | Phụ trách chính |
|---|---|---|---|
| **Nguyễn Phước Thọ** | 24110343 | Trưởng nhóm / Frontend & POS | Court, BookingService, POS cashier, VietQR webhook, WebSocket realtime, Regression tests |
| **Nguyễn Phước Minh Triết** | 24110357 | Core Architecture & Backend | Spring Boot baseline, JWT Security, UserService, Admin, System Settings, Chatbot AI, Docker & Release |
| **Trần Tiến Đạt** | 24110198 | Operations & Backend | Product catalog, WarehouseService, PricingService, Staff scheduling, Branch & Tournament management |
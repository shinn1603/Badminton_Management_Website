-- =====================================================================
-- BẢN SAO LƯU TOÀN DIỆN HỆ THỐNG QUẢN LÝ SÂN CẦU LÔNG UTE SPORT
-- Thời gian xuất: 2026-10-25 22:49:00
-- Người tạo: admin
-- =====================================================================

-- =====================================================================
-- DỰ ÁN QUẢN LÝ CHUỖI SÂN CẦU LÔNG - UTE SPORT
-- HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU: MICROSOFT SQL SERVER
-- TÀI KHOẢN KẾT NỐI MẶC ĐỊNH: sa / 123456
-- TIÊU CHUẨN THIẾT KẾ: ĐỒNG BỘ 100% THEO FILE WORD ĐẶC TẢ DỰ ÁN & 25 MÀN HÌNH WEB
-- =====================================================================

-- 1. TẠO CƠ SỞ DỮ LIỆU NẾU CHƯA CÓ
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'badminton')
BEGIN
    CREATE DATABASE badminton;
END
GO

USE badminton;
GO

-- 2. XÓA CÁC BẢNG, VIEW, PROCEDURE, TRIGGER VÀ FUNCTION CŨ ĐỂ KHỞI TẠO MỚI TOÀN DIỆN
-- 2.0 Xóa Triggers, Procedures và Functions cũ nếu đã tồn tại
IF OBJECT_ID('dbo.trg_CheckBookingOverlap', 'TR') IS NOT NULL DROP TRIGGER dbo.trg_CheckBookingOverlap;
IF OBJECT_ID('dbo.trg_UpdateStockOnInvoiceProduct', 'TR') IS NOT NULL DROP TRIGGER dbo.trg_UpdateStockOnInvoiceProduct;
IF OBJECT_ID('dbo.trg_UpdateStockOnReceiptDetails', 'TR') IS NOT NULL DROP TRIGGER dbo.trg_UpdateStockOnReceiptDetails;
IF OBJECT_ID('dbo.trg_UpdateCustomerMembershipLevel', 'TR') IS NOT NULL DROP TRIGGER dbo.trg_UpdateCustomerMembershipLevel;
IF OBJECT_ID('dbo.trg_AuditCourtStatusChange', 'TR') IS NOT NULL DROP TRIGGER dbo.trg_AuditCourtStatusChange;
IF OBJECT_ID('dbo.trg_ValidatePromotionDates', 'TR') IS NOT NULL DROP TRIGGER dbo.trg_ValidatePromotionDates;

IF OBJECT_ID('dbo.sp_LockSlotAndCreateBooking', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_LockSlotAndCreateBooking;
IF OBJECT_ID('dbo.sp_ConfirmBookingDeposit', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_ConfirmBookingDeposit;
IF OBJECT_ID('dbo.sp_CheckInBooking', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_CheckInBooking;
IF OBJECT_ID('dbo.sp_CheckOutAndCreateInvoice', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_CheckOutAndCreateInvoice;
IF OBJECT_ID('dbo.sp_CancelBookingWithRefundPolicy', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_CancelBookingWithRefundPolicy;
IF OBJECT_ID('dbo.sp_ImportStockReceipt', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_ImportStockReceipt;
IF OBJECT_ID('dbo.sp_GetBranchDailyRevenueReport', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_GetBranchDailyRevenueReport;

IF OBJECT_ID('dbo.fn_CheckSlotAvailability', 'FN') IS NOT NULL DROP FUNCTION dbo.fn_CheckSlotAvailability;
IF OBJECT_ID('dbo.fn_CalculateBookingPrice', 'FN') IS NOT NULL DROP FUNCTION dbo.fn_CalculateBookingPrice;
IF OBJECT_ID('dbo.fn_GetCustomerTierDiscount', 'FN') IS NOT NULL DROP FUNCTION dbo.fn_GetCustomerTierDiscount;
IF OBJECT_ID('dbo.fn_GetBranchRevenue', 'FN') IS NOT NULL DROP FUNCTION dbo.fn_GetBranchRevenue;
IF OBJECT_ID('dbo.fn_GetAvailableCourtsByBranch', 'IF') IS NOT NULL DROP FUNCTION dbo.fn_GetAvailableCourtsByBranch;
GO

-- 2.1 Xóa các View tiếng Việt nếu đã tồn tại
IF OBJECT_ID('ChiNhanh', 'V') IS NOT NULL DROP VIEW ChiNhanh;
IF OBJECT_ID('San', 'V') IS NOT NULL DROP VIEW San;
IF OBJECT_ID('KhungGio', 'V') IS NOT NULL DROP VIEW KhungGio;
IF OBJECT_ID('BangGia', 'V') IS NOT NULL DROP VIEW BangGia;
IF OBJECT_ID('KhachHang', 'V') IS NOT NULL DROP VIEW KhachHang;
IF OBJECT_ID('DonDatSan', 'V') IS NOT NULL DROP VIEW DonDatSan;
IF OBJECT_ID('DungCu', 'V') IS NOT NULL DROP VIEW DungCu;
IF OBJECT_ID('SanPham', 'V') IS NOT NULL DROP VIEW SanPham;
IF OBJECT_ID('HoaDon', 'V') IS NOT NULL DROP VIEW HoaDon;
IF OBJECT_ID('ChiTietThueDungCu', 'V') IS NOT NULL DROP VIEW ChiTietThueDungCu;
IF OBJECT_ID('ChiTietBanSanPham', 'V') IS NOT NULL DROP VIEW ChiTietBanSanPham;
IF OBJECT_ID('NhanVien', 'V') IS NOT NULL DROP VIEW NhanVien;
IF OBJECT_ID('TaiKhoan', 'V') IS NOT NULL DROP VIEW TaiKhoan;
IF OBJECT_ID('GiaiDau', 'V') IS NOT NULL DROP VIEW GiaiDau;
IF OBJECT_ID('DangKyGiaiDau', 'V') IS NOT NULL DROP VIEW DangKyGiaiDau;
IF OBJECT_ID('ThanhToan', 'V') IS NOT NULL DROP VIEW ThanhToan;
IF OBJECT_ID('PhanCa', 'V') IS NOT NULL DROP VIEW PhanCa;
IF OBJECT_ID('PhieuNhapKho', 'V') IS NOT NULL DROP VIEW PhieuNhapKho;
IF OBJECT_ID('ChiTietNhapDungCu', 'V') IS NOT NULL DROP VIEW ChiTietNhapDungCu;
IF OBJECT_ID('ChiTietNhapSanPham', 'V') IS NOT NULL DROP VIEW ChiTietNhapSanPham;
IF OBJECT_ID('KhuyenMai', 'V') IS NOT NULL DROP VIEW KhuyenMai;
IF OBJECT_ID('NhatKyHoatDong', 'V') IS NOT NULL DROP VIEW NhatKyHoatDong;
IF OBJECT_ID('TranDau', 'V') IS NOT NULL DROP VIEW TranDau;
IF OBJECT_ID('CauHinhHeThong', 'V') IS NOT NULL DROP VIEW CauHinhHeThong;
GO

-- 2.2 Xóa các bảng dữ liệu cũ
IF OBJECT_ID('tournament_matches', 'U') IS NOT NULL DROP TABLE tournament_matches;
IF OBJECT_ID('tournament_registrations', 'U') IS NOT NULL DROP TABLE tournament_registrations;
IF OBJECT_ID('tournaments', 'U') IS NOT NULL DROP TABLE tournaments;
IF OBJECT_ID('invoice_equipment_details', 'U') IS NOT NULL DROP TABLE invoice_equipment_details;
IF OBJECT_ID('invoice_product_details', 'U') IS NOT NULL DROP TABLE invoice_product_details;
IF OBJECT_ID('invoices', 'U') IS NOT NULL DROP TABLE invoices;
IF OBJECT_ID('payments', 'U') IS NOT NULL DROP TABLE payments;
IF OBJECT_ID('booking_services', 'U') IS NOT NULL DROP TABLE booking_services;
IF OBJECT_ID('bookings', 'U') IS NOT NULL DROP TABLE bookings;
IF OBJECT_ID('pricing_proposals', 'U') IS NOT NULL DROP TABLE pricing_proposals;
IF OBJECT_ID('pricing_rules', 'U') IS NOT NULL DROP TABLE pricing_rules;
IF OBJECT_ID('time_slots', 'U') IS NOT NULL DROP TABLE time_slots;
IF OBJECT_ID('courts', 'U') IS NOT NULL DROP TABLE courts;
IF OBJECT_ID('stock_equipment_details', 'U') IS NOT NULL DROP TABLE stock_equipment_details;
IF OBJECT_ID('stock_product_details', 'U') IS NOT NULL DROP TABLE stock_product_details;
IF OBJECT_ID('stock_receipts', 'U') IS NOT NULL DROP TABLE stock_receipts;
IF OBJECT_ID('equipment', 'U') IS NOT NULL DROP TABLE equipment;
IF OBJECT_ID('products', 'U') IS NOT NULL DROP TABLE products;
IF OBJECT_ID('work_shifts', 'U') IS NOT NULL DROP TABLE work_shifts;
IF OBJECT_ID('audit_logs', 'U') IS NOT NULL DROP TABLE audit_logs;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
IF OBJECT_ID('staff', 'U') IS NOT NULL DROP TABLE staff;
IF OBJECT_ID('customers', 'U') IS NOT NULL DROP TABLE customers;
IF OBJECT_ID('promotions', 'U') IS NOT NULL DROP TABLE promotions;
IF OBJECT_ID('system_settings', 'U') IS NOT NULL DROP TABLE system_settings;
IF OBJECT_ID('branches', 'U') IS NOT NULL DROP TABLE branches;
GO

-- =====================================================================
-- 3. ĐỊNH NGHĨA 25 BẢNG DỮ LIỆU CỐT LÕI (CORE TABLES)
-- =====================================================================

-- BẢNG 1: CHI NHÁNH TOÀN CHUỖI (branches / ChiNhanh)
CREATE TABLE branches (
    id INT IDENTITY(1,1) PRIMARY KEY,
    branch_code VARCHAR(20) NOT NULL UNIQUE,
    branch_name NVARCHAR(100) NOT NULL,
    address NVARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    operating_hours NVARCHAR(100) DEFAULT N'06:00 - 23:00 hàng ngày',
    manager_name NVARCHAR(100),
    total_courts INT DEFAULT 8,
    status NVARCHAR(30) DEFAULT N'Hoạt động'
);
GO

-- BẢNG 2: SÂN CẦU LÔNG (courts / San)
CREATE TABLE courts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    court_code VARCHAR(20) NOT NULL UNIQUE,
    branch_code VARCHAR(20) NOT NULL REFERENCES branches(branch_code),
    court_name NVARCHAR(100) NOT NULL,
    court_type NVARCHAR(50) NOT NULL, -- Cầu lông VIP (Thảm Yonex), Cầu lông tiêu chuẩn (BWF), Cầu lông thi đấu
    hourly_rate DECIMAL(18,2) NOT NULL,
    status NVARCHAR(30) DEFAULT N'Trống', -- Trống, Đang sử dụng, Bảo trì, Đặt trước
    image_url VARCHAR(255) DEFAULT '/images/badminton-court.jpg'
);
GO

-- BẢNG 3: KHUNG GIỜ HOẠT ĐỘNG (time_slots / KhungGio)
CREATE TABLE time_slots (
    slot_code VARCHAR(20) NOT NULL PRIMARY KEY,
    start_time VARCHAR(10) NOT NULL,
    end_time VARCHAR(10) NOT NULL,
    slot_type NVARCHAR(50) NOT NULL -- Thường (Sáng), Thường (Chiều), Giờ vàng cao điểm, Tối muộn
);
GO

-- BẢNG 4: BẢNG GIÁ THEO KHUNG GIỜ & NGÀY (pricing_rules / BangGia)
CREATE TABLE pricing_rules (
    pricing_code VARCHAR(20) NOT NULL PRIMARY KEY,
    branch_code VARCHAR(20) NOT NULL REFERENCES branches(branch_code),
    slot_code VARCHAR(20) NOT NULL REFERENCES time_slots(slot_code),
    day_type NVARCHAR(50) NOT NULL, -- Ngày thường (T2-T6), Cuối tuần (T7-CN), Ngày lễ
    start_date DATE NOT NULL,
    end_date DATE NULL,
    hourly_rate DECIMAL(18,2) NOT NULL
);
GO

-- BẢNG 5: KHÁCH HÀNG THÀNH VIÊN (customers / KhachHang)
CREATE TABLE customers (
    id INT IDENTITY(1,1) PRIMARY KEY,
    customer_code VARCHAR(20) NOT NULL UNIQUE,
    full_name NVARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    password VARCHAR(100) NOT NULL DEFAULT '123456',
    total_play_hours DECIMAL(10,1) DEFAULT 0,
    reward_points INT DEFAULT 0,
    membership_tier NVARCHAR(30) DEFAULT N'Đồng', -- Đồng, Bạc, Vàng, Kim Cương
    status NVARCHAR(30) DEFAULT N'Hoạt động',
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- BẢNG 6: ĐƠN ĐẶT SÂN (bookings / DonDatSan)
CREATE TABLE bookings (
    id INT IDENTITY(1,1) PRIMARY KEY,
    booking_code VARCHAR(30) NOT NULL UNIQUE,
    customer_code VARCHAR(20) NULL REFERENCES customers(customer_code),
    customer_name NVARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_email VARCHAR(100) NULL,
    court_code VARCHAR(20) NOT NULL REFERENCES courts(court_code),
    branch_code VARCHAR(20) NOT NULL REFERENCES branches(branch_code),
    booking_date DATE NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    slot_code VARCHAR(20) NULL REFERENCES time_slots(slot_code),
    hourly_price DECIMAL(18,2) NOT NULL,
    total_price DECIMAL(18,2) NOT NULL,
    deposit_amount DECIMAL(18,2) DEFAULT 0,
    payment_method NVARCHAR(50) DEFAULT N'Tiền mặt', -- Tiền mặt, Chuyển khoản VietQR, VNPay, MoMo
    status NVARCHAR(50) NOT NULL, -- Đang sử dụng, Đã xác nhận, Chờ xác nhận, Đã cọc 30%, Hoàn thành, Đã hủy
    notes NVARCHAR(255),
    created_at DATETIME DEFAULT GETDATE(),
    check_in_time DATETIME NULL,
    check_out_time DATETIME NULL
);
GO

-- BẢNG 7: DỤNG CỤ CHO THUÊ (equipment / DungCu)
CREATE TABLE equipment (
    equipment_code VARCHAR(20) NOT NULL PRIMARY KEY,
    branch_code VARCHAR(20) NOT NULL REFERENCES branches(branch_code),
    equipment_name NVARCHAR(100) NOT NULL,
    stock_quantity INT DEFAULT 0,
    rental_price DECIMAL(18,2) NOT NULL,
    condition NVARCHAR(50) DEFAULT N'Tốt' -- Mới 99%, Tốt, Cần căng cước
);
GO

-- BẢNG 8: SẢN PHẨM & ĐỒ UỐNG (products / SanPham)
CREATE TABLE products (
    id INT IDENTITY(1,1) PRIMARY KEY,
    product_code VARCHAR(20) NOT NULL UNIQUE,
    branch_code VARCHAR(20) NOT NULL REFERENCES branches(branch_code),
    product_name NVARCHAR(100) NOT NULL,
    category NVARCHAR(50) NOT NULL, -- Nước giải khát, Dụng cụ, Cầu lông, Phụ kiện
    unit NVARCHAR(20) NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    cost_price DECIMAL(18,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    min_quantity INT DEFAULT 10
);
GO

-- BẢNG 9: HÓA ĐƠN THANH TOÁN TẠI QUẦY POS (invoices / HoaDon)
CREATE TABLE invoices (
    id INT IDENTITY(1,1) PRIMARY KEY,
    invoice_code VARCHAR(30) NOT NULL UNIQUE,
    booking_code VARCHAR(30) NULL REFERENCES bookings(booking_code),
    staff_code VARCHAR(20) NULL,
    invoice_date DATETIME DEFAULT GETDATE(),
    court_fee DECIMAL(18,2) NOT NULL,
    equipment_fee DECIMAL(18,2) DEFAULT 0,
    product_fee DECIMAL(18,2) DEFAULT 0,
    discount_amount DECIMAL(18,2) DEFAULT 0,
    deposit_paid DECIMAL(18,2) DEFAULT 0,
    total_payment DECIMAL(18,2) NOT NULL,
    payment_method NVARCHAR(50) DEFAULT N'Tiền mặt',
    status NVARCHAR(30) DEFAULT N'Đã thanh toán'
);
GO

-- BẢNG 10: CHI TIẾT THUÊ DỤNG CỤ THEO HÓA ĐƠN (invoice_equipment_details / ChiTietThueDungCu)
CREATE TABLE invoice_equipment_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    invoice_code VARCHAR(30) NOT NULL REFERENCES invoices(invoice_code),
    equipment_code VARCHAR(20) NOT NULL REFERENCES equipment(equipment_code),
    quantity INT NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL
);
GO

-- BẢNG 11: CHI TIẾT BÁN SẢN PHẨM / ĐỒ UỐNG THEO HÓA ĐƠN (invoice_product_details / ChiTietBanSanPham)
CREATE TABLE invoice_product_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    invoice_code VARCHAR(30) NOT NULL REFERENCES invoices(invoice_code),
    product_code VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL
);
GO

-- BẢNG DỊCH VỤ ĐI KÈM ĐƠN ĐẶT SÂN (booking_services - Dành cho tương thích ngược)
CREATE TABLE booking_services (
    id INT IDENTITY(1,1) PRIMARY KEY,
    booking_code VARCHAR(30) NOT NULL REFERENCES bookings(booking_code),
    product_name NVARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL
);
GO

-- BẢNG 12: NHÂN VIÊN CHI NHÁNH (staff / NhanVien)
CREATE TABLE staff (
    id INT IDENTITY(1,1) PRIMARY KEY,
    staff_code VARCHAR(20) NOT NULL UNIQUE,
    branch_code VARCHAR(20) NOT NULL REFERENCES branches(branch_code),
    full_name NVARCHAR(100) NOT NULL,
    position NVARCHAR(50) NOT NULL, -- Quản lý chi nhánh, Thu ngân quầy POS, Kỹ thuật sân
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address NVARCHAR(255),
    status NVARCHAR(30) DEFAULT N'Đang làm việc'
);
GO

-- BẢNG 13: TÀI KHOẢN NGƯỜI DÙNG & PHÂN QUYỀN (users / TaiKhoan)
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(30) NOT NULL, -- DIRECTOR, MANAGER, POS, CUSTOMER, ADMIN
    staff_code VARCHAR(20) NULL REFERENCES staff(staff_code),
    branch_code VARCHAR(20) DEFAULT 'ALL',
    status NVARCHAR(30) DEFAULT N'Hoạt động',
    created_at DATETIME DEFAULT GETDATE(),
    last_login DATETIME NULL
);
GO

-- BẢNG 14: GIẢI ĐẤU VÀ SỰ KIỆN CẦU LÔNG (tournaments / GiaiDau)
CREATE TABLE tournaments (
    id INT IDENTITY(1,1) PRIMARY KEY,
    tournament_code VARCHAR(20) NOT NULL UNIQUE,
    branch_code VARCHAR(20) NULL REFERENCES branches(branch_code),
    tournament_name NVARCHAR(150) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    rules NVARCHAR(MAX),
    category NVARCHAR(50) DEFAULT N'Đôi nam nữ', -- Đơn nam, Đơn nữ, Đôi nam, Đôi nữ, Đôi nam nữ, Tổng hợp nhiều nội dung
    max_participants INT DEFAULT 32,
    current_participants INT DEFAULT 24,
    entry_fee DECIMAL(18,2) DEFAULT 0,
    total_prize DECIMAL(18,2) DEFAULT 0,
    status NVARCHAR(50) DEFAULT N'Đang mở đăng ký' -- Sắp mở đăng ký, Đang mở đăng ký, Đang diễn ra, Đã kết thúc
);
GO

-- BẢNG 15: ĐĂNG KÝ THAM GIA GIẢI ĐẤU (tournament_registrations / DangKyGiaiDau)
CREATE TABLE tournament_registrations (
    id INT IDENTITY(1,1) PRIMARY KEY,
    tournament_code VARCHAR(20) NOT NULL REFERENCES tournaments(tournament_code),
    customer_code VARCHAR(20) NOT NULL REFERENCES customers(customer_code),
    team_name NVARCHAR(100) NOT NULL,
    category NVARCHAR(50) DEFAULT N'Đôi nam nữ', -- Đơn nam, Đơn nữ, Đôi nam, Đôi nữ, Đôi nam nữ
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NULL,
    registration_date DATETIME DEFAULT GETDATE(),
    status NVARCHAR(30) DEFAULT N'Đã xác nhận' -- Chờ xác nhận, Đã xác nhận, Đã hủy
);
GO

-- BẢNG 16: GIAO DỊCH THANH TOÁN TRỰC TUYẾN & TẠI QUẦY (payments / ThanhToan)
CREATE TABLE payments (
    id INT IDENTITY(1,1) PRIMARY KEY,
    payment_code VARCHAR(30) NOT NULL UNIQUE,
    booking_code VARCHAR(30) NOT NULL REFERENCES bookings(booking_code),
    amount DECIMAL(18,2) NOT NULL,
    method NVARCHAR(50) NOT NULL, -- VietQR Pro, Tiền mặt, VNPay, MoMo
    transaction_type NVARCHAR(50) NOT NULL, -- Đặt cọc 30%, Thanh toán đủ, Hoàn tiền cọc
    transaction_ref VARCHAR(100),
    payment_time DATETIME DEFAULT GETDATE(),
    status NVARCHAR(30) DEFAULT N'Thành công'
);
GO

-- BẢNG 17: PHÂN CA LÀM VIỆC NHÂN VIÊN (work_shifts / PhanCa)
CREATE TABLE work_shifts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    shift_code VARCHAR(50) NOT NULL UNIQUE,
    staff_code VARCHAR(20) NOT NULL REFERENCES staff(staff_code),
    branch_code VARCHAR(20) DEFAULT 'CN01' REFERENCES branches(branch_code),
    day_of_week VARCHAR(10) NULL,
    shift_type NVARCHAR(30) NOT NULL, -- morning, night, Ca Sáng, Ca Chiều, Ca Tối
    duty NVARCHAR(100) NULL, -- POS, Thu ngân, Lễ tân, Kỹ thuật, Quản lý
    icon VARCHAR(30) DEFAULT 'POS',
    shift_date DATE NOT NULL,
    start_time VARCHAR(10) NOT NULL,
    end_time VARCHAR(10) NOT NULL,
    status NVARCHAR(50) DEFAULT N'Đã phân ca' -- Đã phân ca, Đang trực, Đã hoàn thành
);
GO

-- BẢNG 18: PHIẾU NHẬP KHO VẬT TƯ (stock_receipts / PhieuNhapKho)
CREATE TABLE stock_receipts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    receipt_code VARCHAR(30) NOT NULL UNIQUE,
    branch_code VARCHAR(20) NOT NULL REFERENCES branches(branch_code),
    staff_code VARCHAR(20) NOT NULL REFERENCES staff(staff_code),
    receipt_date DATETIME DEFAULT GETDATE(),
    supplier_name NVARCHAR(100) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    notes NVARCHAR(255)
);
GO

-- BẢNG 19: CHI TIẾT NHẬP DỤNG CỤ (stock_equipment_details / ChiTietNhapDungCu)
CREATE TABLE stock_equipment_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    receipt_code VARCHAR(30) NOT NULL REFERENCES stock_receipts(receipt_code),
    equipment_code VARCHAR(20) NOT NULL REFERENCES equipment(equipment_code),
    quantity INT NOT NULL,
    import_price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL
);
GO

-- BẢNG 20: CHI TIẾT NHẬP SẢN PHẨM / ĐỒ UỐNG (stock_product_details / ChiTietNhapSanPham)
CREATE TABLE stock_product_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    receipt_code VARCHAR(30) NOT NULL REFERENCES stock_receipts(receipt_code),
    product_code VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    import_price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL
);
GO

-- BẢNG 21: CHƯƠNG TRÌNH KHUYẾN MÃI (promotions / KhuyenMai)
CREATE TABLE promotions (
    id INT IDENTITY(1,1) PRIMARY KEY,
    promo_code VARCHAR(20) NOT NULL UNIQUE,
    promo_name NVARCHAR(150) NOT NULL,
    discount_type NVARCHAR(30) NOT NULL, -- Phần trăm (%), Số tiền (VNĐ)
    discount_value DECIMAL(18,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    scope NVARCHAR(50) DEFAULT N'Toàn chuỗi',
    status NVARCHAR(30) DEFAULT N'Đang áp dụng'
);
GO

-- BẢNG 22: NHẬT KÝ HOẠT ĐỘNG HỆ THỐNG (audit_logs / NhatKyHoatDong)
CREATE TABLE audit_logs (
    log_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    action_type NVARCHAR(100) NOT NULL,
    action_time DATETIME DEFAULT GETDATE(),
    details NVARCHAR(MAX) NOT NULL,
    ip_address VARCHAR(50) DEFAULT '127.0.0.1',
    module NVARCHAR(50) DEFAULT N'Hệ thống',
    status NVARCHAR(30) DEFAULT N'Thành công'
);
GO

-- BẢNG 23: LỊCH THI ĐẤU & KẾT QUẢ TRẬN ĐẤU (tournament_matches / TranDau)
CREATE TABLE tournament_matches (
    id INT IDENTITY(1,1) PRIMARY KEY,
    match_code VARCHAR(20) NOT NULL UNIQUE,
    tournament_code VARCHAR(20) NOT NULL REFERENCES tournaments(tournament_code),
    court_code VARCHAR(20) NOT NULL REFERENCES courts(court_code),
    player1_name NVARCHAR(100) NOT NULL,
    player2_name NVARCHAR(100) NOT NULL,
    match_time DATETIME NOT NULL,
    result NVARCHAR(50) DEFAULT N'Chưa diễn ra',
    status NVARCHAR(30) DEFAULT N'Sắp diễn ra' -- Sắp diễn ra, Đang thi đấu, Đã kết thúc
);
GO

-- BẢNG 24: CẤU HÌNH THÔNG SỐ HỆ THỐNG (system_settings / CauHinhHeThong)
CREATE TABLE system_settings (
    id INT IDENTITY(1,1) PRIMARY KEY,
    setting_code VARCHAR(50) NOT NULL UNIQUE,
    setting_name NVARCHAR(100) NOT NULL,
    setting_value NVARCHAR(255) NOT NULL,
    description NVARCHAR(255),
    updated_at DATETIME DEFAULT GETDATE()
);
GO

-- BẢNG 25: ĐỀ XUẤT ĐIỀU CHỈNH GIÁ SÂN (pricing_proposals / DeXuatGia)
CREATE TABLE pricing_proposals (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    proposal_code VARCHAR(20) NOT NULL UNIQUE,
    branch_code VARCHAR(20) NOT NULL REFERENCES branches(branch_code),
    proposer_name NVARCHAR(100) NOT NULL,
    court_type NVARCHAR(100) NOT NULL,
    slot_info NVARCHAR(100) NOT NULL,
    current_price DECIMAL(18,2) NOT NULL,
    proposed_price DECIMAL(18,2) NOT NULL,
    reason NVARCHAR(255) NULL,
    status NVARCHAR(30) DEFAULT N'Chờ phê duyệt', -- Chờ phê duyệt, Đã phê duyệt, Từ chối
    created_at DATETIME2 DEFAULT GETDATE(),
    approved_at DATETIME2 NULL,
    note NVARCHAR(255) NULL
);
GO

-- =====================================================================
-- 4. TẠO CÁC VIEW ÁNH XẠ TIẾNG VIỆT THEO CHUẨN ĐẶC TẢ CHƯƠNG 5 FILE WORD
-- =====================================================================

CREATE VIEW ChiNhanh AS SELECT branch_code AS MaCN, branch_name AS TenCN, address AS DiaChi, phone AS SoDienThoai, operating_hours AS GioHoatDong FROM branches;
GO
CREATE VIEW San AS SELECT court_code AS MaSan, branch_code AS MaCN, court_name AS TenSan, court_type AS LoaiSan, status AS TinhTrang FROM courts;
GO
CREATE VIEW KhungGio AS SELECT slot_code AS MaKG, start_time AS ThoiGianBatDau, end_time AS ThoiGianKetThuc, slot_type AS LoaiKhungGio FROM time_slots;
GO
CREATE VIEW BangGia AS SELECT pricing_code AS MaBG, branch_code AS MaCN, slot_code AS MaKG, day_type AS LoaiNgay, start_date AS NgayBatDau, end_date AS NgayKetThuc, hourly_rate AS DonGia FROM pricing_rules;
GO
CREATE VIEW KhachHang AS SELECT customer_code AS MaKH, full_name AS HoTen, phone AS SoDienThoai, email AS Email, password AS MatKhau, total_play_hours AS TongGioChoi, reward_points AS DiemTichLuy, membership_tier AS HangThanhVien FROM customers;
GO
CREATE VIEW DonDatSan AS SELECT booking_code AS MaDatSan, customer_code AS MaKH, court_code AS MaSan, booking_date AS NgayChoi, slot_code AS MaKG, 1 AS SoGioDat, deposit_amount AS TienCoc, status AS TrangThai, created_at AS ThoiGianTao, check_in_time AS ThoiGianCheckIn, check_out_time AS ThoiGianCheckOut FROM bookings;
GO
CREATE VIEW DungCu AS SELECT equipment_code AS MaDungCu, branch_code AS MaCN, equipment_name AS TenDungCu, stock_quantity AS SoLuongTon, rental_price AS DonGiaThue, condition AS TinhTrang FROM equipment;
GO
CREATE VIEW SanPham AS SELECT product_code AS MaSP, branch_code AS MaCN, product_name AS TenSP, unit_price AS DonGia, stock_quantity AS SoLuongTon FROM products;
GO
CREATE VIEW HoaDon AS SELECT invoice_code AS MaHD, booking_code AS MaDatSan, staff_code AS MaNV, invoice_date AS NgayLap, court_fee AS TienSan, equipment_fee AS TienDungCu, product_fee AS TienDoUong, discount_amount AS ChietKhauThanhVien, deposit_paid AS TienCocDaTra, total_payment AS TongThanhToan FROM invoices;
GO
CREATE VIEW ChiTietThueDungCu AS SELECT invoice_code AS MaHD, equipment_code AS MaDungCu, quantity AS SoLuongThue, total_amount AS ThanhTien FROM invoice_equipment_details;
GO
CREATE VIEW ChiTietBanSanPham AS SELECT invoice_code AS MaHD, product_code AS MaSP, quantity AS SoLuongBan, total_amount AS ThanhTien FROM invoice_product_details;
GO
CREATE VIEW NhanVien AS SELECT staff_code AS MaNV, branch_code AS MaCN, full_name AS HoTen, position AS ChucVu, phone AS SoDienThoai, address AS DiaChi FROM staff;
GO
CREATE VIEW TaiKhoan AS SELECT username AS TenDangNhap, staff_code AS MaNV, password AS MatKhau, role AS VaiTro, status AS TrangThai FROM users;
GO
CREATE VIEW GiaiDau AS SELECT tournament_code AS MaGiaiDau, branch_code AS MaCN, tournament_name AS TenGiaiDau, start_date AS ThoiGianToChuc, rules AS TheLe, max_participants AS SoLuongToiDa, status AS TrangThai FROM tournaments;
GO
CREATE VIEW DangKyGiaiDau AS SELECT tournament_code AS MaGiaiDau, customer_code AS MaKH, registration_date AS NgayDangKy, status AS TrangThai FROM tournament_registrations;
GO
CREATE VIEW ThanhToan AS SELECT payment_code AS MaTT, booking_code AS MaDatSan, amount AS SoTien, method AS PhuongThuc, transaction_type AS LoaiGiaoDich, transaction_ref AS MaGiaoDich, payment_time AS ThoiGianThanhToan, status AS TrangThai FROM payments;
GO
CREATE VIEW PhanCa AS SELECT shift_code AS MaCa, staff_code AS MaNV, shift_date AS NgayLam, start_time AS GioBatDau, end_time AS GioKetThuc, status AS TrangThai FROM work_shifts;
GO
CREATE VIEW PhieuNhapKho AS SELECT receipt_code AS MaPN, branch_code AS MaCN, staff_code AS MaNV, receipt_date AS NgayNhap, total_amount AS TongTien, notes AS GhiChu FROM stock_receipts;
GO
CREATE VIEW ChiTietNhapDungCu AS SELECT receipt_code AS MaPN, equipment_code AS MaDungCu, quantity AS SoLuongNhap, import_price AS DonGiaNhap, total_amount AS ThanhTien FROM stock_equipment_details;
GO
CREATE VIEW ChiTietNhapSanPham AS SELECT receipt_code AS MaPN, product_code AS MaSP, quantity AS SoLuongNhap, import_price AS DonGiaNhap, total_amount AS ThanhTien FROM stock_product_details;
GO
CREATE VIEW KhuyenMai AS SELECT promo_code AS MaKM, promo_name AS TenKM, discount_type AS LoaiKM, discount_value AS GiaTriGiam, start_date AS NgayBatDau, end_date AS NgayKetThuc, scope AS PhamViApDung, status AS TrangThai FROM promotions;
GO
CREATE VIEW NhatKyHoatDong AS SELECT log_id AS MaLog, username AS TenDangNhap, action_type AS HanhDong, action_time AS ThoiGian, details AS NoiDung FROM audit_logs;
GO
CREATE VIEW TranDau AS SELECT match_code AS MaTran, tournament_code AS MaGiaiDau, court_code AS MaSan, player1_name AS MaKH1, player2_name AS MaKH2, match_time AS ThoiGianThiDau, result AS KetQua, status AS TrangThai FROM tournament_matches;
GO
CREATE VIEW CauHinhHeThong AS SELECT setting_code AS MaCauHinh, setting_name AS TenCauHinh, setting_value AS GiaTri, description AS MoTa, updated_at AS NgayCapNhat FROM system_settings;
GO
CREATE VIEW DeXuatGia AS SELECT proposal_code AS MaDeXuat, branch_code AS MaCN, proposer_name AS NguoiDeXuat, court_type AS LoaiSan, slot_info AS KhungGio, current_price AS GiaHienTai, proposed_price AS GiaDeXuat, reason AS LyDo, status AS TrangThai, created_at AS NgayDeXuat, approved_at AS NgayPheDuyet, note AS GhiChuPheDuyet FROM pricing_proposals;
GO

-- =====================================================================

-- 5. DỮ LIỆU THỰC TẾ SAO LƯU
EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all";
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: branches (4 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT branches ON;
INSERT INTO branches (TOTAL_COURTS, ID, BRANCH_CODE, PHONE, STATUS, BRANCH_NAME, MANAGER_NAME, ADDRESS) VALUES
(8, 1, N'CN01', N'0903123456', N'Hoạt động', N'Chi nhánh 1 - Sân Cầu Lông Thủ Đức', N'Trần Phúc Bảo', N'Số 01 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức'),
(6, 2, N'CN02', N'0903333444', N'Hoạt động', N'Chi nhánh 2 - Sân Cầu Lông Bình Thạnh', N'Trần Phúc Bảo', N'Số 234 Điện Biên Phủ, Phường 25, Bình Thạnh'),
(6, 3, N'CN03', N'0903789012', N'Hoạt động', N'Chi nhánh 3 - Sân Cầu Lông Quận 9', N'Trần Hữu Nam', N'Số 70 Đỗ Xuân Hợp, Phước Long B, TP. Thủ Đức'),
(6, 4, N'CN04', N'0903555777', N'Hoạt động', N'Chi nhánh 4 - Sân Cầu Lông Gò Vấp', N'Lê Hoàng Khang', N'Số 18 Quang Trung, Phường 10, Gò Vấp');
SET IDENTITY_INSERT branches OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: courts (26 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT courts ON;
INSERT INTO courts (HOURLY_RATE, ID, BRANCH_CODE, COURT_CODE, STATUS, COURT_TYPE, COURT_NAME, IMAGE_URL) VALUES
(120000.00, 1, N'CN01', N'CL01', N'Trống', N'Cầu lông VIP', N'Sân cầu lông 1 (VIP)', N'/images/badminton-court.jpg'),
(90000.00, 2, N'CN01', N'CL02', N'Đang sử dụng', N'Cầu lông tiêu chuẩn', N'Sân cầu lông 2', N'/images/badminton-court.jpg'),
(90000.00, 3, N'CN01', N'CL03', N'Đang sử dụng', N'Cầu lông tiêu chuẩn', N'Sân cầu lông 3', N'/images/badminton-court.jpg'),
(90000.00, 4, N'CN01', N'CL04', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân cầu lông 4', N'/images/badminton-court.jpg'),
(90000.00, 5, N'CN01', N'CL05', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân cầu lông 5', N'/images/badminton-court.jpg'),
(120000.00, 6, N'CN01', N'CL06', N'Bảo trì', N'Cầu lông VIP', N'Sân cầu lông 6 (VIP)', N'/images/badminton-court.jpg'),
(90000.00, 7, N'CN01', N'CL07', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân cầu lông 7', N'/images/badminton-court.jpg'),
(90000.00, 8, N'CN01', N'CL08', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân cầu lông 8', N'/images/badminton-court.jpg'),
(120000.00, 9, N'CN02', N'BT01', N'Trống', N'Cầu lông VIP', N'Sân BT 1 (VIP)', N'/images/badminton-court.jpg'),
(90000.00, 10, N'CN02', N'BT02', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân BT 2', N'/images/badminton-court.jpg'),
(90000.00, 11, N'CN02', N'BT03', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân BT 3', N'/images/badminton-court.jpg'),
(90000.00, 12, N'CN02', N'BT04', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân BT 4', N'/images/badminton-court.jpg'),
(90000.00, 13, N'CN02', N'BT05', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân BT 5', N'/images/badminton-court.jpg'),
(120000.00, 14, N'CN02', N'BT06', N'Trống', N'Cầu lông VIP', N'Sân BT 6 (VIP)', N'/images/badminton-court.jpg'),
(120000.00, 15, N'CN03', N'Q901', N'Trống', N'Cầu lông VIP', N'Sân Q9 1 (VIP)', N'/images/badminton-court.jpg'),
(90000.00, 16, N'CN03', N'Q902', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân Q9 2', N'/images/badminton-court.jpg'),
(90000.00, 17, N'CN03', N'Q903', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân Q9 3', N'/images/badminton-court.jpg'),
(90000.00, 18, N'CN03', N'Q904', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân Q9 4', N'/images/badminton-court.jpg'),
(90000.00, 19, N'CN03', N'Q905', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân Q9 5', N'/images/badminton-court.jpg'),
(90000.00, 20, N'CN03', N'Q906', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân Q9 6', N'/images/badminton-court.jpg'),
(120000.00, 21, N'CN04', N'GV01', N'Trống', N'Cầu lông VIP', N'Sân GV 1 (VIP)', N'/images/badminton-court.jpg'),
(90000.00, 22, N'CN04', N'GV02', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân GV 2', N'/images/badminton-court.jpg'),
(90000.00, 23, N'CN04', N'GV03', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân GV 3', N'/images/badminton-court.jpg'),
(90000.00, 24, N'CN04', N'GV04', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân GV 4', N'/images/badminton-court.jpg'),
(90000.00, 25, N'CN04', N'GV05', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân GV 5', N'/images/badminton-court.jpg'),
(90000.00, 26, N'CN04', N'GV06', N'Trống', N'Cầu lông tiêu chuẩn', N'Sân GV 6', N'/images/badminton-court.jpg');
SET IDENTITY_INSERT courts OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: time_slots (4 bản ghi)
-- -----------------------------------------------------
INSERT INTO time_slots (END_TIME, START_TIME, SLOT_CODE, SLOT_TYPE) VALUES
(N'09:00', N'06:00', N'KG01', N'Thường (Sáng sớm)'),
(N'17:00', N'09:00', N'KG02', N'Thường (Ban ngày)'),
(N'22:00', N'17:00', N'KG03', N'Giờ vàng cao điểm'),
(N'23:00', N'22:00', N'KG04', N'Tối muộn');
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: pricing_rules (11 bản ghi)
-- -----------------------------------------------------
INSERT INTO pricing_rules (END_DATE, HOURLY_RATE, START_DATE, BRANCH_CODE, PRICING_CODE, SLOT_CODE, DAY_TYPE) VALUES
(NULL, 80000.00, N'2026-01-01', N'CN01', N'BG01', N'KG01', N'Ngày thường (T2-T6)'),
(NULL, 90000.00, N'2026-01-01', N'CN01', N'BG02', N'KG02', N'Ngày thường (T2-T6)'),
(NULL, 130000.00, N'2026-01-01', N'CN01', N'BG03', N'KG03', N'Ngày thường (T2-T6)'),
(NULL, 90000.00, N'2026-01-01', N'CN01', N'BG04', N'KG04', N'Ngày thường (T2-T6)'),
(NULL, 110000.00, N'2026-01-01', N'CN01', N'BG05', N'KG01', N'Cuối tuần (T7-CN)'),
(NULL, 120000.00, N'2026-01-01', N'CN01', N'BG06', N'KG02', N'Cuối tuần (T7-CN)'),
(NULL, 150000.00, N'2026-01-01', N'CN01', N'BG07', N'KG03', N'Cuối tuần (T7-CN)'),
(NULL, 110000.00, N'2026-01-01', N'CN01', N'BG08', N'KG04', N'Cuối tuần (T7-CN)'),
(NULL, 125000.00, N'2026-01-01', N'CN02', N'BG09', N'KG03', N'Ngày thường (T2-T6)'),
(NULL, 120000.00, N'2026-01-01', N'CN03', N'BG10', N'KG03', N'Ngày thường (T2-T6)'),
(NULL, 120000.00, N'2026-01-01', N'CN04', N'BG11', N'KG03', N'Ngày thường (T2-T6)');
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: customers (8 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT customers ON;
INSERT INTO customers (REWARD_POINTS, TOTAL_PLAY_HOURS, CREATED_AT, ID, CUSTOMER_CODE, PHONE, MEMBERSHIP_TIER, STATUS, EMAIL, FULL_NAME, PASSWORD, RAW_PASSWORD) VALUES
(4250, 42.50, N'2026-10-25 22:48:48.838879', 1, N'KH001', N'0912345678', N'Kim Cương', N'Hoạt động', N'dat.le@gmail.com', N'Lê Bá Đạt', N'123456', NULL),
(2800, 28.00, N'2026-10-25 22:48:48.838879', 2, N'KH002', N'0903123456', N'Vàng', N'Hoạt động', N'nam.nguyen@gmail.com', N'Nguyễn Hoàng Nam', N'123456', NULL),
(1500, 15.00, N'2026-10-25 22:48:48.838879', 3, N'KH003', N'0988777666', N'Bạc', N'Hoạt động', N'huy.pham@gmail.com', N'Phạm Gia Huy', N'123456', NULL),
(800, 8.00, N'2026-10-25 22:48:48.838879', 4, N'KH004', N'0977555444', N'Bạc', N'Hoạt động', N'maianh.vu@gmail.com', N'Vũ Thị Mai Anh', N'123456', NULL),
(400, 4.00, N'2026-10-25 22:48:48.838879', 5, N'KH005', N'0933444555', N'Đồng', N'Hoạt động', N'hang.tran@gmail.com', N'Trần Thanh Hằng', N'123456', NULL),
(350, 3.50, N'2026-10-25 22:48:48.838879', 6, N'KH006', N'0909111222', N'Đồng', N'Hoạt động', N'linh.bui@gmail.com', N'Bùi Khánh Linh', N'123456', NULL),
(0, 0.00, N'2026-10-25 22:48:59.650406', 7, N'KH29936', N'0908123456', N'Đồng', N'Hoạt động', NULL, N'Nguyen Van Test', N'123456', NULL),
(150, 0.00, N'2026-10-25 22:49:00.562359', 12, N'KH9999', N'0987654321', N'Vàng', N'Hoạt động', N'triet.test@utesport.vn', N'Nguyễn Văn Triết', N'$2a$10$abcdefghijklmnopqrstuvwxyz1234567890ABCDEF', NULL);
SET IDENTITY_INSERT customers OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: staff (14 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT staff ON;
INSERT INTO staff (ID, BRANCH_CODE, PHONE, STAFF_CODE, EMAIL, ADDRESS, FULL_NAME, POSITION, STATUS) VALUES
(1, N'ALL', N'0901111222', N'NS01', N'nam.vh@utesport.vn', N'Quận 1, TP. Hồ Chí Minh', N'Võ Hoàng Nam', N'Giám đốc điều hành', N'Đang làm việc'),
(2, N'CN01', N'0903123456', N'NS02', N'bao.tran@utesport.vn', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Trần Phúc Bảo', N'Quản lý chi nhánh Thủ Đức', N'Đang làm việc'),
(3, N'CN01', N'0904555666', N'NS03', N'tam.nguyen@utesport.vn', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Nguyễn Thanh Tâm', N'Thu ngân quầy POS', N'Đang làm việc'),
(4, N'CN01', N'0904777888', N'NS04', N'mai.lt@utesport.vn', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Lê Thị Mai', N'Thu ngân quầy POS', N'Đang làm việc'),
(5, N'CN02', N'0903333444', N'NS05', N'quan.nv@utesport.vn', N'Bình Thạnh, TP. Hồ Chí Minh', N'Nguyễn Văn Quản', N'Quản lý chi nhánh Bình Thạnh', N'Đang làm việc'),
(6, N'CN03', N'0903789012', N'NS06', N'nam.th@utesport.vn', N'Quận 9, TP. Hồ Chí Minh', N'Trần Hữu Nam', N'Quản lý chi nhánh Quận 9', N'Đang làm việc'),
(7, N'CN04', N'0903555777', N'NS07', N'khang.lh@utesport.vn', N'Gò Vấp, TP. Hồ Chí Minh', N'Lê Hoàng Khang', N'Quản lý chi nhánh Gò Vấp', N'Đang làm việc'),
(8, N'CN04', N'0908111333', N'NS08', N'hung.lv@utesport.vn', N'Gò Vấp, TP. Hồ Chí Minh', N'Lê Văn Hùng', N'Nhân viên kỹ thuật sân', N'Đang làm việc'),
(9, N'CN01', N'0909111222', N'NS09', N'linh.bk@utesport.vn', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Bùi Khánh Linh', N'Lễ tân & Dịch vụ', N'Đang làm việc'),
(10, N'CN01', N'0903888777', N'NS10', N'nam.nh@utesport.vn', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Nguyễn Hoàng Nam', N'Thu ngân POS quầy', N'Đang làm việc'),
(11, N'CN01', N'0988777666', N'NS11', N'huy.pg@utesport.vn', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Phạm Gia Huy', N'Kỹ thuật & Bảo dưỡng sân', N'Đang làm việc'),
(12, N'CN01', N'0977222333', N'NS12', N'vinh.tg@utesport.vn', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Trịnh Gia Vinh', N'Kỹ thuật & Bảo dưỡng sân', N'Đang làm việc'),
(13, N'CN01', N'0909999888', N'NV-TEST-01', N'test@utesport.vn', NULL, N'Nhan Vien Test', N'Thu ngan POS quầy', N'Đang làm việc'),
(14, N'CN01', N'0903000000', N'NVQ01', N'nvq01@utesport.vn', NULL, N'Nhân viên NVQ01', N'Thu ngân POS quầy', N'Đang làm việc');
SET IDENTITY_INSERT staff OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: users (25 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT users ON;
INSERT INTO users (CREATED_AT, ID, PHONE, STAFF_CODE, ROLE, STATUS, USERNAME, BRANCH_CODE, EMAIL, FULL_NAME, PASSWORD, RAW_PASSWORD) VALUES
(N'2026-10-25 22:48:48.895397', 1, N'0901111222', N'NS01', N'DIRECTOR', N'Hoạt động', N'director', N'ALL', N'director@utesport.vn', N'Nguyễn Phước Thọ', N'$2a$10$sgQdSyzPVRMhZKGEfizLYOZZsnDAltWlQ.TZ.e5SuR5oKBlmUueny', NULL),
(N'2026-10-25 22:48:48.973207', 2, N'0903123456', N'NS02', N'MANAGER', N'Hoạt động', N'manager', N'CN01,CN02', N'manager@utesport.vn', N'Trần Phúc Bảo', N'$2a$10$ibQbUELBuJd44BoimmGgsOwqAqsWsiMXkq4BiJ/MC.dBIqcJIY9NW', NULL),
(N'2026-10-25 22:48:49.038006', 3, N'0904555666', N'NS03', N'POS', N'Hoạt động', N'pos', N'CN01', N'pos@utesport.vn', N'Nguyễn Thanh Tâm', N'$2a$10$ixWyC9IhqTKd020z3z5jfucNsrxG3L4g9ncF.egpca0Jc.jNw5Skm', NULL),
(N'2026-10-25 22:48:49.101518', 4, N'0908888999', NULL, N'ADMIN', N'Hoạt động', N'admin', N'ALL', N'admin@utesport.vn', N'Trần Biểu Hương', N'$2a$10$H3DISxopjX8O9emomQ9RJOAVy4kZiI896/gOfTou5ifEK0QW34QM2', NULL),
(N'2026-10-25 22:48:49.174196', 5, N'0912345678', NULL, N'CUSTOMER', N'Hoạt động', N'customer', N'CN01', N'khachhang@gmail.com', N'Lê Bá Đạt', N'$2a$10$ZVzY7r3DQFmWPOjj6ZAE8eqHFid5epqH4O1dtFv.uJFGj/BB3rLBS', NULL),
(N'2026-10-25 22:48:49.239833', 6, N'0901111222', N'NS01', N'DIRECTOR', N'Hoạt động', N'nam.vh', N'ALL', N'nam.vh@utesport.vn', N'Võ Hoàng Nam', N'$2a$10$rte.rvyWOJhkMXM34ELqV.ceSx9aJ7Z2Fr.mV1ybxL3QcV5mSHLnu', NULL),
(N'2026-10-25 22:48:49.305318', 7, N'0903123456', N'NS02', N'MANAGER', N'Hoạt động', N'ql_cn01', N'CN01', N'bao.tran@utesport.vn', N'Trần Phúc Bảo', N'$2a$10$xxRCZGT88hmEG8RD8AFGDONZmTO8WI3ues7x2pioaJ7C9K9C4hsy2', NULL),
(N'2026-10-25 22:48:49.373975', 8, N'0903333444', N'NS05', N'MANAGER', N'Hoạt động', N'quan.nv', N'CN02', N'quan.nv@utesport.vn', N'Nguyễn Văn Quản', N'$2a$10$1HBD2fTjZPVac18GyY3gqO5u/7OjqmzPbMVKyiwB4hkgaN.3mhD3.', NULL),
(N'2026-10-25 22:48:49.445359', 9, N'0903333444', N'NS05', N'MANAGER', N'Hoạt động', N'ql_cn02', N'CN02', N'quan.nv.mgr@utesport.vn', N'Nguyễn Văn Quản', N'$2a$10$QxkJ6bbHp5vDIaamHEz5NuPHBe/qTJqHTHn4UCiNoFNH0OI7mHV6.', NULL),
(N'2026-10-25 22:48:49.511085', 10, N'0903789012', N'NS06', N'MANAGER', N'Hoạt động', N'nam.th', N'CN03', N'nam.th@utesport.vn', N'Trần Hữu Nam', N'$2a$10$RP7UlYPrY1xAUeTPzznrC.i0Gy1j49yTOCThIj9nQknb5PzNxnzAK', NULL),
(N'2026-10-25 22:48:49.575417', 11, N'0903789012', N'NS06', N'MANAGER', N'Hoạt động', N'ql_cn03', N'CN03', N'nam.th.mgr@utesport.vn', N'Trần Hữu Nam', N'$2a$10$ubt7YwSenKIM9aQm0/ICTu4gCDqfWR7iUZ2jVFnkdsuuKaQNLPGIC', NULL),
(N'2026-10-25 22:48:49.644144', 12, N'0903555777', N'NS07', N'MANAGER', N'Hoạt động', N'khang.lh', N'CN04', N'khang.lh@utesport.vn', N'Lê Hoàng Khang', N'$2a$10$E81IGDBYsVtoYZPzLMH/0eGSIdcsAYXxTOyU1L8jRdQFMlwGTPelq', NULL),
(N'2026-10-25 22:48:49.707004', 13, N'0903555777', N'NS07', N'MANAGER', N'Hoạt động', N'ql_cn04', N'CN04', N'khang.lh.mgr@utesport.vn', N'Lê Hoàng Khang', N'$2a$10$y1M/ljm66JHUpyefBjwgROBceaWJnsfMCxbitIVF8gqLHnNoVOgv.', NULL),
(N'2026-10-25 22:48:49.780103', 14, N'0904777888', N'NS04', N'POS', N'Hoạt động', N'mai.lt', N'CN01', N'mai.lt@utesport.vn', N'Lê Thị Mai', N'$2a$10$lcfXmNrZyinTLUk1W//xCOihCWcBIQtFbWrHMP6QQSUq1w3wa5phO', NULL),
(N'2026-10-25 22:48:49.848654', 15, N'0903999888', N'NS04', N'POS', N'Hoạt động', N'nv_cn01', N'CN01', N'mai.nguyen@utesport.vn', N'Nguyễn Thị Mai', N'$2a$10$JRwVXeJ/2IXqF9/UWI8Be.rvRfHf4GH/7dtE07OD64zyADV.Nz4Oa', NULL),
(N'2026-10-25 22:48:49.911901', 16, N'0904555666', N'NS03', N'POS', N'Hoạt động', N'tam.nguyen', N'CN01', N'tam.nguyen@utesport.vn', N'Nguyễn Thanh Tâm', N'$2a$10$x1jln3d2bEmnqFVewhwb.eU/gdHuPwz5vTzABBGg14H42tF6jhtjO', NULL),
(N'2026-10-25 22:48:49.977061', 17, N'0903888777', N'NS10', N'POS', N'Hoạt động', N'nam.nh', N'CN01', N'nam.nh@utesport.vn', N'Nguyễn Hoàng Nam', N'$2a$10$ZISJZ6DENvjNtHMz6Ixzou8xriiuet4M9Ho9TJO2W0HND7u.rcWKe', NULL),
(N'2026-10-25 22:48:50.040344', 18, N'0988777666', N'NS11', N'POS', N'Hoạt động', N'huy.pg', N'CN01', N'huy.pg@utesport.vn', N'Phạm Gia Huy', N'$2a$10$kX.py7.b7tfpREyF2fDLCeO6kEXZ6L893MuZLbirqKptBSRHrj/ja', NULL),
(N'2026-10-25 22:48:50.105242', 19, N'0908111222', NULL, N'POS', N'Hoạt động', N'nv_cn02', N'CN02', N'quy.le@utesport.vn', N'Lê Văn Quý', N'$2a$10$xhOQChZ4LpK50m4bvjU9yuNoznIPbSqMUypsQw/nFvm4EVeVSq98K', NULL),
(N'2026-10-25 22:48:50.180433', 20, N'0909555444', NULL, N'POS', N'Hoạt động', N'nv_cn03', N'CN03', N'lan.hoang@utesport.vn', N'Hoàng Thị Lan', N'$2a$10$FMe6KHxgrseadeM1c4eX7OZaSPz8cycNrEKJGtdZg.dVPzPOc8Olu', NULL),
(N'2026-10-25 22:48:50.259096', 21, N'0908111333', N'NS08', N'POS', N'Hoạt động', N'nv_cn04', N'CN04', N'hung.lv@utesport.vn', N'Lê Văn Hùng', N'$2a$10$lPotmPBmFFpgla3Adkn7tObXfNYp4J87USKeoFNn8lS7LOioePUju', NULL),
(N'2026-10-25 22:48:59.141302', 22, NULL, NULL, N'CUSTOMER', N'Hoạt động', N'toggle_test_user', NULL, NULL, N'User Can Khoa', N'Pass123456', NULL),
(N'2026-10-25 22:48:59.28834', 23, NULL, NULL, N'MANAGER', N'Hoạt động', N'perm_test_user', N'CN02', NULL, N'Perm Test User', N'Pass123456', NULL),
(N'2026-10-25 22:48:59.315595', 24, N'0911223344', NULL, N'POS', N'Hoạt động', N'test_staff_day05', N'CN01', NULL, N'Nguyen Van Staff Updated', N'$2a$10$K/3qBDFjqr0/gZBzG0IsNeIcubvXsNGZ9YSLrlsVO2rydPi6vqp2m', NULL),
(N'2026-10-25 22:49:00.561259', 29, N'0987654321', NULL, N'CUSTOMER', N'Hoạt động', N'cust_profile_test', N'CN01', N'triet.test@utesport.vn', N'Nguyễn Văn Triết', N'$2a$10$abcdefghijklmnopqrstuvwxyz1234567890ABCDEF', NULL);
SET IDENTITY_INSERT users OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: equipment (9 bản ghi)
-- -----------------------------------------------------
INSERT INTO equipment (RENTAL_PRICE, STOCK_QUANTITY, BRANCH_CODE, EQUIPMENT_CODE, CONDITION, EQUIPMENT_NAME) VALUES
(25000.00, 18, N'CN01', N'DC01', N'Mới 99%', N'Vợt Yonex Astrox 77 Play (4U/G5)'),
(35000.00, 12, N'CN01', N'DC02', N'Tốt', N'Vợt Yonex Astrox 88D Tour'),
(30000.00, 10, N'CN01', N'DC03', N'Tốt', N'Vợt Victor Thruster K 9900'),
(30000.00, 8, N'CN01', N'DC04', N'Tốt', N'Vợt Lining Axforce 80'),
(180000.00, 45, N'CN01', N'DC05', N'Mới nguyên hộp', N'Ống Cầu Lông Hải Yến Đỏ (12 quả)'),
(210000.00, 35, N'CN01', N'DC06', N'Mới nguyên hộp', N'Ống Cầu Lông Thành Công 77'),
(25000.00, 15, N'CN02', N'DC07', N'Tốt', N'Vợt Yonex Astrox 77 Play'),
(25000.00, 15, N'CN03', N'DC08', N'Tốt', N'Vợt Yonex Astrox 77 Play'),
(25000.00, 15, N'CN04', N'DC09', N'Tốt', N'Vợt Yonex Astrox 77 Play');
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: products (11 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT products ON;
INSERT INTO products (COST_PRICE, MIN_QUANTITY, STOCK_QUANTITY, UNIT_PRICE, ID, BRANCH_CODE, PRODUCT_CODE, UNIT, CATEGORY, PRODUCT_NAME) VALUES
(8437.50, 20, 160, 15000.00, 1, N'CN01', N'P01', N'Chai', N'Nước giải khát', N'Nước bù khoáng Revive Chanh muối 500ml'),
(11000.00, 20, 120, 18000.00, 2, N'CN01', N'P02', N'Chai', N'Nước giải khát', N'Nước điện giải Pocari Sweat 500ml'),
(5000.00, 30, 220, 10000.00, 3, N'CN01', N'P03', N'Chai', N'Nước giải khát', N'Nước suối tinh khiết Aquafina 500ml'),
(12000.00, 15, 95, 20000.00, 4, N'CN01', N'P04', N'Lon', N'Nước giải khát', N'Nước tăng lực Bò Húc Red Bull Thái'),
(145000.00, 10, 65, 180000.00, 5, N'CN01', N'P05', N'Ống', N'Cầu lông', N'Ống Cầu Lông Hải Yến Đỏ (12 quả)'),
(175000.00, 10, 40, 210000.00, 6, N'CN01', N'P06', N'Ống', N'Cầu lông', N'Ống Cầu Lông Thành Công 77 (12 quả)'),
(7000.00, 15, 85, 15000.00, 7, N'CN01', N'P07', N'Cái', N'Phụ kiện', N'Quấn cán vợt cầu lông VS chống trượt'),
(25000.00, 5, 30, 45000.00, 8, N'CN01', N'P08', N'Cái', N'Phụ kiện', N'Khăn lau mồ hôi thể thao Yonex'),
(8000.00, 20, 110, 15000.00, 9, N'CN02', N'P09', N'Chai', N'Nước giải khát', N'Nước bù khoáng Revive Chanh muối 500ml'),
(8000.00, 20, 90, 15000.00, 10, N'CN03', N'P10', N'Chai', N'Nước giải khát', N'Nước bù khoáng Revive Chanh muối 500ml'),
(8000.00, 20, 100, 15000.00, 11, N'CN04', N'P11', N'Chai', N'Nước giải khát', N'Nước bù khoáng Revive Chanh muối 500ml');
SET IDENTITY_INSERT products OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: bookings (28 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT bookings ON;
INSERT INTO bookings (BOOKING_DATE, DEPOSIT_AMOUNT, HOURLY_PRICE, TOTAL_PRICE, CHECK_IN_TIME, CHECK_OUT_TIME, CREATED_AT, ID, BRANCH_CODE, COURT_CODE, CUSTOMER_CODE, CUSTOMER_PHONE, BOOKING_CODE, PAYMENT_METHOD, STATUS, TIME_SLOT, CUSTOMER_EMAIL, CUSTOMER_NAME, NOTES) VALUES
(N'2026-10-25', 54000.00, 90000.00, 180000.00, NULL, N'2026-10-25 22:48:58.262602', N'2026-10-25 22:48:50.428339', 1, N'CN01', N'CL02', N'KH002', N'0903123456', N'DS0101', N'Tiền mặt', N'Hoàn thành', N'08:00 - 10:00', NULL, N'Nguyễn Hoàng Nam', N'Khách quen tuần 3 buổi'),
(N'2026-10-25', 36000.00, 120000.00, 120000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 2, N'CN01', N'CL01', N'KH001', N'0912345678', N'DS0102', N'Tiền mặt', N'Hoàn thành', N'09:00 - 10:00', NULL, N'Lê Bá Đạt', N'Đã thanh toán đủ tiền mặt'),
(N'2026-10-25', 72000.00, 120000.00, 240000.00, NULL, N'2026-10-25 22:48:58.262602', N'2026-10-25 22:48:50.428339', 3, N'CN01', N'CL05', N'KH003', N'0988777666', N'DS0103', N'Chuyển khoản VietQR', N'Hoàn thành', N'18:00 - 20:00', NULL, N'Phạm Gia Huy', N'Đặt sân VIP tối'),
(N'2026-10-25', 27000.00, 90000.00, 90000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 4, N'CN01', N'CL03', N'KH004', N'0977555444', N'DS0104', N'VietQR Pro', N'Chờ xác nhận', N'19:00 - 20:00', NULL, N'Vũ Thị Mai Anh', N'Khách đặt qua web portal'),
(N'2026-10-26', 36000.00, 120000.00, 120000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 5, N'CN01', N'CL01', N'KH005', N'0933444555', N'DS0105', N'Chuyển khoản VietQR', N'Đã cọc 30%', N'17:00 - 18:00', NULL, N'Trần Thanh Hằng', N'Chuyển khoản VietQR giữ chỗ 10p'),
(N'2026-10-26', 54000.00, 90000.00, 180000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 6, N'CN01', N'CL04', N'KH006', N'0909111222', N'DS0106', N'Tiền mặt', N'Đã xác nhận', N'20:00 - 22:00', NULL, N'Bùi Khánh Linh', N'Giao lưu công ty FPT'),
(N'2026-10-24', 0.00, 90000.00, 90000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 7, N'CN01', N'CL02', N'KH002', N'0944333222', N'DS0107', N'Tiền mặt', N'Đã hủy', N'15:00 - 16:00', NULL, N'Đỗ Quang Vinh', N'Bận công tác đột xuất'),
(N'2026-10-25', 72000.00, 120000.00, 240000.00, NULL, N'2026-10-25 22:48:58.262602', N'2026-10-25 22:48:50.428339', 8, N'CN02', N'BT01', N'KH001', N'0912345678', N'DS0201', N'Chuyển khoản VietQR', N'Hoàn thành', N'18:00 - 20:00', NULL, N'Lê Bá Đạt', N'Sân VIP Bình Thạnh'),
(N'2026-10-25', 54000.00, 90000.00, 180000.00, NULL, N'2026-10-25 22:48:58.262602', N'2026-10-25 22:48:50.428339', 9, N'CN02', N'BT04', N'KH003', N'0988777666', N'DS0202', N'Tiền mặt', N'Hoàn thành', N'19:00 - 21:00', NULL, N'Phạm Gia Huy', N'Tập luyện đôi nam'),
(N'2026-10-25', 54000.00, 90000.00, 180000.00, NULL, N'2026-10-25 22:48:58.262602', N'2026-10-25 22:48:50.428339', 10, N'CN03', N'Q902', N'KH004', N'0977555444', N'DS0301', N'VietQR Pro', N'Hoàn thành', N'17:00 - 19:00', NULL, N'Vũ Thị Mai Anh', N'Nhóm bạn sinh viên'),
(N'2026-10-25', 54000.00, 90000.00, 180000.00, NULL, N'2026-10-25 22:48:58.262602', N'2026-10-25 22:48:50.428339', 11, N'CN04', N'GV02', N'KH005', N'0933444555', N'DS0401', N'Tiền mặt', N'Hoàn thành', N'18:00 - 20:00', NULL, N'Trần Thanh Hằng', N'Khách vãng lai chi nhánh GV'),
(N'2026-10-20', 72000.00, 120000.00, 240000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 12, N'CN01', N'CL01', N'KH001', N'0912345678', N'DS01_1001_1', N'Tiền mặt', N'Hoàn thành', N'08:00 - 10:00', NULL, N'Lê Bá Đạt', N'Khách quen tập sáng'),
(N'2026-10-20', 54000.00, 90000.00, 180000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 13, N'CN01', N'CL03', N'KH003', N'0988777666', N'DS01_1001_2', N'VietQR Pro', N'Hoàn thành', N'17:00 - 19:00', NULL, N'Phạm Gia Huy', N'Đặt sân sau giờ làm'),
(N'2026-10-21', 54000.00, 90000.00, 180000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 14, N'CN01', N'CL02', N'KH002', N'0903123456', N'DS01_1002_1', N'Tiền mặt', N'Hoàn thành', N'09:00 - 11:00', NULL, N'Nguyễn Hoàng Nam', N'Tập thể lực'),
(N'2026-10-21', 54000.00, 90000.00, 180000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 15, N'CN01', N'CL04', N'KH005', N'0933444555', N'DS01_1002_2', N'VietQR Pro', N'Hoàn thành', N'18:00 - 20:00', NULL, N'Trần Thanh Hằng', N'CLB Thủ Đức'),
(N'2026-10-22', 72000.00, 120000.00, 240000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 16, N'CN01', N'CL01', N'KH001', N'0912345678', N'DS01_1003_1', N'Chuyển khoản VietQR', N'Hoàn thành', N'15:00 - 17:00', NULL, N'Lê Bá Đạt', N'Tập đôi nam'),
(N'2026-10-22', 54000.00, 90000.00, 180000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 17, N'CN01', N'CL05', N'KH003', N'0988777666', N'DS01_1003_2', N'VietQR Pro', N'Hoàn thành', N'17:00 - 19:00', NULL, N'Phạm Gia Huy', N'Khách vãng lai'),
(N'2026-10-23', 54000.00, 90000.00, 180000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 18, N'CN01', N'CL02', N'KH002', N'0903123456', N'DS01_1004_1', N'Tiền mặt', N'Hoàn thành', N'08:00 - 10:00', NULL, N'Nguyễn Hoàng Nam', N'Khách cố định'),
(N'2026-10-23', 54000.00, 90000.00, 180000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 19, N'CN01', N'CL03', N'KH005', N'0933444555', N'DS01_1004_2', N'VietQR Pro', N'Hoàn thành', N'10:00 - 12:00', NULL, N'Trần Thanh Hằng', N'Tập kỹ thuật'),
(N'2026-10-23', 72000.00, 120000.00, 240000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 20, N'CN01', N'CL01', N'KH001', N'0912345678', N'DS01_1005_1', N'VietQR Pro', N'Hoàn thành', N'18:00 - 20:00', NULL, N'Lê Bá Đạt', N'Tối thứ 2'),
(N'2026-10-24', 54000.00, 90000.00, 180000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 21, N'CN01', N'CL02', N'KH002', N'0903123456', N'DS01_1006_1', N'Tiền mặt', N'Hoàn thành', N'17:00 - 19:00', NULL, N'Nguyễn Hoàng Nam', N'Tập phát cầu'),
(N'2026-10-24', 72000.00, 120000.00, 240000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 22, N'CN01', N'CL01', N'KH001', N'0912345678', N'DS01_1007_1', N'Chuyển khoản VietQR', N'Hoàn thành', N'09:00 - 11:00', NULL, N'Lê Bá Đạt', N'Khách quen VIP'),
(N'2026-10-24', 54000.00, 90000.00, 180000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 23, N'CN01', N'CL02', N'KH002', N'0903123456', N'DS01_1008_1', N'Tiền mặt', N'Hoàn thành', N'18:00 - 20:00', NULL, N'Nguyễn Hoàng Nam', N'Định kỳ thứ 5'),
(N'2026-10-24', 72000.00, 120000.00, 240000.00, NULL, NULL, N'2026-10-25 22:48:50.428339', 24, N'CN01', N'CL01', N'KH001', N'0912345678', N'DS01_1009_1', N'Chuyển khoản VietQR', N'Hoàn thành', N'17:00 - 19:00', NULL, N'Lê Bá Đạt', N'VIP Thứ 6'),
(N'2026-10-25', 30000.00, 100000.00, 100000.00, NULL, NULL, N'2026-10-25 22:49:00.034687', 25, N'CN01', N'CL03', NULL, N'0905556667', N'DS623515', N'Chuyển khoản VietQR', N'Đã cọc 30%', N'10:00 - 11:00', NULL, N'Khach Webhook', NULL),
(N'2026-11-24', 0.00, 90000.00, 90000.00, NULL, NULL, N'2026-10-25 22:49:00.105804', 26, N'CN01', N'CL02', NULL, N'0903334445', N'DS_API_TRF_105', N'Tiền mặt', N'Đang sử dụng', N'19:00 - 20:00', NULL, N'Khach Chuyen San', N'Chuyển từ CL01 sang CL02 (Chênh lệch: 0.00đ)'),
(N'2026-11-14', 30000.00, 100000.00, 100000.00, NULL, NULL, N'2026-10-25 22:49:00.219225', 27, N'CN01', N'CL02', NULL, N'0901112223', N'DS_CONFLICT_TEST_219', N'Tiền mặt', N'Chờ cọc 30%', N'07:00 - 08:00', NULL, N'Khach Hang Cu', NULL),
(N'2026-12-14', 36000.00, 120000.00, 120000.00, NULL, NULL, N'2026-10-25 22:49:00.38129', 28, N'CN01', N'CL01', NULL, N'0908888777', N'DS562219', N'Chuyển khoản VietQR', N'Chờ cọc 30%', N'06:00 - 07:00', NULL, N'Nguyen Van Test', N'Đặt qua Lưới giờ Sân trực tuyến');
SET IDENTITY_INSERT bookings OFF;
GO

-- Lỗi trích xuất bảng booking_services: Table "BOOKING_SERVICES" not found; SQL statement:
SELECT * FROM booking_services [42102-240]

-- Bảng invoices: Không có dữ liệu.

-- Lỗi trích xuất bảng invoice_equipment_details: Table "INVOICE_EQUIPMENT_DETAILS" not found; SQL statement:
SELECT * FROM invoice_equipment_details [42102-240]

-- Lỗi trích xuất bảng invoice_product_details: Table "INVOICE_PRODUCT_DETAILS" not found; SQL statement:
SELECT * FROM invoice_product_details [42102-240]


-- -----------------------------------------------------
-- Dữ liệu bảng: tournaments (5 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT tournaments ON;
INSERT INTO tournaments (CURRENT_PARTICIPANTS, ENTRY_FEE, MAX_PARTICIPANTS, TOTAL_PRIZE, END_DATE, ID, START_DATE, BRANCH_CODE, TOURNAMENT_CODE, CATEGORY, STATUS, TOURNAMENT_NAME, RULES) VALUES
(24, 300000.00, 32, 20000000.00, N'2026-10-12 18:00:00.0', 1, N'2026-10-10 08:00:00.0', N'CN01', N'GD01', N'Đôi nam nữ', N'Đã kết thúc', N'Giải Cầu Lông Đôi Nam Nữ Mùa Thu UTE Open 2026', N'clob9: U&''Thi \0111\1ea5u theo th\1ec3 th\1ee9c lo\1ea1i tr\1ef1c ti\1ebfp 3 hi\1ec7p 21 \0111i\1ec3m theo lu\1eadt BWF. M\1ed7i V\0110V ch\1ec9 \0111\01b0\1ee3c \0111\0103ng k\00fd 1 n\1ed9i dung.'''),
(9, 250000.00, 24, 15000000.00, N'2026-10-26 17:00:00.0', 2, N'2026-10-25 08:00:00.0', N'CN01', N'GD02', N'Tổng hợp nhiều nội dung', N'Đang mở đăng ký', N'Giải Cầu Lông Doanh Nghiệp & Sinh Viên Cúp Thủ Đức 2026', N'clob10: U&''Gi\1ea3i \0111\1ea5u phong tr\00e0o k\1ebft n\1ed1i c\00e1c doanh nghi\1ec7p v\00e0 c\1ef1u sinh vi\00ean \0111am m\00ea c\1ea7u l\00f4ng.'''),
(0, 200000.00, 16, 10000000.00, N'2026-11-06 18:00:00.0', 3, N'2026-11-05 08:00:00.0', N'CN02', N'GD03', N'Đơn nam', N'Sắp mở đăng ký', N'Giải Đơn Nam Bình Thạnh Mở Rộng 2026', N'clob11: U&''Gi\1ea3i \0111\01a1n nam c\00e1c CLB phong tr\00e0o khu v\1ef1c B\00ecnh Th\1ea1nh - Ph\00fa Nhu\1eadn.'''),
(4, 250000.00, 24, 20000000.00, N'2026-11-13 18:00:00.0', 4, N'2026-11-12 08:00:00.0', N'CN03', N'GD04', N'Đôi nam nữ', N'Sắp mở đăng ký', N'Giải Đôi Nam Nữ Trẻ Quận 9 Mở Rộng 2026', N'clob12: U&''Gi\1ea3i \0111\1ea5u phong tr\00e0o d\00e0nh cho thanh ni\00ean v\00e0 sinh vi\00ean khu v\1ef1c TP Th\1ee7 \0110\1ee9c - Qu\1eadn 9.'''),
(6, 200000.00, 20, 18000000.00, N'2026-11-21 18:00:00.0', 5, N'2026-11-20 08:00:00.0', N'CN04', N'GD05', N'Đôi nam', N'Sắp mở đăng ký', N'Giải Cầu Lông Giao Lưu Các CLB Gò Vấp 2026', N'clob13: U&''S\00e2n ch\01a1i k\1ebft n\1ed1i giao l\01b0u gi\1eefa c\00e1c c\00e2u l\1ea1c b\1ed9 c\1ea7u l\00f4ng khu v\1ef1c G\00f2 V\1ea5p - T\00e2n B\00ecnh.''');
SET IDENTITY_INSERT tournaments OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: tournament_registrations (6 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT tournament_registrations ON;
INSERT INTO tournament_registrations (ID, REGISTRATION_DATE, CUSTOMER_CODE, PHONE, TOURNAMENT_CODE, CATEGORY, STATUS, EMAIL, TEAM_NAME) VALUES
(1, N'2026-10-25 22:48:50.517087', N'KH001', N'0912345678', N'GD01', N'Đôi nam nữ', N'Đã xác nhận', NULL, N'Cặp Đôi Đạt & Trang (CLB Thủ Đức)'),
(2, N'2026-10-25 22:48:50.517087', N'KH002', N'0903123456', N'GD01', N'Đôi nam nữ', N'Đã xác nhận', NULL, N'Cặp Đôi Nam & Thảo (ĐH Sư Phạm Kỹ Thuật)'),
(3, N'2026-10-25 22:48:50.517087', N'KH003', N'0988777666', N'GD01', N'Đôi nam nữ', N'Đã xác nhận', NULL, N'Cặp Đôi Huy & Hằng (Công ty TMA)'),
(4, N'2026-10-25 22:48:50.517087', N'KH004', N'0977555444', N'GD01', N'Đôi nam nữ', N'Đã xác nhận', NULL, N'Cặp Đôi Mai Anh & Đức (CLB Yonex)'),
(5, N'2026-10-25 22:48:50.517087', N'KH005', N'0933221100', N'GD01', N'Đôi nam nữ', N'Đã xác nhận', NULL, N'Cặp Đôi Tuấn & Lan (CLB Bình Thạnh)'),
(6, N'2026-10-25 22:48:59.651423', N'KH29936', N'0908123456', N'GD02', N'Doi Nam Nu', N'Đã xác nhận', NULL, N'CLB UTE Masters Test - Nguyen Van Test');
SET IDENTITY_INSERT tournament_registrations OFF;
GO

-- Lỗi trích xuất bảng tournament_matches: Table "TOURNAMENT_MATCHES" not found; SQL statement:
SELECT * FROM tournament_matches [42102-240]


-- -----------------------------------------------------
-- Dữ liệu bảng: payments (7 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT payments ON;
INSERT INTO payments (AMOUNT, ID, PAYMENT_TIME, BOOKING_CODE, PAYMENT_CODE, STATUS, METHOD, TRANSACTION_TYPE, TRANSACTION_REF) VALUES
(54000.00, 1, N'2026-10-25 22:48:50.523573', N'DS0101', N'TT0101', N'Thành công', N'Tiền mặt', N'Đặt cọc 30%', N'CASH-20260919-01'),
(36000.00, 2, N'2026-10-25 22:48:50.523573', N'DS0102', N'TT0102', N'Thành công', N'Tiền mặt', N'Đặt cọc 30%', N'CASH-20260919-02'),
(72000.00, 3, N'2026-10-25 22:48:50.523573', N'DS0103', N'TT0103', N'Thành công', N'VietQR Pro', N'Đặt cọc 30%', N'MB-998822110034'),
(27000.00, 4, N'2026-10-25 22:48:50.523573', N'DS0104', N'TT0104', N'Đang xử lý', N'VietQR Pro', N'Đặt cọc 30%', N'VCB-776655441122'),
(36000.00, 5, N'2026-10-25 22:48:50.523573', N'DS0105', N'TT0105', N'Thành công', N'VietQR Pro', N'Đặt cọc 30%', N'TCB-334455667788'),
(72000.00, 6, N'2026-10-25 22:48:50.523573', N'DS0201', N'TT0106', N'Thành công', N'VietQR Pro', N'Đặt cọc 30%', N'MB-556677889900'),
(30000.00, 7, N'2026-10-25 22:49:00.042722', N'DS623515', N'PAY521444', N'Thành công', N'Chuyển khoản VietQR', N'Đặt cọc 30%', N'SEPAY-DS623515');
SET IDENTITY_INSERT payments OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: work_shifts (5 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT work_shifts ON;
INSERT INTO work_shifts (SHIFT_DATE, ID, DAY_OF_WEEK, END_TIME, START_TIME, BRANCH_CODE, STAFF_CODE, ICON, SHIFT_TYPE, SHIFT_CODE, DUTY, STATUS) VALUES
(N'2026-10-25', 1, NULL, N'14:00', N'06:00', N'CN01', N'NS03', N'POS', N'Ca Sáng', N'CA01', N'POS Bán hàng & Quản lý', N'Đang trực'),
(N'2026-10-25', 2, NULL, N'22:00', N'14:00', N'CN01', N'NS04', N'POS', N'Ca Chiều', N'CA02', N'POS Bán hàng & Quản lý', N'Đã phân ca'),
(N'2026-10-25', 5, NULL, N'23:00', N'17:00', N'CN01', N'NS08', N'POS', N'Ca Tối', N'CA05', N'POS Bán hàng & Quản lý', N'Đang trực'),
(N'2026-10-25', 6, N'mon', N'13:30', N'05:00', N'CN01', N'NV-TEST-01', N'POS', N'morning', N'WS-MON-MOR-NV-TEST-01-3DCAB0', N'POS', N'Đã phân ca'),
(N'2026-10-25', 7, N'sun', N'22:48', N'06:00', N'CN01', N'NVQ01', N'POS', N'handover', N'BG733813', N'POS', N'Đã kết ca');
SET IDENTITY_INSERT work_shifts OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: stock_receipts (3 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT stock_receipts ON;
INSERT INTO stock_receipts (TOTAL_AMOUNT, ID, RECEIPT_DATE, BRANCH_CODE, STAFF_CODE, RECEIPT_CODE, SUPPLIER_NAME, NOTES) VALUES
(14500000.00, 1, N'2026-10-20 22:48:50.593642', N'CN01', N'NS02', N'PN0101', N'Công ty TNHH Thể Thao Yonex Việt Nam', N'Nhập bổ sung vợt Yonex Astrox và 50 ống cầu Hải Yến'),
(4200000.00, 2, N'2026-10-23 22:48:50.596825', N'CN01', N'NS02', N'PN0102', N'NPP Dụng Cụ Cầu Lông Đại Phát', N'Nhập nước bù khoáng Revive và Pocari Sweat đợt cuối tháng'),
(150000.00, 3, N'2026-10-25 22:48:59.726134', N'CN01', N'NS02', N'PNK235053', N'Cong Ty TNHH The Thao Viet', N'Nhap hang dau tuan - Nước bù khoáng Revive Chanh muối 500ml x10');
SET IDENTITY_INSERT stock_receipts OFF;
GO

-- Lỗi trích xuất bảng stock_equipment_details: Table "STOCK_EQUIPMENT_DETAILS" not found; SQL statement:
SELECT * FROM stock_equipment_details [42102-240]


-- -----------------------------------------------------
-- Dữ liệu bảng: stock_product_details (1 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT stock_product_details ON;
INSERT INTO stock_product_details (IMPORT_PRICE, QUANTITY, TOTAL_AMOUNT, ID, PRODUCT_CODE, RECEIPT_CODE) VALUES
(15000.00, 10, 150000.00, 1, N'P01', N'PNK235053');
SET IDENTITY_INSERT stock_product_details OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: promotions (3 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT promotions ON;
INSERT INTO promotions (DISCOUNT_VALUE, END_DATE, START_DATE, ID, PROMO_CODE, DISCOUNT_TYPE, STATUS, SCOPE, PROMO_NAME) VALUES
(15.00, N'2026-12-31', N'2026-09-01', 1, N'KM01', N'Phần trăm (%)', N'Đang áp dụng', N'Toàn chuỗi', N'Ưu Đãi Giờ Vàng Thứ Tư Hàng Tuần'),
(15.00, N'2026-12-31', N'2026-01-01', 2, N'KM02', N'Phần trăm (%)', N'Đang áp dụng', N'Toàn chuỗi', N'Đặc Quyền Hội Viên Kim Cương & Vàng'),
(90000.00, N'2026-10-31', N'2026-10-01', 3, N'KM03', N'Số tiền (VNĐ)', N'Đang áp dụng', N'Chi nhánh Gò Vấp', N'Khai Trương Cụm Sân Gò Vấp - Tặng 1 Giờ Đánh');
SET IDENTITY_INSERT promotions OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: system_settings (18 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT system_settings ON;
INSERT INTO system_settings (ID, UPDATED_AT, SETTING_CODE, SETTING_NAME, DESCRIPTION, SETTING_VALUE) VALUES
(1, N'2026-10-25 22:48:50.550096', N'CFG_DEPOSIT_RATE', N'Tỷ lệ đặt cọc tối thiểu (%)', N'Khách hàng phải thanh toán cọc ít nhất 30% để giữ sân hợp lệ', N'30'),
(2, N'2026-10-25 22:48:50.55356', N'CFG_HOLDING_MINUTES', N'Thời gian tạm giữ chỗ chờ cọc (Phút)', N'Sau 10 phút nếu không có giao dịch cọc, hệ thống tự động giải phóng ô sân', N'10'),
(3, N'2026-10-25 22:48:50.555565', N'CFG_CANCEL_HOURS', N'Thời hạn hủy sân tối thiểu để được hoàn cọc (Giờ)', N'Hủy trước giờ chơi tối thiểu 4 tiếng để được nhận lại 100% tiền cọc (QĐ-KH03)', N'4'),
(4, N'2026-10-25 22:48:50.55859', N'CFG_ADVANCE_DAYS', N'Số ngày tối đa được phép đặt trước', N'Giới hạn đặt sân trong vòng 30 ngày tới', N'30'),
(5, N'2026-10-25 22:48:50.560567', N'CFG_VNPAY_TMN', N'Mã đối tác VNPay (TMN Code)', N'Tích hợp cổng thanh toán VNPay QR Sandbox/Production', N'UTESPORT2026'),
(6, N'2026-10-25 22:48:50.562566', N'CFG_VNPAY_ENV', N'Môi trường VNPay', N'Môi trường thử nghiệm Sandbox hoặc Production', N'SANDBOX'),
(7, N'2026-10-25 22:48:50.565108', N'CFG_VNPAY_SECRET', N'Khóa bí mật VNPay Hash Secret', N'Khóa mã hóa bảo mật checksum giao dịch VNPay', N'••••••••••••••••••••••••'),
(8, N'2026-10-25 22:48:50.567108', N'CFG_MOMO_PARTNER', N'Partner Code MoMo Business', N'Tích hợp cổng Ví điện tử MoMo Business', N'MOMO_UTE_BADMINTON'),
(9, N'2026-10-25 22:48:50.570109', N'CFG_MOMO_KEY', N'Access Key MoMo', N'Khóa truy cập API tích hợp MoMo Business', N'F8B24C5E9012A34B'),
(10, N'2026-10-25 22:48:50.572109', N'CFG_POINTS_PER_1000', N'Số điểm tích lũy cho mỗi 1.000 VNĐ chi tiêu', N'Nhập số điểm nhận được cho mỗi 1.000 VNĐ chi tiêu', N'1'),
(11, N'2026-10-25 22:48:50.574662', N'CFG_TIER_SILVER_POINTS', N'Hạn mức điểm lên Hạng Bạc', N'Tích lũy đủ 500 điểm để thăng hạng Bạc (Giảm 5%)', N'500'),
(12, N'2026-10-25 22:48:50.576667', N'CFG_TIER_GOLD_POINTS', N'Hạn mức điểm lên Hạng Vàng', N'Tích lũy đủ 2.000 điểm để thăng hạng Vàng (Giảm 10%)', N'2000'),
(13, N'2026-10-25 22:48:50.57956', N'CFG_TIER_DIAMOND_POINTS', N'Hạn mức điểm lên Hạng Kim Cương', N'Tích lũy đủ 4.000 điểm để thăng hạng Kim Cương (Giảm 15%)', N'4000'),
(14, N'2026-10-25 22:48:50.581536', N'CFG_MAIL_HOST', N'Máy chủ gửi thư SMTP', N'Địa chỉ máy chủ SMTP (mặc định: smtp.gmail.com)', N'smtp.gmail.com'),
(15, N'2026-10-25 22:48:50.584105', N'CFG_MAIL_PORT', N'Cổng kết nối SMTP', N'Cổng gửi thư STARTTLS (mặc định: 587)', N'587'),
(16, N'2026-10-25 22:48:50.586111', N'CFG_MAIL_USERNAME', N'Tài khoản gửi email (Gmail)', N'Địa chỉ email Gmail của Ban quản trị để gửi OTP và vé điện tử', N'tho8189@gmail.com'),
(17, N'2026-10-25 22:48:50.589097', N'CFG_MAIL_PASSWORD', N'Mật khẩu ứng dụng Google (App Password)', N'Mật khẩu ứng dụng 16 ký tự tạo từ myaccount.google.com/apppasswords', N''),
(18, N'2026-10-25 22:48:50.590644', N'CFG_MAIL_FROM_NAME', N'Tên hiển thị người gửi', N'Tên hiển thị thương hiệu trong hộp thư người nhận', N'UTE Sport Badminton Arena');
SET IDENTITY_INSERT system_settings OFF;
GO


-- -----------------------------------------------------
-- Dữ liệu bảng: audit_logs (18 bản ghi)
-- -----------------------------------------------------
SET IDENTITY_INSERT audit_logs ON;
INSERT INTO audit_logs (LOG_ID, ACTION_TIME, USER_CODE, STATUS, IP_ADDRESS, MODULE, USERNAME, ACTION_TYPE, DETAILS) VALUES
(1, N'2026-10-25 22:36:50.607342', N'AD01', N'Thành công', N'192.168.1.105', N'An ninh & Đăng nhập', N'admin', N'Đăng nhập hệ thống', N'clob14: U&''X\00e1c th\1ef1c t\00e0i kho\1ea3n qu\1ea3n tr\1ecb vi\00ean th\00e0nh c\00f4ng'''),
(2, N'2026-10-25 21:23:50.607342', N'AD01', N'Thành công', N'192.168.1.105', N'Bảng giá & Khuyến mãi', N'admin', N'Cập nhật bảng giá sân', N'clob15: U&''\0110i\1ec1u ch\1ec9nh ph\1ee5 thu khung gi\1edd cao \0111i\1ec3m 17:00 - 21:00 chi nh\00e1nh Th\1ee7 \0110\1ee9c (CN01)'''),
(3, N'2026-10-25 19:48:50.607342', N'NVQ01', N'Thành công', N'192.168.1.42', N'Quản lý sân', N'baophuc', N'Chuyển trạng thái bảo trì', N'clob16: U&''T\1ea1m kh\00f3a b\1ea3o tr\00ec s\00e2n CL06 (Th\1ee7 \0110\1ee9c) \0111\1ec3 thay th\1ea3m s\00e0n thi \0111\1ea5u BWF'''),
(4, N'2026-10-25 17:48:50.607342', N'SYS', N'Thành công', N'127.0.0.1', N'Hệ thống sao lưu', N'system', N'Sao lưu cơ sở dữ liệu tự động', N'clob17: U&''T\1ea1o b\1ea3n sao l\01b0u \0111\1ecbnh k\1ef3 backup_ute_sport_auto_20261019.sql.gz (28.5 MB)'''),
(5, N'2026-10-25 16:48:50.607342', N'NV01', N'Thành công', N'192.168.1.18', N'Thanh toán & Thu ngân', N'thu_ngan_01', N'Xác nhận thanh toán tiền cọc', N'clob18: U&''Ghi nh\1eadn giao d\1ecbch chuy\1ec3n kho\1ea3n VietQR 60.000\0111 cho \0111\01a1n \0111\1eb7t s\00e2n DS001248'''),
(6, N'2026-10-25 14:48:50.607342', N'AD01', N'Thành công', N'192.168.1.105', N'Tài khoản & Phân quyền', N'admin', N'Phân quyền người dùng', N'clob19: U&''G\00e1n vai tr\00f2 Qu\1ea3n l\00fd chi nh\00e1nh B\00ecnh Th\1ea1nh cho t\00e0i kho\1ea3n ql_binhthanh'''),
(7, N'2026-10-25 08:48:50.607342', N'AD01', N'Thành công', N'192.168.1.105', N'Cấu hình thông số', N'admin', N'Cấu hình tham số hệ thống', N'clob20: U&''C\1eadp nh\1eadt th\1eddi h\1ea1n gi\1eef ch\1ed7 tr\01b0\1edbc khi h\1ee7y c\1ecdc th\00e0nh 15 ph\00fat'''),
(8, N'2026-10-25 04:48:50.607342', N'SYS', N'Thành công', N'127.0.0.1', N'Tích hợp VietQR', N'system', N'Đồng bộ giao dịch VietQR', N'clob21: U&''Ki\1ec3m tra webhook \0111\1ed1i so\00e1t v\1edbi ng\00e2n h\00e0ng VietinBank/MBBank'''),
(9, N'2026-10-24 22:48:50.607342', N'GD01', N'Thành công', N'192.168.1.200', N'Giải đấu & Sự kiện', N'giam_doc', N'Phê duyệt kế hoạch giải đấu', N'clob22: U&''Ph\00ea duy\1ec7t \0111i\1ec1u l\1ec7 Gi\1ea3i C\1ea7u L\00f4ng Doanh Nghi\1ec7p C\00fap Th\1ee7 \0110\1ee9c 2026'''),
(10, N'2026-10-24 20:48:50.607342', N'AD01', N'Thành công', N'192.168.1.105', N'Hệ thống sao lưu', N'admin', N'Sao lưu CSDL thủ công', N'clob23: U&''T\1ea1o b\1ea3n sao l\01b0u backup_ute_sport_manual_20261018.sql.gz tr\01b0\1edbc khi b\1ea3o tr\00ec'''),
(11, N'2026-10-23 22:48:50.607342', N'NVQ01', N'Thành công', N'192.168.1.42', N'Kho hàng & Dịch vụ', N'baophuc', N'Nhập kho sản phẩm POS', N'clob24: U&''Nh\1eadp b\1ed5 sung 50 \1ed1ng c\1ea7u Yonex Aerosensa 50 cho chi nh\00e1nh CN01'''),
(12, N'2026-10-22 22:48:50.607342', N'SYS', N'Thành công', N'127.0.0.1', N'Bảo trì CSDL', N'system', N'Tối ưu hóa chỉ mục CSDL', N'clob25: U&''Rebuild index b\1ea3ng bookings v\00e0 audit_logs \0111\1ecbnh k\1ef3'''),
(13, N'2026-10-25 22:48:59.174162', NULL, N'Thành công', N'127.0.0.1', N'Tài khoản & Phân quyền', N'admin', N'Khóa / Mở khóa tài khoản', N'clob26: U&''Chuy\1ec3n tr\1ea1ng th\00e1i t\00e0i kho\1ea3n [toggle_test_user] sang: B\1ecb kh\00f3a'''),
(14, N'2026-10-25 22:48:59.188721', NULL, N'Thành công', N'127.0.0.1', N'Tài khoản & Phân quyền', N'admin', N'Khóa / Mở khóa tài khoản', N'clob27: U&''Chuy\1ec3n tr\1ea1ng th\00e1i t\00e0i kho\1ea3n [toggle_test_user] sang: Ho\1ea1t \0111\1ed9ng'''),
(15, N'2026-10-25 22:48:59.298864', NULL, N'Thành công', N'127.0.0.1', N'Phân hệ Quản trị', N'admin', N'Cập nhật ma trận phân quyền chi nhánh', N'clob28: U&''C\1eadp nh\1eadt ph\00e2n quy\1ec1n chi nh\00e1nh cho: perm_test_user -> CN02; '''),
(16, N'2026-10-25 22:48:59.383276', NULL, N'Thành công', N'127.0.0.1', N'Tài khoản & Phân quyền', N'admin', N'Phân quyền & Cập nhật người dùng', N'clob29: U&''Thi\1ebft l\1eadp t\00e0i kho\1ea3n [test_staff_day05] - Vai tr\00f2: POS - Chi nh\00e1nh: CN01'''),
(17, N'2026-10-25 22:48:59.456487', NULL, N'Thành công', N'127.0.0.1', N'Tài khoản & Phân quyền', N'admin', N'Phân quyền & Cập nhật người dùng', N'clob30: U&''Thi\1ebft l\1eadp t\00e0i kho\1ea3n [test_staff_day05] - Vai tr\00f2: POS - Chi nh\00e1nh: CN01'''),
(18, N'2026-10-25 22:49:00.172887', NULL, N'Cảnh báo', N'127.0.0.1', N'An ninh & Đăng nhập', N'non_existing_user_9999', N'Đăng nhập thất bại', N'clob31: U&''\0110\0103ng nh\1eadp kh\00f4ng th\00e0nh c\00f4ng: T\00e0i kho\1ea3n ho\1eb7c m\1eadt kh\1ea9u kh\00f4ng ch\00ednh x\00e1c!''');
SET IDENTITY_INSERT audit_logs OFF;
GO

EXEC sp_MSforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all";
GO

-- 6. TẠO CÁC CHỈ MỤC INDEX TỐI ƯU HIỆU NĂNG TRUY VẤN (BUG-MED-25)
IF NOT EXISTS (SELECT name FROM sys.indexes WHERE name = N'idx_bookings_date_court')
    CREATE INDEX idx_bookings_date_court ON bookings(booking_date, court_code);

IF NOT EXISTS (SELECT name FROM sys.indexes WHERE name = N'idx_bookings_customer_phone')
    CREATE INDEX idx_bookings_customer_phone ON bookings(customer_phone);

IF NOT EXISTS (SELECT name FROM sys.indexes WHERE name = N'idx_courts_branch_status')
    CREATE INDEX idx_courts_branch_status ON courts(branch_code, status);

IF NOT EXISTS (SELECT name FROM sys.indexes WHERE name = N'idx_invoices_booking_date')
    CREATE INDEX idx_invoices_booking_date ON invoices(booking_code, invoice_date);
GO

PRINT N'>>> ĐÃ KHỞI TẠO TOÀN DIỆN 25 BẢNG DỮ LIỆU, 25 VIEW TIẾNG VIỆT VÀ CHÈN SEED DATA HOÀN TẤT THÀNH CÔNG!';
GO

-- =====================================================================
-- 7. HÀM NGƯỜI DÙNG ĐỊNH NGHĨA (USER-DEFINED FUNCTIONS - UDF)
-- =====================================================================

-- 7.1 Hàm kiểm tra tính khả dụng của sân trong khung giờ cụ thể
CREATE FUNCTION dbo.fn_CheckSlotAvailability
(
    @court_code VARCHAR(20),
    @booking_date DATE,
    @time_slot VARCHAR(50)
)
RETURNS BIT
AS
BEGIN
    IF EXISTS (SELECT 1 FROM courts WHERE court_code = @court_code AND status = N'Bảo trì')
    BEGIN
        RETURN 0;
    END

    IF EXISTS (
        SELECT 1 
        FROM bookings 
        WHERE court_code = @court_code 
          AND booking_date = @booking_date 
          AND status <> N'Đã hủy'
          AND (
              time_slot = @time_slot
              OR CHARINDEX(@time_slot, time_slot) > 0
              OR CHARINDEX(time_slot, @time_slot) > 0
          )
    )
    BEGIN
        RETURN 0;
    END

    RETURN 1;
END;
GO

-- 7.2 Hàm tính tiền thuê sân theo thời lượng khung giờ và chiết khấu khuyến mãi
CREATE FUNCTION dbo.fn_CalculateBookingPrice
(
    @court_code VARCHAR(20),
    @time_slot VARCHAR(50),
    @promo_code VARCHAR(20) = NULL
)
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @hourly_rate DECIMAL(18,2) = 90000;
    DECLARE @total_price DECIMAL(18,2) = 0;
    DECLARE @duration INT = 1;

    SELECT @hourly_rate = ISNULL(hourly_rate, 90000)
    FROM courts
    WHERE court_code = @court_code;

    IF CHARINDEX('-', @time_slot) > 0
    BEGIN
        DECLARE @start_str VARCHAR(10) = LTRIM(RTRIM(SUBSTRING(@time_slot, 1, CHARINDEX('-', @time_slot) - 1)));
        DECLARE @end_str VARCHAR(10) = LTRIM(RTRIM(SUBSTRING(@time_slot, CHARINDEX('-', @time_slot) + 1, LEN(@time_slot))));
        DECLARE @start_h INT = TRY_CAST(SUBSTRING(@start_str, 1, 2) AS INT);
        DECLARE @end_h INT = TRY_CAST(SUBSTRING(@end_str, 1, 2) AS INT);
        
        IF @start_h IS NOT NULL AND @end_h IS NOT NULL AND @end_h > @start_h
            SET @duration = @end_h - @start_h;
    END
    ELSE IF CHARINDEX(',', @time_slot) > 0
    BEGIN
        SET @duration = LEN(@time_slot) - LEN(REPLACE(@time_slot, ',', '')) + 1;
    END

    SET @total_price = @hourly_rate * @duration;

    IF @promo_code IS NOT NULL AND LEN(@promo_code) > 0
    BEGIN
        DECLARE @disc_type NVARCHAR(30);
        DECLARE @disc_val DECIMAL(18,2);
        
        SELECT @disc_type = discount_type, @disc_val = discount_value
        FROM promotions
        WHERE promo_code = @promo_code 
          AND status = N'Đang áp dụng'
          AND CAST(GETDATE() AS DATE) BETWEEN start_date AND end_date;

        IF @disc_type IS NOT NULL
        BEGIN
            IF @disc_type LIKE N'%Phần trăm%' OR @disc_type LIKE '%Percent%'
                SET @total_price = @total_price - (@total_price * (@disc_val / 100.0));
            ELSE
                SET @total_price = @total_price - @disc_val;

            IF @total_price < 0 SET @total_price = 0;
        END
    END

    RETURN @total_price;
END;
GO

-- 7.3 Hàm lấy tỷ lệ giảm giá theo hạng thẻ thành viên
CREATE FUNCTION dbo.fn_GetCustomerTierDiscount
(
    @customer_code VARCHAR(20)
)
RETURNS DECIMAL(5,2)
AS
BEGIN
    DECLARE @tier NVARCHAR(30);
    DECLARE @discount_percent DECIMAL(5,2) = 0.0;

    SELECT @tier = membership_tier
    FROM customers
    WHERE customer_code = @customer_code;

    IF @tier = N'Bạc'
        SET @discount_percent = 5.0;
    ELSE IF @tier = N'Vàng'
        SET @discount_percent = 10.0;
    ELSE IF @tier = N'Kim Cương'
        SET @discount_percent = 15.0;

    RETURN @discount_percent;
END;
GO

-- 7.4 Hàm tổng hợp tổng doanh thu của một chi nhánh trong một khoảng thời gian
CREATE FUNCTION dbo.fn_GetBranchRevenue
(
    @branch_code VARCHAR(20),
    @from_date DATETIME,
    @to_date DATETIME
)
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @total DECIMAL(18,2) = 0;

    SELECT @total = ISNULL(SUM(i.total_payment), 0)
    FROM invoices i
    INNER JOIN bookings b ON i.booking_code = b.booking_code
    WHERE b.branch_code = @branch_code
      AND i.invoice_date BETWEEN @from_date AND @to_date
      AND i.status = N'Đã thanh toán';

    RETURN @total;
END;
GO

-- 7.5 Hàm trả về danh sách các sân còn trống theo chi nhánh và khung giờ (Table-Valued Function)
CREATE FUNCTION dbo.fn_GetAvailableCourtsByBranch
(
    @branch_code VARCHAR(20),
    @booking_date DATE,
    @time_slot VARCHAR(50)
)
RETURNS TABLE
AS
RETURN
(
    SELECT 
        c.court_code,
        c.court_name,
        c.court_type,
        c.hourly_rate,
        c.status AS current_court_status,
        c.branch_code
    FROM courts c
    WHERE c.branch_code = @branch_code
      AND c.status <> N'Bảo trì'
      AND NOT EXISTS (
          SELECT 1 
          FROM bookings b
          WHERE b.court_code = c.court_code
            AND b.booking_date = @booking_date
            AND b.status <> N'Đã hủy'
            AND (
                b.time_slot = @time_slot
                OR CHARINDEX(@time_slot, b.time_slot) > 0
                OR CHARINDEX(b.time_slot, @time_slot) > 0
            )
      )
);
GO

-- =====================================================================
-- 8. THỦ TỤC LƯU TRỮ NGHIỆP VỤ (STORED PROCEDURES)
-- =====================================================================

-- 8.1 Thủ tục đặt & khóa sân trực tuyến với cơ chế bảo vệ giao dịch (Concurrency Transaction)
CREATE PROCEDURE dbo.sp_LockSlotAndCreateBooking
    @court_code VARCHAR(20),
    @branch_code VARCHAR(20),
    @booking_date DATE,
    @time_slot VARCHAR(50),
    @customer_name NVARCHAR(100),
    @customer_phone VARCHAR(20),
    @notes NVARCHAR(255) = NULL,
    @promo_code VARCHAR(20) = NULL,
    @addon_amount DECIMAL(18,2) = 0,
    @booking_code VARCHAR(30) OUTPUT,
    @deposit_amount DECIMAL(18,2) OUTPUT,
    @total_price DECIMAL(18,2) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        IF dbo.fn_CheckSlotAvailability(@court_code, @booking_date, @time_slot) = 0
        BEGIN
            THROW 50001, N'Khung giờ hoặc sân đã có người đặt trước hoặc đang bảo trì!', 1;
        END

        DECLARE @new_code VARCHAR(30);
        DECLARE @random_num INT;
        SET @random_num = CAST(RAND(CHECKSUM(NEWID())) * 899999 + 100000 AS INT);
        SET @new_code = 'DS' + CAST(@random_num AS VARCHAR(10));
        WHILE EXISTS (SELECT 1 FROM bookings WHERE booking_code = @new_code)
        BEGIN
            SET @random_num = CAST(RAND(CHECKSUM(NEWID())) * 899999 + 100000 AS INT);
            SET @new_code = 'DS' + CAST(@random_num AS VARCHAR(10));
        END
        SET @booking_code = @new_code;

        DECLARE @hourly_price DECIMAL(18,2);
        SELECT @hourly_price = ISNULL(hourly_rate, 90000) FROM courts WHERE court_code = @court_code;

        DECLARE @court_fee DECIMAL(18,2);
        SET @court_fee = dbo.fn_CalculateBookingPrice(@court_code, @time_slot, @promo_code);

        SET @total_price = @court_fee + ISNULL(@addon_amount, 0);
        SET @deposit_amount = ROUND(@total_price * 0.3, 0);

        DECLARE @cust_code VARCHAR(20) = NULL;
        SELECT @cust_code = customer_code FROM customers WHERE phone = @customer_phone;

        INSERT INTO bookings (
            booking_code, customer_code, customer_name, customer_phone,
            court_code, branch_code, booking_date, time_slot,
            hourly_price, total_price, deposit_amount,
            payment_method, status, notes, created_at
        )
        VALUES (
            @booking_code, @cust_code, @customer_name, @customer_phone,
            @court_code, @branch_code, @booking_date, @time_slot,
            @hourly_price, @total_price, @deposit_amount,
            N'Chuyển khoản VietQR', N'Chờ cọc 30%', 
            ISNULL(@notes, N'Đặt qua hệ thống trực tuyến'), GETDATE()
        );

        INSERT INTO audit_logs (username, action_type, action_time, details, module, status)
        VALUES (
            ISNULL(@cust_code, 'GUEST'), 
            N'Khóa slot đặt sân trực tuyến', 
            GETDATE(), 
            N'Mã đơn: ' + @booking_code + N', Sân: ' + @court_code + N', Giờ: ' + @time_slot + N', Tiền cọc: ' + FORMAT(@deposit_amount, 'N0') + N'đ',
            N'Phân hệ Đặt Sân', N'Thành công'
        );

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 8.2 Thủ tục xác nhận thanh toán cọc 30% qua VietQR
CREATE PROCEDURE dbo.sp_ConfirmBookingDeposit
    @booking_code VARCHAR(30),
    @amount DECIMAL(18,2),
    @method NVARCHAR(50) = N'VietQR Pro',
    @transaction_ref VARCHAR(100) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        IF NOT EXISTS (SELECT 1 FROM bookings WHERE booking_code = @booking_code)
        BEGIN
            THROW 50002, N'Không tìm thấy mã đơn đặt sân trong cơ sở dữ liệu!', 1;
        END

        UPDATE bookings
        SET status = N'Đã cọc 30%',
            deposit_amount = @amount
        WHERE booking_code = @booking_code;

        DECLARE @pay_code VARCHAR(30) = 'TT' + SUBSTRING(@booking_code, 3, LEN(@booking_code));
        IF EXISTS (SELECT 1 FROM payments WHERE payment_code = @pay_code)
        BEGIN
            SET @pay_code = 'TT' + CAST(CAST(RAND(CHECKSUM(NEWID())) * 899999 + 100000 AS INT) AS VARCHAR(10));
        END

        INSERT INTO payments (
            payment_code, booking_code, amount, method, 
            transaction_type, transaction_ref, payment_time, status
        )
        VALUES (
            @pay_code, @booking_code, @amount, @method,
            N'Đặt cọc 30%', ISNULL(@transaction_ref, 'AUTO-VIETQR-' + @booking_code),
            GETDATE(), N'Thành công'
        );

        DECLARE @b_date DATE;
        DECLARE @c_code VARCHAR(20);
        SELECT @b_date = booking_date, @c_code = court_code FROM bookings WHERE booking_code = @booking_code;
        
        IF @b_date = CAST(GETDATE() AS DATE)
        BEGIN
            UPDATE courts SET status = N'Đặt trước' WHERE court_code = @c_code AND status = N'Trống';
        END

        INSERT INTO audit_logs (username, action_type, action_time, details, module, status)
        VALUES (
            'SYSTEM_VIETQR', 
            N'Xác nhận cọc 30% đơn đặt sân', 
            GETDATE(), 
            N'Đơn ' + @booking_code + N' đã nhận cọc: ' + FORMAT(@amount, 'N0') + N'đ qua ' + @method,
            N'Cổng Thanh Toán', N'Thành công'
        );

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 8.3 Thủ tục Check-in nhận sân tại quầy POS
CREATE PROCEDURE dbo.sp_CheckInBooking
    @booking_code VARCHAR(30),
    @staff_code VARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @court_code VARCHAR(20);
        SELECT @court_code = court_code FROM bookings WHERE booking_code = @booking_code;

        IF @court_code IS NULL
        BEGIN
            THROW 50003, N'Không tìm thấy đơn đặt sân!', 1;
        END

        UPDATE bookings
        SET status = N'Đang sử dụng',
            check_in_time = GETDATE()
        WHERE booking_code = @booking_code;

        UPDATE courts
        SET status = N'Đang sử dụng'
        WHERE court_code = @court_code;

        INSERT INTO audit_logs (username, action_type, action_time, details, module, status)
        VALUES (
            ISNULL(@staff_code, 'POS_STAFF'),
            N'Check-in nhận sân tại quầy POS',
            GETDATE(),
            N'Khách nhận sân ' + @court_code + N' cho đơn ' + @booking_code,
            N'Phân hệ POS', N'Thành công'
        );

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 8.4 Thủ tục Check-out trả sân & Xuất hóa đơn POS
CREATE PROCEDURE dbo.sp_CheckOutAndCreateInvoice
    @booking_code VARCHAR(30),
    @staff_code VARCHAR(20) = NULL,
    @payment_method NVARCHAR(50) = N'Tiền mặt',
    @invoice_code VARCHAR(30) OUTPUT,
    @final_payment DECIMAL(18,2) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @court_code VARCHAR(20);
        DECLARE @court_fee DECIMAL(18,2);
        DECLARE @deposit_paid DECIMAL(18,2);
        DECLARE @cust_code VARCHAR(20);

        SELECT 
            @court_code = court_code,
            @court_fee = total_price,
            @deposit_paid = ISNULL(deposit_amount, 0),
            @cust_code = customer_code
        FROM bookings 
        WHERE booking_code = @booking_code;

        IF @court_code IS NULL
        BEGIN
            THROW 50004, N'Không tìm thấy thông tin đơn đặt sân để thanh toán!', 1;
        END

        UPDATE bookings
        SET status = N'Hoàn thành',
            check_out_time = GETDATE()
        WHERE booking_code = @booking_code;

        UPDATE courts
        SET status = N'Trống'
        WHERE court_code = @court_code;

        DECLARE @count INT;
        SELECT @count = COUNT(*) FROM invoices;
        SET @invoice_code = 'HD' + RIGHT('000000' + CAST(@count + 1 AS VARCHAR(10)), 6);

        SET @final_payment = @court_fee - @deposit_paid;
        IF @final_payment < 0 SET @final_payment = 0;

        INSERT INTO invoices (
            invoice_code, booking_code, staff_code, invoice_date,
            court_fee, equipment_fee, product_fee, discount_amount,
            deposit_paid, total_payment, payment_method, status
        )
        VALUES (
            @invoice_code, @booking_code, @staff_code, GETDATE(),
            @court_fee, 0, 0, 0,
            @deposit_paid, @final_payment, @payment_method, N'Đã thanh toán'
        );

        IF @cust_code IS NOT NULL
        BEGIN
            UPDATE customers
            SET total_play_hours = total_play_hours + 1.0,
                reward_points = reward_points + CAST(@court_fee / 10000 AS INT)
            WHERE customer_code = @cust_code;
        END

        INSERT INTO audit_logs (username, action_type, action_time, details, module, status)
        VALUES (
            ISNULL(@staff_code, 'POS_STAFF'),
            N'Check-out & Xuất hóa đơn POS',
            GETDATE(),
            N'Xuất hóa đơn ' + @invoice_code + N' cho đơn ' + @booking_code + N', thực thu: ' + FORMAT(@final_payment, 'N0') + N'đ',
            N'Phân hệ POS', N'Thành công'
        );

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 8.5 Thủ tục hủy đơn đặt sân và áp dụng chính sách hoàn cọc (trước 4 tiếng hoàn 100%)
CREATE PROCEDURE dbo.sp_CancelBookingWithRefundPolicy
    @booking_code VARCHAR(30),
    @cancel_reason NVARCHAR(255) = N'Khách báo hủy lịch',
    @refund_amount DECIMAL(18,2) OUTPUT,
    @is_refunded BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @booking_date DATE;
        DECLARE @time_slot VARCHAR(50);
        DECLARE @deposit DECIMAL(18,2);
        DECLARE @court_code VARCHAR(20);

        SELECT 
            @booking_date = booking_date,
            @time_slot = time_slot,
            @deposit = ISNULL(deposit_amount, 0),
            @court_code = court_code
        FROM bookings
        WHERE booking_code = @booking_code;

        IF @booking_date IS NULL
        BEGIN
            THROW 50005, N'Không tìm thấy mã đơn đặt sân để thực hiện hủy!', 1;
        END

        DECLARE @start_hour INT = 8;
        IF CHARINDEX('-', @time_slot) > 0
        BEGIN
            SET @start_hour = TRY_CAST(SUBSTRING(LTRIM(RTRIM(@time_slot)), 1, 2) AS INT);
        END
        IF @start_hour IS NULL SET @start_hour = 8;

        DECLARE @play_datetime DATETIME;
        SET @play_datetime = DATEADD(HOUR, @start_hour, CAST(@booking_date AS DATETIME));

        DECLARE @min_cancel_hours INT = 4;
        SELECT @min_cancel_hours = TRY_CAST(setting_value AS INT)
        FROM system_settings
        WHERE setting_code = 'CFG_CANCEL_HOURS';
        IF @min_cancel_hours IS NULL SET @min_cancel_hours = 4;

        DECLARE @diff_hours INT = DATEDIFF(HOUR, GETDATE(), @play_datetime);

        IF @diff_hours >= @min_cancel_hours AND @deposit > 0
        BEGIN
            SET @refund_amount = @deposit;
            SET @is_refunded = 1;

            INSERT INTO payments (
                payment_code, booking_code, amount, method,
                transaction_type, transaction_ref, payment_time, status
            )
            VALUES (
                'HT' + SUBSTRING(@booking_code, 3, LEN(@booking_code)),
                @booking_code, @deposit, N'Chuyển khoản VietQR',
                N'Hoàn tiền cọc', N'REFUND-100%-' + @booking_code,
                GETDATE(), N'Thành công'
            );
        END
        ELSE
        BEGIN
            SET @refund_amount = 0;
            SET @is_refunded = 0;
        END

        UPDATE bookings
        SET status = N'Đã hủy',
            notes = ISNULL(notes, '') + N' [Hủy: ' + @cancel_reason + N' | Hoàn cọc: ' + FORMAT(@refund_amount, 'N0') + N'đ]'
        WHERE booking_code = @booking_code;

        UPDATE courts
        SET status = N'Trống'
        WHERE court_code = @court_code AND status = N'Đặt trước';

        INSERT INTO audit_logs (username, action_type, action_time, details, module, status)
        VALUES (
            'CUSTOMER_PORTAL',
            N'Hủy đặt sân theo chính sách',
            GETDATE(),
            N'Hủy đơn ' + @booking_code + N'. Hoàn cọc: ' + FORMAT(@refund_amount, 'N0') + N'đ (' + CASE WHEN @is_refunded = 1 THEN N'Hoàn 100%' ELSE N'Không hoàn do hủy trễ' END + N')',
            N'Chính Sách Hoàn Cọc', N'Thành công'
        );

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 8.6 Thủ tục lập phiếu nhập kho vật tư
CREATE PROCEDURE dbo.sp_ImportStockReceipt
    @receipt_code VARCHAR(30),
    @branch_code VARCHAR(20),
    @staff_code VARCHAR(20),
    @supplier_name NVARCHAR(100),
    @total_amount DECIMAL(18,2),
    @notes NVARCHAR(255) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO stock_receipts (receipt_code, branch_code, staff_code, receipt_date, supplier_name, total_amount, notes)
        VALUES (@receipt_code, @branch_code, @staff_code, GETDATE(), @supplier_name, @total_amount, @notes);

        INSERT INTO audit_logs (username, action_type, action_time, details, module, status)
        VALUES (
            @staff_code,
            N'Lập phiếu nhập kho vật tư',
            GETDATE(),
            N'Phiếu nhập ' + @receipt_code + N' tại chi nhánh ' + @branch_code + N', tổng tiền: ' + FORMAT(@total_amount, 'N0') + N'đ',
            N'Phân hệ Kho', N'Thành công'
        );

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 8.7 Thủ tục báo cáo tổng kết doanh thu theo ngày của chi nhánh
CREATE PROCEDURE dbo.sp_GetBranchDailyRevenueReport
    @branch_code VARCHAR(20),
    @report_date DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF @report_date IS NULL SET @report_date = CAST(GETDATE() AS DATE);

    SELECT 
        b.branch_code,
        br.branch_name,
        @report_date AS report_date,
        COUNT(DISTINCT b.booking_code) AS total_bookings,
        COUNT(DISTINCT CASE WHEN b.status = N'Hoàn thành' THEN b.booking_code END) AS completed_bookings,
        COUNT(DISTINCT CASE WHEN b.status = N'Đã hủy' THEN b.booking_code END) AS cancelled_bookings,
        ISNULL(SUM(i.court_fee), 0) AS total_court_fee,
        ISNULL(SUM(i.equipment_fee), 0) AS total_equipment_fee,
        ISNULL(SUM(i.product_fee), 0) AS total_product_fee,
        ISNULL(SUM(i.discount_amount), 0) AS total_discount,
        ISNULL(SUM(i.total_payment), 0) AS net_revenue
    FROM branches br
    LEFT JOIN bookings b ON br.branch_code = b.branch_code AND b.booking_date = @report_date
    LEFT JOIN invoices i ON b.booking_code = i.booking_code AND i.status = N'Đã thanh toán'
    WHERE br.branch_code = @branch_code
    GROUP BY b.branch_code, br.branch_name;
END;
GO

-- =====================================================================
-- 9. TRIGGER TỰ ĐỘNG HÓA VÀ RÀNG BUỘC TOÀN VẸN (TRIGGERS)
-- =====================================================================

-- 9.1 Trigger kiểm soát chống đặt trùng giờ trên cùng một sân (Chống Race Condition)
CREATE TRIGGER dbo.trg_CheckBookingOverlap
ON dbo.bookings
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        INNER JOIN dbo.bookings b ON i.court_code = b.court_code 
                                AND i.booking_date = b.booking_date
                                AND i.booking_code <> b.booking_code
        WHERE b.status <> N'Đã hủy'
          AND i.status <> N'Đã hủy'
          AND (
              i.time_slot = b.time_slot
              OR CHARINDEX(i.time_slot, b.time_slot) > 0
              OR CHARINDEX(b.time_slot, i.time_slot) > 0
          )
    )
    BEGIN
        RAISERROR (N'Lỗi xung đột lịch: Đã có đơn đặt sân khác sử dụng khung giờ này cho cùng một sân trong ngày!', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END;
GO

-- 9.2 Trigger tự động giảm số lượng tồn kho sản phẩm khi xuất hóa đơn POS
CREATE TRIGGER dbo.trg_UpdateStockOnInvoiceProduct
ON dbo.invoice_product_details
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE p
    SET p.stock_quantity = p.stock_quantity - i.quantity
    FROM dbo.products p
    INNER JOIN inserted i ON p.product_code = i.product_code;

    IF EXISTS (SELECT 1 FROM dbo.products WHERE stock_quantity < 0)
    BEGIN
        RAISERROR (N'Lỗi xuất kho: Số lượng tồn kho sản phẩm trong kho không đủ để bán!', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END;
GO

-- 9.3 Trigger tự động tăng số lượng tồn kho khi nhập hàng từ nhà cung cấp
CREATE TRIGGER dbo.trg_UpdateStockOnReceiptDetails
ON dbo.stock_product_details
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE p
    SET p.stock_quantity = p.stock_quantity + i.quantity
    FROM dbo.products p
    INNER JOIN inserted i ON p.product_code = i.product_code;
END;
GO

-- 9.4 Trigger tự động thăng hạng thẻ thành viên theo lũy kế giờ chơi
CREATE TRIGGER dbo.trg_UpdateCustomerMembershipLevel
ON dbo.customers
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(total_play_hours)
    BEGIN
        UPDATE c
        SET c.membership_tier = CASE
            WHEN c.total_play_hours >= 70 THEN N'Kim Cương'
            WHEN c.total_play_hours >= 30 THEN N'Vàng'
            WHEN c.total_play_hours >= 10 THEN N'Bạc'
            ELSE N'Đồng'
        END
        FROM dbo.customers c
        INNER JOIN inserted i ON c.customer_code = i.customer_code;
    END
END;
GO

-- 9.5 Trigger tự động ghi nhật ký hệ thống khi trạng thái sân bãi thay đổi
CREATE TRIGGER dbo.trg_AuditCourtStatusChange
ON dbo.courts
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(status)
    BEGIN
        INSERT INTO dbo.audit_logs (username, action_type, action_time, details, module, status)
        SELECT 
            'SYSTEM_COURT_MONITOR',
            N'Cập nhật trạng thái sân',
            GETDATE(),
            N'Sân [' + i.court_code + N' - ' + i.court_name + N'] chuyển từ [' + ISNULL(d.status, N'Chưa rõ') + N'] sang [' + ISNULL(i.status, N'Chưa rõ') + N']',
            N'Giám Sát Sân Bãi',
            N'Thành công'
        FROM inserted i
        INNER JOIN deleted d ON i.court_code = d.court_code
        WHERE i.status <> d.status;
    END
END;
GO

-- 9.6 Trigger xác thực tính hợp lệ của chương trình khuyến mãi
CREATE TRIGGER dbo.trg_ValidatePromotionDates
ON dbo.promotions
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1 
        FROM inserted 
        WHERE end_date < start_date OR discount_value <= 0
    )
    BEGIN
        RAISERROR (N'Lỗi: Ngày kết thúc khuyến mãi phải lớn hơn hoặc bằng ngày bắt đầu và mức giảm giá phải lớn hơn 0!', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END;
GO

PRINT N'=====================================================================';
PRINT N'>>> ĐÃ KHỞI TẠO TOÀN DIỆN 25 BẢNG DỮ LIỆU, 25 VIEW TIẾNG VIỆT,';
PRINT N'>>> 5 FUNCTIONS, 7 STORED PROCEDURES VÀ 6 TRIGGERS HOÀN TẤT THÀNH CÔNG!';
PRINT N'=====================================================================';
GO


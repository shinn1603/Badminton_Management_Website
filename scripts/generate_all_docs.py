# -*- coding: utf-8 -*-
"""
generate_all_docs.py: Master script xuất file tài liệu Word (.docx) hoàn chỉnh
"""

import os
import sys

# Thiết lập UTF-8 cho stdout trên console Windows
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

# Đảm bảo đường dẫn import tương đối chính xác
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)

from doc_helper import init_document
from build_doc_part1 import build_part_1
from build_doc_part2 import build_part_2
from build_doc_part3 import build_part_3

def main():
    print(">>> BAT DAU QUY TRINH TAO TAI LIEU TOAN DIEN UTE SPORT (.DOCX) <<<")
    
    # 1. Khởi tạo văn bản với chuẩn Times New Roman, cỡ chữ 13, lề 1 inch
    doc = init_document()
    print("[-] Da khoi tao cau hinh van ban chuan: Times New Roman, co 13pt, chu den, le 2.54cm.")
    
    # 2. Xây dựng Phần 1: Kiến trúc ứng dụng, Luồng dữ liệu, Bảo mật, WebSocket
    print("[-] Dang bien soan Phan 1: Tong quan & Kien truc ung dung...")
    build_part_1(doc)
    
    # 3. Xây dựng Phần 2: Diễn giải chi tiết chức năng 1 đến 6
    print("[-] Dang bien soan Phan 2: Dien giai chi tiet tung chuc nang (Chuc nang 1 - 6)...")
    build_part_2(doc)
    
    # 4. Xây dựng Phần 3: Diễn giải chức năng 7, 8, 9 và Hướng dẫn lập trình viên
    print("[-] Dang bien soan Phan 3: Dien giai chuc nang 7 - 9 va Cam nang Developer...")
    build_part_3(doc)
    
    # 5. Lưu tài liệu ra thư mục gốc dự án
    output_filename = "Tai_Lieu_Huong_Dan_Xay_Dung_Ung_Dung_UTE_Sport.docx"
    output_path = os.path.join(os.path.dirname(current_dir), output_filename)
    
    doc.save(output_path)
    print(f"[+] TAI LIEU DA DUOC TAO THANH CONG TAI: {output_path}")
    
    # Thống kê văn bản
    num_paragraphs = len(doc.paragraphs)
    num_tables = len(doc.tables)
    file_size_kb = os.path.getsize(output_path) / 1024
    
    print("=" * 60)
    print(f"THONG KE TAI LIEU XUAT RA:")
    print(f" - Tong so doan van (Paragraphs): {num_paragraphs}")
    print(f" - Tong so bang bieu & Khoi code (Tables): {num_tables}")
    print(f" - Kich thuoc tep tin Word: {file_size_kb:.2f} KB")
    print("=" * 60)

if __name__ == "__main__":
    main()

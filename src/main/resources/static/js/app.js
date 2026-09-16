/**
 * UTE SPORT - BADMINTON CHAIN MANAGEMENT SYSTEM
 * Core Application Logic & State Management
 */

// Global State
const AppState = {
  currentRole: 'KH', // KH, NVQ, QLCN, GD, QT
  currentBranch: 'CN01',
  branches: [
    { id: 'CN01', name: 'UTE Sport - Cơ sở 1 (Thủ Đức)', address: 'Số 1 Võ Văn Ngân, TP. Thủ Đức', phone: '028.3896.8641', courts: 8 },
    { id: 'CN02', name: 'UTE Sport - Cơ sở 2 (Quận 1)', address: '12 Nguyễn Thị Minh Khai, Quận 1', phone: '028.3822.4411', courts: 6 },
    { id: 'CN03', name: 'UTE Sport - Cơ sở 3 (Bình Thạnh)', address: '152 Điện Biên Phủ, P.25, Bình Thạnh', phone: '028.3512.9988', courts: 10 }
  ],
  products: [
    { id: 'SP01', name: 'Nước Pocari Sweat 500ml', price: 20000, stock: 45, category: 'drink', icon: 'bi-cup-straw' },
    { id: 'SP02', name: 'Nước tăng lực Revive Chanh Muối', price: 15000, stock: 62, category: 'drink', icon: 'bi-lightning-charge' },
    { id: 'SP03', name: 'Nước khoáng Aquafina 500ml', price: 10000, stock: 80, category: 'drink', icon: 'bi-droplet' },
    { id: 'SP04', name: 'Ống cầu lông Thành Công 12 quả', price: 130000, stock: 24, category: 'shuttlecock', icon: 'bi-circle' },
    { id: 'SP05', name: 'Ống cầu lông Hải Yến S90', price: 140000, stock: 18, category: 'shuttlecock', icon: 'bi-circle-fill' },
    { id: 'DC01', name: 'Thuê vợt Yonex Astrox 88D Pro', price: 35000, stock: 12, category: 'rental', icon: 'bi-activity' },
    { id: 'DC02', name: 'Thuê vợt Lining Axforce 80', price: 30000, stock: 10, category: 'rental', icon: 'bi-activity' }
  ],
  activeBookingHold: null, // Holds the 10-minute timer for booking
  holdRemainingSeconds: 600,
  holdInterval: null,
  selectedSlots: [],
  posOrder: {
    courtId: 'S01',
    customerName: 'Nguyễn Văn An',
    customerPhone: '0912345678',
    memberRank: 'Vàng', // 10% discount
    discountRate: 0.10,
    startTime: '08:00',
    endTime: '10:00',
    courtPrice: 160000,
    depositPaid: 50000,
    items: [
      { id: 'SP01', name: 'Pocari Sweat 500ml', price: 20000, quantity: 2 },
      { id: 'DC01', name: 'Thuê vợt Yonex Astrox', price: 35000, quantity: 1 }
    ]
  }
};

// Format currency in VND
function formatVND(amount) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

// Switch User Role Functionality
function switchRole(roleCode, roleTitle) {
  AppState.currentRole = roleCode;
  
  // Update badge UI
  const badge = document.getElementById('current-role-badge');
  if (badge) {
    badge.className = `role-badge role-${roleCode.toLowerCase()}`;
    badge.innerHTML = `<i class="bi bi-shield-check"></i> <span>Vai trò: ${roleTitle}</span>`;
  }

  // Show corresponding notifications
  showToast(`Đã chuyển sang vai trò: <strong>${roleTitle}</strong>`, 'info');
}

// Toast notification helper
function showToast(message, type = 'success') {
  let toastContainer = document.getElementById('toast-container');
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toast-container';
    toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    toastContainer.style.zIndex = '9999';
    document.body.appendChild(toastContainer);
  }

  const bgClass = type === 'success' ? 'bg-success text-white' : (type === 'danger' ? 'bg-danger text-white' : 'bg-primary text-white');
  const toastId = 'toast-' + Date.now();
  const toastHtml = `
    <div id="${toastId}" class="toast align-items-center ${bgClass} border-0 shadow-lg" role="alert" aria-live="assertive" aria-atomic="true">
      <div class="d-flex">
        <div class="toast-body d-flex align-items-center gap-2">
          <i class="bi ${type === 'success' ? 'bi-check-circle-fill' : 'bi-info-circle-fill'}"></i>
          <div>${message}</div>
        </div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
      </div>
    </div>
  `;
  toastContainer.insertAdjacentHTML('beforeend', toastHtml);
  const toastElement = document.getElementById(toastId);
  const bsToast = new bootstrap.Toast(toastElement, { delay: 4000 });
  bsToast.show();
  toastElement.addEventListener('hidden.bs.toast', () => toastElement.remove());
}

// Matrix Grid Slot Selection & Hold Lock Logic
function handleSlotClick(element, courtName, timeSlot, price) {
  if (element.classList.contains('slot-booked') || element.classList.contains('slot-maintenance')) {
    showToast('Khung giờ này đã có người đặt hoặc đang bảo trì!', 'danger');
    return;
  }

  const slotKey = `${courtName}_${timeSlot}`;
  const isSelected = element.classList.contains('slot-selected');

  if (isSelected) {
    element.classList.remove('slot-selected');
    element.classList.add('slot-available');
    AppState.selectedSlots = AppState.selectedSlots.filter(s => s.key !== slotKey);
  } else {
    element.classList.remove('slot-available');
    element.classList.add('slot-selected');
    AppState.selectedSlots.push({ key: slotKey, court: courtName, time: timeSlot, price: price });
  }

  updateBookingBar();
}

function updateBookingBar() {
  const bar = document.getElementById('booking-summary-bar');
  if (!bar) return;

  if (AppState.selectedSlots.length > 0) {
    bar.classList.remove('d-none');
    
    let totalPrice = 0;
    AppState.selectedSlots.forEach(s => totalPrice += s.price);
    const deposit = Math.round(totalPrice * 0.3); // 30% deposit rule QĐ-KH02

    document.getElementById('summary-slot-count').innerText = `${AppState.selectedSlots.length} khung giờ`;
    document.getElementById('summary-total-price').innerText = formatVND(totalPrice);
    document.getElementById('summary-deposit-price').innerText = formatVND(deposit);
  } else {
    bar.classList.add('d-none');
  }
}

// Start 10-minute hold timer
function openBookingCheckoutModal() {
  if (AppState.selectedSlots.length === 0) return;

  let totalPrice = 0;
  AppState.selectedSlots.forEach(s => totalPrice += s.price);
  const deposit = Math.round(totalPrice * 0.3);

  document.getElementById('modal-total-amt').innerText = formatVND(totalPrice);
  document.getElementById('modal-deposit-amt').innerText = formatVND(deposit);
  
  // Render selected list in modal
  const listEl = document.getElementById('modal-selected-slots-list');
  if (listEl) {
    listEl.innerHTML = AppState.selectedSlots.map(s => `
      <div class="d-flex justify-content-between align-items-center py-1 border-bottom">
        <span><i class="bi bi-geo-alt-fill text-primary me-1"></i> <strong>${s.court}</strong> (${s.time})</span>
        <span class="fw-bold">${formatVND(s.price)}</span>
      </div>
    `).join('');
  }

  // Generate VietQR URL
  const qrEl = document.getElementById('vietqr-image');
  if (qrEl) {
    const memo = `COC ${AppState.selectedSlots[0].court.replace(' ', '')} ${Date.now().toString().slice(-4)}`;
    qrEl.src = `https://api.vietqr.io/image/970422-0912345678-compact2.jpg?amount=${deposit}&addInfo=${encodeURIComponent(memo)}&accountName=UTE%20SPORT%20BADMINTON`;
  }

  startHoldCountdown();

  const modal = new bootstrap.Modal(document.getElementById('bookingCheckoutModal'));
  modal.show();
}

function startHoldCountdown() {
  clearInterval(AppState.holdInterval);
  AppState.holdRemainingSeconds = 600; // 10 minutes

  const timerEl = document.getElementById('hold-timer-display');
  
  function updateTimer() {
    const mins = Math.floor(AppState.holdRemainingSeconds / 60);
    const secs = AppState.holdRemainingSeconds % 60;
    if (timerEl) {
      timerEl.innerText = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    if (AppState.holdRemainingSeconds <= 0) {
      clearInterval(AppState.holdInterval);
      showToast('Hết thời gian giữ chỗ 10 phút! Khung giờ đã được tự động mở lại.', 'danger');
      const modalEl = document.getElementById('bookingCheckoutModal');
      const modal = bootstrap.Modal.getInstance(modalEl);
      if (modal) modal.hide();
      
      // Reset slots
      AppState.selectedSlots.forEach(s => {
        const el = document.getElementById(`slot-${s.key}`);
        if (el) {
          el.className = 'court-slot slot-available';
        }
      });
      AppState.selectedSlots = [];
      updateBookingBar();
    }
    AppState.holdRemainingSeconds--;
  }

  updateTimer();
  AppState.holdInterval = setInterval(updateTimer, 1000);
}

function confirmPaymentSuccess() {
  clearInterval(AppState.holdInterval);
  const modalEl = document.getElementById('bookingCheckoutModal');
  const modal = bootstrap.Modal.getInstance(modalEl);
  if (modal) modal.hide();

  // Mark slots as booked
  AppState.selectedSlots.forEach(s => {
    const el = document.getElementById(`slot-${s.key}`);
    if (el) {
      el.className = 'court-slot slot-booked';
      el.innerHTML = `<span>ĐÃ ĐẶT</span><small class="text-muted">Online</small>`;
    }
  });

  showToast('Thanh toán đặt cọc 30% thành công! Mã đơn: <strong>BK-' + Math.floor(100000 + Math.random() * 900000) + '</strong> đã gửi qua SMS/Email.', 'success');
  AppState.selectedSlots = [];
  updateBookingBar();
}

// POS System: Add Item to Cart
function posAddItem(itemId) {
  const product = AppState.products.find(p => p.id === itemId);
  if (!product) return;

  if (product.stock <= 0) {
    showToast(`Mặt hàng ${product.name} đã hết tồn kho!`, 'danger');
    return;
  }

  const existing = AppState.posOrder.items.find(i => i.id === itemId);
  if (existing) {
    existing.quantity++;
  } else {
    AppState.posOrder.items.push({
      id: product.id,
      name: product.name,
      price: product.price,
      quantity: 1
    });
  }

  product.stock--;
  renderPosCart();
  showToast(`Đã thêm: <strong>${product.name}</strong> vào hóa đơn.`, 'success');
}

function posRemoveItem(itemId) {
  const idx = AppState.posOrder.items.findIndex(i => i.id === itemId);
  if (idx !== -1) {
    const item = AppState.posOrder.items[idx];
    const product = AppState.products.find(p => p.id === itemId);
    if (product) product.stock += item.quantity;
    
    AppState.posOrder.items.splice(idx, 1);
    renderPosCart();
  }
}

function renderPosCart() {
  const container = document.getElementById('pos-cart-items');
  if (!container) return;

  let totalItemsPrice = 0;
  let html = '';

  AppState.posOrder.items.forEach(item => {
    const itemTotal = item.price * item.quantity;
    totalItemsPrice += itemTotal;
    html += `
      <div class="d-flex justify-content-between align-items-center py-2 border-bottom">
        <div>
          <div class="fw-bold fs-7">${item.name}</div>
          <small class="text-muted">${formatVND(item.price)} x ${item.quantity}</small>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold text-dark">${formatVND(itemTotal)}</span>
          <button class="btn btn-sm btn-outline-danger p-0 px-1" onclick="posRemoveItem('${item.id}')">
            <i class="bi bi-trash"></i>
          </button>
        </div>
      </div>
    `;
  });

  container.innerHTML = html || '<div class="text-center py-4 text-muted"><i class="bi bi-cart-x fs-1 d-block mb-1"></i>Chưa có dịch vụ phụ sinh</div>';

  // Calculate POS Formula (Chương 4 BM-NVQ03)
  const courtPrice = AppState.posOrder.courtPrice;
  const depositPaid = AppState.posOrder.depositPaid;
  const discountAmt = Math.round((courtPrice + totalItemsPrice) * AppState.posOrder.discountRate);
  const remainingTotal = Math.max(0, (courtPrice + totalItemsPrice) - discountAmt - depositPaid);

  if (document.getElementById('pos-court-price')) document.getElementById('pos-court-price').innerText = formatVND(courtPrice);
  if (document.getElementById('pos-services-price')) document.getElementById('pos-services-price').innerText = formatVND(totalItemsPrice);
  if (document.getElementById('pos-discount-price')) document.getElementById('pos-discount-price').innerText = `-${formatVND(discountAmt)}`;
  if (document.getElementById('pos-deposit-paid')) document.getElementById('pos-deposit-paid').innerText = `-${formatVND(depositPaid)}`;
  if (document.getElementById('pos-final-total')) document.getElementById('pos-final-total').innerText = formatVND(remainingTotal);
}

// Print Bill Receipt BM-NVQ03
function openReceiptModal() {
  const totalItemsPrice = AppState.posOrder.items.reduce((sum, i) => sum + i.price * i.quantity, 0);
  const courtPrice = AppState.posOrder.courtPrice;
  const depositPaid = AppState.posOrder.depositPaid;
  const discountAmt = Math.round((courtPrice + totalItemsPrice) * AppState.posOrder.discountRate);
  const remainingTotal = Math.max(0, (courtPrice + totalItemsPrice) - discountAmt - depositPaid);

  document.getElementById('receipt-invoice-no').innerText = 'HD-' + Math.floor(100000 + Math.random() * 900000);
  document.getElementById('receipt-date').innerText = new Date().toLocaleString('vi-VN');
  document.getElementById('receipt-court-fee').innerText = formatVND(courtPrice);
  document.getElementById('receipt-services-fee').innerText = formatVND(totalItemsPrice);
  document.getElementById('receipt-discount').innerText = formatVND(discountAmt);
  document.getElementById('receipt-deposit').innerText = formatVND(depositPaid);
  document.getElementById('receipt-final-amount').innerText = formatVND(remainingTotal);

  const modal = new bootstrap.Modal(document.getElementById('receiptModal'));
  modal.show();
}

// Initialize on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
  renderPosCart();
});

document.addEventListener("DOMContentLoaded", function() {
    const modal = document.getElementById('commonModal');
    const modalPanel = document.getElementById('commonModalPanel');
    const modalTitle = document.getElementById('commonModalTitle');
    const modalContent = document.getElementById('commonModalContent');
    const closeModalBtn = document.getElementById('commonModalCloseBtn');
    const defaultModalSize = 'max-w-md';

    // Đóng modal
    function closeModal() {
        if (modal) {
            modal.classList.add('hidden');
            modalContent.innerHTML = '';
            modalTitle.textContent = 'Loading...';
            modalPanel.className = modalPanel.className.replace(/max-w-\S+/g, defaultModalSize);
        }
    }

    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', closeModal);
    }

    // Mở modal
    function openModal() {
        if (modal) modal.classList.remove('hidden');
    }

    // Xử lý session hết hạn
    function handleSessionExpired() {
        closeModal();
        // Sử dụng window.location.replace để tránh thêm vào history
        window.location.replace('/login?timeout=true');
    }

    // Lắng nghe click trên tất cả nút mở modal
    document.querySelectorAll('.open-modal-btn').forEach(button => {
        button.addEventListener('click', function () {
            const url = this.dataset.modalUrl;
            const title = this.dataset.modalTitle;
            const size = this.dataset.modalSize || defaultModalSize;

            if (!url || !title) {
                console.error('Nút modal thiếu data-modal-url hoặc data-modal-title');
                return;
            }

            // Cập nhật UI
            modalTitle.textContent = title;
            modalPanel.className = modalPanel.className.replace(/max-w-\S+/g, size);
            modalContent.innerHTML = '<div class="text-center p-8 font-medium text-gray-500">Đang tải nội dung...</div>';

            openModal();

            // Fetch nội dung
            fetch(url, {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'include' // Quan trọng: gửi cả session cookie
            })
                .then(response => {
                    // // Xử lý session hết hạn
                    if (response.status === 401) {
                        handleSessionExpired();
                        throw new Error('SESSION_EXPIRED');
                    }

                    // Xử lý redirect (302, 303, etc.)
                    if (response.redirected) {
                        if (response.url.includes('/login')) {
                            handleSessionExpired();
                            throw new Error('REDIRECT_TO_LOGIN');
                        }
                    }

                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }

                    return response.text();
                })
                .then(html => {
                    // Kiểm tra nếu đã redirect thì không xử lý tiếp
                    if (!html || window.location.pathname.includes('/login')) {
                        return;
                    }


                    // Chèn nội dung hợp lệ
                    modalContent.innerHTML = html;

                    // Gán lại sự kiện cho các nút cancel
                    modalContent.querySelectorAll('.modal-cancel-btn').forEach(cancelBtn => {
                        cancelBtn.addEventListener('click', closeModal);
                    });
                })
                .catch(error => {
                    // Chỉ log error nếu không phải do session expired
                    if (error.message !== 'SESSION_EXPIRED' &&
                        error.message !== 'REDIRECT_TO_LOGIN' &&
                        !window.location.pathname.includes('/login')) {
                        console.error('Fetch error:', error);
                        modalContent.innerHTML = `
                        <div class="text-center p-8">
                            <p class="text-red-500 font-semibold text-sm">Đã xảy ra lỗi. Vui lòng thử lại.</p>
                            <button onclick="location.reload()" class="mt-4 px-4 py-2 bg-blue-500 text-white rounded-md text-xs">
                                Tải lại trang
                            </button>
                        </div>`;
                    }
                });
        });
    });

    // Thêm event để đóng modal khi click bên ngoài
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            closeModal();
        }
    });

    // Thêm phím ESC để đóng modal
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && !modal.classList.contains('hidden')) {
            closeModal();
        }
    });
});
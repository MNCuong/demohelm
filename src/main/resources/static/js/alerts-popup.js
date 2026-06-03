/**
 * File: /static/js/app-alerts.js
 * * Script này sẽ tự động chạy khi trang được tải xong,
 * kiểm tra xem có thông báo lỗi hoặc thành công nào được
 * gắn vào thẻ <body> hay không và hiển thị popup.
 */

// Chờ cho toàn bộ cây DOM được tải xong
document.addEventListener("DOMContentLoaded", function() {

    // Lấy thẻ body
    const bodyElement = document.body;

    // Lấy nội dung từ thuộc tính 'data-error-message'
    // .dataset.errorMessage tự động map với data-error-message
    const errorMessage = bodyElement.dataset.errorMessage;
    
    if (errorMessage) {
        Swal.fire({
            icon: 'error',
            title: 'Lỗi Nghiệp Vụ',
            html: errorMessage,
            confirmButtonText: 'Đóng',
            width: '400px' // Giữ lại tùy chỉnh kích thước nhỏ
        });
    }

    // Tương tự, kiểm tra thông báo thành công
    const successMessage = bodyElement.dataset.successMessage;
    
    if (successMessage) {
        Swal.fire({
            icon: 'success',
            title: 'Thành công!',
            html: successMessage,
            timer: 2500,
            showConfirmButton: false,
            width: '400px'
        });
    }
});
/**
 * monitor-core.js
 * Chứa các hàm tiện ích dùng chung cho toàn bộ Web Monitor
 */

const MonitorCore = {
    // Hàm gọi API chung
    async get(url) {
        try {
            const response = await fetch(url);
            if (!response.ok) {
                console.warn(`API Error ${response.status} at: ${url}`);
                return null;
            }
            return await response.json();
        } catch (error) {
            console.error("Network Error:", error);
            return null;
        }
    },

    // Hàm format số (Ví dụ: 1200 -> 1,200)
    formatNumber(num) {
        if (num === null || num === undefined) return "--";
        return new Intl.NumberFormat('en-US').format(num);
    },

    // Hàm lấy thời gian hiện tại string
    getCurrentTime() {
        const now = new Date();
        return `${now.toLocaleDateString()} ${now.toLocaleTimeString()}`;
    },

    //  Hàm tạo hiệu ứng nháy màu khi dữ liệu thay đổi
    // elementId: ID của thẻ HTML
    // successClass: Lớp màu khi thành công (mặc định màu xanh)
    animateChange(elementId, successClass = 'text-blue-600') {
        const element = document.getElementById(elementId);
        if (!element) return;

        // Xóa màu cũ (thường là màu đen/xám)
        const originalClass = 'text-gray-900'; 
        element.classList.remove(originalClass);
        element.classList.add(successClass);

        // Sau 0.5s thì trả về màu cũ
        setTimeout(() => {
            element.classList.remove(successClass);
            element.classList.add(originalClass);
        }, 500);
    },
    
    // Hàm cập nhật text an toàn (kiểm tra null)
    setText(elementId, text) {
        const el = document.getElementById(elementId);
        if (el) el.innerText = text;
    }
};
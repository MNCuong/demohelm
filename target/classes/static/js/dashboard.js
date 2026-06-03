/**
 * dashboard.js
 * Logic riêng cho trang Dashboard
 * Yêu cầu: Phải nhúng monitor-core.js trước file này
 */

const DashboardController = {
    config: {
        topicName: "",      // Sẽ được set từ HTML
        refreshRate: 5000   // 5 giây
    },

    // Hàm khởi tạo, nhận tham số từ Thymeleaf truyền vào
    init(topicName) {
        this.config.topicName = topicName;
        console.log("Dashboard Init for topic:", topicName);

        // Gọi ngay lần đầu
        this.refreshAllData();

        // Đặt lịch chạy định kỳ
        setInterval(() => {
            this.updateLastUpdatedTime();
            this.refreshAllData();
        }, this.config.refreshRate);
    },
    // Hàm điều phối việc update dữ liệu
    refreshAllData() {
        this.updateKafkaStats();
        this.updateLastUpdatedTime();
        this.updateThroughput();
        this.updateAvgLatency();
        this.updateErrorRate();
    },

    updateLastUpdatedTime() {
        // Dùng hàm chung từ MonitorCore
        MonitorCore.setText('last-updated-time', MonitorCore.getCurrentTime());
    },

    async updateKafkaStats() {
        //Gọi API bằng hàm chung
        const url = `/api/kafka/count/${this.config.topicName}`;
        const data = await MonitorCore.get(url);

        if (data) {
            //Cập nhật giao diện
            const topicEl = document.getElementById('monitor-topic-name');
            topicEl.textContent = data.topic;

            // Thiết lập link Kafka UI
            const kafkaUiLink = `http://192.168.1.205:8080/ui/clusters/Kafka_205/all-topics/${encodeURIComponent(data.topic)}/messages?keySerde=String&valueSerde=String&limit=100`;
            topicEl.setAttribute('href', kafkaUiLink);

            MonitorCore.setText('total-msg-count', MonitorCore.formatNumber(data.count));

            //Gọi hiệu ứng nháy màu
            MonitorCore.animateChange('total-msg-count');
        } else {
            // Xử lý khi lỗi
            MonitorCore.setText('total-msg-count', 'Error');
            document.getElementById('total-msg-count').classList.add('text-red-500');
        }
    },

    async updateThroughput() {
        // 1. Gọi API mới
        const url = `/api/kafka/throughput/${this.config.topicName}`;
        const data = await MonitorCore.get(url);

        if (data) {
            // 2. Cập nhật số liệu lên màn hình
            // data.throughput trả về số (ví dụ: 50.5), formatNumber sẽ biến nó thành chuỗi
            MonitorCore.setText('throughput-count', MonitorCore.formatNumber(data.throughput));

            // 3. Nháy màu để báo hiệu
            MonitorCore.animateChange('throughput-count');
        } else {
            MonitorCore.setText('throughput-count', 'Err');
        }
    },

    async updateAvgLatency(){
        // 1. Gọi API mới
        const url = `/api/kafka/avg-latency`;
        const data = await MonitorCore.get(url);

        if (data !== null && data !== undefined) {
            // 2. Cập nhật số liệu lên màn hình
            // data.throughput trả về số (ví dụ: 50.5), formatNumber sẽ biến nó thành chuỗi
            MonitorCore.setText('avg-latency', MonitorCore.formatNumber(data));

            // 3. Nháy màu để báo hiệu
            MonitorCore.animateChange('avg-latency');
        } else {
            MonitorCore.setText('avg-latency', 'Err');
        }
    },

    async updateErrorRate() {
        // Lấy ngày hiện tại
        const dateParam = this.getCurrentDateISO();

        // Truyền tham số vào URL
        const url = `/api/kafka/error-rate?date=${dateParam}`;

        const data = await MonitorCore.get(url);

        // Kiểm tra dữ liệu (data có thể là số 0 nên check null/undefined chuẩn hơn)
        if (data !== null && data !== undefined) {
            // Format số liệu: Giữ 2 số thập phân (ví dụ: 1.25)
            // const formattedRate = Number(data).toFixed(2) + "%";
            // Cập nhật lên DOM (Lưu ý: ID là error-rate chứ không phải avg-latency)
            MonitorCore.setText('error-rate', data);

            // Nháy màu
            MonitorCore.animateChange('error-rate');

            // Logic phụ: Cập nhật text so sánh (Mockup)
            MonitorCore.setText('error-rate-vs-ytd', `Data for ${dateParam}`);
        } else {
            MonitorCore.setText('error-rate', 'Err');
        }
    },


    getCurrentDateISO() {
        const now = new Date();
        return now.toISOString().split('T')[0];
    }

};
document.addEventListener('DOMContentLoaded', (event) => {
    
    // Lấy các phần tử
    const selectAllCheckbox = document.getElementById('checkbox-all');
    const rowCheckboxes = document.querySelectorAll('.row-checkbox');
    const processButton = document.getElementById('process-selected-btn');
    const totalRows = rowCheckboxes.length;

    /**
     * HÀM MỚI: Cập nhật trạng thái của checkbox "Chọn tất cả"
     * dựa trên việc các checkbox hàng đã được chọn hay chưa.
     */
    function updateSelectAllState() {
        if (!selectAllCheckbox) return;

        let checkedCount = 0;
        rowCheckboxes.forEach(checkbox => {
            if (checkbox.checked) {
                checkedCount++;
            }
        });

        if (totalRows === 0) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
        } else if (checkedCount === 0) {
            // Không có hàng nào được chọn
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
        } else if (checkedCount === totalRows) {
            // Tất cả các hàng đã được chọn
            selectAllCheckbox.checked = true;
            selectAllCheckbox.indeterminate = false;
        } else {
            // Một vài hàng được chọn (trạng thái "dở dang")
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = true;
        }
    }

    // 1. Chức năng "Chọn tất cả" (Master -> Slaves)
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('click', function() {
            // Cập nhật tất cả các hàng
            rowCheckboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
            
            // Cập nhật lại trạng thái (để xóa "indeterminate")
            updateSelectAllState();
        });
    }

    // 2. BỔ SUNG: Chức năng cập nhật "Chọn tất cả" (Slaves -> Master)
    rowCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('click', function() {
            // Khi click vào 1 hàng, gọi hàm cập nhật
            updateSelectAllState();
        });
    });

    // 3. Chức năng "Lấy các ID đã chọn" (Giữ nguyên)
    if (processButton) {
        processButton.addEventListener('click', function() {
            const selectedValues = [];
            rowCheckboxes.forEach(checkbox => {
                if (checkbox.checked) {
                    selectedValues.push(checkbox.value);
                }
            });

            if (selectedValues.length === 0) {
                alert('Vui lòng chọn ít nhất một mục.');
                return;
            }

            alert('Các mục đã chọn: ' + selectedValues.join(', '));
            
            // fetch('/users/delete-batch', { ... })
        });
    }

    // (Tùy chọn) Chạy một lần khi tải trang, phòng trường hợp có checkbox được chọn sẵn
    updateSelectAllState();
});


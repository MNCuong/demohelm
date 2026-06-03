const DashboardRealtime = (function () {
    const STATUS_CLASS = {
        UP: {
            icon: 'bg-green-100 text-green-600',
            text: 'text-green-600',
            dot: 'bg-green-500',
            label: 'UP'
        },
        WARNING: {
            icon: 'bg-yellow-100 text-yellow-600',
            text: 'text-yellow-600',
            dot: 'bg-yellow-500',
            label: 'WARNING'
        },
        DOWN: {
            icon: 'bg-red-100 text-red-600',
            text: 'text-red-600',
            dot: 'bg-red-500',
            label: 'DOWN'
        },
        UNKNOWN: {
            icon: 'bg-gray-100 text-gray-500',
            text: 'text-gray-500',
            dot: 'bg-gray-400',
            label: 'UNKNOWN'
        }
    };

    /* ================= SUMMARY ================= */
    function updateSummary(summary) {
        if (!summary) return;

        flashTextJS('summary-total-in', summary.totalIn);
        flashTextJS('summary-total-out', summary.totalOut);
        flashTextJS('summary-total-all', summary.totalAll);
    }


    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) el.innerText = value ?? 0;
    }


    /* ================= ALERTS ================= */
    function updateAlerts(alerts) {
        const tbody = document.getElementById('alert-table-body');
        if (!tbody) return;

        // Clear cũ
        tbody.innerHTML = '';

        // Không có dữ liệu
        if (!Array.isArray(alerts) || alerts.length === 0) {
            tbody.innerHTML = `
            <tr>
                <td colspan="3" 
                    class="py-6 text-center text-gray-500 italic">
                    Hiện tại chưa có cảnh báo
                </td>
            </tr>
        `;
            return;
        }

        // Có dữ liệu
        alerts.forEach(a => {

            const color =
                a.severity === 'HIGH' ? 'red' :
                    a.severity === 'MEDIUM' ? 'yellow' :
                        'blue';

            tbody.insertAdjacentHTML('beforeend', `
            <tr class="hover:bg-gray-50 transition">
                <td class="py-4">
                    <span class="px-3 py-1 rounded-full text-xs font-semibold
                        bg-${color}-100 text-${color}-700 ring-1 ring-${color}-300">
                        ${a.severity}
                    </span>
                </td>

                <td class="py-4 text-gray-700 truncate">
                    <div>${a.message}</div>
                    ${formatUsage(a)}
                </td>

                <td class="py-4 text-gray-500 text-right font-mono">
                    ${formatDateTime(a.time)}
                </td>
            </tr>
        `);
        });
    }
    function formatUsage(a) {
        if (a.usedValue == null || a.totalValue == null) return "";

        const used = Number(a.usedValue).toFixed(0);
        const total = Number(a.totalValue).toFixed(0);
        const percent = a.usedPercent != null
            ? Number(a.usedPercent).toFixed(1)
            : ((used / total) * 100).toFixed(1);

        return `
        <div class="text-xs text-gray-500 mt-1">
            ${used} / ${total} (${percent}%)
        </div>
    `;
    }

    function formatDateTime(timeStr) {
        // "2026-02-10 09:41:39.016"
        const date = new Date(timeStr.replace(" ", "T"));

        const hh = String(date.getHours()).padStart(2, "0");
        const mm = String(date.getMinutes()).padStart(2, "0");
        const ss = String(date.getSeconds()).padStart(2, "0");

        const dd = String(date.getDate()).padStart(2, "0");
        const MM = String(date.getMonth() + 1).padStart(2, "0");
        const yyyy = date.getFullYear();

        return `${hh}:${mm}:${ss} - ${dd}/${MM}/${yyyy}`;
    }

    /* ========== CORE COMPONENTS ========== */
    function renderCoreComponents(coreComponents) {
        const container = document.getElementById('core-components');
        if (!container || !coreComponents) return;

        container.innerHTML = '';

        coreComponents.forEach(comp => {
            container.insertAdjacentHTML('beforeend', `
                <div data-comp-name="${comp.name}"
                     class="flex items-center justify-between px-4 py-3 rounded-lg hover:bg-gray-50 transition">

                    <div class="flex items-center gap-3">
                        <div class="w-9 h-9 flex items-center justify-center rounded-lg
                                    bg-gray-100 text-gray-500 comp-icon">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor"
                                 viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round"
                                      stroke-width="2"
                                      d="M4 7c0-1.657 3.582-3 8-3s8 1.343 8 3M4 7v10
                                         c0 1.657 3.582 3 8 3s8-1.343 8-3V7"/>
                            </svg>
                        </div>

                        <span class="text-gray-800 font-medium">
                            ${comp.name}
                        </span>
                    </div>

                    <div class="flex items-center gap-2">
                        <span class="text-sm font-semibold text-gray-400 comp-status">
                            UNKNOWN
                        </span>
                       <span class="relative inline-flex h-2.5 w-2.5 rounded-full comp-dot">
    <span class="absolute inline-flex h-full w-full rounded-full
                 opacity-75 comp-ping"></span>
</span>

                    </div>
                </div>
            `);
        });
    }

    function updateCoreComponents(coreComponents) {
        coreComponents.forEach(comp => {
            const row = document.querySelector(`[data-comp-name="${comp.name}"]`);
            if (!row) return;

            const icon = row.querySelector('.comp-icon');
            const text = row.querySelector('.comp-status');
            const dot = row.querySelector('.comp-dot');
            const ping = row.querySelector('.comp-ping');

            icon.className = 'w-9 h-9 flex items-center justify-center rounded-lg comp-icon';
            text.className = 'text-sm font-semibold comp-status';
            dot.className = 'relative inline-flex h-2.5 w-2.5 rounded-full comp-dot';
            ping.className = 'absolute inline-flex h-full w-full rounded-full opacity-75 comp-ping';

            const cfg = STATUS_CLASS[comp.status] || STATUS_CLASS.UNKNOWN;

            icon.classList.add(...cfg.icon.split(' '));
            text.classList.add(cfg.text);
            text.innerText = cfg.label;
            dot.classList.add(cfg.dot);

            if (comp.status !== 'UNKNOWN') {
                ping.classList.add(cfg.dot, 'animate-ping');
            } else {
                ping.classList.remove('animate-ping');
            }
        });
    }



    /* ================= POLLING ================= */
    async function refresh() {
        const data = await MonitorCore.get('/api/monitor/realtime');
        if (!data) return;
        console.log(data)
        updateSummary(data.summary);
        renderCoreComponents(data.coreComponents);
        updateCoreComponents(data.coreComponents);
        updateAlerts(data.recentAlerts);
    }

    function init(intervalMs = 30000) {
        refresh();
        setInterval(refresh, intervalMs);
    }
    function flashTextJS(id, value) {
        const el = document.getElementById(id);
        if (!el) return;

        // cập nhật giá trị
        el.innerText = value ?? 0;

        // flash màu
        el.classList.add('text-blue-600');

        // trả về trạng thái thường (không màu)
        setTimeout(() => {
            el.classList.remove('text-blue-600');
        }, 350);
    }


    return { init };
})();

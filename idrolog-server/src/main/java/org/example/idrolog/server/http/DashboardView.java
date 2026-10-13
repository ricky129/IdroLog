package org.example.idrolog.server.http;

public class DashboardView {

    public static final String HTML = """
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IdroLog</title>
    <link rel="icon" href="data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>🌊</text></svg>">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.7/dist/chart.umd.min.js"></script>
    <style>
        :root {
            --bg-body: #f3f4f6;
            --bg-card: #ffffff;
            --text-main: #111827;
            --text-bold: #374151;
            --text-muted: #6b7280;
            --color-primary: #0ea5e9;
            --color-orange: #f5a623;
            --color-red: #fb3640;
            --border: #e5e7eb;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        }

        body {
            background-color: var(--bg-body);
            color: var(--text-main);
            min-height: 100vh;
            padding: 24px;
        }

        .container {
            max-width: 1100px;
            margin: 0 auto;
        }

        header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 24px;
        }

        .header-title {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .header-title h1 {
            font-size: 32px;
            font-weight: 800;
            color: var(--text-main);
            letter-spacing: -0.5px;
        }

        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            font-weight: 600;
            padding: 4px 10px;
            border-radius: 999px;
            transition: all 0.2s ease-in-out;
        }

        .status-badge.live {
            background: #e0f2fe;
            color: #0369a1;
        }

        .status-badge.offline {
            background: #fee2e2;
            color: #b91c1c;
        }

        .pulse-dot {
            width: 8px;
            height: 8px;
            background-color: #0284c7;
            border-radius: 50%;
            animation: pulse 2s infinite;
        }

        .offline-dot {
            width: 8px;
            height: 8px;
            background-color: #dc2626;
            border-radius: 50%;
        }

        @keyframes pulse {
            0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(2, 132, 199, 0.7); }
            70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(2, 132, 199, 0); }
            100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(2, 132, 199, 0); }
        }

        .kpi-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 16px;
            margin-bottom: 24px;
        }

        .card {
            background-color: var(--bg-card);
            border-radius: 16px;
            padding: 18px 24px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .card-label {
            font-size: 13px;
            font-weight: 600;
            color: var(--text-muted);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .kpi-value-row {
            display: flex;
            align-items: baseline;
            gap: 8px;
            margin-bottom: 8px;
        }

        .kpi-value {
            font-size: 34px;
            font-weight: 800;
            color: var(--color-primary);
            line-height: 1;
        }

        .kpi-subtext {
            font-size: 14px;
            font-weight: 600;
            color: var(--text-muted);
        }

        .kpi-footer {
            display: flex;
            justify-content: space-between;
            font-size: 13px;
            font-weight: 600;
            color: var(--text-bold);
            margin-top: 4px;
        }

        .alert-card {
            background: linear-gradient(135deg, #2563eb, #1d4ed8);
            color: #ffffff;
            box-shadow: 0 8px 24px rgba(37, 99, 235, 0.25);
        }

        .alert-card.warning {
            background: linear-gradient(135deg, #d97706, #b45309);
            box-shadow: 0 8px 24px rgba(217, 119, 6, 0.25);
        }

        .alert-card.danger {
            background: linear-gradient(135deg, #dc2626, #b91c1c);
            box-shadow: 0 8px 24px rgba(220, 38, 38, 0.25);
        }

        .alert-card .card-label {
            color: rgba(255, 255, 255, 0.8);
        }

        .alert-title {
            font-size: 20px;
            font-weight: 800;
            margin-bottom: 6px;
        }

        .alert-desc {
            font-size: 14px;
            color: rgba(255, 255, 255, 0.9);
            font-weight: 500;
        }

        .chart-card {
            background-color: var(--bg-card);
            border-radius: 16px;
            padding: 24px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
        }

        .chart-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 12px;
            margin-bottom: 20px;
        }

        .chart-title {
            font-size: 18px;
            font-weight: 700;
            color: var(--text-main);
        }

        .chart-controls {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .chart-controls label {
            font-size: 14px;
            font-weight: 600;
            color: var(--text-muted);
        }

        .select-hours {
            background: #f9fafb;
            border: 1px solid var(--border);
            border-radius: 10px;
            padding: 8px 14px;
            font-size: 14px;
            font-weight: 600;
            color: var(--text-bold);
            outline: none;
            cursor: pointer;
            transition: all 0.2s;
        }

        .select-hours:hover {
            background: #f3f4f6;
        }

        .chart-wrapper {
            position: relative;
            height: 420px;
            width: 100%;
        }

        .footer-note {
            text-align: center;
            margin-top: 24px;
            font-size: 13px;
            color: var(--text-muted);
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <div class="header-title">
                <h1>IdroLog</h1>
                <span id="statusBadge" class="status-badge live" style="display: none;">
                    <span class="pulse-dot"></span>
                    Live
                </span>
            </div>
            <div id="clock" style="font-weight: 600; color: var(--text-muted); font-size: 14px;"></div>
        </header>

        <section class="kpi-grid">
            <div class="card">
                <div>
                    <div class="card-label">Livello Attuale</div>
                    <div class="kpi-value-row">
                        <span class="kpi-value" id="currentLevel">-- m</span>
                        <span class="kpi-subtext" id="lastUpdate">(--:--)</span>
                    </div>
                </div>
                <div class="kpi-footer">
                    <span id="statusLabel">Stato: Normale</span>
                    <span id="trendLabel" style="color: var(--text-muted);">Tendenza: --</span>
                </div>
            </div>

            <div class="card">
                <div>
                    <div class="card-label" id="precipitationTitle">Precipitazioni 24h</div>
                    <div class="kpi-value-row">
                        <span class="kpi-value" id="precipitationValue">0.0 mm</span>
                    </div>
                </div>
                <div class="kpi-footer">
                    <span id="precipitationStatus" style="color: var(--text-muted);">Sensore collegato</span>
                </div>
            </div>

            <div class="card alert-card" id="alertCard">
                <div>
                    <div class="card-label">Avvisi</div>
                    <div class="alert-title" id="alertTitle">Nessuna Allerta</div>
                </div>
                <div class="alert-desc" id="alertDesc">Condizioni idrometriche stabili.</div>
            </div>
        </section>

        <section class="chart-card">
            <div class="chart-header">
                <div class="chart-title">Andamento Livello Idrometrico</div>
                <div class="chart-controls">
                    <label for="hoursSelect">Visualizza:</label>
                    <select id="hoursSelect" class="select-hours">
                        <option value="1">1h</option>
                        <option value="2">2h</option>
                        <option value="6">6h</option>
                        <option value="12">12h</option>
                        <option value="24" selected>24h</option>
                        <option value="48">48h</option>
                    </select>
                </div>
            </div>
            <div class="chart-wrapper">
                <canvas id="levelChart"></canvas>
            </div>
        </section>
    </div>

    <script>
        let chart = null;

        function initChart() {
            const ctx = document.getElementById('levelChart').getContext('2d');
            const gradient = ctx.createLinearGradient(0, 0, 0, 400);
            gradient.addColorStop(0, 'rgba(14, 165, 233, 0.35)');
            gradient.addColorStop(1, 'rgba(14, 165, 233, 0.00)');

            chart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: 'Livello Idrometrico (m)',
                        data: [],
                        borderColor: '#0ea5e9',
                        borderWidth: 3.5,
                        backgroundColor: gradient,
                        fill: true,
                        tension: 0.35,
                        pointRadius: 3,
                        pointHoverRadius: 6,
                        pointBackgroundColor: '#0ea5e9',
                        pointBorderColor: '#ffffff',
                        pointBorderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    interaction: {
                        intersect: false,
                        mode: 'index'
                    },
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: '#111827',
                            titleColor: '#ffffff',
                            bodyColor: '#e0f2fe',
                            padding: 12,
                            cornerRadius: 8,
                            callbacks: {
                                label: context => ` Livello: ${context.parsed.y.toFixed(2)} m`
                            }
                        }
                    },
                    scales: {
                        x: {
                            grid: { display: false },
                            ticks: {
                                color: '#9ca3af',
                                maxTicksLimit: 12,
                                font: { weight: 600, size: 12 }
                            }
                        },
                        y: {
                            beginAtZero: true,
                            suggestedMax: 1.5,
                            grid: {
                                color: '#f3f4f6',
                                borderDash: [5, 5]
                            },
                            ticks: {
                                color: '#9ca3af',
                                font: { weight: 600, size: 12 },
                                callback: value => value.toFixed(2) + ' m'
                            }
                        }
                    }
                }
            });
        }

        function updateStatusBadge(isOnline) {
            const badge = document.getElementById('statusBadge');
            if (!badge) return;
            badge.style.display = 'inline-flex';
            if (isOnline) {
                badge.className = 'status-badge live';
                badge.innerHTML = '<span class="pulse-dot"></span> Live';
            } else {
                badge.className = 'status-badge offline';
                badge.innerHTML = '<span class="offline-dot"></span> Offline';
            }
        }

        async function refreshData() {
            const hours = document.getElementById('hoursSelect').value;
            document.getElementById('precipitationTitle').textContent = `Precipitazioni ${hours}h`;

            try {
                const [riverRes, rainRes] = await Promise.all([
                    fetch(`/api/river_levels?hours=${hours}`).then(r => {
                        if (!r.ok) throw new Error(`HTTP ${r.status}`);
                        return r.json();
                    }),
                    fetch(`/api/precipitation?hours=${hours}`).then(r => {
                        if (!r.ok) throw new Error(`HTTP ${r.status}`);
                        return r.json();
                    })
                ]);

                updateStatusBadge(true);

                if (Array.isArray(riverRes) && riverRes.length > 0) {
                    const last = riverRes[riverRes.length - 1];
                    const currentVal = last.value;
                    document.getElementById('currentLevel').textContent = `${currentVal.toFixed(2)} m`;

                    const dateObj = new Date(last.timestamp);
                    const timeStr = dateObj.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' });
                    document.getElementById('lastUpdate').textContent = `${timeStr}`;

                    const levelEl = document.getElementById('currentLevel');
                    const statusEl = document.getElementById('statusLabel');
                    const alertCard = document.getElementById('alertCard');
                    const alertTitle = document.getElementById('alertTitle');
                    const alertDesc = document.getElementById('alertDesc');

                    alertCard.className = 'card alert-card';

                    if (currentVal >= 3.0) {
                        levelEl.style.color = 'var(--color-red)';
                        statusEl.textContent = 'Stato: Allerta Rossa';
                        alertCard.classList.add('danger');
                        alertTitle.textContent = 'Allerta Rossa';
                        alertDesc.textContent = 'Superata soglia di criticità elevata!';
                    } else if (currentVal >= 2.0) {
                        levelEl.style.color = 'var(--color-orange)';
                        statusEl.textContent = 'Stato: Allerta Arancione';
                        alertCard.classList.add('warning');
                        alertTitle.textContent = 'Allerta Arancione';
                        alertDesc.textContent = 'Livello idrometrico in crescita rapida.';
                    } else {
                        levelEl.style.color = 'var(--color-primary)';
                        statusEl.textContent = 'Stato: Normale';
                        alertTitle.textContent = 'Nessuna Allerta';
                        alertDesc.textContent = 'Condizioni idrometriche stabili.';
                    }

                    if (riverRes.length >= 2) {
                        const prev = riverRes[riverRes.length - 2].value;
                        const diff = currentVal - prev;
                        if (diff > 0.02) {
                            document.getElementById('trendLabel').textContent = 'Tendenza: ↗ In salita';
                        } else if (diff < -0.02) {
                            document.getElementById('trendLabel').textContent = 'Tendenza: ↘ In discesa';
                        } else {
                            document.getElementById('trendLabel').textContent = 'Tendenza: → Stazionario';
                        }
                    }

                    const labels = riverRes.map(item => {
                        const d = new Date(item.timestamp);
                        return d.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' });
                    });
                    const values = riverRes.map(item => item.value);

                    chart.data.labels = labels;
                    chart.data.datasets[0].data = values;
                    chart.update('none');
                }

                if (Array.isArray(rainRes) && rainRes.length > 0) {
                    const totalRain = rainRes.reduce((acc, cur) => acc + (cur.value || 0), 0);
                    document.getElementById('precipitationValue').textContent = `${totalRain.toFixed(1)} mm`;
                    document.getElementById('precipitationStatus').textContent = 'Sensore attivo';
                } else {
                    document.getElementById('precipitationValue').textContent = '0.0 mm';
                    document.getElementById('precipitationStatus').textContent = 'Nessun dato';
                }

            } catch (err) {
                console.error('Errore durante il recupero dei dati:', err);
                updateStatusBadge(false);
            }
        }

        document.getElementById('hoursSelect').addEventListener('change', refreshData);

        window.addEventListener('DOMContentLoaded', () => {
            initChart();
            refreshData();
            setInterval(refreshData, 60000);
        });
    </script>
</body>
</html>
""";
}

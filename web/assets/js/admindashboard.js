// Draws the Users by Role donut chart on the Admin Dashboard.
// Expects window.roleData to be set by the JSP before this script runs.
(function () {
    var ctx = document.getElementById('roleChart');
    if (!ctx || !window.roleData) return;

    var isDark = function () {
        return document.documentElement.dataset.theme === 'dark'
            || (!document.documentElement.dataset.theme
                && matchMedia('(prefers-color-scheme:dark)').matches);
    };

    new Chart(ctx.getContext('2d'), {
        type: 'doughnut',
        data: {
            labels: ['Admin', 'Expert', 'Teacher', 'Student'],
            datasets: [{
                data: [
                    window.roleData.admin,
                    window.roleData.expert,
                    window.roleData.teacher,
                    window.roleData.student
                ],
                backgroundColor: ['#ef4444', '#22c55e', '#f59e0b', '#06b6d4'],
                borderWidth: 2,
                borderColor: isDark() ? '#161a23' : '#ffffff'
            }]
        },
        options: {
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: isDark() ? '#98a0b3' : '#6b7280',
                        padding: 16,
                        font: { size: 13 }
                    }
                }
            }
        }
    });
})();

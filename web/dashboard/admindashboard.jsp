<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css" />
        <title>Admin Dashboard – AMS</title>
    </head>
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <div class="app-layout">

            <%-- Sidebar --%>
            <nav class="sidebar">
                <span class="sidebar-label">Management</span>

                <a href="${pageContext.request.contextPath}/admindashboard" class="active">
                    <svg viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
                    Dashboard
                </a>

                <a href="${pageContext.request.contextPath}/admin/users">
                    <svg viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                    User Management
                </a>

                <a href="${pageContext.request.contextPath}/admin/settings">
                    <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
                    System Settings
                </a>

                <div class="sidebar-divider"></div>

                <form method="post" action="${pageContext.request.contextPath}/auth" style="margin:0;">
                    <input type="hidden" name="action" value="logout" />
                    <button type="submit" class="sidebar-logout-btn">
                        <svg viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                        Logout
                    </button>
                </form>
            </nav>

            <%-- Main content --%>
            <main class="main-content">

                <div class="hello">
                    <h1>Admin Dashboard</h1>
                    <p class="sub">Here's an overview of your system today.</p>
                </div>

                <%-- Stats --%>
                <div class="stats-grid">
                    <div class="stat">
                        <div class="stat-label">Total Users</div>
                        <div class="stat-value">${dash.totalUsers}</div>
                    </div>
                    <div class="stat">
                        <div class="stat-label">Total Subjects</div>
                        <div class="stat-value">${dash.totalSubjects}</div>
                    </div>
                    <div class="stat">
                        <div class="stat-label">Active Users</div>
                        <div class="stat-value" style="color:var(--okT)">${dash.activeUsers}</div>
                    </div>
                    <div class="stat">
                        <div class="stat-label">Inactive Users</div>
                        <div class="stat-value" style="color:var(--badT)">${dash.inactiveUsers}</div>
                    </div>
                    <div class="stat">
                        <div class="stat-label">Active Subjects</div>
                        <div class="stat-value" style="color:var(--okT)">${dash.activeSubjects}</div>
                    </div>
                    <div class="stat">
                        <div class="stat-label">Inactive Subjects</div>
                        <div class="stat-value" style="color:var(--badT)">${dash.inactiveSubjects}</div>
                    </div>
                </div>

                <%-- Bottom row: chart + role breakdown --%>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-top:4px;">
                    <%-- Donut chart: Users by Role --%>
                    <div class="panel">
                        <div class="panel-head">
                            <h2>Users by Role</h2>
                        </div>
                        <div style="display:flex;justify-content:center;align-items:center;padding:12px 0 8px;">
                            <div style="position:relative;width:260px;height:260px;">
                                <canvas id="roleChart"></canvas>
                            </div>
                        </div>
                    </div>

                    <%-- Role breakdown list --%>
                    <div class="panel">
                        <div class="panel-head">
                            <h2>Role Breakdown</h2>
                        </div>
                        <p class="sub" style="margin-bottom:16px;">Number of users per role.</p>
                        <ul class="due-list">
                            <li class="due-item">
                                <div class="due-main">
                                    <span class="due-title">
                                        <span style="display:inline-block;width:12px;height:12px;border-radius:50%;background:#ef4444;margin-right:8px;vertical-align:middle;"></span>
                                        Admin
                                    </span>
                                    <span class="due-sub">System administrators</span>
                                </div>
                                <span class="badge">${dash.adminCount} ${dash.adminCount == 1 ? 'user' : 'users'}</span>
                            </li>
                            <li class="due-item">
                                <div class="due-main">
                                    <span class="due-title">
                                        <span style="display:inline-block;width:12px;height:12px;border-radius:50%;background:#22c55e;margin-right:8px;vertical-align:middle;"></span>
                                        Expert
                                    </span>
                                    <span class="due-sub">Subject matter experts</span>
                                </div>
                                <span class="badge">${dash.expertCount} ${dash.expertCount == 1 ? 'user' : 'users'}</span>
                            </li>
                            <li class="due-item">
                                <div class="due-main">
                                    <span class="due-title">
                                        <span style="display:inline-block;width:12px;height:12px;border-radius:50%;background:#f59e0b;margin-right:8px;vertical-align:middle;"></span>
                                        Teacher
                                    </span>
                                    <span class="due-sub">Class instructors</span>
                                </div>
                                <span class="badge">${dash.teacherCount} ${dash.teacherCount == 1 ? 'user' : 'users'}</span>
                            </li>
                            <li class="due-item">
                                <div class="due-main">
                                    <span class="due-title">
                                        <span style="display:inline-block;width:12px;height:12px;border-radius:50%;background:#06b6d4;margin-right:8px;vertical-align:middle;"></span>
                                        Student
                                    </span>
                                    <span class="due-sub">Enrolled students</span>
                                </div>
                                <span class="badge">${dash.studentCount} ${dash.studentCount == 1 ? 'user' : 'users'}</span>
                            </li>
                        </ul>
                    </div>
                </div>

                <%-- Classes section: Total Classes + filter by Subject --%>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-top:16px;">

                    <%-- Total Classes --%>
                    <div class="panel" style="display:flex;flex-direction:column;gap:8px;">
                        <div class="panel-head">
                            <h2>Total Classes</h2>
                        </div>
                        <div style="font-size:2.8rem;font-weight:700;color:var(--accent,#4f8ef7);">
                            ${dash.totalClasses}
                        </div>
                        <p class="sub">Across ${dash.activeSubjects} active ${dash.activeSubjects == 1 ? 'subject' : 'subjects'}.</p>
                    </div>

                    <%-- Classes by Subject --%>
                    <div class="panel">
                        <div class="panel-head">
                            <h2>Classes by Subject</h2>
                        </div>
                        <div class="subject-scroll">
                            <c:forEach var="row" items="${dash.classesPerSubject}">
                                <div class="subject-row">
                                    <span style="font-size:.9rem;">${row[1]}</span>
                                    <span class="badge" style="flex-shrink:0;margin-left:8px;">${row[2]} ${row[2] == 1 ? 'class' : 'classes'}</span>
                                </div>
                            </c:forEach>
                            <c:if test="${empty dash.classesPerSubject}">
                                <p class="sub">No subjects found.</p>
                            </c:if>
                        </div>
                    </div>
                </div>

            </main>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
        <script src="${pageContext.request.contextPath}/assets/js/chart.umd.min.js"></script>
        <script>
            window.roleData = {
                admin:   ${dash.adminCount},
                expert:  ${dash.expertCount},
                teacher: ${dash.teacherCount},
                student: ${dash.studentCount}
            };
        </script>
        <script src="${pageContext.request.contextPath}/assets/js/admindashboard.js" defer></script>
    </body>
</html>

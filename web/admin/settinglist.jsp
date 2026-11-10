<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css" />
        <style>
            .setting-table { width: 100%; border-collapse: collapse; font-size: .9rem; table-layout: fixed; }
            .setting-table th { text-align: left; color: var(--dim); font-size: .78rem; font-weight: 700; text-transform: uppercase; letter-spacing: .04em; padding: 8px 12px; border-bottom: 1px solid var(--edge); white-space: nowrap; }
            .setting-table td { padding: 12px; border-bottom: 1px solid var(--edge); vertical-align: middle; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
            .setting-table tr:last-child td { border-bottom: 0; }
            .setting-table td:last-child { overflow: visible; white-space: nowrap; }
            .sort-link { display: inline-flex; align-items: center; gap: 5px; color: inherit; text-decoration: none; cursor: pointer; border-radius: 4px; padding: 2px 4px; margin: -2px -4px; transition: background .15s, color .15s; user-select: none; }
            .sort-link:hover { color: var(--accent, #4f8ef7); text-decoration: underline; text-underline-offset: 3px; }
            .sort-icon { font-size: .75rem; opacity: .35; transition: opacity .15s; flex-shrink: 0; }
            .sort-link.active { color: var(--accent, #4f8ef7); }
            .sort-link.active .sort-icon { opacity: 1; color: var(--accent, #4f8ef7); }
        </style>
        <title>System Settings – AMS</title>
    </head>
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <main class="page">

            <a class="back-link" href="${pageContext.request.contextPath}/admindashboard">
                &#8592; Admin Dashboard
            </a>

            <div class="hello">
                <h1>Setting List</h1>
                <p class="sub">View and manage system-wide configuration and master data.</p>
            </div>

            <%-- Flash success message --%>
            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert alert-success">${sessionScope.flashSuccess}</div>
                <c:remove var="flashSuccess" scope="session" />
            </c:if>

            <%-- Flash error message --%>
            <c:if test="${not empty sessionScope.flashError}">
                <div class="alert">${sessionScope.flashError}</div>
                <c:remove var="flashError" scope="session" />
            </c:if>

            <%-- Error message --%>
            <c:if test="${not empty error}">
                <div class="alert">${error}</div>
            </c:if>

            <%-- Toolbar: filter + search + New Setting button --%>
            <form method="get" action="${pageContext.request.contextPath}/admin/settings">
                <div class="toolbar">
                    <%-- Filter by Type --%>
                    <select name="type" title="Setting Type">
                        <option value="">All Types</option>
                        <c:forEach var="t" items="${settingTypes}">
                            <option value="${t}" ${param.type == t ? 'selected' : ''}>${t}</option>
                        </c:forEach>
                    </select>

                    <%-- Filter by Status --%>
                    <select name="status" title="Setting Status">
                        <option value="">All Statuses</option>
                        <option value="Active"   ${param.status == 'Active'   ? 'selected' : ''}>Active</option>
                        <option value="Inactive" ${param.status == 'Inactive' ? 'selected' : ''}>Inactive</option>
                    </select>

                    <%-- Search by name or value --%>
                    <input type="text" name="keyword" placeholder="Search by name or value..."
                           value="${param.keyword}" />

                    <button type="submit" class="action-btn">Search</button>

                    <%-- New Setting button --%>
                    <a class="btn-new" href="${pageContext.request.contextPath}/admin/settings?action=new">
                        + New Setting
                    </a>
                </div>
            </form>

            <%-- Setting list table --%>
            <div class="panel">
                <div class="table-wrap">
                    <table class="setting-table">
                        <colgroup>
                            <col style="width:6%">  <%-- ID --%>
                            <col style="width:18%"> <%-- Name --%>
                            <col style="width:11%"> <%-- Type --%>
                            <col style="width:22%"> <%-- Value --%>
                            <col style="width:8%">  <%-- Priority --%>
                            <col style="width:10%"> <%-- Status --%>
                            <col style="width:25%"> <%-- Actions --%>
                        </colgroup>
                        <thead>
                            <tr>
                                <th>
                                    <a href="?sortBy=settingId&sortDir=${sortBy == 'settingId' && sortDir == 'asc' ? 'desc' : 'asc'}&type=${param.type}&status=${param.status}&keyword=${param.keyword}"
                                       class="sort-link ${sortBy == 'settingId' ? 'active' : ''}" title="Sort by ID">
                                        ID <span class="sort-icon">${sortBy == 'settingId' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}</span>
                                    </a>
                                </th>
                                <th>
                                    <a href="?sortBy=name&sortDir=${sortBy == 'name' && sortDir == 'asc' ? 'desc' : 'asc'}&type=${param.type}&status=${param.status}&keyword=${param.keyword}"
                                       class="sort-link ${sortBy == 'name' ? 'active' : ''}" title="Sort by Name">
                                        Name <span class="sort-icon">${sortBy == 'name' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}</span>
                                    </a>
                                </th>
                                <th>
                                    <a href="?sortBy=type&sortDir=${sortBy == 'type' && sortDir == 'asc' ? 'desc' : 'asc'}&type=${param.type}&status=${param.status}&keyword=${param.keyword}"
                                       class="sort-link ${sortBy == 'type' ? 'active' : ''}" title="Sort by Type">
                                        Type <span class="sort-icon">${sortBy == 'type' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}</span>
                                    </a>
                                </th>
                                <th>Value</th>
                                <th>
                                    <a href="?sortBy=priority&sortDir=${sortBy == 'priority' && sortDir == 'asc' ? 'desc' : 'asc'}&type=${param.type}&status=${param.status}&keyword=${param.keyword}"
                                       class="sort-link ${sortBy == 'priority' ? 'active' : ''}" title="Sort by Priority">
                                        Priority <span class="sort-icon">${sortBy == 'priority' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}</span>
                                    </a>
                                </th>
                                <th>
                                    <a href="?sortBy=status&sortDir=${sortBy == 'status' && sortDir == 'asc' ? 'desc' : 'asc'}&type=${param.type}&status=${param.status}&keyword=${param.keyword}"
                                       class="sort-link ${sortBy == 'status' ? 'active' : ''}" title="Sort by Status">
                                        Status <span class="sort-icon">${sortBy == 'status' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}</span>
                                    </a>
                                </th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty settings}">
                                    <tr>
                                        <td colspan="7" style="text-align:center;padding:24px;color:var(--dim);">
                                            No search results found.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="s" items="${settings}">
                                        <tr>
                                            <td>${s.id}</td>
                                            <td>${s.name}</td>
                                            <td>${s.type}</td>
                                            <td>${s.value}</td>
                                            <td>${s.priority}</td>
                                            <td>
                                                <span class="status-badge ${s.status == 'Active' ? 'status-active' : 'status-inactive'}">
                                                    ${s.status}
                                                </span>
                                            </td>
                                            <td>
                                                <a class="action-btn"
                                                   href="${pageContext.request.contextPath}/admin/settings?action=edit&id=${s.id}">
                                                    Edit
                                                </a>
                                                <c:choose>
                                                    <c:when test="${s.status == 'Active'}">
                                                        <a class="action-btn deactivate"
                                                           href="${pageContext.request.contextPath}/admin/settings?action=deactivate&id=${s.id}"
                                                           onclick="return confirm('Deactivate setting \'${s.name}\'?')">
                                                            Deactivate
                                                        </a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <a class="action-btn activate"
                                                           href="${pageContext.request.contextPath}/admin/settings?action=activate&id=${s.id}"
                                                           onclick="return confirm('Activate setting \'${s.name}\'?')">
                                                            Activate
                                                        </a>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <%-- Pagination --%>
            <c:if test="${totalPages > 1}">
                <div class="pagination">
                    <c:choose>
                        <c:when test="${currentPage > 1}">
                            <a class="page-btn" href="?page=${currentPage - 1}&type=${param.type}&status=${param.status}&keyword=${param.keyword}&sortBy=${sortBy}&sortDir=${sortDir}">&#8592; Prev</a>
                        </c:when>
                        <c:otherwise>
                            <span class="page-btn disabled">&#8592; Prev</span>
                        </c:otherwise>
                    </c:choose>

                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <c:choose>
                            <c:when test="${i == currentPage}">
                                <span class="page-btn active">${i}</span>
                            </c:when>
                            <c:otherwise>
                                <a class="page-btn" href="?page=${i}&type=${param.type}&status=${param.status}&keyword=${param.keyword}&sortBy=${sortBy}&sortDir=${sortDir}">${i}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>

                    <c:choose>
                        <c:when test="${currentPage < totalPages}">
                            <a class="page-btn" href="?page=${currentPage + 1}&type=${param.type}&status=${param.status}&keyword=${param.keyword}&sortBy=${sortBy}&sortDir=${sortDir}">Next &#8594;</a>
                        </c:when>
                        <c:otherwise>
                            <span class="page-btn disabled">Next &#8594;</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:if>

        </main>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

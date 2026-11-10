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
            .user-table { width: 100%; border-collapse: collapse; font-size: .9rem; table-layout: fixed; }
            .user-table th { text-align: left; color: var(--dim); font-size: .78rem; font-weight: 700; text-transform: uppercase; letter-spacing: .04em; padding: 8px 12px; border-bottom: 1px solid var(--edge); white-space: nowrap; }
            .user-table td { padding: 12px; border-bottom: 1px solid var(--edge); vertical-align: middle; max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
            .user-table td.nowrap, .user-table td:last-child { max-width: none; white-space: nowrap; }
            .user-table tr:last-child td { border-bottom: 0; }
            .sort-link { display: inline-flex; align-items: center; gap: 5px; color: inherit; text-decoration: none; cursor: pointer; border-radius: 4px; padding: 2px 4px; margin: -2px -4px; transition: background .15s, color .15s; user-select: none; }
            .sort-link:hover { color: var(--accent, #4f8ef7); text-decoration: underline; text-underline-offset: 3px; }
            .sort-icon { font-size: .75rem; opacity: .35; transition: opacity .15s; flex-shrink: 0; }
            .sort-link.active { color: var(--accent, #4f8ef7); }
            .sort-link.active .sort-icon { opacity: 1; color: var(--accent, #4f8ef7); }
        </style>
        <title>User Management – AMS</title>
    </head>
    
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <main class="page">

            <%-- Back to dashboard --%>
            <a class="back-link" href="${pageContext.request.contextPath}/admindashboard">
                &#8592; Admin Dashboard
            </a>

            <div class="hello">
                <h1>User List</h1>
                <p class="sub">View and manage all user accounts in the system.</p>
            </div>

            <%-- Flash success message --%>
            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert alert-success">${sessionScope.flashSuccess}</div>
                <c:remove var="flashSuccess" scope="session" />
            </c:if>

            <%-- Error message --%>
            <c:if test="${not empty error}">
                <div class="alert">${error}</div>
            </c:if>

            <%-- Toolbar: filter + search + New User button --%>
            <form method="get" action="${pageContext.request.contextPath}/admin/users">
                <div class="toolbar">
                    <%-- Filter by Role — load từ DB --%>
                    <select name="roleId" title="Role">
                        <option value="">All Roles</option>
                        <c:forEach var="r" items="${roles}">
                            <option value="${r.id}" ${param.roleId == r.id ? 'selected' : ''}>${r.name}</option>
                        </c:forEach>
                    </select>

                    <%-- Filter by Status --%>
                    <select name="status" title="Status">
                        <option value="">All Statuses</option>
                        <option value="Active"   ${param.status == 'Active'   ? 'selected' : ''}>Active</option>
                        <option value="Inactive" ${param.status == 'Inactive' ? 'selected' : ''}>Inactive</option>
                    </select>

                    <%-- Search by name / username / email --%>
                    <input type="text" name="keyword" placeholder="Search by name, username or email..."
                           value="${param.keyword}" />

                    <button type="submit" class="action-btn">Search</button>

                    <%-- New User button --%>
                    <a class="btn-new" href="${pageContext.request.contextPath}/admin/users?action=new">
                        + New User
                    </a>
                </div>
            </form>

            <%-- User list table --%>
            <div class="panel">
                <div class="table-wrap">
                    <table class="user-table">
                        <colgroup>
                            <col style="width:16%">  <%-- Full Name --%>
                            <col style="width:16%">  <%-- Username --%>
                            <col style="width:20%">  <%-- Email --%>
                            <col style="width:8%">   <%-- Role --%>
                            <col style="width:9%">   <%-- Status --%>
                            <col style="width:11%">  <%-- Created At --%>
                            <col style="width:20%">  <%-- Actions --%>
                        </colgroup>
                        <thead>
                            <tr>
                                <th>
                                    <a href="?sortBy=fullName&sortDir=${sortBy == 'fullName' && sortDir == 'asc' ? 'desc' : 'asc'}&roleId=${param.roleId}&status=${param.status}&keyword=${param.keyword}"
                                       class="sort-link ${sortBy == 'fullName' ? 'active' : ''}"
                                       title="Click to sort by Full Name">
                                        Full Name
                                        <span class="sort-icon">
                                            ${sortBy == 'fullName' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}
                                        </span>
                                    </a>
                                </th>
                                <th>
                                    <a href="?sortBy=username&sortDir=${sortBy == 'username' && sortDir == 'asc' ? 'desc' : 'asc'}&roleId=${param.roleId}&status=${param.status}&keyword=${param.keyword}"
                                       class="sort-link ${sortBy == 'username' ? 'active' : ''}"
                                       title="Click to sort by Username">
                                        Username
                                        <span class="sort-icon">
                                            ${sortBy == 'username' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}
                                        </span>
                                    </a>
                                </th>
                                <th>
                                    <a href="?sortBy=email&sortDir=${sortBy == 'email' && sortDir == 'asc' ? 'desc' : 'asc'}&roleId=${param.roleId}&status=${param.status}&keyword=${param.keyword}"
                                       class="sort-link ${sortBy == 'email' ? 'active' : ''}"
                                       title="Click to sort by Email">
                                        Email
                                        <span class="sort-icon">
                                            ${sortBy == 'email' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}
                                        </span>
                                    </a>
                                </th>
                                <th>Role</th>
                                <th>
                                    <a href="?sortBy=status&sortDir=${sortBy == 'status' && sortDir == 'asc' ? 'desc' : 'asc'}&roleId=${param.roleId}&status=${param.status}&keyword=${param.keyword}"
                                       class="sort-link ${sortBy == 'status' ? 'active' : ''}"
                                       title="Click to sort by Status">
                                        Status
                                        <span class="sort-icon">
                                            ${sortBy == 'status' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}
                                        </span>
                                    </a>
                                </th>
                                <th>
                                    <a href="?sortBy=createdAt&sortDir=${sortBy == 'createdAt' && sortDir == 'asc' ? 'desc' : 'asc'}&roleId=${param.roleId}&status=${param.status}&keyword=${param.keyword}"
                                       class="sort-link ${sortBy == 'createdAt' ? 'active' : ''}"
                                       title="Click to sort by Created At">
                                        Created At
                                        <span class="sort-icon">
                                            ${sortBy == 'createdAt' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}
                                        </span>
                                    </a>
                                </th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty users}">
                                    <tr>
                                        <td colspan="7" class="empty">No search results found.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="u" items="${users}">
                                        <tr>
                                            <td>${u.fullName}</td>
                                            <td>${u.username}</td>
                                            <td>${u.email}</td>
                                            <td>
                                                <c:set var="roleName" value="Unknown"/>
                                                <c:forEach var="r" items="${roles}">
                                                    <c:if test="${r.id == u.roleId}">
                                                        <c:set var="roleName" value="${r.name}"/>
                                                    </c:if>
                                                </c:forEach>
                                                ${roleName}
                                            </td>
                                            <td>
                                                <span class="status-badge ${u.status == 'Active' ? 'status-active' : 'status-inactive'}">
                                                    ${u.status}
                                                </span>
                                            </td>
                                            <td class="nowrap">${u.createdAt}</td>
                                            <td>
                                                <%-- Edit button -> User Detail --%>
                                                <a class="action-btn"
                                                   href="${pageContext.request.contextPath}/admin/users?action=edit&id=${u.id}">
                                                    Edit
                                                </a>
                                                <%-- Activate / Deactivate button --%>
                                                <c:choose>
                                                    <c:when test="${u.status == 'Active'}">
                                                        <a class="action-btn deactivate"
                                                           href="${pageContext.request.contextPath}/admin/users?action=deactivate&id=${u.id}"
                                                           onclick="return confirm('Deactivate user \'${u.username}\'?')">
                                                            Deactivate
                                                        </a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <a class="action-btn activate"
                                                           href="${pageContext.request.contextPath}/admin/users?action=activate&id=${u.id}"
                                                           onclick="return confirm('Activate user \'${u.username}\'?')">
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
                    <%-- Nút Previous --%>
                    <c:choose>
                        <c:when test="${currentPage > 1}">
                            <a class="page-btn" href="?page=${currentPage - 1}&roleId=${param.roleId}&status=${param.status}&keyword=${param.keyword}&sortBy=${sortBy}&sortDir=${sortDir}">&#8592; Prev</a>
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
                                <a class="page-btn" href="?page=${i}&roleId=${param.roleId}&status=${param.status}&keyword=${param.keyword}&sortBy=${sortBy}&sortDir=${sortDir}">${i}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>

                    <c:choose>
                        <c:when test="${currentPage < totalPages}">
                            <a class="page-btn" href="?page=${currentPage + 1}&roleId=${param.roleId}&status=${param.status}&keyword=${param.keyword}&sortBy=${sortBy}&sortDir=${sortDir}">Next &#8594;</a>
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

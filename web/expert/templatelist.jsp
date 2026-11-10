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
            .template-table { width:100%; border-collapse:collapse; font-size:.9rem; table-layout:fixed; }
            .template-table th { text-align:left; color:var(--dim); font-size:.78rem; font-weight:700; text-transform:uppercase; letter-spacing:.04em; padding:8px 12px; border-bottom:1px solid var(--edge); white-space:nowrap; }
            .template-table td { padding:12px; border-bottom:1px solid var(--edge); vertical-align:middle; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
            .template-table tr:last-child td { border-bottom:0; }
            .template-table td:last-child { overflow:visible; white-space:nowrap; }
            .template-table td.wrap { white-space:normal; }
            .template-title { font-weight:600; color:var(--accent,#4f8ef7); text-decoration:none; }
            .template-title:hover { text-decoration:underline; }
            .template-preview { font-size:.82rem; color:var(--dim); margin-top:3px; }
            .subject-tag { display:inline-block; padding:3px 10px; border-radius:20px; background:var(--card); border:1px solid var(--edge); font-size:.8rem; white-space:nowrap; }
            .sort-link { display:inline-flex; align-items:center; gap:5px; color:inherit; text-decoration:none; cursor:pointer; border-radius:4px; padding:2px 4px; margin:-2px -4px; transition:background .15s,color .15s; user-select:none; }
            .sort-link:hover { color:var(--accent,#4f8ef7); text-decoration:underline; text-underline-offset:3px; }
            .sort-icon { font-size:.75rem; opacity:.35; transition:opacity .15s; flex-shrink:0; }
            .sort-link.active { color:var(--accent,#4f8ef7); }
            .sort-link.active .sort-icon { opacity:1; color:var(--accent,#4f8ef7); }
        </style>
        <title>Assignment Templates – AMS</title>
    </head>
    <body class="app">

        <jsp:include page="/common/header.jsp" />

        <div class="app-layout">
            <main class="main-content">
                
                <a class="back-link" href="${pageContext.request.contextPath}/expertdashboard">
                    &#8592; Expert Dashboard
                </a>

                <div class="hello">
                    <h1>Assignment Templates</h1>
                    <p class="sub">View and manage assignment templates for your subjects.</p>
                </div>

                <%-- Flash messages --%>
                <c:if test="${not empty sessionScope.flashSuccess}">
                    <div class="alert alert-success">${sessionScope.flashSuccess}</div>
                    <c:remove var="flashSuccess" scope="session" />
                </c:if>
                <c:if test="${not empty sessionScope.flashError}">
                    <div class="alert">${sessionScope.flashError}</div>
                    <c:remove var="flashError" scope="session" />
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert">${error}</div>
                </c:if>

                <%-- Toolbar --%>
                <form method="get" action="${pageContext.request.contextPath}/expert/templates">
                    <div class="toolbar">
                        <%-- Filter by Subject --%>
                        <select name="subjectId" title="Subject">
                            <option value="">All Subjects</option>
                            <c:forEach var="s" items="${subjects}">
                                <option value="${s.id}" ${param.subjectId == s.id ? 'selected' : ''}>${s.name}</option>
                            </c:forEach>
                        </select>

                        <%-- Search by title --%>
                        <input type="text" name="keyword" placeholder="Search by title..."
                               value="${param.keyword}" />

                        <button type="submit" class="action-btn">Search</button>

                        <a class="btn-new" href="${pageContext.request.contextPath}/expert/templates?action=new">
                            + New Template
                        </a>
                    </div>
                </form>

                <%-- Template list table --%>
                <div class="panel">
                    <div class="table-wrap">
                        <table class="template-table">
                            <colgroup>
                                <col style="width:6%">   <%-- ID --%>
                                <col style="width:52%">  <%-- Title --%>
                                <col style="width:26%">  <%-- Subject --%>
                                <col style="width:16%">  <%-- Actions --%>
                            </colgroup>
                            <thead>
                                <tr>
                                    <th>
                                        <a href="?sortBy=template_id&sortDir=${sortBy == 'template_id' && sortDir == 'asc' ? 'desc' : 'asc'}&subjectId=${param.subjectId}&keyword=${param.keyword}"
                                           class="sort-link ${sortBy == 'template_id' ? 'active' : ''}" title="Sort by ID">
                                            ID <span class="sort-icon">${sortBy == 'template_id' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}</span>
                                        </a>
                                    </th>
                                    <th>
                                        <a href="?sortBy=title&sortDir=${sortBy == 'title' && sortDir == 'asc' ? 'desc' : 'asc'}&subjectId=${param.subjectId}&keyword=${param.keyword}"
                                           class="sort-link ${sortBy == 'title' ? 'active' : ''}" title="Sort by Title">
                                            Title <span class="sort-icon">${sortBy == 'title' ? (sortDir == 'asc' ? '▲' : '▼') : '⇅'}</span>
                                        </a>
                                    </th>
                                    <th>Subject</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty templates}">
                                        <tr>
                                            <td colspan="4" class="empty">No search results found.</td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="t" items="${templates}">
                                            <tr>
                                                <td>${t.id}</td>
                                                <td>${t.title}</td>
                                                <td>
                                                    <%-- Hiển thị subject name từ list subjects --%>
                                                    <c:forEach var="s" items="${subjects}">
                                                        <c:if test="${s.id == t.subjectId}">${s.name}</c:if>
                                                    </c:forEach>
                                                </td>
                                                <td>
                                                    <a class="action-btn"
                                                       href="${pageContext.request.contextPath}/expert/templates?action=edit&id=${t.id}">
                                                        Edit
                                                    </a>
                                                    <a class="action-btn deactivate"
                                                       href="${pageContext.request.contextPath}/expert/templates?action=delete&id=${t.id}"
                                                       onclick="return confirm('Delete template \'${t.title}\'? This cannot be undone.')">
                                                        Delete
                                                    </a>
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
                                <a class="page-btn" href="?page=${currentPage - 1}&subjectId=${param.subjectId}&keyword=${param.keyword}&sortBy=${sortBy}&sortDir=${sortDir}">&#8592; Prev</a>
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
                                    <a class="page-btn" href="?page=${i}&subjectId=${param.subjectId}&keyword=${param.keyword}&sortBy=${sortBy}&sortDir=${sortDir}">${i}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <c:choose>
                            <c:when test="${currentPage < totalPages}">
                                <a class="page-btn" href="?page=${currentPage + 1}&subjectId=${param.subjectId}&keyword=${param.keyword}&sortBy=${sortBy}&sortDir=${sortDir}">Next &#8594;</a>
                            </c:when>
                            <c:otherwise>
                                <span class="page-btn disabled">Next &#8594;</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

            </main>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

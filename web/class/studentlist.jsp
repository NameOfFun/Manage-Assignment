
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/students"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="listUrl" value="${ctx}/students?${filterQuery}${empty filterQuery ? '' : '&'}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Student List · AMS</title>
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css?v=20261003">
        <link rel="stylesheet" href="${ctx}/assets/css/manage.css?v=20261016">
    </head>
    <body class="app">
        <header class="topbar">
            <div class="topbar-in">
                <a class="brand" href="${ctx}/dashboard/${fn:toLowerCase(meRole)}dashboard.jsp" title="Back to your dashboard" style="color:inherit;text-decoration:none">
                    <div class="mark" aria-hidden="true">A</div>
                    <span>AMS</span>
                </a>
                <div class="topbar-right">
                    <button class="tog" id="tog" type="button" aria-label="Switch colour theme">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4.2"/><path d="M12 2v2.4M12 19.6V22M4.2 4.2l1.7 1.7M18.1 18.1l1.7 1.7M2 12h2.4M19.6 12H22M4.2 19.8l1.7-1.7M18.1 5.9l1.7-1.7"/></svg>
                    </button>
                    <c:set var="meName" value="${empty me.fullName ? me.username : me.fullName}"/>
                    <a class="me" href="${ctx}/profile" title="View your profile">
                        <span class="avatar" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(meName, 0, 1))}"/></span>
                        <span class="me-text">
                            <strong><c:out value="${meName}"/></strong>
                            <small><c:out value="${meRole}"/></small>
                        </span>
                    </a>
                </div>
            </div>
        </header>

        <main class="page">
            <div class="page-head">
                <div>
                    <a class="back" href="${ctx}/classes">&larr; Class List</a>
                    <h1>Student List</h1>
                    <p class="sub">Students in the classes you teach.</p>
                </div>
            </div>

            <c:if test="${not empty flash and not flashOk}">
                <div class="alert" role="alert"><c:out value="${flash}"/></div>
            </c:if>

            <form class="filters filters-2" method="get" action="${ctx}/students" role="search">
                <label class="sr" for="f-class">Class</label>
                <select class="ctl" id="f-class" name="classId" onchange="this.form.submit()">
                    <option value="">All my classes</option>
                    <c:forEach var="cl" items="${classes}">
                        <option value="${cl.id}" ${fClass == cl.id.toString() ? 'selected' : ''}>
                            <c:out value="${cl.classCode}"/> · <c:out value="${cl.semesterCode}"/>
                        </option>
                    </c:forEach>
                </select>
                <div class="kw">
                    <label class="sr" for="f-q">Search by name, username or email</label>
                    <input class="ctl" id="f-q" type="search" name="q" maxlength="50"
                           value="<c:out value='${fKeyword}'/>" placeholder="Enter name, username or email">
                </div>
                <div class="actions">
                    <button class="btn-sm" type="submit">Search</button>
                    <c:if test="${not empty filterQuery}">
                        <a class="btn-sm ghost" href="${ctx}/students">Clear</a>
                    </c:if>
                </div>
            </form>

            <section class="card" aria-label="Students">
                <c:choose>
                    <c:when test="${empty result.items}">
                        <p class="empty-state">
                            <c:choose>
                                <c:when test="${empty classes}">You are not teaching any class yet.</c:when>
                                <c:when test="${not empty filterQuery}">No student matches these filters.</c:when>
                                <c:otherwise>No students are enrolled in your classes yet.</c:otherwise>
                            </c:choose>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <div class="data-wrap">
                            <table class="data">
                                <thead>
                                    <tr>
                                        <th scope="col">Id</th>
                                        <th scope="col">Full Name</th>
                                        <th scope="col">Username</th>
                                        <th scope="col">Email</th>
                                        <th scope="col">Classes</th>
                                        <th scope="col">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="st" items="${result.items}">
                                        <tr>
                                            <td class="id">${st.userId}</td>
                                            <td class="strong"><c:out value="${st.displayName}"/></td>
                                            <td class="muted"><c:out value="${st.username}"/></td>
                                            <td class="${empty st.email ? 'muted' : ''}"><c:out value="${empty st.email ? '—' : st.email}"/></td>
                                            <td>
                                                <div class="chips">
                                                    <c:forEach var="code" items="${st.classCodes}">
                                                        <span class="term-chip"><c:out value="${code}"/></span>
                                                    </c:forEach>
                                                </div>
                                            </td>
                                            <td><a href="${ctx}/students/detail?id=${st.userId}">View</a></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:if test="${result.totalItems > 0}">
                    <div class="list-foot">
                        <span>Showing ${result.from}–${result.to} of ${result.totalItems} student${result.totalItems == 1 ? '' : 's'}</span>
                        <c:if test="${result.totalPages > 1}">
                            <nav aria-label="Pages">
                                <ul class="pager">
                                    <li>
                                        <c:choose>
                                            <c:when test="${result.hasPrev}"><a href="${listUrl}page=${result.page - 1}" aria-label="Previous page">&lsaquo;</a></c:when>
                                            <c:otherwise><span class="off" aria-hidden="true">&lsaquo;</span></c:otherwise>
                                        </c:choose>
                                    </li>
                                    <c:forEach var="p" items="${result.pageNumbers}">
                                        <li>
                                            <c:choose>
                                                <c:when test="${p == 0}"><span class="gap">…</span></c:when>
                                                <c:when test="${p == result.page}"><span class="cur" aria-current="page">${p}</span></c:when>
                                                <c:otherwise><a href="${listUrl}page=${p}">${p}</a></c:otherwise>
                                            </c:choose>
                                        </li>
                                    </c:forEach>
                                    <li>
                                        <c:choose>
                                            <c:when test="${result.hasNext}"><a href="${listUrl}page=${result.page + 1}" aria-label="Next page">&rsaquo;</a></c:when>
                                            <c:otherwise><span class="off" aria-hidden="true">&rsaquo;</span></c:otherwise>
                                        </c:choose>
                                    </li>
                                </ul>
                            </nav>
                        </c:if>
                    </div>
                </c:if>
            </section>
        </main>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

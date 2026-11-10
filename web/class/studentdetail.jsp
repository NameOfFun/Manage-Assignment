
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/students"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="stName" value="${empty student.fullName ? student.username : student.fullName}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Student Details · <c:out value="${stName}"/> · AMS</title>
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
                    <a class="back" href="${ctx}/students">&larr; Student List</a>
                    <h1>Student Details</h1>
                </div>
            </div>

            <div class="stack">
                <section class="card card-pad" aria-labelledby="h-info">
                    <div class="profile-head">
                        <span class="avatar big" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(stName, 0, 1))}"/></span>
                        <div>
                            <h2 id="h-info"><c:out value="${stName}"/></h2>
                            <span class="status-badge ${student.status == 'Active' ? 'status-active' : 'status-inactive'}"><c:out value="${student.status}"/></span>
                        </div>
                    </div>
                    <dl class="kv">
                        <div><dt>Id</dt><dd>${student.id}</dd></div>
                        <div><dt>Username</dt><dd><c:out value="${student.username}"/></dd></div>
                        <div><dt>Email</dt><dd class="${empty student.email ? 'muted' : ''}"><c:out value="${empty student.email ? '—' : student.email}"/></dd></div>
                        <div><dt>Classes with you</dt><dd>${fn:length(enrolments)}</dd></div>
                    </dl>
                </section>

                <section class="card card-pad roster-card" aria-labelledby="h-classes">
                    <div class="section-head">
                        <h2 id="h-classes">Classes and groups</h2>
                        <span class="note">Only the classes you teach</span>
                    </div>
                    <c:choose>
                        <c:when test="${empty enrolments}">
                            <p class="empty-state">This student is not in any of your classes.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="data-wrap">
                                <table class="data narrow">
                                    <thead>
                                        <tr>
                                            <th scope="col">Class</th>
                                            <th scope="col">Subject</th>
                                            <th scope="col">Semester</th>
                                            <th scope="col">Group</th>
                                            <th scope="col">Enrolled</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="e" items="${enrolments}">
                                            <tr>
                                                <td class="strong"><a href="${ctx}/classes/detail?id=${e.classId}"><c:out value="${e.classCode}"/></a></td>
                                                <td><c:out value="${empty e.subjectLabel ? '—' : e.subjectLabel}"/></td>
                                                <td><span class="term-chip"><c:out value="${e.semesterCode}"/></span></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${e.groupId > 0}"><a href="${ctx}/groups/detail?id=${e.groupId}">Group ${e.groupId}</a></c:when>
                                                        <c:otherwise><a class="warn-txt" href="${ctx}/groups?classId=${e.classId}">No group</a></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="muted"><c:out value="${e.enrollDateText}"/></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </main>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

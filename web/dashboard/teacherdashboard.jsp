<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="meName" value="${empty sessionScope.user.fullName ? sessionScope.user.username : sessionScope.user.fullName}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css">
        <title>Teacher Dashboard · AMS</title>
    </head>
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <div class="app-layout">
            <jsp:include page="/common/teachersidebar.jsp">
                <jsp:param name="active" value="dashboard"/>
            </jsp:include>

            <main class="main-content">
                <section class="hello page-title-row">
                    <div>
                        <p class="eyebrow">TEACHING OVERVIEW<c:if test="${not empty currentTerm}"> · <c:out value="${currentTerm}"/></c:if></p>
                        <h1>Hello, <c:out value="${meName}"/></h1>
                        <p class="sub">Your classes, assignments and submissions to evaluate.</p>
                    </div>
                    <a class="button button-primary" href="${ctx}/assignment?action=new">Create assignment</a>
                </section>

                <c:if test="${not empty error}">
                    <div class="alert" role="alert"><c:out value="${error}"/></div>
                </c:if>

                <%-- Stats --%>
                <section class="stats teacher-stats" aria-label="Teacher summary">
                    <div class="stat">
                        <span class="stat-label">Classes this semester</span>
                        <span class="stat-value"><c:out value="${classCount}"/></span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">Assignments created</span>
                        <span class="stat-value"><c:out value="${assignmentCount}"/></span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">Submissions awaiting evaluation</span>
                        <span class="stat-value${pendingReviewCount > 0 ? ' is-warn' : ''}"><c:out value="${pendingReviewCount}"/></span>
                    </div>
                </section>

                <div class="cols teacher-columns">
                    <%-- Classes in the current term --%>
                    <section class="panel" aria-labelledby="classes-heading">
                        <div class="panel-head">
                            <div>
                                <h2 id="classes-heading">Classes</h2>
                                <p class="panel-note"><c:out value="${empty currentTerm ? 'No assigned classes' : currentTerm}"/></p>
                            </div>
                            <a class="text-link" href="${ctx}/classes">All classes</a>
                        </div>
                        <c:choose>
                            <c:when test="${empty teacherClasses}">
                                <p class="empty">No classes in the current semester.</p>
                            </c:when>
                            <c:otherwise>
                                <ul class="class-list">
                                    <c:forEach var="cl" items="${teacherClasses}">
                                        <li class="class-item">
                                            <div class="class-top">
                                                <span class="code"><c:out value="${cl.classCode}"/></span>
                                                <span class="term"><c:out value="${cl.semester}"/> <c:out value="${cl.year}"/></span>
                                            </div>
                                            <div class="class-subject"><c:out value="${empty cl.subjectName ? 'No subject' : cl.subjectName}"/></div>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <%-- 5 most recent assignments --%>
                    <section class="panel" aria-labelledby="assignments-heading">
                        <div class="panel-head">
                            <div>
                                <h2 id="assignments-heading">Recent assignments</h2>
                                <p class="panel-note">The 5 newest assignments of your classes.</p>
                            </div>
                            <a class="text-link" href="${ctx}/assignment">All assignments</a>
                        </div>
                        <c:choose>
                            <c:when test="${empty recentAssignments}">
                                <p class="empty">No assignments yet. <a href="${ctx}/assignment?action=new">Create your first assignment</a>.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="table-wrap">
                                    <table class="dashboard-table">
                                        <thead>
                                            <tr>
                                                <th scope="col">Assignment</th>
                                                <th scope="col">Class</th>
                                                <th scope="col">Due date</th>
                                                <th scope="col">Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="a" items="${recentAssignments}">
                                                <tr>
                                                    <td>
                                                        <a href="${ctx}/assignment?id=${a.assignmentId}"><strong><c:out value="${a.title}"/></strong></a>
                                                        <span class="table-sub"><c:out value="${a.subjectName}"/></span>
                                                    </td>
                                                    <td class="nowrap"><c:out value="${a.classCode}"/></td>
                                                    <td class="nowrap"><c:out value="${a.dueDateText}"/></td>
                                                    <td><span class="status-pill status-${fn:toLowerCase(a.status)}"><c:out value="${a.status}"/></span></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </div>

                <%-- Assignments with ungraded submissions --%>
                <section class="panel" aria-labelledby="reviews-heading">
                    <div class="panel-head">
                        <div>
                            <h2 id="reviews-heading">Submissions to evaluate</h2>
                            <p class="panel-note">Assignments with ungraded submissions.</p>
                        </div>
                    </div>
                    <c:choose>
                        <c:when test="${empty reviewAssignments}">
                            <p class="empty">No submissions are waiting for evaluation.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="dashboard-table">
                                    <thead>
                                        <tr>
                                            <th scope="col">Assignment</th>
                                            <th scope="col">Class</th>
                                            <th scope="col">Due date</th>
                                            <th scope="col" class="num">Pending</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="a" items="${reviewAssignments}">
                                            <tr>
                                                <td>
                                                    <a href="${ctx}/assignment?id=${a.assignmentId}"><strong><c:out value="${a.title}"/></strong></a>
                                                    <span class="table-sub"><c:out value="${a.subjectName}"/></span>
                                                </td>
                                                <td class="nowrap"><c:out value="${a.classCode}"/></td>
                                                <td class="nowrap"><c:out value="${a.dueDateText}"/></td>
                                                <td class="num"><span class="badge bad"><c:out value="${a.pendingSubmissionCount}"/></span></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </main>
        </div>

        <script src="${ctx}/assets/js/theme.js" defer></script>
    </body>
</html>

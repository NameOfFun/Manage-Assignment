
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Student Dashboard · AMS</title>
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css">
    </head>
    <body class="app">
        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />
        

        <main class="page">
            <c:if test="${not empty error}">
                <div class="alert" role="alert"><c:out value="${error}"/></div>
            </c:if>

            <c:if test="${not empty dash}">
                <section class="hello">
                    <h1>Welcome back, <c:out value="${dash.greetingName}"/></h1>
                    <p class="sub">Here is what is happening in your classes.</p>
                </section>

                <section class="stats" aria-label="Summary">
                    <div class="stat">
                        <span class="stat-label">My classes</span>
                        <span class="stat-value">${dash.classCount}</span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">Due, not submitted</span>
                        <span class="stat-value${dash.pendingCount > 0 ? ' is-warn' : ''}">${dash.pendingCount}</span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">Submitted</span>
                        <span class="stat-value">${dash.submittedCount}</span>
                    </div>
                </section>

                <div class="cols">
                    <section class="panel" aria-labelledby="h-classes">
                        <div class="panel-head">
                            <h2 id="h-classes">My classes</h2>
                        </div>
                        <c:choose>
                            <c:when test="${empty dash.classes}">
                                <p class="empty">You are not enrolled in any class yet.</p>
                            </c:when>
                            <c:otherwise>
                                <ul class="class-list">
                                    <c:forEach var="cl" items="${dash.classes}">
                                        <li class="class-item">
                                            <a class="class-link" href="${ctx}/myassignments?classId=${cl.id}">
                                            <div class="class-top">
                                                <span class="code"><c:out value="${cl.classCode}"/></span>
                                                <span class="term"><c:out value="${cl.semester}"/></span>
                                            </div>
                                            <div class="class-subject"><c:out value="${empty cl.subjectName ? '—' : cl.subjectName}"/></div>
                                            <div class="class-meta">
                                                <span>Teacher: <c:out value="${empty cl.teacherName ? '—' : cl.teacherName}"/></span>
                                                <span>${cl.assignmentCount} assignment${cl.assignmentCount == 1 ? '' : 's'}</span>
                                            </div>
                                            </a>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <section class="panel" aria-labelledby="h-upcoming">
                        <div class="panel-head">
                            <h2 id="h-upcoming">Upcoming deadlines</h2>
                        </div>
                        <c:choose>
                            <c:when test="${empty dash.upcoming}">
                                <p class="empty">No upcoming deadlines. Nice!</p>
                            </c:when>
                            <c:otherwise>
                                <ul class="due-list">
                                    <c:forEach var="a" items="${dash.upcoming}">
                                        <li class="due-item">
                                            <div class="due-main">
                                                <a class="due-title" href="${ctx}/myassignments?id=${a.assignmentId}"><c:out value="${a.title}"/></a>
                                                <span class="due-sub"><c:out value="${a.classCode}"/> · <c:out value="${a.dueDateText}"/></span>
                                            </div>

                                            <%-- Store current time in page scope --%>
                                            <c:set var="now" value="<%= java.time.LocalDateTime.now() %>" />

                                            <c:choose>
                                                <c:when test="${a.submitted}">
                                                    <span class="badge ok">Submitted</span>
                                                </c:when>
                                                <c:when test="${a.dueDate != null && a.dueDate.isBefore(now)}">
                                                    <span class="badge bad"><c:out value="${a.dueDateText}"/></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge"><c:out value="${a.dueDateText}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </div>

                <section class="panel" aria-labelledby="h-grades">
                    <div class="panel-head">
                        <h2 id="h-grades">Recent grades</h2>
                    </div>
                    <c:choose>
                        <c:when test="${empty dash.recentGrades}">
                            <p class="empty">No graded work yet.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="grades">
                                    <thead>
                                        <tr>
                                            <th scope="col">Assignment</th>
                                            <th scope="col">Class</th>
                                            <th scope="col" class="num">Score</th>
                                            <th scope="col">Feedback</th>
                                            <th scope="col">Graded on</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="g" items="${dash.recentGrades}">
                                            <tr>
                                                <td><c:out value="${g.assignmentTitle}"/></td>
                                                <td class="nowrap"><c:out value="${g.classCode}"/></td>
                                                <td class="num score"><c:out value="${g.scoreText}"/></td>
                                                <td class="feedback"><c:out value="${empty g.feedbackPreview ? '—' : g.feedbackPreview}"/></td>
                                                <td class="nowrap"><c:out value="${g.evaluatedText}"/></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:if>
        </main>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/myassignments"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Class Assignments · AMS</title>
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css">
    </head>
    <body class="app">
        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <main class="page">
            <a class="text-link" href="${ctx}/studentdashboard">← Back to dashboard</a>

            <c:if test="${not empty error}">
                <div class="alert" role="alert"><c:out value="${error}"/></div>
            </c:if>

            <c:if test="${not empty classInfo}">
            <section class="hello">
                <p class="eyebrow"><c:out value="${classInfo.semester}"/> <c:out value="${classInfo.year}"/></p>
                <h1><c:out value="${classInfo.classCode}"/></h1>
                <p class="sub"><c:out value="${classInfo.subjectName}"/> · Teacher: <c:out value="${empty classInfo.teacherName ? '—' : classInfo.teacherName}"/></p>
            </section>

            <section class="panel" aria-labelledby="list-heading">
                <div class="panel-head">
                    <h2 id="list-heading">Assignment list</h2>
                    <c:if test="${result.totalItems > 0}">
                        <span class="result-count">${result.from}–${result.to} / ${result.totalItems} assignments</span>
                    </c:if>
                </div>
                <c:choose>
                    <c:when test="${result.totalItems == 0}">
                        <p class="empty">This class has no assignments yet.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table class="assignment-table">
                                <thead>
                                    <tr>
                                        <th scope="col">Assignment</th>
                                        <th scope="col">Due date</th>
                                        <th scope="col">Status</th>
                                        <th scope="col" class="num">Score</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${result.items}">
                                        <tr>
                                            <td class="assignment-name">
                                                <a href="${ctx}/myassignments?id=${row.assignment.assignmentId}"><strong><c:out value="${row.assignment.title}"/></strong></a>
                                            </td>
                                            <td class="nowrap"><c:out value="${row.assignment.dueDateText}"/></td>
                                            <td><span class="badge ${row.statusClass}"><c:out value="${row.statusText}"/></span></td>
                                            <td class="num score"><c:out value="${row.graded ? row.submission.scoreText : '—'}"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <c:url var="pageBaseUrl" value="/myassignments" scope="request">
                            <c:param name="classId" value="${classInfo.id}"/>
                        </c:url>
                        <jsp:include page="/common/pagination.jsp"/>
                    </c:otherwise>
                </c:choose>
            </section>
            </c:if>
        </main>

        <script src="${ctx}/assets/js/theme.js" defer></script>
    </body>
</html>

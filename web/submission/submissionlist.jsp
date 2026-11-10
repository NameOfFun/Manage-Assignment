<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/submissions"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css">
        <title>Submission List · AMS</title>
    </head>
    <body class="app">

        <jsp:include page="/common/header.jsp" />

        <div class="app-layout">
            <jsp:include page="/common/teachersidebar.jsp">
                <jsp:param name="active" value="submissions"/>
            </jsp:include>

            <main class="main-content">
                <section class="hello">
                    <p class="eyebrow">SUBMISSIONS</p>
                    <h1>Submission List</h1>
                    <p class="sub">Group submissions in the classes you teach. Ungraded submissions are shown first.</p>
                </section>

                <c:if test="${not empty error}">
                    <div class="alert" role="alert"><c:out value="${error}"/></div>
                </c:if>

                <section class="filter-bar" aria-label="Filter submissions">
                    <form class="filter-form filter-form-compact" method="get" action="${ctx}/submissions">
                        <div class="filter-search">
                            <label for="keyword">Search</label>
                            <input class="control" id="keyword" name="keyword" type="search" maxlength="100"
                                   value="<c:out value='${filterKeyword}'/>" placeholder="Assignment, student name or username">
                        </div>
                        <div>
                            <label for="filterClass">Class</label>
                            <select class="control" id="filterClass" name="classId">
                                <option value="">All classes</option>
                                <c:forEach var="cl" items="${classes}">
                                    <option value="${cl.id}" ${filterClassId == cl.id ? 'selected' : ''}>
                                        <c:out value="${cl.classCode}"/> · <c:out value="${cl.semester}"/> <c:out value="${cl.year}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div>
                            <label for="filterStatus">Status</label>
                            <select class="control" id="filterStatus" name="status">
                                <option value="">All</option>
                                <option value="pending" ${filterStatus == 'pending' ? 'selected' : ''}>Not graded</option>
                                <option value="graded" ${filterStatus == 'graded' ? 'selected' : ''}>Graded</option>
                                <option value="late" ${filterStatus == 'late' ? 'selected' : ''}>Late</option>
                            </select>
                        </div>
                        <div class="filter-actions">
                            <button class="button button-primary" type="submit">Filter</button>
                            <a class="button button-quiet" href="${ctx}/submissions">Clear filters</a>
                        </div>
                    </form>
                </section>

                <section class="panel">
                    <div class="panel-head">
                        <div>
                            <p class="eyebrow">OVERVIEW</p>
                            <h2>Submissions</h2>
                        </div>
                        <c:if test="${result.totalItems > 0}">
                            <span class="result-count">${result.from}–${result.to} / ${result.totalItems} submissions</span>
                        </c:if>
                    </div>
                    <c:choose>
                        <c:when test="${result.totalItems == 0 and filtered}">
                            <p class="empty">No submissions match the filter.</p>
                        </c:when>
                        <c:when test="${result.totalItems == 0}">
                            <p class="empty">There are no submissions in your classes yet.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="assignment-table">
                                    <thead>
                                        <tr>
                                            <th scope="col">ID</th>
                                            <th scope="col">Assignment</th>
                                            <th scope="col">Class</th>
                                            <th scope="col">Group</th>
                                            <th scope="col">Submitted at</th>
                                            <th scope="col">Status</th>
                                            <th scope="col" class="num">Score</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="s" items="${result.items}">
                                            <tr class="clickable-row" onclick="window.location.href='${ctx}/submissions/detail?id=${s.submissionId}';">
                                                <td class="row-id">#${s.submissionId}</td>
                                                <td class="assignment-name">
                                                    <a href="${ctx}/submissions/detail?id=${s.submissionId}"><strong><c:out value="${s.assignmentTitle}"/></strong></a>
                                                    <span class="table-sub">Due <c:out value="${s.dueDateText}"/></span>
                                                </td>
                                                <td><c:out value="${s.classCode}"/></td>
                                                <td class="assignment-name">
                                                    Group #${s.groupId}
                                                    <span class="table-sub">submitted by <c:out value="${s.studentName}"/></span>
                                                </td>
                                                <td>
                                                    <c:out value="${s.submitTimeText}"/>
                                                    <c:if test="${s.late}"><span class="badge bad">Late</span></c:if>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${s.graded}"><span class="badge ok">Graded</span></c:when>
                                                        <c:otherwise><span class="badge">Not graded</span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="num score"><c:out value="${s.scoreText}"/></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <c:url var="pageBaseUrl" value="/submissions" scope="request">
                                <c:if test="${not empty filterKeyword}"><c:param name="keyword" value="${filterKeyword}"/></c:if>
                                <c:if test="${not empty filterClassId}"><c:param name="classId" value="${filterClassId}"/></c:if>
                                <c:if test="${not empty filterStatus}"><c:param name="status" value="${filterStatus}"/></c:if>
                            </c:url>
                            <jsp:include page="/common/pagination.jsp"/>
                        </c:otherwise>
                    </c:choose>
                </section>
            </main>
        </div>

        <script src="${ctx}/assets/js/theme.js" defer></script>
    </body>
</html>

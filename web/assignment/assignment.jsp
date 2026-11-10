<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/assignment"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css">
        <title>Assignment Management · AMS</title>
    </head>
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <div class="app-layout">
            <jsp:include page="/common/teachersidebar.jsp">
                <jsp:param name="active" value="assignments"/>
            </jsp:include>

            <main class="main-content">
                <section class="hello page-title-row">
                    <div>
                        <p class="eyebrow">ASSIGNMENT MANAGEMENT</p>
                        <h1>Assignments</h1>
                        <p class="sub">Assignments of the classes you teach.</p>
                    </div>
                    <a class="button button-primary" href="${ctx}/assignment?action=new">Create assignment</a>
                </section>

                <c:if test="${saved}">
                    <div class="notice" role="status">Assignment has been created.</div>
                </c:if>                <c:if test="${not empty error}">
                    <div class="alert" role="alert"><c:out value="${error}"/></div>
                </c:if>

                <%-- Filter --%>
                <section class="filter-bar" aria-label="Filter assignments">
                    <form class="filter-form" method="get" action="${ctx}/assignment">
                        <div class="filter-search">
                            <label for="keyword">Search assignment</label>
                            <input class="control" id="keyword" name="keyword" type="search"
                                   value="<c:out value='${filterKeyword}'/>" placeholder="Name or content">
                        </div>
                        <div>
                            <label for="filterSubject">Subject</label>
                            <select class="control" id="filterSubject" name="subjectId">
                                <option value="">All subjects</option>
                                <c:forEach var="subject" items="${subjects}">
                                    <option value="${subject.id}" ${filterSubjectId == subject.id ? 'selected' : ''}><c:out value="${subject.name}"/></option>
                                </c:forEach>
                            </select>
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
                                <option value="">All statuses</option>
                                <c:forEach var="s" items="Draft,Published,Closed">
                                    <option value="${s}" ${filterStatus == s ? 'selected' : ''}>${s}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="filter-actions">
                            <button class="button button-primary" type="submit">Filter</button>
                            <a class="button button-quiet" href="${ctx}/assignment">Clear filters</a>
                        </div>
                    </form>
                </section>

                <%-- List --%>
                <section class="panel list-panel" aria-labelledby="list-heading">
                    <div class="panel-head">
                        <div>
                            <p class="eyebrow">OVERVIEW</p>
                            <h2 id="list-heading">Assignment list</h2>
                        </div>
                        <c:if test="${result.totalItems > 0}">
                            <span class="result-count">${result.from}–${result.to} / ${result.totalItems} assignments</span>
                        </c:if>
                    </div>
                    <c:choose>
                        <c:when test="${result.totalItems == 0}">
                            <p class="empty">No matching assignments found.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="assignment-table">
                                    <thead>
                                        <tr>
                                            <th scope="col">ID</th>
                                            <th scope="col">Assignment</th>
                                            <th scope="col">Subject</th>
                                            <th scope="col">Class</th>
                                            <th scope="col">Start date</th>
                                            <th scope="col">Due date</th>
                                            <th scope="col">Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="a" items="${result.items}">
                                            <tr style="cursor: pointer;" onclick="window.location.href='${ctx}/assignment?id=${a.assignmentId}';">
                                                <td class="row-id">#<c:out value="${a.assignmentId}"/></td>
                                                <td class="assignment-name">
                                                    <a href="${ctx}/assignment?id=${a.assignmentId}"><strong><c:out value="${a.title}"/></strong></a>
                                                    <c:if test="${not empty a.instruction}">
                                                        <span class="table-sub"><c:out value="${a.instruction}"/></span>
                                                    </c:if>
                                                </td>
                                                <td><c:out value="${a.subjectName}"/></td>
                                                <td><c:out value="${a.classCode}"/></td>
                                                <td><c:out value="${a.startDateText}"/></td>
                                                <td><c:out value="${a.dueDateText}"/></td>
                                                <td><span class="status-pill status-${fn:toLowerCase(a.status)}"><c:out value="${a.status}"/></span></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <c:url var="pageBaseUrl" value="/assignment" scope="request">
                                <c:if test="${not empty filterKeyword}"><c:param name="keyword" value="${filterKeyword}"/></c:if>
                                <c:if test="${not empty filterSubjectId}"><c:param name="subjectId" value="${filterSubjectId}"/></c:if>
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

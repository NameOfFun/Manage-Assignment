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
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css?v=20261003">
        <title>Expert Dashboard · AMS</title>
    </head>
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <div class="app-layout">
            <jsp:include page="/common/expertsidebar.jsp">
                <jsp:param name="active" value="dashboard"/>
            </jsp:include>


            <main class="main-content">
                <section class="hello page-title-row">
                    <div>
                        <p class="eyebrow">EXPERT OVERVIEW</p>
                        <h1>Hello, <c:out value="${meName}"/></h1>
                        <p class="sub">Your classes and assignment templates to manage and create.</p>
                    </div>
                </section>

                <c:if test="${not empty error}">
                    <div class="alert" role="alert"><c:out value="${error}"/></div>
                </c:if>

                <div class="stats-grid">
                    <div class="stat">
                        <span class="stat-label">Classes</span>
                        <span class="stat-value"><c:out value="${classCount}"/></span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">Subjects you manage</span>
                        <span class="stat-value"><c:out value="${subjectCount}"/></span>
                    </div>
                    <div class="stat">
                        <div class="panel-head">
                            <div>
                                <h2 id="classes-heading">Latest classes</h2>
                            </div>
                            <a class="text-link" href="${ctx}/classes">All classes</a>
                        </div>
                        <c:choose>
                            <c:when test="${empty classes}">
                                <p class="empty">No classes in the current semester.</p>
                            </c:when>
                            <c:otherwise>
                                <ul class="class-list">
                                    <c:forEach var="cl" items="${classes}">
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
                    </div>
                    <div class="stat">
                        <div class="panel-head">
                            <div>
                                <h2 id="classes-heading">All subjects</h2>
                            </div>
                        </div>
                        <c:choose>
                            <c:when test="${empty subjects}">
                                <p class="empty">You are not managing any subject.</p>
                            </c:when>
                            <c:otherwise>
                                <ul class="class-list">
                                    <c:forEach var="subject" items="${subjects}">
                                        <li class="class-item">
                                            <div class="class-top">
                                                <span class="code"><c:out value="${subject.name}"/></span>
                                            </div>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </main>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

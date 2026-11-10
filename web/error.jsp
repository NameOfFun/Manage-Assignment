<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css?v=20261003" />
        <title>Error Page</title>
    </head>
    <body class="app">

        <!-- Profile header -->
        <%@ include file="/common/header.jsp" %>

        <c:if test="${not empty sessionScope.error}">
            <div class="error-msg" 
                 style="color: #d93025; background-color: rgba(217, 48, 37, 0.1); border: 1px solid #d93025; padding: 0.75rem 1rem;
                 border-radius: 6px; margin-bottom: 1rem; font-size: 0.875rem; text-align: center;">
                ${sessionScope.error}
            </div>
            <c:remove var="error" scope="session" />
        </c:if>

        <!-- Navigation Back Link -->
        <div class="panel">
            <ul class="due-list">
                <li class="due-item">
                    <div class="due-side">
                        <span class="due-title">
                            <form action="${pageContext.request.contextPath}/auth" method="get">
                                <button class="btn logout-btn" type="submit">Return to Dashboard</button>
                            </form>
                        </span>
                    </div>
                </li>
            </ul>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>
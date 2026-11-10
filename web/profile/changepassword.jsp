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
        <title>User Profile</title>
    </head>
    <body class="app">

        <!-- Profile header -->
        <%@ include file="/common/header.jsp" %>

        <main class="page">
            <div class="hello">
                <h1>Change password</h1>
                <p class="sub">Update your current password.</p>
            </div>

            <c:if test="${not empty error}">
                <div class="error-msg" 
                     style="color: #d93025; background-color: rgba(217, 48, 37, 0.1); border: 1px solid #d93025; padding: 0.75rem 1rem;
                     border-radius: 6px; margin-bottom: 1rem; font-size: 0.875rem; text-align: center;">
                    ${error}
                </div>
                <c:remove var="error" scope="session" />
            </c:if>

            <c:if test="${not empty success}">
                <div class="error-msg" 
                     style="color: #25d957; background-color: rgba(37, 217, 48, 0.1); border: 1px solid #25d957; padding: 0.75rem 1rem;
                     border-radius: 6px; margin-bottom: 1rem; font-size: 0.875rem; text-align: center;">
                    ${success}
                </div>
                <c:remove var="success" scope="session" />
            </c:if>

            <!-- Security & Password Card Panel -->
            <div class="panel">
                <form id="password-form" method="post" action="${pageContext.request.contextPath}/profile?action=changepassword">
                    <ul class="due-list">
                        <li class="due-item">
                            <div class="due-main">
                                <label for="curpw" class="due-title">Current Password</label>
                                <span class="due-sub">Enter your current password to authorize changes</span>
                            </div>
                            <input class="field" id="dpw" name="password" type="password" placeholder="••••••••">
                        </li>

                        <li class="due-item">
                            <div class="due-main">
                                <label for="newpw" class="due-title">New Password</label>
                                <span class="due-sub">Choose a strong, secure new password</span>
                            </div>
                            <input class="field" id="newpw" name="newPassword" type="password" placeholder="••••••••">
                        </li>

                        <li class="due-item">
                            <div class="due-main">
                                <label for="newpw" class="due-title">Confirm Password</label>
                                <span class="due-sub">Re-enter your new password</span>
                            </div>
                            <input class="field" id="conpw" name="confirmPassword" type="password" placeholder="••••••••">
                        </li>
                    </ul>

                    <div style="margin-top: 1.5rem; display: flex; justify-content: flex-end;">
                        <button class="btn" type="submit">Update Password</button>
                    </div>
                </form>
            </div>

            <!-- Navigation Back Link -->
            <div class="panel">
                <ul class="due-list">
                    <li class="due-item">
                        <div class="due-side">
                            <span class="due-title">
                                <a href="${pageContext.request.contextPath}/profile">&larr; Return to profile</a>
                            </span>
                        </div>
                    </li>
                </ul>
            </div>
        </main>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>
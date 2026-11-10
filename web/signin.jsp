<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css" />
        <title>Sign in</title>

        <script src="${pageContext.request.contextPath}/assets/js/google-auth.js" defer></script>
        <script src="https://accounts.google.com/gsi/client" async defer onload="initGoogleAuth()"></script>
    </head>
    <body>
        <main class="box">
            <button class="tog" id="tog" type="button" aria-label="Switch colour theme">
                <svg class="sun" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4.2"/><path d="M12 2v2.4M12 19.6V22M4.2 4.2l1.7 1.7M18.1 18.1l1.7 1.7M2 12h2.4M19.6 12H22M4.2 19.8l1.7-1.7M18.1 5.9l1.7-1.7"/></svg>
            </button>
            <div class="mark" aria-hidden="true">N</div>

            <h1>Sign in</h1>
            <p class="sub">The social constructive learning tool.</p>

            <c:if test="${not empty messages}">
                <c:forEach var="msg" items="${messages}">
                    <c:choose>
                        <c:when test="${not msg.success}">
                            <div class="error-msg" 
                                 style="color: #d93025; background-color: rgba(217, 48, 37, 0.1); border: 1px solid #d93025; padding: 0.75rem 1rem;
                                 border-radius: 6px; margin-bottom: 1rem; font-size: 0.875rem; text-align: center;">
                                ${msg.message}
                            </div>
                        </c:when>

                        <c:when test="${msg.success}">
                            <div class="error-msg" 
                                 style="color: #25d957; background-color: rgba(37, 217, 48, 0.1); border: 1px solid #25d957; padding: 0.75rem 1rem;
                                 border-radius: 6px; margin-bottom: 1rem; font-size: 0.875rem; text-align: center;">
                                ${msg.message}
                            </div>
                        </c:when>                       
                    </c:choose>
                </c:forEach>
                <c:remove var="messages" scope="session" />
            </c:if>

            <form id="signin-form" method="post" action="auth?action=signin">
                <label for="duser">Username</label>
                <input class="field" id="duser" name="username" type="text" autocomplete="text" 
                       value="${param.username}" placeholder="Username">
                <label for="dpw">Password</label>
                <input class="field" id="dpw" name="password" type="password" autocomplete="current-password" 
                       value="${param.password}" placeholder="Password">
                <div class="row">
                    <a href="${pageContext.request.contextPath}/forgotpassword.jsp">Forgot password?</a>
                </div>
                <button class="btn" type="submit">Sign in</button>
            </form>

            <div class="divider">
                <span>or</span>
            </div>

            <!-- Container where JS will render the custom Google button -->
            <div id="google-btn-container"></div>

            <!-- Hidden Form to submit Google Credential to Servlet -->
            <form id="google-form" method="post" action="auth/google">
                <input type="hidden" name="id_token" id="google_credential">
            </form>

            <p class="foot">No account? <a href="${pageContext.request.contextPath}/register.jsp">Create one</a></p>
        </main>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

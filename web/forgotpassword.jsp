<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css" />
        <title>Reset password</title>
    </head>
    <body>
        <main class="box">
            <button class="tog" id="tog" type="button" aria-label="Switch colour theme">
                <svg class="sun" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4.2"/><path d="M12 2v2.4M12 19.6V22M4.2 4.2l1.7 1.7M18.1 18.1l1.7 1.7M2 12h2.4M19.6 12H22M4.2 19.8l1.7-1.7M18.1 5.9l1.7-1.7"/></svg>
            </button>
            <div class="mark" aria-hidden="true">N</div>

            <h1>Reset password</h1>
            <p class="sub">Don't remember the password to your account?</p>

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


            <form id="forgot-form" method="post" action="recover">
                <label for="duser">Email</label>
                <input class="field" id="demail" name="email" type="text" autocomplete="text" 
                       value="${param.email}" placeholder="example@gmail.com">
                <div class="row">
                    <a href="${pageContext.request.contextPath}/signin.jsp">Back to sign in</a>
                </div>
                <button class="btn" type="submit">Send recover email</button>
            </form>
        </main>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

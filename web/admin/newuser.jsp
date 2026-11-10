<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css" />
        <style>
            .radio-group { display: flex; flex-direction: row; gap: 20px; padding: 8px 0; }
            .radio-label { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: .9rem; }
            .radio-label input[type="radio"] { width: 16px; height: 16px; cursor: pointer; }
            .form-actions .btn { width: auto !important; padding: 13px 32px; }
        </style>
        <title>New User – AMS</title>
    </head>
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <main class="page">

            <%-- Back link --%>
            <a class="back-link" href="${pageContext.request.contextPath}/admin/users">
                &#8592; User Management
            </a>

            <div class="hello">
                <h1>New User</h1>
                <p class="sub">Create a new user account in the system.</p>
            </div>

            <c:if test="${not empty error}">
                <div class="alert">${error}</div>
            </c:if>

            <div class="form-card">
                <form id="user-form" method="post" action="${pageContext.request.contextPath}/admin/users">
                    <input type="hidden" name="action" value="create" />

                    <div class="form-row">
                        <%-- Full Name --%>
                        <div class="form-group">
                            <label for="fullName">Full Name <span style="color:var(--badT)">*</span></label>
                            <input class="field" id="fullName" name="fullName" type="text"
                                   placeholder="Nguyen Van A"
                                   value="${param.fullName}"/>
                            <c:if test="${not empty fullNameError}">
                                <span class="err-msg">${fullNameError}</span>
                            </c:if>
                        </div>

                        <%-- Username --%>
                        <div class="form-group">
                            <label for="username">Username <span style="color:var(--badT)">*</span></label>
                            <input class="field" id="username" name="username" type="text"
                                   placeholder="nguyenvana"
                                   value="${param.username}" />
                            <c:if test="${not empty usernameError}">
                                <span class="err-msg">${usernameError}</span>
                            </c:if>
                        </div>
                    </div>

                    <div class="form-row">
                        <%-- Email --%>
                        <div class="form-group">
                            <label for="email">Email <span style="color:var(--badT)">*</span></label>
                            <input class="field" id="email" name="email" type="email"
                                   placeholder="example@email.com" maxlength="50"
                                   value="${param.email}" />
                            <c:if test="${not empty emailError}">
                                <span class="err-msg">${emailError}</span>
                            </c:if>
                        </div>

                        <%-- Role --%>
                        <div class="form-group">
                            <label for="roleId">Role <span style="color:var(--badT)">*</span></label>
                            <select class="field" id="roleId" name="roleId">
                                <option value="">Select role...</option>
                                <c:forEach var="r" items="${roles}">
                                    <option value="${r.id}" ${param.roleId == r.id ? 'selected' : ''}>${r.name}</option>
                                </c:forEach>
                            </select>
                            <c:if test="${not empty roleError}">
                                <span class="err-msg">${roleError}</span>
                            </c:if>
                        </div>
                    </div>

                    <div class="form-row">
                        <%-- Status: radio button Active / Inactive --%>
                        <div class="form-group">
                            <label>Status <span style="color:var(--badT)">*</span></label>
                            <div class="radio-group">
                                <label class="radio-label">
                                    <input type="radio" name="status" value="Active"
                                           ${empty param.status || param.status == 'Active' ? 'checked' : ''} />
                                    Active
                                </label>
                                <label class="radio-label">
                                    <input type="radio" name="status" value="Inactive"
                                           ${param.status == 'Inactive' ? 'checked' : ''} />
                                    Inactive
                                </label>
                            </div>
                            <span id="statusError" class="err-msg" style="display:none;">Please select a status.</span>
                        </div>
                        <div class="form-group"></div>
                    </div>

                    <div class="form-actions">
                        <a class="btn-cancel" href="${pageContext.request.contextPath}/admin/users">Cancel</a>
                        <button class="btn" type="submit">Create User</button>
                    </div>
                </form>
            </div>
        </main>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

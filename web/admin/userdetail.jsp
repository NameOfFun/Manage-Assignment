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
        <title>User Detail – AMS</title>
    </head>
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <main class="page">

            <a class="back-link" href="${pageContext.request.contextPath}/admin/users">
                &#8592; User Management
            </a>

            <div class="hello">
                <h1>User Detail</h1>
                <p class="sub">Update information for <strong>${not empty param.username ? param.username : editUser.username}</strong>.</p>
            </div>

            <c:if test="${not empty error}">
                <div class="alert">${error}</div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>

            <div class="form-card">
                <form method="post" action="${pageContext.request.contextPath}/admin/users">
                    <input type="hidden" name="action" value="update" />
                    <input type="hidden" name="id"     value="${editUser.id}" />

                    <%-- Avatar + basic info --%>
                    <div style="display:flex;align-items:center;gap:16px;margin-bottom:20px;">
                        <img src="${not empty editUser.imageUrl ? editUser.imageUrl : 'https://res.cloudinary.com/b429n8kl/image/upload/v1792319974/default-avatar.png'}"
                             alt="Avatar"
                             style="width:64px;height:64px;border-radius:50%;object-fit:cover;border:2px solid var(--edge);" />
                        <div>
                            <div style="font-weight:600;font-size:1rem;">${editUser.fullName}</div>
                            <div style="font-size:.85rem;color:var(--dim);">${editUser.email}</div>
                        </div>
                    </div>

                    <div class="form-row">
                        <%-- Full Name --%>
                        <div class="form-group">
                            <label for="fullName">Full Name <span style="color:var(--badT)">*</span></label>
                            <input class="field" id="fullName" name="fullName" type="text"
                                   placeholder="Nguyen Van A"
                                   value="${not empty param.fullName ? param.fullName : editUser.fullName}"
                                    />
                            <c:if test="${not empty fullNameError}">
                                <span class="err-msg">${fullNameError}</span>
                            </c:if>
                        </div>

                        <%-- Username --%>
                        <div class="form-group">
                            <label for="username">Username <span style="color:var(--badT)">*</span></label>
                            <input class="field" id="username" name="username" type="text"
                                   value="${not empty param.username ? param.username : editUser.username}"
                                    />
                            <c:if test="${not empty usernameError}">
                                <span class="err-msg">${usernameError}</span>
                            </c:if>
                        </div>
                    </div>

                    <div class="form-row">
                        <%-- Email — read-only --%>
                        <div class="form-group">
                            <label>Email</label>
                            <div class="field-readonly">${editUser.email}</div>
                        </div>

                        <%-- Role --%>
                        <div class="form-group">
                            <label for="roleId">Role <span style="color:var(--badT)">*</span></label>
                            <select class="field" id="roleId" name="roleId">
                                <c:set var="currentRole" value="${not empty param.roleId ? param.roleId : editUser.roleId}" />
                                <c:forEach var="r" items="${roles}">
                                    <option value="${r.id}" ${currentRole == r.id ? 'selected' : ''}>${r.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <%-- Status --%>
                        <div class="form-group">
                            <label>Status <span style="color:var(--badT)">*</span></label>
                            <c:set var="currentStatus" value="${not empty param.status ? param.status : editUser.status}" />
                            <div class="radio-group">
                                <label class="radio-label">
                                    <input type="radio" name="status" value="Active"
                                           ${currentStatus == 'Active' ? 'checked' : ''} />
                                    Active
                                </label>
                                <label class="radio-label">
                                    <input type="radio" name="status" value="Inactive"
                                           ${currentStatus == 'Inactive' ? 'checked' : ''} />
                                    Inactive
                                </label>
                            </div>
                        </div>

                        <%-- Created At — read-only, formatted dd-MM-yyyy --%>
                        <div class="form-group">
                            <label>Created At</label>
                            <div class="field-readonly">${not empty createdAtFormatted ? createdAtFormatted : editUser.createdAt}</div>
                        </div>
                    </div>

                    <%-- Nút Cancel/Save: với role khác Expert thì hiện ở đây --%>
                    <c:if test="${empty allSubjects}">
                        <div class="form-actions">
                            <a class="btn-cancel" href="${pageContext.request.contextPath}/admin/users">Cancel</a>
                            <button class="btn" type="submit">Save Changes</button>
                        </div>
                    </c:if>

                    <%-- Managed Subjects — chỉ hiện khi user là Expert, nằm trong cùng form --%>
                    <c:if test="${not empty allSubjects}">
                        <hr style="border:none;border-top:1px solid var(--edge);margin:20px 0;" />
                        <div style="margin-bottom:8px;">
                            <label style="font-weight:600;font-size:.9rem;">Managed Subjects</label>
                            <p class="sub" style="margin-top:4px;">Select the subjects this expert is responsible for.</p>
                        </div>
                        <div class="subject-scroll">
                            <c:forEach var="s" items="${allSubjects}">
                                <c:set var="assigned" value="false" />
                                <c:forEach var="es" items="${expertSubjects}">
                                    <c:if test="${es.id == s.id}"><c:set var="assigned" value="true" /></c:if>
                                </c:forEach>
                                <label style="display:flex;align-items:center;gap:10px;cursor:pointer;padding:8px 12px;border-radius:8px;border:1px solid var(--edge);">
                                    <input type="checkbox" name="subjectIds" value="${s.id}"
                                           ${assigned ? 'checked' : ''}
                                           style="width:16px;height:16px;cursor:pointer;accent-color:var(--accent,#4f8ef7);flex-shrink:0;" />
                                    <span style="font-size:.9rem;">${s.name}</span>
                                </label>
                            </c:forEach>
                        </div>
                        <%-- Nút Cancel/Save: với Expert thì hiện sau danh sách môn --%>
                        <div class="form-actions" style="margin-top:16px;">
                            <a class="btn-cancel" href="${pageContext.request.contextPath}/admin/users">Cancel</a>
                            <button class="btn" type="submit">Save Changes</button>
                        </div>
                    </c:if>

                </form>
            </div>

        </main>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

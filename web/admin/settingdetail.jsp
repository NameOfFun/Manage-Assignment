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
        <title>${empty editSetting ? 'New Setting' : 'Edit Setting'} – AMS</title>
    </head>
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <main class="page">

            <a class="back-link" href="${pageContext.request.contextPath}/admin/settings">
                &#8592; System Settings
            </a>

            <div class="hello">
                <c:choose>
                    <c:when test="${empty editSetting}">
                        <h1>New Setting</h1>
                        <p class="sub">Add a new master data entry to the system.</p>
                    </c:when>
                    <c:otherwise>
                        <h1>Edit Setting</h1>
                        <p class="sub">Update details for <strong>${editSetting.name}</strong>.</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <c:if test="${not empty error}">
                <div class="alert">${error}</div>
            </c:if>

            <div class="form-card">
                <form method="post" action="${pageContext.request.contextPath}/admin/settings">
                    <input type="hidden" name="action" value="${empty editSetting ? 'create' : 'update'}" />
                    <c:if test="${not empty editSetting}">
                        <input type="hidden" name="id" value="${editSetting.id}" />
                    </c:if>

                    <div class="form-row">
                        <%-- Name --%>
                        <div class="form-group">
                            <label for="name">Name <span style="color:var(--badT)">*</span></label>
                            <input class="field" id="name" name="name" type="text"
                                   placeholder="e.g. Mathematics"
                                   value="${not empty param.name ? param.name : editSetting.name}"
                                    />
                            <c:if test="${not empty nameError}">
                                <span class="err-msg">${nameError}</span>
                            </c:if>
                        </div>

                        <%-- Type --%>
                        <div class="form-group">
                            <label for="type">Type <span style="color:var(--badT)">*</span></label>
                            <c:set var="currentType" value="${not empty param.type ? param.type : editSetting.type}" />
                            <c:choose>
                                <c:when test="${isProtected}">
                                    <%-- Role/Semester: hiển thị readonly, không cho sửa --%>
                                    <div class="field-readonly">${currentType}</div>
                                    <input type="hidden" name="type" value="${currentType}" />
                                </c:when>
                                <c:otherwise>
                                    <select class="field" id="type" name="type">
                                        <option value="">Select type...</option>
                                        <c:forEach var="t" items="${settingTypes}">
                                            <option value="${t}" ${currentType == t ? 'selected' : ''}>${t}</option>
                                        </c:forEach>
                                    </select>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${not empty typeError}">
                                <span class="err-msg">${typeError}</span>
                            </c:if>
                        </div>
                    </div>

                    <div class="form-row">
                        <%-- Value --%>
                        <div class="form-group">
                            <label for="value">Value</label>
                            <input class="field" id="value" name="value" type="text"
                                   placeholder="e.g. MATH"
                                   value="${not empty param.value ? param.value : editSetting.value}" />
                            <c:if test="${not empty valueError}">
                                <span class="err-msg">${valueError}</span>
                            </c:if>
                        </div>

                        <%-- Priority --%>
                        <div class="form-group">
                            <label for="priority">Priority <span style="color:var(--badT)">*</span></label>
                            <input class="field" id="priority" name="priority" type="number" min="1"
                                   placeholder="e.g. 1"
                                   value="${not empty param.priority ? param.priority : editSetting.priority}"
                                    />
                            <c:if test="${not empty priorityError}">
                                <span class="err-msg">${priorityError}</span>
                            </c:if>
                        </div>
                    </div>

                    <div class="form-row">
                        <%-- Description --%>
                        <div class="form-group form-full">
                            <label for="description">Description</label>
                            <textarea class="field" id="description" name="description"
                                      placeholder="Optional description..."><c:out value="${not empty param.description ? param.description : editSetting.description}" /></textarea>
                            <c:if test="${not empty descriptionError}">
                                <span class="err-msg">${descriptionError}</span>
                            </c:if>
                        </div>
                    </div>

                    <div class="form-row">
                        <%-- Status: radio button, new setting mặc định Active --%>
                        <div class="form-group">
                            <label>Status</label>
                            <c:set var="currentStatus" value="${not empty param.status ? param.status : (not empty editSetting ? editSetting.status : 'Active')}" />
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
                        <div class="form-group"></div>
                    </div>

                    <div class="form-actions">
                        <a class="btn-cancel" href="${pageContext.request.contextPath}/admin/settings">Cancel</a>
                        <button class="btn" type="submit">
                            <c:choose>
                                <c:when test="${empty editSetting}">Add Setting</c:when>
                                <c:otherwise>Save Changes</c:otherwise>
                            </c:choose>
                        </button>
                    </div>
                </form>
            </div>

        </main>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

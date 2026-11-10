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
            .form-actions .btn { width: auto !important; padding: 13px 32px; }
        </style>
        <title>${empty editTemplate ? 'New Template' : 'Edit Template'} – AMS</title>
    </head>
    <body class="app">

        <jsp:include page="/common/header.jsp" />

        <div class="app-layout">
            <main class="main-content">
                <a class="back-link" href="${pageContext.request.contextPath}/expert/templates">
                    &#8592; Assignment Templates
                </a>

                <div class="hello">
                    <c:choose>
                        <c:when test="${empty editTemplate}">
                            <h1>New Template</h1>
                            <p class="sub">Create a new assignment template for a subject.</p>
                        </c:when>
                        <c:otherwise>
                            <h1>Edit Template</h1>
                            <p class="sub">Update details for <strong>${editTemplate.title}</strong>.</p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert">${error}</div>
                </c:if>

                <div class="form-card">
                    <form method="post" action="${pageContext.request.contextPath}/expert/templates">
                        <input type="hidden" name="action" value="${empty editTemplate ? 'create' : 'update'}" />
                        <c:if test="${not empty editTemplate}">
                            <input type="hidden" name="id" value="${editTemplate.id}" />
                        </c:if>

                        <%-- Row 1: Title + Subject --%>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="title">Title <span style="color:var(--badT)">*</span></label>
                                <input class="field" id="title" name="title" type="text"
                                       placeholder="e.g. SRS Document"
                                       value="${not empty param.title ? param.title : editTemplate.title}" />
                                <c:if test="${not empty titleError}">
                                    <span class="err-msg">${titleError}</span>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label for="subjectId">Subject <span style="color:var(--badT)">*</span></label>
                                <c:set var="currentSubject" value="${not empty param.subjectId ? param.subjectId : editTemplate.subjectId}" />
                                <select class="field" id="subjectId" name="subjectId">
                                    <option value="">Select subject...</option>
                                    <c:forEach var="s" items="${subjects}">
                                        <option value="${s.id}" ${currentSubject == s.id ? 'selected' : ''}>${s.name}</option>
                                    </c:forEach>
                                </select>
                                <c:if test="${not empty subjectError}">
                                    <span class="err-msg">${subjectError}</span>
                                </c:if>
                            </div>
                        </div>

                        <%-- Row 2: Instruction full width --%>
                        <div class="form-row">
                            <div class="form-group form-full">
                                <label for="instruction">Instruction <span style="color:var(--badT)">*</span></label>
                                <textarea class="field" id="instruction" name="instruction"
                                          placeholder="Enter assignment instructions..."
                                          style="min-height:200px;resize:vertical;"><c:out value="${not empty param.instruction ? param.instruction : editTemplate.instruction}" /></textarea>
                                <c:if test="${not empty instructionError}">
                                    <span class="err-msg">${instructionError}</span>
                                </c:if>
                            </div>
                        </div>

                        <div class="form-actions">
                            <a class="btn-cancel" href="${pageContext.request.contextPath}/expert/templates">Cancel</a>
                            <button class="btn" type="submit">
                                <c:choose>
                                    <c:when test="${empty editTemplate}">Add Template</c:when>
                                    <c:otherwise>Save Changes</c:otherwise>
                                </c:choose>
                            </button>
                        </div>
                    </form>
                </div>

            </main>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>

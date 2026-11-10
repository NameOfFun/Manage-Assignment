<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/assignment"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="valTitle" value="${formSubmitted ? formTitle : assignment.title}"/>
<c:set var="valInstruction" value="${formSubmitted ? formInstruction : assignment.instruction}"/>
<c:set var="valStartDate" value="${formSubmitted ? formStartDate : assignment.startDateInput}"/>
<c:set var="valDueDate" value="${formSubmitted ? formDueDate : assignment.dueDateInput}"/>
<c:set var="valStatus" value="${formSubmitted ? formStatus : assignment.status}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Edit Assignment · AMS</title>
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css">
        <style>
            .edit-wrap {
                max-width: 760px;
            }
            .back-link {
                display: inline-block;
                margin-bottom: 16px;
                font-size: .88rem;
            }
            .form-actions {
                display: flex;
                justify-content: flex-end;
                gap: 8px;
            }
        </style>
    </head>
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <div class="app-layout">
            <jsp:include page="/common/teachersidebar.jsp">
                <jsp:param name="active" value="assignments"/>
            </jsp:include>

            <main class="main-content">
                <div class="edit-wrap">
                    <a class="back-link" href="${ctx}/assignment?id=${assignment.assignmentId}">← Back to details</a>

                    <section class="hello page-title-row">
                        <div>
                            <p class="eyebrow">EDIT ASSIGNMENT · #<c:out value="${assignment.assignmentId}"/></p>
                            <h1><c:out value="${assignment.title}"/></h1>
                            <p class="sub">Update the content, deadline and status of the assignment.</p>
                        </div>
                    </section>

                    <c:if test="${not empty error}">
                        <div class="alert" role="alert"><c:out value="${error}"/></div>
                    </c:if>

                    <section class="panel create-panel" aria-labelledby="edit-heading">
                        <div class="panel-head">
                            <div>
                                <p class="eyebrow">DETAILS</p>
                                <h2 id="edit-heading">Assignment information</h2>
                            </div>
                            <span class="status-pill ${assignment.status == 'Published' ? 'status-published' : assignment.status == 'Closed' ? 'status-closed' : 'status-draft'}">
                                <c:out value="${assignment.status}"/>
                            </span>
                        </div>

                        <form class="assignment-form" method="post" action="${ctx}/assignment" data-submit-once>
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="id" value="${assignment.assignmentId}">

                            <div class="field-row">
                                <div>
                                    <label for="classInfo">Class</label>
                                    <input class="control" id="classInfo" type="text" readonly
                                           value="<c:out value='${assignment.classCode}'/> · <c:out value='${assignment.semester}'/> <c:out value='${assignment.year}'/>">
                                </div>
                                <div>
                                    <label for="subjectName">Subject</label>
                                    <input class="control" id="subjectName" type="text" readonly
                                           value="<c:out value='${assignment.subjectName}'/>">
                                </div>
                            </div>
                            <div>
                                <label for="title">Assignment name <span class="required">*</span></label>
                                <input class="control" id="title" name="title" maxlength="255"
                                       value="<c:out value='${valTitle}'/>" required>
                            </div>
                            <div>
                                <label for="instruction">Instruction <span class="required">*</span></label>
                                <textarea class="control" id="instruction" name="instruction" rows="7" required><c:out value="${valInstruction}"/></textarea>
                            </div>
                            <div class="field-row">
                                <div>
                                    <label for="startDate">Start date <span class="required">*</span></label>
                                    <input class="control" id="startDate" name="startDate" type="datetime-local"
                                           value="<c:out value='${valStartDate}'/>" required>
                                </div>
                                <div>
                                    <label for="dueDate">Due date <span class="required">*</span></label>
                                    <input class="control" id="dueDate" name="dueDate" type="datetime-local"
                                           value="<c:out value='${valDueDate}'/>" required>
                                </div>
                            </div>
                            <div>
                                <label for="status">Status</label>
                                <select class="control" id="status" name="status" required>
                                    <option value="Draft" ${valStatus == 'Draft' ? 'selected' : ''}>Draft</option>
                                    <option value="Published" ${valStatus == 'Published' ? 'selected' : ''}>Published</option>
                                    <option value="Closed" ${valStatus == 'Closed' ? 'selected' : ''}>Closed</option>
                                </select>
                            </div>
                            <p class="form-hint">The class cannot be changed after the assignment is created.</p>
                            <div class="form-actions">
                                <a class="button button-quiet" href="${ctx}/assignment?id=${assignment.assignmentId}">Cancel</a>
                                <button class="button button-primary" type="submit">Save changes</button>
                            </div>
                        </form>
                    </section>
                </div>
            </main>
        </div>

        <script>
            // Same rule as the server: a date the teacher changes cannot be in the past;
            // unchanged dates of an assignment that already started/ended are left alone.
            const startField = document.getElementById('startDate');
            const dueField = document.getElementById('dueDate');
            const originalStart = startField.value;
            const originalDue = dueField.value;
            const pad = n => String(n).padStart(2, '0');
            const now = new Date();
            const today = now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-' + pad(now.getDate());
            const nowValue = today + 'T' + pad(now.getHours()) + ':' + pad(now.getMinutes());

            function check() {
                const startChanged = startField.value !== originalStart;
                const dueChanged = dueField.value !== originalDue;
                startField.setCustomValidity(startChanged && startField.value && startField.value < today + 'T00:00'
                        ? 'Start date cannot be in the past.' : '');
                let dueError = '';
                if (dueChanged && dueField.value && dueField.value <= nowValue) {
                    dueError = 'Due date must be in the future.';
                } else if (dueField.value && startField.value && dueField.value <= startField.value) {
                    dueError = 'Due date must be after the start date.';
                }
                dueField.setCustomValidity(dueError);
            }
            startField.addEventListener('input', check);
            dueField.addEventListener('input', check);
            check();
        </script>
        <script src="${ctx}/assets/js/theme.js" defer></script>
        <script src="${ctx}/assets/js/submit-once.js" defer></script>
    </body>
</html>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/assignment?action=new"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css">
        <title>Create Assignment · AMS</title>
        <style>
            .create-wrap {
                max-width: 760px;
            }
            .back-link {
                display: inline-block;
                margin-bottom: 16px;
                font-size: .88rem;
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
                <div class="create-wrap">
                    <a class="back-link" href="${ctx}/assignment">← Back to assignments</a>

                    <section class="hello page-title-row">
                        <div>
                            <p class="eyebrow">ASSIGNMENT MANAGEMENT</p>
                            <h1>Create assignment</h1>
                            <p class="sub">Create a new assignment for one of your classes.</p>
                        </div>
                    </section>

                    <c:if test="${not empty error}">
                        <div class="alert" role="alert"><c:out value="${error}"/></div>
                    </c:if>

                    <section class="panel create-panel" aria-labelledby="create-heading">
                        <div class="panel-head">
                            <div>
                                <p class="eyebrow">NEW</p>
                                <h2 id="create-heading">Assignment information</h2>
                            </div>
                        </div>
                        <c:choose>
                            <c:when test="${empty classes}">
                                <p class="empty">You have no assigned class to create an assignment for.</p>
                            </c:when>
                            <c:otherwise>
                                <form class="assignment-form" method="post" action="${ctx}/assignment" data-submit-once>
                                    <div class="field-row">
                                        <div>
                                            <label for="classId">Class <span class="required">*</span></label>
                                            <select class="control" id="classId" name="classId" required>
                                                <option value="">Choose a class</option>
                                                <c:forEach var="cl" items="${classes}">
                                                    <option value="${cl.id}" data-subject-id="${cl.subjectId}"
                                                            data-subject-name="<c:out value='${cl.subjectName}'/>"
                                                            ${formClassId == cl.id ? 'selected' : ''}>
                                                        <c:out value="${cl.classCode}"/> · <c:out value="${cl.semester}"/> <c:out value="${cl.year}"/>
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div>
                                            <label for="subjectName">Subject</label>
                                            <input class="control" id="subjectName" type="text" readonly value="">
                                        </div>
                                    </div>
                                    <div>
                                        <label for="templateId">Template <span class="optional">Optional</span></label>
                                        <select class="control" id="templateId" name="templateId">
                                            <option value="">No template</option>
                                            <c:forEach var="template" items="${templates}">
                                                <option value="${template.templateId}"
                                                        data-subject-id="${template.subjectId}"
                                                        data-title="<c:out value='${template.title}'/>"
                                                        data-instruction="<c:out value='${template.instruction}'/>"
                                                        ${formTemplateId == template.templateId ? 'selected' : ''}>
                                                    <c:out value="${template.subjectName}"/> · <c:out value="${template.title}"/>
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div>
                                        <label for="title">Assignment name <span class="required">*</span></label>
                                        <input class="control" id="title" name="title" maxlength="255"
                                               value="<c:out value='${formTitle}'/>" required>
                                    </div>
                                    <div>
                                        <label for="instruction">Instruction <span class="required">*</span></label>
                                        <textarea class="control" id="instruction" name="instruction" rows="5" required><c:out value="${formInstruction}"/></textarea>
                                    </div>
                                    <div class="field-row">
                                        <div>
                                            <label for="startDate">Start date <span class="required">*</span></label>
                                            <input class="control" id="startDate" name="startDate" type="datetime-local"
                                                   value="<c:out value='${formStartDate}'/>" required>
                                        </div>
                                        <div>
                                            <label for="dueDate">Due date <span class="required">*</span></label>
                                            <input class="control" id="dueDate" name="dueDate" type="datetime-local"
                                                   value="<c:out value='${formDueDate}'/>" required>
                                        </div>
                                    </div>
                                    <div>
                                        <label for="status">Save as</label>
                                        <select class="control" id="status" name="status" required>
                                            <option value="Draft" ${empty formStatus || formStatus == 'Draft' ? 'selected' : ''}>Draft</option>
                                            <option value="Published" ${formStatus == 'Published' ? 'selected' : ''}>Publish to class</option>
                                        </select>
                                    </div>
                                    <p class="form-hint">The assignment is given to all students of the chosen class.</p>
                                    <button class="button button-primary button-wide" type="submit">Save assignment</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </div>
            </main>
        </div>

        <script src="${ctx}/assets/js/theme.js" defer></script>
        <script src="${ctx}/assets/js/submit-once.js" defer></script>
        <script>
            // Create form: fill subject from the chosen class, hide templates of other subjects,
            // copy template text into the form, and stop the due date going before the start date.
            const classSelect = document.getElementById('classId');
            const subjectName = document.getElementById('subjectName');
            const templateSelect = document.getElementById('templateId');
            const titleField = document.getElementById('title');
            const instructionField = document.getElementById('instruction');
            const startField = document.getElementById('startDate');
            const dueField = document.getElementById('dueDate');

            function syncClassDetails() {
                const selectedClass = classSelect.selectedOptions[0];
                // The "Choose a class" option has no data-* attributes, so fall back to '' instead of "undefined".
                const subjectId = (selectedClass && selectedClass.dataset.subjectId) || '';
                subjectName.value = (selectedClass && selectedClass.dataset.subjectName) || '';

                Array.from(templateSelect.options).slice(1).forEach(option => {
                    option.hidden = option.dataset.subjectId !== subjectId;
                    if (option.hidden && option.selected) {
                        templateSelect.value = '';
                    }
                });
            }

            if (classSelect) {
                classSelect.addEventListener('change', syncClassDetails);
                templateSelect.addEventListener('change', () => {
                    const template = templateSelect.selectedOptions[0];
                    if (template && template.value) {
                        titleField.value = template.dataset.title || '';
                        instructionField.value = template.dataset.instruction || '';
                    }
                });
                // No past days: start from today 00:00, due date after now and after the start date.
                const pad = n => String(n).padStart(2, '0');
                const now = new Date();
                const today = now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-' + pad(now.getDate());
                const nowValue = today + 'T' + pad(now.getHours()) + ':' + pad(now.getMinutes());
                startField.min = today + 'T00:00';
                function syncDueMin() {
                    dueField.min = startField.value && startField.value > nowValue ? startField.value : nowValue;
                }
                startField.addEventListener('change', syncDueMin);
                syncClassDetails();
                syncDueMin();
            }
        </script>
    </body>
</html>

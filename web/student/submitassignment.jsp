<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/myassignments"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Submit Assignment · AMS</title>
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css">
        <style>
            .submit-wrap {
                max-width: 760px;
            }
            .instruction-text {
                white-space: pre-wrap;
                margin: 0;
            }
        </style>
    </head>
    <body class="app">
        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <main class="page">
            <div class="submit-wrap">
                <c:choose>
                    <c:when test="${not empty row}">
                        <a class="text-link" href="${ctx}/myassignments?classId=${row.assignment.classId}">← Back to class</a>
                    </c:when>
                    <c:otherwise>
                        <a class="text-link" href="${ctx}/studentdashboard">← Back to dashboard</a>
                    </c:otherwise>
                </c:choose>

                <c:if test="${not empty error}">
                    <div class="alert" role="alert"><c:out value="${error}"/></div>
                </c:if>

                <c:if test="${not empty row}">
                    <c:set var="a" value="${row.assignment}"/>
                    <c:set var="s" value="${row.submission}"/>

                    <section class="hello page-title-row">
                        <div>
                            <p class="eyebrow"><c:out value="${a.classCode}"/> · <c:out value="${a.semester}"/> <c:out value="${a.year}"/></p>
                            <h1><c:out value="${a.title}"/></h1>
                            <p class="sub"><c:out value="${a.subjectName}"/></p>
                        </div>
                        <span class="badge ${row.statusClass}"><c:out value="${row.statusText}"/></span>
                    </section>

                    <c:if test="${submitted}">
                        <div class="notice" role="status">Your work has been submitted.</div>
                    </c:if>

                    <section class="panel" aria-labelledby="info-heading">
                        <div class="panel-head">
                            <h2 id="info-heading">Assignment</h2>
                        </div>
                        <dl class="detail-list">
                            <dt>Start date</dt>
                            <dd><c:out value="${a.startDateText}"/></dd>
                            <dt>Due date</dt>
                            <dd><c:out value="${a.dueDateText}"/></dd>
                            <dt>Instruction</dt>
                            <dd><p class="instruction-text"><c:out value="${a.instruction}"/></p></dd>
                        </dl>
                    </section>

                    <c:if test="${row.submitted}">
                        <section class="panel" aria-labelledby="work-heading">
                            <div class="panel-head">
                                <h2 id="work-heading">Your group's submission</h2>
                            </div>
                            <dl class="detail-list">
                                <dt>Work</dt>
                                <dd>
                                    <c:choose>
                                        <c:when test="${s.uploaded}"><c:out value="${s.fileName}"/></c:when>
                                        <c:otherwise><a class="text-link" href="<c:out value='${s.fileUrl}'/>" target="_blank" rel="noopener"><c:out value="${s.fileUrl}"/></a></c:otherwise>
                                    </c:choose>
                                </dd>
                                <dt>Submitted by</dt>
                                <dd><c:out value="${s.studentName}"/></dd>
                                <dt>Submitted at</dt>
                                <dd>
                                    <c:out value="${s.submitTimeText}"/>
                                    <c:if test="${s.late}"><span class="badge bad">Late</span></c:if>
                                </dd>
                                <c:if test="${row.graded}">
                                    <dt>Score</dt>
                                    <dd><strong><c:out value="${s.scoreText}"/></strong></dd>
                                    <dt>Feedback</dt>
                                    <dd><p class="instruction-text"><c:out value="${empty s.feedback ? '—' : s.feedback}"/></p></dd>
                                </c:if>
                            </dl>
                        </section>
                    </c:if>

                    <section class="panel create-panel" aria-labelledby="form-heading">
                        <div class="panel-head">
                            <h2 id="form-heading">${row.submitted ? 'Replace your submission' : 'Submit your work'}</h2>
                        </div>

                        <c:if test="${not empty formError}">
                            <div class="alert" role="alert"><c:out value="${formError}"/></div>
                        </c:if>

                        <c:choose>
                            <c:when test="${not row.canSubmit}">
                                <p class="empty"><c:out value="${row.blockedReason}"/></p>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${row.pastDue}">
                                    <p class="form-hint">The due date has passed. Your work will be marked as late.</p>
                                </c:if>
                                <form class="assignment-form" method="post" enctype="multipart/form-data"
                                      action="${ctx}/myassignments?id=${a.assignmentId}" data-submit-once>
                                    <div>
                                        <label for="file">Upload a file</label>
                                        <input class="control" id="file" name="file" type="file"
                                               accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.rar,.7z,.png,.jpg,.jpeg">
                                    </div>
                                    <div>
                                        <label for="link">Or enter a link</label>
                                        <input class="control" id="link" name="link" type="url" maxlength="255"
                                               placeholder="https://github.com/your-group/your-project"
                                               value="<c:out value='${formLink}'/>">
                                    </div>
                                    <p class="form-hint">One submission per group, maximum file size 20 MB. You can replace it until it is graded.</p>
                                    <button class="button button-primary button-wide" type="submit">Submit</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </c:if>
            </div>
        </main>

        <script src="${ctx}/assets/js/theme.js" defer></script>
        <script>
            // Same limit as the server: stop a file that is too large before it is uploaded.
            const fileField = document.getElementById('file');
            if (fileField) {
                fileField.addEventListener('change', () => {
                    const tooLarge = fileField.files.length > 0 && fileField.files[0].size > 20 * 1024 * 1024;
                    fileField.setCustomValidity(tooLarge ? 'The file cannot be larger than 20 MB.' : '');
                    fileField.reportValidity();
                });
            }
        </script>
        <script src="${ctx}/assets/js/submit-once.js" defer></script>
    </body>
</html>

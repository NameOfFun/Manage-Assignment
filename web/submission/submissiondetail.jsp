<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/submissions"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css">
        <title>Submission Details · AMS</title>
    </head>
    <body class="app">

        <jsp:include page="/common/header.jsp" />

        <div class="app-layout">
            <jsp:include page="/common/teachersidebar.jsp">
                <jsp:param name="active" value="submissions"/>
            </jsp:include>

            <main class="main-content">
                <a class="back-link" href="${ctx}/submissions">&#8592; Back to Submission List</a>

                <c:choose>
                    <c:when test="${not empty accessError}">
                        <section class="hello">
                            <p class="eyebrow">ACCESS DENIED</p>
                            <h1>Access not allowed</h1>
                        </section>
                        <div class="alert" role="alert"><c:out value="${accessError}"/></div>
                    </c:when>

                    <c:otherwise>
                        <c:set var="s" value="${submission}"/>
                        <c:if test="${graded}">
                            <div class="notice" role="status">Score saved.</div>
                        </c:if>

                        <section class="hello page-title-row">
                            <div>
                                <p class="eyebrow">SUBMISSION DETAILS · #${s.submissionId}</p>
                                <h1><c:out value="${s.assignmentTitle}"/></h1>
                                <p class="sub">Group #${s.groupId} · <c:out value="${s.classCode}"/></p>
                            </div>
                            <div class="title-actions">
                                <c:if test="${s.late}"><span class="badge bad">Late submission</span></c:if>
                                <c:choose>
                                    <c:when test="${s.graded}"><span class="badge ok">Graded</span></c:when>
                                    <c:otherwise><span class="badge">Not graded</span></c:otherwise>
                                </c:choose>
                            </div>
                        </section>

                        <div class="detail-columns">
                            <%-- Submission --%>
                            <section class="panel">
                                <div class="panel-head">
                                    <div>
                                        <p class="eyebrow">SUBMISSION</p>
                                        <h2>Submission information</h2>
                                    </div>
                                </div>
                                <dl class="detail-list">
                                    <dt>Work</dt>
                                    <dd>
                                        <a class="text-link" href="${ctx}/submission/file?id=${s.submissionId}" target="_blank" rel="noopener"><c:out value="${s.fileName}"/></a>
                                        <span class="table-sub">${s.uploaded ? 'File upload' : 'Link'}</span>
                                    </dd>
                                    <dt>Submitted by</dt>
                                    <dd><c:out value="${s.studentName}"/> <span class="table-sub"><c:out value="${s.studentUsername}"/></span></dd>
                                    <dt>Group</dt>
                                    <dd>Group #${s.groupId}<span class="table-sub"><c:out value="${empty s.groupMembers ? '—' : s.groupMembers}"/></span></dd>
                                    <dt>Submitted at</dt>
                                    <dd>
                                        <c:out value="${s.submitTimeText}"/>
                                        <c:if test="${s.late}"><span class="badge bad">Late</span></c:if>
                                    </dd>
                                    <dt>Status</dt>
                                    <dd><c:out value="${s.status}"/></dd>
                                </dl>
                            </section>

                            <%-- Assignment --%>
                            <section class="panel">
                                <div class="panel-head">
                                    <div>
                                        <p class="eyebrow">ASSIGNMENT</p>
                                        <h2>Assignment information</h2>
                                    </div>
                                    <a class="text-link" href="${ctx}/assignment?id=${s.assignmentId}">Xem assignment</a>
                                </div>
                                <dl class="detail-list">
                                    <dt>Class</dt>
                                    <dd><c:out value="${s.classCode}"/> · <c:out value="${s.semester}"/> <c:out value="${s.year}"/></dd>
                                    <dt>Subject</dt>
                                    <dd><c:out value="${empty s.subjectName ? '—' : s.subjectName}"/></dd>
                                    <dt>Due date</dt>
                                    <dd><c:out value="${s.dueDateText}"/></dd>
                                    <dt>Status</dt>
                                    <dd><span class="status-pill status-${fn:toLowerCase(s.assignmentStatus)}"><c:out value="${s.assignmentStatus}"/></span></dd>
                                </dl>
                            </section>
                        </div>

                        <%-- Evaluation --%>
                        <section class="panel" id="evaluation">
                            <div class="panel-head">
                                <div>
                                    <p class="eyebrow">EVALUATION</p>
                                    <h2>Score and feedback</h2>
                                </div>
                            </div>
                            <c:if test="${not empty gradeError}">
                                <div class="alert" role="alert"><c:out value="${gradeError}"/></div>
                            </c:if>
                            <c:choose>
                                <c:when test="${canGrade}">
                                    <form class="assignment-form" method="post" action="${ctx}/submissions/detail" data-submit-once>
                                        <input type="hidden" name="id" value="${s.submissionId}">
                                        <div class="field-row">
                                            <div>
                                                <label for="score">Score (0 – 10) <span class="required">*</span></label>
                                                <input class="control" id="score" name="score" type="number" min="0" max="10" step="0.01" required
                                                       value="<c:out value='${not empty gradeError ? formScore : s.score}'/>">
                                            </div>
                                            <div>
                                                <label for="gradedBy">Graded by</label>
                                                <c:set var="gradedBy" value="Not graded"/>
                                                <c:if test="${s.graded}">
                                                    <c:set var="gradedBy" value="${empty s.graderName ? '—' : s.graderName} · ${s.evaluatedText}"/>
                                                </c:if>
                                                <input class="control" id="gradedBy" type="text" readonly value="<c:out value='${gradedBy}'/>">
                                            </div>
                                        </div>
                                        <div>
                                            <label for="feedback">Feedback <span class="optional">At most 2000 characters</span></label>
                                            <textarea class="control" id="feedback" name="feedback" rows="5" maxlength="2000"><c:out value="${not empty gradeError ? formFeedback : s.feedback}"/></textarea>
                                        </div>
                                        <button class="button button-primary" type="submit">${s.graded ? 'Update score' : 'Save score'}</button>
                                    </form>
                                </c:when>
                                <c:when test="${s.graded}">
                                    <dl class="detail-list">
                                        <dt>Score</dt>
                                        <dd class="score"><c:out value="${s.scoreText}"/> / 10</dd>
                                        <dt>Feedback</dt>
                                        <dd class="pre-line"><c:out value="${empty s.feedback ? '—' : s.feedback}"/></dd>
                                        <dt>Graded by</dt>
                                        <dd><c:out value="${s.graderName}"/> · <c:out value="${s.evaluatedText}"/></dd>
                                    </dl>
                                </c:when>
                                <c:otherwise>
                                    <p class="empty">This submission has not been graded yet.</p>
                                </c:otherwise>
                            </c:choose>
                        </section>
                    </c:otherwise>
                </c:choose>
            </main>
        </div>

        <script src="${ctx}/assets/js/theme.js" defer></script>
        <script src="${ctx}/assets/js/submit-once.js" defer></script>
    </body>
</html>

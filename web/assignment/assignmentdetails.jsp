<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/assignment"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css">
        <title>Assignment Details · AMS</title>
    </head>
    <body class="app">

        <%-- Shared header --%>
        <jsp:include page="/common/header.jsp" />

        <div class="app-layout">
            <jsp:include page="/common/teachersidebar.jsp">
                <jsp:param name="active" value="assignments"/>
            </jsp:include>

            <main class="main-content">
                <a class="back-link" href="${ctx}/assignment">&#8592; Back to list</a>

                <c:choose>
                    <%-- Invalid id, missing assignment or another teacher's assignment --%>
                    <c:when test="${not empty accessError}">
                        <section class="hello">
                            <p class="eyebrow">ACCESS DENIED</p>
                            <h1>Access not allowed</h1>
                        </section>
                        <div class="alert" role="alert"><c:out value="${accessError}"/></div>
                    </c:when>

                    <c:otherwise>
                        <c:if test="${updated}">
                            <div class="notice" role="status">Assignment has been updated.</div>
                        </c:if>

                        <section class="hello page-title-row">
                            <div>
                                <p class="eyebrow">ASSIGNMENT DETAILS · #<c:out value="${assignment.assignmentId}"/></p>
                                <h1><c:out value="${assignment.title}"/></h1>
                            </div>
                            <div class="title-actions">
                                <span class="status-pill status-${fn:toLowerCase(assignment.status)}"><c:out value="${assignment.status}"/></span>
                                <a class="button button-primary" href="${ctx}/assignment?action=edit&amp;id=${assignment.assignmentId}">Edit assignment</a>
                            </div>
                        </section>

                        <%-- Assignment information --%>
                        <section class="panel">
                            <dl class="detail-list">
                                <dt>Class</dt>
                                <dd><c:out value="${assignment.classCode}"/> · <c:out value="${assignment.semester}"/> <c:out value="${assignment.year}"/></dd>
                                <dt>Subject</dt>
                                <dd><c:out value="${assignment.subjectName}"/></dd>
                                <dt>Start date</dt>
                                <dd><c:out value="${assignment.startDateText}"/></dd>
                                <dt>Due date</dt>
                                <dd><c:out value="${assignment.dueDateText}"/></dd>
                                <dt>Instruction</dt>
                                <dd class="pre-line"><c:out value="${assignment.instruction}"/></dd>
                            </dl>
                        </section>

                        <%-- Submissions: one row per group of the class --%>
                        <section class="panel" id="submissions">
                            <div class="panel-head">
                                <div>
                                    <p class="eyebrow">SUBMISSIONS</p>
                                    <h2>Student Submissions</h2>
                                </div>
                                <c:if test="${not empty submissions}">
                                    <span class="result-count">${submittedCount}/${fn:length(submissions)} groups submitted · ${gradedCount} graded</span>
                                </c:if>
                            </div>
                            <c:if test="${graded}">
                                <div class="notice" role="status">Score saved.</div>
                            </c:if>
                            <c:if test="${not empty gradeError}">
                                <div class="alert" role="alert"><c:out value="${gradeError}"/></div>
                            </c:if>
                            <c:choose>
                                <c:when test="${not empty submissionError}">
                                    <div class="alert" role="alert"><c:out value="${submissionError}"/></div>
                                </c:when>
                                <c:when test="${empty submissions}">
                                    <p class="empty">This class has no groups yet.</p>
                                </c:when>
                                <c:otherwise>
                                    <div class="table-wrap">
                                        <table class="assignment-table submission-table">
                                            <thead>
                                                <tr>
                                                    <th scope="col">Group</th>
                                                    <th scope="col">Submission</th>
                                                    <th scope="col">Submitted at</th>
                                                    <th scope="col">Grading</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="s" items="${submissions}">
                                                    <tr>
                                                        <td class="assignment-name">
                                                            <strong>Group #${s.groupId}</strong>
                                                            <span class="table-sub"><c:out value="${empty s.groupMembers ? 'No members yet' : s.groupMembers}"/></span>
                                                        </td>
                                                        <c:choose>
                                                            <c:when test="${not s.submitted}">
                                                                <td><span class="badge bad">Not submitted</span></td>
                                                                <td>-</td>
                                                                <td>-</td>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <td>
                                                                    <a class="text-link" href="${ctx}/submission/file?id=${s.submissionId}" target="_blank" rel="noopener"><c:out value="${s.fileName}"/></a>
                                                                    <span class="table-sub">by <c:out value="${s.studentName}"/> · <a href="${ctx}/submissions/detail?id=${s.submissionId}">Details</a></span>
                                                                </td>
                                                                <td>
                                                                    <c:out value="${s.submitTimeText}"/>
                                                                    <c:if test="${s.late}"><span class="badge bad">Late</span></c:if>
                                                                </td>
                                                                <td>
                                                                    <c:set var="isErrorRow" value="${gradeSubmissionId == s.submissionId}"/>
                                                                    <form class="grade-form" method="post" action="${ctx}/assignment" data-submit-once>
                                                                        <input type="hidden" name="action" value="grade">
                                                                        <input type="hidden" name="id" value="${assignment.assignmentId}">
                                                                        <input type="hidden" name="submissionId" value="${s.submissionId}">
                                                                        <input class="control score-input" name="score" type="number" min="0" max="10" step="0.01" required
                                                                               aria-label="Score (0-10)" placeholder="0-10"
                                                                               value="<c:out value='${isErrorRow ? formScore : s.score}'/>">
                                                                        <textarea class="control" name="feedback" rows="2" maxlength="2000"
                                                                                  aria-label="Feedback" placeholder="Feedback"><c:out value="${isErrorRow ? formFeedback : s.feedback}"/></textarea>
                                                                        <button class="button ${s.graded ? 'button-quiet' : 'button-primary'}" type="submit">${s.graded ? 'Update' : 'Grade'}</button>
                                                                    </form>
                                                                    <c:if test="${s.graded}">
                                                                        <span class="table-sub">Graded <c:out value="${s.evaluatedText}"/></span>
                                                                    </c:if>
                                                                </td>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
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

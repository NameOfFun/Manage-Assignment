
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/classes"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="curPrefix" value="${fn:toUpperCase(fn:substring(cls.semester, 0, 2))}"/>
<c:set var="yy" value="${cls.year % 100 < 10 ? '0' : ''}${cls.year % 100}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${isNew ? 'New class' : 'Class Details'} · AMS</title>
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css?v=20261003">
        <link rel="stylesheet" href="${ctx}/assets/css/manage.css?v=20261003">
    </head>
    <body class="app">
        <header class="topbar">
            <div class="topbar-in">
                <a class="brand" href="${ctx}/dashboard/${fn:toLowerCase(meRole)}dashboard.jsp" title="Back to your dashboard" style="color:inherit;text-decoration:none">
                    <div class="mark" aria-hidden="true">A</div>
                    <span>AMS</span>
                </a>
                <div class="topbar-right">
                    <button class="tog" id="tog" type="button" aria-label="Switch colour theme">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4.2"/><path d="M12 2v2.4M12 19.6V22M4.2 4.2l1.7 1.7M18.1 18.1l1.7 1.7M2 12h2.4M19.6 12H22M4.2 19.8l1.7-1.7M18.1 5.9l1.7-1.7"/></svg>
                    </button>
                    <c:set var="meName" value="${empty me.fullName ? me.username : me.fullName}"/>
                    <a class="me" href="${pageContext.request.contextPath}/profile" title="View your profile">
                        <span class="avatar" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(meName, 0, 1))}"/></span>
                        <span class="me-text">
                            <strong><c:out value="${meName}"/></strong>
                            <small><c:out value="${meRole}"/></small>
                        </span>
                    </a>
                </div>
            </div>
        </header>

        <main class="page">
            <div class="page-head">
                <div>
                    <a class="back" href="${ctx}/classes">&larr; Class List</a>
                    <h1>
                        <c:choose>
                            <c:when test="${isNew}">New class</c:when>
                            <c:otherwise>Class Details · <c:out value="${cls.classCode}"/></c:otherwise>
                        </c:choose>
                    </h1>
                </div>
            </div>

            <c:if test="${not empty flash and not flashOk}">
                <div class="alert" role="alert"><c:out value="${flash}"/></div>
            </c:if>
            <c:if test="${not empty formError}">
                <div class="alert" role="alert"><c:out value="${formError}"/></div>
            </c:if>

            <div class="stack">
                <%-- ============ Class information ============ --%>
                <section class="card card-pad" aria-labelledby="h-info">
                    <div class="section-head">
                        <h2 id="h-info">${canEdit ? 'Class configuration' : 'Class overview'}</h2>
                        <span class="note">
                            <c:choose>
                                <c:when test="${meRole == 'Expert'}">Subjects you manage</c:when>
                                <c:when test="${canEdit}">Admin view · all subjects</c:when>
                                <c:otherwise>Your assigned class</c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <c:choose>
                        <c:when test="${canEdit}">
                            <form method="post" action="${ctx}/classes/detail" novalidate>
                                <input type="hidden" name="action" value="${isNew ? 'create' : 'update'}">
                                <c:if test="${not isNew}">
                                    <input type="hidden" name="id" value="${cls.id}">
                                </c:if>

                                <div class="form-grid">
                                    <div class="w3">
                                        <label for="classCode">Class name (code)</label>
                                        <input class="ctl${not empty errors.classCode ? ' invalid' : ''}" id="classCode" name="classCode"
                                               type="text" maxlength="10" required autocomplete="off"
                                               value="<c:out value='${cls.classCode}'/>" placeholder="e.g. SE2051-NET"
                                               ${not empty errors.classCode ? 'aria-invalid="true" aria-describedby="e-code"' : ''}>
                                        <c:choose>
                                            <c:when test="${not empty errors.classCode}"><p class="err" id="e-code"><c:out value="${errors.classCode}"/></p></c:when>
                                            <c:otherwise><p class="hint">Up to 10 characters, must be unique.</p></c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="w3">
                                        <label for="subjectId">Subject</label>
                                        <select class="ctl${not empty errors.subjectId ? ' invalid' : ''}" id="subjectId" name="subjectId" required>
                                            <option value="">Choose a subject</option>
                                            <c:forEach var="s" items="${subjects}">
                                                <option value="${s.id}" ${s.id == cls.subjectId ? 'selected' : ''}>
                                                    <c:out value="${empty s.value ? s.name : s.value}"/><c:if test="${not empty s.value and s.name != s.value}"> — <c:out value="${s.name}"/></c:if>
                                                </option>
                                            </c:forEach>
                                        </select>
                                        <c:if test="${not empty errors.subjectId}"><p class="err"><c:out value="${errors.subjectId}"/></p></c:if>
                                        <c:if test="${empty subjects}"><p class="hint">You are not assigned to manage any subject yet.</p></c:if>
                                    </div>

                                    <div class="w2">
                                        <label for="year">Year</label>
                                        <select class="ctl${not empty errors.year ? ' invalid' : ''}" id="year" name="year">
                                            <c:forEach var="y" items="${years}">
                                                <option value="${y}" ${y == cls.year ? 'selected' : ''}>${y}</option>
                                            </c:forEach>
                                        </select>
                                        <c:if test="${not empty errors.year}"><p class="err"><c:out value="${errors.year}"/></p></c:if>
                                    </div>

                                    <div class="w2">
                                        <label for="semester">Semester</label>
                                        <select class="ctl${not empty errors.semester ? ' invalid' : ''}" id="semester" name="semester">
                                            <c:if test="${isNew and empty cls.semester}">
                                                <option value="">Choose</option>
                                            </c:if>
                                            <c:forEach var="sn" items="${semesterNames}">
                                                <c:set var="pre" value="${fn:toUpperCase(fn:substring(sn, 0, 2))}"/>
                                                <option value="${sn}" data-prefix="${pre}" ${pre == curPrefix ? 'selected' : ''}>${pre}${yy} · ${sn}</option>
                                            </c:forEach>
                                        </select>
                                        <c:if test="${not empty errors.semester}"><p class="err"><c:out value="${errors.semester}"/></p></c:if>
                                    </div>

                                    <div class="w2">
                                        <label for="teacherId">Teacher</label>
                                        <select class="ctl${not empty errors.teacherId ? ' invalid' : ''}" id="teacherId" name="teacherId" required>
                                            <option value="">Choose a teacher</option>
                                            <c:set var="teacherListed" value="${false}"/>
                                            <c:forEach var="t" items="${teachers}">
                                                <c:if test="${t.id == cls.teacherId}"><c:set var="teacherListed" value="${true}"/></c:if>
                                                <option value="${t.id}" ${t.id == cls.teacherId ? 'selected' : ''}>
                                                    <c:out value="${empty t.fullName ? t.username : t.fullName}"/> (<c:out value="${t.username}"/>)
                                                </option>
                                            </c:forEach>
                                            <%-- Current teacher no longer has the Teacher role: keep them selectable --%>
                                            <c:if test="${not teacherListed and cls.teacherId > 0 and not empty cls.teacherLabel}">
                                                <option value="${cls.teacherId}" selected><c:out value="${cls.teacherLabel}"/></option>
                                            </c:if>
                                        </select>
                                        <c:if test="${not empty errors.teacherId}"><p class="err"><c:out value="${errors.teacherId}"/></p></c:if>
                                        <c:if test="${empty teachers}"><p class="hint">No account has the Teacher role yet.</p></c:if>
                                    </div>
                                </div>

                                <c:if test="${not isNew and not empty cls.teacherLabel}">
                                    <div class="facts">
                                        <span><strong>${cls.studentCount}</strong> student${cls.studentCount == 1 ? '' : 's'}</span>
                                        <span><strong>${cls.groupCount}</strong> group${cls.groupCount == 1 ? '' : 's'}</span>
                                        <span><strong>${cls.assignmentCount}</strong> assignment${cls.assignmentCount == 1 ? '' : 's'}</span>
                                    </div>
                                </c:if>

                                <div class="form-actions">
                                    <button class="btn-sm" type="submit">${isNew ? 'Create class' : 'Submit'}</button>
                                    <a class="btn-sm ghost" href="${ctx}/classes">Cancel</a>
                                </div>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <dl class="kv">
                                <div><dt>Class Code</dt><dd class="strong"><c:out value="${cls.classCode}"/></dd></div>
                                <div><dt>Subject</dt><dd><c:out value="${empty cls.subjectLabel ? '—' : cls.subjectLabel}"/></dd></div>
                                <div><dt>Semester</dt><dd><span class="term-chip"><c:out value="${cls.semesterCode}"/></span> · <c:out value="${cls.semester}"/> ${cls.year}</dd></div>
                                <div><dt>Teacher</dt><dd><c:out value="${empty cls.teacherLabel ? '—' : cls.teacherLabel}"/></dd></div>
                            </dl>
                            <div class="facts">
                                <span><strong>${cls.studentCount}</strong> student${cls.studentCount == 1 ? '' : 's'}</span>
                                <span><strong>${cls.groupCount}</strong> group${cls.groupCount == 1 ? '' : 's'}</span>
                                <span><strong>${cls.assignmentCount}</strong> assignment${cls.assignmentCount == 1 ? '' : 's'}</span>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <%-- ============ Student roster (teacher of this class only) ============ --%>
                <c:if test="${canRoster}">
                    <section class="card card-pad roster-card" id="roster" aria-labelledby="h-roster">
                        <div class="section-head">
                            <h2 id="h-roster">Manage student roster</h2>
                            <span class="note">Only viewable to Teacher</span>
                        </div>

                        <div class="roster-tools">
                            <form class="add-form" method="post" action="${ctx}/classes/detail">
                                <input type="hidden" name="action" value="addStudent">
                                <input type="hidden" name="id" value="${cls.id}">
                                <label class="sr" for="student">Student username, email or full name</label>
                                <input class="ctl" id="student" name="student" type="text" maxlength="50" required
                                       value="<c:out value='${studentQuery}'/>"
                                       placeholder="Enter student username, email or name">
                                <button class="btn-sm" type="submit">Add to class</button>
                            </form>
                            <div class="links">

                                <a href="${ctx}/groups?classId=${cls.id}">Groups</a>
                                <a href="${ctx}/assignment?classId=${cls.id}">Assignments</a>
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${empty roster.items}">
                                <p class="empty-state">No students in this class yet. Add one above.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="data-wrap">
                                    <table class="data narrow">
                                        <thead>
                                            <tr>
                                                <th scope="col">Id</th>
                                                <th scope="col">Full Name</th>
                                                <th scope="col">Email</th>
                                                <th scope="col">Enrolled</th>
                                                <th scope="col">Action</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="st" items="${roster.items}">
                                                <tr>
                                                    <td class="id">${st.userId}</td>
                                                    <td class="strong"><a href="${ctx}/students/detail?id=${st.userId}"><c:out value="${st.displayName}"/></a></td>
                                                    <td class="${empty st.email ? 'muted' : ''}"><c:out value="${empty st.email ? '—' : st.email}"/></td>
                                                    <td class="muted">${st.enrollDateText}</td>
                                                    <td>
                                                        <form method="post" action="${ctx}/classes/detail"
                                                              data-confirm="Remove ${fn:escapeXml(st.displayName)} from ${fn:escapeXml(cls.classCode)}?">
                                                            <input type="hidden" name="action" value="removeStudent">
                                                            <input type="hidden" name="id" value="${cls.id}">
                                                            <input type="hidden" name="studentId" value="${st.userId}">
                                                            <input type="hidden" name="rpage" value="${roster.page}">
                                                            <button class="link-btn danger" type="submit">Remove</button>
                                                        </form>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <c:if test="${roster.totalItems > 0}">
                            <c:set var="rUrl" value="${ctx}/classes/detail?id=${cls.id}&rpage="/>
                            <div class="list-foot">
                                <span>${roster.totalItems} student${roster.totalItems == 1 ? '' : 's'}</span>
                                <c:if test="${roster.totalPages > 1}">
                                    <nav aria-label="Roster pages">
                                        <ul class="pager">
                                            <li>
                                                <c:choose>
                                                    <c:when test="${roster.hasPrev}"><a href="${rUrl}${roster.page - 1}#roster" aria-label="Previous page">&lsaquo;</a></c:when>
                                                    <c:otherwise><span class="off" aria-hidden="true">&lsaquo;</span></c:otherwise>
                                                </c:choose>
                                            </li>
                                            <c:forEach var="p" items="${roster.pageNumbers}">
                                                <li>
                                                    <c:choose>
                                                        <c:when test="${p == 0}"><span class="gap">…</span></c:when>
                                                        <c:when test="${p == roster.page}"><span class="cur" aria-current="page">${p}</span></c:when>
                                                        <c:otherwise><a href="${rUrl}${p}#roster">${p}</a></c:otherwise>
                                                    </c:choose>
                                                </li>
                                            </c:forEach>
                                            <li>
                                                <c:choose>
                                                    <c:when test="${roster.hasNext}"><a href="${rUrl}${roster.page + 1}#roster" aria-label="Next page">&rsaquo;</a></c:when>
                                                    <c:otherwise><span class="off" aria-hidden="true">&rsaquo;</span></c:otherwise>
                                                </c:choose>
                                            </li>
                                        </ul>
                                    </nav>
                                </c:if>
                            </div>
                        </c:if>
                    </section>
                </c:if>
            </div>
        </main>

        <%-- Success message: slides in at the bottom-right corner, then slides away --%>
        <c:if test="${not empty flash and flashOk}">
            <div class="toast" id="toast" role="status" aria-live="polite">
                <svg class="toast-ic" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="10"/><path d="M7.5 12.5l3 3 6-6.5"/></svg>
                <span class="toast-msg"><c:out value="${flash}"/></span>
                <button class="toast-x" type="button" aria-label="Close">&times;</button>
                <span class="toast-bar" aria-hidden="true"></span>
            </div>
        </c:if>

        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
        <script>
            // Keep the semester labels (SP26 / SU26 / FA26) in step with the chosen year.
            const year = document.getElementById('year'), sem = document.getElementById('semester');
            if (year && sem) {
                const relabel = () => {
                    const yy = String(year.value).slice(-2);
                    sem.querySelectorAll('option[data-prefix]').forEach(o => {
                        o.textContent = o.dataset.prefix + yy + ' · ' + o.value;
                    });
                };
                year.addEventListener('change', relabel);
            }

            document.querySelectorAll('form[data-confirm]').forEach(f => {
                f.addEventListener('submit', e => {
                    if (!confirm(f.dataset.confirm)) {
                        e.preventDefault();
                    }
                });
            });
            // Toast: slide in, stay 4 seconds (paused while the mouse is over it), slide out.
            const toast = document.getElementById('toast');
            if (toast) {
                let remaining = 4000, startedAt = 0, timer = 0;
                const hide = () => {
                    clearTimeout(timer);
                    toast.classList.remove('show');
                    setTimeout(() => toast.remove(), 400);
                };
                const run = () => {
                    startedAt = Date.now();
                    timer = setTimeout(hide, remaining);
                };
                requestAnimationFrame(() => requestAnimationFrame(() => {
                    toast.classList.add('show');
                    run();
                }));
                toast.addEventListener('mouseenter', () => {
                    clearTimeout(timer);
                    remaining -= Date.now() - startedAt;
                    toast.classList.add('paused');
                });
                toast.addEventListener('mouseleave', () => {
                    toast.classList.remove('paused');
                    run();
                });
                toast.querySelector('.toast-x').addEventListener('click', hide);
            }
        </script>
    </body>
</html>

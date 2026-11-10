<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%-- Opened directly: go through the servlet so the data is loaded. --%>
<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/classes"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="listUrl" value="${ctx}/classes?${filterQuery}${empty filterQuery ? '' : '&'}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Class List · AMS</title>
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
                    <h1>Class List</h1>
                    <p class="sub">
                        <c:choose>
                            <c:when test="${meRole == 'Expert'}">Classes of the subjects you manage.</c:when>
                            <c:when test="${canManage}">View, filter and manage all classes.</c:when>
                            <c:otherwise>Classes you teach.</c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <c:if test="${canManage}">
                    <a class="btn-sm" href="${ctx}/classes/detail">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>
                        New class
                    </a>
                </c:if>
            </div>

            <c:if test="${not empty flash and not flashOk}">
                <div class="alert" role="alert"><c:out value="${flash}"/></div>
            </c:if>

            <form class="filters" method="get" action="${ctx}/classes" role="search">
                <label class="sr" for="f-subject">Subject</label>
                <select class="ctl" id="f-subject" name="subject" onchange="this.form.submit()">
                    <option value="">${meRole == 'Admin' ? 'All Subjects' : 'All My Subjects'}</option>
                    <c:forEach var="s" items="${subjects}">
                        <option value="${s.id}" ${fSubject == s.id.toString() ? 'selected' : ''}>
                            <c:out value="${empty s.value ? s.name : s.value}"/>
                        </option>
                    </c:forEach>
                </select>
                <label class="sr" for="f-semester">Semester</label>
                <select class="ctl" id="f-semester" name="semester" onchange="this.form.submit()">
                    <option value="">All Semesters</option>
                    <c:forEach var="t" items="${semesters}">
                        <option value="${t.value}" ${fSemester == t.value ? 'selected' : ''}><c:out value="${t.label}"/></option>
                    </c:forEach>
                </select>
                <div class="kw">
                    <label class="sr" for="f-q">Search by class name</label>
                    <input class="ctl" id="f-q" type="search" name="q" maxlength="50"
                           value="<c:out value='${fKeyword}'/>" placeholder="Enter keyword(s) to search">
                </div>
                <div class="actions">
                    <button class="btn-sm" type="submit">Search</button>
                    <c:if test="${not empty filterQuery}">
                        <a class="btn-sm ghost" href="${ctx}/classes">Clear</a>
                    </c:if>
                </div>
            </form>

            <section class="card" aria-label="Classes">
                <c:choose>
                    <c:when test="${empty result.items}">
                        <p class="empty-state">
                            <c:choose>
                                <c:when test="${not empty filterQuery}">No class matches these filters.</c:when>
                                <c:when test="${canManage}">There are no classes yet. Use “New class” to add one.</c:when>
                                <c:otherwise>You are not teaching any class yet.</c:otherwise>
                            </c:choose>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <div class="data-wrap">
                            <table class="data">
                                <thead>
                                    <tr>
                                        <th scope="col">Id</th>
                                        <th scope="col">Name</th>
                                        <th scope="col">Subject</th>
                                        <c:if test="${canManage}">
                                            <th scope="col">Teacher</th>
                                        </c:if>
                                        <th scope="col">Semester</th>
                                        <th scope="col" class="num">Year</th>
                                        <th scope="col" class="num">Students</th>
                                        <c:if test="${not canManage}">
                                            <th scope="col" class="num">Groups</th>
                                            <th scope="col" class="num">Assignments</th>
                                        </c:if>
                                        <th scope="col">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="cl" items="${result.items}">
                                        <tr>
                                            <td class="id">${cl.id}</td>
                                            <td class="strong"><c:out value="${cl.classCode}"/></td>
                                            <td title="<c:out value='${cl.subjectName}'/>"><c:out value="${empty cl.subjectLabel ? '—' : cl.subjectLabel}"/></td>
                                            <c:if test="${canManage}">
                                                <td><c:out value="${empty cl.teacherLabel ? '—' : cl.teacherLabel}"/></td>
                                            </c:if>
                                            <td><span class="term-chip"><c:out value="${cl.semesterCode}"/></span></td>
                                            <td class="num">${cl.year}</td>
                                            <td class="num">${cl.studentCount}</td>
                                            <c:if test="${not canManage}">
                                                <td class="num">${cl.groupCount}</td>
                                                <td class="num">${cl.assignmentCount}</td>
                                            </c:if>
                                            <td>
                                                <div class="row-actions">
                                                    <c:choose>
                                                        <c:when test="${canManage}">
                                                            <a href="${ctx}/classes/detail?id=${cl.id}">Edit</a>
                                                            <c:set var="ask">Remove class ${fn:escapeXml(cl.classCode)}?<c:if test="${cl.studentCount > 0}"> Its ${cl.studentCount} student enrolment(s) will be removed too.</c:if></c:set>
                                                            <form method="post" action="${ctx}/classes" data-confirm="${ask}">
                                                                <input type="hidden" name="action" value="remove">
                                                                <input type="hidden" name="id" value="${cl.id}">
                                                                <input type="hidden" name="subject" value="<c:out value='${fSubject}'/>">
                                                                <input type="hidden" name="semester" value="<c:out value='${fSemester}'/>">
                                                                <input type="hidden" name="q" value="<c:out value='${fKeyword}'/>">
                                                                <input type="hidden" name="page" value="${result.page}">
                                                                <button class="link-btn danger" type="submit">Remove</button>
                                                            </form>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <a href="${ctx}/classes/detail?id=${cl.id}">View</a>
                                                            <a href="${ctx}/groups?classId=${cl.id}">Groups</a>
                                                            <a href="${ctx}/assignment?classId=${cl.id}">Assignments</a>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:if test="${result.totalItems > 0}">
                    <div class="list-foot">
                        <span>Showing ${result.from}–${result.to} of ${result.totalItems} class${result.totalItems == 1 ? '' : 'es'}</span>
                        <c:if test="${result.totalPages > 1}">
                            <nav aria-label="Pages">
                                <ul class="pager">
                                    <li>
                                        <c:choose>
                                            <c:when test="${result.hasPrev}"><a href="${listUrl}page=${result.page - 1}" aria-label="Previous page">&lsaquo;</a></c:when>
                                            <c:otherwise><span class="off" aria-hidden="true">&lsaquo;</span></c:otherwise>
                                        </c:choose>
                                    </li>
                                    <c:forEach var="p" items="${result.pageNumbers}">
                                        <li>
                                            <c:choose>
                                                <c:when test="${p == 0}"><span class="gap">…</span></c:when>
                                                <c:when test="${p == result.page}"><span class="cur" aria-current="page">${p}</span></c:when>
                                                <c:otherwise><a href="${listUrl}page=${p}">${p}</a></c:otherwise>
                                            </c:choose>
                                        </li>
                                    </c:forEach>
                                    <li>
                                        <c:choose>
                                            <c:when test="${result.hasNext}"><a href="${listUrl}page=${result.page + 1}" aria-label="Next page">&rsaquo;</a></c:when>
                                            <c:otherwise><span class="off" aria-hidden="true">&rsaquo;</span></c:otherwise>
                                        </c:choose>
                                    </li>
                                </ul>
                            </nav>
                        </c:if>
                    </div>
                </c:if>
            </section>
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
            // Ask before removing a class.
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

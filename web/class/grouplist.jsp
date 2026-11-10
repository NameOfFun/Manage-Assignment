
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/classes"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="listUrl" value="${ctx}/groups?classId=${cls.id}&${filterQuery}${empty filterQuery ? '' : '&'}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Group List · <c:out value="${cls.classCode}"/> · AMS</title>
        <link rel="stylesheet" href="${ctx}/assets/css/main.css">
        <link rel="stylesheet" href="${ctx}/assets/css/dashboard.css?v=20261003">
        <link rel="stylesheet" href="${ctx}/assets/css/manage.css?v=20261016">
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
                    <a class="me" href="${ctx}/profile" title="View your profile">
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
                    <a class="back" href="${ctx}/classes/detail?id=${cls.id}">&larr; Class Details · <c:out value="${cls.classCode}"/></a>
                    <h1>Group List</h1>
                    <p class="sub">
                        <c:out value="${cls.classCode}"/> · <c:out value="${cls.semesterCode}"/> ·
                        ${cls.studentCount} student${cls.studentCount == 1 ? '' : 's'},
                        <c:choose>
                            <c:when test="${unassignedCount == 0}">all in a group</c:when>
                            <c:otherwise><strong class="warn-txt">${unassignedCount} without a group</strong></c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <a class="btn-sm" href="${ctx}/groups/detail?classId=${cls.id}">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>
                    New group
                </a>
            </div>

            <c:if test="${not empty flash and not flashOk}">
                <div class="alert" role="alert"><c:out value="${flash}"/></div>
            </c:if>

            <div class="group-tools">
                <form class="tool-form" method="get" action="${ctx}/groups" role="search">
                    <input type="hidden" name="classId" value="${cls.id}">
                    <label class="sr" for="f-q">Search groups by student name</label>
                    <input class="ctl" id="f-q" type="search" name="q" maxlength="50"
                           value="<c:out value='${fKeyword}'/>" placeholder="Enter student to search">
                    <button class="btn-sm" type="submit">Search</button>
                    <c:if test="${not empty fKeyword}">
                        <a class="btn-sm ghost" href="${ctx}/groups?classId=${cls.id}">Clear</a>
                    </c:if>
                </form>

                <form class="tool-form rand-form" method="post" action="${ctx}/groups" id="rand-form"
                      data-groups="${result.totalItems}" data-students="${cls.studentCount}">
                    <input type="hidden" name="action" value="randomize">
                    <input type="hidden" name="classId" value="${cls.id}">
                    <label class="sr" for="r-count">Number of groups</label>
                    <input class="ctl num-ctl" id="r-count" name="count" type="number" inputmode="numeric"
                           min="1" max="${cls.studentCount > 0 ? cls.studentCount : 1}" required
                           placeholder="Number of groups" ${cls.studentCount == 0 ? 'disabled' : ''}>
                    <button class="btn-sm ghost" type="submit" ${cls.studentCount == 0 ? 'disabled' : ''}
                            title="Delete every group and split all students of the class at random">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M16 3h5v5M4 20 21 3M21 16v5h-5M15 15l6 6M4 4l5 5"/></svg>
                        Randomize group
                    </button>
                </form>
            </div>

            <section class="card" aria-label="Groups">
                <c:choose>
                    <c:when test="${empty result.items}">
                        <p class="empty-state">
                            <c:choose>
                                <c:when test="${not empty fKeyword}">No group has a student matching “<c:out value="${fKeyword}"/>”.</c:when>
                                <c:when test="${cls.studentCount == 0}">This class has no students yet. Add students in Class Details first.</c:when>
                                <c:otherwise>No groups yet. Use “New group”, or enter a number and press “Randomize group”.</c:otherwise>
                            </c:choose>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <div class="data-wrap">
                            <table class="data">
                                <thead>
                                    <tr>
                                        <th scope="col">Group Id</th>
                                        <th scope="col">Students</th>
                                        <th scope="col" class="num">Members</th>
                                        <th scope="col">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="g" items="${result.items}">
                                        <tr>
                                            <td class="id">${g.id}</td>
                                            <td class="names-cell">
                                                <c:choose>
                                                    <c:when test="${g.memberCount == 0}"><span class="muted">No students yet</span></c:when>
                                                    <c:otherwise>
                                                        <c:forEach var="n" items="${g.memberNames}" end="3" varStatus="s"><c:if test="${not s.first}">, </c:if><c:out value="${n}"/></c:forEach>
                                                        <c:if test="${g.memberCount > 4}"><span class="more">+${g.memberCount - 4} more</span></c:if>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="num">${g.memberCount}</td>
                                            <td>
                                                <div class="row-actions">
                                                    <a href="${ctx}/groups/detail?id=${g.id}">Edit</a>
                                                    <c:set var="ask">Remove group ${g.id}?<c:if test="${g.memberCount > 0}"> Its ${g.memberCount} student(s) will have no group.</c:if></c:set>
                                                    <form method="post" action="${ctx}/groups" data-confirm="${ask}">
                                                        <input type="hidden" name="action" value="remove">
                                                        <input type="hidden" name="id" value="${g.id}">
                                                        <input type="hidden" name="classId" value="${cls.id}">
                                                        <input type="hidden" name="q" value="<c:out value='${fKeyword}'/>">
                                                        <input type="hidden" name="page" value="${result.page}">
                                                        <button class="link-btn danger" type="submit">Remove</button>
                                                    </form>
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
                        <span>Showing ${result.from}–${result.to} of ${result.totalItems} group${result.totalItems == 1 ? '' : 's'}</span>
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
            // Ask before removing a group.
            document.querySelectorAll('form[data-confirm]').forEach(f => {
                f.addEventListener('submit', e => {
                    if (!confirm(f.dataset.confirm)) {
                        e.preventDefault();
                    }
                });
            });

            // Randomize replaces every group of the class, so say exactly what will happen first.
            const rand = document.getElementById('rand-form');
            rand.addEventListener('submit', e => {
                const n = Number(rand.elements.count.value);
                const groups = Number(rand.dataset.groups), students = Number(rand.dataset.students);
                let msg = 'Split all ' + students + ' student(s) at random into ' + n + ' group(s)?';
                if (groups > 0) {
                    msg += '\nThe current ' + groups + ' group(s) will be deleted.';
                }
                if (!confirm(msg)) {
                    e.preventDefault();
                }
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

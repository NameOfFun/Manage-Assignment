<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- Teacher sidebar. Usage: <jsp:include page="/common/teachersidebar.jsp"><jsp:param name="active" value="dashboard|classes|assignments|submissions"/></jsp:include> --%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="sidebar" aria-label="Teacher navigation">
    <span class="sidebar-label">Teaching</span>

    <a href="${ctx}/teacherdashboard" class="${param.active == 'dashboard' ? 'active' : ''}">
        <svg viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
        Dashboard
    </a>

    <a href="${ctx}/classes" class="${param.active == 'classes' ? 'active' : ''}">
        <svg viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
        Classes
    </a>

    <a href="${ctx}/assignment" class="${param.active == 'assignments' ? 'active' : ''}">
        <svg viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="8" y1="13" x2="16" y2="13"/><line x1="8" y1="17" x2="13" y2="17"/></svg>
        Assignments
    </a>

    <a href="${ctx}/submissions" class="${param.active == 'submissions' ? 'active' : ''}">
        <svg viewBox="0 0 24 24"><path d="M22 12h-6l-2 3h-4l-2-3H2"/><path d="M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"/></svg>
        Submissions
    </a>

    <div class="sidebar-divider"></div>

    <form method="post" action="${ctx}/auth" style="margin:0;">
        <input type="hidden" name="action" value="logout" />
        <button type="submit" class="sidebar-logout-btn">
            <svg viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            Logout
        </button>
    </form>
</nav>

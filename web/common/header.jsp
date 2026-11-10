<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<header class="topbar">
    <div class="topbar-in">
        <div class="brand">
            <div class="mark" aria-hidden="true">A</div>
            AMS
        </div>
        <div class="topbar-right">
            <button class="tog" id="tog" type="button" aria-label="Switch colour theme">
                <svg class="sun" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4.2"/><path d="M12 2v2.4M12 19.6V22M4.2 4.2l1.7 1.7M18.1 18.1l1.7 1.7M2 12h2.4M19.6 12H22M4.2 19.8l1.7-1.7M18.1 5.9l1.7-1.7"/></svg>
            </button>
            <c:set var="meName" value="${empty sessionScope.user.fullName ? sessionScope.user.username : sessionScope.user.fullName}"/>
            <a class="me" href="${pageContext.request.contextPath}/profile" title="View your profile">
                <img class="profile-avatar" src="${not empty user.imageUrl ? user.imageUrl : "https://res.cloudinary.com/b429n8kl/image/upload/v1792319974/default-avatar.png"}" 
                                     alt="${not empty user.fullName ? user.fullName : user.username}'s Avatar" 
                                     style="width: 32px; height: 32px; border-radius: 50%; object-fit: cover; border: 2px solid rgba(255, 255, 255, 0.1);" />
                <span class="me-text">
                    <strong><c:out value="${meName}"/></strong>
                    <small><c:out value="${sessionScope.user.username}"/></small>
                </span>
            </a>
        </div>
    </div>
</header>
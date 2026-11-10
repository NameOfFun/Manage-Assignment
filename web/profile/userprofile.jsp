<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css?v=20261003" />
        <title>User Profile</title>
    </head>
    <body class="app">

        <!-- Profile header -->
        <%@ include file="/common/header.jsp" %>

        <main class="page">
            <div class="hello">
                <h1>User Profile</h1>
                <p class="sub">Manage your personal information and account preferences.</p>
            </div>

            <c:if test="${not empty error}">
                <div class="error-msg" 
                     style="color: #d93025; background-color: rgba(217, 48, 37, 0.1); border: 1px solid #d93025; padding: 0.75rem 1rem;
                     border-radius: 6px; margin-bottom: 1rem; font-size: 0.875rem; text-align: center;">
                    ${error}
                </div>
                <c:remove var="error" scope="session" />
            </c:if>

            <c:if test="${not empty success}">
                <div class="error-msg" 
                     style="color: #25d957; background-color: rgba(37, 217, 48, 0.1); border: 1px solid #25d957; padding: 0.75rem 1rem;
                     border-radius: 6px; margin-bottom: 1rem; font-size: 0.875rem; text-align: center;">
                    ${success}
                </div>
                <c:remove var="success" scope="session" />
            </c:if>

            <!-- Profile Overview Header Card -->
            <div class="panel">
                <div class="panel-head">
                    <h2>Account Information</h2>
                </div>
                <form id="profile-form" method="post" action="${pageContext.request.contextPath}/upload" 
                      enctype="multipart/form-data">
                    <ul class="due-list">
                        <li class="due-item" style="align-items: center;">
                            <div class="due-main">
                                <label class="due-title">Profile Picture</label>
                                <span class="due-sub">Your avatar displayed across the application</span>
                            </div>
                            <div class="due-avatar-wrapper" style="display: flex; align-items: center; gap: 1rem;">
                                <img id="avatarPreview" class="profile-avatar" 
                                     src="${not empty user.imageUrl ? user.imageUrl : "https://res.cloudinary.com/b429n8kl/image/upload/v1792319974/default-avatar.png"}" 
                                     alt="${not empty user.fullName ? user.fullName : user.username}'s Avatar" 
                                     style="width: 128px; height: 128px; border-radius: 50%; object-fit: cover; border: 2px solid rgba(255, 255, 255, 0.1);" />
                            </div>
                            <input id="avatarInput" class="btn" type="file" name="image" accept="image/*">
                        </li>
                        <button class="btn" type="submit">Save user profile</button>
                </form>

                <form id="profile-form" method="post" action="${pageContext.request.contextPath}/profile?action=update">
                    <ul class="due-list">
                        <li class="due-item">
                            <div class="due-main">
                                <label for="duser" class="due-title">Username</label>
                                <span class="due-sub">Unique identification name for your account</span>
                            </div>
                            <input class="field" id="duser" name="username" type="text" 
                                   value="${user.username}" placeholder="Username">
                        </li>

                        <li class="due-item">
                            <div class="due-main">
                                <label for="duser" class="due-title">Role</label>
                                <span class="due-sub">The assigned role to your account</span>
                            </div>
                            <input class="field" id="drole" name="role" type="text" 
                                   value="${sessionScope.roleName}" placeholder="Role" readonly>
                        </li>

                        <li class="due-item">
                            <div class="due-main">
                                <label for="demail" class="due-title">Email Address</label>
                                <span class="due-sub">Primary contact email address</span>
                            </div>
                            <input class="field" id="demail" name="email" type="text"
                                   value="${user.email}" placeholder="name@example.com" readonly>
                        </li>

                        <li class="due-item">
                            <div class="due-main">
                                <label for="dfullname" class="due-title">Full Name</label>
                                <span class="due-sub">Your display name across the system</span>
                            </div>
                            <input class="field" id="dfullname" name="fullName" type="text" autocomplete="name" 
                                   value="${user.fullName}" placeholder="Full Name">
                        </li>
                    </ul>

                    <div style="margin-top: 1.5rem; display: flex; justify-content: flex-end;">
                        <button class="btn" type="submit">Save account changes</button>
                    </div>
                </form>
            </div>

            <!-- Navigation Back Link -->
            <div class="panel">
                <ul class="due-list">
                    <li class="due-item">
                        <div class="due-side">
                            <span class="due-title">
                                <a href="${pageContext.request.contextPath}/auth">&larr; Return to Dashboard</a>
                                <br>
                                <a href="${pageContext.request.contextPath}/profile/changepassword.jsp">&larr; Go to Change Password</a>
                            </span>
                            <span class="due-title">
                                <form action="${pageContext.request.contextPath}/auth?action=logout" method="post" style="display: inline;">
                                    <button class="btn logout-btn" type="submit">Logout</button>
                                </form>
                            </span>
                        </div>

                    </li>
                </ul>
            </div>
        </main>
        
        <script src="${pageContext.request.contextPath}/assets/js/imagepreview.js" defer></script>
        <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
    </body>
</html>
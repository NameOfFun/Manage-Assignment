
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:if test="${empty requestScope.fromServlet}">
    <c:redirect url="/classes"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${isNew ? 'New group' : 'Group Details'} · <c:out value="${cls.classCode}"/> · AMS</title>
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
                    <a class="back" href="${ctx}/groups?classId=${cls.id}">&larr; Group List · <c:out value="${cls.classCode}"/></a>
                    <h1>${isNew ? 'New group' : 'Group Details'}</h1>
                    <p class="sub">
                        <c:choose>
                            <c:when test="${isNew}">The group is created when you press Submit.</c:when>
                            <c:otherwise>Group ID: <strong>${groupId}</strong> · <c:out value="${cls.classCode}"/> · <c:out value="${cls.semesterCode}"/></c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </div>

            <c:if test="${not empty flash and not flashOk}">
                <div class="alert" role="alert"><c:out value="${flash}"/></div>
            </c:if>
            <c:if test="${not empty formError}">
                <div class="alert" role="alert"><c:out value="${formError}"/></div>
            </c:if>

            <section class="card card-pad roster-card" aria-labelledby="h-members">
                <div class="section-head">
                    <h2 id="h-members">Members <span class="count-chip" id="m-count">${fn:length(members)}</span></h2>
                    <span class="dirty-pill" id="dirty" hidden>Unsaved changes</span>
                </div>

                <div class="roster-tools">
                    <div class="add-form" id="add-box">
                        <label class="sr" for="who">Student name, username or email</label>
                        <input class="ctl" id="who" type="text" list="cands" maxlength="50" autocomplete="off"
                               placeholder="Enter student name to add" ${empty candidates and empty members ? 'disabled' : ''}>
                        <button class="btn-sm" type="button" id="add-btn">Add to group</button>
                    </div>
                    <span class="note" id="cand-note">${fn:length(candidates)} student${fn:length(candidates) == 1 ? '' : 's'} in this class without a group</span>
                </div>
                <p class="err add-msg" id="add-msg" role="alert" hidden></p>

                <datalist id="cands">
                    <c:forEach var="s" items="${candidates}">
                        <option value="<c:out value='${s.displayName}'/> (<c:out value='${s.username}'/>)"
                                data-id="${s.userId}" data-name="<c:out value='${s.displayName}'/>"
                                data-user="<c:out value='${s.username}'/>" data-email="<c:out value='${s.email}'/>"></option>
                    </c:forEach>
                </datalist>

                <div class="data-wrap">
                    <table class="data narrow" id="m-table">
                        <thead>
                            <tr>
                                <th scope="col">Id</th>
                                <th scope="col">Full Name</th>
                                <th scope="col">Email</th>
                                <th scope="col">Action</th>
                            </tr>
                        </thead>
                        <tbody id="m-body">
                            <c:forEach var="s" items="${members}">
                                <tr data-id="${s.userId}" data-name="<c:out value='${s.displayName}'/>"
                                    data-user="<c:out value='${s.username}'/>" data-email="<c:out value='${s.email}'/>">
                                    <td class="id">${s.userId}</td>
                                    <td class="strong"><c:out value="${s.displayName}"/></td>
                                    <td class="${empty s.email ? 'muted' : ''}"><c:out value="${empty s.email ? '—' : s.email}"/></td>
                                    <td><button class="link-btn danger" type="button" data-remove>Remove</button></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <p class="empty-state" id="m-empty" ${empty members ? '' : 'hidden'}>
                        No students in this group yet. Add one above.
                    </p>
                </div>

                <form method="post" action="${ctx}/groups/detail" id="save-form">
                    <input type="hidden" name="action" value="save">
                    <input type="hidden" name="classId" value="${cls.id}">
                    <c:if test="${not isNew}">
                        <input type="hidden" name="id" value="${groupId}">
                    </c:if>
                    <div id="ids"></div>
                    <div class="form-actions">
                        <button class="btn-sm" type="submit" id="submit" disabled>Submit</button>
                        <a class="btn-sm ghost" href="${ctx}/groups?classId=${cls.id}">Cancel</a>
                    </div>
                </form>
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
            // ---- Members: Add / Remove change only this table; Submit sends the final list. ----
            const isNew = ${isNew ? 'true' : 'false'};
            const body = document.getElementById('m-body'), empty = document.getElementById('m-empty');
            const who = document.getElementById('who'), addBtn = document.getElementById('add-btn');
            const msg = document.getElementById('add-msg'), cands = document.getElementById('cands');
            const count = document.getElementById('m-count'), candNote = document.getElementById('cand-note');
            const dirtyPill = document.getElementById('dirty'), submit = document.getElementById('submit');
            const ids = () => [...body.rows].map(r => r.dataset.id);
            const initial = ids().sort().join(',');
            let saving = false;

            const say = text => {
                msg.textContent = text;
                msg.hidden = !text;
            };
            const refresh = () => {
                const now = ids();
                const dirty = now.slice().sort().join(',') !== initial;
                count.textContent = now.length;
                empty.hidden = now.length > 0;
                document.getElementById('m-table').hidden = now.length === 0;
                dirtyPill.hidden = !dirty;
                // A new group needs at least one student; an existing one only needs a change.
                submit.disabled = !dirty || (isNew && now.length === 0);
                const left = cands.options.length;
                candNote.textContent = left + ' student' + (left === 1 ? '' : 's') + ' in this class without a group';
                who.disabled = left === 0;
                addBtn.disabled = left === 0;
                who.placeholder = left === 0 ? 'Every student of this class is in a group' : 'Enter student name to add';
            };

            const cell = (text, cls) => {
                const td = document.createElement('td');
                if (cls) td.className = cls;
                td.textContent = text;
                return td;
            };
            const addRow = s => {
                const tr = document.createElement('tr');
                tr.className = 'staged';
                Object.assign(tr.dataset, {id: s.id, name: s.name, user: s.user, email: s.email});
                tr.append(cell(s.id, 'id'));
                const name = cell(s.name, 'strong');
                const tag = document.createElement('span');
                tag.className = 'new-tag';
                tag.textContent = 'New';
                name.append(' ', tag);
                tr.append(name, cell(s.email || '—', s.email ? '' : 'muted'));
                const act = document.createElement('td');
                act.innerHTML = '<button class="link-btn danger" type="button" data-remove>Remove</button>';
                tr.append(act);
                body.append(tr);
            };
            const toOption = s => {
                const o = document.createElement('option');
                o.value = s.name + ' (' + s.user + ')';
                Object.assign(o.dataset, {id: s.id, name: s.name, user: s.user, email: s.email});
                return o;
            };

            // Lower case and without Vietnamese accents, so "nguyen" also finds "Nguyễn".
            const fold = s => (s || '').normalize('NFD').replace(/[̀-ͯ]/g, '')
                        .replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase();
            const add = () => {
                const v = fold(who.value.trim());
                if (!v) {
                    say('Enter a student name, username or email.');
                    return;
                }
                const all = [...cands.options];
                let hit = all.filter(o => fold(o.value) === v);
                if (hit.length === 0) {
                    hit = all.filter(o => [o.dataset.name, o.dataset.user, o.dataset.email]
                                .some(x => fold(x).includes(v)));
                }
                if (hit.length === 0) {
                    say('No student without a group matches “' + who.value.trim() + '”.');
                    return;
                }
                if (hit.length > 1) {
                    say(hit.length + ' students match “' + who.value.trim() + '”. Pick one from the list.');
                    return;
                }
                const o = hit[0];
                addRow({...o.dataset});
                o.remove();
                who.value = '';
                say('');
                refresh();
                who.focus();
            };
            addBtn.addEventListener('click', add);
            who.addEventListener('keydown', e => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    add();
                }
            });
            who.addEventListener('input', () => say(''));

            body.addEventListener('click', e => {
                const btn = e.target.closest('[data-remove]');
                if (!btn) return;
                const tr = btn.closest('tr');
                // The student has no group again, so they can be added back.
                cands.append(toOption({...tr.dataset}));
                tr.remove();
                refresh();
            });

            document.getElementById('save-form').addEventListener('submit', () => {
                const box = document.getElementById('ids');
                box.replaceChildren(...ids().map(id => {
                    const i = document.createElement('input');
                    i.type = 'hidden';
                    i.name = 'studentId';
                    i.value = id;
                    return i;
                }));
                saving = true;
            });
            // Warn before leaving with changes that were not submitted.
            window.addEventListener('beforeunload', e => {
                if (!saving && !dirtyPill.hidden) {
                    e.preventDefault();
                    e.returnValue = '';
                }
            });
            refresh();

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

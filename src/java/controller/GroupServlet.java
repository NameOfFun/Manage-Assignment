package controller;

import dto.ClassDTO;
import dto.ClassResultDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Group;
import model.User;
import service.ClassService;
import service.ClassService.Access;
import service.GroupService;
import utils.SessionUtil;

public class GroupServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(GroupServlet.class.getName());

    private static final String LIST_PATH = "/groups";
    private static final String DETAIL_PATH = "/groups/detail";
    private static final String LIST_VIEW = "/class/grouplist.jsp";
    private static final String DETAIL_VIEW = "/class/groupdetail.jsp";
    private static final String FLASH = "groupFlash";
    private static final String FLASH_OK = "groupFlashOk";
    private static final int KEYWORD_MAX = 50;

    private final ClassService classService = new ClassService();
    private final GroupService service = new GroupService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User me = authorize(request, response);
            if (me == null) {
                return;
            }
            if (DETAIL_PATH.equals(request.getServletPath())) {
                showDetail(request, response, me);
            } else {
                showList(request, response, me);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Groups: database error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "We could not reach the database. Please try again in a moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            User me = authorize(request, response);
            if (me == null) {
                return;
            }
            String action = request.getParameter("action");
            if (DETAIL_PATH.equals(request.getServletPath())) {
                if ("save".equals(action)) {
                    saveGroup(request, response, me);
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
                }
            } else if ("remove".equals(action)) {
                removeGroup(request, response, me);
            } else if ("randomize".equals(action)) {
                randomize(request, response, me);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Groups: database error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "We could not reach the database. Please try again in a moment.");
        }
    }

    // ------------------------------------------------------------------ Group List
    private void showList(HttpServletRequest request, HttpServletResponse response, User me)
            throws ServletException, IOException, SQLException {
        ClassDTO cls = loadOwnClass(request, response, me, ClassService.parseIntOrNull(request.getParameter("classId")));
        if (cls == null) {
            return;
        }
        String keyword = trimToNull(request.getParameter("q"));
        if (keyword != null && keyword.length() > KEYWORD_MAX) {
            keyword = keyword.substring(0, KEYWORD_MAX);
        }
        request.setAttribute("cls", cls);
        request.setAttribute("result", service.getGroups(cls.getId(), keyword, parsePage(request.getParameter("page"))));
        request.setAttribute("unassignedCount", service.countUnassigned(cls.getId()));
        request.setAttribute("fKeyword", keyword == null ? "" : keyword);
        request.setAttribute("filterQuery", keyword == null ? "" : "q=" + enc(keyword));
        forward(request, response, me, LIST_VIEW);
    }

    private void removeGroup(HttpServletRequest request, HttpServletResponse response, User me)
            throws IOException, SQLException {
        ClassDTO cls = loadOwnClass(request, response, me, ClassService.parseIntOrNull(request.getParameter("classId")));
        if (cls == null) {
            return;
        }
        Integer id = ClassService.parseIntOrNull(request.getParameter("id"));
        flash(request, id == null ? ClassResultDTO.fail("Choose a group to remove.") : service.removeGroup(cls.getId(), id));

        // Back to the same search and page
        String q = trimToNull(request.getParameter("q"));
        int page = parsePage(request.getParameter("page"));
        response.sendRedirect(request.getContextPath() + LIST_PATH + "?classId=" + cls.getId()
                + (q == null ? "" : "&q=" + enc(q)) + (page > 1 ? "&page=" + page : ""));
    }

    private void randomize(HttpServletRequest request, HttpServletResponse response, User me)
            throws IOException, SQLException {
        ClassDTO cls = loadOwnClass(request, response, me, ClassService.parseIntOrNull(request.getParameter("classId")));
        if (cls == null) {
            return;
        }
        flash(request, service.randomize(cls.getId(), request.getParameter("count"), cls.getStudentCount()));
        response.sendRedirect(request.getContextPath() + LIST_PATH + "?classId=" + cls.getId());
    }

    // ------------------------------------------------------------------ Group Details
    private void showDetail(HttpServletRequest request, HttpServletResponse response, User me)
            throws ServletException, IOException, SQLException {
        String idParam = request.getParameter("id");
        Group group = null;
        Integer classId;
        if (idParam == null || idParam.isBlank()) {
            classId = ClassService.parseIntOrNull(request.getParameter("classId")); // new group
        } else {
            Integer id = ClassService.parseIntOrNull(idParam);
            group = id == null ? null : service.findGroup(id);
            if (group == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "This group does not exist.");
                return;
            }
            classId = group.getClassId();
        }
        ClassDTO cls = loadOwnClass(request, response, me, classId);
        if (cls == null) {
            return;
        }
        request.setAttribute("cls", cls);
        request.setAttribute("isNew", group == null);
        request.setAttribute("groupId", group == null ? 0 : group.getId());
        request.setAttribute("members", group == null ? Collections.emptyList() : service.getMembers(group.getId()));
        request.setAttribute("candidates", service.getCandidates(cls.getId()));
        forward(request, response, me, DETAIL_VIEW);
    }

    private void saveGroup(HttpServletRequest request, HttpServletResponse response, User me)
            throws IOException, SQLException {
        ClassDTO cls = loadOwnClass(request, response, me, ClassService.parseIntOrNull(request.getParameter("classId")));
        if (cls == null) {
            return;
        }
        String idParam = request.getParameter("id");
        Integer groupId = null;
        if (idParam != null && !idParam.isBlank()) {
            groupId = ClassService.parseIntOrNull(idParam);
            if (groupId == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid group id.");
                return;
            }
        }
        ClassResultDTO r = service.saveGroup(cls.getId(), groupId, request.getParameterValues("studentId"));
        flash(request, r);
        if (r.isSuccess()) {
            response.sendRedirect(request.getContextPath() + LIST_PATH + "?classId=" + cls.getId());
        } else {
            // Show the error on the form, loaded again from the database.
            response.sendRedirect(request.getContextPath() + DETAIL_PATH
                    + (groupId == null ? "?classId=" + cls.getId() : "?id=" + groupId));
        }
    }

    private User authorize(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        User user = SessionUtil.validateSession(request, response);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return null;
        }
        if (ClassService.accessFor(classService.getRoleName(user)) != Access.TEACH) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Groups are managed by the class's teacher.");
            return null;
        }
        return user;
    }

    private ClassDTO loadOwnClass(HttpServletRequest request, HttpServletResponse response, User me, Integer classId)
            throws IOException, SQLException {
        ClassDTO c = classId == null ? null : classService.findClass(classId);
        if (c == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "This class does not exist.");
            return null;
        }
        if (c.getTeacherId() != me.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not teach this class.");
            return null;
        }
        return c;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, User me, String view)
            throws ServletException, IOException, SQLException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // One-time message from the previous POST
            request.setAttribute("flash", session.getAttribute(FLASH));
            request.setAttribute("flashOk", session.getAttribute(FLASH_OK));
            session.removeAttribute(FLASH);
            session.removeAttribute(FLASH_OK);
        }
        request.setAttribute("me", me);
        request.setAttribute("meRole", classService.getRoleName(me));
        request.setAttribute("fromServlet", Boolean.TRUE);
        request.getRequestDispatcher(view).forward(request, response);
    }

    private static void flash(HttpServletRequest request, ClassResultDTO r) {
        HttpSession session = request.getSession();
        session.setAttribute(FLASH, r.getMessage());
        session.setAttribute(FLASH_OK, r.isSuccess());
    }

    private static int parsePage(String s) {
        Integer p = ClassService.parseIntOrNull(s);
        return p == null || p < 1 ? 1 : p;
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    @Override
    public String getServletInfo() {
        return "Class Management: Group List and Group Details";
    }
}

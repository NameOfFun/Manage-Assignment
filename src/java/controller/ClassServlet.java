package controller;

import dto.ClassDTO;
import dto.ClassResultDTO;
import dto.PageDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import service.ClassService;
import service.ClassService.Access;
import utils.SemesterUtil;
import utils.SessionUtil;

public class ClassServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ClassServlet.class.getName());

    private static final String LIST_PATH = "/classes";
    private static final String DETAIL_PATH = "/classes/detail";
    private static final String LIST_VIEW = "/class/classlist.jsp";
    private static final String DETAIL_VIEW = "/class/classdetail.jsp";
    private static final String FLASH = "classFlash";
    private static final String FLASH_OK = "classFlashOk";
    private static final int KEYWORD_MAX = 50;

    private final ClassService service = new ClassService();

    private static final class Viewer {

        User user;
        String role;
        Access access;

        Integer teacherFilterId() {
            return access == Access.TEACH ? user.getId() : null;
        }

        Integer expertFilterId() {
            return ClassService.ROLE_EXPERT.equalsIgnoreCase(role) ? user.getId() : null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Viewer v = authorize(request, response);
            if (v == null) {
                return;
            }
            if (DETAIL_PATH.equals(request.getServletPath())) {
                showDetail(request, response, v);
            } else {
                showList(request, response, v);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Class Management: database error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "We could not reach the database. Please try again in a moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            Viewer v = authorize(request, response);
            if (v == null) {
                return;
            }
            String action = request.getParameter("action");
            if (DETAIL_PATH.equals(request.getServletPath())) {
                if ("create".equals(action) || "update".equals(action)) {
                    saveClass(request, response, v, "create".equals(action));
                } else if ("addStudent".equals(action) || "removeStudent".equals(action)) {
                    changeRoster(request, response, v, "addStudent".equals(action));
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
                }
            } else if ("remove".equals(action)) {
                removeClass(request, response, v);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Class Management: database error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "We could not reach the database. Please try again in a moment.");
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, Viewer v)
            throws ServletException, IOException, SQLException {
        String subject = trimToNull(request.getParameter("subject"));
        String semester = trimToNull(request.getParameter("semester"));
        String keyword = trimToNull(request.getParameter("q"));
        if (keyword != null && keyword.length() > KEYWORD_MAX) {
            keyword = keyword.substring(0, KEYWORD_MAX);
        }
        int page = parsePage(request.getParameter("page"));
        Integer onlyTeacher = v.teacherFilterId();
        Integer onlyExpert = v.expertFilterId();

        PageDTO<ClassDTO> result = service.getClasses(subject, semester, keyword, onlyTeacher, onlyExpert, page);

        request.setAttribute("result", result);
        request.setAttribute("subjects", service.getSubjectsForFilter(onlyTeacher, onlyExpert));
        request.setAttribute("semesters", service.getSemesterOptions(onlyTeacher, onlyExpert));
        request.setAttribute("fSubject", subject == null ? "" : subject);
        request.setAttribute("fSemester", semester == null ? "" : semester);
        request.setAttribute("fKeyword", keyword == null ? "" : keyword);
        request.setAttribute("filterQuery", filterQuery(subject, semester, keyword));
        forward(request, response, v, LIST_VIEW);
    }

    private void removeClass(HttpServletRequest request, HttpServletResponse response, Viewer v)
            throws IOException, SQLException {
        if (v.access != Access.MANAGE) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only experts and admins can remove classes.");
            return;
        }
        Integer id = ClassService.parseIntOrNull(request.getParameter("id"));
        ClassResultDTO r = id == null ? ClassResultDTO.fail("Choose a class to remove.")
                : service.removeClass(id, v.expertFilterId());
        flash(request, r);

        // Go back to the same filters and page the user was on
        String q = filterQuery(trimToNull(request.getParameter("subject")),
                trimToNull(request.getParameter("semester")), trimToNull(request.getParameter("q")));
        int page = parsePage(request.getParameter("page"));
        String url = request.getContextPath() + LIST_PATH + "?" + q + (q.isEmpty() ? "" : "&") + "page=" + page;
        response.sendRedirect(url);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response, Viewer v)
            throws ServletException, IOException, SQLException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            // New class
            if (v.access != Access.MANAGE) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only experts and admins can create classes.");
                return;
            }
            ClassDTO blank = new ClassDTO();
            blank.setYear(Year.now().getValue());
            showForm(request, response, v, blank, null);
            return;
        }

        ClassDTO c = loadClassFor(request, response, v, ClassService.parseIntOrNull(idParam));
        if (c == null) {
            return;
        }
        showForm(request, response, v, c, null);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Viewer v,
            ClassDTO c, ClassResultDTO failed) throws ServletException, IOException, SQLException {
        boolean isNew = c.getId() == 0;
        boolean canEdit = v.access == Access.MANAGE;
        boolean canRoster = !isNew && v.access == Access.TEACH && c.getTeacherId() == v.user.getId();

        request.setAttribute("cls", c);
        request.setAttribute("isNew", isNew);
        request.setAttribute("canEdit", canEdit);
        request.setAttribute("canRoster", canRoster);
        request.setAttribute("errors", failed == null ? null : failed.getErrors());
        request.setAttribute("formError", failed == null ? null : failed.getMessage());

        if (canEdit) {
            request.setAttribute("subjects", service.getSubjectsForManage(v.expertFilterId()));
            request.setAttribute("teachers", service.getTeachers());
        }
        request.setAttribute("semesterNames", SemesterUtil.ALL);
        request.setAttribute("years", yearsAround(c.getYear()));

        if (canRoster) {
            int rpage = parsePage(request.getParameter("rpage"));
            request.setAttribute("roster", service.getRoster(c.getId(), rpage));
        }
        forward(request, response, v, DETAIL_VIEW);
    }

    private void saveClass(HttpServletRequest request, HttpServletResponse response, Viewer v, boolean create)
            throws ServletException, IOException, SQLException {
        if (v.access != Access.MANAGE) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only experts and admins can change class information.");
            return;
        }
        ClassDTO form = new ClassDTO();
        if (!create) {
            Integer id = ClassService.parseIntOrNull(request.getParameter("id"));
            if (id == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing class id.");
                return;
            }
            form.setId(id);
        }
        form.setClassCode(request.getParameter("classCode"));
        form.setSubjectId(intOrZero(request.getParameter("subjectId")));
        form.setTeacherId(intOrZero(request.getParameter("teacherId")));
        form.setSemester(request.getParameter("semester"));
        form.setYear(intOrZero(request.getParameter("year")));

        ClassResultDTO r = create
                ? service.createClass(form, v.expertFilterId())
                : service.updateClass(form, v.expertFilterId());
        if (r.isSuccess()) {
            flash(request, r);
            response.sendRedirect(request.getContextPath() + LIST_PATH);
            return;
        }
        if (!create && r.getErrors().isEmpty()) {
            // The class was deleted while the form was open.
            flash(request, r);
            response.sendRedirect(request.getContextPath() + LIST_PATH);
            return;
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        showForm(request, response, v, form, r);
    }

    private void changeRoster(HttpServletRequest request, HttpServletResponse response, Viewer v, boolean add)
            throws IOException, SQLException {
        ClassDTO c = loadClassFor(request, response, v, ClassService.parseIntOrNull(request.getParameter("id")));
        if (c == null) {
            return;
        }
        if (v.access != Access.TEACH || c.getTeacherId() != v.user.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only the class's teacher can change its students.");
            return;
        }
        ClassResultDTO r;
        if (add) {
            r = service.addStudent(c.getId(), request.getParameter("student"));
            if (!r.isSuccess()) {
                // Keep what the teacher typed so they can correct it
                request.getSession().setAttribute("classStudentQuery", request.getParameter("student"));
            }
        } else {
            Integer studentId = ClassService.parseIntOrNull(request.getParameter("studentId"));
            r = studentId == null ? ClassResultDTO.fail("Choose a student.") : service.removeStudent(c.getId(), studentId);
        }
        flash(request, r);
        int rpage = parsePage(request.getParameter("rpage"));
        response.sendRedirect(request.getContextPath() + DETAIL_PATH + "?id=" + c.getId()
                + (rpage > 1 ? "&rpage=" + rpage : "") + "#roster");
    }

    private ClassDTO loadClassFor(HttpServletRequest request, HttpServletResponse response, Viewer v, Integer id)
            throws IOException, SQLException {
        ClassDTO c = id == null ? null : service.findClass(id);
        if (c == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "This class does not exist.");
            return null;
        }
        if (v.access == Access.TEACH && c.getTeacherId() != v.user.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not teach this class.");
            return null;
        }
        if (v.expertFilterId() != null && !service.canManageClass(c, v.expertFilterId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not manage the subject of this class.");
            return null;
        }
        return c;
    }

    private Viewer authorize(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        User user = SessionUtil.validateSession(request, response);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return null;
        }
        Viewer v = new Viewer();
        v.user = user;
        v.role = service.getRoleName(user);
        v.access = ClassService.accessFor(v.role);
        if (v.access == Access.NONE) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Class Management is for experts, admins and teachers.");
            return null;
        }
        return v;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, Viewer v, String view)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // One-time message from the previous POST
            request.setAttribute("flash", session.getAttribute(FLASH));
            request.setAttribute("flashOk", session.getAttribute(FLASH_OK));
            session.removeAttribute(FLASH);
            session.removeAttribute(FLASH_OK);
            Object typed = session.getAttribute("classStudentQuery");
            if (typed != null) {
                request.setAttribute("studentQuery", typed);
                session.removeAttribute("classStudentQuery");
            }
        }
        request.setAttribute("me", v.user);
        request.setAttribute("meRole", v.role);
        request.setAttribute("canManage", v.access == Access.MANAGE);
        request.setAttribute("fromServlet", Boolean.TRUE);
        request.getRequestDispatcher(view).forward(request, response);
    }

    private static void flash(HttpServletRequest request, ClassResultDTO r) {
        HttpSession session = request.getSession();
        session.setAttribute(FLASH, r.getMessage());
        session.setAttribute(FLASH_OK, r.isSuccess());
    }

    private static String filterQuery(String subject, String semester, String keyword) {
        List<String> parts = new ArrayList<>();
        if (subject != null) {
            parts.add("subject=" + enc(subject));
        }
        if (semester != null) {
            parts.add("semester=" + enc(semester));
        }
        if (keyword != null) {
            parts.add("q=" + enc(keyword));
        }
        return String.join("&", parts);
    }

    private static List<Integer> yearsAround(int classYear) {
        int now = Year.now().getValue();
        List<Integer> years = new ArrayList<>();
        int from = Math.min(now - 2, classYear > 0 ? classYear : now);
        int to = Math.max(now + 3, classYear);
        for (int y = to; y >= from; y--) {
            years.add(y);
        }
        return years;
    }

    private static int parsePage(String s) {
        Integer p = ClassService.parseIntOrNull(s);
        return p == null || p < 1 ? 1 : p;
    }

    private static int intOrZero(String s) {
        Integer i = ClassService.parseIntOrNull(s);
        return i == null ? 0 : i;
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    @Override
    public String getServletInfo() {
        return "Class Management: Class List and Class Detail";
    }
}

package controller;

import dto.TemplateDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.AssignmentTemplate;
import model.User;
import service.TemplateService;
import utils.SessionUtil;

public class TemplateServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TemplateServlet.class.getName());
    private static final int PAGE_SIZE = 10;

    private final TemplateService service = new TemplateService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User sessionUser = SessionUtil.validateSession(request, response);
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("new".equals(action)) {
            request.setAttribute("subjects", service.getSubjectsForExpert(sessionUser.getId()));
            request.getRequestDispatcher("/expert/templatedetail.jsp").forward(request, response);

        } else if ("edit".equals(action)) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                TemplateDTO dto = service.getById(Integer.parseInt(idStr));
                if (dto.isSuccess()) request.setAttribute("editTemplate", dto.getTemplate());
            }
            request.setAttribute("subjects", service.getSubjectsForExpert(sessionUser.getId()));
            request.getRequestDispatcher("/expert/templatedetail.jsp").forward(request, response);

        } else if ("delete".equals(action)) {
            handleDelete(request, response, sessionUser);

        } else {
            showTemplateList(request, response, sessionUser);
        }
    }

    private void showTemplateList(HttpServletRequest request, HttpServletResponse response, User expert)
            throws ServletException, IOException {
        try {
            String subjectIdStr = request.getParameter("subjectId");
            String keyword      = request.getParameter("keyword");
            String pageStr      = request.getParameter("page");
            String sortBy       = request.getParameter("sortBy");
            String sortDir      = request.getParameter("sortDir");

            Integer subjectId = (subjectIdStr != null && !subjectIdStr.isEmpty()) ? Integer.valueOf(subjectIdStr) : null;
            int page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
            if (page < 1) page = 1;
            if (sortBy == null || sortBy.isEmpty()) sortBy = "template_id";
            if (sortDir == null || sortDir.isEmpty()) sortDir = "asc";

            int total = service.countTemplates(expert.getId(), subjectId, keyword);
            List<AssignmentTemplate> templates = service.getTemplates(expert.getId(), subjectId, keyword, sortBy, sortDir, page);

            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
            if (totalPages < 1) totalPages = 1;

            request.setAttribute("templates", templates);
            request.setAttribute("subjects", service.getSubjectsForExpert(expert.getId()));
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("total", total);
            request.setAttribute("sortBy", sortBy);
            request.setAttribute("sortDir", sortDir);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot load template list", e);
            request.setAttribute("error", "Could not load templates. Please try again.");
        }

        request.getRequestDispatcher("/expert/templatelist.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, User expert)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            // Kiểm tra template có thuộc về expert này không
            TemplateDTO dto = service.getById(id);
            if (dto.isSuccess() && dto.getTemplate().getExpertId() != expert.getId()) {
                request.getSession().setAttribute("flashError", "You do not have permission to delete this template.");
                response.sendRedirect(request.getContextPath() + "/expert/templates");
                return;
            }
            TemplateDTO result = service.deleteTemplate(id);
            if (result.isSuccess()) {
                request.getSession().setAttribute("flashSuccess", result.getMessage());
            } else {
                request.getSession().setAttribute("flashError", result.getMessage());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot delete template", e);
        }
        response.sendRedirect(request.getContextPath() + "/expert/templates");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User sessionUser = SessionUtil.validateSession(request, response);
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }

        String action = request.getParameter("action");
        if ("create".equals(action)) {
            handleSave(request, response, sessionUser, false);
        } else if ("update".equals(action)) {
            handleSave(request, response, sessionUser, true);
        } else {
            response.sendRedirect(request.getContextPath() + "/expert/templates");
        }
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response,
            User expert, boolean isUpdate) throws ServletException, IOException {

        String title       = request.getParameter("title")       != null ? request.getParameter("title").trim()       : "";
        String instruction = request.getParameter("instruction") != null ? request.getParameter("instruction").trim() : "";
        String subjectIdStr = request.getParameter("subjectId");
        String idStr        = request.getParameter("id");

        int subjectId = (subjectIdStr != null && !subjectIdStr.isEmpty()) ? Integer.parseInt(subjectIdStr) : 0;

        // Validation
        boolean hasError = false;
        if (title.isEmpty()) {
            request.setAttribute("titleError", "Title is required.");
            hasError = true;
        } else if (title.length() > 255) {
            request.setAttribute("titleError", "Title must not exceed 255 characters.");
            hasError = true;
        }

        if (instruction.isEmpty()) {
            request.setAttribute("instructionError", "Instruction is required.");
            hasError = true;
        }

        if (subjectId == 0) {
            request.setAttribute("subjectError", "Subject is required.");
            hasError = true;
        }

        if (hasError) {
            request.setAttribute("subjects", service.getSubjectsForExpert(expert.getId()));
            if (isUpdate && idStr != null) {
                TemplateDTO dto = service.getById(Integer.parseInt(idStr));
                if (dto.isSuccess()) request.setAttribute("editTemplate", dto.getTemplate());
            }
            request.getRequestDispatcher("/expert/templatedetail.jsp").forward(request, response);
            return;
        }

        AssignmentTemplate t = new AssignmentTemplate();
        t.setTitle(title);
        t.setInstruction(instruction);
        t.setSubjectId(subjectId);
        t.setExpertId(expert.getId());

        TemplateDTO result;
        if (isUpdate && idStr != null) {
            t.setId(Integer.parseInt(idStr));
            result = service.updateTemplate(t);
        } else {
            result = service.createTemplate(t);
        }

        request.getSession().setAttribute("flashSuccess", result.getMessage());
        response.sendRedirect(request.getContextPath() + "/expert/templates");
    }
}

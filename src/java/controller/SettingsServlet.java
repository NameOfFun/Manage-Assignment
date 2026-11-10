package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import dto.SettingDTO;
import model.Setting;
import model.User;
import service.SettingService;
import utils.SessionUtil;

public class SettingsServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SettingsServlet.class.getName());
    private static final int PAGE_SIZE = 10;

    private final SettingService settingService = new SettingService();

    // Các type không cho tạo mới và không cho deactivate
    private static final Set<String> PROTECTED_TYPES =
        new HashSet<>(Arrays.asList("role", "semester"));

    private boolean isProtectedType(String type) {
        return type != null && PROTECTED_TYPES.contains(type.toLowerCase());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User sessionUser = SessionUtil.validateSession(request, response);
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("activate".equals(action) || "deactivate".equals(action)) {
            handleStatusChange(request, response, action);
        } else if ("new".equals(action)) {
            // Chỉ cho chọn type không phải Role/Semester
            request.setAttribute("settingTypes", settingService.getDistinctTypesFiltered());
            request.getRequestDispatcher("/admin/settingdetail.jsp").forward(request, response);
        } else if ("edit".equals(action)) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                SettingDTO dto = settingService.getSettingById(Integer.parseInt(idStr));
                if (dto.isSuccess()) {
                    request.setAttribute("editSetting", dto.getSetting());
                    // Nếu type là Role hoặc Semester thì không cho sửa type
                    boolean isProtected = isProtectedType(dto.getSetting().getType());
                    request.setAttribute("isProtected", isProtected);
                }
            }
            request.setAttribute("settingTypes", settingService.getDistinctTypesFiltered());
            request.getRequestDispatcher("/admin/settingdetail.jsp").forward(request, response);
        } else {
            showSettingList(request, response);
        }
    }

    private void showSettingList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String type    = request.getParameter("type");
            String status  = request.getParameter("status");
            String keyword = request.getParameter("keyword");
            String pageStr = request.getParameter("page");
            String sortBy  = request.getParameter("sortBy");
            String sortDir = request.getParameter("sortDir");

            int page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
            if (page < 1) page = 1;
            if (sortBy == null || sortBy.isEmpty()) sortBy = "settingId";
            if (sortDir == null || sortDir.isEmpty()) sortDir = "asc";

            List<Setting> settings = settingService.getSettings(type, status, keyword, sortBy, sortDir, page, PAGE_SIZE);
            int total = settingService.countSettings(type, status, keyword);
            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
            if (totalPages < 1) totalPages = 1;

            List<String> settingTypes = settingService.getDistinctTypes();

            request.setAttribute("settings", settings);
            request.setAttribute("settingTypes", settingTypes);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("sortBy", sortBy);
            request.setAttribute("sortDir", sortDir);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot load setting list", e);
            request.setAttribute("error", "Could not load settings. Please try again.");
        }

        request.getRequestDispatcher("/admin/settinglist.jsp").forward(request, response);
    }

    private void handleStatusChange(HttpServletRequest request, HttpServletResponse response, String action)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String newStatus = "activate".equals(action) ? "Active" : "Inactive";

            // Không cho deactivate Role hoặc Semester
            if ("deactivate".equals(action)) {
                SettingDTO dto = settingService.getSettingById(id);
                if (dto.isSuccess() && isProtectedType(dto.getSetting().getType())) {
                    request.getSession().setAttribute("flashError",
                        "Cannot deactivate a " + dto.getSetting().getType() + " setting.");
                    response.sendRedirect(request.getContextPath() + "/admin/settings");
                    return;
                }
            }

            settingService.updateStatus(id, newStatus);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot update setting status", e);
        }
        response.sendRedirect(request.getContextPath() + "/admin/settings");
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
            handleSaveSetting(request, response, false);
        } else if ("update".equals(action)) {
            handleSaveSetting(request, response, true);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/settings");
        }
    }

    private void handleSaveSetting(HttpServletRequest request, HttpServletResponse response, boolean isUpdate)
            throws ServletException, IOException {

        String name        = request.getParameter("name")        != null ? request.getParameter("name").trim()        : "";
        String type        = request.getParameter("type")        != null ? request.getParameter("type").trim()        : "";
        String value       = request.getParameter("value")       != null ? request.getParameter("value").trim()       : "";
        String description = request.getParameter("description") != null ? request.getParameter("description").trim() : "";
        String priorityStr = request.getParameter("priority");
        String idStr       = request.getParameter("id");

        // Validation
        boolean hasError = false;

        if (name.isEmpty()) {
            request.setAttribute("nameError", "Name is required.");
            hasError = true;
        } else if (name.length() > 50) {
            request.setAttribute("nameError", "Name must not exceed 50 characters.");
            hasError = true;
        }

        if (type.isEmpty()) {
            request.setAttribute("typeError", "Type is required.");
            hasError = true;
        }

        if (value.length() > 50) {
            request.setAttribute("valueError", "Value must not exceed 50 characters.");
            hasError = true;
        }

        int priority = 1;
        if (priorityStr == null || priorityStr.trim().isEmpty()) {
            request.setAttribute("priorityError", "Priority is required.");
            hasError = true;
        } else {
            try {
                priority = Integer.parseInt(priorityStr.trim());
                if (priority < 1) {
                    request.setAttribute("priorityError", "Priority must be a positive integer.");
                    hasError = true;
                }
            } catch (NumberFormatException ex) {
                request.setAttribute("priorityError", "Priority must be a positive integer.");
                hasError = true;
            }
        }

        if (!name.isEmpty() && !type.isEmpty()) {
            int excludeId = (isUpdate && idStr != null) ? Integer.parseInt(idStr) : 0;
            if (settingService.existsByNameAndType(name, type, excludeId)) {
                request.setAttribute("nameError", "A setting with this name and type already exists.");
                hasError = true;
            }
        }

        if (hasError) {
            request.setAttribute("settingTypes", settingService.getDistinctTypesFiltered());
            if (isUpdate && idStr != null) {
                SettingDTO dto = settingService.getSettingById(Integer.parseInt(idStr));
                if (dto.isSuccess()) {
                    request.setAttribute("editSetting", dto.getSetting());
                    request.setAttribute("isProtected", isProtectedType(dto.getSetting().getType()));
                }
            }
            request.getRequestDispatcher("/admin/settingdetail.jsp").forward(request, response);
            return;
        }

        Setting s = new Setting();
        s.setName(name);
        s.setType(type);
        s.setValue(value);
        s.setDescription(description);
        s.setPriority(priority);

        if (isUpdate && idStr != null) {
            s.setId(Integer.parseInt(idStr));
            String status = request.getParameter("status");
            s.setStatus(status != null && !status.isEmpty() ? status : "Active");
            SettingDTO result = settingService.updateSetting(s);
            request.getSession().setAttribute("flashSuccess", result.getMessage());
        } else {
            s.setStatus("Active");
            SettingDTO result = settingService.addSetting(s);
            request.getSession().setAttribute("flashSuccess", result.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/settings");
    }
}

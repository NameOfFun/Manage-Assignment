package service;

import dao.ClassDAO;
import dao.TemplateDAO;
import dto.TemplateDTO;
import java.util.ArrayList;
import java.util.List;
import model.AssignmentTemplate;
import model.Setting;

public class TemplateService {

    private final TemplateDAO templateDAO = new TemplateDAO();
    private static final int PAGE_SIZE = 10;

    // Lấy danh sách subjects mà expert này quản lý (từ SubjectExpert)
    public List<Setting> getSubjectsForExpert(int expertId) {
        try (ClassDAO classDAO = new ClassDAO()) {
            return classDAO.getSubjectsForExpert(expertId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Lấy danh sách template — chỉ các template thuộc subject của expert này
    public List<AssignmentTemplate> getTemplates(int expertId, Integer subjectId, String keyword, String sortBy, String sortDir, int page) {
        int offset = (page - 1) * PAGE_SIZE;
        return templateDAO.getTemplatesByExpertSubjects(expertId, subjectId, keyword, sortBy, sortDir, PAGE_SIZE, offset);
    }

    public int countTemplates(int expertId, Integer subjectId, String keyword) {
        return templateDAO.countTemplatesByExpertSubjects(expertId, subjectId, keyword);
    }

    // Lấy template theo id
    public TemplateDTO getById(int id) {
        AssignmentTemplate t = templateDAO.getById(id);
        if (t == null) return new TemplateDTO(false, "Template not found.", null);
        return new TemplateDTO(true, null, t);
    }

    // Tạo template mới
    public TemplateDTO createTemplate(AssignmentTemplate t) {
        templateDAO.addTemplate(t);
        return new TemplateDTO(true, "Template \"" + t.getTitle() + "\" has been created successfully.", t);
    }

    // Cập nhật template
    public TemplateDTO updateTemplate(AssignmentTemplate t) {
        templateDAO.updateTemplate(t);
        return new TemplateDTO(true, "Template \"" + t.getTitle() + "\" has been updated successfully.", t);
    }

    // Xóa template — chỉ xóa được nếu không có class assignment nào dùng
    public TemplateDTO deleteTemplate(int id) {
        AssignmentTemplate t = templateDAO.getById(id);
        if (t == null) return new TemplateDTO(false, "Template not found.", null);
        if (templateDAO.isUsed(id)) {
            return new TemplateDTO(false, "Cannot delete template \"" + t.getTitle()
                + "\" because it is linked to active class assignments.", null);
        }
        templateDAO.deleteTemplate(id);
        return new TemplateDTO(true, "Template \"" + t.getTitle() + "\" has been deleted.", null);
    }
}

package dao;

import java.util.ArrayList;
import java.util.List;
import model.AssignmentTemplate;

public class TemplateDAO extends BaseDAO {

    // Map một row ResultSet thành AssignmentTemplate (kèm subjectName từ join)
    private AssignmentTemplate mapRow() throws Exception {
        AssignmentTemplate t = new AssignmentTemplate();
        t.setId(rs.getInt("template_id"));
        t.setExpertId(rs.getInt("expert_id"));
        t.setSubjectId(rs.getInt("subject_id"));
        t.setTitle(rs.getString("title"));
        t.setInstruction(rs.getString("instruction"));
        return t;
    }

    // Lấy danh sách template — chỉ các template thuộc subject mà expert này quản lý
    public List<AssignmentTemplate> getTemplatesByExpertSubjects(int expertId, Integer subjectId, String keyword, String sortBy, String sortDir, int limit, int offset) {
        List<AssignmentTemplate> list = new ArrayList<>();

        java.util.List<String> allowed = java.util.Arrays.asList("template_id", "title");
        if (!allowed.contains(sortBy)) sortBy = "template_id";
        if (!"desc".equalsIgnoreCase(sortDir)) sortDir = "asc";

        xSql = "SELECT t.template_id, t.expert_id, t.subject_id, t.title, t.instruction "
             + "FROM \"AssignmentTemplate\" t "
             + "WHERE t.subject_id IN (SELECT subject_id FROM \"SubjectExpert\" WHERE user_id = ?)";
        if (subjectId != null) xSql += " AND t.subject_id = ?";
        if (keyword != null && !keyword.isEmpty()) xSql += " AND LOWER(t.title) LIKE ?";
        xSql += " ORDER BY " + sortBy + " " + sortDir + " LIMIT ? OFFSET ?";
        try {
            ps = con.prepareStatement(xSql);
            int idx = 1;
            ps.setInt(idx++, expertId);
            if (subjectId != null) ps.setInt(idx++, subjectId);
            if (keyword != null && !keyword.isEmpty()) ps.setString(idx++, "%" + keyword.toLowerCase() + "%");
            ps.setInt(idx++, limit);
            ps.setInt(idx++, offset);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow());
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Đếm template theo subject của expert
    public int countTemplatesByExpertSubjects(int expertId, Integer subjectId, String keyword) {
        int count = 0;
        xSql = "SELECT COUNT(*) FROM \"AssignmentTemplate\" t "
             + "WHERE t.subject_id IN (SELECT subject_id FROM \"SubjectExpert\" WHERE user_id = ?)";
        if (subjectId != null) xSql += " AND t.subject_id = ?";
        if (keyword != null && !keyword.isEmpty()) xSql += " AND LOWER(t.title) LIKE ?";
        try {
            ps = con.prepareStatement(xSql);
            int idx = 1;
            ps.setInt(idx++, expertId);
            if (subjectId != null) ps.setInt(idx++, subjectId);
            if (keyword != null && !keyword.isEmpty()) ps.setString(idx++, "%" + keyword.toLowerCase() + "%");
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // Lấy danh sách template theo expert (có thể lọc thêm theo subject và keyword)
    public List<AssignmentTemplate> getTemplates(int expertId, Integer subjectId, String keyword, String sortBy, String sortDir, int limit, int offset) {
        List<AssignmentTemplate> list = new ArrayList<>();

        // Whitelist các cột được phép sort
        java.util.List<String> allowed = java.util.Arrays.asList("template_id", "title");
        if (!allowed.contains(sortBy)) sortBy = "template_id";
        if (!"desc".equalsIgnoreCase(sortDir)) sortDir = "asc";

        xSql = "SELECT t.template_id, t.expert_id, t.subject_id, t.title, t.instruction "
             + "FROM \"AssignmentTemplate\" t "
             + "WHERE t.expert_id = ?";
        if (subjectId != null) xSql += " AND t.subject_id = ?";
        if (keyword != null && !keyword.isEmpty()) xSql += " AND LOWER(t.title) LIKE ?";
        xSql += " ORDER BY " + sortBy + " " + sortDir + " LIMIT ? OFFSET ?";
        try {
            ps = con.prepareStatement(xSql);
            int idx = 1;
            ps.setInt(idx++, expertId);
            if (subjectId != null) ps.setInt(idx++, subjectId);
            if (keyword != null && !keyword.isEmpty()) ps.setString(idx++, "%" + keyword.toLowerCase() + "%");
            ps.setInt(idx++, limit);
            ps.setInt(idx++, offset);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow());
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Đếm tổng số template (dùng cho phân trang)
    public int countTemplates(int expertId, Integer subjectId, String keyword) {
        int count = 0;
        xSql = "SELECT COUNT(*) FROM \"AssignmentTemplate\" t WHERE t.expert_id = ?";
        if (subjectId != null) xSql += " AND t.subject_id = ?";
        if (keyword != null && !keyword.isEmpty()) xSql += " AND LOWER(t.title) LIKE ?";
        try {
            ps = con.prepareStatement(xSql);
            int idx = 1;
            ps.setInt(idx++, expertId);
            if (subjectId != null) ps.setInt(idx++, subjectId);
            if (keyword != null && !keyword.isEmpty()) ps.setString(idx++, "%" + keyword.toLowerCase() + "%");
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // Lấy template theo id
    public AssignmentTemplate getById(int id) {
        AssignmentTemplate t = null;
        xSql = "SELECT template_id, expert_id, subject_id, title, instruction "
             + "FROM \"AssignmentTemplate\" WHERE template_id = ?";
        try {
            ps = con.prepareStatement(xSql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) t = mapRow();
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    // Kiểm tra template có đang được dùng bởi ClassAssignment không
    public boolean isUsed(int templateId) {
        int count = 0;
        xSql = "SELECT COUNT(*) FROM \"ClassAssignment\" WHERE template_id = ?";
        try {
            ps = con.prepareStatement(xSql);
            ps.setInt(1, templateId);
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count > 0;
    }

    // Thêm template mới
    public void addTemplate(AssignmentTemplate t) {
        xSql = "INSERT INTO \"AssignmentTemplate\" (expert_id, subject_id, title, instruction) VALUES (?,?,?,?)";
        try {
            ps = con.prepareStatement(xSql);
            ps.setInt(1, t.getExpertId());
            ps.setInt(2, t.getSubjectId());
            ps.setString(3, t.getTitle());
            ps.setString(4, t.getInstruction());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cập nhật template
    public void updateTemplate(AssignmentTemplate t) {
        xSql = "UPDATE \"AssignmentTemplate\" SET subject_id=?, title=?, instruction=? WHERE template_id=?";
        try {
            ps = con.prepareStatement(xSql);
            ps.setInt(1, t.getSubjectId());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getInstruction());
            ps.setInt(4, t.getId());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Xóa template
    public void deleteTemplate(int id) {
        xSql = "DELETE FROM \"AssignmentTemplate\" WHERE template_id = ?";
        try {
            ps = con.prepareStatement(xSql);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

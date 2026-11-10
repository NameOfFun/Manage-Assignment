package service;

import dao.GroupDAO;
import dao.GroupDAO.Outcome;
import dao.GroupDAO.SaveResult;
import dto.ClassResultDTO;
import dto.ClassStudentDTO;
import dto.GroupRowDTO;
import dto.PageDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.Group;

/**
 */
public class GroupService {

    public static final int GROUP_PAGE_SIZE = 10;

    public PageDTO<GroupRowDTO> getGroups(int classId, String keyword, int page) throws SQLException {
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();
        try (GroupDAO dao = new GroupDAO()) {
            int total = dao.countGroups(classId, kw);
            int p = PageDTO.clampPage(page, total, GROUP_PAGE_SIZE);
            return new PageDTO<>(dao.getGroups(classId, kw, (p - 1) * GROUP_PAGE_SIZE, GROUP_PAGE_SIZE),
                    p, GROUP_PAGE_SIZE, total);
        }
    }

    public int countUnassigned(int classId) throws SQLException {
        try (GroupDAO dao = new GroupDAO()) {
            return dao.countUnassigned(classId);
        }
    }

    public Group findGroup(int groupId) throws SQLException {
        try (GroupDAO dao = new GroupDAO()) {
            return dao.findGroup(groupId);
        }
    }

    public List<ClassStudentDTO> getMembers(int groupId) throws SQLException {
        try (GroupDAO dao = new GroupDAO()) {
            return dao.getMembers(groupId);
        }
    }

    /** Students of the class without a group: the ones that can be added. */
    public List<ClassStudentDTO> getCandidates(int classId) throws SQLException {
        try (GroupDAO dao = new GroupDAO()) {
            return dao.getUnassigned(classId);
        }
    }

    public ClassResultDTO removeGroup(int classId, int groupId) throws SQLException {
        try (GroupDAO dao = new GroupDAO()) {
            switch (dao.deleteGroup(classId, groupId)) {
                case OK:
                    return ClassResultDTO.ok("Group " + groupId + " was removed.", null);
                case IN_USE:
                    return ClassResultDTO.fail("Group " + groupId + " cannot be removed because it already has submissions.");
                default:
                    return ClassResultDTO.fail("This group no longer exists.");
            }
        }
    }

    /**
     * @param countText what the teacher typed in "Number of groups"
     * @param studentCount students in the class
     */
    public ClassResultDTO randomize(int classId, String countText, int studentCount) throws SQLException {
        if (studentCount == 0) {
            return ClassResultDTO.fail("This class has no students to put into groups.");
        }
        Integer count = ClassService.parseIntOrNull(countText);
        if (count == null || count < 1 || count > studentCount) {
            return ClassResultDTO.fail("Enter a number of groups from 1 to " + studentCount + ".");
        }
        try (GroupDAO dao = new GroupDAO()) {
            Outcome o = dao.randomize(classId, count);
            if (o == Outcome.IN_USE) {
                return ClassResultDTO.fail("Groups cannot be randomized because some groups already have submissions.");
            }
            if (o != Outcome.OK) {
                return ClassResultDTO.fail("This class no longer exists.");
            }
            int min = studentCount / count, max = (studentCount + count - 1) / count;
            String size = min == max ? min + " students each" : min + "–" + max + " students each";
            return ClassResultDTO.ok(studentCount + " students were split into " + count
                    + " group" + (count == 1 ? "" : "s") + " (" + size + ").", null);
        }
    }

    /**
     * Saves the members chosen on Group Details.
     *
     * @param groupId null for a new group
     * @param studentIdTexts the "studentId" values posted by the form
     */
    public ClassResultDTO saveGroup(int classId, Integer groupId, String[] studentIdTexts) throws SQLException {
        Set<Integer> ids = new LinkedHashSet<>();
        if (studentIdTexts != null) {
            for (String s : studentIdTexts) {
                Integer id = ClassService.parseIntOrNull(s);
                if (id == null) {
                    return ClassResultDTO.fail("The student list was not valid. Reload the page and try again.");
                }
                ids.add(id);
            }
        }
        if (groupId == null && ids.isEmpty()) {
            return ClassResultDTO.fail("Add at least one student to create a group.");
        }
        try (GroupDAO dao = new GroupDAO()) {
            SaveResult r = dao.saveGroup(classId, groupId, new ArrayList<>(ids));
            switch (r.outcome) {
                case OK:
                    return ClassResultDTO.ok((groupId == null ? "Group " + r.groupId + " was created" : "Group " + r.groupId + " was saved")
                            + " with " + ids.size() + " student" + (ids.size() == 1 ? "" : "s") + ".", null);
                case IN_OTHER_GROUP:
                    return ClassResultDTO.fail("Already in another group: " + String.join(", ", r.names)
                            + ". Reload the page and try again.");
                case INVALID_STUDENT:
                    return ClassResultDTO.fail("Some students are no longer in this class. Reload the page and try again.");
                default:
                    return ClassResultDTO.fail("This group no longer exists.");
            }
        }
    }
}

package dto;

import java.util.Collections;
import java.util.List;

public class GroupRowDTO {

    private final int id;
    private final int classId;
    private final List<String> memberNames;

    public GroupRowDTO(int id, int classId, List<String> memberNames) {
        this.id = id;
        this.classId = classId;
        this.memberNames = memberNames == null ? Collections.emptyList() : memberNames;
    }

    public int getId() {
        return id;
    }

    public int getClassId() {
        return classId;
    }

    public List<String> getMemberNames() {
        return memberNames;
    }

    public int getMemberCount() {
        return memberNames.size();
    }
}

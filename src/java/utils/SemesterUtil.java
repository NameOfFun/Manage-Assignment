package utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SemesterUtil {

    public static final String SPRING = "Spring";
    public static final String SUMMER = "Summer";
    public static final String FALL = "Fall";

    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(SPRING, SUMMER, FALL));

    private SemesterUtil() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim().toUpperCase();
        if (v.startsWith("SP")) {
            return SPRING;
        }
        if (v.startsWith("SU")) {
            return SUMMER;
        }
        if (v.startsWith("FA")) {
            return FALL;
        }
        return null;
    }

    public static String prefix(String semester) {
        String s = normalize(semester);
        if (s == null) {
            return semester == null ? "" : semester;
        }
        return s.substring(0, 2).toUpperCase();
    }

    public static String code(String semester, int year) {
        return prefix(semester) + String.format("%02d", Math.floorMod(year, 100));
    }
}

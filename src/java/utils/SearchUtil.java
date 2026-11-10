package utils;

import java.text.Normalizer;


public final class SearchUtil {

    private static final String LOWER_FROM = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ";
    private static final String LOWER_TO = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd";

    public static final String FROM = LOWER_FROM + LOWER_FROM.toUpperCase();
    public static final String TO = LOWER_TO + LOWER_TO.toUpperCase();

    private SearchUtil() {
    }

    public static String foldSql(String column) {
        return "LOWER(TRANSLATE(COALESCE(" + column + ", ''), ?, ?))";
    }

    public static String fold(String s) {
        if (s == null) {
            return "";
        }
        String noMarks = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return noMarks.replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }

    public static String likePattern(String keyword) {
        String k = fold(keyword.trim()).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + k + "%";
    }
}

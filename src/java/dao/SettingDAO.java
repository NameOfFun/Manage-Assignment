/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.ArrayList;
import java.util.List;
import model.Setting;

public class SettingDAO extends BaseDAO {

    // Lấy danh sách setting theo type (dùng để load roles, subjects...)
    public List<Setting> getByType(String type) {
        List<Setting> list = new ArrayList<>();
        xSql = "select * from \"Settings\" where type = ? and status = 'Active' order by priority";
        try {
            ps = con.prepareStatement(xSql);
            ps.setString(1, type);
            rs = ps.executeQuery();
            while (rs.next()) {
                Setting s = new Setting(
                    rs.getInt("settingId"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getString("value"),
                    rs.getString("description"),
                    rs.getInt("priority"),
                    rs.getString("status")
                );
                list.add(s);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Đếm số setting theo type (ví dụ: type = 'Subject', type = 'Role')
    public int countByType(String type) {
        int count = 0;
        xSql = "select count(*) from \"Settings\" where type = ?";
        try {
            ps = con.prepareStatement(xSql);
            ps.setString(1, type);
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // Đếm số setting theo type và status
    public int countByTypeAndStatus(String type, String status) {
        int count = 0;
        xSql = "select count(*) from \"Settings\" where type = ? and status = ?";
        try {
            ps = con.prepareStatement(xSql);
            ps.setString(1, type);
            ps.setString(2, status);
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // Kiểm tra tồn tại setting trùng name + type (bỏ qua chính nó khi edit)
    public boolean existsByNameAndType(String name, String type, int excludeId) {
        int count = 0;
        xSql = "SELECT COUNT(*) FROM \"Settings\" WHERE LOWER(name) = LOWER(?) AND LOWER(type) = LOWER(?) AND settingId != ?";
        try {
            ps = con.prepareStatement(xSql);
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setInt(3, excludeId);
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count > 0;
    }

    // Lấy danh sách type phân biệt, bỏ Role và Semester (dùng cho New Setting)
    public List<String> getDistinctTypesFiltered() {
        List<String> types = new ArrayList<>();
        xSql = "select distinct type from \"Settings\" where type is not null"
             + " and lower(type) not in ('role', 'semester') order by type";
        try {
            ps = con.prepareStatement(xSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                types.add(rs.getString("type"));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return types;
    }

    // Lấy danh sách tất cả các type phân biệt (dùng cho dropdown filter)
    public List<String> getDistinctTypes() {
        List<String> types = new ArrayList<>();
        xSql = "select distinct type from \"Settings\" where type is not null order by type";
        try {
            ps = con.prepareStatement(xSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                types.add(rs.getString("type"));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return types;
    }

    // Lấy danh sách setting theo filter + sort + phân trang
    public List<Setting> getSettings(String type, String status, String keyword, String sortBy, String sortDir, int limit, int offset) {
        List<Setting> list = new ArrayList<>();

        // Whitelist các cột được phép sort để tránh SQL injection
        List<String> allowedSort = java.util.Arrays.asList("name", "type", "priority", "status", "settingId");
        if (!allowedSort.contains(sortBy)) sortBy = "name";
        if (!"desc".equalsIgnoreCase(sortDir)) sortDir = "asc";

        xSql = "select * from \"Settings\" where 1=1";
        if (type != null && !type.isEmpty()) {
            xSql += " and type = ?";
        }
        if (status != null && !status.isEmpty()) {
            xSql += " and status = ?";
        }
        if (keyword != null && !keyword.isEmpty()) {
            xSql += " and (lower(name) like ? or lower(value) like ?)";
        }
        xSql += " order by " + sortBy + " " + sortDir + " limit ? offset ?";
        try {
            ps = con.prepareStatement(xSql);
            int idx = 1;
            if (type != null && !type.isEmpty())    ps.setString(idx++, type);
            if (status != null && !status.isEmpty()) ps.setString(idx++, status);
            if (keyword != null && !keyword.isEmpty()) {
                String kw = "%" + keyword.toLowerCase() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx++, offset);
            rs = ps.executeQuery();
            while (rs.next()) {
                Setting s = new Setting(
                    rs.getInt("settingId"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getString("value"),
                    rs.getString("description"),
                    rs.getInt("priority"),
                    rs.getString("status")
                );
                list.add(s);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Đếm tổng số setting theo filter (dùng để tính tổng số trang)
    public int countSettings(String type, String status, String keyword) {
        int count = 0;
        xSql = "select count(*) from \"Settings\" where 1=1";
        if (type != null && !type.isEmpty()) {
            xSql += " and type = ?";
        }
        if (status != null && !status.isEmpty()) {
            xSql += " and status = ?";
        }
        if (keyword != null && !keyword.isEmpty()) {
            xSql += " and (lower(name) like ? or lower(value) like ?)";
        }
        try {
            ps = con.prepareStatement(xSql);
            int idx = 1;
            if (type != null && !type.isEmpty())    ps.setString(idx++, type);
            if (status != null && !status.isEmpty()) ps.setString(idx++, status);
            if (keyword != null && !keyword.isEmpty()) {
                String kw = "%" + keyword.toLowerCase() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // Lấy setting theo id
    public Setting getSettingById(int id) {
        Setting s = null;
        xSql = "select * from \"Settings\" where settingId = ?";
        try {
            ps = con.prepareStatement(xSql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                s = new Setting(
                    rs.getInt("settingId"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getString("value"),
                    rs.getString("description"),
                    rs.getInt("priority"),
                    rs.getString("status")
                );
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    // Cập nhật status (Activate/Deactivate)
    public void updateStatus(int id, String status) {
        xSql = "update \"Settings\" set status = ? where settingId = ?";
        try {
            ps = con.prepareStatement(xSql);
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Thêm setting mới
    public void addSetting(Setting s) {
        xSql = "insert into \"Settings\" (name, type, value, description, priority, status) values (?,?,?,?,?,?)";
        try {
            ps = con.prepareStatement(xSql);
            ps.setString(1, s.getName());
            ps.setString(2, s.getType());
            ps.setString(3, s.getValue());
            ps.setString(4, s.getDescription());
            ps.setInt(5, s.getPriority());
            ps.setString(6, s.getStatus());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cập nhật setting
    public void updateSetting(Setting s) {
        xSql = "update \"Settings\" set name=?, type=?, value=?, description=?, priority=?, status=? where settingId=?";
        try {
            ps = con.prepareStatement(xSql);
            ps.setString(1, s.getName());
            ps.setString(2, s.getType());
            ps.setString(3, s.getValue());
            ps.setString(4, s.getDescription());
            ps.setInt(5, s.getPriority());
            ps.setString(6, s.getStatus());
            ps.setInt(7, s.getId());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.*;
import model.User;

/**
 *
 * @author ASUS
 */
public class UserDAO extends BaseDAO {

	public List<User> getUsers() {
		List<User> t = new ArrayList<>();
		xSql = "select * from \"User\"";
		try {
			ps = con.prepareStatement(xSql);
			rs = ps.executeQuery();
			String xName, xPass;
			int xId;
			User x;
			while (rs.next()) {
				xId = rs.getInt("role_id");
				xName = rs.getString("username");
				xPass = rs.getString("password_hash");
				x = new User(xId, xName, xPass);
				x.setId(rs.getInt("user_id"));
				x.setImageUrl(rs.getString("image_url"));
				t.add(x);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (t);
	}

	public User getUser(String xName) {
		xSql = "select * from \"User\" where username = ?";

		String xPass;
		int xId;
		User x = null;
		try {
			ps = con.prepareStatement(xSql);
			ps.setString(1, xName);
			rs = ps.executeQuery();

			if (rs.next()) {
				xId = rs.getInt("role_id");
				xName = rs.getString("username");
				xPass = rs.getString("password_hash");
				x = new User(xId, xName, xPass);
				x.setId(rs.getInt("user_id"));
				x.setEmail(rs.getString("email"));
				x.setFullName(rs.getString("full_name"));
				x.setStatus(rs.getString("status"));
				x.setImageUrl(rs.getString("image_url"));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (x);
	}

	public User getUserFromEmail(String xEmail) {
		xSql = "select * from \"User\" where email = ?";

		String xName, xPass;
		int xId;
		User x = null;
		try {
			ps = con.prepareStatement(xSql);
			ps.setString(1, xEmail);
			rs = ps.executeQuery();

			if (rs.next()) {
				xId = rs.getInt("role_id");
				xName = rs.getString("username");
				xPass = rs.getString("password_hash");
				x = new User(xId, xName, xPass);
				x.setId(rs.getInt("user_id"));
				x.setEmail(xEmail);
				x.setFullName(rs.getString("full_name"));
				x.setStatus(rs.getString("status"));
				x.setImageUrl(rs.getString("image_url"));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (x);
	}

	public void addUser(User u) {
		xSql = "insert into \"User\" (role_id,username,password_hash,email,status,created_at,full_name) values (?,?,?,?,?,?,?)";
		try {
			ps = con.prepareStatement(xSql);
			ps.setInt(1, u.getRoleId());
			ps.setString(2, u.getUsername());
			ps.setString(3, u.getPasswordHash());
			ps.setString(4, u.getEmail());
			ps.setString(5, u.getStatus());
			ps.setObject(6, u.getCreatedAt());
			ps.setString(7, u.getFullName());
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int countUsers() {
		int count = 0;
		xSql = "select count(*) from \"User\"";
		try {
			ps = con.prepareStatement(xSql);
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

	public User getUserById(int userId) {
		xSql = "select * from \"User\" where user_id = ?";
		User x = null;
		try {
			ps = con.prepareStatement(xSql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();
			if (rs.next()) {
				x = new User(
					rs.getInt("user_id"),
					rs.getInt("role_id"),
					rs.getString("username"),
					rs.getString("password_hash"),
					rs.getString("email"),
					rs.getString("full_name"),
					rs.getString("status"),
					rs.getObject("created_at", java.time.LocalDate.class)
				);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return x;
	}

	public void updateUser(User u) {
		xSql = "update \"User\" set full_name=?, username=?, password_hash=?, email=?, role_id=?, status=?, image_url=? where user_id=?";
		try {
			ps = con.prepareStatement(xSql);
			ps.setString(1, u.getFullName());
			ps.setString(2, u.getUsername());
			ps.setString(3, u.getPasswordHash());
			ps.setString(4, u.getEmail());
			ps.setInt(5, u.getRoleId());
			ps.setString(6, u.getStatus());
			ps.setString(7, u.getImageUrl());
			ps.setInt(8, u.getId());
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateUserStatus(int userId, String status) {
		xSql = "update \"User\" set status=? where user_id=?";
		try {
			ps = con.prepareStatement(xSql);
			ps.setString(1, status);
			ps.setInt(2, userId);
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getRoleName(int roleId) {
		xSql = "select * from \"Settings\" where settingId = ? and type = \'Role\'";
		String role = "";
		try {
			ps = con.prepareStatement(xSql);
			ps.setInt(1, roleId);
			rs = ps.executeQuery();
			if (rs.next()) {
				role = rs.getString("name");
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return role;
	}

	public List<User> getUsersByRoleAndStatus(Integer roleId, String status) {
		List<User> t = new ArrayList<>();
		xSql = "select * from \"User\" where 1=1";
		if (roleId != null) {
			xSql += " and role_id = ?";
		}
		if (status != null && !status.isEmpty()) {
			xSql += " and status = ?";
		}
		try {
			ps = con.prepareStatement(xSql);
			int idx = 1;
			if (roleId != null) {
				ps.setInt(idx++, roleId);
			}
			if (status != null && !status.isEmpty()) {
				ps.setString(idx++, status);
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				User x = new User(
					rs.getInt("user_id"),
					rs.getInt("role_id"),
					rs.getString("username"),
					rs.getString("password_hash"),
					rs.getString("email"),
					rs.getString("full_name"),
					rs.getString("status"),
					rs.getObject("created_at", java.time.LocalDate.class)
				);
				t.add(x);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

	// Lấy danh sách user theo filter + keyword + sort + phân trang (LIMIT/OFFSET)
	public List<User> searchUsers(Integer roleId, String status, String keyword, String sortBy, String sortDir, int limit, int offset) {
		List<User> t = new ArrayList<>();
		xSql = "select * from \"User\" where 1=1";
		if (roleId != null) xSql += " and role_id = ?";
		if (status != null && !status.isEmpty()) xSql += " and status = ?";
		if (keyword != null && !keyword.isEmpty()) {
			xSql += " and (lower(full_name) like ? or lower(username) like ? or lower(email) like ?)";
		}
		// Sort
		String col = "user_id";
		if ("fullName".equals(sortBy))  col = "full_name";
		else if ("username".equals(sortBy)) col = "username";
		else if ("email".equals(sortBy))    col = "email";
		else if ("status".equals(sortBy))   col = "status";
		else if ("createdAt".equals(sortBy)) col = "created_at";
		String dir = "desc".equalsIgnoreCase(sortDir) ? "desc" : "asc";
		xSql += " order by " + col + " " + dir + " limit ? offset ?";
		try {
			ps = con.prepareStatement(xSql);
			int idx = 1;
			if (roleId != null) ps.setInt(idx++, roleId);
			if (status != null && !status.isEmpty()) ps.setString(idx++, status);
			if (keyword != null && !keyword.isEmpty()) {
				String kw = "%" + keyword.toLowerCase() + "%";
				ps.setString(idx++, kw);
				ps.setString(idx++, kw);
				ps.setString(idx++, kw);
			}
			ps.setInt(idx++, limit);
			ps.setInt(idx++, offset);
			rs = ps.executeQuery();
			while (rs.next()) {
				User x = new User(
					rs.getInt("user_id"), rs.getInt("role_id"),
					rs.getString("username"), rs.getString("password_hash"),
					rs.getString("email"), rs.getString("full_name"),
					rs.getString("status"),
					rs.getObject("created_at", java.time.LocalDate.class)
				);
				t.add(x);
			}
			rs.close(); ps.close();
		} catch (Exception e) { e.printStackTrace(); }
		return t;
	}

	// Đếm tổng số user theo filter + keyword
	public int countSearchUsers(Integer roleId, String status, String keyword) {
		int count = 0;
		xSql = "select count(*) from \"User\" where 1=1";
		if (roleId != null) xSql += " and role_id = ?";
		if (status != null && !status.isEmpty()) xSql += " and status = ?";
		if (keyword != null && !keyword.isEmpty()) {
			xSql += " and (lower(full_name) like ? or lower(username) like ? or lower(email) like ?)";
		}
		try {
			ps = con.prepareStatement(xSql);
			int idx = 1;
			if (roleId != null) ps.setInt(idx++, roleId);
			if (status != null && !status.isEmpty()) ps.setString(idx++, status);
			if (keyword != null && !keyword.isEmpty()) {
				String kw = "%" + keyword.toLowerCase() + "%";
				ps.setString(idx++, kw);
				ps.setString(idx++, kw);
				ps.setString(idx++, kw);
			}
			rs = ps.executeQuery();
			if (rs.next()) count = rs.getInt(1);
			rs.close(); ps.close();
		} catch (Exception e) { e.printStackTrace(); }
		return count;
	}

	// Lấy danh sách user theo filter + phân trang (LIMIT/OFFSET)
	public List<User> getUsersByRoleAndStatus(Integer roleId, String status, int limit, int offset) {
		List<User> t = new ArrayList<>();
		xSql = "select * from \"User\" where 1=1";
		if (roleId != null) {
			xSql += " and role_id = ?";
		}
		if (status != null && !status.isEmpty()) {
			xSql += " and status = ?";
		}
		xSql += " order by user_id limit ? offset ?";
		try {
			ps = con.prepareStatement(xSql);
			int idx = 1;
			if (roleId != null) {
				ps.setInt(idx++, roleId);
			}
			if (status != null && !status.isEmpty()) {
				ps.setString(idx++, status);
			}
			ps.setInt(idx++, limit);
			ps.setInt(idx++, offset);
			rs = ps.executeQuery();
			while (rs.next()) {
				User x = new User(
					rs.getInt("user_id"),
					rs.getInt("role_id"),
					rs.getString("username"),
					rs.getString("password_hash"),
					rs.getString("email"),
					rs.getString("full_name"),
					rs.getString("status"),
					rs.getObject("created_at", java.time.LocalDate.class)
				);
				t.add(x);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

    // Đếm số user theo tên role (join với Settings để không hardcode role_id)
    public int countUsersByRoleName(String roleName) {
        int count = 0;
        xSql = "select count(*) from \"User\" u"
             + " join \"Settings\" s on u.role_id = s.settingId"
             + " where upper(s.name) = upper(?) and upper(s.type) = 'ROLE'";
        try {
            ps = con.prepareStatement(xSql);
            ps.setString(1, roleName);
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

	// -------------------------------------------------------------------------
	// SubjectExpert — quản lý subject của Expert
	// -------------------------------------------------------------------------

	// Lấy danh sách subject đang được assign cho expert
	public List<model.Setting> getSubjectsByExpert(int expertId) {
		List<model.Setting> list = new ArrayList<>();
		xSql = "select s.settingid, s.name, s.type, s.value, s.description, s.priority, s.status "
		     + "from \"Settings\" s "
		     + "join \"SubjectExpert\" se on se.subject_id = s.settingid "
		     + "where se.user_id = ? and upper(s.type) = 'SUBJECT' "
		     + "order by s.name";
		try {
			ps = con.prepareStatement(xSql);
			ps.setInt(1, expertId);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new model.Setting(
					rs.getInt("settingid"), rs.getString("name"), rs.getString("type"),
					rs.getString("value"), rs.getString("description"),
					rs.getInt("priority"), rs.getString("status")
				));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// Assign subject cho expert
	public void addSubjectExpert(int expertId, int subjectId) {
		xSql = "insert into \"SubjectExpert\" (user_id, subject_id) values (?, ?) on conflict do nothing";
		try {
			ps = con.prepareStatement(xSql);
			ps.setInt(1, expertId);
			ps.setInt(2, subjectId);
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Xóa toàn bộ subject của expert rồi insert lại (dùng khi save)
	public void replaceSubjectExpert(int expertId, List<Integer> subjectIds) {
		xSql = "delete from \"SubjectExpert\" where user_id = ?";
		try {
			ps = con.prepareStatement(xSql);
			ps.setInt(1, expertId);
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (subjectIds == null) return;
		for (int subjectId : subjectIds) {
			addSubjectExpert(expertId, subjectId);
		}
	}

	// Đếm tổng số user theo filter (dùng để tính tổng số trang)
	public int countUsersByRoleAndStatus(Integer roleId, String status) {		int count = 0;
		xSql = "select count(*) from \"User\" where 1=1";
		if (roleId != null) {
			xSql += " and role_id = ?";
		}
		if (status != null && !status.isEmpty()) {
			xSql += " and status = ?";
		}
		try {
			ps = con.prepareStatement(xSql);
			int idx = 1;
			if (roleId != null) {
				ps.setInt(idx++, roleId);
			}
			if (status != null && !status.isEmpty()) {
				ps.setString(idx++, status);
			}
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
}

package service;

import dao.SettingDAO;
import dto.SettingDTO;
import java.util.List;
import model.Setting;

public class SettingService {

    private final SettingDAO settingDAO = new SettingDAO();

    public List<Setting> getByType(String type){
        return settingDAO.getByType(type);
    }
    
    public List<Setting> getSettings(String type, String status, String keyword, String sortBy, String sortDir, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return settingDAO.getSettings(type, status, keyword, sortBy, sortDir, pageSize, offset);
    }

    public boolean existsByNameAndType(String name, String type, int excludeId) {
        return settingDAO.existsByNameAndType(name, type, excludeId);
    }

    public int countSettings(String type, String status, String keyword) {
        return settingDAO.countSettings(type, status, keyword);
    }

    public List<String> getDistinctTypes() {
        return settingDAO.getDistinctTypes();
    }

    public List<String> getDistinctTypesFiltered() {
        return settingDAO.getDistinctTypesFiltered();
    }

    public SettingDTO getSettingById(int id) {
        Setting s = settingDAO.getSettingById(id);
        if (s == null) {
            return new SettingDTO(false, "Setting not found.", null);
        }
        return new SettingDTO(true, null, s);
    }

    public SettingDTO addSetting(Setting s) {
        try {
            settingDAO.addSetting(s);
            return new SettingDTO(true, "Setting \"" + s.getName() + "\" has been created successfully.", s);
        } catch (Exception e) {
            return new SettingDTO(false, "Could not create setting. Please try again.", null);
        }
    }

    public SettingDTO updateSetting(Setting s) {
        try {
            settingDAO.updateSetting(s);
            return new SettingDTO(true, "Setting \"" + s.getName() + "\" has been updated successfully.", s);
        } catch (Exception e) {
            return new SettingDTO(false, "Could not update setting. Please try again.", null);
        }
    }

    public void updateStatus(int id, String status) {
        settingDAO.updateStatus(id, status);
    }
}


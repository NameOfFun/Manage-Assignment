package dto;

import model.Setting;

public class SettingDTO {

    private boolean success;
    private String message;
    private Setting setting;

    public SettingDTO() {
    }

    public SettingDTO(boolean success, String message, Setting setting) {
        this.success = success;
        this.message = message;
        this.setting = setting;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }
}

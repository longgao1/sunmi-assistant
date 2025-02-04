package com.sunmi.ipc.model;

/**
 * Description:
 * Created by bruce on 2019/9/17.
 */
public class IpcManageBean {

    private int type;
    private int leftImageResId;
    private String title;
    private String summary;
    private String rightText;
    private boolean isEnabled;
    private int status;
    private int tagImageResId = -1;

    public IpcManageBean(int type, int leftImageResId, String title, String rightText) {
        this.type = type;
        this.leftImageResId = leftImageResId;
        this.title = title;
        this.rightText = rightText;
    }

    public IpcManageBean(int type, int leftImageResId, String title, String summary, String rightText, boolean isEnabled) {
        this.type = type;
        this.leftImageResId = leftImageResId;
        this.title = title;
        this.summary = summary;
        this.rightText = rightText;
        this.isEnabled = isEnabled;
    }

    public int getType() {
        return type;
    }

    public int getLeftImageResId() {
        return leftImageResId;
    }

    public void setLeftImageResId(int leftImageResId) {
        this.leftImageResId = leftImageResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTagImageResId() {
        return tagImageResId;
    }

    public void setTagImageResId(int tagImageResId) {
        this.tagImageResId = tagImageResId;
    }
}

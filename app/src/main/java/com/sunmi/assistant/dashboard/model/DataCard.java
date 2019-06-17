package com.sunmi.assistant.dashboard.model;

/**
 * 小卡片数据
 *
 * @author yinhui
 * @since 2019-06-13
 */
public class DataCard {
    public String title;
    public String data;
    public String trendName;
    public float trendData;

    public DataCard(String title, String data, String trendName, float trendData) {
        this.title = title;
        this.data = data;
        this.trendName = trendName;
        this.trendData = trendData;
    }
}

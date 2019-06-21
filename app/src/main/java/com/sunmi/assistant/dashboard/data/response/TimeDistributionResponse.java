package com.sunmi.assistant.dashboard.data.response;

import java.util.List;

public class TimeDistributionResponse {

    private List<TimeSpanItem> order_list;

    public List<TimeSpanItem> getOrder_list() {
        return order_list;
    }

    public static class TimeSpanItem {

        private long time;
        private int count;
        private float amount;

        public long getTime() {
            return time;
        }

        public int getCount() {
            return count;
        }

        public float getAmount() {
            return amount;
        }
    }
}

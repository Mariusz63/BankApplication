package com.example.myapplication.Models;

public class Location {
        private long historyId;
        private long userId;
        private String loginTime;
        private String loginLocation;

    public Location() {
    }

    public Location(long historyId, long userId, String loginTime, String loginLocation) {
        this.historyId = historyId;
        this.userId = userId;
        this.loginTime = loginTime;
        this.loginLocation = loginLocation;
    }

    public long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(long historyId) {
        this.historyId = historyId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation) {
        this.loginLocation = loginLocation;
    }

    @Override
    public String toString() {
        return "Location{" +
                "historyId=" + historyId +
                ", userId=" + userId +
                ", loginTime='" + loginTime + '\'' +
                ", loginLocation='" + loginLocation + '\'' +
                '}';
    }
}

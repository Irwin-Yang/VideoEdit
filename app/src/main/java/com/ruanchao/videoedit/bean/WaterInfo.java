package com.ruanchao.videoedit.bean;

public class WaterInfo {

    private String waterPath;
    private double startTime = 0;
    private double endTime = 10;
    private String xPosition = "10";
    private String yPosition = "10";

    public String getWaterPath() {
        return waterPath;
    }

    public void setWaterPath(String waterPath) {
        this.waterPath = waterPath;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public String getxPosition() {
        return xPosition;
    }

    public void setxPosition(String xPosition) {
        this.xPosition = xPosition;
    }

    public String getyPosition() {
        return yPosition;
    }

    public void setyPosition(String yPosition) {
        this.yPosition = yPosition;
    }
}

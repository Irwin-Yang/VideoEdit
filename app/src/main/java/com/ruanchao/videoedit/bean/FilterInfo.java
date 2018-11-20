package com.ruanchao.videoedit.bean;

public class FilterInfo {

    private int filterID;
    private String filterName;
    private int filterImage;
    private boolean filterStateChecked;

    public int getFilterID() {
        return filterID;
    }

    public void setFilterID(int filterID) {
        this.filterID = filterID;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public int getFilterImage() {
        return filterImage;
    }

    public void setFilterImage(int filterImage) {
        this.filterImage = filterImage;
    }

    public boolean isFilterStateChecked() {
        return filterStateChecked;
    }

    public void setFilterStateChecked(boolean filterStateChecked) {
        this.filterStateChecked = filterStateChecked;
    }
}

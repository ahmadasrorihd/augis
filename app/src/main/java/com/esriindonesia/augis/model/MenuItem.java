package com.esriindonesia.augis.model;

public class MenuItem {
    private String title;
    private int imageResId;
    private Class<?> activityClass;

    public MenuItem(String title, int imageResId, Class<?> activityClass) {
        this.title = title;
        this.imageResId = imageResId;
        this.activityClass = activityClass;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public Class<?> getActivityClass() {
        return activityClass;
    }
}

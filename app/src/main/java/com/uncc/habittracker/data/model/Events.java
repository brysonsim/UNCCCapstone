package com.uncc.habittracker.data.model;

import java.util.ArrayList;

public class Events {
    private String createdBy, description, habitTypeId, location;
    private ArrayList<String> goingList, tags;

    public Events(String createdBy, String description) {
        this.createdBy = createdBy;
        this.description = getDescription();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHabitTypeId() {
        return habitTypeId;
    }

    public void setHabitTypeId(String habitTypeId) {
        this.habitTypeId = habitTypeId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<String> getGoingList() {
        return goingList;
    }

    public void setGoingList(ArrayList<String> goingList) {
        this.goingList = goingList;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
}

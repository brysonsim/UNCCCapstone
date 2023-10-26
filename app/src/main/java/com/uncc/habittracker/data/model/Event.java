package com.uncc.habittracker.data.model;

import com.google.firebase.Timestamp;

public class Event {
    public com.google.firebase.Timestamp createdAt;
    public String description, docId, habitType, ownerId, ownerName, title;

    public Event() {
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getHabitType() {
        return habitType;
    }

    public void setHabitType(String habitType) {
        this.habitType = habitType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;

    }

    @Override
    public String toString() {
        return "Event{" +
                "createdAt=" + createdAt +
                ", description='" + description + '\'' +
                ", docId='" + docId + '\'' +
                ", habitType='" + habitType + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}

package com.uncc.habittracker.data.model;

import java.io.Serializable;
import com.google.firebase.Timestamp;

public class UserHabit implements Serializable {
    public Timestamp createdAt;
    public int progress, progressSecondary;
    public String frequency, habitTypeID, nameOverride, userId;

    public UserHabit() {
        this.progressSecondary = -1;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgressSecondary() {
        return progressSecondary;
    }

    public void setProgressSecondary(int progressSecondary) {
        this.progressSecondary = progressSecondary;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getHabitTypeID() {
        return habitTypeID;
    }

    public void setHabitTypeID(String habitTypeID) {
        this.habitTypeID = habitTypeID;
    }

    public String getNameOverride() {
        return nameOverride;
    }

    public void setNameOverride(String nameOverride) {
        this.nameOverride = nameOverride;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}

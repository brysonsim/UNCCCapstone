package com.uncc.habittracker.data.model;

import java.io.Serializable;
import com.google.firebase.Timestamp;

public class UserHabit implements Serializable {
    public Timestamp createdAt;
    public int progress, progressSecondary;
    public String frequency, habitTypeId, nameOverride, userId;

    public UserHabit() {

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

    public String getHabitTypeId() {
        return habitTypeId;
    }

    public void setHabitTypeId(String habitTypeId) {
        this.habitTypeId = habitTypeId;
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

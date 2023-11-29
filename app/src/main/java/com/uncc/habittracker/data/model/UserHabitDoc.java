package com.uncc.habittracker.data.model;

public class UserHabitDoc extends UserHabit {
    public String docId;
    public boolean isEligible;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public boolean isEligible() {
        return isEligible;
    }

    public void setEligible(boolean eligible) {
        isEligible = eligible;
    }
}

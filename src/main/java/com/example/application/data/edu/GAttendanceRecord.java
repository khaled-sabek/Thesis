package com.example.application.data.edu;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.Entity;

@Entity
public class GAttendanceRecord extends AbstractEntity {

    private String studentEmail;
    private String classroomEmail;
    private String date;
    private boolean present;

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getClassroomEmail() {
        return classroomEmail;
    }

    public void setClassroomEmail(String classroomEmail) {
        this.classroomEmail = classroomEmail;
    }

    public String getDate() {
        return date;
    }

    public void setAttendanceDate(String date) {
        this.date = date;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}

package com.example.application.data.edu;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Entity
public class GClassroom extends AbstractEntity {

    @NotEmpty
    private String classroomName;

    private String classroomDescription;

    private String classroomDepartment;


    private int numberOfStudents;


    private String gClassroomEmail;

    @OneToMany(mappedBy = "gClassroom")
    private List<GStudies> gStudies;


    private String ownerUserEmail;

    private int numberOfAssignments;



    private String attendanceResponsibility;

    private String GID;

    public String getGID() {
        return GID;
    }

    public void setGID(String GID) {
        this.GID = GID;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }



    public void setClassroomDescription(String classroomDescription) {
        this.classroomDescription = classroomDescription;
    }

    public void setClassroomDepartment(String classroomDepartment) {
        this.classroomDepartment = classroomDepartment;
    }
    public String getClassroomDescription() {
        return classroomDescription;
    }
    public String getClassroomDepartment() {
        return classroomDepartment;
    }


    public String getClassroomName() {
        return classroomName;
    }

    public int getNumberOfStudents() {
        return numberOfStudents;
    }



    public int getNumberOfAssignments() {
        return numberOfAssignments;
    }


    public void setNumberOfStudents(int size) {
        this.numberOfStudents = size;
    }

    public void setOwnerUserEmail(String usersEmail) {
        this.ownerUserEmail = usersEmail;
    }

    public String getOwnerUserEmail() {
        return ownerUserEmail;
    }

    public void setgClassroomEmail(String courseGroupEmail) {
        this.gClassroomEmail = courseGroupEmail;
    }

    public String getgClassroomEmail() {
        return gClassroomEmail;
    }

    public String getAttendanceResponsibility() {
        return attendanceResponsibility;
    }

    public void setAttendanceResponsibility(String attendanceResponsibility) {
        this.attendanceResponsibility = attendanceResponsibility;
    }
}

package com.example.application.data.edu;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class GStudies extends AbstractEntity {
    @ManyToOne
    private GStudent gStudent;

    @ManyToOne
    private GClassroom gClassroom;

    private String gClassroomEmail;

    private String gStudentEmail;

    private String ownerUserEmail;

    public void setOwnerUserEmail(String usersEmail) {
        this.ownerUserEmail = usersEmail;
    }

    public void setgClassroomEmail(String name) {
        this.gClassroomEmail = name;
    }

    public String getgClassroomEmail() {
        return gClassroomEmail;
    }

    public void setgStudentEmail(String name) {
        this.gStudentEmail = name;
    }
    public String getgStudentEmail() {
        return gStudentEmail;
    }


}

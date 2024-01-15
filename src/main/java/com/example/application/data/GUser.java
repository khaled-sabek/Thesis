package com.example.application.data;

import jakarta.persistence.Entity;

import java.util.List;

@Entity
public class GUser extends AbstractEntity{

    private String firstName;
    private String lastName;
    private String email;

    private int numOfClassrooms;

    private int numberOfStudents;

    private List<String> classroomEmails;

    private List<String> studentEmails;
    public GUser(String firstName, String lastName, String email){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public GUser() {

    }

    public int getNumOfClasses() {

        return numOfClassrooms;
    }

    public int getNumberOfStudents() {
        return numberOfStudents;
    }
}

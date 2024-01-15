package com.example.application.data.edu;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;

import java.util.ArrayList;
import java.util.List;

@Entity
public class GStudent extends AbstractEntity {


    private String firstName;

    private String lastName;

    private String GID;
    @Email
    private String email;

    private String picture;

    private String ownerUserEmail;

    private int numOfClassrooms;

    private String homeroom;

    private String yearGroup;

    @OneToMany(mappedBy = "gStudent")
    private List<GStudies> gStudies = new ArrayList<GStudies>();


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }


    public int getNumOfClassrooms() {
        //numOfClassrooms= gStudies.size();
        return numOfClassrooms;
    }


    public void setNumOfClassrooms(int numOfClassrooms) {
        this.numOfClassrooms = numOfClassrooms;
    }


    public void setOwnerUserEmail(String ownerUserEmail) {
        this.ownerUserEmail = ownerUserEmail;
    }

    public String getHomeroom() {
        return homeroom;
    }

    public void setHomeroom(String homeroom) {
        this.homeroom = homeroom;
    }

    public String getYearGroup() {
        return yearGroup;
    }

    public void setYearGroup(String yearGroup) {
        this.yearGroup = yearGroup;
    }

    public String getGID() {
        return GID;
    }

    public void setGID(String GID) {
        this.GID = GID;
    }


}

package com.example.application.data.edu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface GStudiesRepository extends JpaRepository<GStudies, Long> {


    @Query("select s from GStudies s " +
            "where s.gStudentEmail = :studentEntity " +
            "and s.gClassroomEmail = :courseEntity")
    GStudies findBygClassroomAndgStudent(String courseEntity, String studentEntity);

    @Query("select s from GStudies s " +
            "where s.gStudentEmail = :usersEmail " +
            "and s.gClassroomEmail = :courseEntity")
    GStudies findBygClassroomAndOwnerUserEmail(String courseEntity, String usersEmail);
}

package com.example.application.data.edu;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
public interface GStudentRepository extends JpaRepository<GStudent, Long> {






    @Query("select s from GStudent s " +
            "where lower(s.firstName) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(s.lastName) like lower(concat('%', :searchTerm, '%'))" +
            "and lower(s.email) like lower(concat('%', :userEmail, '%'))")
    List<GStudent> search(@Param("searchTerm") String searchTerm,String userEmail);
    //ordering doesnt matter because the grid will sort it anyway





    @Query("select count(s) from GStudies s " +
            "where lower(s.gStudentEmail) like lower(concat('%', :studentEmail, '%')) ")
    int numberOfClassesForStudent(@Param("studentEmail") String studentEmail);



    @Query("select s from GStudent s join GStudies g on s.email=g.gStudentEmail " +
            "where lower(g.gClassroomEmail) like lower(concat('%', :classroomEmail, '%')) ")
    List<GStudent> findBygClassroom(String classroomEmail);

    @Query("Select s from GStudent s " +
            "where lower(s.yearGroup) like lower(concat('%', :year, '%')) ")
    List<GStudent> getYeargroup(String year);

    @Query("Select s from GStudent s " +
            "where lower(s.yearGroup) like lower(concat('%', :year, '%')) " +
            "and lower(s.firstName) like lower(concat('%', :filter, '%')) ")
    List<GStudent> getYeargroupFiltered(String filter,String year);

    @Query("Select s from GStudent s " +
            "where lower(s.yearGroup) like lower(concat('%', :number, '%')) " +
            "or lower(s.yearGroup) like lower(concat('%', :number1, '%'))" +
            "or lower(s.yearGroup) like lower(concat('%', :number2, '%'))")
    List<GStudent> getSchoolSection( String number, String number1, String number2);
    @Query("Select s from GStudent s " +
            "where lower(s.yearGroup) like lower(concat('%', :number, '%')) " +
            "or lower(s.yearGroup) like lower(concat('%', :number1, '%'))" +
            "or lower(s.yearGroup) like lower(concat('%', :number2, '%'))"+
            "and lower(s.firstName) like lower(concat('%', :filter, '%')) ")
    List<GStudent> getSchoolSectionFiltered(String filter, String number, String number1, String number2);


}

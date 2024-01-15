package com.example.application.data.edu;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GClassroomRepository extends JpaRepository<GClassroom, Long> {

    @Modifying
    @Transactional
    @Query("Delete FROM GClassroom g WHERE g.ownerUserEmail =:usersEmail")
    void deleteAll(String usersEmail);

    @Query("select c from GClassroom c " +
            "where c.ownerUserEmail = :userEmail")
    List<GClassroom> findAll(@Param("userEmail") String userEmail);



    @Query("select c from GClassroom c " +
            "where lower(c.classroomName) like lower(concat('%', :searchTerm, '%'))" )
    List<GClassroom> search(@Param("searchTerm") String searchTerm);




    @Query("select c from GClassroom c " +
            "where lower(c.gClassroomEmail) like lower(concat('%', :email, '%')) " )
    GClassroom findByGClassroomEmail(@Param("email")String email);


    @Query("select c from GClassroom c " +
            "where lower(c.classroomName) like lower(concat('%', :year, '%')) " )
    List<GClassroom> getYear(String year);

    @Query("select c from GClassroom c " +
            "where lower(c.classroomName) like lower(concat('%', :year, '%'))" +
            "and  lower(c.classroomName) like lower(concat('%', :filter, '%')) " )
    List<GClassroom> getYearFiltered(String filter,String year);


    @Query("select c from GClassroom c " +
            "where lower(c.classroomName) like lower(concat('%', :kg, '%')) " )
    List<GClassroom> getKG(String kg);

    @Query("select c from GClassroom c " +
            "where lower(c.classroomName) like lower(concat('%', :kg, '%'))" +
            "and   lower(c.classroomName) like lower(concat('%', :filter, '%'))" )
    List<GClassroom> getKGFiltered(String filter, String kg);

    @Query("select c from GClassroom c " +
            "where lower(c.classroomName) like lower(concat('%', :number, '%'))" +
            "or  lower(c.classroomName) like lower(concat('%', :number1, '%'))" +
            "or  lower(c.classroomName) like lower(concat('%', :number2, '%'))" )
    List<GClassroom> getSchoolSection(String number, String number1, String number2);

    @Query("select c from GClassroom c " +
            "where lower(c.classroomName) like lower(concat('%', :number, '%'))" +
            "or  lower(c.classroomName) like lower(concat('%', :number1, '%'))" +
            "or  lower(c.classroomName) like lower(concat('%', :number2, '%'))" +
            "and  lower(c.classroomName) like lower(concat('%', :filter, '%'))" )
    List<GClassroom> getSchoolSectionFiltered(String filter, String number, String number1, String number2);



    @Query("SELECT c FROM GClassroom c " +
            "WHERE lower(c.attendanceResponsibility) like lower(:usersEmail)")
    List<GClassroom>    findClassroomWithAttendancePermission(String usersEmail);

    @Query("SELECT c FROM GClassroom c " +
            "WHERE lower(c.gClassroomEmail) like lower(:classroomEmail)")
    GClassroom findBygClassroomEmail(String classroomEmail);
}

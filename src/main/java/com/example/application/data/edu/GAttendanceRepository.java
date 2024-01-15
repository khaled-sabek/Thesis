package com.example.application.data.edu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface GAttendanceRepository extends JpaRepository<GAttendanceRecord, Long> {

    @Query("select count(a) from GAttendanceRecord a " +
            "where lower(a.classroomEmail) like lower(concat('%', :classroomEmail, '%')) " +
            "and lower(a.studentEmail) like lower(concat('%', :studentEmail, '%'))" +
            "and lower(a.date) like lower(concat('%', :date, '%')) ")
    boolean attendanceOnDateForStudent(String date, String classroomEmail, String studentEmail);

    @Query("select a from GAttendanceRecord a " +
            "where lower(a.classroomEmail) like lower(concat('%', :classroomEmail, '%')) " +
            "and lower(a.date) like lower(concat('%', :date, '%')) ")
    List<GAttendanceRecord> attendanceOnDateForClassroom(String date, String classroomEmail);

    @Query("select a from GAttendanceRecord a " +
            "where a.studentEmail = :studentEmail " +
            "and a.date = :date")
    List<GAttendanceRecord> getPresentToday(String date, String studentEmail);
}

package com.example.application.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GUserRepository extends CrudRepository<GUser, Integer> {

    @Query("select u from GUser u " +
            "where lower(u.email) like lower(concat('%', :email, '%')) ")
    GUser findByEmail(String email);


}

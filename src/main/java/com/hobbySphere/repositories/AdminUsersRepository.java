package com.hobbySphere.repositories;

import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Businesses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AdminUsersRepository extends JpaRepository<AdminUsers, Long> {

    
    Optional<AdminUsers> findByUsername(String username);
    
    Optional<AdminUsers> findByEmail(String email);
    
    Optional<AdminUsers> findByUsernameOrEmail(String username, String email);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM AdminUsers au WHERE au.business.id = :businessId")
    void deleteByBusinessId(@Param("businessId") Long businessId);
    
    List<AdminUsers> findByEmailAndBusiness(String email, Businesses business);


}

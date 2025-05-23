package com.hobbySphere.repositories;

import com.hobbySphere.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // ✅ Corrected import from Guava to Java standard

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findById(Long id); // ✅ Optional is from java.util

    Users findByEmail(String email);

    Users findByUsername(String username);

    // ✅ Correct method for createdAt field
    long countByCreatedAtAfter(LocalDateTime date);

    @Query(value = """
    	    SELECT TO_CHAR(u.createdAt, 'YYYY-MM') AS month, COUNT(u) 
    	    FROM Users u 
    	    WHERE u.createdAt >= :startDate 
    	    GROUP BY TO_CHAR(u.createdAt, 'YYYY-MM') 
    	    ORDER BY TO_CHAR(u.createdAt, 'YYYY-MM')
    	""")
    	List<Object[]> countMonthlyRegistrations(@Param("startDate") LocalDateTime startDate);


}

package com.hobbySphere.repositories;

import com.hobbySphere.entities.Interests;
import com.hobbySphere.entities.UserInterests;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.hobbySphere.entities.UserInterests.UserInterestId;
import com.hobbySphere.entities.Users;

@Repository
public interface UserInterestsRepository extends JpaRepository<UserInterests, UserInterestId> {

	 @Query("SELECT ui.interest FROM UserInterests ui WHERE ui.id.user.id = :userId")
	    List<Interests> findInterestsByUserId(@Param("userId") Long userId);

	 List<UserInterests> findByInterestIdIn(List<Long> myInterestIds);

	 List<UserInterests> findById_User_Id(Long userId);

	 
	
}


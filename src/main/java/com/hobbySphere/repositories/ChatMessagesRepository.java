package com.hobbySphere.repositories;

import com.hobbySphere.entities.ChatMessages;
import com.hobbySphere.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {

   

	List<ChatMessages> findBySenderAndReceiverOrReceiverAndSenderOrderByMessageDatetimeAsc(Users user1, Users user2,
			Users user22, Users user12);
}

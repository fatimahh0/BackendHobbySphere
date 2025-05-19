package com.hobbySphere.repositories;

import com.hobbySphere.entities.ChatMessages;
import com.hobbySphere.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {

    // ✅ Conversation between two users
	@Query("SELECT m FROM ChatMessages m WHERE " +
		       "(m.sender.id = :id1 AND m.receiver.id = :id2) OR " +
		       "(m.sender.id = :id2 AND m.receiver.id = :id1) " +
		       "ORDER BY m.sentAt ASC")
		List<ChatMessages> findConversationBetween(@Param("id1") Long id1, @Param("id2") Long id2);


    // ✅ All messages where user is sender or receiver
    List<ChatMessages> findBySenderOrReceiver(Users sender, Users receiver);

	List<ChatMessages> findBySenderAndReceiverOrReceiverAndSenderOrderByMessageDatetimeAsc(Users user1, Users user2,
			Users user22, Users user12);

	List<ChatMessages> findBySenderOrReceiverOrderByMessageDatetimeDesc(Users user, Users user2);
	
}

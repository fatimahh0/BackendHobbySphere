package com.hobbySphere.repositories;

import com.hobbySphere.entities.ChatMessages;
import com.hobbySphere.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {

    // ✅ Conversation between two users
    @Query("SELECT m FROM ChatMessages m WHERE " +
           "(m.sender.id = :user1 AND m.receiver.id = :user2) OR " +
           "(m.sender.id = :user2 AND m.receiver.id = :user1) " +
           "ORDER BY m.sentAt ASC")
    List<ChatMessages> findConversation(Long user1, Long user2);

    // ✅ All messages where user is sender or receiver
    List<ChatMessages> findBySenderOrReceiver(Users sender, Users receiver);

	List<ChatMessages> findBySenderAndReceiverOrReceiverAndSenderOrderByMessageDatetimeAsc(Users user1, Users user2,
			Users user22, Users user12);

	List<ChatMessages> findBySenderOrReceiverOrderByMessageDatetimeDesc(Users user, Users user2);
	
}

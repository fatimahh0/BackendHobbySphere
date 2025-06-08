package com.hobbySphere.services;

import com.hobbySphere.entities.Friendship;
import com.hobbySphere.entities.Users;
import com.hobbySphere.enums.NotificationType;
import com.hobbySphere.repositories.FriendshipRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepo;
    private final NotificationsService notificationsService;

    public FriendshipService(FriendshipRepository friendshipRepo,
                             NotificationsService notificationsService) {
        this.friendshipRepo = friendshipRepo;
        this.notificationsService = notificationsService;
    }

    public Friendship sendFriendRequest(Users sender, Users receiver) {
        if (sender.equals(receiver)) {
            throw new RuntimeException("You cannot add yourself.");
        }

        boolean exists = friendshipRepo.existsByUserIdAndFriendId(sender.getId(), receiver.getId());
        if (exists) {
            throw new RuntimeException("Friend request already exists.");
        }

        Friendship friendship = new Friendship();
        friendship.setUser(sender);
        friendship.setFriend(receiver);
        friendship.setStatus("PENDING");

        Friendship saved = friendshipRepo.save(friendship);

        notificationsService.createNotification(
            receiver,
            sender.getUsername() + " sent you a friend request.",
            NotificationType.FRIEND_REQUEST_SENT
        );

        return saved;
    }

    public Friendship acceptRequest(Long requestId, Users receiver) {
        Friendship request = friendshipRepo.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getFriend().getId().equals(receiver.getId())) {
            throw new RuntimeException("Not authorized to accept this request");
        }

        request.setStatus("ACCEPTED");
        Friendship accepted = friendshipRepo.save(request);

        notificationsService.createNotification(
            request.getUser(),
            receiver.getUsername() + " accepted your friend request.",
            NotificationType.FRIEND_REQUEST_ACCEPTED
        );

        return accepted;
    }

    public void rejectRequest(Long requestId, Users receiver) {
        Friendship request = friendshipRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getFriend().getId().equals(receiver.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        request.setStatus("REJECTED");
        friendshipRepo.save(request);

        notificationsService.createNotification(
            request.getUser(),
            receiver.getUsername() + " rejected your friend request.",
            NotificationType.FRIEND_REQUEST_REJECTED
        );
    }

    public void cancelRequest(Users sender, Users receiver) {
        Friendship request = friendshipRepo.findByUserIdAndFriendIdAndStatus(
                sender.getId(), receiver.getId(), "PENDING"
        ).orElseThrow(() -> new RuntimeException("Request not found"));

        friendshipRepo.delete(request);

        notificationsService.createNotification(
            receiver,
            sender.getUsername() + " canceled the friend request.",
            NotificationType.FRIEND_REQUEST_REJECTED
        );
    }

    public void blockUser(Users blocker, Users blocked) {
        Friendship friendship = new Friendship();
        friendship.setUser(blocker);
        friendship.setFriend(blocked);
        friendship.setStatus("BLOCKED");

        friendshipRepo.save(friendship);

        notificationsService.createNotification(
            blocked,
            "You have been blocked by " + blocker.getUsername(),
            NotificationType.FRIEND_BLOCKED
        );
    }

    public void unblockUser(Users blocker, Users blocked) {
        friendshipRepo.findByUserIdAndFriendIdAndStatus(blocker.getId(), blocked.getId(), "BLOCKED")
            .ifPresent(friendshipRepo::delete);
    }

    public void unfriend(Users user1, Users user2) {
        friendshipRepo.findAcceptedFriendship(user1.getId(), user2.getId())
            .ifPresent(friendship -> {
                friendshipRepo.delete(friendship);

                Users other = friendship.getUser().equals(user1) ? friendship.getFriend() : friendship.getUser();

                notificationsService.createNotification(
                    other,
                    user1.getUsername() + " removed you from their friends.",
                    NotificationType.FRIEND_REQUEST_REJECTED
                );
            });
    }

    public List<Friendship> getPendingRequests(Users user) {
        return friendshipRepo.findByFriendIdAndStatus(user.getId(), "PENDING");
    }

    public int getPendingRequestCount(Users user) {
        return friendshipRepo.findByFriendIdAndStatus(user.getId(), "PENDING").size();
    }

    public List<Users> getAcceptedFriends(Users user) {
        List<Friendship> friendships = friendshipRepo.findAcceptedFriendships(user.getId());

        return friendships.stream().map(f -> {
            return f.getUser().getId().equals(user.getId()) ? f.getFriend() : f.getUser();
        }).collect(Collectors.toList());
    }

   
    
    public List<Users> getSentRequests(Users user) {
        List<Friendship> sentRequests = friendshipRepo.findByUserIdAndStatus(user.getId(), "PENDING");

        return sentRequests.stream()
                .map(Friendship::getFriend)  // return receivers
                .collect(Collectors.toList());
    }


} 

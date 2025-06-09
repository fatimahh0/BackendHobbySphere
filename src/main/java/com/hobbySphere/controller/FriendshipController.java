package com.hobbySphere.controller;

import com.hobbySphere.entities.Friendship;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.FriendshipService;
import com.hobbySphere.services.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserService userService;

    public FriendshipController(FriendshipService friendshipService, UserService userService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
    }

    @PostMapping("/add/{friendId}")
    public ResponseEntity<?> sendFriendRequest(@PathVariable Long friendId, Principal principal) {
        try {
            Users sender = userService.getUserByEmaill(principal.getName());
            Users receiver = userService.getUserById(friendId);
            Friendship friendship = friendshipService.sendFriendRequest(sender, receiver);
            return ResponseEntity.ok(friendship);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, Principal principal) {
        try {
            Users receiver = userService.getUserByEmaill(principal.getName());
            Friendship accepted = friendshipService.acceptRequest(requestId, receiver);
            return ResponseEntity.ok(accepted);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(Principal principal) {
        try {
            Users user = userService.getUserByEmaill(principal.getName());
            return ResponseEntity.ok(friendshipService.getPendingRequests(user));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }

    @GetMapping("/pending/count")
    public ResponseEntity<?> getPendingRequestCount(Principal principal) {
        try {
            Users user = userService.getUserByEmaill(principal.getName());
            return ResponseEntity.ok(friendshipService.getPendingRequestCount(user));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyFriends(Principal principal) {
        try {
            Users user = userService.getUserByEmaill(principal.getName());
            return ResponseEntity.ok(friendshipService.getAcceptedFriends(user));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }
    
  
    @GetMapping("/sent")
    public ResponseEntity<?> getSentRequests(Principal principal) {
        try {
            Users user = userService.getUserByEmaill(principal.getName());
            return ResponseEntity.ok(friendshipService.getSentRequests(user));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }



    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long requestId, Principal principal) {
        try {
            Users receiver = userService.getUserByEmaill(principal.getName());
            friendshipService.rejectRequest(requestId, receiver);
            return ResponseEntity.ok(Collections.singletonMap("message", "Request rejected."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/cancel/{friendId}")
    public ResponseEntity<?> cancelFriendRequest(@PathVariable Long friendId, Principal principal) {
        try {
            Users sender = userService.getUserByEmaill(principal.getName());
            Users receiver = userService.getUserById(friendId);
            friendshipService.cancelRequest(sender, receiver);
            return ResponseEntity.ok(Collections.singletonMap("message", "Request cancelled."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }

    @PostMapping("/block/{userId}")
    public ResponseEntity<?> blockUser(@PathVariable Long userId, Principal principal) {
        try {
            Users blocker = userService.getUserByEmaill(principal.getName());
            Users blocked = userService.getUserById(userId);
            friendshipService.blockUser(blocker, blocked);
            return ResponseEntity.ok(Collections.singletonMap("message", "User blocked."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/unblock/{userId}")
    public ResponseEntity<?> unblockUser(@PathVariable Long userId, Principal principal) {
        try {
            Users blocker = userService.getUserByEmaill(principal.getName());
            Users blocked = userService.getUserById(userId);
            friendshipService.unblockUser(blocker, blocked);
            return ResponseEntity.ok(Collections.singletonMap("message", "User unblocked."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/unfriend/{userId}")
    public ResponseEntity<?> unfriend(@PathVariable Long userId, Principal principal) {
        try {
            Users currentUser = userService.getUserByEmaill(principal.getName());
            Users friend = userService.getUserById(userId);
            friendshipService.unfriend(currentUser, friend);
            return ResponseEntity.ok(Collections.singletonMap("message", "User unfriended."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }
    
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getFriendshipStatus(@PathVariable Long userId, Principal principal) {
        try {
            Users currentUser = userService.getUserByEmaill(principal.getName());
            Users otherUser = userService.getUserById(userId);

            boolean youBlockedThem = friendshipService.didBlock(currentUser, otherUser);
            boolean theyBlockedYou = friendshipService.didBlock(otherUser, currentUser);
            boolean isFriend = friendshipService.areFriends(currentUser, otherUser);

            return ResponseEntity.ok(
                Map.of(
                    "youBlockedThem", youBlockedThem,
                    "theyBlockedYou", theyBlockedYou,
                    "isFriend", isFriend
                )
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
        }
    }



}

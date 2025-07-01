package com.hobbySphere.services;

import com.hobbySphere.dto.PostDto;
import com.hobbySphere.entities.Posts;
import com.hobbySphere.entities.Users;
import com.hobbySphere.entities.PostVisibility;
import com.hobbySphere.entities.UserStatus;

import com.hobbySphere.repositories.PostsRepository;
import com.hobbySphere.repositories.PostVisibilityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostsRepository postsRepository;
    private final PostVisibilityRepository postVisibilityRepository;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    public PostService(PostsRepository postsRepository, PostVisibilityRepository postVisibilityRepository) {
        this.postsRepository = postsRepository;
        this.postVisibilityRepository = postVisibilityRepository;
    }

    /**
     * Create a new post with optional image and visibility level.
     */
    public Posts createPost(String content, MultipartFile image, String hashtags, Users user, PostVisibility visibility) {
        String imageUrl = null;

        // Upload image if provided
        if (image != null && !image.isEmpty()) {
            try {
                String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(filename);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                imageUrl = "/uploads/" + filename;
            } catch (Exception e) {
                throw new RuntimeException("Error uploading image", e);
            }
        }

        // Save post with visibility
        Posts post = new Posts(user, content, imageUrl, hashtags);
        post.setVisibility(visibility);
        return postsRepository.save(post);
    }

    /**
     * Delete a post only if it belongs to the user.
     */
    public void deletePost(Long postId, Users user) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this post");
        }

        postsRepository.delete(post);
    }

    /**
     * Return all visible posts for the current user, respecting visibility rules.
     */
    @Transactional
    public List<PostDto> getAllPostDtos(Long currentUserId) {
        System.out.println("👉 Fetching posts for user: " + currentUserId);

        List<Posts> posts = postsRepository.findAll();

        return posts.stream()
            .filter(post -> {
                try {
                    Users poster = post.getUser();

                    // ✅ Skip if user is not active
                    if (poster.getStatus() == null || !"ACTIVE".equals(poster.getStatus().getName()))
                        return false;

                    // ✅ Show own posts
                    if (poster.getId().equals(currentUserId))
                        return true;

                    String visibilityName = post.getVisibility() != null ? post.getVisibility().getName() : "PUBLIC";

                    // ✅ Show public posts
                    if ("PUBLIC".equalsIgnoreCase(visibilityName))
                        return true;

                    // ✅ Show FRIENDS_ONLY posts if they are friends
                    if ("FRIENDS_ONLY".equalsIgnoreCase(visibilityName)
                            && areFriends(currentUserId, poster.getId())) {
                        return true;
                    }

                    return false;

                } catch (Exception e) {
                    System.err.println("❌ Error in filter logic: " + e.getMessage());
                    return false;
                }
            })
            .map(post -> new PostDto(post, currentUserId))
            .collect(Collectors.toList());
    }

    /**
     * Check if two users are friends.
     */
    private boolean areFriends(Long viewerId, Long posterId) {
        Users viewer = new Users();
        viewer.setId(viewerId);

        Users poster = new Users();
        poster.setId(posterId);

        return friendshipService.areFriends(viewer, poster);
    }

    /**
     * Delete a post if it belongs to the specified user ID.
     */
    public boolean deletePostByUser(Long postId, Long userId) {
        Optional<Posts> post = postsRepository.findById(postId);
        if (post.isPresent() && post.get().getUser().getId().equals(userId)) {
            postsRepository.delete(post.get());
            return true;
        }
        return false;
    }

    /**
     * Get posts by specific user (no visibility filtering here).
     */
    @Transactional
    public List<PostDto> getPostDtosByUser(Long userId) {
        List<Posts> posts = postsRepository.findByUserId(userId);
        return posts.stream()
                .map(post -> new PostDto(post, userId))
                .collect(Collectors.toList());
    }
}

package com.hobbySphere.services;

import com.hobbySphere.dto.PostDto;
import com.hobbySphere.entities.Posts;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.PostsRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import com.hobbySphere.services.PostService; 
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostsRepository postsRepository;

    public PostService(PostsRepository postsRepository) {
        this.postsRepository = postsRepository;
    }

    public Posts createPost(String content, MultipartFile image, String hashtags, Users user) {
        String imageUrl = null;
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
        Posts post = new Posts(user, content, imageUrl, hashtags);
        return postsRepository.save(post);
    }

    public void deletePost(Long postId, Users user) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this post");
        }
        postsRepository.delete(post);
    }

    @Transactional // ✅ Keeps Hibernate session open
    public List<PostDto> getAllPostDtos(Long currentUserId) {
        List<Posts> posts = postsRepository.findAll();
        return posts.stream()
                .map(post -> new PostDto(post, currentUserId))
                .collect(Collectors.toList());
    }

  




    public boolean deletePostByUser(Long postId, Long userId) {
        Optional<Posts> post = postsRepository.findById(postId);
        if (post.isPresent() && post.get().getUser().getId().equals(userId)) {
            postsRepository.delete(post.get());
            return true;
        }
        return false;
    }

    @Transactional
    public List<PostDto> getPostDtosByUser(Long userId) {
        List<Posts> posts = postsRepository.findByUserId(userId);
        return posts.stream()
                    .map(post -> new PostDto(post, userId))
                    .collect(Collectors.toList());
    }



}
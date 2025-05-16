package com.hobbySphere.services;

import com.hobbySphere.entities.Posts;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.PostsRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;

import java.util.List;

@Service
public class PostService {

    private final PostsRepository postsRepository;

    public PostService(PostsRepository postsRepository) {
        this.postsRepository = postsRepository;
    }

    public Posts createPost(String content, MultipartFile image, String hashtags, Users user) {
        String imageUrl = null;

        // create post with or without image
        if (image != null && !image.isEmpty()) {
            try {
                String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                Path uploadPath = Paths.get("src/main/resources/static/uploads");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(filename);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                imageUrl = "/uploads/" + filename;
            } catch (Exception e) {
                throw new RuntimeException("Error upload image", e);
            }
        }

        Posts post = new Posts(user, content, imageUrl, hashtags);
        return postsRepository.save(post);
    }

    public List<Posts> getAllPosts() {
        return postsRepository.findAll();
    }

   
    
    public void deletePost(Long postId, Users user) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Vérifie que l'utilisateur est le propriétaire
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Non autorisé à supprimer ce post");
        }

        postsRepository.delete(post); // ✅ pas userRepository.delete()
    }

}

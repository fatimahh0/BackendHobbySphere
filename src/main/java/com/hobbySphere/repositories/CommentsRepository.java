package com.hobbySphere.repositories;

import com.hobbySphere.entities.Comments;
import com.hobbySphere.entities.Posts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    
    
    List<Comments> findByPost(Posts post);
}

package com.hobbySphere.services;

import com.hobbySphere.entities.Posts;
import java.util.List;

public interface PostService {
  List<Posts> findAll();

  Posts save(Posts post);

  String toggleLike(Long postId, Long userId);

  int countLikes(Long postId);

}

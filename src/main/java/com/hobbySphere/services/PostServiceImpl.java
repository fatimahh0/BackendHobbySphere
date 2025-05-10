package com.hobbySphere.services;

import com.hobbySphere.entities.Posts;
import com.hobbySphere.entities.Users;
import com.hobbySphere.entities.PostLikes;
import com.hobbySphere.repositories.PostsRepository;
import com.hobbySphere.repositories.UsersRepository;
import com.hobbySphere.repositories.PostLikesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

  @Autowired
  private PostsRepository postsRepository;

  @Autowired
  private PostLikesRepository postLikesRepository;

  @Autowired
  private UsersRepository usersRepository;

  @Override
  public List<Posts> findAll() {
    return postsRepository.findAll();
  }

  @Override
  public Posts save(Posts post) {
    return postsRepository.save(post);
  }

  @Override
  public int countLikes(Long postId) {
    return postLikesRepository.countByPostId(postId);
  }

  @Override
  public String toggleLike(Long postId, Long userId) {
    Optional<Posts> postOpt = postsRepository.findById(postId);
    Optional<Users> userOpt = usersRepository.findById(userId);

    if (postOpt.isPresent() && userOpt.isPresent()) {
      Posts post = postOpt.get();
      Users user = userOpt.get();

      Optional<PostLikes> existingLike = postLikesRepository.findByUserAndPost(user, post);

      if (existingLike.isPresent()) {
        // Unlike
        postLikesRepository.delete(existingLike.get());
        return "Unliked";
      } else {
        // Like
        PostLikes newLike = new PostLikes(user, post);
        postLikesRepository.save(newLike);
        return "Liked";
      }
    }
    return "Post or User not found";
  }
}

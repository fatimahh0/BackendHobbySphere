package com.hobbySphere.services;

import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

   

    public Users registerUser(
            String username,
            String firstName,
            String lastName,
            String email,
            String password,
            MultipartFile profileImage
    ) throws IOException {
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }

        String imageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
            Path uploadPath = Paths.get("uploads/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            Files.copy(profileImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            imageUrl = "/uploads/" + filename;
        }

        Users user = new Users();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setProfilePictureUrl(imageUrl);

        return userRepository.save(user);
    }
    
   


    public Users findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Users findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Users save(Users user) {
        return userRepository.save(user);
    }

	public String saveProfileImage(MultipartFile file) throws IOException {
		String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get("uploads");

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        Path fullPath = path.resolve(filename);
        Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + filename; // adjust based on your static file serving
	}

}

package com.hobbySphere.services;

import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.hobbySphere.dto.UserDto;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final EmailService emailService;

    //  Inject EmailService using constructor
    public UserService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    private final Map<String, String> resetCodes = new ConcurrentHashMap<>();

   

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




	public Users getUserByEmaill(String email) {
	    Users user = userRepository.findByEmail(email);  
	    if (user == null) {
	        throw new RuntimeException("Utilisateur non trouvé avec l'email : " + email);
	    }
	    return user;
	}




	public Users getUserById(Long userId) {
	    return userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + userId));
	}
	




	public List<UserDto> getAllUserDtos() {
		 return userRepository.findAll()
                 .stream()
                 .map(UserDto::new)
                 .collect(Collectors.toList());
	}




	public boolean deleteUserById(Long id) {
	    Optional<Users> userOptional = userRepository.findById(id);
	    if (userOptional.isPresent()) {
	        userRepository.deleteById(id);
	        return true;
	    } else {
	        return false;
	    }
	}




	public boolean deleteUserByIdWithPassword(Long id, String inputPassword) {
	    Optional<Users> optionalUser = userRepository.findById(id);
	    if (optionalUser.isEmpty()) {
	        return false;
	    }

	    Users user = optionalUser.get();
	    if (!passwordEncoder.matches(inputPassword, user.getPasswordHash())) {
	        return false;
	    }

	    userRepository.deleteById(id);
	    return true;
	}



	// Step 1: Send reset code by email
    public boolean resetPassword(String email) {
        Users user = userRepository.findByEmail(email);
        if (user == null) return false;

        String code = String.format("%06d", new Random().nextInt(999999)); // 6-digit code
        resetCodes.put(email, code);

        emailService.sendEmail(
            email,
            "Password Reset Code",
            "Your password reset code is: " + code
        );

        return true;
    }

    // Step 2: Verify the reset code
    public boolean verifyResetCode(String email, String code) {
        return resetCodes.containsKey(email) && resetCodes.get(email).equals(code);
    }

    // Step 3: Update the password
    public boolean updatePassword(String email, String code, String newPassword) {
        if (verifyResetCode(email, code)) {
            Users user = userRepository.findByEmail(email);
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            resetCodes.remove(email); // clear used code
            return true;
        }
        return false;
    }




    public boolean updatePasswordDirectly(String email, String newPassword) {
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            return false;
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);
        return true;
    }    
    
}









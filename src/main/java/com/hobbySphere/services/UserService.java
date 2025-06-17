package com.hobbySphere.services;

import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Interests;
import com.hobbySphere.entities.PendingUser;
import com.hobbySphere.entities.UserInterests;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.InterestsRepository;
import com.hobbySphere.repositories.PendingUserRepository;
import com.hobbySphere.repositories.UserInterestsRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserInterestsRepository userInterestsRepository;

    @Autowired
    private InterestsRepository interestsRepository;
    
    @Autowired
    private PendingUserRepository pendingUserRepository;
    
    @Autowired
    private FriendshipService friendshipService;

    
    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private final EmailService emailService;

    //  Inject EmailService using constructor
    public UserService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    public boolean sendVerificationCodeForRegistration(Map<String, String> userData, MultipartFile profileImage) throws IOException {
        String email = userData.get("email");
        String phone = userData.get("phoneNumber");

        String username = userData.get("username");
        String password = userData.get("password");
        String firstName = userData.get("firstName");
        String lastName = userData.get("lastName");

        boolean emailProvided = email != null && !email.trim().isEmpty();
        boolean phoneProvided = phone != null && !phone.trim().isEmpty();

        if (!emailProvided && !phoneProvided) {
            throw new RuntimeException("You must provide either email or phone");
        }
        if (emailProvided && phoneProvided) {
            throw new RuntimeException("You must provide either email or phone, not both");
        }


        if (email != null && (pendingUserRepository.existsByEmail(email) || userRepository.findByEmail(email) != null)) {
            throw new RuntimeException("Email is already in use");
        }

        if (phone != null && (pendingUserRepository.existsByPhoneNumber(phone) || userRepository.findByPhoneNumber(phone) != null)) {
            throw new RuntimeException("Phone number is already in use");
        }

        if (pendingUserRepository.existsByUsername(username) || userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Username is already in use");
        }

        String code = phone != null ? "123456" : String.format("%06d", new Random().nextInt(999999));
        String profileImageUrl = null;

        if (profileImage != null && !profileImage.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
            Path path = Paths.get("uploads");
            if (!Files.exists(path)) Files.createDirectories(path);
            Path fullPath = path.resolve(filename);
            Files.copy(profileImage.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);
            profileImageUrl = "/uploads/" + filename;
        }

        PendingUser pending = new PendingUser();
        pending.setEmail(email);
        pending.setPhoneNumber(phone);
        pending.setUsername(username);
        pending.setPasswordHash(passwordEncoder.encode(password));
        pending.setFirstName(firstName);
        pending.setLastName(lastName);
        pending.setProfilePictureUrl(profileImageUrl);
        pending.setVerificationCode(code);
        pending.setCreatedAt(LocalDateTime.now());

        pendingUserRepository.save(pending);

        if (email != null) {
            String htmlMessage = """
                <html>
                <body style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
                    <h2 style="color: #4CAF50;">Welcome to HobbySphere!</h2>
                    <p style="font-size: 16px;">Hello,</p>
                    <p style="font-size: 16px;">Thank you for registering. Please use the code below to verify your email address:</p>
                    <h1 style="color: #2196F3;">%s</h1>
                    <p style="font-size: 14px; color: #777;">This code will expire in 10 minutes.</p>
                </body>
                </html>
            """.formatted(code);

            emailService.sendHtmlEmail(email, "Email Verification Code", htmlMessage);
        }

        return true;
    }

    public boolean resendVerificationCode(String emailOrPhone) throws IOException {
        PendingUser pending = null;

        if (emailOrPhone.contains("@")) {
            pending = pendingUserRepository.findByEmail(emailOrPhone);
            if (pending == null) throw new RuntimeException("No pending user found with this email");

            String code;
            
                code = String.format("%06d", new Random().nextInt(999999)); // real for email
            

            pending.setVerificationCode(code);
            pending.setCreatedAt(LocalDateTime.now()); // reset timestamp
            pendingUserRepository.save(pending);

            String html = """
                <html>
                <body>
                    <h2>HobbySphere Verification</h2>
                    <p>Your new email verification code is:</p>
                    <h1>%s</h1>
                </body>
                </html>
            """.formatted(code);

            emailService.sendHtmlEmail(emailOrPhone, "New Verification Code", html);
            return true;
        } else {
            pending = pendingUserRepository.findByPhoneNumber(emailOrPhone);
            if (pending == null) throw new RuntimeException("No pending user found with this phone");

            pending.setVerificationCode("123456"); // static again
            pending.setCreatedAt(LocalDateTime.now());
            pendingUserRepository.save(pending);
            return true;
        }
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
    
   
    //user register with number 
    public boolean verifyPhoneCodeAndRegister(String phoneNumber, String code) {
        if (!"123456".equals(code)) {
            return false;
        }

        PendingUser pending = pendingUserRepository.findByPhoneNumber(phoneNumber);
        if (pending == null) {
            return false;
        }

        Users user = new Users();
        user.setPhoneNumber(pending.getPhoneNumber());
        user.setUsername(pending.getUsername());
        user.setPasswordHash(pending.getPasswordHash());
        user.setFirstName(pending.getFirstName());
        user.setLastName(pending.getLastName());
        user.setProfilePictureUrl(pending.getProfilePictureUrl());
        user.setStatus("Active");
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        pendingUserRepository.delete(pending);

        return true;
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




	public Users getUserByEmaill(String identifier) {
		 Users user = null;

	        if (identifier.contains("@")) {
	            user = userRepository.findByEmail(identifier);
	        } else {
	            user = userRepository.findByPhoneNumber(identifier);
	        }

	        if (user == null) {
	            throw new RuntimeException("User not found with: " + identifier);
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
	        Users user = userOptional.get();

	        // 🔁 Remove manager record if user is promoted as a manager
	        Optional<AdminUsers> adminOpt = adminUserService.findByUserEmail(user.getEmail());
	        adminOpt.ifPresent(admin -> adminUserService.deleteManagerById(admin.getAdminId()));

	        // ✅ Now delete the user
	        userRepository.delete(user);
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

		  
		    if (user.getEmail() != null) {
		        List<AdminUsers> admins = adminUserService.findAllByUserEmail(user.getEmail());
		        for (AdminUsers admin : admins) {
		            adminUserService.deleteManagerById(admin.getAdminId());
		        }
		    }

		  
		    userRepository.delete(user);

		    return true;
		}



	// Step 1: Send reset code by email
    public boolean resetPassword(String email) {
        Users user = userRepository.findByEmail(email);
        if (user == null) return false;

        String code = String.format("%06d", new Random().nextInt(999999)); // 6-digit code
        resetCodes.put(email, code);

        String htmlMessage = """
        	    <html>
        	    <body style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
        	        <h2 style="color: #FF9800;">Reset Your Password</h2>
        	        <p style="font-size: 16px;">Hello,</p>
        	        <p style="font-size: 16px;">We received a request to reset your password. Please use the code below to proceed:</p>
        	        <h1 style="color: #2196F3;">%s</h1>
        	        <p style="font-size: 14px; color: #777;">This code will expire in 10 minutes. If you didn't request this, you can safely ignore this email.</p>
        	        <p style="font-size: 14px; color: #999; margin-top: 40px;">— The HobbySphere Team</p>
        	    </body>
        	    </html>
        	""".formatted(code);

        	emailService.sendHtmlEmail(email, "Password Reset Code", htmlMessage);


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
    
    public void addUserInterests(Long userId, List<Long> interestIds) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Long interestId : interestIds) {
            Interests interest = interestsRepository.findById(interestId)
                    .orElseThrow(() -> new RuntimeException("Interest not found"));

            UserInterests.UserInterestId compositeKey = new UserInterests.UserInterestId(user, interest);

            if (!userInterestsRepository.existsById(compositeKey)) {
                UserInterests userInterest = new UserInterests();
                userInterest.setId(compositeKey);
                userInterest.setInterest(interest);
                userInterestsRepository.save(userInterest);
            }
        }
    }

    public boolean verifyEmailCodeAndRegister(String email, String code) {
        PendingUser pending = pendingUserRepository.findByEmail(email);

        if (pending == null || !pending.getVerificationCode().equals(code)) {
            return false;
        }

        // Create actual user
        Users user = new Users();
        user.setEmail(pending.getEmail());
        user.setUsername(pending.getUsername());
        user.setPasswordHash(pending.getPasswordHash());
        user.setFirstName(pending.getFirstName());
        user.setLastName(pending.getLastName());
        user.setStatus("Active");
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        pendingUserRepository.delete(pending); // Clean up

        return true;
    }
    
    public List<Users> getAllUsers(){
    	return userRepository.findAll();
    	}

    public Users findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }
    
    public boolean deleteUserProfileImage(Long userId) {
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String imageUrl = user.getProfilePictureUrl(); // e.g., "/uploads/uuid_filename.jpg"
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String imagePath = imageUrl.replace("/uploads", "uploads"); // adjust path
            Path path = Paths.get(imagePath);
            try {
                Files.deleteIfExists(path); // delete image from disk
            } catch (IOException e) {
                throw new RuntimeException("Error deleting image: " + e.getMessage());
            }
            user.setProfilePictureUrl(null); // clear DB field
            userRepository.save(user);
            return true;
        }

        return false; // no image to delete
    }

    public List<Users> suggestFriendsByInterest(Long userId) {
        Users currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Get current user's interest IDs
        List<Long> myInterestIds = userInterestsRepository.findById_User_Id(userId)
            .stream()
            .map(ui -> ui.getId().getInterest().getId())
            .collect(Collectors.toList());

        if (myInterestIds.isEmpty()) return List.of();

        // Users with shared interests
        List<UserInterests> sharedInterests = userInterestsRepository.findByInterestIdIn(myInterestIds);

        Set<Users> potentialFriends = sharedInterests.stream()
            .map(ui -> ui.getId().getUser())
            .filter(user -> !user.getId().equals(userId))
            .collect(Collectors.toSet());

        // Remove current friends
        List<Users> currentFriends = friendshipService.getAcceptedFriends(currentUser);
        potentialFriends.removeAll(currentFriends);

        // Remove blocked users and pending requests
        return potentialFriends.stream()
            .filter(user -> !friendshipService.didBlock(currentUser, user))
            .filter(user -> !friendshipService.didBlock(user, currentUser))
            .filter(user -> !friendshipService.hasPendingRequestBetween(currentUser, user))
            .toList();
    }

}
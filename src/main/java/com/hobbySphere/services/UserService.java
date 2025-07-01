package com.hobbySphere.services;

import com.hobbySphere.entities.AdminUsers;

import com.hobbySphere.entities.Interests;
import com.hobbySphere.entities.PendingUser;
import com.hobbySphere.entities.UserInterests;
import com.hobbySphere.entities.Users;
import com.hobbySphere.entities.UserStatus;

import com.hobbySphere.repositories.InterestsRepository;
import com.hobbySphere.repositories.UserStatusRepository;
import com.hobbySphere.repositories.PendingUserRepository;
import com.hobbySphere.repositories.UserInterestsRepository;
import com.hobbySphere.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.concurrent.atomic.AtomicBoolean;
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
    private UserStatusRepository userStatusRepository;


    @Autowired
    private final EmailService emailService;

    //  Inject EmailService using constructor
    public UserService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    private UserStatus getStatus(String name) {
        return userStatusRepository.findByName(name.toUpperCase())
            .orElseThrow(() -> new RuntimeException("UserStatus " + name + " not found"));
    }

    
    public boolean sendVerificationCodeForRegistration(Map<String, String> userData) {
        String email = userData.get("email");
        String phone = userData.get("phoneNumber");
        String password = userData.get("password");

        // Default status = PENDING
        UserStatus statusEnum = getStatus("PENDING");

        String statusStr = userData.get("status");
        if (statusStr != null) {
            try {
                statusEnum = getStatus(statusStr.toUpperCase());
            } catch (Exception e) {
                throw new RuntimeException("Invalid status value: " + statusStr);
            }
        }

        boolean emailProvided = email != null && !email.trim().isEmpty();
        boolean phoneProvided = phone != null && !phone.trim().isEmpty();

        if (!emailProvided && !phoneProvided) {
            throw new RuntimeException("You must provide either email or phone.");
        }
        if (emailProvided && phoneProvided) {
            throw new RuntimeException("Provide only one: email or phone, not both.");
        }

        // ✅ Uniqueness checks
        if (emailProvided) {
            Users existing = userRepository.findByEmail(email);
            if (existing != null && !"DELETED".equals(existing.getStatus().getName())) {
                throw new RuntimeException("Email already in use.");
            }
            if (pendingUserRepository.existsByEmail(email)) {
                throw new RuntimeException("Email is already pending verification");
            }
        }

        if (phoneProvided) {
            Users existing = userRepository.findByPhoneNumber(phone);
            if (existing != null && !"DELETED".equals(existing.getStatus().getName())) {
                throw new RuntimeException("Phone number already in use.");
            }
            if (pendingUserRepository.existsByPhoneNumber(phone)) {
                throw new RuntimeException("Phone number is already pending verification");
            }
        }

        // 🔐 Generate verification code
        String code = phoneProvided ? "123456" : String.format("%06d", new Random().nextInt(999999));

        // 📥 Create PendingUser
        PendingUser pending = new PendingUser();
        pending.setEmail(email);
        pending.setPhoneNumber(phone);
        pending.setPasswordHash(passwordEncoder.encode(password));
        pending.setVerificationCode(code);
        pending.setCreatedAt(LocalDateTime.now());
        pending.setStatus(statusEnum);
        pending.setIsPublicProfile(true); // default

        pendingUserRepository.save(pending);

        // 📧 Send email code if needed
        if (emailProvided) {
            String htmlMessage = """
                <html>
                <body style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
                    <h2 style="color: #4CAF50;">Welcome to HobbySphere!</h2>
                    <p style="font-size: 16px;">Please use the code below to verify your email address:</p>
                    <h1 style="color: #2196F3;">%s</h1>
                    <p style="font-size: 14px; color: #777;">This code will expire in 10 minutes.</p>
                </body>
                </html>
            """.formatted(code);

            emailService.sendHtmlEmail(email, "Email Verification Code", htmlMessage);
        }

        return true;
    }

    public boolean resendVerificationCode(String emailOrPhone) {
        PendingUser pending;

        boolean isEmail = emailOrPhone.contains("@");

        if (isEmail) {
            pending = pendingUserRepository.findByEmail(emailOrPhone);
            if (pending == null) {
                throw new RuntimeException("No pending user found with this email");
            }

            // 🔁 Generate new email code
            String code = String.format("%06d", new Random().nextInt(999999));
            pending.setVerificationCode(code);
            pending.setCreatedAt(LocalDateTime.now());
            pendingUserRepository.save(pending);

            // ✉️ Send email
            String html = """
                <html>
                <body style="font-family: Arial; padding: 20px;">
                    <h2>HobbySphere Verification</h2>
                    <p>Your new verification code is:</p>
                    <h1>%s</h1>
                </body>
                </html>
            """.formatted(code);

            emailService.sendHtmlEmail(emailOrPhone, "New Verification Code", html);
            return true;
        } else {
            pending = pendingUserRepository.findByPhoneNumber(emailOrPhone);
            if (pending == null) {
                throw new RuntimeException("No pending user found with this phone");
            }

            // 🔁 Static code for SMS testing
            pending.setVerificationCode("123456");
            pending.setCreatedAt(LocalDateTime.now());
            pendingUserRepository.save(pending);
            return true;
        }
    }


    private final Map<String, String> resetCodes = new ConcurrentHashMap<>();
    
    
    //user register with email
    public Long verifyEmailCodeAndRegister(String email, String code) {
        PendingUser pending = pendingUserRepository.findByEmail(email);

        if (pending == null) {
            throw new RuntimeException("No pending user found for this email.");
        }

        if (!pending.getVerificationCode().equals(code)) {
            throw new RuntimeException("Invalid verification code.");
        }

        return pending.getId(); // ✅ Return pending user ID for next step (complete profile)
    }




    //user register with number 
    public Long verifyPhoneCodeAndRegister(String phoneNumber, String code) {
        if (!"123456".equals(code)) {
            throw new RuntimeException("Invalid verification code.");
        }

        PendingUser pending = pendingUserRepository.findByPhoneNumber(phoneNumber);
        if (pending == null) {
            throw new RuntimeException("Pending user not found.");
        }

        return pending.getId(); // ✅ Return pending user ID for profile completion step
    }

    
    public boolean completeUserProfile(
    	    Long pendingId,
    	    String username,
    	    String firstName,
    	    String lastName,
    	    MultipartFile profileImage,
    	    Boolean isPublicProfile 
    	) throws IOException {
    	    PendingUser pending = pendingUserRepository.findById(pendingId)
    	        .orElseThrow(() -> new RuntimeException("Pending user not found."));

    	    if (userRepository.findByUsername(username) != null) {
    	        throw new RuntimeException("Username already in use.");
    	    }

    	    Users user = new Users();
    	    user.setUsername(username);
    	    user.setFirstName(firstName);
    	    user.setLastName(lastName);
    	    user.setPasswordHash(pending.getPasswordHash());
    	    user.setStatus(getStatus("ACTIVE"));
    	    user.setCreatedAt(LocalDateTime.now());

    	    if (pending.getEmail() != null) user.setEmail(pending.getEmail());
    	    if (pending.getPhoneNumber() != null) user.setPhoneNumber(pending.getPhoneNumber());

    	   
    	    user.setIsPublicProfile(isPublicProfile != null ? isPublicProfile : true);

    	    if (profileImage != null && !profileImage.isEmpty()) {
    	        String filename = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
    	        Path path = Paths.get("uploads");
    	        if (!Files.exists(path)) Files.createDirectories(path);
    	        Files.copy(profileImage.getInputStream(), path.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
    	        user.setProfilePictureUrl("/uploads/" + filename);
    	    }

    	    user.setUpdatedAt(LocalDateTime.now());
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
	    return userRepository.findAll().stream()
	            .filter(user -> "ACTIVE".equals(user.getStatus().getName()))
	            .filter(Users::isPublicProfile)
	            .map(UserDto::new)
	            .toList();
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

    
    public List<Users> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> "ACTIVE".equals(user.getStatus().getName()))
                .filter(Users::isPublicProfile)
                .toList();
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
            .toList();

        if (myInterestIds.isEmpty()) return List.of();

        // Get users who share interests
        List<UserInterests> sharedInterests = userInterestsRepository.findByInterestIdIn(myInterestIds);

        Set<Users> potentialFriends = sharedInterests.stream()
            .map(ui -> ui.getId().getUser())
            .filter(user -> !user.getId().equals(userId))
            .collect(Collectors.toSet());

        List<Users> currentFriends = friendshipService.getAcceptedFriends(currentUser);

        // ✅ Apply all filters together
        return potentialFriends.stream()
            .filter(user -> "ACTIVE".equals(user.getStatus().getName()))
            .filter(Users::isPublicProfile)
            .filter(user -> !currentFriends.contains(user))
            .filter(user -> !friendshipService.didBlock(currentUser, user))
            .filter(user -> !friendshipService.didBlock(user, currentUser))
            .filter(user -> !friendshipService.hasPendingRequestBetween(currentUser, user))
            .toList();
    }

    public boolean updateVisibilityAndStatus(Long userId, boolean isPublicProfile, UserStatus newStatus) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsPublicProfile(isPublicProfile);
        user.setStatus(newStatus);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return true;
    }

    @Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
    public void softDeleteInactiveUsersAfter30Days() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        List<Users> inactiveUsers = userRepository.findAll().stream()
                .filter(u -> "INACTIVE".equals(u.getStatus().getName()))
                .filter(u -> u.getUpdatedAt() != null && u.getUpdatedAt().isBefore(cutoff))
                .toList();

        for (Users user : inactiveUsers) {
            user.setStatus(getStatus("DELETED")); // ✅ set via entity
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("Soft-deleted user: " + user.getEmail());
        }
    }

    @Scheduled(cron = "0 0 3 * * *") // Every day at 3 AM
    public void permanentlyDeleteUsersAfter90Days() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);

        List<Users> toDelete = userRepository.findAll().stream()
                .filter(u -> "DELETED".equals(u.getStatus().getName()))
                .filter(u -> u.getUpdatedAt() != null && u.getUpdatedAt().isBefore(cutoff))
                .toList();

        for (Users user : toDelete) {
            userRepository.delete(user);
            System.out.println("Permanently deleted user: " + user.getEmail());
        }
    }


    public Users handleGoogleUser(String email, String fullName, String pictureUrl, AtomicBoolean wasInactive) {
        System.out.println("🔥 handleGoogleUser() called");
        System.out.println("📩 Incoming Email: " + email);
        System.out.println("👤 Incoming FullName: " + fullName);
        System.out.println("🖼️ Incoming Picture URL: " + pictureUrl);

        Users existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            System.out.println("👀 User already exists: " + existingUser.getUsername());

            // 🔁 If INACTIVE, mark it but DO NOT update the DB yet
            if ("INACTIVE".equals(existingUser.getStatus().getName())) {
                System.out.println("🟡 User is INACTIVE. Reactivating...");
                existingUser.setStatus(getStatus("ACTIVE")); // ✅ Reactivate using entity
                existingUser.setUpdatedAt(LocalDateTime.now());
                existingUser.setLastLogin(LocalDateTime.now());
                wasInactive.set(true);
                return userRepository.save(existingUser);
            }

            existingUser.setLastLogin(LocalDateTime.now());
            return userRepository.save(existingUser);
        }

        // Fallback for name parsing
        String firstName = "Google", lastName = "User";
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split(" ", 2);
            firstName = parts[0];
            if (parts.length > 1) lastName = parts[1];
        }

        Users newUser = new Users();
        newUser.setEmail(email);
        newUser.setUsername(email.split("@")[0]);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setProfilePictureUrl(pictureUrl);
        newUser.setIsPublicProfile(true);
        newUser.setStatus(getStatus("ACTIVE")); // ✅ set via DB entity
        newUser.setPasswordHash("");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setLastLogin(LocalDateTime.now());

        System.out.println("📥 Saving new Google user: " + newUser.getUsername());
        return userRepository.save(newUser);
    }


    // ✅ Reactivate confirmed inactive user manually
    public Users confirmReactivation(Long userId) {
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if ("INACTIVE".equals(user.getStatus().getName())) {
            user.setStatus(getStatus("ACTIVE")); // ✅ use entity
            user.setUpdatedAt(LocalDateTime.now());
            user.setLastLogin(LocalDateTime.now());
            return userRepository.save(user);
        }

        return user;
    }

    
    public Optional<Users> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean checkPassword(Users user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
        }

}
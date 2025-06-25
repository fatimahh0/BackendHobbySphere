package com.hobbySphere.controller;

import com.hobbySphere.repositories.*;
import com.hobbySphere.entities.*;
import com.hobbySphere.enums.BusinessStatus;
import com.hobbySphere.enums.UserStatus;
import com.hobbySphere.security.JwtUtil;
import com.hobbySphere.services.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.hobbySphere.dto.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:5175"
})
@Tag(name = "Authentication", description = "Endpoints for user login, registration, and Google login")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private UsersRepository UserRepository;

    @PostMapping(value = "/send-verification", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendVerification(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam("username") String username,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("password") String password,
            @RequestParam(required = false, defaultValue = "true") Boolean isPublicProfile,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            Map<String, String> userData = new HashMap<>();
            if (email != null) userData.put("email", email);
            if (phoneNumber != null) userData.put("phoneNumber", phoneNumber);
            userData.put("username", username);
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);
            userData.put("password", password);
            userData.put("isPublicProfile", String.valueOf(isPublicProfile)); // ✅ Include visibility

            boolean sent = userService.sendVerificationCodeForRegistration(userData, profileImage);
            return ResponseEntity.ok(Map.of("message", "Verification code sent"));
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-email-code")
    public ResponseEntity<?> verifyEmailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean verified = userService.verifyEmailCodeAndRegister(email, code);

        if (verified) {
            Users user = userService.findByEmail(email);
            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("firstName", user.getFirstName());
                userData.put("lastName", user.getLastName());
                userData.put("email", user.getEmail());
                userData.put("profilePictureUrl", user.getProfilePictureUrl());
                userData.put("status", user.getStatus()); // ✅ Add status
                userData.put("isPublicProfile", user.isPublicProfile()); // ✅ Add visibility

                return ResponseEntity.ok(Map.of(
                        "message", "User verified and account created successfully",
                        "user", userData));
            }
        }

        return ResponseEntity.status(400).body(Map.of("error", "Invalid code or email"));
    }

    /// user register with number
    @PostMapping("/user/verify-phone-code")
    public ResponseEntity<?> verifyUserPhoneCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phoneNumber");
        String code = request.get("code");

        boolean verified = userService.verifyPhoneCodeAndRegister(phone, code);

        if (verified) {
            Users user = userService.findByPhoneNumber(phone);
            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("firstName", user.getFirstName());
                userData.put("lastName", user.getLastName());
                userData.put("phoneNumber", user.getPhoneNumber());
                userData.put("profilePictureUrl", user.getProfilePictureUrl());
                userData.put("status", user.getStatus()); // ✅ Add status
                userData.put("isPublicProfile", user.isPublicProfile()); // ✅ Add visibility

                return ResponseEntity.ok(Map.of(
                        "message", "User phone verified and account created successfully",
                        "user", userData));
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid phone number or verification code"));
    }


    @PostMapping(value = "/business/send-verification", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendBusinessVerification(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam String businessName,
            @RequestParam String password,
            @RequestParam String description,
            @RequestParam String websiteUrl,
            @RequestParam(required = false, defaultValue = "true") boolean isPublicProfile,
            @RequestPart(value = "businessLogo", required = false) MultipartFile logo,
            @RequestPart(value = "businessBanner", required = false) MultipartFile banner) {
        try {
            Map<String, String> businessData = new HashMap<>();
            if (email != null) businessData.put("email", email);
            if (phoneNumber != null) businessData.put("phoneNumber", phoneNumber);
            businessData.put("businessName", businessName);
            businessData.put("password", password);
            businessData.put("description", description);
            businessData.put("websiteUrl", websiteUrl);
            businessData.put("isPublicProfile", String.valueOf(isPublicProfile)); // ✅ Set visibility

            businessService.sendBusinessVerificationCode(businessData, logo, banner);

            return ResponseEntity.ok(Map.of(
                    "message", phoneNumber != null
                            ? "Static code 123456 set for phone verification"
                            : "Verification code sent to email"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/business/verify-code")
    public ResponseEntity<?> verifyBusinessCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("code");

        boolean verified = businessService.verifyBusinessEmailCode(email, code);

        if (verified) {
            Optional<Businesses> businessOpt = businessService.findByEmail(email);
            if (businessOpt.isPresent()) {
                Businesses business = businessOpt.get();

                Map<String, Object> businessData = new HashMap<>();
                businessData.put("id", business.getId());
                businessData.put("businessName", business.getBusinessName());
                businessData.put("email", business.getEmail());
                businessData.put("phoneNumber", business.getPhoneNumber());
                businessData.put("websiteUrl", business.getWebsiteUrl());
                businessData.put("description", business.getDescription());
                businessData.put("businessLogo", business.getBusinessLogoUrl());
                businessData.put("businessBanner", business.getBusinessBannerUrl());
                businessData.put("status", business.getStatus().name()); // ✅ Add status
                businessData.put("isPublicProfile", business.getIsPublicProfile()); // ✅ Add visibility

                return ResponseEntity.ok(Map.of(
                        "message", "Business email verified and account created successfully",
                        "business", businessData));
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid verification code or email"));
    }


    @PostMapping("/business/verify-phone-code")
    public ResponseEntity<?> verifyBusinessPhoneCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phoneNumber");
        String code = request.get("code");

        boolean verified = businessService.verifyBusinessPhoneCode(phone, code);

        if (verified) {
            Businesses business = businessService.findByPhoneNumber(phone);
            if (business != null) {
                Map<String, Object> businessData = new HashMap<>();
                businessData.put("id", business.getId());
                businessData.put("businessName", business.getBusinessName());
                businessData.put("email", business.getEmail());
                businessData.put("phoneNumber", business.getPhoneNumber());
                businessData.put("websiteUrl", business.getWebsiteUrl());
                businessData.put("description", business.getDescription());
                businessData.put("businessLogo", business.getBusinessLogoUrl());
                businessData.put("businessBanner", business.getBusinessBannerUrl());
                businessData.put("status", business.getStatus().name()); // ✅ Add status
                businessData.put("isPublicProfile", business.getIsPublicProfile()); // ✅ Add visibility

                return ResponseEntity.ok(Map.of(
                        "message", "Business phone verified and account created successfully",
                        "business", businessData));
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid phone number or verification code"));
    }

    @PostMapping("/resend-user-code")
    public ResponseEntity<?> resendUserCode(@RequestBody Map<String, String> request) {
        String contact = request.get("emailOrPhone");

        try {
            userService.resendVerificationCode(contact);
            return ResponseEntity.ok(Map.of("message", "Verification code resent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resend-business-code")
    public ResponseEntity<?> resendBusinessCode(@RequestBody Map<String, String> request) {
        String contact = request.get("emailOrPhone");

        try {
            businessService.resendBusinessVerificationCode(contact);
            return ResponseEntity.ok(Map.of("message", "Verification code resent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // User Login
    @PostMapping("/user/login")
    public ResponseEntity<?> userLogin(@RequestBody @Valid Users user) {
        Users existingUser = userService.findByEmail(user.getEmail());
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found"));
        }

        if (!passwordEncoder.matches(user.getPasswordHash(), existingUser.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Incorrect password"));
        }

        // ❌ If DELETED, reject login
        if (existingUser.getStatus() == UserStatus.DELETED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "This account has been deleted and cannot be accessed."));
        }

        // 🔄 If INACTIVE, reactivate and update status
        boolean wasInactive = false;
        if (existingUser.getStatus() == UserStatus.INACTIVE) {
            existingUser.setStatus(UserStatus.ACTIVE);
            wasInactive = true;
        }

        // Update last login time
        existingUser.setLastLogin(LocalDateTime.now());
        userService.save(existingUser);

        String token = jwtUtil.generateToken(existingUser);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", existingUser.getId());
        userData.put("username", existingUser.getUsername());
        userData.put("firstName", existingUser.getFirstName());
        userData.put("lastName", existingUser.getLastName());
        userData.put("email", existingUser.getEmail());
        userData.put("profilePictureUrl", existingUser.getProfilePictureUrl());

        String message = wasInactive ?
                "User login successful. Your account has been reactivated." :
                "User login successful";

        return ResponseEntity.ok(Map.of(
                "message", message,
                "token", token,
                "user", userData
        ));
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestPart("firstName") String firstName,
            @RequestPart("lastName") String lastName,
            @RequestPart("username") String username,
            @RequestPart(value = "password", required = false) String password,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture
    ) throws IOException {

        Optional<Users> optionalUser = UserRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        Users user = optionalUser.get();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String imageUrl = userService.saveProfileImage(profilePicture);
            user.setProfilePictureUrl(imageUrl);
        }

        if (password != null && !password.isEmpty()) {
            user.setPasswordHash(password); // Optional: hash it
        }

        UserRepository.save(user);

        // ✅ Return updated user data in response
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("id", user.getId());
        updatedData.put("firstName", user.getFirstName());
        updatedData.put("lastName", user.getLastName());
        updatedData.put("username", user.getUsername());
        updatedData.put("email", user.getEmail());
        updatedData.put("phoneNumber", user.getPhoneNumber());
        updatedData.put("profilePictureUrl", user.getProfilePictureUrl());

        return ResponseEntity.ok(updatedData);
    }

    // user login with number

    @PostMapping("/user/login-phone")
    public ResponseEntity<?> userLoginWithPhone(@RequestBody @Valid Users user) {
        String phone = user.getPhoneNumber();
        String rawPassword = user.getPasswordHash();

        if (phone == null || rawPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number and password are required"));
        }

        Users existingUser = userService.findByPhoneNumber(phone);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found with this phone number"));
        }

        if (!passwordEncoder.matches(rawPassword, existingUser.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Incorrect password"));
        }

        // ❌ If DELETED, reject login
        if (existingUser.getStatus() == UserStatus.DELETED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "This account has been deleted and cannot be accessed."));
        }

        // 🔄 If INACTIVE, reactivate
        boolean wasInactive = false;
        if (existingUser.getStatus() == UserStatus.INACTIVE) {
            existingUser.setStatus(UserStatus.ACTIVE);
            wasInactive = true;
        }

        // 🕒 Update last login
        existingUser.setLastLogin(LocalDateTime.now());
        userService.save(existingUser);

        String token = jwtUtil.generateToken(existingUser);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", existingUser.getId());
        userData.put("username", existingUser.getUsername());
        userData.put("firstName", existingUser.getFirstName());
        userData.put("lastName", existingUser.getLastName());
        userData.put("phoneNumber", existingUser.getPhoneNumber());
        userData.put("profilePictureUrl", existingUser.getProfilePictureUrl());

        String message = wasInactive
                ? "User login with phone successful. Your account has been reactivated."
                : "User login with phone successful";

        return ResponseEntity.ok(Map.of(
                "message", message,
                "token", token,
                "user", userData));
    }

    // Business Login
    @PostMapping("/business/login")
    public ResponseEntity<?> businessLogin(@RequestBody @Valid Users user) {
        Optional<Businesses> optionalBusiness = businessService.findByEmail(user.getEmail());

        if (optionalBusiness.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Business not found"));
        }

        Businesses business = optionalBusiness.get();

        if (!passwordEncoder.matches(user.getPasswordHash(), business.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Incorrect password"));
        }

        //  If deleted, reject login
        if (business.getStatus() == BusinessStatus.DELETED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "This account has been deleted and cannot be accessed."));
        }

        //  Reactivate if inactive
        boolean wasInactive = false;
        if (business.getStatus() == BusinessStatus.INACTIVE) {
            business.setStatus(BusinessStatus.ACTIVE);
            wasInactive = true;
        }

        business.setLastLoginAt(LocalDateTime.now());
        businessService.save(business);

        String token = jwtUtil.generateToken(business);

        Map<String, Object> businessData = new HashMap<>();
        businessData.put("id", business.getId());
        businessData.put("businessName", business.getBusinessName());
        businessData.put("email", business.getEmail());
        businessData.put("businessLogo", business.getBusinessLogoUrl());
        businessData.put("businessBanner", business.getBusinessBannerUrl());
        businessData.put("phoneNumber", business.getPhoneNumber());
        businessData.put("WebsiteUrl", business.getWebsiteUrl());
        businessData.put("Description", business.getDescription());

        // ✅ Send reactivation message if applicable
        String message = wasInactive ? 
            "Business login successful. Your account has been reactivated." :
            "Business login successful";

        return ResponseEntity.ok(Map.of(
                "message", message,
                "token", token,
                "business", businessData));
    }


    // business login with number
    @PostMapping("/business/login-phone")
    public ResponseEntity<?> businessLoginWithPhone(@RequestBody @Valid Users user) {
        String phone = user.getPhoneNumber();
        String rawPassword = user.getPasswordHash();

        if (phone == null || rawPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number and password are required"));
        }

        Businesses business = businessService.findByPhoneNumber(phone);
        if (business == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Business not found with this phone number"));
        }

        if (!passwordEncoder.matches(rawPassword, business.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Incorrect password"));
        }

        // ❌ If account is deleted, reject login
        if (business.getStatus() == BusinessStatus.DELETED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "This account has been deleted and cannot be accessed."));
        }

        // ✅ If inactive, reactivate and show message
        boolean wasInactive = false;
        if (business.getStatus() == BusinessStatus.INACTIVE) {
            business.setStatus(BusinessStatus.ACTIVE);
            wasInactive = true;
        }

        business.setLastLoginAt(LocalDateTime.now());
        businessService.save(business);

        String token = jwtUtil.generateToken(business);

        Map<String, Object> businessData = new HashMap<>();
        businessData.put("id", business.getId());
        businessData.put("businessName", business.getBusinessName());
        businessData.put("email", business.getEmail());
        businessData.put("phoneNumber", business.getPhoneNumber());
        businessData.put("websiteUrl", business.getWebsiteUrl());
        businessData.put("description", business.getDescription());
        businessData.put("businessLogo", business.getBusinessLogoUrl());
        businessData.put("businessBanner", business.getBusinessBannerUrl());

        String message = wasInactive
                ? "Business login with phone successful. Your account has been reactivated."
                : "Business login with phone successful";

        return ResponseEntity.ok(Map.of(
                "message", message,
                "token", token,
                "business", businessData));
    }


    // Google Login
    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request, @RequestParam String userType) {
        String idToken = request.getIdToken();

        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body("Missing idToken");
        }

        try {
            GoogleIdToken.Payload payload = googleAuthService.verifyToken(idToken);

            if (payload == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid ID token.");
            }

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // 🏢 Business login flow
            if ("business".equalsIgnoreCase(userType)) {
                Optional<Businesses> optionalBusiness = businessService.findByEmail(email);
                Businesses business = optionalBusiness.orElseGet(() -> {
                    Businesses newBusiness = new Businesses();
                    newBusiness.setEmail(email);
                    newBusiness.setBusinessName(name != null ? name : email.split("@")[0]);
                    newBusiness.setPasswordHash(passwordEncoder.encode("google_placeholder_password"));
                    return businessService.save(newBusiness);
                });

                String token = jwtUtil.generateToken(business);
                return ResponseEntity.ok(Map.of(
                        "message", "Google business login successful",
                        "token", token));
            }

            // 👤 User login flow
            Users user = userService.findByEmail(email);
            boolean wasInactive = false;

            if (user != null) {
                if (user.getStatus() == UserStatus.DELETED) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("message", "This account has been deleted and cannot be accessed."));
                }

                if (user.getStatus() == UserStatus.INACTIVE) {
                    user.setStatus(UserStatus.ACTIVE);
                    wasInactive = true;
                }

                user.setLastLogin(LocalDateTime.now());
                userService.save(user);
            } else {
                user = new Users();
                user.setEmail(email);
                user.setUsername(name != null ? name : email.split("@")[0]);
                user.setPasswordHash(passwordEncoder.encode("google_placeholder_password"));
                user.setLastLogin(LocalDateTime.now());
                userService.save(user);
            }

            String token = jwtUtil.generateToken(user);
            String message = wasInactive
                    ? "Google login successful. Your account has been reactivated."
                    : "Google login successful";

            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "token", token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Google ID token: " + e.getMessage());
        }
    }


    @PostMapping("/admin/promote-to-manager/{userId}/{businessId}")
    public ResponseEntity<?> promoteToManager(@PathVariable Long userId, @PathVariable Long businessId) {
        Users user = userService.getUserById(userId);
        Businesses business = businessService.findById(businessId); // No Optional

        AdminUsers manager = adminUserService.promoteUserToManager(user, business);

        return ResponseEntity.ok(Map.of(
                "message", "User promoted to Manager successfully",
                "manager", Map.of(
                        "id", manager.getAdminId(),
                        "email", manager.getEmail(),
                        "role", manager.getRole().getName(),
                        "businessId", manager.getBusiness().getId())));
    }

    @Operation(summary = "Login as a Manager", description = "Authenticates a Manager from the AdminUsers table based on email/username and password", responses = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or not a manager")
    })
    @PostMapping("/manager/login")
    public ResponseEntity<?> managerLogin(@RequestBody AdminLoginRequest request) {
        // 1. Find manager by username or email
        Optional<AdminUsers> optionalAdmin = adminUserService.findByUsernameOrEmail(request.getUsernameOrEmail());

        // 2. If not found, error
        if (optionalAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        AdminUsers admin = optionalAdmin.get();

        // 3. Password check
        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        // 4. Role check
        if (!"MANAGER".equalsIgnoreCase(admin.getRole().getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Access denied: Not a Manager"));
        }

        // 5. JWT
        String token = jwtUtil.generateToken(admin);

        // 6. Manager data
        Map<String, Object> managerData = new HashMap<>();
        managerData.put("id", admin.getAdminId());
        managerData.put("username", admin.getUsername());
        managerData.put("firstName", admin.getFirstName());
        managerData.put("lastName", admin.getLastName());
        managerData.put("email", admin.getEmail());
        managerData.put("role", admin.getRole().getName());

        // 7. Business data (the business that promoted this manager)
        Map<String, Object> businessData = null;
        if (admin.getBusiness() != null) {
            Businesses business = admin.getBusiness();
            businessData = new HashMap<>();
            businessData.put("id", business.getId());
            businessData.put("businessName", business.getBusinessName());
            businessData.put("email", business.getEmail());
            businessData.put("phoneNumber", business.getPhoneNumber());
            businessData.put("businessLogoUrl", business.getBusinessLogoUrl());
            businessData.put("businessBannerUrl", business.getBusinessBannerUrl());
            businessData.put("description", business.getDescription());
            businessData.put("websiteUrl", business.getWebsiteUrl());
            // Add other fields as needed
        }

        // 8. Return token, manager, and business info
        return ResponseEntity.ok(Map.of(
                "message", "Manager login successful",
                "token", token,
                "manager", managerData,
                "business", businessData));
    }

    @Operation(summary = "Login as Super Admin", description = "Authenticates a Super Admin using email and password", responses = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or not a Super Admin")
    })
    @PostMapping("/superadmin/login")
    public ResponseEntity<?> superAdminLogin(@RequestBody AdminLoginRequest request) {
        if (request.getUsernameOrEmail() == null || request.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email and password are required"));
        }

        // Force login by email only
        Optional<AdminUsers> adminOpt = adminUserService.findByEmail(request.getUsernameOrEmail());

        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No Super Admin found with this email"));
        }

        AdminUsers admin = adminOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Incorrect password"));
        }

        if (!"SUPER_ADMIN".equalsIgnoreCase(admin.getRole().getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Access denied: Not a Super Admin"));
        }

        String token = jwtUtil.generateToken(admin);

        Map<String, Object> adminData = new HashMap<>();
        adminData.put("id", admin.getAdminId());
        adminData.put("username", admin.getUsername());
        adminData.put("firstName", admin.getFirstName());
        adminData.put("lastName", admin.getLastName());
        adminData.put("email", admin.getEmail());
        adminData.put("role", admin.getRole().getName());

        return ResponseEntity.ok(Map.of(
                "message", "Super Admin login successful",
                "token", token,
                "admin", adminData));
    }

    @Operation(summary = "Register a new Super Admin", description = "Registers a new AdminUser with the default role of SUPER_ADMIN")
    @PostMapping("/admin/register")
    public ResponseEntity<?> registerSuperAdmin(@RequestBody AdminRegisterRequest request) {
        // Check if email or username already exists
        if (adminUserService.findByUsernameOrEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Username or email already in use"));
        }

        // Fetch the SUPER_ADMIN role
        Role role = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new RuntimeException("Role SUPER_ADMIN not found"));

        // Create the admin user
        AdminUsers newAdmin = new AdminUsers();
        newAdmin.setUsername(request.getUsername());
        newAdmin.setFirstName(request.getFirstName());
        newAdmin.setLastName(request.getLastName());
        newAdmin.setEmail(request.getEmail());
        newAdmin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newAdmin.setRole(role);

        adminUserService.save(newAdmin);

        return ResponseEntity.ok(Map.of(
                "message", "Super Admin registered successfully",
                "adminId", newAdmin.getAdminId()));
    }

    @Operation(summary = "Remove a manager", description = "Deletes a manager from AdminUsers and BusinessAdmins tables")
    @ApiResponse(responseCode = "200", description = "Manager removed successfully")
    @ApiResponse(responseCode = "404", description = "Manager not found")
    @DeleteMapping("/admin/remove-manager/{adminId}")
    public ResponseEntity<?> removeManager(@PathVariable Long adminId) {
        Optional<AdminUsers> optionalManager = adminUserService.findById(adminId);

        if (optionalManager.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Manager not found"));
        }

        adminUserService.deleteManagerById(adminId);

        return ResponseEntity.ok(Map.of("message", "Manager removed successfully"));
        
   }

}

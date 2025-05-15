package com.hobbySphere.controller;
import com.hobbySphere.dto.GoogleLoginRequest;
import com.hobbySphere.entities.*;
import com.hobbySphere.security.JwtUtil;
import com.hobbySphere.services.*;
import com.hobbySphere.repositories.RoleRepository;
import com.hobbySphere.repositories.UsersRepository;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
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

    // Default User Registration
    @PostMapping("/user/register")
    public ResponseEntity<?> userRegister(@RequestBody @Valid Users user) {
        boolean emailExists = userService.findByEmail(user.getEmail()) != null;
        boolean usernameExists = userService.findByUsername(user.getUsername()) != null;

        if (emailExists && usernameExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and Username are already in use.");
        } else if (usernameExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is already in use.");
        } else if (emailExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already in use.");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password cannot be null or empty.");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        userService.save(user);

        return ResponseEntity.ok("User registration successful");
    }

    // Multipart User Registration
    @PostMapping(value = "/user/register", consumes = "multipart/form-data")
    public ResponseEntity<?> userRegisterMultipart(
            @RequestParam("username") String username,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            boolean emailExists = userService.findByEmail(email) != null;
            boolean usernameExists = userService.findByUsername(username) != null;

            if (emailExists && usernameExists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Email and Username are already in use.");
            } else if (usernameExists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Username is already in use.");
            } else if (emailExists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Email is already in use.");
            }

            Users savedUser = userService.registerUser(
                    username, firstName, lastName, email, password, profileImage
            );

            return ResponseEntity.ok(Map.of(
                    "message", "User registration successful",
                    "user", savedUser
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    // Default Business Registration
    @PostMapping("/business/register")
    public ResponseEntity<?> businessRegister(@RequestBody @Valid Businesses business) {
        Optional<Businesses> existingBusiness = businessService.findByEmail(business.getEmail());
        if (existingBusiness.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Business email is already in use.");
        }

        if (business.getPasswordHash() == null || business.getPasswordHash().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password cannot be null or empty.");
        }

        business.setPasswordHash(passwordEncoder.encode(business.getPasswordHash()));
        businessService.save(business);

        return ResponseEntity.ok("Business registration successful");
    }

  
    @PostMapping(value = "/business/register", consumes = "multipart/form-data")
    public ResponseEntity<?> registerBusinessMultipart(
            @RequestParam("businessName") String businessName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("description") String description,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("websiteUrl") String websiteUrl,
            @RequestParam(value = "businessLogo", required = false) MultipartFile businessLogo,
            @RequestParam(value = "businessBanner", required = false) MultipartFile businessBanner
    ) {
        try {
            Optional<Businesses> existing = businessService.findByEmail(email);
            if (existing.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Email already in use"));
            }

            Businesses savedBusiness = businessService.registerBusiness(
                    businessName, email, password, description,
                    phoneNumber, websiteUrl, businessLogo, businessBanner
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Business registration successful",
                    "business", savedBusiness
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    // Admin Registration
    @PostMapping("/admin/register")
    public ResponseEntity<?> adminRegister(@RequestBody @Valid AdminUsers adminUser) {
        if (adminUser.getPasswordHash() == null || adminUser.getPasswordHash().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password cannot be null or empty.");
        }

        boolean emailExists = adminUserService.findByEmail(adminUser.getEmail()).isPresent();
        boolean usernameExists = adminUserService.findByUsername(adminUser.getUsername()).isPresent();

        if (emailExists && usernameExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email and Username are already in use.");
        } else if (usernameExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Username is already in use.");
        } else if (emailExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email is already in use.");
        }

        Role role = roleRepository.findByName("ALL_REFERENCES")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ALL_REFERENCES");
                    return roleRepository.save(newRole);
                });

        adminUser.setRole(role);
        adminUser.setPasswordHash(passwordEncoder.encode(adminUser.getPasswordHash()));
        adminUserService.save(adminUser);

        return ResponseEntity.ok("Admin registration successful");
    }

    // Admin Login
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminUsers loginRequest) {
        Optional<AdminUsers> optionalAdmin = adminUserService.findByEmail(loginRequest.getEmail());

        if (optionalAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Admin not found"));
        }

        AdminUsers adminUser = optionalAdmin.get();
        if (!passwordEncoder.matches(loginRequest.getPasswordHash(), adminUser.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Incorrect password"));
        }

        String token = jwtUtil.generateToken(adminUser);

        return ResponseEntity.ok(Map.of(
                "message", "Admin login successful",
                "token", token,
                "role", adminUser.getRole().getName()
        ));
    }

    // User Login
    @PostMapping("/user/login")
    public ResponseEntity<?> userLogin(@RequestBody @Valid Users user) {
        Users existingUser = userService.findByEmail(user.getEmail());
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
        }

        if (!passwordEncoder.matches(user.getPasswordHash(), existingUser.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Incorrect password"));
        }

        String token = jwtUtil.generateToken(user);
       // String token = jwtUtil.generateToken(existingUser);
        return ResponseEntity.ok(Map.of(
            "message", "User login successful",
            "token", token,
            "user", existingUser
        ));

    }
    
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestPart("firstName") String firstName,
            @RequestPart("lastName") String lastName,
            @RequestPart("username") String username,
            @RequestPart(value = "password", required = false) String password,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) throws IOException {

        Optional<Users> optionalUser = UserRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Users user = optionalUser.get();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            // TODO: Save the file and get its URL
            String imageUrl = userService.saveProfileImage(profilePicture); // You must implement this
            user.setProfilePictureUrl(imageUrl);
        }

        if (password != null && !password.isEmpty()) {
            // Optional: hash password before saving
            user.setPasswordHash(password);
        }

        UserRepository.save(user);

        return ResponseEntity.ok("Profile updated successfully");
    }

    // Business Login
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

        String token = jwtUtil.generateToken(business);

        // Manually extract safe fields
        Map<String, Object> businessData = new HashMap<>();
        businessData.put("id", business.getId());
        businessData.put("businessName", business.getBusinessName());
        businessData.put("email", business.getEmail());
        businessData.put("businessLogo", business.getBusinessLogoUrl());
        businessData.put("businessBanner", business.getBusinessBannerUrl());
        businessData.put("businessName",business.getBusinessName());
        businessData.put("phoneNumber",business.getPhoneNumber());

        // Full response map
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Business login successful");
        response.put("token", token);
        response.put("business", businessData);

        return ResponseEntity.ok(response);
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
                        "token", token
                ));
            } else {
                Users user = userService.findByEmail(email);
                if (user == null) {
                    user = new Users();
                    user.setEmail(email);
                    user.setUsername(name != null ? name : email.split("@")[0]);
                    user.setPasswordHash(passwordEncoder.encode("google_placeholder_password"));
                    userService.save(user);
                }

                String token = jwtUtil.generateToken(user);
                return ResponseEntity.ok(Map.of(
                        "message", "Google login successful",
                        "token", token
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Google ID token: " + e.getMessage());
        }
    }

}

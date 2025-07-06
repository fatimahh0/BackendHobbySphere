package com.hobbySphere.services;

import com.hobbySphere.entities.Businesses;
import com.hobbySphere.dto.LowRatedBusinessDTO;
import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.repositories.*;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.hobbySphere.entities.PendingBusiness;
import com.hobbySphere.entities.PendingManager;
import com.hobbySphere.entities.Review;
import com.hobbySphere.entities.Role;
import com.hobbySphere.entities.BusinessStatus; 


import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class BusinessService {

    @Autowired
    private BusinessesRepository businessRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Only in File1
    @Autowired
    private ActivitiesRepository activityRepository;

    @Autowired
    private ActivityBookingsRepository activityBookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BusinessAdminsRepository businessAdminsRepository;

    @Autowired
    private AdminUsersRepository adminUsersRepository;

    @Autowired
    private PendingBusinessRepository pendingBusinessRepository;
    
    @Autowired
    private PendingManagerRepository pendingManagerRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private BusinessStatusRepository businessStatusRepository;


    
    private final EmailService emailService;

    public BusinessService(EmailService emailService) {
        this.emailService = emailService;
    }

    private final Map<String, String> resetCodes = new ConcurrentHashMap<>();

    
    public Businesses findByEmail(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }

        Businesses business = null;

        if (identifier.contains("@")) {
            business = businessRepository.findByEmail(identifier).orElse(null);
        } else {
            business = businessRepository.findByPhoneNumber(identifier).orElse(null);
        }

        if (business == null) {
            throw new RuntimeException("Business not found with: " + identifier);
        }

        return business;
    }


    
    
    

    public Businesses save(Businesses business) {
        if (business.getId() != null) {
            Optional<Businesses> existingBusiness = businessRepository.findByEmail(business.getEmail());
            if (existingBusiness.isPresent() && !existingBusiness.get().getId().equals(business.getId())) {
                throw new IllegalArgumentException("Email already exists for another business!");
            }
        }
        return businessRepository.save(business);
    }

    public Businesses getByEmailOrThrow(String email) {
        return businessRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Business not found with email: " + email));
    }

    public Businesses findById(Long id) {
        return businessRepository.findById(id).orElse(null);
    }

    public List<Businesses> findAll() {
        return businessRepository.findAll();
    }

    // From File1: smart delete with cleanup
    @Transactional
    public void delete(Long businessId) {
        Businesses business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // Step 1: Get all activities owned by this business
        List<Activities> activities = activityRepository.findByBusinessId(businessId);

        // Step 2: For each activity, delete all bookings & reviews
        for (Activities activity : activities) {
            Long activityId = activity.getId();

            // Delete all user bookings for this activity
            activityBookingRepository.deleteByActivity_Id(activityId);

            // Delete all user reviews for this activity
            reviewRepository.deleteByActivity_Id(activityId);

            // Delete the activity itself
            activityRepository.deleteById(activityId);
        }

        // Step 3: Delete links to business admins & internal admins
        businessAdminsRepository.deleteByBusinessId(businessId);
        adminUsersRepository.deleteByBusinessId(businessId);

        // Step 4: Finally delete the business itself
        businessRepository.deleteById(businessId);
    }

    //  Step 1: Send verification code and save to PendingBusiness
    public Long sendBusinessVerificationCode(Map<String, String> businessData) {
        String email = businessData.get("email");
        String phone = businessData.get("phoneNumber");
        String password = businessData.get("password");

        String statusStr = businessData.get("status");
        String isPublicProfileStr = businessData.get("isPublicProfile");

        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            throw new RuntimeException("Provide either email or phone.");
        }

        if (email != null && phone != null) {
            throw new RuntimeException("Only one of email or phone should be provided.");
        }

        // ✅ Convert status string to BusinessStatus entity
        BusinessStatus status = businessStatusRepository.findByName(
            statusStr != null ? statusStr.toUpperCase() : "ACTIVE"
        ).orElseThrow(() -> new RuntimeException("Invalid or missing status"));

        // ✅ Check duplicates
        if (email != null) {
            businessRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getStatus().getName().equals("DELETED")) {
                    throw new RuntimeException("Email already in use.");
                } else {
                    businessRepository.delete(existing);
                }
            });
            if (pendingBusinessRepository.existsByEmail(email)) {
                throw new RuntimeException("Email is already pending verification");
            }
        }

        if (phone != null) {
            businessRepository.findByPhoneNumber(phone).ifPresent(existing -> {
                if (!existing.getStatus().getName().equals("DELETED")) {
                    throw new RuntimeException("Phone already in use.");
                } else {
                    businessRepository.delete(existing);
                }
            });
            if (pendingBusinessRepository.existsByPhoneNumber(phone)) {
                throw new RuntimeException("Phone is already pending verification");
            }
        }

        // ✅ Generate code
        String code = (phone != null)
            ? "123456"
            : String.format("%06d", new Random().nextInt(999999));

        // ✅ Create pending business
        PendingBusiness pending = new PendingBusiness();
        pending.setEmail(email);
        pending.setPhoneNumber(phone);
        pending.setPasswordHash(passwordEncoder.encode(password));
        pending.setVerificationCode(code);
        pending.setCreatedAt(LocalDateTime.now());
        pending.setStatus(status);
        pending.setIsPublicProfile(isPublicProfileStr == null || Boolean.parseBoolean(isPublicProfileStr));

        PendingBusiness saved = pendingBusinessRepository.save(pending);

        // ✅ Send email or simulate SMS
        if (email != null) {
            String html = """
                <html>
                <body style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
                    <h2 style="color: #4CAF50;">Welcome to HobbySphere Business!</h2>
                    <p>Please use the code below to verify your business email:</p>
                    <h1 style="color: #2196F3;">%s</h1>
                    <p>This code will expire in 10 minutes.</p>
                </body>
                </html>
            """.formatted(code);
            emailService.sendHtmlEmail(email, "Business Verification Code", html);
        } else {
            System.out.println("📱 Sending SMS to " + phone + ": Your verification code is " + code);
        }

        return saved.getId(); // ✅ Return ID for next step
    }


    // ✅ Step 2: Verify code and create actual Businesses entry
    public Long verifyBusinessEmailCode(String email, String code) {
        PendingBusiness pending = pendingBusinessRepository.findByEmail(email);

        if (pending == null || !pending.getVerificationCode().equals(code)) {
            throw new RuntimeException("Invalid verification code");
        }

      
        return pending.getId();  
    }




    public boolean resendBusinessVerificationCode(String emailOrPhone) {
        PendingBusiness pending;

        // 🔍 Determine whether it's email or phone
        boolean isEmail = emailOrPhone.contains("@");

        if (isEmail) {
            pending = pendingBusinessRepository.findByEmail(emailOrPhone);
            if (pending == null)
                throw new RuntimeException("No pending business found with this email");

            String code = String.format("%06d", new Random().nextInt(999999));
            pending.setVerificationCode(code);
            pending.setCreatedAt(LocalDateTime.now());
            pendingBusinessRepository.save(pending);

            // 📧 Send email
            String html = """
                <html>
                <body style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
                    <h2 style="color: #4CAF50;">Verification Code Resent</h2>
                    <p>Use the code below to complete your business email verification:</p>
                    <h1 style="color: #2196F3;">%s</h1>
                    <p>This code will expire in 10 minutes.</p>
                </body>
                </html>
            """.formatted(code);
            emailService.sendHtmlEmail(emailOrPhone, "Resend Business Verification Code", html);

            return true;

        } else {
            pending = pendingBusinessRepository.findByPhoneNumber(emailOrPhone);
            if (pending == null)
                throw new RuntimeException("No pending business found with this phone");

            String code = "123456"; // Simulated SMS code
            pending.setVerificationCode(code);
            pending.setCreatedAt(LocalDateTime.now());
            pendingBusinessRepository.save(pending);

            // 🧾 Simulate SMS
            System.out.println("📱 Resending SMS to " + emailOrPhone + ": Your code is " + code);
            return true;
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty())
            return null;
        Path uploadPath = Paths.get("uploads");
        if (!Files.exists(uploadPath))
            Files.createDirectories(uploadPath);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path fullPath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + fileName;
    }

    public Long verifyBusinessPhoneCode(String phone, String code) {
        PendingBusiness pending = pendingBusinessRepository.findByPhoneNumber(phone);

        if (pending == null || !pending.getVerificationCode().equals(code)) {
            throw new RuntimeException("Invalid verification code");
        }

        return pending.getId();
    }

    public Businesses completeBusinessProfile(
            Long pendingId,
            String businessName,
            String description,
            String websiteUrl,
            MultipartFile logo,
            MultipartFile banner
    ) throws IOException {

        PendingBusiness pending = pendingBusinessRepository.findById(pendingId)
                .orElseThrow(() -> new RuntimeException("Pending business not found."));

        Businesses business = new Businesses();
        business.setEmail(pending.getEmail());
        business.setPhoneNumber(pending.getPhoneNumber());
        business.setPasswordHash(pending.getPasswordHash());
        business.setStatus(pending.getStatus());
        business.setIsPublicProfile(pending.getIsPublicProfile());
        business.setBusinessName(businessName);
        business.setDescription(description);
        business.setWebsiteUrl(websiteUrl);
        business.setCreatedAt(LocalDateTime.now());
        business.setUpdatedAt(LocalDateTime.now());

        // Upload logo
        if (logo != null && !logo.isEmpty()) {
            String logoFileName = UUID.randomUUID() + "_" + logo.getOriginalFilename();
            Path logoPath = Paths.get("uploads").resolve(logoFileName);
            Files.createDirectories(logoPath.getParent());
            Files.copy(logo.getInputStream(), logoPath, StandardCopyOption.REPLACE_EXISTING);
            business.setBusinessLogoUrl("/uploads/" + logoFileName);
        }

        // Upload banner
        if (banner != null && !banner.isEmpty()) {
            String bannerFileName = UUID.randomUUID() + "_" + banner.getOriginalFilename();
            Path bannerPath = Paths.get("uploads").resolve(bannerFileName);
            Files.createDirectories(bannerPath.getParent());
            Files.copy(banner.getInputStream(), bannerPath, StandardCopyOption.REPLACE_EXISTING);
            business.setBusinessBannerUrl("/uploads/" + bannerFileName);
        }

        Businesses saved = businessRepository.save(business);
        pendingBusinessRepository.delete(pending);

        return saved;
    }


    public Businesses updateBusinessWithImages(
            Long id,
            String name,
            String email,
            String password,
            String description,
            String phoneNumber,
            String websiteUrl,
            MultipartFile logo,
            MultipartFile banner) throws IOException {
        Businesses existing = businessRepository.findById(id).orElse(null);

        if (existing == null) {
            throw new IllegalArgumentException("Business with ID " + id + " not found.");
        }

        // Validate unique email
        Optional<Businesses> byEmail = businessRepository.findByEmail(email);
        if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("Email already exists for another business!");
        }

        existing.setBusinessName(name);
        existing.setEmail(email);

        // ✅ Only update password if it's not empty and >= 6 characters
        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters long.");
            }
            existing.setPasswordHash(passwordEncoder.encode(password));
        }

        existing.setDescription(description);
        existing.setPhoneNumber(phoneNumber);
        existing.setWebsiteUrl(websiteUrl);

        // File upload directory
        Path uploadDir = Paths.get("uploads/");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // ✅ Logo upload
        if (logo != null && !logo.isEmpty()) {
            String logoFileName = UUID.randomUUID() + "_" + logo.getOriginalFilename();
            Path logoPath = uploadDir.resolve(logoFileName);
            Files.copy(logo.getInputStream(), logoPath, StandardCopyOption.REPLACE_EXISTING);
            existing.setBusinessLogoUrl("/uploads/" + logoFileName);
        }

        // ✅ Banner upload
        if (banner != null && !banner.isEmpty()) {
            String bannerFileName = UUID.randomUUID() + "_" + banner.getOriginalFilename();
            Path bannerPath = uploadDir.resolve(bannerFileName);
            Files.copy(banner.getInputStream(), bannerPath, StandardCopyOption.REPLACE_EXISTING);
            existing.setBusinessBannerUrl("/uploads/" + bannerFileName);
        }

        return businessRepository.save(existing);
    }

    @Transactional
    public boolean deleteBusinessByIdWithPassword(Long id, String password) {
        Optional<Businesses> optionalBusiness = businessRepository.findById(id);
        if (optionalBusiness.isEmpty())
            return false;

        Businesses business = optionalBusiness.get();

        // 🔐 Password check
        if (!passwordEncoder.matches(password, business.getPasswordHash())) {
            return false;
        }

        delete(id);

        return true;
    }

    // ✅ Update password with code sent by email (STEP 1)
    public boolean resetPassword(String email) {
        Optional<Businesses> optional = businessRepository.findByEmail(email);
        if (optional.isEmpty())
            return false;

        String code = String.format("%06d", new Random().nextInt(999999));
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
                """
                .formatted(code);

        emailService.sendHtmlEmail(email, "Password Reset Code", htmlMessage);

        return true;
    }

    // ✅ Verify the reset code (STEP 2)
    public boolean verifyResetCode(String email, String code) {
        return resetCodes.containsKey(email) && resetCodes.get(email).equals(code);
    }

    // ✅ Update password (STEP 3)
    public boolean updatePasswordDirectly(String email, String newPassword) {
        Optional<Businesses> optional = businessRepository.findByEmail(email);
        if (optional.isEmpty())
            return false;

        Businesses business = optional.get();
        business.setPasswordHash(passwordEncoder.encode(newPassword));
        businessRepository.save(business);
        resetCodes.remove(email);
        return true;
    }


    public Optional<Businesses> findByPhoneNumber(String phone) {
        return businessRepository.findByPhoneNumber(phone);
    }



    public boolean deleteBusinessLogo(Long businessId) {
        Businesses business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (business.getBusinessLogoUrl() != null && !business.getBusinessLogoUrl().isEmpty()) {
            String logoPath = business.getBusinessLogoUrl().replace("/uploads", "uploads");
            try {
                Files.deleteIfExists(Paths.get(logoPath));
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete logo: " + e.getMessage());
            }
            business.setBusinessLogoUrl(null);
            businessRepository.save(business);
            return true;
        }

        return false;
    }

    public boolean deleteBusinessBanner(Long businessId){
        Businesses business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (business.getBusinessBannerUrl() != null && !business.getBusinessBannerUrl().isEmpty()) {
            String bannerPath = business.getBusinessBannerUrl().replace("/uploads", "uploads");
            try {
                Files.deleteIfExists(Paths.get(bannerPath));
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete banner: " + e.getMessage());
            }
            business.setBusinessBannerUrl(null);
            businessRepository.save(business);
            return true;
        }

        return false;
    }

    public List<Businesses> getAllPublicActiveBusinesses() {
        BusinessStatus activeStatus = businessStatusRepository.findByName("ACTIVE")
            .orElseThrow(() -> new RuntimeException("ACTIVE status not found"));

        return businessRepository.findByIsPublicProfileTrueAndStatus(activeStatus);
    }

    
    @Scheduled(cron = "0 0 2 * * *")
    public void deleteInactiveBusinessesOlderThan30Days() {
        BusinessStatus inactiveStatus = businessStatusRepository.findByName("INACTIVE")
            .orElseThrow(() -> new RuntimeException("INACTIVE status not found"));

        BusinessStatus deletedStatus = businessStatusRepository.findByName("DELETED")
            .orElseThrow(() -> new RuntimeException("DELETED status not found"));

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        List<Businesses> toSoftDelete = businessRepository.findAll().stream()
                .filter(b -> b.getStatus().getName().equals("INACTIVE")
                        && b.getUpdatedAt() != null
                        && b.getUpdatedAt().isBefore(cutoffDate))
                .toList();

        for (Businesses b : toSoftDelete) {
            b.setStatus(deletedStatus);
            b.setUpdatedAt(LocalDateTime.now());
            businessRepository.save(b);
            System.out.println("Soft-deleted business: " + b.getEmail());
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void permanentlyDeleteBusinessesAfter90Days() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);

        List<Businesses> toDelete = businessRepository.findAll().stream()
                .filter(b -> b.getStatus().getName().equals("DELETED")
                        && b.getUpdatedAt() != null
                        && b.getUpdatedAt().isBefore(cutoff))
                .toList();

        for (Businesses b : toDelete) {
            businessRepository.delete(b);
            System.out.println("Permanently deleted business: " + b.getEmail());
        }
    }


    public Businesses findByEmailOrThrow(String email) {
        return businessRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Business not found with email: " + email));
    }

 


    public List<LowRatedBusinessDTO> getLowRatedBusinesses() {
        List<Businesses> businesses = businessRepository.findAll();
        List<LowRatedBusinessDTO> result = new ArrayList<>();

        for (Businesses b : businesses) {
            List<Review> reviews = reviewRepository.findByBusinessId(b.getId());
            if (reviews.isEmpty()) continue;

            double avg = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);

            if (avg <= 3.0) {
            	result.add(new LowRatedBusinessDTO(
            		    b.getId(),
            		    b.getBusinessName(),
            		    b.getStatus().getName(), 
            		    avg
            		));

            }
        }

        return result;
    }

    public boolean checkPassword(Businesses business, String rawPassword) {
        return passwordEncoder.matches(rawPassword, business.getPasswordHash());
    }

    public boolean verifyPassword(Long businessId, String rawPassword) {
        Optional<Businesses> businessOpt = businessRepository.findById(businessId);
        return businessOpt.isPresent() &&
               passwordEncoder.matches(rawPassword, businessOpt.get().getPasswordHash());
    }

    public void sendManagerInvite(String email, Businesses business) {
        // Generate unique token
        String token = UUID.randomUUID().toString();

        // Save to PendingManager table
        PendingManager pending = new PendingManager(email, business, token);
        pendingManagerRepository.save(pending);

        // Email content
        String inviteLink = "http://localhost:5173/assign-manager?token=" + token;

        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
                <h2 style="color: #4CAF50;">You're Invited to Be a Manager!</h2>
                <p>You have been invited to become a manager at HobbySphere.</p>
                <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #2196F3; color: white; text-decoration: none; border-radius: 5px;">Complete Registration</a></p>
                <p>This link will expire in a few days.</p>
            </body>
            </html>
        """.formatted(inviteLink);

        // Send email
        emailService.sendHtmlEmail(email, "Manager Invitation", html);
    }


    @Transactional
    public boolean registerManagerFromInvite(String token, String username, String firstName, String lastName, String password) {
        // 1. Check if token exists
        Optional<PendingManager> pendingOpt = pendingManagerRepository.findByToken(token);
        if (pendingOpt.isEmpty()) {
            return false;
        }

        PendingManager pending = pendingOpt.get();
        Businesses business = pending.getBusiness();

        // ✅ 2. Get MANAGER Role using RoleRepository
        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseThrow(() -> new RuntimeException("Manager role not found"));


        // 3. Check for duplicates (optional good practice)
        if (adminUsersRepository.findByEmail(pending.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists as admin");
        }

        // ✅ 4. Save AdminUser (Manager)
        AdminUsers newManager = new AdminUsers();
        newManager.setUsername(username);
        newManager.setFirstName(firstName);
        newManager.setLastName(lastName);
        newManager.setEmail(pending.getEmail());
        newManager.setPasswordHash(passwordEncoder.encode(password));
        newManager.setRole(managerRole);
        newManager.setBusiness(business);
        newManager.setNotifyActivityUpdates(true);
        newManager.setNotifyUserFeedback(true);
        newManager.setCreatedAt(LocalDateTime.now());
        newManager.setUpdatedAt(LocalDateTime.now());

        adminUsersRepository.save(newManager);

        // 5. Delete the used token
        pendingManagerRepository.delete(pending);

        return true;
    }
    
    public Optional<Businesses> findByIdOptional(Long id) {
        return businessRepository.findById(id);
    }

    public BusinessStatus getStatusByName(String name) {
        return businessStatusRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Status '" + name + "' not found"));
    }


}
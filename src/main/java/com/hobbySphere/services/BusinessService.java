package com.hobbySphere.services;
import com.hobbySphere.entities.Businesses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hobbySphere.repositories.BusinessesRepository; 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

@Service
public class BusinessService {

    @Autowired
    private BusinessesRepository businessRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;


    public Optional<Businesses> findByEmail(String email) {
        return businessRepository.findByEmail(email);
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

    public Businesses findById(Long id) {
        return businessRepository.findById(id).orElse(null);
    }

    public List<Businesses> findAll() {
        return businessRepository.findAll();
    }

    public void delete(Long id) {
        businessRepository.deleteById(id);
    }
    
    public Businesses registerBusiness(
            String name,
            String email,
            String password,
            String description,
            String phoneNumber,
            String websiteUrl,
            MultipartFile logo,
            MultipartFile banner
    ) throws IOException {

        String logoPath = null;
        String bannerPath = null;

        Path uploadDir = Paths.get("uploads/");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        if (logo != null && !logo.isEmpty()) {
            String logoFileName = UUID.randomUUID() + "_" + logo.getOriginalFilename();
            Path logoPathFull = uploadDir.resolve(logoFileName);
            Files.copy(logo.getInputStream(), logoPathFull, StandardCopyOption.REPLACE_EXISTING);
            logoPath = "/uploads/" + logoFileName;
        }

        if (banner != null && !banner.isEmpty()) {
            String bannerFileName = UUID.randomUUID() + "_" + banner.getOriginalFilename();
            Path bannerPathFull = uploadDir.resolve(bannerFileName);
            Files.copy(banner.getInputStream(), bannerPathFull, StandardCopyOption.REPLACE_EXISTING);
            bannerPath = "/uploads/" + bannerFileName;
        }

        Businesses business = new Businesses();
        business.setBusinessName(name);
        business.setEmail(email);
        business.setPasswordHash(passwordEncoder.encode(password));
        business.setDescription(description);
        business.setPhoneNumber(phoneNumber);
        business.setWebsiteUrl(websiteUrl);
        business.setBusinessLogoUrl(logoPath);
        business.setBusinessBannerUrl(bannerPath);

        return businessRepository.save(business);
    }

}

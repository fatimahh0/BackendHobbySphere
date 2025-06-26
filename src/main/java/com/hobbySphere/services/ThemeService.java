package com.hobbySphere.services;

import com.hobbySphere.entities.Theme;
import com.hobbySphere.entities.Businesses;
import com.hobbySphere.entities.Users;
import com.hobbySphere.entities.ThemeAssignment;
import com.hobbySphere.repositories.ThemeRepository;
import com.hobbySphere.repositories.ThemeAssignmentRepository;
import com.hobbySphere.repositories.BusinessesRepository;
import com.hobbySphere.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ThemeService {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ThemeAssignmentRepository themeAssignmentRepository;

    @Autowired
    private BusinessesRepository businessesRepository;

    @Autowired
    private UsersRepository usersRepository;

    // === SUPERADMIN: Global Themes ===

    public Theme saveTheme(Theme theme) {
        return themeRepository.save(theme);
    }

    public List<Theme> getAllThemes() {
        return themeRepository.findAll();
    }

    public Optional<Theme> getThemeById(Long id) {
        return themeRepository.findById(id);
    }

    public void deleteTheme(Long id) {
        themeRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return themeRepository.existsByName(name);
    }

    // === BUSINESS: Assign Theme ===

    public void assignThemeToBusiness(Long businessId, Long themeId) {
        Businesses business = businessesRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme not found"));

        Optional<ThemeAssignment> opt = themeAssignmentRepository.findByBusiness_Id(businessId);
        ThemeAssignment assignment = opt.orElse(new ThemeAssignment());
        assignment.setBusiness(business);
        assignment.setUser(null);
        assignment.setTheme(theme);
        themeAssignmentRepository.save(assignment);
    }

    // === USER: Assign Theme ===

    public void assignThemeToUser(Long userId, Long themeId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme not found"));

        Optional<ThemeAssignment> opt = themeAssignmentRepository.findByUser_Id(userId);
        ThemeAssignment assignment = opt.orElse(new ThemeAssignment());
        assignment.setUser(user);
        assignment.setBusiness(null);
        assignment.setTheme(theme);
        themeAssignmentRepository.save(assignment);
    }

    // === BUSINESS: Get Assigned Theme ===

    public Theme getThemeByBusinessId(Long businessId) {
        return themeAssignmentRepository.findByBusiness_Id(businessId)
                .map(ThemeAssignment::getTheme)
                .orElse(null);
    }

    // === USER: Get Assigned Theme ===

    public Theme getThemeByUserId(Long userId) {
        return themeAssignmentRepository.findByUser_Id(userId)
                .map(ThemeAssignment::getTheme)
                .orElse(null);
        }
}

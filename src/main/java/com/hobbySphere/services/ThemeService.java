package com.hobbySphere.services;

import com.hobbySphere.entities.Theme;

import com.hobbySphere.repositories.ThemeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ThemeService {

    @Autowired
    private ThemeRepository themeRepository;

    // === SUPERADMIN: Global Themes ===

@Transactional
public void setActiveTheme(Long id) {
    System.out.println("Deactivating all themes...");
    themeRepository.deactivateAllThemes();

    Theme theme = themeRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Theme not found"));
    System.out.println("Activating theme: " + theme.getName() + " (id: " + id + ")");
    theme.setIsActive(true);
    themeRepository.save(theme);

    // Print all themes and status
    System.out.println("Themes after activation:");
    for (Theme t : themeRepository.findAll()) {
        System.out.println("  Theme " + t.getId() + ": " + t.getName() + " - active: " + t.getIsActive());
    }
}



   @Transactional
    public Theme saveTheme(Theme theme) {
        if (Boolean.TRUE.equals(theme.getIsActive())) {
            themeRepository.deactivateAllThemes();
        }
        return themeRepository.save(theme);
    }


    public Optional<Theme> getActiveTheme() {
        return themeRepository.findByIsActiveTrue();
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

}
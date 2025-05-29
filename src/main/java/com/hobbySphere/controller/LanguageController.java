package com.hobbySphere.controller;

import com.hobbySphere.entities.Languages;
import com.hobbySphere.enums.LanguageType;
import com.hobbySphere.repositories.LanguageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/languages")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
})
@Tag(name = "Language Management", description = "Operations for viewing available languages")
public class LanguageController {

    @Autowired
    private LanguageRepository languageRepository;

    @Operation(summary = "Get all available languages")
    @GetMapping
    public ResponseEntity<List<Languages>> getAllLanguages() {
        List<Languages> languages = languageRepository.findAll();
        return ResponseEntity.ok(languages);
    }

    @Operation(summary = "Get a language by its name (enum)")
    @GetMapping("/{name}")
    public ResponseEntity<?> getLanguageByName(@PathVariable String name) {
        try {
            LanguageType type = LanguageType.valueOf(name.toUpperCase());
            Optional<Languages> language = languageRepository.findByLanguageName(type);
            return language.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid language name. Use ARABIC, ENGLISH, or FRENCH.");
        }
    }
}

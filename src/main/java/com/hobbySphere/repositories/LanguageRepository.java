package com.hobbySphere.repositories;

import com.hobbySphere.entities.Languages;
import com.hobbySphere.enums.LanguageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Languages, Long> {
    Optional<Languages> findByLanguageName(LanguageType languageName);
}

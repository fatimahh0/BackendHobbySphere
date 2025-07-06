package com.hobbySphere.config;

import com.hobbySphere.entities.Interests;
import com.hobbySphere.enums.IconLibraryEnum;
import com.hobbySphere.enums.InterestEnum;
import com.hobbySphere.enums.InterestIconEnum;
import com.hobbySphere.repositories.InterestsRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class InterestSeeder {

    private final InterestsRepository interestsRepository;

    public InterestSeeder(InterestsRepository interestsRepository) {
        this.interestsRepository = interestsRepository;
    }

    @PostConstruct
    public void seedInterests() {
        for (InterestEnum interestEnum : InterestEnum.values()) {
            String name = interestEnum.name(); 

            if (interestsRepository.findByName(name).isEmpty()) {
                InterestIconEnum iconEnum = mapToIconEnum(name);

                Interests interest = new Interests();
                interest.setName(name);
                interest.setIcon(iconEnum); // ✅ enum used directly
                interest.setIconLib(IconLibraryEnum.FontAwesome5); // ✅ hardcoded for now

                interestsRepository.save(interest);
                System.out.println("✅ Inserted: " + name + " with icon " + iconEnum.name());
            }
        }
    }

    private InterestIconEnum mapToIconEnum(String name) {
        return switch (name) {
            case "SPORTS" -> InterestIconEnum.BASKETBALL;
            case "MUSIC" -> InterestIconEnum.MUSIC;
            case "ART" -> InterestIconEnum.ART;
            case "TECH" -> InterestIconEnum.TECH;
            case "FITNESS" -> InterestIconEnum.FITNESS;
            case "COOKING" -> InterestIconEnum.COOKING;
            case "TRAVEL" -> InterestIconEnum.TRAVEL;
            case "GAMING" -> InterestIconEnum.GAMING;
            case "THEATER" -> InterestIconEnum.THEATER;
            case "LANGUAGE" -> InterestIconEnum.LANGUAGE;
            case "PHOTOGRAPHY" -> InterestIconEnum.PHOTOGRAPHY;
            case "DIY" -> InterestIconEnum.DIY;
            case "BEAUTY" -> InterestIconEnum.BEAUTY;
            case "FINANCE" -> InterestIconEnum.FINANCE;
            default -> InterestIconEnum.OTHER;
        };
    }
}

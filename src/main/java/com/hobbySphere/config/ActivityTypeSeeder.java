package com.hobbySphere.config;

import com.hobbySphere.entities.ActivityType;
import com.hobbySphere.entities.Interests;
import com.hobbySphere.enums.*;
import com.hobbySphere.repositories.ActivityTypeRepository;
import com.hobbySphere.repositories.InterestsRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ActivityTypeSeeder {

    private final ActivityTypeRepository activityTypeRepository;
    private final InterestsRepository interestsRepository;

    public ActivityTypeSeeder(ActivityTypeRepository activityTypeRepository,
                              InterestsRepository interestsRepository) {
        this.activityTypeRepository = activityTypeRepository;
        this.interestsRepository = interestsRepository;
    }

    @PostConstruct
    public void seedActivityTypes() {
        for (ActivityTypeEnum enumValue : ActivityTypeEnum.values()) {
        	String activityTypeName = enumValue.getDisplayName();

            if (activityTypeRepository.findByName(activityTypeName).isEmpty()) {

                String interestName = enumValue.getInterest().name();

                Interests interestEntity = interestsRepository.findByName(interestName)
                        .orElseGet(() -> interestsRepository.save(new Interests(interestName)));

                // Get icon & library from helper method
                ActivityIconEnum iconEnum = getIconForActivity(enumValue);
                IconLibraryEnum iconLibEnum = getIconLibraryForActivity(enumValue);

                ActivityType newType = new ActivityType(
                        activityTypeName,
                        iconEnum,
                        iconLibEnum,
                        interestEntity
                );

                activityTypeRepository.save(newType);
            }
        }
    }

    private ActivityIconEnum getIconForActivity(ActivityTypeEnum type) {
        return switch (type) {
            case HIKING -> ActivityIconEnum.TREE;
            case FOOTBALL -> ActivityIconEnum.FOOTBALL_BALL;
            case YOGA -> ActivityIconEnum.SPA;
            case MUSIC, MUSIC_PRODUCTION -> ActivityIconEnum.MUSIC;
            case DANCE -> ActivityIconEnum.MUSIC;
            case ART, SCULPTING, KNITTING, CALLIGRAPHY -> ActivityIconEnum.PALETTE;
            case CODING, ROBOTICS -> ActivityIconEnum.CODE;
            case COOKING -> ActivityIconEnum.RESTAURANT;
            case GAMING, BOARD_GAMES -> ActivityIconEnum.GAMEPAD;
            case WRITING, PUBLIC_SPEAKING -> ActivityIconEnum.BOOK_OPEN;
            case PHOTOGRAPHY -> ActivityIconEnum.CAMERA;
            case FILM_MAKING -> ActivityIconEnum.VIDEO;
            case FITNESS, SELF_DEFENSE -> ActivityIconEnum.DUMBBELL;
            case TRAVEL, NATURE_WALKS -> ActivityIconEnum.GLOBE;
            case MAKEUP_BEAUTY -> ActivityIconEnum.Heart;
            case THEATER, STAND_UP_COMEDY -> ActivityIconEnum.THEATER_MASKS;
            default -> ActivityIconEnum.STAR; // fallback icon
        };
    }

    private IconLibraryEnum getIconLibraryForActivity(ActivityTypeEnum type) {
        return switch (type) {
            case YOGA, FITNESS -> IconLibraryEnum.FontAwesome5;
            case COOKING -> IconLibraryEnum.MaterialIcons;
            case ART, SCULPTING, KNITTING -> IconLibraryEnum.MaterialCommunityIcons;
            case CODING, ROBOTICS, HIKING, FOOTBALL -> IconLibraryEnum.FontAwesome5;
            case GAMING -> IconLibraryEnum.FontAwesome5;
            case WRITING, PHOTOGRAPHY -> IconLibraryEnum.Feather;
            case MAKEUP_BEAUTY -> IconLibraryEnum.FontAwesome5;
            default -> IconLibraryEnum.FontAwesome5; // default fallback
        };
    }
    
    @PostConstruct
    public void updateExistingActivityTypesWithIcons() {
        for (ActivityType type : activityTypeRepository.findAll()) {
            if (type.getIcon() == null || type.getIconLib() == null) {
                try {
                    ActivityTypeEnum typeEnum = ActivityTypeEnum.valueOf(type.getName());
                    type.setIcon(getIconForActivity(typeEnum));
                    type.setIconLib(getIconLibraryForActivity(typeEnum));
                    activityTypeRepository.save(type);
                    System.out.println("Updated " + type.getName() + " with icon and lib.");
                } catch (IllegalArgumentException e) {
                    System.out.println("No matching enum found for: " + type.getName());
                }
            }
        }
    }

}

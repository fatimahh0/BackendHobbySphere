package com.hobbySphere.enums;

public enum ActivityIconEnum {
    // SPORTS
    FOOTBALL_BALL("football-ball"),
    SPA("spa"),
    DUMBBELL("dumbbell"),
    TREE("tree"),
    HORSE("horse"),
    FISH("fish"),

    // MUSIC
    MUSIC("music"),
    MUSIC_NOTE("music note"),
    HEADPHONES("headphones"),

    // ART
    PALETTE("palette"),
    SCISSORS("scissors"),
    YARN("yarn"),
    PEN_NIB("pen-nib"),

    // TECH
    CODE("code"),
    ROBOT("robot"),
    CUBE("cube"),
    FLASK("flask"),

    // FITNESS
    SELF_DEFENSE("shield-alt"),
    MEDITATION("yin-yang"),

    // COOKING
    RESTAURANT("restaurant"),

    // TRAVEL
    GLOBE("globe"),
    HIKING_BOOTS("hiking"),

    // GAMING
    GAMEPAD("gamepad"),
    CHESS("chess-board"),

    // THEATER
    THEATER_MASKS("theater-masks"),
    LAUGH("laugh"),
    BOOK_OPEN("book-open"),

    // LANGUAGE
    LANGUAGE("language"),
    MICROPHONE("microphone"),
    PEN("pen"),

    // PHOTOGRAPHY
    CAMERA("camera"),
    VIDEO("video"),

    // DIY
    HAMMER("hammer"),
    TOOLS("tools"),
    RULER("ruler-combined"),

    // BEAUTY
    LIPSTICK("lipstick"),
    Heart("heart"),

    // FINANCE
    CHART_LINE("chart-line"),
    BRIEFCASE("briefcase"),

    // OTHER
    DOG("dog"),
    PODCAST("podcast"),
    MAGIC("magic"),
    STAR("star"),
    HAND_HOLDING_HEART("hand-holding-heart"),
    CLOCK("clock"),
    BINOCULARS("binoculars"),
    PEOPLE_ARROWS("people-arrows");

    private final String iconName;

    ActivityIconEnum(String iconName) {
        this.iconName = iconName;
    }

    public String getIconName() {
        return iconName;
    }
}

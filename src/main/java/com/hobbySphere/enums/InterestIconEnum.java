package com.hobbySphere.enums;

public enum InterestIconEnum {
    BASKETBALL("basketball-ball"),
    MUSIC("music"),
    ART("palette"),
    TECH("laptop-code"),
    FITNESS("dumbbell"),
    COOKING("utensils"),
    TRAVEL("plane"),
    GAMING("gamepad"),
    THEATER("theater-masks"),
    LANGUAGE("language"),
    PHOTOGRAPHY("camera"),
    DIY("tools"),
    BEAUTY("spa"),
    FINANCE("wallet"),
    OTHER("star");

    private final String iconName;

    InterestIconEnum(String iconName) {
        this.iconName = iconName;
    }

    public String getIconName() {
        return iconName;
    }
}


package com.hobbySphere.enums;

public enum DefaultCurrencies {
    DOLLAR(CurrencyType.DOLLAR, "$"),
    EURO(CurrencyType.EURO, "€"),
    CAD(CurrencyType.CAD, "C$");

    private final CurrencyType type;
    private final String symbol;

    DefaultCurrencies(CurrencyType type, String symbol) {
        this.type = type;
        this.symbol = symbol;
    }

    public CurrencyType getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }
}

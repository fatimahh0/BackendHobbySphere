package com.hobbySphere.config;

import com.hobbySphere.entities.Currency;
import com.hobbySphere.enums.DefaultCurrencies;
import com.hobbySphere.repositories.CurrencyRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class CurrencySeeder {

    private final CurrencyRepository currencyRepository;

    public CurrencySeeder(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @PostConstruct
    public void seedCurrencies() {
        seed("DOLLAR", "$");
        seed("EURO", "€");
        seed("CAD", "C$");
    }

    private void seed(String currencyType, String symbol) {
        currencyRepository.findByCurrencyType(currencyType)
            .orElseGet(() -> currencyRepository.save(new Currency(currencyType, symbol)));
    }

}

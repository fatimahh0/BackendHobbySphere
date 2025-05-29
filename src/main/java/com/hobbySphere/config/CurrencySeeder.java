package com.hobbySphere.config;

import com.hobbySphere.entities.Currency;
import com.hobbySphere.enums.CurrencyType;
import com.hobbySphere.repositories.CurrencyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CurrencySeeder {

    @Bean
    CommandLineRunner seedCurrencies(CurrencyRepository currencyRepository) {
        return args -> {
            for (CurrencyType type : CurrencyType.values()) {
                if (!currencyRepository.existsByCurrencyType(type)) {
                    currencyRepository.save(new Currency(type));
                }
            }
        };
    }
}

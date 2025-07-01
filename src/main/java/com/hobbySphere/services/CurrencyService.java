package com.hobbySphere.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hobbySphere.entities.Currency;
import com.hobbySphere.repositories.CurrencyRepository;

import java.util.Map;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    public void ensureDefaultCurrencies() {
      
        Map<String, String> defaultCurrencies = Map.of(
                "DOLLAR", "$",
                "EURO", "€",
                "CAD", "C$"
        );

        for (Map.Entry<String, String> entry : defaultCurrencies.entrySet()) {
            String type = entry.getKey();
            String symbol = entry.getValue();

            currencyRepository.findByCurrencyType(type).ifPresentOrElse(
                existing -> {
                    if (!existing.getSymbol().equals(symbol)) {
                        existing.setSymbol(symbol);
                        currencyRepository.save(existing);
                    }
                },
                () -> {
                    Currency currency = new Currency(type, symbol);
                    currencyRepository.save(currency);
                }
            );
        }
    }
}

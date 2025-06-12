package com.hobbySphere.config;
import com.hobbySphere.entities.Currency;
import com.hobbySphere.enums.CurrencyType;
import com.hobbySphere.repositories.CurrencyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class CurrencySeeder implements CommandLineRunner {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Override
    public void run(String... args) {
        insertCurrencyIfNotExists(CurrencyType.DOLLAR, "$");
        insertCurrencyIfNotExists(CurrencyType.EURO, "€");
        insertCurrencyIfNotExists(CurrencyType.CAD, "C$");
    }

    private void insertCurrencyIfNotExists(CurrencyType type, String symbol) {
        currencyRepository.findByCurrencyType(type).ifPresentOrElse(
            existing -> {
                // Update symbol if different
                if (!symbol.equals(existing.getSymbol())) {
                    existing.setSymbol(symbol);
                    currencyRepository.save(existing);
                }
            },
            () -> {
                Currency newCurrency = new Currency(type, symbol);
                currencyRepository.save(newCurrency);
            }
        );
    }
}

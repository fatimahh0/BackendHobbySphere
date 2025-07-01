package com.hobbySphere.repositories;

import java.util.Optional; // ✅ Correct
import com.hobbySphere.entities.Currency;
import com.hobbySphere.enums.CurrencyType;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    
    boolean existsByCurrencyType(CurrencyType currencyType);
    
    Optional<Currency> findByCurrencyType(CurrencyType currencyType);
    
    boolean existsByCurrencyType(String currencyType);

    Optional<Currency> findByCurrencyType(String currencyType);

}

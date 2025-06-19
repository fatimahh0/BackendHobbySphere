package com.hobbySphere.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hobbySphere.entities.Currency;
import com.hobbySphere.enums.DefaultCurrencies;
import com.hobbySphere.repositories.CurrencyRepository;

@Service
public class CurrencyService {
	
	@Autowired 
	CurrencyRepository currencyRepository; 
	
	public void ensureDefaultCurrencies() {
	    for (DefaultCurrencies defaultCurrency : DefaultCurrencies.values()) {
	        currencyRepository.findByCurrencyType(defaultCurrency.getType()).ifPresentOrElse(
	            existing -> {
	                // Optional: update symbol if it's different
	                if (!existing.getSymbol().equals(defaultCurrency.getSymbol())) {
	                    existing.setSymbol(defaultCurrency.getSymbol());
	                    currencyRepository.save(existing);
	                }
	            },
	            () -> {
	                Currency currency = new Currency(defaultCurrency.getType(), defaultCurrency.getSymbol());
	                currencyRepository.save(currency);
	            }
	        );
	    }
	}


}

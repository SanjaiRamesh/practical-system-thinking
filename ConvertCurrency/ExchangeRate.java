package ConvertCurrency;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


public class ExchangeRate {


    Map<String , BigDecimal> exchangeRates = new HashMap<>();

    public void initialize() {
        exchangeRates.put("INR#USD", new BigDecimal("0.01"));
        exchangeRates.put("INR#EUR", new BigDecimal("0.01"));
        exchangeRates.put("INR#GBP", new BigDecimal("0.01"));
        exchangeRates.put("INR#SGD", new BigDecimal("0.01"));
    }

    public BigDecimal exchangeRate(SourceCurrency sourceCurrency, DestinationCurrency destinationCurrency) {
        BigDecimal exchangeRate = exchangeRates.get(sourceCurrency+"#"+destinationCurrency);



        return exchangeRate;
    }
}

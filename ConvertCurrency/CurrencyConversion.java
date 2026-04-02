package ConvertCurrency;

import java.math.BigDecimal;

public class CurrencyConversion {
    ExchangeRate exchangeRate ;
    public CurrencyConversion(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
        this.exchangeRate.initialize();
    }
    public BigDecimal convert(BigDecimal amount, SourceCurrency sourceCurrency, DestinationCurrency destinationCurrency) {

        BigDecimal rate = exchangeRate.exchangeRate(sourceCurrency, destinationCurrency);

        return amount.multiply(rate);
    }

    public static void main(String[] args) {
        CurrencyConversion currencyConversion = new CurrencyConversion(new ExchangeRate());

        System.out.println(currencyConversion.convert(BigDecimal.valueOf(200), SourceCurrency.INR, DestinationCurrency.USD));

    }
}

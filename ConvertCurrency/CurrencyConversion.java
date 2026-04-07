package ConvertCurrency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class CurrencyConversion {
    ExchangeRate exchangeRate ;
    FeeService feeService ;
    public CurrencyConversion(ExchangeRate exchangeRate,FeeService feeService) {
        this.exchangeRate = exchangeRate;
        this.exchangeRate.initialize();
        this.feeService = feeService;
        this.feeService.initialize();
    }
    private boolean skipCurrencyConversion(BigDecimal amount, SourceCurrency sourceCurrency, DestinationCurrency destinationCurrency) {

        return  BigDecimal.ZERO.compareTo(amount) == 0 || sourceCurrency.name().equals(destinationCurrency.name());
    }
    public BigDecimal convert(UserType userType, BigDecimal amount, SourceCurrency sourceCurrency, DestinationCurrency destinationCurrency) {

        BigDecimal convertedAmount = amount ;
        if( amount == null || sourceCurrency == null || destinationCurrency == null ) {
            throw new RuntimeException("Invalid arguments");
        }
        if( skipCurrencyConversion(amount, sourceCurrency, destinationCurrency) ) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }

        Optional<BigDecimal> feePercent = feeService.getFee(userType,sourceCurrency, destinationCurrency);

        if( feePercent.isPresent() ) {

            BigDecimal fee = amount.multiply(feePercent.get());
            convertedAmount = amount.subtract(fee);

        }

        Optional<BigDecimal> rate = exchangeRate.exchangeRate(userType,sourceCurrency, destinationCurrency);

        if(rate.isEmpty()) {
            List<String> path = exchangeRate.getPath(userType, sourceCurrency, destinationCurrency);
            if(path.isEmpty()) {
                throw new RuntimeException("exchange rate not found");
            }
            for(int i=1;i<path.size();i++) {
                Optional<BigDecimal> mulRate = exchangeRate.exchangeRate(userType, SourceCurrency.valueOf(path.get(i - 1)), DestinationCurrency.valueOf(path.get(i)));
                if(mulRate.isEmpty()) {
                    throw new RuntimeException("exchange rate not found");
                }
                convertedAmount = convertedAmount.multiply(mulRate.get());
            }
        } else {
            convertedAmount = convertedAmount.multiply(rate.get());
        }

        return convertedAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public static void main(String[] args) {
        CurrencyConversion currencyConversion = new CurrencyConversion(new ExchangeRate(), new FeeService());
        System.out.println(currencyConversion.convert(UserType.NORMAL, BigDecimal.valueOf(200), SourceCurrency.INR, DestinationCurrency.GBP));
//
//        double dValue = 10;
//        double dvalue1 = 9.90;
//        double dvalue2 = dValue-dvalue1;
//        System.out.println(dvalue2);
//
//        BigDecimal value = new BigDecimal("10");
//        BigDecimal value1 = new BigDecimal("9.90");
//        BigDecimal value2 = value.subtract(value1);
//        System.out.println(value2+BigDecimal.);
    }
}

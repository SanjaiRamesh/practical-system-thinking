package ConvertCurrency;

import java.math.BigDecimal;
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
    public BigDecimal convert(UserType userType, BigDecimal amount, SourceCurrency sourceCurrency, DestinationCurrency destinationCurrency) {

        BigDecimal convertedAmount = amount ;
        Optional<BigDecimal> rate = exchangeRate.exchangeRate(userType,sourceCurrency, destinationCurrency);

        if(rate.isEmpty()) {
            rate = Optional.of(BigDecimal.ONE);
            List<String> path = exchangeRate.getPath(userType, sourceCurrency, destinationCurrency);
            for(int i=1;i<path.size();i++) {
                Optional<BigDecimal> mulRate = exchangeRate.exchangeRate(userType, SourceCurrency.valueOf(path.get(i - 1)), DestinationCurrency.valueOf(path.get(i)));
                if(mulRate.isEmpty()) {
                    throw new RuntimeException("exchange rate not found");
                }
                convertedAmount = convertedAmount.multiply(mulRate.get());
                convertedAmount = convertedAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        } else {
            convertedAmount = convertedAmount.multiply(rate.get()).setScale(2, BigDecimal.ROUND_HALF_UP);;
        }
        Optional<BigDecimal> fee = feeService.getFee(userType,sourceCurrency, destinationCurrency);

        if( fee.isPresent() ) {

            return convertedAmount.multiply(fee.get().add(BigDecimal.ONE)).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return convertedAmount;
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

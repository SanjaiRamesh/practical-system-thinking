package ConvertCurrency;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FeeService {

    Map<UserType, Map<String , BigDecimal>> fee = new HashMap<>();

    public void initialize() {
        fee.put(UserType.NORMAL, Map.ofEntries(Map.entry("INR#USD", new BigDecimal("0.01")),Map.entry("INR#EUR", new BigDecimal("0.01")),Map.entry("INR#GBP", new BigDecimal("0.01"))));
        fee.put(UserType.GOLD, Map.ofEntries(Map.entry("INR#USD", new BigDecimal("0.01")),Map.entry("INR#EUR", new BigDecimal("0.01")),Map.entry("INR#GBP", new BigDecimal("0.01"))));
    }
    public Optional<BigDecimal> getFee(UserType userType, SourceCurrency sourceCurrency, DestinationCurrency destinationCurrency) {

        Map<String, BigDecimal> exchageRateMap =fee.get(userType);
        if (exchageRateMap != null) {
            return Optional.of(exchageRateMap.get(sourceCurrency+"#"+destinationCurrency));
        }
        exchageRateMap =fee.get(UserType.NORMAL);
        if (exchageRateMap != null) {
            return Optional.of(exchageRateMap.get(sourceCurrency+"#"+destinationCurrency));
        }

        return Optional.empty();
    }
}

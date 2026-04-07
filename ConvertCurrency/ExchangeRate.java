package ConvertCurrency;

import java.math.BigDecimal;
import java.util.*;


public class ExchangeRate {


    Map<UserType,Map<String , BigDecimal>> exchangeRates = new HashMap<>();

    public void initialize() {
        exchangeRates.put(UserType.NORMAL, Map.ofEntries(Map.entry("INR#USD", new BigDecimal("0.012")),Map.entry("INR#EUR", new BigDecimal("0.01")),Map.entry("USD#GBP", new BigDecimal("0.85")),Map.entry("GBP#INR", new BigDecimal("98"))));
        exchangeRates.put(UserType.GOLD, Map.ofEntries(Map.entry("INR#USD", new BigDecimal("0.01")),Map.entry("INR#EUR", new BigDecimal("0.01")),Map.entry("INR#GBP", new BigDecimal("0.01"))));
    }
    public Optional<BigDecimal> exchangeRate(UserType userType, SourceCurrency sourceCurrency, DestinationCurrency destinationCurrency) {

        Map<String, BigDecimal> exchageRateMap =exchangeRates.get(userType);
        if (exchageRateMap != null) {
            return exchageRateMap.get(sourceCurrency+"#"+destinationCurrency)==null? Optional.empty(): Optional.of(exchageRateMap.get(sourceCurrency+"#"+destinationCurrency));
        }
        exchageRateMap =exchangeRates.get(UserType.NORMAL);
        if (exchageRateMap != null) {
            return exchageRateMap.get(sourceCurrency+"#"+destinationCurrency)==null? Optional.empty(): Optional.of(exchageRateMap.get(sourceCurrency+"#"+destinationCurrency));
        }

        return Optional.empty();
    }

    public List<String> getPath(UserType userType, SourceCurrency sourceCurrency, DestinationCurrency destinationCurrency) {

        Map<String, BigDecimal> exchangeMap= exchangeRates.get(userType);

        if( exchangeMap == null ) {
            return Collections.emptyList();
        }
        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(List.of(sourceCurrency.name()));

        while (!queue.isEmpty()) {
            List<String> path = queue.poll();
            String latestCurrency =  path.get(path.size()-1);

            if(latestCurrency.equals(destinationCurrency.name())) {
                return path;
            }
            if(!visited.contains(latestCurrency)) {
                visited.add(latestCurrency);

                for(String parts: exchangeMap.keySet()){
                    String[] part = parts.split("#");

                    if(part[0].equals(latestCurrency)){
                        List<String> newPath = new ArrayList<>(path);
                        newPath.add(part[1]);
                        queue.offer(newPath);
                    }
                }

            }
        }
        return Collections.emptyList();
    }

}

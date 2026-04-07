package wallet;

import ConvertCurrency.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserWallet implements Wallet {
    private String userId;
    private Map<String, BigDecimal> balance;
    private CurrencyConversion currencyConversion;

    public UserWallet(String userId, Map<String, BigDecimal> balance, CurrencyConversion currencyConversion) {
        this.userId = userId;
        this.balance = balance;
        this.currencyConversion = currencyConversion;
    }

    @Override
    public synchronized BigDecimal credit(Money amount, String currency) {

        if (amount == null || !amount.isValidMoney() || currency == null || currency.isBlank()) {
            throw new RuntimeException("Invalid Input");
        }
        BigDecimal convertedAmount = BigDecimal.ZERO;
        if (currency.equalsIgnoreCase(amount.getCurrency())) {
            convertedAmount = amount.getAmount();
        } else {
            convertedAmount = currencyConversion.convert(UserType.NORMAL, amount.getAmount(), SourceCurrency.valueOf(amount.getCurrency()), DestinationCurrency.valueOf(currency));

        }
        balance.put(currency, balance.getOrDefault(currency, BigDecimal.ZERO).add(convertedAmount));
        return balance.get(currency);
    }

    @Override
    public synchronized BigDecimal debit(Money amount, String currency) {

        if (amount == null || !amount.isValidMoney() || currency == null || currency.isBlank()) {
            throw new RuntimeException("Invalid Input");
        }
        if(balance.getOrDefault(currency, BigDecimal.ZERO).compareTo(BigDecimal.ZERO)  <= 0) {
            throw new RuntimeException("Insufficient balance");
        }
        BigDecimal convertedAmount = BigDecimal.ZERO;
        if (currency.equalsIgnoreCase(amount.getCurrency())) {
            convertedAmount = amount.getAmount();
        } else {
            convertedAmount = currencyConversion.convert(UserType.NORMAL, amount.getAmount(), SourceCurrency.valueOf(amount.getCurrency()), DestinationCurrency.valueOf(currency));

        }
        balance.put(currency, balance.getOrDefault(currency, BigDecimal.ZERO).subtract(convertedAmount));
        return balance.get(currency);
    }

    @Override
    public synchronized void transfer(Money amount, String currency) {

        if( amount ==  null || !amount.isValidMoney() || currency == null || currency.isBlank()  ) {
            throw new RuntimeException("Invalid Input");
        }
        if( currency.equalsIgnoreCase(amount.getCurrency()) ) {
            return;
        }
        BigDecimal sourceBalance = balance.getOrDefault(amount.getCurrency(), BigDecimal.ZERO);
        if(sourceBalance.compareTo(amount.getAmount())<0)
            throw new RuntimeException("Insufficient balance");

        this.debit(amount,amount.getCurrency());
        this.credit(amount,currency);

    }

    public static void main(String[] args) throws InterruptedException {
        UserWallet userWallet = new UserWallet("sanjai", new ConcurrentHashMap<>(), new CurrencyConversion(new ExchangeRate(), new FeeService()));
        System.out.println("User Id : " + userWallet.userId);
        System.out.println("Balance : " + userWallet.balance);
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                userWallet.credit(new Money(new BigDecimal("100"), "INR"), "GBP");
            }

        });
        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                userWallet.credit(new Money(new BigDecimal("100"), "INR"), "GBP");
            }
        });
        Thread thread3 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                userWallet.credit(new Money(new BigDecimal("100"), "INR"), "INR");
            }
        });
        Thread thread4 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                userWallet.transfer(new Money(new BigDecimal("1"), "GBP"), "INR");
            }
        });
//        userWallet.credit( new Money(new BigDecimal("100.25"), "INR"), "USD");
//        System.out.println("Balance : " +userWallet.balance);
//        userWallet.credit( new Money(new BigDecimal("100.25"), "USD"), "USD");
//        System.out.println("Balance : " +userWallet.balance);
//        userWallet.debit( new Money(new BigDecimal("1.25"), "USD"), "USD");
//        System.out.println("Balance : " +userWallet.balance);
//        userWallet.credit( new Money(new BigDecimal("100.25"), "INR"), "GBP");
//        System.out.println("Balance : " +userWallet.balance);
//        userWallet.credit( new Money(new BigDecimal("100.25"), "USD"), "GBP");
        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();

//        try {
//            userWallet.debit( new Money(new BigDecimal("100.25"), "INR"), "GBP");
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }

        System.out.println("Balance : " + userWallet.balance);
        thread4.start();
        thread4.join();
        System.out.println("Balance : " + userWallet.balance);
    }
}

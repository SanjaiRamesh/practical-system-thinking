package wallet;

import java.math.BigDecimal;

public interface Wallet {
    BigDecimal credit(Money amount, String currency);
    BigDecimal debit(Money amount, String currency);
    void transfer(Money amount, String currency);
}

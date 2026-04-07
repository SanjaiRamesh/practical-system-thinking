package wallet;

import java.math.BigDecimal;

public class Money {
    private BigDecimal amount;
    private String currency;


    public Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public boolean isValidMoney(){
        return amount!=null && currency!=null && amount.compareTo(BigDecimal.ZERO)>0 && !currency.isBlank();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

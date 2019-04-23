package pl.com.bottega.ecommerce.builders;

import java.math.BigDecimal;
import java.util.Currency;

import pl.com.bottega.ecommerce.sharedkernel.Money;

public class MoneyBuilder {

    private Currency currency = Money.DEFAULT_CURRENCY;
    private BigDecimal denomination = BigDecimal.ZERO;

    public MoneyBuilder() {};

    public MoneyBuilder currency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public MoneyBuilder denomination(BigDecimal denomination) {
        this.denomination = denomination;
        return this;
    }

    public Money build() {
        return new Money(this.denomination, this.currency);
    }
}

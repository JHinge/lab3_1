package pl.com.bottega.ecommerce.builders;

import java.math.BigDecimal;
import java.util.Date;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class ProductDataBuilder {

    private Id productId = Id.generate();
    private Money price = new Money(BigDecimal.ZERO);
    private String name = "no_name";
    private Date snapshotDate = new Date();
    private ProductType type = ProductType.STANDARD;

    public ProductDataBuilder id(Id id) {
        this.productId = id;
        return this;
    }

    public ProductDataBuilder price(Money price) {
        this.price = price;
        return this;
    }

    public ProductDataBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ProductDataBuilder snapshotDate(Date date) {
        this.snapshotDate = date;
        return this;
    }

    public ProductDataBuilder type(ProductType type) {
        this.type = type;
        return this;
    }

    public ProductData build() {
        return new ProductData(productId, price, name, type, snapshotDate);
    }
}

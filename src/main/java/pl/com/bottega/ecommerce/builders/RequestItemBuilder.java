package pl.com.bottega.ecommerce.builders;

import java.math.BigDecimal;
import java.util.Date;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.invoicing.RequestItem;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class RequestItemBuilder {

    private ProductData productData = new ProductData(Id.generate(), new Money(BigDecimal.ZERO), "no_name", ProductType.STANDARD,
            new Date());
    private int quantity = 0;

    public RequestItemBuilder() {};

    public RequestItemBuilder productData(ProductData productData) {
        this.productData = productData;
        return this;
    }

    public RequestItemBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public RequestItem build() {
        return new RequestItem(productData, quantity, productData.getPrice()
                                                                 .multiplyBy(quantity));
    }
}

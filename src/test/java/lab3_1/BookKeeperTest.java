package lab3_1;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.invoicing.BookKeeper;
import pl.com.bottega.ecommerce.sales.domain.invoicing.Invoice;
import pl.com.bottega.ecommerce.sales.domain.invoicing.InvoiceFactory;
import pl.com.bottega.ecommerce.sales.domain.invoicing.InvoiceRequest;
import pl.com.bottega.ecommerce.sales.domain.invoicing.RequestItem;
import pl.com.bottega.ecommerce.sales.domain.invoicing.Tax;
import pl.com.bottega.ecommerce.sales.domain.invoicing.TaxPolicy;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class BookKeeperTest {

    private BookKeeper bookKeeper;
    private InvoiceRequest invoiceRequest;
    private TaxPolicy taxPolicy;
    private Id id;
    private ClientData client;
    private ProductData productData;

    @Before
    public void initialize() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        id = Id.generate();
        client = new ClientData(id, "Jarosław");
        invoiceRequest = new InvoiceRequest(client);
        taxPolicy = mock(TaxPolicy.class);
        productData = new ProductData(Id.generate(), new Money(new BigDecimal(100), Currency.getInstance("EUR")), "name", ProductType.DRUG,
                new Date());
    }

    @Test
    public void shouldRetrunInvoiceWithOnePosition() {
        int quantity = 5;
        Money totalCost = productData.getPrice()
                                     .multiplyBy(quantity);
        RequestItem item = new RequestItem(productData, quantity, totalCost);
        invoiceRequest.add(item);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(100), Currency.getInstance("EUR")), "Podatek od towarów i usług (VAT)"));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems()
                          .get(0)
                          .getProduct(),
                equalTo(productData));
        assertThat(invoice.getItems()
                          .size(),
                equalTo(1));

    }

    @Test
    public void shouldCallCalculateTaxTwiceWhenInoviceHasTwoPositions() {
        productData = new ProductData(Id.generate(), new Money(new BigDecimal(7822.11), Currency.getInstance("USD")), "next_name",
                ProductType.DRUG, new Date());
        int quantity = 5;
        Money totalCost = productData.getPrice()
                                     .multiplyBy(quantity);
        RequestItem item = new RequestItem(productData, quantity, totalCost);
        invoiceRequest.add(item);

        productData = new ProductData(Id.generate(), new Money(new BigDecimal(34.81), Currency.getInstance("USD")), "next_name",
                ProductType.FOOD, new Date());
        quantity = 12;
        item = new RequestItem(productData, quantity, totalCost);
        invoiceRequest.add(item);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(100), Currency.getInstance("USD")), "Podatek od towarów i usług (VAT)"));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(2)).calculateTax(any(), any());

    }

}

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

import pl.com.bottega.ecommerce.builders.BookKeeperBuilder;
import pl.com.bottega.ecommerce.builders.ClientDataBuilder;
import pl.com.bottega.ecommerce.builders.InvoiceRequestBuilder;
import pl.com.bottega.ecommerce.builders.MoneyBuilder;
import pl.com.bottega.ecommerce.builders.ProductDataBuilder;
import pl.com.bottega.ecommerce.builders.RequestItemBuilder;
import pl.com.bottega.ecommerce.builders.TaxBuilder;
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
    private Money price;

    BookKeeperBuilder bookKeeperBuilder;
    InvoiceRequestBuilder invoiceRequestBuilder;
    ClientDataBuilder clientDataBuilder;
    RequestItemBuilder requestItemBuilder;
    MoneyBuilder moneyBuilder;
    ProductDataBuilder productDataBuilder;
    TaxBuilder taxBuilder;

    @Before
    public void initialize() {
        bookKeeperBuilder = new BookKeeperBuilder();
        invoiceRequestBuilder = new InvoiceRequestBuilder();
        clientDataBuilder = new ClientDataBuilder();
        requestItemBuilder = new RequestItemBuilder();
        moneyBuilder = new MoneyBuilder();
        productDataBuilder = new ProductDataBuilder();
        taxBuilder = new TaxBuilder();

        bookKeeper = bookKeeperBuilder.build();
        id = Id.generate();
        client = clientDataBuilder.name("Jarosław")
                                  .id(id)
                                  .build();
        invoiceRequest = invoiceRequestBuilder.clientData(client)
                                              .build();

        taxPolicy = mock(TaxPolicy.class);

        price = moneyBuilder.denomination(new BigDecimal(100))
                            .currency(Currency.getInstance("EUR"))
                            .build();
        productData = productDataBuilder.id(Id.generate())
                                        .price(price)
                                        .type(ProductType.DRUG)
                                        .snapshotDate(new Date())
                                        .build();
    }

    @Test
    public void shouldRetrunInvoiceWithOnePosition() {
        int quantity = 5;

        RequestItem item = requestItemBuilder.productData(productData)
                                             .quantity(quantity)
                                             .build();
        invoiceRequest.add(item);
        price = moneyBuilder.build();
        Tax tax = taxBuilder.amount(price)
                            .description("Podatek od towarów i usług (VAT)")
                            .build();

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(tax);
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
        price = moneyBuilder.denomination(new BigDecimal(7822.11))
                            .currency(Currency.getInstance("USD"))
                            .build();

        productData = productDataBuilder.price(price)
                                        .name("next_name")
                                        .snapshotDate(new Date())
                                        .build();
        int quantity = 5;

        RequestItem item = requestItemBuilder.productData(productData)
                                             .quantity(quantity)
                                             .build();
        invoiceRequest.add(item);

        price = moneyBuilder.denomination(new BigDecimal(34.81))
                            .build();

        productData = productDataBuilder.price(price)
                                        .name("next_name")
                                        .snapshotDate(new Date())
                                        .type(ProductType.FOOD)
                                        .build();
        quantity = 12;
        item = requestItemBuilder.productData(productData)
                                 .quantity(quantity)
                                 .build();
        invoiceRequest.add(item);
        Tax tax = taxBuilder.amount(price)
                            .description("Podatek od towarów i usług (VAT)")
                            .build();
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(tax);
        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(2)).calculateTax(any(), any());

    }

    @Test
    public void shouldReturnEmptyInvoiceIfAnyProducIsAdded() {
        price = moneyBuilder.denomination(new BigDecimal(770))
                            .currency(Currency.getInstance("USD"))
                            .build();
        Tax tax = taxBuilder.amount(price)
                            .description("Podatek od towarów i usług (VAT)")
                            .build();
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(tax);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems()
                          .size(),
                equalTo(0));

    }

    @Test
    public void shouldNotCallCalculateTaxTwiceWhenInoviceIsEmpty() {

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(103), Currency.getInstance("USD")), "Podatek od towarów i usług (VAT)"));
        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(0)).calculateTax(any(), any());

    }

    @Test
    public void shouldReturnInvoiceWithTwoPositionsWhenInoviceRequestHasTwoPositionsAdded() {
        productData = new ProductData(Id.generate(), new Money(new BigDecimal(7822.11), Currency.getInstance("USD")), "next_name",
                ProductType.DRUG, new Date());
        int quantity = 5;
        Money totalCost = productData.getPrice()
                                     .multiplyBy(quantity);
        RequestItem item = new RequestItem(productData, quantity, totalCost);
        invoiceRequest.add(item);

        ProductData secondProductData = new ProductData(Id.generate(), new Money(new BigDecimal(999.81), Currency.getInstance("USD")),
                "another_name", ProductType.STANDARD, new Date());
        quantity = 12;
        item = new RequestItem(secondProductData, quantity, totalCost);
        invoiceRequest.add(item);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(100), Currency.getInstance("USD")), "Podatek od towarów i usług (VAT)"));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems()
                          .get(0)
                          .getProduct(),
                equalTo(productData));
        assertThat(invoice.getItems()
                          .get(1)
                          .getProduct(),
                equalTo(secondProductData));
        assertThat(invoice.getItems()
                          .size(),
                equalTo(2));

    }

    @Test
    public void testIfMethodCreateFromInvoiceFactoryIsCalledOnce() {
        InvoiceFactory invoiceFactory = mock(InvoiceFactory.class);
        bookKeeper = new BookKeeper(invoiceFactory);
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(7870), Currency.getInstance("CHF")), "Podatek od towarów i usług (VAT)"));
        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(invoiceFactory, times(1)).create(any());
    }
}


package lab3_1;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.application.api.command.AddProductCommand;
import pl.com.bottega.ecommerce.sales.application.api.handler.AddProductCommandHandler;
import pl.com.bottega.ecommerce.sales.domain.client.Client;
import pl.com.bottega.ecommerce.sales.domain.client.ClientRepository;
import pl.com.bottega.ecommerce.sales.domain.equivalent.SuggestionService;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductRepository;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sales.domain.reservation.Reservation;
import pl.com.bottega.ecommerce.sales.domain.reservation.ReservationRepository;
import pl.com.bottega.ecommerce.sharedkernel.Money;
import pl.com.bottega.ecommerce.system.application.SystemContext;

public class AddProductCommandHandlerTest {

    private AddProductCommandHandler addProductCommandHandler;
    private ReservationRepository reservationRepository;
    private ProductRepository productRepository;
    private SuggestionService suggestionService;
    private ClientRepository clientRepository;
    private SystemContext systemContext;
    private AddProductCommand productCommand;
    private Reservation reservationSpy;
    Reservation reservation;
    Product product;
    AddProductCommand addProductCommand;

    @Before
    public void initialize() {
        addProductCommandHandler = new AddProductCommandHandler();

        reservationRepository = mock(ReservationRepository.class);
        productRepository = mock(ProductRepository.class);
        clientRepository = mock(ClientRepository.class);
        suggestionService = mock(SuggestionService.class);
        productCommand = new AddProductCommand(new Id("1"), new Id("2"), 6);
        systemContext = new SystemContext();
        reservation = new Reservation(Id.generate(), Reservation.ReservationStatus.OPENED, new ClientData(Id.generate(), "Client"),
                new Date());
        product = new Product(Id.generate(), new Money(new BigDecimal(132)), "no_name", ProductType.FOOD);

        // client = new Client();

        Whitebox.setInternalState(addProductCommandHandler, "reservationRepository", reservationRepository);
        Whitebox.setInternalState(addProductCommandHandler, "productRepository", productRepository);
        Whitebox.setInternalState(addProductCommandHandler, "suggestionService", suggestionService);
        Whitebox.setInternalState(addProductCommandHandler, "clientRepository", clientRepository);
        Whitebox.setInternalState(addProductCommandHandler, "systemContext", systemContext);

        reservationSpy = spy(reservation);
    }

    @Test
    public void shouldLoadReservationOnceInHandleMethod() {

        when(reservationRepository.load(any(Id.class))).thenReturn(reservation);
        when(productRepository.load(any(Id.class))).thenReturn(product);
        addProductCommandHandler.handle(productCommand);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    public void shouldNotCallSuggestEquivalentIfProductIsAvailable() {
        when(reservationRepository.load(productCommand.getOrderId())).thenReturn(reservation);
        when(productRepository.load(productCommand.getProductId())).thenReturn(product);

        SuggestionService suggestionService = mock(SuggestionService.class);
        addProductCommandHandler.handle(productCommand);

        assertThat(product.isAvailable(), equalTo(true));
        verify(suggestionService, never()).suggestEquivalent(any(Product.class), any(Client.class));
    }

    @Test
    public void shouldUseAddMethodOnce() {
        when(reservationRepository.load(any(Id.class))).thenReturn(reservationSpy);
        when(productRepository.load(any(Id.class))).thenReturn(product);
        addProductCommandHandler.handle(productCommand);
        verify(reservationSpy, times(1)).add(product, 6);
    }
}

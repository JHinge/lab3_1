
package lab3_1;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
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
    private Client client;
    Reservation reservation;
    Product product;

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
        client = new Client();

        Whitebox.setInternalState(addProductCommandHandler, "reservationRepository", reservationRepository);
        Whitebox.setInternalState(addProductCommandHandler, "productRepository", productRepository);
        Whitebox.setInternalState(addProductCommandHandler, "suggestionService", suggestionService);
        Whitebox.setInternalState(addProductCommandHandler, "clientRepository", clientRepository);
        Whitebox.setInternalState(addProductCommandHandler, "systemContext", systemContext);
    }

    @Test
    public void shouldLoadReservationOnceInHandleMethod() {

        when(reservationRepository.load(any(Id.class))).thenReturn(reservation);
        when(productRepository.load(any(Id.class))).thenReturn(product);
        addProductCommandHandler.handle(productCommand);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }
}

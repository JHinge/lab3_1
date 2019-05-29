package pl.com.bottega.ecommerce.sales.domain.reservation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.com.bottega.ddd.support.domain.BaseAggregateRoot;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.offer.Discount;
import pl.com.bottega.ecommerce.sales.domain.offer.DiscountPolicy;
import pl.com.bottega.ecommerce.sales.domain.offer.Offer;
import pl.com.bottega.ecommerce.sales.domain.offer.OfferItem;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class Reservation extends BaseAggregateRoot {

    public enum ReservationStatus {
        OPENED,
        CLOSED
    }

    private ReservationStatus status;

    private List<ReservationItem> items;

    private ClientData clientData;

    private Date createDate;

    @SuppressWarnings("unused")
    private Reservation() {}

    public Reservation(Id aggregateId, ReservationStatus status, ClientData clientData, Date createDate) {
        this.id = aggregateId;
        this.status = status;
        this.clientData = clientData;
        this.createDate = createDate;
        this.items = new ArrayList<>();
    }

    public void add(Product product, int quantity) {
        if (isClosed()) {
            domainError("Reservation already closed");
        }
        if (!product.isAvailable()) {
            domainError("Product is no longer available");
        }

        if (contains(product)) {
            increase(product, quantity);
        } else {
            addNew(product, quantity);
        }
    }

    /**
     * Sample function closured by policy </br>
     * Higher order function closured by policy function</br>
     * </br>
     * Function loads current prices, and prepares offer according to the current availability and given discount
     * 
     * @param discountPolicy
     * @return
     */
    public Offer calculateOffer(DiscountPolicy discountPolicy) {
        List<OfferItem> availabeItems = new ArrayList<>();
        List<OfferItem> unavailableItems = new ArrayList<>();

        for (ReservationItem item : items) {
            if (item.getProduct()
                    .isAvailable()) {
                Discount discount = discountPolicy.applyDiscount(item.getProduct(), item.getQuantity(), item.getProduct()
                                                                                                            .getPrice());
                OfferItem offerItem = new OfferItem(item.getProduct()
                                                        .generateSnapshot(),
                        item.getQuantity(), discount);

                availabeItems.add(offerItem);
            } else {
                OfferItem offerItem = new OfferItem(item.getProduct()
                                                        .generateSnapshot(),
                        item.getQuantity());

                unavailableItems.add(offerItem);
            }
        }

        return new Offer(availabeItems, unavailableItems);
    }

    private void addNew(Product product, int quantity) {
        ReservationItem item = new ReservationItem(product, quantity);
        items.add(item);
    }

    private void increase(Product product, int quantity) {
        for (ReservationItem item : items) {
            if (item.getProduct()
                    .equals(product)) {
                item.changeQuantityBy(quantity);
                break;
            }
        }
    }

    public boolean contains(Product product) {
        for (ReservationItem item : items) {
            if (item.getProduct()
                    .equals(product)) {
                return true;
            }
        }
        return false;
    }

    public boolean isClosed() {
        return status.equals(ReservationStatus.CLOSED);
    }

    public void close() {
        if (isClosed()) {
            domainError("Reservation is already closed");
        }
        status = ReservationStatus.CLOSED;
    }

    public List<ReservedProduct> getReservedProducts() {
        ArrayList<ReservedProduct> result = new ArrayList<>(items.size());

        for (ReservationItem item : items) {
            result.add(new ReservedProduct(item.getProduct()
                                               .getId(),
                    item.getProduct()
                        .getName(),
                    item.getQuantity(), calculateItemCost(item)));
        }

        return result;
    }

    private Money calculateItemCost(ReservationItem item) {
        return item.getProduct()
                   .getPrice()
                   .multiplyBy(item.getQuantity());
    }

    public ClientData getClientData() {
        return clientData;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}

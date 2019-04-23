package pl.com.bottega.ecommerce.builders;

import java.util.ArrayList;
import java.util.List;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.invoicing.InvoiceRequest;
import pl.com.bottega.ecommerce.sales.domain.invoicing.RequestItem;

public class InvoiceRequestBuilder {

    private List<RequestItem> items = new ArrayList<>();
    private ClientData clientData = new ClientData(Id.generate(), "no_name");

    public InvoiceRequestBuilder() {};

    public InvoiceRequestBuilder clientData(ClientData clientData) {
        this.clientData = clientData;
        return this;
    }

    public InvoiceRequestBuilder items(List<RequestItem> items) {
        this.items = items;
        return this;
    }

    public InvoiceRequest build() {
        InvoiceRequest request = new InvoiceRequest(clientData);
        for (RequestItem item : items) {
            request.add(item);
        }
        return request;
    }
}

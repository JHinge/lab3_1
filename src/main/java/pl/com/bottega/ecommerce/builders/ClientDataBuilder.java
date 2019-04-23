package pl.com.bottega.ecommerce.builders;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;

public class ClientDataBuilder {

    private Id id = Id.generate();
    private String name = "no_name";

    public ClientDataBuilder() {};

    public ClientDataBuilder id(Id id) {
        this.id = id;
        return this;
    }

    public ClientDataBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ClientData build() {
        return new ClientData(id, name);
    }

}

package toolshop.api;

import com.fasterxml.jackson.databind.JsonNode;

public final class ProductApi {

    private final Http http = new Http();

    public Http.Response page(int number) {
        return http.get("/products?page=" + number, null);
    }

    public JsonNode search(String query) {
        return http.get("/products/search?q=" + query.replace(" ", "%20"), null).body();
    }
}

package toolshop.api;

import java.util.Map;

public final class BrandApi {

    private final Http http = new Http();

    /** Создание бренда. Токен передаётся отдельно: запрос без него — отдельный сценарий. */
    public Http.Response create(String name, String slug, String token) {
        return http.post("/brands", Map.of("name", name, "slug", slug), token);
    }
}

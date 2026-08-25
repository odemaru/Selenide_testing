package toolshop.data;

import com.fasterxml.jackson.databind.JsonNode;
import toolshop.api.ProductApi;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Справочник товаров стенда.
 *
 * Идентификаторы в тест не зашиваются: демо-стенд периодически пересоздаёт
 * базу и меняет все ULID разом. Это не предположение — на прежнем
 * идентификаторе страница товара открывалась пустой. Названия при этом
 * сохраняются, поэтому товар ищется по названию через API.
 *
 * По той же причине тесты не рассчитывают на точное число товаров: создание
 * товара открыто без авторизации, и каталог пополняется чужими записями.
 */
public final class Products {

    public static final String IN_STOCK = "Combination Pliers";

    private static final ProductApi API = new ProductApi();
    private static final int MAX_PAGES = 20;

    /**
     * Идентификатор запрашивается заново на каждый вызов, без кэша.
     *
     * Кэш здесь выглядит очевидной экономией и оказывается ловушкой: стенд
     * пересоздаёт базу и меняет все ULID разом, причём делает это когда
     * угодно — за время работы над проектом это случилось дважды. Запомненный
     * идентификатор после такого превращается в ссылку на пустую страницу,
     * и разом падают все тесты, которые открывают карточку товара. Лишний
     * запрос к каталогу стоит трети секунды и такой размен окупает.
     */
    public static String idOf(String name) {
        return find(product -> name.equals(product.path("name").asText()))
                .map(product -> product.path("id").asText())
                .orElseThrow(() -> new IllegalStateException(
                        "На стенде нет товара с названием «" + name + "»"));
    }

    /** Любой товар не в наличии: конкретный набор таких товаров меняется. */
    public static String anyOutOfStockName() {
        return find(product -> !product.path("in_stock").asBoolean(true))
                .map(product -> product.path("name").asText())
                .orElseThrow(() -> new IllegalStateException(
                        "На стенде не нашлось ни одного товара не в наличии"));
    }

    private static Optional<JsonNode> find(Predicate<JsonNode> match) {
        for (int page = 1; page <= MAX_PAGES; page++) {
            JsonNode body = API.page(page).body();
            JsonNode items = body.path("data");
            if (!items.isArray() || items.isEmpty()) {
                return Optional.empty();
            }
            for (JsonNode product : items) {
                if (match.test(product)) {
                    return Optional.of(product);
                }
            }
            if (page >= body.path("last_page").asInt(MAX_PAGES)) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Products() {
    }
}

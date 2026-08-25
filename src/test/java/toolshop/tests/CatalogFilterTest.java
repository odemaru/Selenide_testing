package toolshop.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import toolshop.data.Pairwise;
import toolshop.pages.HomePage;
import toolshop.setup.UiTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Фильтры каталога, попарное покрытие.
 *
 * Четыре параметра дают 24 сочетания, и гонять их через браузер целиком
 * незачем: дефекты фильтрации проявляются на взаимодействии двух условий.
 * Набор строится {@link Pairwise} и покрывает все пары значений.
 *
 * Проверяется не состав выдачи, а её порядок. Состав зависит от данных
 * стенда, а товары на нём заводятся кем угодно без авторизации, так что
 * ожидать конкретные названия нельзя. Порядок же обязан держаться при любом
 * сочетании фильтров, и это свойство проверяемо.
 */
@Feature("Каталог")
class CatalogFilterTest extends UiTest {

    private final HomePage home = new HomePage();

    static Stream<Arguments> filterCombinations() {
        List<List<Object>> parameters = List.of(
                List.of("Hammer", "Pliers", "Drill"),
                List.of("ForgeFlex Tools", "MightyCraft Hardware"),
                List.of("price,asc", "price,desc"),
                List.of(true, false));

        return Pairwise.combinations(parameters).stream().map(Arguments::of);
    }

    @ParameterizedTest(name = "{0} + {1} + {2} + эко={3}")
    @MethodSource("filterCombinations")
    @TmsLink("ТК-10")
    @DisplayName("Порядок по цене держится при любом сочетании фильтров")
    void sortingHoldsUnderAnyFilterCombination(String category, String brand, String sort, boolean eco) {
        home.openPage()
                .filterByCategory(category)
                .filterByBrand(brand)
                .filterEcoFriendly(eco)
                .sortBy(sort);

        List<BigDecimal> prices = home.productPrices().texts().stream()
                .map(text -> new BigDecimal(text.replace("$", "").replace(",", "").trim()))
                .toList();

        if (prices.size() < 2) {
            return;
        }
        assertThat(prices)
                .as("Цены при сортировке %s", sort)
                .isSortedAccordingTo(sort.endsWith("asc") ? BigDecimal::compareTo : (a, b) -> b.compareTo(a));
    }
}

package toolshop.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import toolshop.data.Products;
import toolshop.pages.HomePage;
import toolshop.setup.UiTest;

import java.util.stream.Stream;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.CollectionCondition.itemWithText;

@Feature("Каталог")
class CatalogSearchTest extends UiTest {

    private final HomePage home = new HomePage();

    @Test
    @TmsLink("ТК-07")
    @DisplayName("Поиск находит товар по названию")
    void searchFindsProductByName() {
        home.openPage().search(Products.IN_STOCK);

        home.productNames().shouldHave(itemWithText(Products.IN_STOCK));
    }

    @Test
    @TmsLink("ТК-08")
    @DisplayName("Поиск по отсутствующему названию не даёт результатов")
    void searchWithoutMatchesReturnsNothing() {
        home.openPage().search("отвёртка-которой-нет-2026");

        home.productNames().shouldHave(size(0));
    }

    /**
     * Предугадывание ошибок: строки, на которых обычно ломается выдача.
     *
     * Проверяется не текст ответа, а то, что страница остаётся работоспособной
     * и поиск можно повторить. Скрипт, попавший в разметку, или пустой экран
     * после эмодзи проявились бы именно здесь.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("hostileQueries")
    @TmsLink("ТК-09")
    @DisplayName("Витрина переживает недружелюбный запрос")
    void catalogSurvivesHostileQuery(String description, String query) {
        home.openPage().search(query);

        home.resetSearch();
        home.productNames().shouldHave(sizeGreaterThan(0));
    }

    static Stream<Arguments> hostileQueries() {
        return Stream.of(
                Arguments.of("скрипт", "<script>alert(1)</script>"),
                Arguments.of("кавычка и запрос", "' OR 1=1 --"),
                Arguments.of("эмодзи", "🔨🔧"),
                Arguments.of("длинная строка", "a".repeat(500)));
    }
}

package toolshop.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import toolshop.pages.HomePage;
import toolshop.setup.UiTest;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;

@Feature("Витрина")
class SmokeTest extends UiTest {

    private final HomePage home = new HomePage();

    @Test
    @TmsLink("ТК-22")
    @DisplayName("Витрина открывается и показывает товары")
    void catalogShowsProducts() {
        home.openPage();

        home.productNames().shouldHave(sizeGreaterThan(0));
    }
}

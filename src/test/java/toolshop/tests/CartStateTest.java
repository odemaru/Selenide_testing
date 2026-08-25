package toolshop.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import toolshop.data.Products;
import toolshop.pages.CheckoutPage;
import toolshop.pages.ProductPage;
import toolshop.setup.UiTest;

import java.math.BigDecimal;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

/**
 * Корзина как набор переходов между состояниями.
 *
 * Состояния: пусто, есть товар, количество изменено. Каждый тест проверяет
 * один переход, включая обратный — удаление возвращает корзину в исходное
 * состояние. Диаграмма приведена в docs/test-design.md.
 *
 * Корзина живёт в sessionStorage, поэтому между тестами она чистится вместе
 * с остальным состоянием браузера в {@link UiTest}.
 */
@Feature("Корзина")
class CartStateTest extends UiTest {

    private final ProductPage product = new ProductPage();
    private final CheckoutPage checkout = new CheckoutPage();

    @Test
    @TmsLink("ТК-11")
    @DisplayName("Пусто → есть товар: счётчик показывает единицу")
    void addingProductFillsCart() {
        product.openById(Products.idOf(Products.IN_STOCK)).addToCart();

        product.header.cartQuantity().shouldBe(visible).shouldHave(exactText("1"));
    }

    @Test
    @TmsLink("ТК-12")
    @DisplayName("Повторное добавление увеличивает количество, а не создаёт строку")
    void addingSameProductTwiceKeepsOneRow() {
        product.openById(Products.idOf(Products.IN_STOCK)).addToCart().addToCart();
        product.header.cartQuantity().shouldHave(exactText("2"));

        checkout.openPage();

        checkout.items().shouldHave(size(1));
    }

    @Test
    @TmsLink("ТК-13")
    @DisplayName("Изменение количества пересчитывает стоимость строки")
    void changingQuantityRecalculatesLinePrice() {
        product.openById(Products.idOf(Products.IN_STOCK)).addToCart();
        checkout.openPage();
        checkout.linePrice(Products.IN_STOCK).shouldBe(visible);

        checkout.setQuantity(Products.IN_STOCK, "3");

        checkout.linePrice(Products.IN_STOCK).shouldHave(text(expectedTotalFor(3)));
    }

    @Test
    @TmsLink("ТК-14")
    @DisplayName("Есть товар → пусто: удаление очищает корзину")
    void removingProductEmptiesCart() {
        product.openById(Products.idOf(Products.IN_STOCK)).addToCart();
        checkout.openPage();
        checkout.items().shouldHave(size(1));

        checkout.removeItem(Products.IN_STOCK);

        checkout.items().shouldHave(size(0));
    }

    /** Цена берётся из строки корзины, а не из константы: стенд её меняет. */
    private String expectedTotalFor(int quantity) {
        String unit = checkout.unitPrice(Products.IN_STOCK).getText().replace("$", "").trim();
        return new BigDecimal(unit).multiply(BigDecimal.valueOf(quantity)).toPlainString();
    }
}

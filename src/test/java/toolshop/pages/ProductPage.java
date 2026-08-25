package toolshop.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import toolshop.pages.component.Header;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;
import static toolshop.pages.DataTest.$test;

public class ProductPage {

    public final Header header = new Header();

    public ProductPage openById(String productId) {
        open("/product/" + productId);
        name().shouldBe(visible);
        return this;
    }

    public SelenideElement name() {
        return $test("product-name");
    }

    public SelenideElement price() {
        return $test("unit-price");
    }

    public SelenideElement quantity() {
        return $test("quantity");
    }

    public SelenideElement outOfStockLabel() {
        return $test("out-of-stock");
    }

    @Step("Задать количество: {value}")
    public ProductPage setQuantity(String value) {
        quantity().setValue(value);
        return this;
    }

    @Step("Увеличить количество")
    public ProductPage increaseQuantity() {
        $test("increase-quantity").click();
        return this;
    }

    @Step("Уменьшить количество")
    public ProductPage decreaseQuantity() {
        $test("decrease-quantity").click();
        return this;
    }

    /**
     * Добавление в корзину асинхронно: магазин отправляет запрос и только
     * потом обновляет счётчик. Без ожидания переход на страницу корзины
     * обрывает запрос на полпути, и корзина открывается пустой — проверено,
     * тесты падали именно так. Поэтому метод ждёт, пока счётчик вырастет.
     */
    @Step("Добавить в корзину")
    public ProductPage addToCart() {
        int before = cartQuantityOrZero();
        $test("add-to-cart").click();
        header.cartQuantity().shouldHave(exactText(String.valueOf(before + 1)));
        return this;
    }

    private int cartQuantityOrZero() {
        SelenideElement badge = header.cartQuantity();
        return badge.exists() ? Integer.parseInt(badge.getText().trim()) : 0;
    }

    public SelenideElement addToCartButton() {
        return $test("add-to-cart");
    }

    public SelenideElement addToFavoritesButton() {
        return $test("add-to-favorites");
    }
}

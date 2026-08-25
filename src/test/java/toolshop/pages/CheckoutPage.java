package toolshop.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import toolshop.api.model.Address;
import toolshop.data.PaymentMethod;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static toolshop.pages.DataTest.$test;
import static toolshop.pages.DataTest.css;

/**
 * Оформление заказа: корзина, вход, адрес, оплата.
 *
 * Все четыре шага живут в одной странице и переключаются без перезагрузки,
 * поэтому и здесь это один объект, а не четыре: разделение повторяло бы
 * структуру кода приложения, а не то, что делает пользователь.
 *
 * Два контрола магазин оставил без data-test — вкладка «Continue as Guest»
 * и кнопка удаления строки из корзины. Они ищутся по подписи и по классу,
 * и это единственные такие места; см. раздел про селекторы в README.
 */
public class CheckoutPage {

    public CheckoutPage openPage() {
        open("/checkout");
        return this;
    }

    // --- шаг 1, корзина -----------------------------------------------------

    public ElementsCollection items() {
        return $$(css("product-title"));
    }

    public SelenideElement quantityField(String product) {
        return row(product).$(css("product-quantity"));
    }

    public SelenideElement unitPrice(String product) {
        return row(product).$(css("product-price"));
    }

    public SelenideElement linePrice(String product) {
        return row(product).$(css("line-price"));
    }

    public SelenideElement total() {
        return $test("cart-total");
    }

    /**
     * Магазин пересчитывает строку по событию input, которое Selenide шлёт
     * при вводе. Нажатие Enter сверх этого сбрасывало правку обратно
     * к прежнему количеству, поэтому его здесь нет.
     */
    @Step("Задать количество «{product}»: {quantity}")
    public CheckoutPage setQuantity(String product, String quantity) {
        quantityField(product).setValue(quantity);
        return this;
    }

    @Step("Удалить «{product}» из корзины")
    public CheckoutPage removeItem(String product) {
        row(product).$("a.btn-danger").click();
        return this;
    }

    @Step("Перейти к оформлению")
    public CheckoutPage proceedFromCart() {
        $test("proceed-1").click();
        return this;
    }

    // --- шаг 2, вход --------------------------------------------------------

    @Step("Войти как {email}")
    public CheckoutPage loginAs(String email, String password) {
        $test("email").shouldBe(visible).setValue(email);
        $test("password").setValue(password);
        $test("login-submit").click();
        $test("proceed-2").shouldBe(visible).click();
        return this;
    }

    @Step("Продолжить как гость")
    public CheckoutPage continueAsGuest(String email, String firstName, String lastName) {
        $$("a.nav-link").findBy(exactText("Continue as Guest")).click();
        $test("guest-email").shouldBe(visible).setValue(email);
        $test("guest-first-name").setValue(firstName);
        $test("guest-last-name").setValue(lastName);
        $test("guest-submit").click();
        $test("proceed-2-guest").shouldBe(visible).click();
        return this;
    }

    // --- шаг 3, адрес -------------------------------------------------------

    @Step("Заполнить адрес доставки")
    public CheckoutPage fillBilling(Address address) {
        $test("street").shouldBe(visible).setValue(address.street());
        $test("house_number").setValue(address.houseNumber());
        $test("postal_code").setValue(address.postalCode());
        $test("city").setValue(address.city());
        $test("state").setValue(address.state());
        $test("country").selectOptionByValue(address.country());
        // Кнопка перехода включается только после того, как Angular признает
        // форму валидной. Клик раньше этого момента проходит вхолостую,
        // и шаг оплаты не открывается.
        $test("proceed-3").shouldBe(enabled).click();
        return this;
    }

    // --- шаг 4, оплата ------------------------------------------------------

    @Step("Выбрать способ оплаты: {method}")
    public CheckoutPage choosePayment(PaymentMethod method) {
        $test("payment-method").shouldBe(visible).selectOptionByValue(method.value());
        return this;
    }

    @Step("Заполнить реквизиты оплаты")
    public CheckoutPage fillPaymentDetails(PaymentMethod method) {
        method.details().forEach((field, value) -> {
            SelenideElement input = $test(field).shouldBe(visible);
            if ("select".equals(input.getTagName())) {
                input.selectOptionByValue(value);
            } else {
                input.setValue(value);
            }
        });
        return this;
    }

    @Step("Подтвердить заказ")
    public CheckoutPage confirm() {
        $test("finish").click();
        return this;
    }

    public SelenideElement confirmButton() {
        return $test("finish");
    }

    public SelenideElement paymentSuccessMessage() {
        return $test("payment-success-message");
    }

    public SelenideElement paymentMethodSelect() {
        return $test("payment-method");
    }

    private SelenideElement row(String product) {
        return $$("tbody tr").findBy(text(product));
    }
}

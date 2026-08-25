package toolshop.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import toolshop.api.model.Address;
import toolshop.data.PaymentMethod;

import static com.codeborne.selenide.Condition.attributeMatching;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
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
     * Количество задаётся установкой значения с событием input, а не набором
     * с клавиатуры.
     *
     * Поле объявлено как input type=number, и до Angular нажатия не долетают:
     * в CI после setValue поле оставалось ng-pristine со старым значением,
     * то есть приложение ввода не заметило вовсе, хотя фокус получало.
     * Известная беда числовых полей в WebDriver; сам Selenide в режиме
     * fastSetValue поступает так же. Событие input магазин слушает —
     * проверено вручную, строка пересчитывается именно по нему.
     *
     * Принятое значение проверяется здесь же, чтобы тест не пошёл дальше
     * с молча потерянным вводом.
     */
    @Step("Задать количество «{product}»: {quantity}")
    public CheckoutPage setQuantity(String product, String quantity) {
        SelenideElement field = quantityField(product);
        field.shouldHave(attributeMatching("value", "\\d+"));
        Selenide.executeJavaScript(
                "const field = arguments[0];"
                        + "Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')"
                        + "  .set.call(field, arguments[1]);"
                        + "field.dispatchEvent(new Event('input', {bubbles: true}));"
                        + "field.dispatchEvent(new Event('change', {bubbles: true}));",
                field, quantity);
        field.shouldHave(value(quantity));
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

    /**
     * Каждое поле проверяется на принятое значение до перехода дальше.
     *
     * Ввод в этом приложении иногда не долетает до Angular — то же, что
     * с полем количества. Без проверки форма остаётся невалидной, кнопка
     * перехода не включается, и тест падает уже на шаге оплаты, где причину
     * не видно. С проверкой падение приходится на конкретное поле.
     */
    @Step("Заполнить адрес доставки")
    public CheckoutPage fillBilling(Address address) {
        $test("street").shouldBe(visible);
        fill("street", address.street());
        fill("house_number", address.houseNumber());
        fill("postal_code", address.postalCode());
        fill("city", address.city());
        fill("state", address.state());
        $test("country").selectOptionByValue(address.country());

        // Кнопка включается только после того, как Angular признает форму
        // валидной; клик раньше проходит вхолостую и шаг оплаты не открывается.
        $test("proceed-3").shouldBe(enabled).click();
        $test("payment-method").shouldBe(visible);
        return this;
    }

    private void fill(String field, String text) {
        SelenideElement input = $test(field);
        input.setValue(text);
        if (!text.equals(input.getValue())) {
            input.setValue(text);
        }
        input.shouldHave(value(text));
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

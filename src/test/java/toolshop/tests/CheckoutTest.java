package toolshop.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import toolshop.api.model.Address;
import toolshop.data.PaymentMethod;
import toolshop.data.Products;
import toolshop.data.Users;
import toolshop.pages.CheckoutPage;
import toolshop.pages.ProductPage;
import toolshop.setup.UiTest;

import static com.codeborne.selenide.Condition.disabled;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;

/**
 * Оформление заказа как таблица решений.
 *
 * Условия — выбран ли способ оплаты и заполнены ли его поля; действие —
 * доступна ли кнопка подтверждения:
 *
 * <pre>
 *   способ не выбран          -> кнопка заблокирована
 *   выбран, поля пусты        -> кнопка заблокирована
 *   выбран, полей нет вовсе   -> кнопка доступна
 *   выбран, поля заполнены    -> кнопка доступна, платёж принимается
 * </pre>
 *
 * Проверяется именно состояние кнопки, а не сообщение об ошибке: магазин
 * ничего не пишет о незаполненных реквизитах и просто держит подтверждение
 * заблокированным. Набор полей у каждого способа свой и описан
 * в {@link PaymentMethod}, поэтому строки таблицы перебираются перечислением —
 * новый способ оплаты попадёт в прогон сам.
 *
 * Заказ оформляется гостем: вход проверяется в LoginTest, и повторять его
 * здесь значит удлинить каждую строку таблицы ради уже проверенного шага.
 */
@Feature("Оформление заказа")
class CheckoutTest extends UiTest {

    private final ProductPage product = new ProductPage();
    private final CheckoutPage checkout = new CheckoutPage();

    @ParameterizedTest(name = "{0}")
    @EnumSource(PaymentMethod.class)
    @TmsLink("ТК-15")
    @DisplayName("Оплата проходит, когда реквизиты заполнены")
    void paymentSucceedsWithCompleteDetails(PaymentMethod method) {
        startCheckoutAsGuest();

        checkout.choosePayment(method).fillPaymentDetails(method).confirm();

        checkout.paymentSuccessMessage().shouldBe(visible);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = PaymentMethod.class, names = {"CASH_ON_DELIVERY", "CREDIT_CARD"},
            mode = EnumSource.Mode.EXCLUDE)
    @TmsLink("ТК-16")
    @DisplayName("С пустыми реквизитами подтверждение недоступно")
    void confirmStaysDisabledWithoutDetails(PaymentMethod method) {
        // Кредитная карта сюда не входит: у её полей нет проверки на
        // обязательность, и подтверждение остаётся доступным. Оформлено
        // как BUG-8 в KnownBugsTest.
        startCheckoutAsGuest();

        checkout.choosePayment(method);

        checkout.confirmButton().shouldBe(disabled);
    }

    @Test
    @TmsLink("ТК-17")
    @DisplayName("Без выбранного способа оплаты подтверждение недоступно")
    void confirmStaysDisabledWithoutPaymentMethod() {
        startCheckoutAsGuest();

        checkout.paymentMethodSelect().shouldBe(visible);

        checkout.confirmButton().shouldBe(disabled);
    }

    /** Способ без дополнительных полей не требует ничего заполнять. */
    @Test
    @TmsLink("ТК-18")
    @DisplayName("Оплата при получении не требует реквизитов")
    void cashOnDeliveryNeedsNoDetails() {
        startCheckoutAsGuest();

        checkout.choosePayment(PaymentMethod.CASH_ON_DELIVERY);

        checkout.confirmButton().shouldBe(enabled);
    }

    private void startCheckoutAsGuest() {
        product.openById(Products.idOf(Products.IN_STOCK)).addToCart();
        checkout.openPage()
                .proceedFromCart()
                .continueAsGuest(Users.uniqueEmail(), "Test", "Guest")
                .fillBilling(new Address("Main Street", "1", "Riga", "Riga", "LV", "LV1001"));
    }
}

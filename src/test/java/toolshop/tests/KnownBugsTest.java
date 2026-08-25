package toolshop.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import toolshop.api.BrandApi;
import toolshop.api.Http;
import toolshop.api.model.Address;
import toolshop.data.Limits;
import toolshop.data.PaymentMethod;
import toolshop.data.Products;
import toolshop.data.Users;
import toolshop.pages.CheckoutPage;
import toolshop.pages.ProductPage;
import toolshop.pages.RegisterPage;
import toolshop.setup.KnownBug;
import toolshop.setup.UiTest;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.disabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.title;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Найденные дефекты магазина, оформленные исполняемым кодом.
 *
 * Каждый тест описывает поведение, которого следует ожидать, и помечен
 * {@link KnownBug} со ссылкой на пункт docs/bug-report.md. Пока дефект
 * воспроизводится, проверка падает и тест засчитывается, поэтому прогон
 * остаётся зелёным на текущем состоянии стенда. Если дефект исправят,
 * проверка пройдёт — и вот это уронит сборку, чтобы исправление
 * не осталось незамеченным.
 *
 * Приём перенесён из моего проекта по API 2GIS, где ту же роль играет
 * pytest-овский xfail(strict=True). В JUnit готового аналога нет, он собран
 * в {@link toolshop.setup.KnownBugExtension}.
 */
@Feature("Найденные дефекты")
class KnownBugsTest extends UiTest {

    private final RegisterPage register = new RegisterPage();
    private final CheckoutPage checkout = new CheckoutPage();
    private final ProductPage product = new ProductPage();

    @Test
    @KnownBug("BUG-1")
    @Issue("BUG-1")
    @DisplayName("Пароль допустимой длины принимается формой")
    void passwordOfClaimedMinimumLengthIsAccepted() {
        register.openPage()
                .type("password", "Aa1!bc".substring(0, Limits.PASSWORD_MIN_CLAIMED))
                .submit();

        assertThat(register.error("password").getText())
                .as("Сообщение при пароле длиной %d", Limits.PASSWORD_MIN_CLAIMED)
                .doesNotContain("must be minimal");
    }

    @Test
    @KnownBug("BUG-2")
    @Issue("BUG-2")
    @DisplayName("В списке стран есть Афганистан")
    void countryListContainsAfghanistan() {
        register.openPage();

        assertThat(register.countryOptions().texts())
                .as("Список стран формы регистрации")
                .contains("Afghanistan");
    }

    @Test
    @KnownBug("BUG-3")
    @Issue("BUG-3")
    @DisplayName("Страны с диакритикой стоят на своём месте в алфавите")
    void countryListIgnoresDiacriticsWhenSorting() {
        register.openPage();
        List<String> countries = register.countryOptions().texts();

        assertThat(countries.indexOf("Åland Islands"))
                .as("«Åland Islands» должно идти до «Albania»: Ala меньше Alb")
                .isLessThan(countries.indexOf("Albania"));
    }

    @Test
    @KnownBug("BUG-4")
    @Issue("BUG-4")
    @DisplayName("Версия в подвале совпадает с версией в заголовке")
    void footerVersionMatchesTitle() {
        register.openPage();

        assertThat($("footer").getText())
                .as("Подвал страницы")
                .contains(versionFromTitle());
    }

    /**
     * Создание бренда без токена должно отклоняться.
     *
     * Проверка обращается к API напрямую: в интерфейсе такой формы нет, а
     * последствие видно на витрине — в фильтре брендов лежат чужие записи
     * «some name». Запрос заводит на стенде ещё один бренд, поэтому имя
     * сделано опознаваемым.
     */
    @Test
    @KnownBug("BUG-5")
    @Issue("BUG-5")
    @DisplayName("Создание бренда без авторизации отклоняется")
    void anonymousBrandCreationIsRejected() {
        String marker = "qa-probe-" + System.currentTimeMillis();

        Http.Response response = new BrandApi().create(marker, marker, null);

        assertThat(response.status())
                .as("Ответ на POST /brands без токена")
                .isEqualTo(401);
    }

    @Test
    @KnownBug("BUG-6")
    @Issue("BUG-6")
    @DisplayName("Пустой пароль не обвиняют в недопустимых символах")
    void emptyPasswordIsNotBlamedForInvalidCharacters() {
        register.openPage().submit();

        assertThat(register.error("password").shouldBe(visible).getText())
                .as("Сообщение при пустом пароле")
                .doesNotContain("invalid characters");
    }

    @Test
    @KnownBug("BUG-7")
    @Issue("BUG-7")
    @DisplayName("Пустая дата рождения даёт одно сообщение, а не два")
    void emptyDateOfBirthReportsSingleMessage() {
        register.openPage().submit();

        assertThat(register.error("dob").shouldBe(visible).getText().lines().count())
                .as("Число сообщений у пустой даты рождения")
                .isEqualTo(1);
    }

    /**
     * У остальных способов оплаты пустые реквизиты блокируют подтверждение.
     * У кредитной карты проверка формата есть, а проверки на обязательность
     * нет, поэтому пустое поле формат не нарушает и форма считается верной.
     */
    @Test
    @KnownBug("BUG-8")
    @Issue("BUG-8")
    @DisplayName("Пустые реквизиты карты блокируют подтверждение заказа")
    void emptyCreditCardBlocksConfirmation() {
        product.openById(Products.idOf(Products.IN_STOCK)).addToCart();
        checkout.openPage()
                .proceedFromCart()
                .continueAsGuest(Users.uniqueEmail(), "Test", "Guest")
                .fillBilling(new Address("Main Street", "1", "Riga", "Riga", "LV", "LV1001"))
                .choosePayment(PaymentMethod.CREDIT_CARD);

        checkout.confirmButton().shouldBe(disabled);
    }

    /** Заголовок вкладки заканчивается версией магазина, например «v5.0». */
    private String versionFromTitle() {
        Matcher version = Pattern.compile("v\\d+\\.\\d+").matcher(title());
        if (!version.find()) {
            throw new IllegalStateException("В заголовке страницы нет версии: " + title());
        }
        return version.group();
    }
}

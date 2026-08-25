package toolshop.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import toolshop.data.Users;
import toolshop.pages.LoginPage;
import toolshop.setup.UiTest;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

/**
 * Вход в магазин.
 *
 * Пользователь заводится через API: форма регистрации проверяется отдельно,
 * и повторять её здесь значит уронить тест входа из-за чужого дефекта.
 */
@Feature("Авторизация")
class LoginTest extends UiTest {

    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final LoginPage login = new LoginPage();

    @Test
    @TmsLink("ТК-01")
    @DisplayName("Вход с верными данными открывает личный кабинет")
    void loginWithValidCredentials() {
        Users.Credentials user = Users.registered();

        login.openPage().login(user.email(), user.password());

        webdriver().shouldHave(urlContaining("/account"));
    }

    @Test
    @TmsLink("ТК-02")
    @DisplayName("Неверный пароль отклоняется")
    void loginWithWrongPassword() {
        Users.Credentials user = Users.registered();

        login.openPage().login(user.email(), "Wrong" + user.password());

        login.error().shouldBe(visible).shouldHave(text(INVALID_CREDENTIALS));
    }

    /**
     * Незарегистрированный адрес и неверный пароль должны давать один и тот же
     * ответ. Разные сообщения позволили бы перебором узнать, какие адреса
     * заведены в магазине.
     */
    @Test
    @TmsLink("ТК-03")
    @DisplayName("Незнакомый адрес даёт то же сообщение, что и неверный пароль")
    void loginWithUnknownEmailDoesNotRevealAccounts() {
        login.openPage().login(Users.uniqueEmail(), Users.randomPassword());

        login.error().shouldBe(visible).shouldHave(text(INVALID_CREDENTIALS));
    }
}

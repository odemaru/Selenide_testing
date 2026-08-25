package toolshop.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;
import static toolshop.pages.DataTest.$test;

public class LoginPage {

    public LoginPage openPage() {
        open("/auth/login");
        $test("login-submit").shouldBe(visible);
        return this;
    }

    @Step("Войти как {email}")
    public LoginPage login(String email, String password) {
        $test("email").setValue(email);
        $test("password").setValue(password);
        $test("login-submit").click();
        return this;
    }

    public SelenideElement error() {
        return $test("login-error");
    }

    public SelenideElement emailError() {
        return $test("email-error");
    }

    public SelenideElement passwordError() {
        return $test("password-error");
    }
}

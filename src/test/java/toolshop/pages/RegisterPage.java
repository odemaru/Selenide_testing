package toolshop.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import toolshop.api.model.NewUser;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;
import static toolshop.pages.DataTest.$$test;
import static toolshop.pages.DataTest.$test;

/**
 * Форма регистрации.
 *
 * Сообщения об ошибках лежат в элементах с именем «имя поля + -error», что
 * позволяет спрашивать ошибку по имени поля и не заводить отдельный метод
 * на каждое из двенадцати.
 */
public class RegisterPage {

    public RegisterPage openPage() {
        open("/auth/register");
        $test("register-submit").shouldBe(visible);
        return this;
    }

    @Step("Заполнить форму регистрации")
    public RegisterPage fill(NewUser user) {
        $test("first-name").setValue(user.firstName());
        $test("last-name").setValue(user.lastName());
        $test("dob").setValue(user.dob());
        $test("street").setValue(user.address().street());
        $test("house_number").setValue(user.address().houseNumber());
        $test("postal_code").setValue(user.address().postalCode());
        $test("city").setValue(user.address().city());
        $test("state").setValue(user.address().state());
        $test("country").selectOptionByValue(user.address().country());
        $test("phone").setValue(user.phone());
        $test("email").setValue(user.email());
        $test("password").setValue(user.password());
        return this;
    }

    @Step("Ввести {field}: «{value}»")
    public RegisterPage type(String field, String value) {
        $test(field).setValue(value);
        return this;
    }

    @Step("Отправить форму")
    public RegisterPage submit() {
        $test("register-submit").click();
        return this;
    }

    public SelenideElement error(String field) {
        return $test(field + "-error");
    }

    public ElementsCollection countryOptions() {
        return $$test("country").first().$$("option");
    }

    public SelenideElement submitButton() {
        return $test("register-submit");
    }
}

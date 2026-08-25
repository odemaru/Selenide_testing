package toolshop.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import toolshop.data.Users;
import toolshop.pages.RegisterPage;
import toolshop.setup.UiTest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * Проверка формы регистрации по классам эквивалентности.
 *
 * Адреса и пароли разбиты на классы: внутри класса поведение формы одинаково,
 * поэтому берётся по одному представителю, а не все мыслимые строки.
 * Разбиение описано в docs/test-design.md.
 */
@Feature("Регистрация")
class RegistrationValidationTest extends UiTest {

    private final RegisterPage register = new RegisterPage();

    /** Поле формы и сообщение, которого требует пустое значение. */
    private static Map<String, String> requiredFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("first-name", "First name is required");
        fields.put("last-name", "Last name is required");
        fields.put("dob", "Date of Birth is required");
        fields.put("street", "Street is required");
        fields.put("house_number", "House number is required");
        fields.put("postal_code", "Postcode is required");
        fields.put("city", "City is required");
        fields.put("state", "State is required");
        fields.put("country", "Country is required");
        fields.put("phone", "Phone is required");
        fields.put("email", "Email is required");
        fields.put("password", "Password is required");
        return fields;
    }

    /**
     * Все обязательные поля проверяются одной отправкой формы.
     *
     * Разбивать на двенадцать тестов значит двенадцать раз открыть страницу
     * ради одного и того же действия. Мягкие проверки при этом показывают
     * сразу все расхождения, а не только первое.
     */
    @Test
    @TmsLink("ТК-04")
    @DisplayName("Пустая форма сообщает обо всех обязательных полях")
    void emptyFormReportsEveryRequiredField() {
        register.openPage().submit();

        assertSoftly(softly -> requiredFields().forEach((field, message) ->
                softly.assertThat(register.error(field).shouldBe(visible).getText())
                        .as("Сообщение поля %s", field)
                        .contains(message)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidEmails")
    @TmsLink("ТК-05")
    @DisplayName("Адрес неверного формата отклоняется")
    void invalidEmailIsRejected(String description, String email) {
        register.openPage()
                .type("email", email)
                .type("password", Users.randomPassword())
                .submit();

        register.error("email").shouldBe(visible);
    }

    /** По одному представителю на класс некорректных адресов. */
    private static Stream<org.junit.jupiter.params.provider.Arguments> invalidEmails() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("без собаки", "user.example.com"),
                org.junit.jupiter.params.provider.Arguments.of("без домена", "user@"),
                org.junit.jupiter.params.provider.Arguments.of("без имени", "@example.com"),
                org.junit.jupiter.params.provider.Arguments.of("пробел внутри", "us er@example.com"),
                org.junit.jupiter.params.provider.Arguments.of("две собаки", "user@@example.com"));
    }

    @ParameterizedTest(name = "длина {0}")
    @ValueSource(ints = {1, 5, 7})
    @TmsLink("ТК-06")
    @DisplayName("Пароль короче допустимого отклоняется")
    void shortPasswordIsRejected(int length) {
        register.openPage()
                .type("password", "Aa1!bcdefg".substring(0, length))
                .submit();

        register.error("password").shouldBe(visible).shouldHave(text("Password"));
    }
}

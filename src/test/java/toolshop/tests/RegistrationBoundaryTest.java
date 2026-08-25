package toolshop.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import toolshop.api.Http;
import toolshop.api.UserApi;
import toolshop.api.model.NewUser;
import toolshop.data.Limits;
import toolshop.data.Users;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Границы длины полей регистрации.
 *
 * Проверка идёт через API, а не через форму, и это осознанный выбор. Правило
 * длины живёт на сервере: форма ни одно из этих полей не ограничивает
 * атрибутом maxlength и просто передаёт введённое дальше. Гонять восемь
 * граничных значений через браузер значит потратить полторы минуты на то,
 * что API отвечает за секунду, и вдобавок поставить результат в зависимость
 * от отрисовки. Через UI проверяется другое — что сообщение об ошибке
 * доходит до пользователя, это RegistrationValidationTest.
 *
 * Браузер этим тестам не нужен, поэтому класс не наследует UiTest.
 */
@Feature("Регистрация")
class RegistrationBoundaryTest {

    private final UserApi api = new UserApi();

    static Stream<Arguments> fieldLimits() {
        return Stream.of(
                Arguments.of("firstName", Limits.FIRST_NAME_MAX),
                Arguments.of("lastName", Limits.LAST_NAME_MAX),
                Arguments.of("phone", Limits.PHONE_MAX));
    }

    @ParameterizedTest(name = "{0}, длина {1}")
    @MethodSource("fieldLimits")
    @TmsLink("ТК-19")
    @DisplayName("Значение предельной длины принимается")
    void valueAtMaxLengthIsAccepted(String field, int max) {
        Http.Response response = api.register(withLength(field, max));

        assertThat(response.status())
                .as("Ответ на %s длиной %d: %s", field, max, response.body())
                .isEqualTo(201);
    }

    @ParameterizedTest(name = "{0}, длина {1} + 1")
    @MethodSource("fieldLimits")
    @TmsLink("ТК-20")
    @DisplayName("Значение длиннее предельного отклоняется")
    void valueAboveMaxLengthIsRejected(String field, int max) {
        Http.Response response = api.register(withLength(field, max + 1));

        assertThat(response.status())
                .as("Ответ на %s длиной %d: %s", field, max + 1, response.body())
                .isEqualTo(422);
    }

    @ParameterizedTest(name = "длина {0}")
    @org.junit.jupiter.params.provider.ValueSource(ints = {7, 8})
    @TmsLink("ТК-21")
    @DisplayName("Граница длины пароля проходит по восьми символам")
    void passwordBoundary(int length) {
        String password = Users.randomPassword().substring(0, length);

        Http.Response response = api.register(Users.valid().withPassword(password));

        int expected = length < Limits.PASSWORD_MIN ? 422 : 201;
        assertThat(response.status())
                .as("Ответ на пароль длиной %d: %s", length, response.body())
                .isEqualTo(expected);
    }

    private NewUser withLength(String field, int length) {
        String value = Users.text(length);
        NewUser user = Users.valid();
        return switch (field) {
            case "firstName" -> user.withFirstName(value);
            case "lastName" -> user.withLastName(value);
            case "phone" -> user.withPhone(value);
            default -> throw new IllegalArgumentException("Неизвестное поле: " + field);
        };
    }
}

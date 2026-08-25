package toolshop.data;

import toolshop.api.Http;
import toolshop.api.UserApi;
import toolshop.api.model.Address;
import toolshop.api.model.NewUser;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Фабрика тестовых пользователей.
 *
 * Тесты, которым нужен вошедший пользователь, получают его отсюда, а не
 * заполняют форму регистрации: двенадцать полей ради предусловия делают тест
 * длинным и заставляют его падать по чужой причине. Регистрация проверяется
 * через UI ровно там, где она и есть объект проверки.
 */
public final class Users {

    private static final UserApi API = new UserApi();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong COUNTER = new AtomicLong();

    public record Credentials(String email, String password) {
    }

    public static NewUser valid() {
        return new NewUser(
                "Тест",
                "Пробный",
                uniqueEmail(),
                randomPassword(),
                "0612345678",
                "1990-01-01",
                new Address("Main Street", "1", "Riga", "Riga", "LV", "LV1001"));
    }

    /**
     * Заводит пользователя через API и возвращает данные для входа.
     *
     * Ошибка здесь означает не провал проверки, а сломанное предусловие,
     * поэтому падение оформлено исключением, а не assert: тест должен
     * сообщить, что не смог подготовиться, а не что нашёл дефект.
     */
    public static Credentials registered() {
        NewUser user = valid();
        Http.Response response = API.register(user);
        if (response.status() != 201) {
            throw new IllegalStateException(
                    "Не удалось завести пользователя через API: " + response.status()
                            + " " + response.body());
        }
        return new Credentials(user.email(), user.password());
    }

    public static String uniqueEmail() {
        return "qa.%d.%d@example.com".formatted(System.currentTimeMillis(), COUNTER.incrementAndGet());
    }

    /**
     * Пароль собирается случайным на каждый вызов.
     *
     * Дело не в уникальности: API сверяет пароль с базами утечек и отклоняет
     * подходящий по длине, но известный вроде "Password123". Случайная строка
     * такую проверку проходит всегда.
     */
    public static String randomPassword() {
        String alphabet = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder password = new StringBuilder("Qx7#");
        for (int i = 0; i < 12; i++) {
            password.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return password.toString();
    }

    /** Строка заданной длины из латиницы: нужна для проверки границ полей. */
    public static String text(int length) {
        return "a".repeat(length);
    }

    private Users() {
    }
}

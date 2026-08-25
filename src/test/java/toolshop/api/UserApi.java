package toolshop.api;

import toolshop.api.model.NewUser;

import java.util.Map;

/**
 * Ручки пользователя: регистрация и вход.
 *
 * Клиент только формирует запрос и возвращает ответ. Ни статус, ни тело
 * здесь не проверяются, чтобы тест падал на своём assert, а не внутри
 * подготовки данных.
 */
public final class UserApi {

    private final Http http = new Http();

    public Http.Response register(NewUser user) {
        return http.post("/users/register", user);
    }

    public Http.Response login(String email, String password) {
        return http.post("/users/login", Map.of("email", email, "password", password));
    }

    public Http.Response me(String token) {
        return http.get("/users/me", token);
    }
}
